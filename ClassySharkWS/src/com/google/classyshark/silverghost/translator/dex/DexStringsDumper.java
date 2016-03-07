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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;

public class DexStringsDumper {

    public static List<String> dumpStrings(File apkFile) {
        File file;
        ZipInputStream zipFile;
        List<String> allStrings = new ArrayList<>();

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

                    DexFile dxFile = DexlibLoader.loadDexFile(file);
                    DexBackedDexFile dataPack = (DexBackedDexFile) dxFile;

                    int stringCount = dataPack.getStringCount();

                    allStrings.add(new String("classes" + i + ".dex\n"));
                    for (int strIndex = 0; strIndex < stringCount; strIndex++) {
                        allStrings.add(dataPack.getString(strIndex) + "\n");
                    }

                    file.delete();
                }
            }
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allStrings;
    }

    public static void main(String[] args) throws Exception {
        String apkFile = System.getProperty("user.home") +
                "/Desktop/Scenarios/4 APKs/com.google.samples.apps.iosched-333.apk";

        List<String> allStrings = dumpStrings(new File(apkFile));
    }
}
