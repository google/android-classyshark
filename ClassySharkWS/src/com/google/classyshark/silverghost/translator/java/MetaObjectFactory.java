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

package com.google.classyshark.silverghost.translator.java;

import com.google.classyshark.silverghost.contentreader.dex.DexlibLoader;
import com.google.classyshark.silverghost.translator.java.clazz.asm.MetaObjectAsmClass;
import com.google.classyshark.silverghost.translator.java.clazz.reflect.ClassUtils;
import com.google.classyshark.silverghost.translator.java.clazz.reflect.MetaObjectClass;
import com.google.classyshark.silverghost.translator.java.dex.DexlibAdapter;
import com.google.classyshark.silverghost.translator.java.dex.MetaObjectDex;
import com.google.classyshark.silverghost.translator.java.dex.Multidex;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

/**
 * Factory for creating meta-objects to represent Java's data
 */
public class MetaObjectFactory {
    private MetaObjectFactory() {
    }

    public static MetaObject buildMetaObject(String className, File archiveFile) {
        MetaObject result;

        if (archiveFile.getName().toLowerCase().endsWith(".jar")) {
            result = getMetaObjectFromJar(className, archiveFile);
        } else if (archiveFile.getName().toLowerCase().endsWith(".class")) {
            result = getMetaObjectFromClass(archiveFile);
        } else if (archiveFile.getName().toLowerCase().endsWith(".dex")) {
            result = getMetaObjectFromDex(className, archiveFile);
        } else if (archiveFile.getName().toLowerCase().endsWith(".apk")) {
            result = getMetaObjectFromApk(className, archiveFile);
        } else if (archiveFile.getName().toLowerCase().endsWith(".aar")) {
            result = getMetaObjectFromAar(className, archiveFile);
        } else {
            result = new MetaObjectClass(Exception.class);
        }

        return result;
    }

    private static MetaObject getMetaObjectFromAar(String className, File archiveFile) {
        try {
            File file = File.createTempFile("classes", "jar");
            file.deleteOnExit();

            OutputStream out = new FileOutputStream(file);
            FileInputStream fin = new FileInputStream(archiveFile);
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

                    MetaObject result = getMetaObjectFromJar(className, file);
                    return result;
                }
            }
        } catch (Exception e) {

        }

        return new MetaObjectClass(Exception.class);
    }

    private static MetaObject getMetaObjectFromJar(String className, File archiveFile) {
        MetaObject result = null;
        Class clazz;
        try {
            clazz = ClassUtils.loadClassFromJar(archiveFile.getPath(), className);
        } catch (ClassNotFoundException e) {
            clazz = Exception.class;
        } catch (MalformedURLException e) {
            clazz = Exception.class;
        } catch (NoClassDefFoundError e) {
            // the fallback to ASM case
            result = new MetaObjectAsmClass(className, archiveFile);
            return result;
        }

        result = verifyLoadedClassAndBuildASMFallback(className, archiveFile, result, clazz);

        return result;
    }

    private static MetaObject verifyLoadedClassAndBuildASMFallback(String className, File archiveFile, MetaObject result, Class clazz) {
        try {
            if (clazz.getFields() != null) {
                result = new MetaObjectClass(clazz);
            }

            if (clazz.getMethods() != null) {
                result = new MetaObjectClass(clazz);
            }

            if (clazz.getConstructors() != null) {
                result = new MetaObjectClass(clazz);
            }

            if (clazz.getDeclaredMethods() != null) {
                result = new MetaObjectClass(clazz);
            }
        } catch (NoClassDefFoundError e) {
            result = new MetaObjectAsmClass(className, archiveFile);
        }
        return result;
    }

    private static MetaObject getMetaObjectFromApk(String className, File apk) {
        MetaObject result;
        try {
            File classesDexWithClass =
                    Multidex.extractClassesDexWithClass(className, apk);
            result = getMetaObjectFromDex(className, classesDexWithClass);
        } catch (Exception e) {
            result = new MetaObjectClass(Exception.class);
        }
        return result;
    }

    private static MetaObject getMetaObjectFromDex(String className, File archiveFile) {
        MetaObject result;
        try {
            DexFile dexFile = DexlibLoader.loadDexFile(archiveFile);
            ClassDef classDef = DexlibAdapter.getClassDefByName(className, dexFile);
            result = new MetaObjectDex(classDef);
        } catch (Exception e) {
            result = new MetaObjectClass(Exception.class);
        }
        return result;
    }

    private static MetaObject getMetaObjectFromClass(File archiveFile) {
        MetaObject result = new MetaObjectAsmClass(archiveFile);
        return result;
    }
}
