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

package com.google.classyshark.silverghost.translator.apk;

import com.google.classyshark.silverghost.TokensMapper;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.apk.dashboard.ApkDashboard;
import com.google.classyshark.silverghost.translator.apk.dashboard.ClassesDexDataEntry;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Translator for the apk
 */
public class ApkTranslator implements Translator {
    private File apkFile;
    private ApkDashboard apkDashboard;

    private List<ELEMENT> elements = new ArrayList<>();

    public ApkTranslator(File apkFile) {
        // TODO add checks for file that is not an APK
        this.apkFile = apkFile;
    }

    @Override
    public String getClassName() {
        return "";
    }

    @Override
    public void addMapper(TokensMapper reverseMappings) {

    }

    @Override
    public void apply() {
        apkDashboard = new ApkDashboard(apkFile);
        apkDashboard.inspect();

        ELEMENT element = new ELEMENT("\n                  ~ APK DASHBOARD ~\n" , TAG.IDENTIFIER);
        elements.add(element);


        Iterator<ClassesDexDataEntry> dexesIter = apkDashboard.iterator();

        while (dexesIter.hasNext()) {

            ClassesDexDataEntry dexEntry = dexesIter.next();

            element = new ELEMENT("\n" + dexEntry.getName(), TAG.MODIFIER);
            elements.add(element);

            element = new ELEMENT(
                    "\nall methods: "
                            + dexEntry.allMethods
                            + "\nnative methods: "
                            + dexEntry.nativeMethodsCount
                            + "\n", TAG.DOCUMENT);
            elements.add(element);
        }

        element = new ELEMENT("\n\nPossible Java Dependencies Errors", TAG.MODIFIER);
        elements.add(element);

        element = new ELEMENT("\n" + apkDashboard.getJavaDependenciesErrorsAsString(), TAG.DOCUMENT);
        elements.add(element);

        element = new ELEMENT("\n\nPossible Java Internal API Errors", TAG.MODIFIER);
        elements.add(element);

        element = new ELEMENT("\n" + apkDashboard.getJavaInternalAPIsErrors(), TAG.DOCUMENT);
        elements.add(element);

        element = new ELEMENT("\n\n\nDynamic Symbol Errors", TAG.MODIFIER);
        elements.add(element);

        for (String error : apkDashboard.getNativeErrors()) {
            element = new ELEMENT("\n" + error, TAG.DOCUMENT);
            elements.add(element);
        }

        element = new ELEMENT("\n\n\nNative Libraries\n", TAG.MODIFIER);
        elements.add(element);

        for (String nativeLib : apkDashboard.getFullPathNativeLibNamesSorted()) {
            element = new ELEMENT(nativeLib, TAG.DOCUMENT);
            elements.add(element);
        }

        element = new ELEMENT("\n\nNative Dependencies\n", TAG.MODIFIER);
        elements.add(element);

        for (String nativeLib : apkDashboard.getNativeLibNamesSorted()) {
            element = new ELEMENT(nativeLib + " " + apkDashboard.getPrivateLibErrorTag(nativeLib)
                    + "\n", TAG.DOCUMENT);
            elements.add(element);
        }

        dexesIter = apkDashboard.iterator();

        while (dexesIter.hasNext()) {

            ClassesDexDataEntry dexEntry = dexesIter.next();

            element = new ELEMENT("\n" + dexEntry.getName(), TAG.MODIFIER);
            elements.add(element);

            element = new ELEMENT(
                    "\nSyntheticAccessors \n", TAG.MODIFIER);
            elements.add(element);

            element = new ELEMENT(
                    "\n" + dexEntry.syntheticAccessors.toString()
                            + "\n", TAG.DOCUMENT);
            elements.add(element);
        }
    }

    @Override
    public List<ELEMENT> getElementsList() {
        return elements;
    }

    @Override
    public List<String> getDependencies() {
        return new LinkedList<>();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ELEMENT element : elements) {
            sb.append(element.text);
        }
        return sb.toString();
    }
}