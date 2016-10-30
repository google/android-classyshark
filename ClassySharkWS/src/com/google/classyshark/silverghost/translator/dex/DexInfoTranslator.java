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

package com.google.classyshark.silverghost.translator.dex;

import com.google.classyshark.silverghost.TokensMapper;
import com.google.classyshark.silverghost.contentreader.dex.DexlibLoader;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.jar.JarInfoTranslator;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;

import static com.google.classyshark.silverghost.translator.apk.dashboard.ApkDashboard.getClassesWithNativeMethodsPerDexIndex;
import static com.google.classyshark.silverghost.translator.java.dex.MultidexReader.extractClassesDex;

/**
 * Translator for the classes.dex entry
 */
public class
DexInfoTranslator implements Translator {
    private File apkFile;
    private String dexFileName;
    private int index;
    private List<ELEMENT> elements = new ArrayList<>();

    public DexInfoTranslator(String dexFileName, File apkFile) {
        this.apkFile = apkFile;
        this.dexFileName = dexFileName;
    }

    @Override
    public String getClassName() {
        return dexFileName;
    }

    @Override
    public void addMapper(TokensMapper reverseMappings) {

    }

    @Override
    public void apply() {
        try {
            elements.clear();

            File classesDex = extractClassesDex(dexFileName, apkFile, this);

            DexFile dxFile = DexlibLoader.loadDexFile(classesDex);
            DexBackedDexFile dataPack = (DexBackedDexFile) dxFile;

            ELEMENT element = new ELEMENT("\nclasses: " + dataPack.getClassCount(),
                    TAG.MODIFIER);
            elements.add(element);
            element = new ELEMENT("\nstrings: " + dataPack.getStringCount(), TAG.DOCUMENT);
            elements.add(element);
            element = new ELEMENT("\ntypes: " + dataPack.getTypeCount(), TAG.DOCUMENT);
            elements.add(element);
            element = new ELEMENT("\nprotos: " + dataPack.getProtoCount(), TAG.DOCUMENT);
            elements.add(element);
            element = new ELEMENT("\nfields: " + dataPack.getFieldCount(), TAG.DOCUMENT);
            elements.add(element);
            element = new ELEMENT("\nmethods: " + dataPack.getMethodCount(), TAG.IDENTIFIER);
            elements.add(element);

            element = new ELEMENT("\n\nFile size: " +
                    JarInfoTranslator.readableFileSize(classesDex.length()), TAG.DOCUMENT);
            elements.add(element);

            element = new ELEMENT("\n\nClasses with Native Calls\n", TAG.MODIFIER);
            elements.add(element);

            Set<String> classesWithNativeMethods = getClassesWithNativeMethodsPerDexIndex(index, classesDex);

            for (String classWithNativeMethods : classesWithNativeMethods) {
                element = new ELEMENT(classWithNativeMethods + "\n", TAG.DOCUMENT);
                elements.add(element);
            }

        } catch (Exception e) {
            e.printStackTrace();
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

    public void setIndex(int index) {
        this.index = index;
    }
}
