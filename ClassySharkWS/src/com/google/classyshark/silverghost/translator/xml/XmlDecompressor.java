/*
 * Copyright 2015 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.classyshark.silverghost.translator.xml;

import com.google.common.io.LittleEndianDataInputStream;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Decompresses an Android binary xml file. It is not the same as the original file, but should
 * contain the same data.
 *
 * This code is based on code posted on StackOverflow by Ribo:
 * http://stackoverflow.com/a/4761689/496992
 *
 * It contains minor fixes to optionally support CDATA elements and namespaces.
 *
 * Improvements made using code android code as reference:
 *  - https://android.googlesource.com/platform/frameworks/base/+/master/include/androidfw/
 *  ResourceTypes.h
 *  - https://android.googlesource.com/platform/frameworks/base/+/master/libs/androidfw/
 *  ResourceTypes.cpp
 */
public class XmlDecompressor {
    //Identifiers for XML Chunk Types
    private static final int PACKED_XML_IDENTIFIER = 0x00080003;
    private static final int END_DOC_TAG = 0x0101;
    private static final int START_ELEMENT_TAG = 0x0102;
    private static final int END_ELEMENT_TAG = 0x0103;
    private static final int CDATA_TAG = 0x00100104;
    private static final int ATTRS_MARKER = 0x00140014;

    private static final int RES_XML_RESOURCE_MAP_TYPE = 0x180;
    private static final int RES_XML_FIRST_CHUNK_TYPE = 0x100;

    //Resource Types
    private static final int RES_TYPE_NULL = 0x00;
    private static final int RES_TYPE_REFERENCE = 0x01;
    private static final int RES_TYPE_ATTRIBUTE = 0x02;
    private static final int RES_TYPE_STRING = 0x03;
    private static final int RES_TYPE_FLOAT = 0x04;
    private static final int RES_TYPE_DIMENSION = 0x05;
    private static final int RES_TYPE_FRACTION = 0x06;
    private static final int RES_TYPE_DYNAMIC_REFERENCE = 0x07;
    private static final int RES_TYPE_INT_DEC = 0x10;
    private static final int RES_TYPE_INT_HEX = 0x11;
    private static final int RES_TYPE_INT_BOOLEAN = 0x12;

    //Complex Types
    private static final int COMPLEX_UNIT_SHIFT = 0;
    private static final int COMPLEX_UNIT_MASK = 0xf;
    private static final int COMPLEX_MANTISSA_SHIFT = 8;
    private static final int COMPLEX_MANTISSA_MASK = 0xffffff;
    private static final int COMPLEX_RADIX_SHIFT = 4;
    private static final int COMPLEX_RADIX_MASK = 0x3;
    private static final int COMPLEX_UNIT_FRACTION = 0;
    private static final int COMPLEX_UNIT_FRACTION_PARENT = 1;
    private static final float MANTISSA_MULT = 1.0f / (1 << COMPLEX_MANTISSA_SHIFT);
    private static final  float RADIX_MULTS[] = {
            1.0f * MANTISSA_MULT, 1.0f / (1 << 7)*MANTISSA_MULT,
            1.0f / (1 << 15) * MANTISSA_MULT, 1.0f / (1 << 23)*MANTISSA_MULT
    };

    //Resource Values
    private static final int RES_VALUE_TRUE = 0xffffffff;
    private static final int RES_VALUE_FALSE = 0x00000000;

    //Char array used to fill spaces.
    private static char[] SPACE_FILL = new char[80];

    private static final int IDENT_SIZE = 2;
    private static final int ATTR_IDENT_SIZE = 4;

    private static final String ERROR_INVALID_MAGIC_NUMBER =
            "Invalid packed XML identifier. Expecting 0x%08X, found 0x%08X\n";
    private static final String ERROR_UNKNOWN_TAG = "Unknown Tag 0x%04X\n";
    private static final String ERROR_ATTRIBUTE_MARKER =  "Expecting %08X, Found %08X\n";

    private boolean appendNamespaces = false;
    private boolean appendCData = false;

    static {
        Arrays.fill(SPACE_FILL, ' ');
    }

    public void setAppendNamespaces(boolean appendNamespaces) {
        this.appendNamespaces = appendNamespaces;
    }

    public void setAppendCData(boolean appendCData) {
        this.appendCData = appendCData;
    }

    public String decompressXml(byte[] bytes) throws IOException {
        try (ByteArrayInputStream bin = new ByteArrayInputStream(bytes)) {
            return decompressXml(bin);
        }
    }

    public String decompressXml(InputStream is) throws IOException {
        StringBuilder result = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        try(LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is)) {
            //Getting and checking the marker for a valid XMl file
            int fileMarker = dis.readInt();
            if (fileMarker != PACKED_XML_IDENTIFIER) {
                throw new IOException(
                        String.format(ERROR_INVALID_MAGIC_NUMBER,
                                PACKED_XML_IDENTIFIER,
                                fileMarker));
            }
            dis.skipBytes(12);
            List<String> packedStrings = parseStrings(dis);

            int ident = 0;
            int tag = dis.readShort();
            do {
                System.out.printf("%04x\n", tag);
                int headerSize = dis.readShort();
                int chunkSize = dis.readInt();
                switch (tag) {
                    case RES_XML_FIRST_CHUNK_TYPE: {
                        dis.skipBytes(chunkSize - 8);
                        break;
                    }
                    case RES_XML_RESOURCE_MAP_TYPE: {
                        dis.skipBytes(chunkSize - 8);
                        break;
                    }
                    case START_ELEMENT_TAG: {
                        parseStartTag(result, dis, packedStrings, ident);
                        ident++;
                        break;
                    }
                    case END_ELEMENT_TAG: {
                        ident--;
                        parseEndTag(result, dis, packedStrings, ident);
                        break;
                    }
                    case CDATA_TAG: {
                        parseCDataTag(result, dis, packedStrings, ident);
                        break;
                    }
                    default:
                        System.err.println(String.format(ERROR_UNKNOWN_TAG, tag));
                        System.err.println(headerSize);
                        System.err.println(chunkSize);
                }
                tag = dis.readShort();
            } while (tag != END_DOC_TAG);
            return result.toString();
        }
    }

    private void parseCDataTag(StringBuilder sb, DataInput dis, List<String> strings, int ident)
            throws IOException {
        //Skipping 3 unknowns integers:
        dis.skipBytes(12);
        int nameStringIndex = dis.readInt();
        //Skipping more 2 unknown integers.
        dis.skipBytes(8);

        if (appendCData) {
            sb.append(SPACE_FILL, 0, ident * IDENT_SIZE);
            sb.append(strings.get(nameStringIndex));
        }
    }

    private void parseEndTag(StringBuilder sb, DataInput dis, List<String> strings, int ident)
            throws IOException {
        sb.append(SPACE_FILL, 0, ident * IDENT_SIZE);
        sb.append("</");

        //Skipping 3 integers:
        // 1 - a flag?, like 38000000
        // 2 - Line of where this tag appeared in the original source file
        // 3 - Unknown: always FFFFFFFF?
        dis.skipBytes(8);
        int namespaceStringIndex = dis.readInt();
        if (appendNamespaces && namespaceStringIndex >= 0) {
            sb.append(strings.get(namespaceStringIndex)).append(":");
        }
        int nameStringIndex = dis.readInt();
        sb.append(strings.get(nameStringIndex)).append(">\n");
    }

    private void parseStartTag(StringBuilder sb, DataInput dis, List<String> strings, int ident)
            throws IOException {
        sb.append(SPACE_FILL, 0, ident * IDENT_SIZE);
        sb.append("<");
        //Skipping 3 integers:
        // 1 - a flag?, like 38000000
        // 2 - Line of where this tag appeared in the original source file
        // 3 - Unknown: always FFFFFFFF?
        dis.skipBytes(8);
        int namespaceStringIndex = dis.readInt();
        if (appendNamespaces && namespaceStringIndex >= 0) {
            sb.append(strings.get(namespaceStringIndex)).append(":");
        }
        int nameStringIndex = dis.readInt();
        sb.append(strings.get(nameStringIndex));
        parseAttributes(sb, dis, strings, ident);
        sb.append(">\n");
    }

    private void parseAttributes(StringBuilder sb, DataInput dis, List<String> strings, int ident)
            throws IOException {
        int marker = dis.readInt();
        if (marker != ATTRS_MARKER) {
            System.err.printf(ERROR_ATTRIBUTE_MARKER, ATTRS_MARKER, marker);
        }
        int numAttributes = dis.readInt();

        //skipping 1 unknown integer: always 00000000?
        dis.skipBytes(4);
        for (int i = 0; i < numAttributes; i++) {
            sb.append("\n").append(SPACE_FILL, 0, ident * IDENT_SIZE + ATTR_IDENT_SIZE);
            int attributeNamespaceIndex = dis.readInt();
            int attributeNameIndex = dis.readInt();
            int attributeValueIndex = dis.readInt();
            dis.skipBytes(3);
            int attrValueType = dis.readByte();
            int attributeResourceId = dis.readInt();
            if (appendNamespaces && attributeNamespaceIndex >= 0) {
                sb.append(strings.get(attributeNamespaceIndex)).append(":");
            }

            System.out.println(attributeNameIndex);
            String attributeName = strings.get(attributeNameIndex);
            if (attributeName.isEmpty()) attributeName="unknown";
            String attributeValue;
            System.out.println(attrValueType);
            switch (attrValueType) {
                case RES_TYPE_NULL:
                    attributeValue = attributeResourceId == 0 ? "<undefined>" : "<empty>";
                    break;
                case RES_TYPE_REFERENCE:
                    attributeValue = String.format("@res/0x%08X", attributeResourceId);
                    break;
                case RES_TYPE_ATTRIBUTE:
                    attributeValue = String.format("@attr/0x%08X", attributeResourceId);
                    break;
                case RES_TYPE_STRING:
                    attributeValue = strings.get(attributeValueIndex);
                    break;
                case RES_TYPE_FLOAT:
                    attributeValue = Float.toString(Float.intBitsToFloat(attributeResourceId));
                    break;
                case RES_TYPE_DIMENSION:
                    float value = resValue(attributeResourceId);
                    String type = getDimensionType(attributeResourceId);
                    attributeValue = value + type;
                    break;
                case RES_TYPE_FRACTION:
                    value = resValue(attributeResourceId);
                    type = getFractionType(attributeResourceId);
                    attributeValue = value + type;
                    break;
                case RES_TYPE_DYNAMIC_REFERENCE:
                    attributeValue = String.format("@dyn/0x%08X", attributeResourceId);
                    break;
                case RES_TYPE_INT_DEC:
                    attributeValue = Integer.toString(attributeResourceId);
                    break;
                case RES_TYPE_INT_HEX:
                    attributeValue = String.format("0x%08X", attributeResourceId);
                    break;
                case RES_TYPE_INT_BOOLEAN:
                    attributeValue = attributeResourceId == RES_VALUE_TRUE ? "true" : "false";
                    break;
                default:
                    attributeValue = String.format("0x%08X", attributeResourceId);

            }
            sb.append(attributeName).append("='").append(attributeValue).append("'");
        }
    }

    private List<String> parseStrings(DataInput dis) throws IOException {
        int numStrings = dis.readInt();
        int numStyles = dis.readInt();
        int flags = dis.readInt();

        boolean isUtf8Encoded = (flags & 0x100) > 0 ;
        int glyphSize;
        String encoding;
        System.out.println("Num Strings: " + numStrings);
        if (isUtf8Encoded) {
            glyphSize = 1;
            encoding = "UTF-8";
        } else {
            glyphSize = 2;
            encoding = "UTF-16LE";
        }

        //skipping to the beggining of stringtable data
        dis.skipBytes(8);

        //Skipping the string offsets.
        dis.skipBytes(Integer.SIZE / 8 * numStrings);

        List<String> packedStrings = new ArrayList<>(numStrings);
        int bytesRead = 0;
        byte[] buffer = new byte[1024];
        for (int i = 0; i < numStrings; i++) {
            int len;
            if (isUtf8Encoded) {
                dis.skipBytes(1);
                len = dis.readUnsignedByte();
            } else {
                len = dis.readUnsignedShort();
            }

            int bytelen = len * glyphSize;
            //String larger than existing buffer. Increase buffer.
            if (bytelen > buffer.length) {
                buffer = new byte[bytelen * 2];
            }

            dis.readFully(buffer, 0, bytelen);
            packedStrings.add(new String(buffer, 0, bytelen, encoding));
            dis.skipBytes(glyphSize);//The string ends with \0. Skip it.
            bytesRead += 2 + bytelen + glyphSize;
        }

//        for (int i = 0; i < numStyles; i++) {
//            int len = dis.readUnsignedShort();
//            dis.skipBytes(len);
//        }
        //Align to a multiple of 4 to continue reading data.
        System.out.println("zzz" + bytesRead);
        System.out.println(packedStrings);
        dis.skipBytes(bytesRead % 4);
        return packedStrings;
    }

    private static String getDimensionType(int data) {
        switch ((data >> COMPLEX_UNIT_SHIFT) & COMPLEX_UNIT_MASK) {
            case 0: return "px";
            case 1: return "dp";
            case 2: return "sp";
            case 3: return "pt";
            case 4: return "in";
            case 5: return "mm";
            default: return " (unknown unit)";
        }
    }

    private static String getFractionType(int data) {
        switch ((data >> COMPLEX_UNIT_SHIFT) & COMPLEX_UNIT_MASK) {
            case COMPLEX_UNIT_FRACTION:
                return "%%";
            case COMPLEX_UNIT_FRACTION_PARENT:
                return "%%p";
            default:
                return "(unknown unit)";
        }
    }

    private static float resValue(int data) {
        float value = (data&(COMPLEX_MANTISSA_MASK
                <<COMPLEX_MANTISSA_SHIFT))
                * RADIX_MULTS[(data>>COMPLEX_RADIX_SHIFT)
                & COMPLEX_RADIX_MASK];
        return value;
    }

    public static void main(String[] args) {
//        String fileName = "/Users/andreban/Desktop/map_pin.xml";
//        String fileName = "/Users/andreban/Desktop/abc_fade_in.xml";
        String fileName = "/Users/andreban/Desktop/abc_search_url_text.xml";
        try (FileInputStream fout = new FileInputStream(fileName)){
            String xml = new XmlDecompressor().decompressXml(fout);
            System.out.println(xml);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}