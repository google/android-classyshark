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

package com.google.classyshark.translator.xml;

import com.google.classyshark.translator.Translator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Is a function : (apk file) --> ,manifest XML text, as list of tokens with tag
 * based om code posted on StackOverflow by Ribo:
 * http://stackoverflow.com/a/4761689/496992
 * <p/>
 * There is a bug that some manifests can't be shown, added a fallback case to display
 * all strings
 */
public class Translator2AndroidXml implements Translator {

    public static String spaces = "                                             ";
    private static int endDocTag = 0x00100101;
    private static int startTag = 0x00100102;
    private static int endTag = 0x00100103;
    private static int cDataTag = 0x00100104;
    private final File archiveFile;
    private String xml;
    private List<ELEMENT> xmlCode;
    private List<String> fallBacklist;

    private boolean fallback = false;

    public Translator2AndroidXml(File archiveFile) {
        this.archiveFile = archiveFile;
        xmlCode = new ArrayList<>();
    }

    @Override
    public String getClassName() {
        return "AndroidManifest.xml";
    }

    @Override
    public void apply() {
        try {
            InputStream is;
            ZipFile zip = null;
            long size;

            if (archiveFile.getName().endsWith(".apk")
                    || archiveFile.getName().endsWith(".zip")) {
                zip = new ZipFile(archiveFile);
                ZipEntry mft = zip.getEntry("AndroidManifest.xml");
                size = mft.getSize();
                is = zip.getInputStream(mft);
            } else {
                size = archiveFile.length();
                is = new FileInputStream(archiveFile);
            }

            if (size > Integer.MAX_VALUE) {
                throw new IOException("File larger than " + Integer.MAX_VALUE + " bytes not supported");
            }

            ByteArrayOutputStream bout = new ByteArrayOutputStream((int)size);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) > 0) {
                bout.write(buffer, 0 , bytesRead);
            }

            is.close();
            if (zip != null) {
                zip.close();
            }

            this.xml = decompressXML(bout.toByteArray());
        } catch (Exception e) {
            fallback = true;
        }
    }

    @Override
    public List<ELEMENT> getElementsList() {
        String formatted = prettyFormat(this.xml);
        StringTokenizer st = new StringTokenizer(formatted, "\n");

        while (st.hasMoreElements()) {
            String currentFormattedXmlLine = st.nextElement().toString();

            xmlCode.add(new ELEMENT("\n",
                    TAG.DOCUMENT));

            int indexTag = currentFormattedXmlLine.indexOf("<");

            xmlCode.add(new ELEMENT(currentFormattedXmlLine.substring(0, indexTag),
                    TAG.DOCUMENT));

            String actualXmlTextLine = currentFormattedXmlLine.substring(indexTag);
            int indexOfSpaceInsideXmlText = actualXmlTextLine.indexOf(" ");

            xmlCode.add(new ELEMENT("<",
                    TAG.IDENTIFIER));
            if (indexOfSpaceInsideXmlText > 0) {
                xmlCode.add(new ELEMENT(actualXmlTextLine.substring(1, indexOfSpaceInsideXmlText),
                        TAG.DOCUMENT));

                xmlCode.add(new ELEMENT(actualXmlTextLine.substring(indexOfSpaceInsideXmlText,
                        actualXmlTextLine.length() - 1),
                        TAG.IDENTIFIER));
            } else {
                xmlCode.add(new ELEMENT(currentFormattedXmlLine.substring(indexTag + 1,
                        currentFormattedXmlLine.length() - 1),
                        TAG.DOCUMENT));
            }
            xmlCode.add(new ELEMENT(">",
                    TAG.IDENTIFIER));
        }

        if (xml.isEmpty() || fallback) {
            xmlCode.add(new ELEMENT("The was a problem decoding the XML, showing all strings: ",
                    TAG.DOCUMENT));
            Collections.sort(fallBacklist);
            for (String s : fallBacklist) {
                if (!s.isEmpty()) {
                    xmlCode.add(new ELEMENT("\n" + s, TAG.IDENTIFIER));
                }
            }
        }

        return this.xmlCode;
    }

    @Override
    public List<String> getDependencies() {
        // TODO fuzzy logic for permissions etc
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return prettyFormat(xml);
    }

    private String decompressXML(byte[] xml) {

        StringBuilder finalXML = new StringBuilder();

        // Compressed XML file/bytes starts with 24x bytes of data,
        // 9 32 bit words in little endian order (LSB first):
        // 0th word is 03 00 08 00
        // 3rd word SEEMS TO BE: Offset at then of StringTable
        // 4th word is: Number of strings in string table
        // WARNING: Sometime I indiscriminently display or refer to word in
        // little endian storage format, or in integer format (ie MSB first).
        int numbStrings = LEW(xml, 4 * 4);

        // StringIndexTable starts at offset 24x, an array of 32 bit LE offsets
        // of the length/string data in the StringTable.
        int sitOff = 0x24; // Offset of start of StringIndexTable

        // StringTable, each string is represented with a 16 bit little endian
        // character count, followed by that number of 16 bit (LE) (Unicode)
        // chars.
        int stOff = sitOff + numbStrings * 4; // StringTable follows
        // StrIndexTable

        // XMLTags, The XML tag tree starts after some unknown content after the
        // StringTable. There is some unknown data after the StringTable, scan
        // forward from this point to the flag for the start of an XML start
        // tag.
        int xmlTagOff = LEW(xml, 3 * 4); // Start from the offset in the 3rd
        // word.
        // Scan forward until we find the bytes: 0x02011000(x00100102 in normal
        // int)
        for (int ii = xmlTagOff; ii < xml.length - 4; ii += 4) {
            if (LEW(xml, ii) == startTag) {
                xmlTagOff = ii;
                break;
            }
        } // end of hack, scanning for start of first start tag

        // XML tags and attributes:
        // Every XML start and end tag consists of 6 32 bit words:
        // 0th word: 02011000 for startTag and 03011000 for endTag
        // 1st word: a flag?, like 38000000
        // 2nd word: Line of where this tag appeared in the original source file
        // 3rd word: FFFFFFFF ??
        // 4th word: StringIndex of NameSpace name, or FFFFFFFF for default NS
        // 5th word: StringIndex of Element Name
        // (Note: 01011000 in 0th word means end of XML document, endDocTag)

        // Start tags (not end tags) contain 3 more words:
        // 6th word: 14001400 meaning??
        // 7th word: Number of Attributes that follow this tag(follow word 8th)
        // 8th word: 00000000 meaning??

        // Attributes consist of 5 words:
        // 0th word: StringIndex of Attribute Name's Namespace, or FFFFFFFF
        // 1st word: StringIndex of Attribute Name
        // 2nd word: StringIndex of Attribute Value, or FFFFFFF if ResourceId
        // used
        // 3rd word: Flags?
        // 4th word: str ind of attr value again, or ResourceId of value

        fallBacklist = new ArrayList<>();
        // TMP, dump string table to tr for debugging
        // tr.addSelect("strings", null);
        for (int ii = 0; ii < numbStrings; ii++) {
            // Length of string starts at StringTable plus offset in StrIndTable
            String str = compXmlString(xml, sitOff, stOff, ii);
            fallBacklist.add(str);
            // tr.add(String.valueOf(ii), str);
        }
        // tr.parent();

        // Step through the XML tree element tags and attributes
        int off = xmlTagOff;
        int indent = 0;
        int startTagLineNo = -2;
        while (off < xml.length) {
            int tag0 = LEW(xml, off);
            // int tag1 = LEW(xml, off+1*4);
            int lineNo = LEW(xml, off + 2 * 4);
            // int tag3 = LEW(xml, off+3*4);
            int nameNsSi = LEW(xml, off + 4 * 4);
            int nameSi = LEW(xml, off + 5 * 4);

            if (tag0 == startTag) { // XML START TAG
                int tag6 = LEW(xml, off + 6 * 4); // Expected to be 14001400
                int numbAttrs = LEW(xml, off + 7 * 4); // Number of Attributes
                // to follow
                // int tag8 = LEW(xml, off+8*4); // Expected to be 00000000
                off += 9 * 4; // Skip over 6+3 words of startTag data
                String name = compXmlString(xml, sitOff, stOff, nameSi);
                // tr.addSelect(name, null);
                startTagLineNo = lineNo;

                // Look for the Attributes
                StringBuffer sb = new StringBuffer();
                for (int ii = 0; ii < numbAttrs; ii++) {
                    int attrNameNsSi = LEW(xml, off); // AttrName Namespace Str
                    // Ind, or FFFFFFFF
                    int attrNameSi = LEW(xml, off + 1 * 4); // AttrName String
                    // Index
                    int attrValueSi = LEW(xml, off + 2 * 4); // AttrValue Str
                    // Ind, or
                    // FFFFFFFF
                    int attrFlags = LEW(xml, off + 3 * 4);
                    int attrResId = LEW(xml, off + 4 * 4); // AttrValue
                    // ResourceId or dup
                    // AttrValue StrInd
                    off += 5 * 4; // Skip over the 5 words of an attribute

                    String attrName = compXmlString(xml, sitOff, stOff,
                            attrNameSi);
                    String attrValue = attrValueSi != -1 ? compXmlString(xml,
                            sitOff, stOff, attrValueSi) : "resourceID 0x"
                            + Integer.toHexString(attrResId);
                    sb.append(" " + attrName + "=\"" + attrValue + "\"");
                    // tr.add(attrName, attrValue);
                }
                finalXML.append("<" + name + sb + ">");
                prtIndent(indent, "<" + name + sb + ">");
                indent++;

            } else if (tag0 == endTag) { // XML END TAG
                indent--;
                off += 6 * 4; // Skip over 6 words of endTag data
                String name = compXmlString(xml, sitOff, stOff, nameSi);

                finalXML.append("</" + name + ">");

                prtIndent(indent, "</" + name + "> (line " + startTagLineNo
                        + "-" + lineNo + ")");
                // tr.parent(); // Step back up the NobTree

            } else if (tag0 == cDataTag) {
                String name = compXmlString(xml, sitOff, stOff, nameNsSi);
                // Commented while the code highlighter doesn't support showing
                // CDATA sections properly. It shouldn' matter to AndroidManifest.xml
                System.err.printf("Found CDATA tag %s. Ignoring it", name);
//                finalXML.append("<![CDATA[").append(name).append("]]>");
                off += 7 * 4;
            } else if (tag0 == endDocTag) { // END OF XML DOC TAG
                break;

            } else {
                prt("  Unrecognized tag code '" + Integer.toHexString(tag0)
                        + "' at offset " + off);
                break;
            }
        }
        return finalXML.toString();
    }

    private static void prt(String str) {
        //System.err.print(str);
    }

    private static String compXmlString(byte[] xml, int sitOff, int stOff, int strInd) {
        if (strInd < 0)
            return null;
        int strOff = stOff + LEW(xml, sitOff + strInd * 4);
        return compXmlStringAt(xml, strOff);
    }

    private static void prtIndent(int indent, String str) {
        prt(spaces.substring(0, Math.min(indent * 2, spaces.length())) + str);
    }

    // compXmlStringAt -- Return the string stored in StringTable format at
    // offset strOff. This offset points to the 16 bit string length, which
    // is followed by that number of 16 bit (Unicode) chars.
    private static String compXmlStringAt(byte[] arr, int strOff) {
        int strLen = arr[strOff + 1] << 8 & 0xff00 | arr[strOff] & 0xff;
        byte[] chars = new byte[strLen];
        for (int ii = 0; ii < strLen; ii++) {
            chars[ii] = arr[strOff + 2 + ii * 2];
        }
        return new String(chars); // Hack, just use 8 byte chars
    }

    // LEW -- Return value of a Little Endian 32 bit word from the byte array
    // at offset off.
    private static int LEW(byte[] arr, int off) {
        return arr[off + 3] << 24 & 0xff000000
                | arr[off + 2] << 16 & 0xff0000
                | arr[off + 1] << 8 & 0xff00
                | arr[off] & 0xFF;
    }

    private  String prettyFormat(String input) {
        return prettyFormat(input, 2);
    }

    private String prettyFormat(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            fallback = true;
            return "";
        }
    }

    public static void main(String[] args) throws Exception {
        String archiveName = System.getProperty("user.home") +
                "/Desktop/Scenarios/2 Samples/app-debug.apk";
        Translator2AndroidXml t2ax = new Translator2AndroidXml(new File(archiveName));
        t2ax.apply();
        System.out.print(t2ax.toString());
    }
}