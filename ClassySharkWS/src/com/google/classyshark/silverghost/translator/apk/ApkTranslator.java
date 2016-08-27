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
import com.google.classyshark.silverghost.translator.apk.apkinspectionsbag.ApkInspectionsBag;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Translator for the apk
 */
public class ApkTranslator implements Translator {
    private File apkFile;
    private ApkInspectionsBag apkInspectionsBag;

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
        apkInspectionsBag = new ApkInspectionsBag(apkFile);
        apkInspectionsBag.inspect();

        int numDexes = apkInspectionsBag.getNumberOfDexes();

        for (int i = 0; i < numDexes; i++) {

            ELEMENT element = new ELEMENT("\nclasses" + ((i==0)? "" : i + "") + ".dex", TAG.MODIFIER);
            elements.add(element);

            element = new ELEMENT(
                    "\nall methods: "
                            + apkInspectionsBag.getAllMethodsCountPerDex(i)
                            + "\nnative methods: "
                            + apkInspectionsBag.getAllNativeMethodsCountPerDex(i)
                            + "\n", TAG.DOCUMENT);
            elements.add(element);

            element = new ELEMENT(
                    "\nSyntheticAccessors \n", TAG.MODIFIER);

            elements.add(element);

            element = new ELEMENT(
                    apkInspectionsBag.getSyntheticAccessors(i).toString(),
                    TAG.DOCUMENT);

            elements.add(element);
        }

        ELEMENT element = new ELEMENT("\n\n\nDynamic Symbol Errors", TAG.MODIFIER);
        elements.add(element);

        for (String error : apkInspectionsBag.getNativeErrors()) {
            element = new ELEMENT("\n" + error, TAG.DOCUMENT);
            elements.add(element);
        }

        element = new ELEMENT("\n\n\nNative Libraries\n", TAG.MODIFIER);
        elements.add(element);

        for (String nativeLib : apkInspectionsBag.getFullPathNativeLibNamesSorted()) {
            element = new ELEMENT(nativeLib, TAG.DOCUMENT);
            elements.add(element);
        }

        element = new ELEMENT("\n\nNative Dependencies\n", TAG.MODIFIER);
        elements.add(element);

        for (String nativeLib : apkInspectionsBag.getNativeLibNamesSorted()) {
            element = new ELEMENT(nativeLib + " " + apkInspectionsBag.getPrivateLibErrorTag(nativeLib)
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
        // TODO some sort of convertion for the command line

        return elements.toString();
    }
}
