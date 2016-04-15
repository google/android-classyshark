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
import com.google.classyshark.silverghost.tokensmapper.ProguardMapper;
import com.google.classyshark.silverghost.tokensmapper.MappingReader;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.TranslatorFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SilverGhost {

    private File binaryArchive;
    private Reducer reducer;
    private Translator translator;
    private List<String> allClassNamesInArchive;
    private ContentReader contentReader;
    private ProguardMapper proguardMapper = ProguardMapper.IDENTITY;

    public SilverGhost() {
    }

    public void setBinaryArchive(File binArchive) {
        this.binaryArchive = binArchive;

        // TODO think of initialyzing data members as they hold prev file state
        proguardMapper = ProguardMapper.IDENTITY;
    }

    //                     1. READ CONTENTS
    public void readContents() {
        contentReader = new ContentReader(getBinaryArchive());
        long start = System.currentTimeMillis();
        contentReader.load();
        allClassNamesInArchive = contentReader.getAllClassNames();
        reducer = new Reducer(allClassNamesInArchive);
        System.out.println("Archive Reading "
                + (System.currentTimeMillis() - start) + " ms ");
    }

    public File getBinaryArchive() {
        return this.binaryArchive;
    }

    public List<ContentReader.Component> getComponents() {
        return contentReader.getAllComponents();
    }

    public List<String> getAllClassNames() {
        return allClassNamesInArchive;
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
        boolean noJavaClasses = allClassNamesInArchive.isEmpty();
        boolean noAndroidClasses = allClassNamesInArchive.size() == 1
                && allClassNamesInArchive.contains("AndroidManifest.xml");

        return noJavaClasses || noAndroidClasses;
    }

    //                     2. READ MAPPINGS FILE
    public static ProguardMapper readMappingFile(File mappingFile) {
        try {
            MappingReader mr = new MappingReader(mappingFile);
            ProguardMapper reverseMappings = new ProguardMapper();
            mr.pump(reverseMappings);
            return reverseMappings;
        } catch (IOException e) {
            return ProguardMapper.IDENTITY;
        }
    }

    public void addMappings(ProguardMapper proguardMapper) {
        this.proguardMapper = proguardMapper;
    }

    //                     3. BINARY ARCHIVE ELEMENT
    public void translateArchiveElement(String elementName) {
        // TODO handle case when reducer is null or whatever
        translator =
                TranslatorFactory.createTranslator(
                        elementName,
                        getBinaryArchive(),
                        reducer.getAllClassNames());
        translator.addMapper(this.proguardMapper);
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
}
