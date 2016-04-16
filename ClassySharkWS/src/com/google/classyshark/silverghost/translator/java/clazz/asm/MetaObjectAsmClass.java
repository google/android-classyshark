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

package com.google.classyshark.silverghost.translator.java.clazz.asm;

import com.google.classyshark.silverghost.translator.java.MetaObject;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.objectweb.asm.ClassReader;

/**
 * Meta object for class format, based on ASM parsing
 */
public class MetaObjectAsmClass extends MetaObject {

    ClassDetailsFiller classDetailsFiller;

    public MetaObjectAsmClass(String className, File archiveFile) {

        String classFileName = className + ".class";
        try {
            byte[] bytes =
                    ClassBytesFromJarExtractor.getBytes(classFileName,
                            archiveFile.getAbsolutePath());

            classDetailsFiller = new ClassDetailsFiller();
            ClassReader cr = new ClassReader(bytes);
            cr.accept(classDetailsFiller, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MetaObjectAsmClass(File archiveFile) {
        try {
            Path path = Paths.get(archiveFile.getAbsolutePath());
            byte[] bytes = Files.readAllBytes(path);

            classDetailsFiller = new ClassDetailsFiller();
            ClassReader cr = new ClassReader(bytes);
            cr.accept(classDetailsFiller, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MetaObjectAsmClass(Class clazz) throws Exception {
        super();
        String className = clazz.getName();
        String classAsPath = className.replace('.', '/') + ".class";
        InputStream stream = clazz.getClassLoader().getResourceAsStream(classAsPath);

        classDetailsFiller = new ClassDetailsFiller();
        ClassReader cr = new ClassReader(stream);
        cr.accept(classDetailsFiller, 0);
    }

    @Override
    public String getClassGenerics(String name) {
        return classDetailsFiller.getClassGenerics(name);
    }

    @Override
    public String getName() {
        return classDetailsFiller.getName();
    }

    @Override
    public AnnotationInfo[] getAnnotations() {
        return classDetailsFiller.getAnnotationInfo();
    }

    @Override
    public int getModifiers() {
        return classDetailsFiller.getModifiers();
    }

    @Override
    public String getSuperclass() {
        return classDetailsFiller.getSuperClass();
    }

    @Override
    public String getSuperclassGenerics() {
        return classDetailsFiller.getSuperclassGenerics();
    }

    @Override
    public InterfaceInfo[] getInterfaces() {
        return classDetailsFiller.getInterfaces();
    }

    @Override
    public FieldInfo[] getDeclaredFields() {
        return classDetailsFiller.getDeclaredFields();
    }

    @Override
    public ConstructorInfo[] getDeclaredConstructors() {
        return classDetailsFiller.getDeclaredConstructors();
    }

    @Override
    public MethodInfo[] getDeclaredMethods() {
        return classDetailsFiller.getDeclaredMethods();
    }

    public static void testCustomClass() throws Exception {
        final File testFile = new File(System.getProperty("user.home") +
                "/Desktop/Scenarios/2 Samples/BytecodeViewer 2.9.8.jar");
        String testClass = "jd.cli.AnalyzerPanel";
        MetaObjectAsmClass moac = new MetaObjectAsmClass(testClass, testFile);
        MethodInfo[] methods = moac.getDeclaredMethods();
        System.out.println(methods);
    }

    public static void main(String[] args) throws Exception {
        testCustomClass();
    }
}
