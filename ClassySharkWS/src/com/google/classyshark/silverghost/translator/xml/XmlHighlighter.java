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

import com.google.classyshark.silverghost.translator.Translator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Receives a String XML and highlights it.
 */
public class XmlHighlighter {
    public enum TagType {
        TAG, ATTR_NAME, ATTR_VALUE, COMMENT, CDATA
    }
    private static final Pattern TAG_PATTERN =
            Pattern.compile("(</?[a-z-]+)\\s?>?", Pattern.MULTILINE);
    private static final Pattern TAG_PATTERN_2 =
            Pattern.compile("(<\\?)[a-z-]+\\s?>?", Pattern.MULTILINE);

    private static final Pattern TAG_END_PATTERN = Pattern.compile("[^\\]](\\??>)", Pattern.MULTILINE);

    private static final Pattern TAG_ATTRIBUTE_PATTERN =
            Pattern.compile("\\s(\\w*)\\=", Pattern.MULTILINE);
    private static final Pattern TAG_ATTRIBUTE_VALUE =
            Pattern.compile("[a-z-]*\\=(\"[^\"]*\")", Pattern.MULTILINE);
    private static final Pattern TAG_ATTRIBUTE_VALUE_2 =
            Pattern.compile("[a-z-]*\\=(\'[^\']*\')", Pattern.MULTILINE);
    private static final Pattern TAG_COMMENT = Pattern.compile("(<!--.*-->)", Pattern.MULTILINE);
    private static final Pattern TAG_CDATA_START =
            Pattern.compile("(\\<!\\[CDATA\\[).*", Pattern.MULTILINE);
    private static final Pattern TAG_CDATA_END = Pattern.compile(".*(]]>)", Pattern.MULTILINE);

    private static final Map<Pattern, TagType> PATTERN_TAG_TYPE_MAP = new HashMap<>();

    static {
        PATTERN_TAG_TYPE_MAP.put(TAG_PATTERN, TagType.TAG);
        PATTERN_TAG_TYPE_MAP.put(TAG_PATTERN_2, TagType.TAG);
        PATTERN_TAG_TYPE_MAP.put(TAG_END_PATTERN, TagType.TAG);
        PATTERN_TAG_TYPE_MAP.put(TAG_ATTRIBUTE_PATTERN, TagType.ATTR_NAME);
        PATTERN_TAG_TYPE_MAP.put(TAG_ATTRIBUTE_VALUE, TagType.ATTR_VALUE);
        PATTERN_TAG_TYPE_MAP.put(TAG_ATTRIBUTE_VALUE_2, TagType.ATTR_VALUE);
        PATTERN_TAG_TYPE_MAP.put(TAG_COMMENT, TagType.COMMENT);
        PATTERN_TAG_TYPE_MAP.put(TAG_CDATA_START, TagType.CDATA);
        PATTERN_TAG_TYPE_MAP.put(TAG_CDATA_END, TagType.CDATA);
    }

    public static class Element implements Comparable<Element> {
        private int start;
        private int end;
        private TagType tag;

        public Element(int start, int end, TagType tag) {
            this.start = start;
            this.end = end;
            this.tag = tag;
        }

        @Override
        public String toString() {
            return "Element{" +
                    "start=" + start +
                    ", end=" + end +
                    ", tag='" + tag + '\'' +
                    '}';
        }

        @Override
        public int compareTo(Element o) {
            return Integer.compare(this.start, o.start);
        }
    }

    public List<Translator.ELEMENT> getElements(String xml) {
        List<Element> elementList = new ArrayList<>();
        for (Map.Entry<Pattern, TagType> entry: PATTERN_TAG_TYPE_MAP.entrySet()) {
            Matcher m = entry.getKey().matcher(xml);
            while (m.find()) {
                elementList.add(new Element(m.start(1), m.end(1), entry.getValue()));
            }
        }

        Collections.sort(elementList);

        int xmlPos = 0;
        Element e;
        List<Translator.ELEMENT> elements = new ArrayList<>();
        for (int i = 0; i < elementList.size(); i++) {
            e = elementList.get(i);
            if (xmlPos < e.start) {
                elements.add(new Translator.ELEMENT(
                        xml.substring(xmlPos, e.start), Translator.TAG.XML_COMMENT));
            }

            Translator.TAG tag;
            switch (e.tag) {
                case TAG:
                    tag = Translator.TAG.XML_TAG;
                    break;
                case ATTR_NAME:
                    tag = Translator.TAG.XML_ATTR_NAME;
                    break;
                case ATTR_VALUE:
                    tag = Translator.TAG.XML_ATTR_VALUE;
                    break;
                case CDATA:
                    tag = Translator.TAG.XML_CDATA;
                    break;
                case COMMENT:
                    tag = Translator.TAG.XML_COMMENT;
                    break;
                default:
                    tag = Translator.TAG.XML_DEFAULT;
            }
            elements.add(new Translator.ELEMENT(xml.substring(e.start, e.end), tag));
            xmlPos = e.end;
            if (i == elementList.size() - 1 && xmlPos < xml.length() - 1) {
                elements.add(new Translator.ELEMENT(
                        xml.substring(e.end, xml.length()), Translator.TAG.XML_DEFAULT));
            }
        }
        return elements;
    }

    public static void main(String[] args) {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<manifest\n" +
                "    versionCode='@res/0x00000001'\n" +
                "    versionName='1.0'\n" +
                "    package='com.google.example.minproject'\n" +
                "    platformBuildVersionCode='23'\n" +
                "    platformBuildVersionName='6.0-2166767'>\n" +
                "  <uses-sdk\n" +
                "      minSdkVersion='@res/0x00000010'\n" +
                "      targetSdkVersion='@res/0x00000017'>\n" +
                "  </uses-sdk>\n" +
                "  <application\n" +
                "      theme='@res/0x7F08007E'\n" +
                "      label='@res/0x7F060012'\n" +
                "      icon='@res/0x7F030000'\n" +
                "      debuggable='@res/0xFFFFFFFF'\n" +
                "      allowBackup='@res/0xFFFFFFFF'\n" +
                "      supportsRtl='@res/0xFFFFFFFF'>\n" +
                "        <![CDATA[Testing 123]]>\n"+
                "    <meta-data\n" +
                "        name='test'>\n" +
                "    </meta-data>\n" +
                "  </application>\n" +
                "</manifest>";
        System.out.println(xml);
        List<Translator.ELEMENT> elements = new XmlHighlighter().getElements(xml);
        for (Translator.ELEMENT e: elements) {
            System.out.println(e.text);
        }
        System.out.println(elements.size());

    }
}