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

package com.google.classyshark.contentreader;

import com.google.classyshark.contentreader.apk.ApkReader;
import com.google.classyshark.contentreader.clazz.ClazzReader;
import com.google.classyshark.contentreader.dex.DexReader;
import com.google.classyshark.contentreader.jar.JarReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Is a function : (binary file) --> {contents: classnames, components}
 */
public class ContentReader {

    /**
     * components that are part of jar & apk
     */
    public enum ARCHIVE_COMPONENT {
        ANDROID_MANIFEST, NATIVE_LIBRARY;
    }

    public class Component {
        public String name;
        public ARCHIVE_COMPONENT component;
    }

    private BinaryContentReader formatReader;
    private List<String> allClassNames;

    public ContentReader(File binaryArchive) {
        allClassNames = new ArrayList<>();

        if (binaryArchive.getName().toLowerCase().endsWith(".jar")) {
            formatReader = new JarReader(binaryArchive);
        } else if (binaryArchive.getName().toLowerCase().endsWith(".dex")) {
            formatReader = new DexReader(binaryArchive);
        } else if (binaryArchive.getName().toLowerCase().endsWith(".apk")) {
            formatReader = new ApkReader(binaryArchive);
        } else {
            formatReader = new ClazzReader(binaryArchive);
        }
    }

    public void load() {
        try {
            if (allClassNames.isEmpty()) {
                formatReader.read();
                allClassNames = formatReader.getClassNames();
            }
        } catch (Exception e) {
            allClassNames = new ArrayList<>();
        }
    }

    public List<String> getAllClassNames() {
        // TODO add wrong state exception if read wasn't called
        return Collections.unmodifiableList(allClassNames);
    }
}
