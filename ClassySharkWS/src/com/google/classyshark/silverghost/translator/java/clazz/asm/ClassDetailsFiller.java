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
import com.google.classyshark.silverghost.translator.java.dex.DexlibAdapter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Type.getArgumentTypes;
import static org.objectweb.asm.Type.getReturnType;

/**
 * ASM class visitor for scanning the class bytes
 */
public class ClassDetailsFiller extends ClassVisitor {
    private MetaObject.AnnotationInfo[] annotationInfo = new MetaObject.AnnotationInfo[0];
    private String name = "";
    private int modifiers = 0;
    private String superClass = "";
    private String superclassGenerics = "";
    private List<MetaObject.InterfaceInfo> interfaces = new ArrayList<>();
    private List<MetaObject.FieldInfo> declaredFields = new ArrayList<>();
    private List<MetaObject.ConstructorInfo> declaredConstructors = new ArrayList<>();
    private List<MetaObject.MethodInfo> declaredMethods = new ArrayList<>();

    public ClassDetailsFiller() {
        super(Opcodes.ASM5);
    }

    public void visit(int version, int access, String name,
                      String signature, String superName,
                      String[] interfaces) {
        this.name = name.replaceAll("/", "\\.");
        this.superClass = superName.replaceAll("/", "\\.");
        this.modifiers = access;

        for (String iface : interfaces) {
            MetaObject.InterfaceInfo ii = new MetaObject.InterfaceInfo();
            ii.interfaceStr = DexlibAdapter.getClassStringFromDex(iface);
            this.interfaces.add(ii);
        }
    }

    public void visitSource(String source, String debug) {
    }

    public void visitOuterClass(String owner, String name, String desc) {
    }

    public AnnotationVisitor visitAnnotation(String desc,
                                             boolean visible) {
        return null;
    }

    public void visitAttribute(Attribute attr) {
    }

    public void visitInnerClass(String name, String outerName,
                                String innerName, int access) {
    }

    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        MetaObject.FieldInfo fi = new
                MetaObject.FieldInfo();

        fi.typeName = DexlibAdapter.getTypeName(desc);
        fi.modifiers = access;
        fi.annotations = new MetaObject.AnnotationInfo[0];
        fi.name = name.replaceAll("/", "\\.");

        declaredFields.add(fi);

        return null;
    }

    public MethodVisitor visitMethod(int access, String name,
                                     String desc, String signature,
                                     String[] exceptions) {
        if (name.equals("<init>")) {
            fillConstructor(access, desc);
            return null;
        }

        MetaObject.MethodInfo mi = new MetaObject.MethodInfo();
        mi.modifiers = access;
        mi.annotations = new MetaObject.AnnotationInfo[0];
        mi.parameterTypes = new MetaObject.ParameterInfo[0];

        // TODO fill exceptions
        mi.exceptionTypes = new MetaObject.ExceptionInfo[0];

        mi.name = name.replaceAll("/", "\\.");
        mi.returnType = DexlibAdapter.getTypeName(getReturnType(desc).toString());

        org.objectweb.asm.Type[] arguments = getArgumentTypes(desc);
        mi.parameterTypes = new MetaObject.ParameterInfo[arguments.length];

        int i = 0;
        for (org.objectweb.asm.Type t : arguments) {
            mi.parameterTypes[i] = new MetaObject.ParameterInfo();
            mi.parameterTypes[i].parameterStr = DexlibAdapter.getTypeName(t.toString());
            i++;
        }

        declaredMethods.add(mi);
        return null;
    }

    private void fillConstructor(int access, String desc) {
        MetaObject.ConstructorInfo ci = new MetaObject.ConstructorInfo();
        ci.modifiers = access;
        ci.annotations = new MetaObject.AnnotationInfo[0];
        ci.parameterTypes = new MetaObject.ParameterInfo[0];

        org.objectweb.asm.Type[] arguments = getArgumentTypes(desc);
        ci.parameterTypes = new MetaObject.ParameterInfo[arguments.length];

        int i = 0;
        for (org.objectweb.asm.Type t : arguments) {
            ci.parameterTypes[i] = new MetaObject.ParameterInfo();
            ci.parameterTypes[i].parameterStr = DexlibAdapter.getTypeName(t.toString());
            i++;
        }

        declaredConstructors.add(ci);
    }

    public void visitEnd() {
    }

    public String getClassGenerics(String name) {
        return "";
    }

    public String getName() {
        return name;
    }

    public MetaObject.AnnotationInfo[] getAnnotationInfo() {
        return annotationInfo;
    }

    public int getModifiers() {
        return modifiers;
    }

    public String getSuperClass() {
        return superClass;
    }

    public String getSuperclassGenerics() {
        return superclassGenerics;
    }

    public MetaObject.InterfaceInfo[] getInterfaces() {
        MetaObject.InterfaceInfo[] array = new MetaObject.InterfaceInfo[interfaces.size()];
        return interfaces.toArray(array);
    }

    public MetaObject.FieldInfo[] getDeclaredFields() {
        MetaObject.FieldInfo[] array = new MetaObject.FieldInfo[declaredFields.size()];
        return declaredFields.toArray(array);
    }

    public MetaObject.ConstructorInfo[] getDeclaredConstructors() {
        MetaObject.ConstructorInfo[] array =
                new MetaObject.ConstructorInfo[declaredConstructors.size()];
        return declaredConstructors.toArray(array);
    }

    public MetaObject.MethodInfo[] getDeclaredMethods() {
        MetaObject.MethodInfo[] array = new MetaObject.MethodInfo[declaredMethods.size()];
        return declaredMethods.toArray(array);
    }

    public static void main(String[] args) throws Exception {
        final File testFile = new File(System.getProperty("user.home") +
                "/Desktop/Scenarios/3 Class/Reducer.class");

        RandomAccessFile f = new RandomAccessFile(testFile, "r");
        byte[] b = new byte[(int) f.length()];
        f.read(b);

        ClassDetailsFiller cp = new ClassDetailsFiller();
        ClassReader cr = new ClassReader(b);
        cr.accept(cp, 0);
    }
}