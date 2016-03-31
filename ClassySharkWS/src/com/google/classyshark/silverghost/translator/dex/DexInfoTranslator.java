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

import com.google.classyshark.silverghost.contentreader.dex.DexlibLoader;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.apk.ApkTranslator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;

/**
 * Translator for the classes.dex entry
 */
public class DexInfoTranslator implements Translator {
    private File apkfile;
    private String dexFileName;
    private int index;
    private List<ELEMENT> elements = new ArrayList<>();

    public DexInfoTranslator(String dexFileName, File apkfile) {
        this.apkfile = apkfile;
        this.dexFileName = dexFileName;
    }

    @Override
    public String getClassName() {
        return dexFileName;
    }

    @Override
    public void apply() {
        try {
            File classesDex = extractClassesDex(dexFileName, apkfile, this);

            DexFile dxFile = DexlibLoader.loadDexFile(classesDex);
            DexBackedDexFile dataPack = (DexBackedDexFile) dxFile;

            ELEMENT element = new ELEMENT("\nclasses: " + dataPack.getClassCount(),
                    TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\nstrings: " + dataPack.getStringCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\ntypes: " + dataPack.getTypeCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\nprotos: " + dataPack.getProtoCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\nfields: " + dataPack.getFieldCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\nmethods: " + dataPack.getMethodCount(), TAG.DOCUMENT);
            elements.add(element);

            element = new ELEMENT("\n\nClasses with Native Calls\n", TAG.DOCUMENT);
            elements.add(element);

            ApkTranslator.DexData dexData = ApkTranslator.fillAnalysis(index,
                    classesDex);

            for (String nativeMethodsClass : dexData.nativeMethodsClasses) {
                element = new ELEMENT(nativeMethodsClass + "\n", TAG.ANNOTATION);
                elements.add(element);
            }

            element = new ELEMENT("\nClasses with Abstract Calls\n", TAG.DOCUMENT);
            elements.add(element);

            for (String abstractMethodsClass : dexData.abstractClasses) {
                element = new ELEMENT(abstractMethodsClass + "\n", TAG.XML_ATTR_NAME);
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

    private static File extractClassesDex(String dexName, File apkFile, DexInfoTranslator diTranslator) {
        File file = new File("classes.dex");
        ZipInputStream zipFile;
        try {
            zipFile = new ZipInputStream(new FileInputStream(apkFile));
            ZipEntry zipEntry;
            int i = 0;
            while (true) {
                zipEntry = zipFile.getNextEntry();

                if (zipEntry == null) {
                    break;
                }

                if (zipEntry.getName().endsWith(".dex")) {
                    String currentClassesDexName = "classes" + i + ".dex";
                    file = File.createTempFile("classes" + i, "dex");
                    file.deleteOnExit();
                    i++;

                    FileOutputStream fos =
                            new FileOutputStream(file);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = zipFile.read(bytes)) >= 0) {
                        fos.write(bytes, 0, length);
                    }

                    fos.close();

                    if (dexName.equals(currentClassesDexName)) {
                        diTranslator.index = i;
                        break;
                    }
                }

                // TODO add here login for custom dex loading
            }
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
