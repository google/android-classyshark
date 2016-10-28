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

package com.google.classyshark.silverghost.translator.java.dex;

import com.google.classyshark.silverghost.contentreader.ContentReader;
import com.google.classyshark.silverghost.contentreader.dex.DexReader;
import com.google.classyshark.silverghost.io.SherlockHash;
import com.google.classyshark.silverghost.translator.dex.DexInfoTranslator;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Multidex {
    private Multidex() {
    }

    public static void readClassNamesFromMultidex(File binaryArchiveFile,
                                                  List<String> classNames,
                                                  List<ContentReader.Component> components) {
        ZipInputStream zipInputStream;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(
                    binaryArchiveFile));

            ZipEntry zipEntry;

            while (true) {
                zipEntry = zipInputStream.getNextEntry();

                if (zipEntry == null) {
                    break;
                }

                if (zipEntry.getName().endsWith(".xml")) {
                    classNames.add(zipEntry.getName());
                }

                if (zipEntry.getName().endsWith(".dex")) {

                    String fName = zipEntry.getName().substring(0, zipEntry.getName().lastIndexOf("."));
                    String ext = "dex";

                    File file = SherlockHash.INSTANCE.getFileFromZipStream(binaryArchiveFile,
                            zipInputStream, fName, ext);

                    List<String> classesAtDex =
                            DexReader.readClassNamesFromDex(file);

                    classNames.add(fName + ".dex");
                    classNames.addAll(classesAtDex);
                }
                if (zipEntry.getName().startsWith("lib")) {
                    components.add(
                            new ContentReader.Component(zipEntry.getName(),
                                    ContentReader.ARCHIVE_COMPONENT.NATIVE_LIBRARY));
                }

                // Dynamic dex loading, currently one one inner zip is supported
                if (zipEntry.getName().endsWith("jar") || zipEntry.getName().endsWith("zip")) {
                    String fName = "inner_zip";
                    String ext = "zip";

                    File innerZip = SherlockHash.INSTANCE.getFileFromZipStream(binaryArchiveFile,
                            zipInputStream, fName, ext);

                    // so far we have a zip file
                    ZipInputStream fromInnerZip = new ZipInputStream(new FileInputStream(
                            innerZip));

                    ZipEntry innerZipEntry;

                    while (true) {
                        innerZipEntry = fromInnerZip.getNextEntry();

                        if (innerZipEntry == null) {
                            break;
                        }

                        // currently only one is supported
                        if (innerZipEntry.getName().endsWith(".dex")) {
                            fName = "inner_zip_classes";
                            ext = "dex";
                            File tempDexFile =
                                    SherlockHash.INSTANCE.getFileFromZipStream(binaryArchiveFile,
                                            fromInnerZip, fName, ext);

                            List<String> classesAtDex =
                                    DexReader.readClassNamesFromDex(tempDexFile);

                            String name = zipEntry.getName() + "###" + innerZipEntry.getName();

                            classNames.add(name);
                            classNames.addAll(classesAtDex);
                        }
                    }
                }
            }
            zipInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File extractClassesDexWithClass(String className, File apkFile) {

        // TODO need to delete this file
        File file = new File("classes.dex");
        ZipInputStream zipFile;
        try {
            zipFile = new ZipInputStream(new FileInputStream(
                    apkFile));

            ZipEntry zipEntry;

            while (true) {
                zipEntry = zipFile.getNextEntry();

                if (zipEntry == null) {
                    break;
                }

                if (zipEntry.getName().endsWith(".dex")) {
                    String fName =
                            zipEntry.getName().substring(0, zipEntry.getName().lastIndexOf("."));

                    String ext = "dex";

                    file = SherlockHash.INSTANCE.getFileFromZipStream(apkFile,
                            zipFile, fName, ext);

                    List<String> classNamesInDex =
                            DexReader.readClassNamesFromDex(file);
                    if (classNamesInDex.contains(className)) {
                        break;
                    }
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
                            fName = "inner_zip_classes";
                            ext = "dex";
                            file = SherlockHash.INSTANCE.getFileFromZipStream(apkFile, fromInnerZip,
                                    fName, ext);

                            List<String> classNamesInDex =
                                    DexReader.readClassNamesFromDex(file);
                            if (classNamesInDex.contains(className)) {
                                fromInnerZip.close();
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

    public static File extractClassesDex(String dexName, File apkFile, DexInfoTranslator diTranslator) {
        if (apkFile.getName().endsWith(".dex")) {
            return apkFile;
        }

        File file = new File("classes.dex");
        ZipInputStream zipFile;
        try {
            zipFile = new ZipInputStream(new FileInputStream(apkFile));
            ZipEntry zipEntry;

            while (true) {
                zipEntry = zipFile.getNextEntry();

                if (zipEntry == null) {
                    break;
                }

                if (zipEntry.getName().endsWith(".dex")) {


                    String fName = zipEntry.getName().substring(0, zipEntry.getName().lastIndexOf("."));
                    String ext = "dex";

                    String currentClassesDexName = fName + ".dex";

                    file = SherlockHash.INSTANCE.getFileFromZipStream(apkFile,
                            zipFile, fName, ext);

                    if (dexName.equals(currentClassesDexName)) {
                        if (dexName.equals("classes.dex")) {
                            diTranslator.setIndex(0);
                        } else {
                            diTranslator.setIndex(Integer.parseInt(fName.substring(fName.length() - 1)));
                        }


                        break;
                    }
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
                            fName = "inner_zip_classes";
                            ext = "dex";
                            file = SherlockHash.INSTANCE.getFileFromZipStream(apkFile, fromInnerZip, fName, ext);

                            if (dexName.startsWith(zipEntry.getName())) {
                                diTranslator.setIndex(99);
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
