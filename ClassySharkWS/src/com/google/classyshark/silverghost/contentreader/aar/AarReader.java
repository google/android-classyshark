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

package com.google.classyshark.silverghost.contentreader.aar;

import com.google.classyshark.silverghost.contentreader.BinaryContentReader;
import com.google.classyshark.silverghost.contentreader.ContentReader;
import com.google.classyshark.silverghost.contentreader.jar.JarReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AarReader implements BinaryContentReader {

    private File binaryArchive;
    private List<String> allClassNames = new ArrayList<>();
    private List<ContentReader.Component> components = new ArrayList<>();

    public AarReader(File binaryArchive) {
        this.binaryArchive = binaryArchive;
    }

    @Override
    public void read() {
        try {
            File file = File.createTempFile("classes", "jar");
            file.deleteOnExit();

            OutputStream out = new FileOutputStream(file);
            FileInputStream fin = new FileInputStream(binaryArchive);
            BufferedInputStream bin = new BufferedInputStream(fin);
            ZipInputStream zin = new ZipInputStream(bin);
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                if (ze.getName().endsWith(".jar")) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = zin.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.close();
                    allClassNames.addAll(
                            JarReader.readClassNamesFromJar(file, this.components));
                    break;
                }

                if (ze.getName().equals("AndroidManifest.xml")) {
                    allClassNames.add("AndroidManifest.xml");
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    public List<String> getClassNames() {
        return this.allClassNames;
    }

    @Override
    public List<ContentReader.Component> getComponents() {
        return this.components;
    }
}