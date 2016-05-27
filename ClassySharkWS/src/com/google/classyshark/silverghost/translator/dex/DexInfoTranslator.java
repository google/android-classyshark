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
import com.google.classyshark.silverghost.io.SherlockHash;
import com.google.classyshark.silverghost.TokensMapper;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.apk.ApkTranslator;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    public void addMapper(TokensMapper reverseMappings) {

    }

    @Override
    public void apply() {
        try {
            File classesDex = extractClassesDex(dexFileName, apkfile, this);

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

            element = new ELEMENT("\n\nClasses with Native Calls\n", TAG.MODIFIER);
            elements.add(element);

            ApkTranslator.DexData dexData = ApkTranslator.fillAnalysis(index,
                    classesDex);

            for (String nativeMethodsClass : dexData.nativeMethodsClasses) {
                element = new ELEMENT(nativeMethodsClass + "\n", TAG.DOCUMENT);
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
        if (apkFile.getName().endsWith(".dex")) {
            return apkFile;
        }

        File file = new File("classes.dex");
        ZipInputStream zipFile;
        try {
            zipFile = new ZipInputStream(new FileInputStream(apkFile));
            ZipEntry zipEntry;
            int dexIndex = 0;
            while (true) {
                zipEntry = zipFile.getNextEntry();

                if (zipEntry == null) {
                    break;
                }

                if (zipEntry.getName().endsWith(".dex")) {
                    String fName = "classes";
                    if (dexIndex > 0) {
                        fName = fName + dexName;
                    }
                    String ext = "dex";

                    String currentClassesDexName = fName + ".dex";

                    file = SherlockHash.INSTANCE.getFileFromZipStream(apkFile,
                            zipFile, fName, ext);

                    if (dexName.equals(currentClassesDexName)) {
                        diTranslator.index = dexIndex;
                        break;
                    }

                    dexIndex++;
                }

                if (zipEntry.getName().endsWith("jar") || zipEntry.getName().endsWith("zip")) {

                    String fName = "inner_zip";
                    String ext = "zip";

                    File innerZip = SherlockHash.INSTANCE.getFileFromZipStream(apkFile,
                            zipFile, fName, ext);

                    // so far we have a zip file
                    ZipInputStream fromInnerZip = new ZipInputStream(new FileInputStream(
                            innerZip));

                    ZipEntry innerZipEntry;

                    while (true) {
                        innerZipEntry = fromInnerZip.getNextEntry();

                        if (innerZipEntry == null) {
                            fromInnerZip.close();
                            break;
                        }

                        if (innerZipEntry.getName().endsWith(".dex")) {
                            fName = "inner_zip_classes" + dexIndex;
                            ext = "dex";
                            file = SherlockHash.INSTANCE.getFileFromZipStream(apkFile, fromInnerZip, fName, ext);

                            if (dexName.startsWith(zipEntry.getName())) {
                                diTranslator.index = 99;
                                zipFile.close();
                                return file;
                            }
                        }
                    }
                }
            }
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
