/*
 * Copyright 2016 Google, Inc.
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

package com.google.classyshark.silverghost.translator.apk.dashboard;

import com.google.classyshark.silverghost.io.SherlockHash;
import com.google.classyshark.silverghost.translator.elf.ElfTranslator;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import nl.lxtreme.binutils.elf.Elf;

import static com.google.classyshark.silverghost.translator.apk.dashboard.ApkDashboard.fillAnalysisPerClassesDexIndex;

public class ApkReader {

    public static class ClassesDexEntry implements Comparable {
        public int index;
        public int nativeMethodsCount = 0;
        public Set<String> classesWithNativeMethods = new TreeSet<>();
        public int allMethods = 0;
        public List<String> syntheticAccessors;

        public ClassesDexEntry(int index) {
            this.index = index;
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof ClassesDexEntry)) {
                return -1;
            }

            return -1 * Integer.valueOf(index).compareTo(((ClassesDexEntry) o).index);
        }

        public String getName() {
            if(index == 0) {
                return "classes.dex";
            } else {
                return "classes" + index + ".dex";
            }
        }

        public String toString() {
            return
                    "\nclasses" + index + ".dex"
                            + "\nnative methods: "
                            + nativeMethodsCount
                            + "\nclasses with native methods"
                            + classesWithNativeMethods;
        }
    }

    public static void fillDashboard(File binaryArchiveFile, ApkDashboard to) {

        try {
            ZipInputStream zipFile = new ZipInputStream(new FileInputStream(
                    binaryArchiveFile));

            ZipEntry zipEntry;

            while (true) {
                zipEntry = zipFile.getNextEntry();

                if (zipEntry == null) {
                    break;
                }

                if (zipEntry.getName().endsWith(".dex")
                        /*|| zipEntry.getName().endsWith(".zip")*/) {

                    String fName = "ANALYZER_classes";

                    int dexIndex = Character.getNumericValue(zipEntry.getName().charAt(zipEntry.getName().length() - 5));

                    // TODO stopped here, need to extract classes.dex entry
                    if(dexIndex == 21 /* zip*/) {
                        dexIndex = 99;
                        fName = "ANALYZER_custom_classes" + dexIndex;
                    } else if (dexIndex != 28 /* classes.dex*/) {
                        fName = "ANALYZER_classes" + dexIndex;
                    } else {
                        /* classes2.dex*/
                        dexIndex = 0;
                    }

                    String ext = "dex";

                    File file = SherlockHash.INSTANCE.getFileFromZipStream(binaryArchiveFile,
                            zipFile, fName, ext);

                    to.classesDexEntries.add(fillAnalysisPerClassesDexIndex(dexIndex, file));

                } else {
                    if (zipEntry.getName().startsWith("lib")) {

                        File nativeLib = ElfTranslator.extractElf(zipEntry.getName(), binaryArchiveFile);

                        Elf dependenciesReader = new Elf(nativeLib);
                        List<String> libraryDependencies = dependenciesReader.getSharedDependencies();
                        for (String dependency : libraryDependencies) {
                            to.nativeDependencies.add(dependency);
                        }

                        DynamicSymbolsInspector dsi = new DynamicSymbolsInspector(dependenciesReader);

                        if (dsi.areErrors()) {
                            to.nativeErrors.add(zipEntry.getName() + " " + dsi.getErrors());
                        }

                        to.nativeLibs.add(zipEntry.getName() + "\n");
                    }
                }
            }
            zipFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}