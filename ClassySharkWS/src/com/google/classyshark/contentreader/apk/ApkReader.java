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

package com.google.classyshark.contentreader.apk;

import com.google.classyshark.contentreader.BinaryContentReader;
import com.google.classyshark.contentreader.ContentReader;
import com.google.classyshark.contentreader.dex.DexReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ApkReader implements BinaryContentReader {

    private File binaryArchive;
    private List<String> allClassNames = new ArrayList<>();

    public ApkReader(File binaryArchive) {
        this.binaryArchive = binaryArchive;
    }

    @Override
    public void read() {
        allClassNames = readClassNamesFromMultidex(binaryArchive);

        // TODO add check for manifest
        allClassNames.add(6, "AndroidManifest.xml");
    }

    @Override
    public List<String> getClassNames() {
        return allClassNames;
    }

    @Override
    public List<ContentReader.Component> getComponents() {
        // TODO add manifest here
        return new ArrayList<>();
    }

    public static List<String> readClassNamesFromMultidex(File binaryArchiveFile) {
        List<String> result = new LinkedList<>();

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
                            DexReader.readClassNamesFromDex(binaryArchiveFile);

                    result.add("classes" + dexIndex + ".dex");
                    result.addAll(classesAtDex);
                    dexIndex++;
                }
            }
            zipFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
