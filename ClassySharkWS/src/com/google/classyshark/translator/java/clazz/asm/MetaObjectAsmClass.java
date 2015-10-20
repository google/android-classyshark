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

import com.google.classyshark.reducer.Reducer;
import com.google.classyshark.translator.java.MetaObject;
import java.io.File;
import java.io.InputStream;
import org.objectweb.asm.ClassReader;

public class MetaObjectAsmClass extends MetaObject {

    ClassDetailsFiller classDetailsFiller;

    public MetaObjectAsmClass(String className, File archiveFile) {
        try {
            byte[] bytes =
                    ClassBytesFromJarExtractor.getBytes(className,
                            archiveFile.getAbsolutePath());

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
                "/Desktop/BytecodeViewer 2.9.8.jar");
        String textClass = "jd.cli.Main.class";
        MetaObjectAsmClass moac = new MetaObjectAsmClass(textClass, testFile);

        //MetaObjectAsmClass moac = new MetaObjectAsmClass(Reducer.class);

        MethodInfo[] ddd = moac.getDeclaredMethods();

        System.out.println(ddd);
    }

    public static void main(String[] args) throws Exception {
        testCustomClass();
    }
}
