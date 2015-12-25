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

package com.google.classyshark.contentreader.jar;

import com.google.classyshark.contentreader.BinaryContentReader;
import com.google.classyshark.contentreader.ContentReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarReader implements BinaryContentReader {

    private File binaryArchive;
    private List<String> allClassNames = new ArrayList<>();

    public JarReader(File binaryArchive) {
        this.binaryArchive = binaryArchive;
    }

    @Override
    public void read() {
        try {
            allClassNames =
                    readClassNamesFromJar(binaryArchive);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(allClassNames);

    }

    @Override
    public List<String> getClassNames() {
        return allClassNames;
    }

    @Override
    public List<ContentReader.Component> getComponents() {
        return new ArrayList<>();
    }

    public static List<String> readClassNamesFromJar(File jarFile)
            throws Exception {

        List<String> classes = new ArrayList<>();

        JarInputStream jarFileStream = new JarInputStream(new FileInputStream(
                jarFile));
        JarEntry jarEntry;

        String formattedClassName;
        String jarEntryName;

        while (true) {
            jarEntry = jarFileStream.getNextJarEntry();

            if (jarEntry == null) {
                break;
            }

            jarEntryName = jarEntry.getName();

            if (jarEntryName.endsWith(".class")) {
                formattedClassName = jarEntryName.replaceAll("/", "\\.");
                formattedClassName =
                        formattedClassName.substring(0, formattedClassName.lastIndexOf('.'));
                classes.add(formattedClassName);
            }
        }

        if (classes.isEmpty()) {
            throw new Exception();
        }

        return classes;
    }
}
