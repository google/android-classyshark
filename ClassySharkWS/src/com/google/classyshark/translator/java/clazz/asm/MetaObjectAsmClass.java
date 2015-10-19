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

package com.google.classyshark.translator.java.clazz.asm;

import com.google.classyshark.translator.java.MetaObject;
import java.io.File;
import org.objectweb.asm.ClassReader;

public class MetaObjectAsmClass extends MetaObject {

    private final String className;
    private final File archiveFile;

    public MetaObjectAsmClass(String className, File archiveFile) {
        this.className = className;
        this.archiveFile = archiveFile;

        try {
            byte[] bytes =
                    ClassBytesFromJarExtractor.getBytes(className,
                            archiveFile.getAbsolutePath());

            ClassPrinter cp = new ClassPrinter();
            ClassReader cr = new ClassReader(bytes);
            cr.accept(cp, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getClassGenerics(String name) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public AnnotationInfo[] getAnnotations() {
        return new AnnotationInfo[0];
    }

    @Override
    public int getModifiers() {
        return 0;
    }

    @Override
    public String getSuperclass() {
        return null;
    }

    @Override
    public String getSuperclassGenerics() {
        return null;
    }

    @Override
    public InterfaceInfo[] getInterfaces() {
        return new InterfaceInfo[0];
    }

    @Override
    public FieldInfo[] getDeclaredFields() {
        return new FieldInfo[0];
    }

    @Override
    public ConstructorInfo[] getDeclaredConstructors() {
        return new ConstructorInfo[0];
    }

    @Override
    public MethodInfo[] getDeclaredMethods() {
        return new MethodInfo[0];
    }

    public static void testCustomClass() throws Exception {
        final File testFile = new File(System.getProperty("user.home") +
                "/Desktop/BytecodeViewer.jar");
        String textClass = "jd.cli.Main.class";

        MetaObjectAsmClass moac = new MetaObjectAsmClass(textClass, testFile);
    }

    public static void main(String[] args) throws Exception {
        testCustomClass();
    }
}
