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

package com.google.classyshark.translator.metaobject.dex;

import com.google.classyshark.translator.Translator;
import com.google.classyshark.translator.TranslatorFactory;
import com.google.classyshark.translator.metaobject.MetaObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;

/**
 * Dex implementation of MetaObject
 */
public class MetaObjectDex extends MetaObject {

    private final ClassDef classDef;

    public MetaObjectDex(ClassDef classDef) {
        super();
        this.classDef = classDef;
    }

    @Override
    public String getName() {
        return DexlibAdapter.getClassStringFromDex(classDef.getType());
    }

    @Override
    public InterfaceInfo[] getInterfaces() {
        List<InterfaceInfo> result = new ArrayList<>();
        for (String iface : classDef.getInterfaces()) {
            InterfaceInfo ii = new InterfaceInfo();
            ii.interfaceStr = DexlibAdapter.getClassStringFromDex(iface);
            result.add(ii);
        }

        InterfaceInfo[] array = new InterfaceInfo[result.size()];
        return result.toArray(array);
    }

    @Override
    public FieldInfo[] getDeclaredFields() {
        List<FieldInfo> result = new ArrayList<>();
        Iterable<? extends Field> implFields = classDef.getFields();

        for (Field field : implFields) {
            FieldInfo fi = new FieldInfo();
            fi.typeName = DexlibAdapter.getTypeName(field.getType());
            fi.modifiers = field.getAccessFlags();
            fi.annotations = convertAnnotations(field.getAnnotations());
            fi.name = field.getName();

            result.add(fi);
        }

        FieldInfo[] array = new FieldInfo[result.size()];
        return  result.toArray(array);
    }

    @Override
    public ConstructorInfo[] getDeclaredConstructors() {
        Iterable<? extends Method> implConstructors = classDef.getMethods();
        List<ConstructorInfo> result = new ArrayList<>();

        for (Method constructor : implConstructors) {
            if(isConstructor(constructor)) {
                ConstructorInfo ci = new ConstructorInfo();
                ci.parameterTypes = convertParameters(constructor.getParameters());
                ci.annotations = convertAnnotations(constructor.getAnnotations());
                ci.modifiers = constructor.getAccessFlags();

                result.add(ci);
            }
        }

        ConstructorInfo[] array = new ConstructorInfo[result.size()];
        return  result.toArray(array);
    }

    @Override
    public MethodInfo[] getDeclaredMethods() {
        Iterable<? extends Method> implMethods = classDef.getMethods();
        List<MethodInfo> result = new ArrayList<>();

        for (Method method : implMethods) {
            if(!isConstructor(method)) {
                MethodInfo mi = new MethodInfo();
                mi.parameterTypes = convertParameters(method.getParameters());
                mi.annotations = convertAnnotations(method.getAnnotations());
                mi.modifiers = method.getAccessFlags();
                mi.name = method.getName();
                mi.exceptionTypes = new ExceptionInfo[0];
                mi.returnType = DexlibAdapter.getTypeName(method.getReturnType());

                result.add(mi);
            }
        }

        MethodInfo[] array = new MethodInfo[result.size()];
        return  result.toArray(array);
    }

    @Override
    public AnnotationInfo[] getAnnotations() {
        return convertAnnotations(classDef.getAnnotations());
    }

    private AnnotationInfo[] convertAnnotations(Set<? extends Annotation> annotations) {
        List<AnnotationInfo> result = new ArrayList<>();
        for (Annotation anot : annotations) {
            AnnotationInfo ai = new AnnotationInfo();
            ai.annotationStr = DexlibAdapter.getTypeName(anot.getType());

            result.add(ai);
        }

        AnnotationInfo[] array = new AnnotationInfo[result.size()];
        return result.toArray(array);
    }

    private ParameterInfo[] convertParameters(List<? extends MethodParameter> parameters) {
        List<ParameterInfo> result = new ArrayList<>();
        for (MethodParameter parameter : parameters) {
            ParameterInfo pi = new ParameterInfo();
            pi.parameterStr = DexlibAdapter.getTypeName(parameter.getType());
            result.add(pi);
        }

        ParameterInfo[] array = new ParameterInfo[result.size()];
        return result.toArray(array);
    }

    @Override
    public String getClassGenerics(String name) {
        return "";
    }

    @Override
    public int getModifiers() {
        return classDef.getAccessFlags();
    }

    @Override
    public String getSuperclass() {
        return DexlibAdapter.getClassStringFromDex(classDef.getSuperclass());
    }

    @Override
    public String getSuperclassGenerics() {
        return "";
    }

    private static boolean isConstructor(Method constructor) {
        return constructor.getName().equals("<init>");
    }

    public static void main(String[] args) throws Exception {
        final File testFile = new File(System.getProperty("user.home") +
                "/Desktop/classes.dex");
        String textClass = "com.google.common.collect.ImmutableSortedMap";
        Translator translator = TranslatorFactory.createTranslator(textClass, testFile);
        translator.apply();

        System.out.println(translator.toString());
    }
}
