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

import com.google.classyshark.silverghost.contentreader.dex.DexReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Multidex {
    private Multidex() {
    }

    public static File extractClassesDexWithClass(String className, File apkFile) {

        // TODO need to delete this file
        File file = new File("classes.dex");
        ZipInputStream zipFile;
        try {
            zipFile = new ZipInputStream(new FileInputStream(
                    apkFile));

            ZipEntry zipEntry;

            int i = 0;
            while (true) {
                zipEntry = zipFile.getNextEntry();

                if (zipEntry == null) {
                    break;
                }

                if (zipEntry.getName().endsWith(".dex")) {
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

                    List<String> classNamesInDex =
                            DexReader.readClassNamesFromDex(file);
                    if (classNamesInDex.contains(className)) {
                        break;
                    }
                }

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
                            fromInnerZip.close();
                            break;
                        }

                        if (innerZipEntry.getName().endsWith(".dex")) {
                            file = File.createTempFile("classes_innerzip", "dex");
                            FileOutputStream fos1 = new FileOutputStream(file);
                            byte[] bytes1 = new byte[1024];

                            while ((length = fromInnerZip.read(bytes1)) >= 0) {
                                fos1.write(bytes1, 0, length);
                            }

                            fos1.close();

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
}
