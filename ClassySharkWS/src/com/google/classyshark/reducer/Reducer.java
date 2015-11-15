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

package com.google.classyshark.reducer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

/**
 * Is a function : (key, list of classes) --> list of classes, reduced by key
 */
public class Reducer {

    /**
     * strategy to handle formats
     */
    public enum FormatStrategy {
        CLASS {
            @Override
            public List<String> fillAllClassesNames(File binaryArchiveFile)
                    throws Exception {
                List<String> result = new LinkedList<>();
                result.add(ArchiveReader.loadClassFromClassFile(binaryArchiveFile).getName() + ".class");
                return result;
            }
        },
        JAR {
            @Override
            public List<String> fillAllClassesNames(File binaryArchiveFile)
                    throws Exception {
                String fullPath = binaryArchiveFile.getPath();
                List<String> result =
                        ArchiveReader.readClassNamesFromJar(fullPath);
                Collections.sort(result);
                return result;
            }
        },
        APK {
            @Override
            public List<String> fillAllClassesNames(File binaryArchiveFile)
                    throws Exception {

                List<String> result = MULTI_DEX.fillAllClassesNames(binaryArchiveFile);

                // TODO add check for manifest
                result.add(0, "AndroidManifest.xml");

                return result;
            }
        },
        DEX {
            @Override
            public List<String> fillAllClassesNames(File binaryArchiveFile)
                    throws Exception {
                DexFile dexFile = ArchiveReader.get(binaryArchiveFile);
                List<String> result = new ArrayList<>();

                for (ClassDef classDef : dexFile.getClasses()) {
                    result.add(classDef.getType().replaceAll("/", ".").
                            substring(1, classDef.getType().length() - 1));
                }

                Collections.sort(result);
                return result;
            }
        },

        MULTI_DEX {
            @Override
            public List<String> fillAllClassesNames(File binaryArchiveFile)
                    throws Exception {

                List<String> result = new LinkedList<>();

                ZipInputStream zipFile;

                try {
                    zipFile = new ZipInputStream(new FileInputStream(
                            binaryArchiveFile));

                    ZipEntry zipEntry;

                    int i = 0;
                    while (true) {
                        zipEntry = zipFile.getNextEntry();

                        if (zipEntry == null) {
                            break;
                        }

                        if (zipEntry.getName().endsWith(".dex")) {
                            File file = new File("classes" + i + ".dex");
                            file.createNewFile();
                            i++;

                            FileOutputStream fos =
                                    new FileOutputStream(file);
                            byte[] bytes = new byte[1024];
                            int length;
                            while ((length = zipFile.read(bytes)) >= 0) {
                                fos.write(bytes, 0, length);
                            }

                            fos.close();

                            List<String> mm =
                                    Reducer.FormatStrategy.DEX.
                                            fillAllClassesNames(file);

                            result.add(zipEntry.getName().toUpperCase());
                            result.addAll(mm);
                        }
                    }
                    zipFile.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return result;
            }
        };

        public abstract List<String> fillAllClassesNames(File binaryArchiveFile)
                throws Exception;
    }

    private List<String> allClassesNames;
    private List<String> reducedClassNames;
    private final File binaryArchiveFile;
    private FormatStrategy formatStrategy = FormatStrategy.JAR;

    public Reducer(File file) {
        allClassesNames = new ArrayList<>();
        reducedClassNames = new ArrayList<>();
        this.binaryArchiveFile = file;

        if (file.getName().toLowerCase().endsWith(".jar")) {
            formatStrategy = FormatStrategy.JAR;
        } else if (file.getName().toLowerCase().endsWith(".dex")) {
            formatStrategy = FormatStrategy.DEX;
        } else if (file.getName().toLowerCase().endsWith(".apk")) {
            formatStrategy = FormatStrategy.APK;
        } else {
            formatStrategy = FormatStrategy.CLASS;
        }
    }

    public List<String> reduce(String key) {
        List<String> result;

        if (key.isEmpty()) {
            result = firstLoad();
            reducedClassNames.clear();
            return result;
        } else {
            reducedClassNames = fuzzyReduceClasses(key, allClassesNames);
            result = reducedClassNames;
            return result;
        }
    }

    private List<String> firstLoad() {
        List<String> result;

        try {
            if (allClassesNames.isEmpty()) {
                allClassesNames =
                        formatStrategy.fillAllClassesNames(binaryArchiveFile);
            }

            result = allClassesNames;
        } catch (Exception e) {
            result = new ArrayList<>();
        }

        return result;
    }

    public String getAutocompleteClassName() {
        if (!reducedClassNames.isEmpty()) {
            return reducedClassNames.get(0);
        }

        return allClassesNames.get(0);
    }

    public List<String> getAllClassesNames() {
        return Collections.unmodifiableList(allClassesNames);
    }

    private static List<String> fuzzyReduceClasses(String key,
                                                   List<String> list) {
        List<String> result = new ArrayList<>();

        int foundEntryIndex;
        String camelSearchKey;

        for (String entry : list) {
            camelSearchKey = entry;
            camelSearchKey = camelSearchKey.replaceAll("[^A-Z]", "");

            foundEntryIndex = entry.indexOf(key);

            if ((camelSearchKey.equalsIgnoreCase(key))
                    || (foundEntryIndex > -1)) {
                result.add(entry);
            }
        }

        return result;
    }
}