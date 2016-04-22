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
import com.google.classyshark.silverghost.io.SherlockHash;
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

            int dexIndex = 0;
            while (true) {
                zipEntry = zipFile.getNextEntry();

                if (zipEntry == null) {
                    break;
                }

                if (zipEntry.getName().endsWith(".dex")) {
                    String fName = "classes";
                    if(dexIndex > 0) {
                        fName = fName + dexIndex;
                    }
                    String ext = "dex";

                    file = SherlockHash.INSTANCE.getFileFromZipStream(apkFile,
                            zipFile, fName, ext);

                    List<String> classNamesInDex =
                            DexReader.readClassNamesFromDex(file);
                    if (classNamesInDex.contains(className)) {
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
}
