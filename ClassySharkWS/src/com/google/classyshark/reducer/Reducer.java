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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
                result.add(ArchiveFileReader.loadClassFromClassFile(binaryArchiveFile).getName() + ".class");
                return result;
            }
        },
        JAR {
            @Override
            public List<String> fillAllClassesNames(File binaryArchiveFile)
                    throws Exception {
                String fullPath = binaryArchiveFile.getPath();
                List<String> result =
                        ArchiveFileReader.readClassNamesFromJar(fullPath);
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
                return ArchiveFileReader.readClassNamesFromDex(binaryArchiveFile);
            }
        },
        MULTI_DEX {
            @Override
            public List<String> fillAllClassesNames(File binaryArchiveFile)
                    throws Exception {
                return ArchiveFileReader.readClassNamesFromMultidex(binaryArchiveFile);
            }
        };

        public abstract List<String> fillAllClassesNames(File binaryArchiveFile)
                throws Exception;
    }

    private List<String> allClassNames;
    private List<String> reducedClassNames;
    private final File binaryArchiveFile;
    private FormatStrategy formatStrategy = FormatStrategy.JAR;

    public Reducer(File file) {
        allClassNames = new ArrayList<>();
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
            reducedClassNames = fuzzyReduceClassNames(key, allClassNames);
            result = reducedClassNames;
            return result;
        }
    }

    private List<String> firstLoad() {
        List<String> result;

        try {
            if (allClassNames.isEmpty()) {
                allClassNames =
                        formatStrategy.fillAllClassesNames(binaryArchiveFile);
            }

            result = allClassNames;
        } catch (Exception e) {
            result = new ArrayList<>();
        }

        return result;
    }

    public String getAutocompleteClassName() {
        if (!reducedClassNames.isEmpty()) {
            return reducedClassNames.get(0);
        }

        return allClassNames.get(0);
    }

    public List<String> getAllClassNames() {
        return Collections.unmodifiableList(allClassNames);
    }

    private static List<String> fuzzyReduceClassNames(String key,
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