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
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.TranslatorFactory;
import java.io.File;
import java.util.List;

public class SilverGhost {

    private File binaryArchive;
    private Reducer reducer;
    private Translator translator;
    // TODO may be to move back to ClassySharkPanel
    public boolean isDataLoaded = false;


    private List<String> allClassNamesInArchive;
    private ContentReader contentReader;

    public SilverGhost() {

    }

    public void setBinaryArchive(File binArchive) {
        this.binaryArchive = binArchive;
    }

    public File getBinaryArchive() {
        return this.binaryArchive;
    }

    public void readContents() {
        contentReader = new ContentReader(getBinaryArchive());
        long start = System.currentTimeMillis();
        contentReader.load();
        allClassNamesInArchive = contentReader.getAllClassNames();
        reducer = new Reducer(allClassNamesInArchive);
        System.out.println("Archive Reading "
                + (System.currentTimeMillis() - start) + " ms ");

    }

    public List<ContentReader.Component> getComponents() {
        return contentReader.getAllComponents();
    }

    public List<String> getImportsForCurrentClass() {
        return translator.getDependencies();
    }

    public List<String> getAllClassNames() {
        return allClassNamesInArchive;
    }

    public void initClassNameFiltering() {
        reducer.reduce("");
    }

    public Translator getCurrentClassTranslator() {
        return translator;
    }

    public boolean isArchiveError() {

        boolean noJavaClasses = allClassNamesInArchive.isEmpty();
        boolean noAndroidClasses = allClassNamesInArchive.size() == 1
                && allClassNamesInArchive.contains("AndroidManifest.xml");

        return noJavaClasses || noAndroidClasses;
    }


    public String getAutoCompleteClassName() {
       return reducer.getAutocompleteClassName();
    }

    public List<String> filter(String text) {
        return reducer.reduce(text);
    }

    // TODO side effect with state
    public List<Translator.ELEMENT> translateClass(String name) {
        translator =
                TranslatorFactory.createTranslator(
                        name, getBinaryArchive(), reducer.getAllClassNames());
        translator.apply();
        return translator.getElementsList();
    }
}
