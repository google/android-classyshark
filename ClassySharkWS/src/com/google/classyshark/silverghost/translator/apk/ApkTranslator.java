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
import com.google.classyshark.silverghost.translator.apk.dashboard.asciitable.Table;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Translator for the apk
 */
public class ApkTranslator implements Translator {
    public static final String[] NEW_LINE = {" ", " "};
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

        ELEMENT element = new ELEMENT("\n                  ~ APK DASHBOARD ~\n\n", TAG.IDENTIFIER);
        elements.add(element);


        String[] headers = {"Entry",
                "Description"
        };

        List<String[]> data = new LinkedList<>();

        data.add(NEW_LINE);

        for (ClassesDexDataEntry dexEntry : apkDashboard.getAllDexEntries()) {
            addRow(data, dexEntry.getName(), String.valueOf(dexEntry.allMethods));
        }

        data.add(NEW_LINE);

        for (String javaDepError : apkDashboard.getJavaDependenciesErrors()) {
            addRow(data, "Java ", javaDepError);
        }

        data.add(NEW_LINE);

        for (String systemAction : apkDashboard.getManifestErrors()) {
            addRow(data, "System Action ", systemAction);
        }

        data.add(NEW_LINE);

        for (String nativeLib : apkDashboard.getNativeLibNamesSorted()) {
            if (!apkDashboard.getPrivateLibErrorTag(nativeLib).isEmpty()) {
                addRow(data, "Native Error ", nativeLib + " " + apkDashboard.getPrivateLibErrorTag(nativeLib));
            }
        }

        String[][] array = new String[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            array[i] = data.get(i);
        }

        element = new ELEMENT(Table.getTable(headers, array).toString(), TAG.DOCUMENT);
        elements.add(element);

    }

    private void addRow(List<String[]> data, String param1, String param2) {
        List<String> row = new LinkedList<>();
        row.add(param1);
        row.add(param2);

        data.add(row.toArray(new String[0]));
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