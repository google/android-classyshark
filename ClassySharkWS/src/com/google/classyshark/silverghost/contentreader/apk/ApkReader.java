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

package com.google.classyshark.silverghost.contentreader.apk;

import com.google.classyshark.silverghost.contentreader.BinaryContentReader;
import com.google.classyshark.silverghost.contentreader.ContentReader;
import com.google.classyshark.silverghost.contentreader.dex.DexReader;
import com.google.classyshark.silverghost.translator.xml.XmlDecompressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ApkReader implements BinaryContentReader {

    private File binaryArchive;
    private List<String> allClassNames = new ArrayList<>();
    private List<ContentReader.Component> components = new ArrayList<>();

    public ApkReader(File binaryArchive) {
        this.binaryArchive = binaryArchive;
    }

    @Override
    public void read() {
        readClassNamesFromMultidex(binaryArchive, allClassNames, components);

        // TODO add check for manifest
//        allClassNames.add(6, "AndroidManifest.xml");
    }

    @Override
    public List<String> getClassNames() {
        return allClassNames;
    }

    @Override
    public List<ContentReader.Component> getComponents() {
        // TODO add manifest here
        return components;
    }

    private static void readClassNamesFromMultidex(File binaryArchiveFile,
                                                   List<String> classNames,
                                                   List<ContentReader.Component> components) {
        ZipInputStream zipFile;
        try {
            zipFile = new ZipInputStream(new FileInputStream(
                    binaryArchiveFile));

            ZipEntry zipEntry;

            int dexIndex = 0;
            while (true) {
                zipEntry = zipFile.getNextEntry();

                if (zipEntry == null) {
                    break;
                }

                if (zipEntry.getName().endsWith(".xml")) {
                    classNames.add(zipEntry.getName());
                }

                if (zipEntry.getName().endsWith(".dex")) {

                    File file = File.createTempFile("classes" + dexIndex, "dex");
                    file.deleteOnExit();

                    FileOutputStream fos =
                            new FileOutputStream(file);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = zipFile.read(bytes)) >= 0) {
                        fos.write(bytes, 0, length);
                    }

                    fos.close();

                    List<String> classesAtDex =
                            DexReader.readClassNamesFromDex(file);

                    classNames.add("classes" + dexIndex + ".dex");
                    classNames.addAll(classesAtDex);
                    dexIndex++;
                }
                if (zipEntry.getName().startsWith("lib")) {
                    components.add(
                            new ContentReader.Component(zipEntry.getName(),
                                    ContentReader.ARCHIVE_COMPONENT.NATIVE_LIBRARY));
                }

                // Dynamic dex loading
                if (zipEntry.getName().endsWith("jar") || zipEntry.getName().endsWith("zip")) {
                    File innerZip = File.createTempFile("inner_zip", "zip");
                    innerZip.deleteOnExit();

                    FileOutputStream fos =
                            new FileOutputStream(innerZip);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = zipFile.read(bytes)) >= 0) {
                        fos.write(bytes, 0, length);
                    }

                    fos.close();

                    // so far we have a zip file
                    ZipInputStream fromInnerZip = new ZipInputStream(new FileInputStream(
                            innerZip));

                    ZipEntry innerZipEntry;

                    while (true) {
                        innerZipEntry = fromInnerZip.getNextEntry();

                        if (innerZipEntry == null) {
                            break;
                        }

                        if (innerZipEntry.getName().endsWith(".dex")) {
                            File  tempDexFile = File.createTempFile("inner_zip_classes" + dexIndex, "dex");
                            tempDexFile.deleteOnExit();

                            FileOutputStream fos1 = new FileOutputStream(tempDexFile);
                            byte[] bytes1 = new byte[1024];

                            while ((length = fromInnerZip.read(bytes1)) >= 0) {
                                fos1.write(bytes1, 0, length);
                            }

                            fos1.close();

                            List<String> classesAtDex =
                                    DexReader.readClassNamesFromDex(tempDexFile);

                            String name = zipEntry.getName() + "###" + innerZipEntry.getName();

                            classNames.add(name);
                            classNames.addAll(classesAtDex);
                        }
                    }
                }
            }
            zipFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
