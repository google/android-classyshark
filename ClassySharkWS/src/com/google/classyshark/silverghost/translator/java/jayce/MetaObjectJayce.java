/*
 * Copyright 2016 Google, Inc.
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

package com.google.classyshark.silverghost.translator.java.jayce;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;
import com.google.classyshark.silverghost.contentreader.jar.JayceReader;
import com.google.classyshark.silverghost.translator.java.MetaObject;
import java.util.List;

/**
 * Jayce implementation of MetaObject
 */
public class MetaObjectJayce extends MetaObject {

    private final String id;
    private final JDefinedClassOrInterface jayceClassInfo;

    public MetaObjectJayce(final String id, final JDefinedClassOrInterface jDefinedClassOrInterface)
    {
        this.id = id;
        this.jayceClassInfo = jDefinedClassOrInterface;
        // FIXME: this one is called from SwingWorker thread poll
        // and it looks like a new thread created every time
        JayceReader.ensureThreadConfigInitialized();
    }

    @Override
    public String getClassGenerics(String name) {
        return ""; // FIXME:
    }

    @Override
    public String getName() {
        return null != jayceClassInfo ? jayceClassInfo.getName() : id;
    }

    @Override
    public AnnotationInfo[] getAnnotations() {
        return JayceReader.toAnnotationInfo(jayceClassInfo.getAnnotations());
    }

    @Override
    public int getModifiers() {
        return jayceClassInfo.getModifier();
    }

    @Override
    public String getSuperclass() {
        return null != jayceClassInfo.getSuperClass()
                ? jayceClassInfo.getSuperClass().getName() : null; // FIXME: correct output format?
    }

    @Override
    public String getSuperclassGenerics() {
        return ""; // FIXME:
    }

    @Override
    public InterfaceInfo[] getInterfaces() {
        final List<JInterface> list = jayceClassInfo.getImplements();
        final int size = list.size();
        final InterfaceInfo[] iis = new InterfaceInfo[size];
        for (int i = 0; i < size; i++) {
            final InterfaceInfo ii = iis[i] = new InterfaceInfo();
            ii.interfaceStr = list.get(i).getName();
        }
        return iis;
    }

    @Override
    public FieldInfo[] getDeclaredFields() {
        try {
            final List<JField> list = jayceClassInfo.getFields();
            final int size = list.size();
            final FieldInfo[] fieldInfos = new FieldInfo[size];
            for (int i = 0; i < size; i++) {
                final JField jField = list.get(i);
                final FieldInfo fi = fieldInfos[i] = new FieldInfo();
                fi.typeName = jField.getType().getName();
                fi.modifiers = jField.getModifier();
                fi.name = jField.getName();
                fi.annotations = JayceReader.toAnnotationInfo(jField.getAnnotations());
            }
            return fieldInfos;
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            return new FieldInfo[0];
        }
    }

    @Override
    public ConstructorInfo[] getDeclaredConstructors() {
        final List<JMethod> list = JayceReader.getConstructors(jayceClassInfo);
        final int size = list.size();
        final ConstructorInfo[] cis = new ConstructorInfo[size];
        for (int i=0; i<size; i++) {
            final JMethod c = list.get(i);
            final ConstructorInfo ci = cis[i] = new ConstructorInfo();
            ci.annotations = JayceReader.toAnnotationInfo(c.getAnnotations());
            ci.parameterTypes = JayceReader.toParameterInfo(c.getParams());
            ci.modifiers = c.getModifier();
        }
        return cis;
    }

    @Override
    public MethodInfo[] getDeclaredMethods() {
        try {
            final List<JMethod> list = JayceReader.getMethods(jayceClassInfo);
            final int size = list.size();
            final MethodInfo[] methodInfos = new MethodInfo[size];
            for (int i = 0; i < size; i++) {
                final JMethod jMethod = list.get(i);
                final MethodInfo mi = methodInfos[i] = new MethodInfo();
                mi.annotations = JayceReader.toAnnotationInfo(jMethod.getAnnotations());
                mi.parameterTypes = JayceReader.toParameterInfo(jMethod.getParams());
                mi.modifiers = jMethod.getModifier();
                mi.name = jMethod.getName();
                mi.exceptionTypes = JayceReader.toExceptionInfo(jMethod.getMarker(ThrownExceptionMarker.class));
                mi.returnType = jMethod.getType().getName();
            }
            return methodInfos;
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            return new MethodInfo[0];
        }
    }
}
