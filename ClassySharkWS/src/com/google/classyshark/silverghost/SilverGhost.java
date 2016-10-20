/*
 * Copyright 2016 Google, Inc.
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

package com.google.classyshark.silverghost;

import com.google.classyshark.gui.panel.reducer.Reducer;
import com.google.classyshark.silverghost.contentreader.ContentReader;
import com.google.classyshark.silverghost.plugins.EmptyFullArchiveReader;
import com.google.classyshark.silverghost.plugins.IdentityMapper;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.TranslatorFactory;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static com.google.classyshark.gui.panel.ClassySharkPanel.ANDROID_MANIFEST_XML_SEARCH;

/**
 * Main API class with respect to threading (never call readXXX method from UI thread)
 */
public class SilverGhost {

    private File binaryArchive;
    private Reducer reducer;
    private Translator translator;
    private ContentReader contentReader;
    private String manifestStr = "";
    private static TokensMapper tokensMapper;
    private static FullArchiveReader fullArchiveReader;

    static {
        tokensMapper = new IdentityMapper();
        fullArchiveReader = new EmptyFullArchiveReader();
    }

    public SilverGhost() {
    }

    public void setBinaryArchive(File binArchive) {
        this.binaryArchive = binArchive;

        // TODO think of initialyzing data members as they hold prev file state
        tokensMapper = new IdentityMapper();
    }

    //                     1. READ CONTENTS
    public void readContents() {
        contentReader = new ContentReader(getBinaryArchive());
        long start = System.currentTimeMillis();
        contentReader.load();
        reducer = new Reducer(contentReader.getAllClassNames());
        System.out.println("Archive Reading "
                + (System.currentTimeMillis() - start) + " ms ");

        if (binaryArchive.getName().endsWith(".apk")) {
            Translator translator =
                    TranslatorFactory.createTranslator("AndroidManifest.xml", binaryArchive);
            translator.apply();
            manifestStr = translator.toString();
        }

        fullArchiveReader.readAsyncArchive(binaryArchive);
    }

    public File getBinaryArchive() {
        return this.binaryArchive;
    }

    public List<ContentReader.Component> getComponents() {
        return contentReader.getAllComponents();
    }

    public List<String> getAllClassNames() {
        return contentReader.getAllClassNames();
    }

    public String getAutoCompleteClassName() {
        return reducer.getAutocompleteClassName();
    }

    public void initClassNameFiltering() {
        reducer.reduce("");
    }

    public List<String> filter(String text) {
        return reducer.reduce(text);
    }

    public boolean isArchiveError() {
        boolean noJavaClasses = contentReader.getAllClassNames().isEmpty();
        boolean noAndroidClasses = contentReader.getAllClassNames().size() == 1
                && contentReader.getAllClassNames().contains("AndroidManifest.xml");

        return noJavaClasses || noAndroidClasses;
    }

    //                     2. READ MAPPINGS FILE
    public TokensMapper readMappingFile(File mappingFile) {
        tokensMapper.readMappings(mappingFile);
        return tokensMapper;
    }

    public void addMappings(TokensMapper tokensMapper) {
        this.tokensMapper = tokensMapper;
    }

    //                     3. BINARY ARCHIVE ELEMENT
    public void translateArchiveElement(String elementName) {
        // TODO handle case when reducer is null or whatever
        translator =
                TranslatorFactory.createTranslator(
                        elementName,
                        getBinaryArchive(),
                        reducer.getAllClassNames(), fullArchiveReader);
        translator.addMapper(tokensMapper);
        translator.apply();
    }

    public List<Translator.ELEMENT> getArchiveElementTokens() {
        return translator.getElementsList();
    }

    public List<String> getImportsForCurrentClass() {
        return translator.getDependencies();
    }

    public String getCurrentClassName() {
        return translator.getClassName();
    }

    public String getCurrentClassContent() {
        return translator.toString();
    }

    public List<Translator.ELEMENT> getManifestMatches(String textFromTypingArea) {

        LinkedList<Translator.ELEMENT> result = new LinkedList<>();

        if (manifestStr.isEmpty()) {
            return result;
        }

        String[] manifestStrArray = manifestStr.split("[\\r\\n]+");

        if (textFromTypingArea.length() > 2) {
            for (int i = 0; i < manifestStrArray.length; i++) {
                if (manifestStrArray[i].contains(textFromTypingArea) ||
                        manifestStrArray[i].equalsIgnoreCase(textFromTypingArea)) {
                    if (i > 2) {
                        int j = i;
                        result.add(new Translator.ELEMENT(ANDROID_MANIFEST_XML_SEARCH + manifestStrArray[j - 1], Translator.TAG.ANNOTATION));
                        result.add(new Translator.ELEMENT(ANDROID_MANIFEST_XML_SEARCH + manifestStrArray[j - 2], Translator.TAG.ANNOTATION));
                    }

                    result.add(new Translator.ELEMENT(ANDROID_MANIFEST_XML_SEARCH + manifestStrArray[i], Translator.TAG.IDENTIFIER));

                    if (i < manifestStrArray.length - 4) {
                        int j = i;
                        result.add(new Translator.ELEMENT(ANDROID_MANIFEST_XML_SEARCH + manifestStrArray[j + 1], Translator.TAG.ANNOTATION));
                        result.add(new Translator.ELEMENT(ANDROID_MANIFEST_XML_SEARCH + manifestStrArray[j + 2], Translator.TAG.ANNOTATION));
                    }

                    result.add(new Translator.ELEMENT(ANDROID_MANIFEST_XML_SEARCH, Translator.TAG.ANNOTATION));
                    result.add(new Translator.ELEMENT(ANDROID_MANIFEST_XML_SEARCH,Translator.TAG.ANNOTATION));
                }
            }
        }

        return result;
    }
}
