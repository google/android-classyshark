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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

/**
 * Is a function : (dex|jar|apk|class) --> list of class names
 */
public class ArchiveFileReader {

    private ArchiveFileReader() {
    }

    public static List<String> readClassNamesFromJar(String jarCanonicalPath)
            throws Exception {

        List<String> classes = new ArrayList<>();

        JarInputStream jarFile = new JarInputStream(new FileInputStream(
                jarCanonicalPath));
        JarEntry jarEntry;

        String formattedClassName;
        String jarEntryName;

        while (true) {
            jarEntry = jarFile.getNextJarEntry();

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
                    File file = new File("classes" + dexIndex + ".dex");
                    file.createNewFile();

                    FileOutputStream fos =
                            new FileOutputStream(file);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = zipFile.read(bytes)) >= 0) {
                        fos.write(bytes, 0, length);
                    }

                    fos.close();

                    List<String> classesAtDex =
                            ArchiveFileReader.readClassNamesFromDex(binaryArchiveFile);

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

    public static List<String> readClassNamesFromDex(File binaryArchiveFile) throws Exception {
        DexFile dexFile = ArchiveFileReader.loadDexFile(binaryArchiveFile);
        List<String> result = new ArrayList<>();

        for (ClassDef classDef : dexFile.getClasses()) {
            result.add(classDef.getType().replaceAll("/", ".").
                    substring(1, classDef.getType().length() - 1));
        }

        Collections.sort(result);
        return result;
    }

    private static class ByteArrayClassLoader extends ClassLoader {
        private final ByteBuffer bb;

        public ByteArrayClassLoader(ByteBuffer bb) {
            this.bb = bb;
        }

        public Class findClass(String name) {
            return defineClass((String) null, bb, (ProtectionDomain) null);
        }
    }

    public static Class loadClassFromClassFile(File file) {
        Class clazz = Exception.class;
        try {
            FileChannel roChannel = new RandomAccessFile(file, "r").getChannel();
            ByteBuffer bb = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) roChannel.size());
            ArchiveFileReader.ByteArrayClassLoader bacLoader = new ArchiveFileReader.ByteArrayClassLoader(bb);
            clazz = bacLoader.findClass(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clazz;
    }

    public static DexFile loadDexFile(File binaryArchiveFile) throws Exception {
        // TODO optimize
        DexFile newDexFile = DexFileFactory.loadDexFile(binaryArchiveFile,
                19 /*api level*/, true);

        return newDexFile;
    }
}