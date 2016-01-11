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

package com.google.classyshark.silverghost.methodscounter;

import com.google.classyshark.silverghost.contentreader.dex.DexlibLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;

/**
 *
 */
public class RootBuilder {
    public ClassNode fillClassesWithMethods(File file) {
        if (file.getName().endsWith("jar")) {
            return fillFromJar(file);
        }
        return fillFromDex(file);
    }

    public ClassNode fillClassesWithMethods(String fileName) {
        return fillClassesWithMethods(new File(fileName));
    }

    private ClassNode fillFromJar(File file) {
        ClassNode rootNode = new ClassNode(file.getName());
        try {
            JarFile theJar = new JarFile(file);
            Enumeration<? extends JarEntry> en = theJar.entries();

            while (en.hasMoreElements()) {
                JarEntry entry = en.nextElement();
                if (entry.getName().endsWith(".class")) {
                    ClassParser cp = new ClassParser(
                            theJar.getInputStream(entry), entry.getName());
                    JavaClass jc = cp.parse();
                    ClassInfo classInfo = new ClassInfo(jc.getClassName(),
                            jc.getMethods().length);
                    rootNode.add(classInfo);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + file + ". " + e.getMessage());
            e.printStackTrace(System.err);
        }

        return rootNode;
    }

    private ClassNode fillFromDex(File file) {
        ClassNode rootNode = new ClassNode(file.getName());
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry zipEntry;
            byte[] buffer = new byte[1024];
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.getName().endsWith(".dex")) {
                    continue;
                }

                System.out.println("Parsing " + zipEntry.getName());
                File tempFile = File.createTempFile("classyshark", "dex");

                tempFile.deleteOnExit();
                try (FileOutputStream fout = new FileOutputStream(tempFile)) {
                    int read;
                    while ((read = zipInputStream.read(buffer)) > 0) {
                        fout.write(buffer, 0, read);
                    }
                }

                try {
                    DexFile dxFile = DexlibLoader.loadDexFile(tempFile);

                    Set<? extends ClassDef> classSet = dxFile.getClasses();
                    for (ClassDef o : classSet) {
                        int methodCount = 0;
                        for (Method method : o.getMethods()) {
                            methodCount++;
                        }

                        String translatedClassName = o.getType().replaceAll("\\/", "\\.").substring(1, o.getType().length() - 1);
                        ClassInfo classInfo = new ClassInfo(translatedClassName, methodCount);
                        rootNode.add(classInfo);
                    }

                } catch (Exception ex) {
                    System.err.println("Error parsing Dexfile: " + zipEntry.getName() + ": " + ex.getMessage());
                    ex.printStackTrace(System.err);
                }

            }
        } catch (IOException ex) {
            System.err.println("Error reading file: " + file + ". " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
        return rootNode;
    }
}
