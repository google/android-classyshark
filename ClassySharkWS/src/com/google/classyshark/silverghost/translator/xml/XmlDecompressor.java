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
 */
public class XmlDecompressor {
    private static final int PACKED_XML_IDENTIFIER = 0x00080003;
    private static final int END_DOC_TAG = 0x00100101;
    private static final int START_ELEMENT_TAG = 0x00100102;
    private static final int END_ELEMENT_TAG = 0x00100103;
    private static final int CDATA_TAG = 0x00100104;
    private static final int ATTRS_MARKER = 0x00140014;
    private static final int RES_VALUE_TRUE = 0xffffffff;
    private static final int RES_VALUE_FALSE = 0x00000000;
    private static final int RES_REF_MARKER = 0x7f000000;

    private static char[] SPACE_FILL = new char[80];

    private static final int IDENT_SIZE = 2;
    private static final int ATTR_IDENT_SIZE = 4;

    private static final String ERROR_INVALID_MAGIC_NUMBER =
            "Invalid packed XML identifier. Expecting 0x%08X, found 0x%08X\n";
    private static final String ERROR_UNKNOWN_TAG = "Unknown Tag 0x%08X\n";
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

            //Unknown content after the strings. Seeking for a start tag
            int tag;
            do {
                tag = dis.readInt();
            } while (tag != START_ELEMENT_TAG);

            int ident = 0;
            do {
                switch (tag) {
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
                }
                tag = dis.readInt();
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
        dis.skipBytes(12);
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
        dis.skipBytes(12);
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
            dis.skipBytes(4);//int attributeFlags = dis.readInt();
            int attributeResourceId = dis.readInt();
            if (appendNamespaces && attributeNamespaceIndex >= 0) {
                sb.append(strings.get(attributeNamespaceIndex)).append(":");
            }

            String attributeName = strings.get(attributeNameIndex);
            String attributeValue;
            if (attributeValueIndex == -1) {
                if (attributeResourceId == RES_VALUE_TRUE) {
                    attributeValue = "true";
                } else if (attributeResourceId == RES_VALUE_FALSE) {
                    attributeValue = "false";
                } else if (attributeResourceId < RES_REF_MARKER) {
                    attributeValue = String.valueOf(attributeResourceId);
                } else {
                    attributeValue = String.format("@res/0x%08X", attributeResourceId);
                }
            } else  {
                attributeValue = strings.get(attributeValueIndex);
            }
            sb.append(attributeName).append("='").append(attributeValue).append("'");
        }
    }

    private List<String> parseStrings(DataInput dis) throws IOException {
        int numStrings = dis.readInt();
        //skipping to the beggining of stringtable data
        dis.skipBytes(16);

        //Skipping the string offsets.
        dis.skipBytes(Integer.SIZE / 8 * numStrings);

        List<String> packedStrings = new ArrayList<>(numStrings);
        int bytesRead = 0;
        byte[] buffer = new byte[1024];
        for (int i = 0; i < numStrings; i++) {
            int len = dis.readUnsignedShort();
            int bytelen = len * 2;
            
            //String larger than existing buffer. Increase buffer.
            if (bytelen > buffer.length) {
                buffer = new byte[bytelen * 2];
            }

            dis.readFully(buffer, 0, bytelen);
            packedStrings.add(new String(buffer, 0, bytelen, "UTF-16LE"));
            dis.skipBytes(2);
            bytesRead += 2 + bytelen + 2;
        }
        //Align to a multiple of 4 to continue reading data.
        dis.skipBytes(bytesRead % 4);
        return packedStrings;
    }

}