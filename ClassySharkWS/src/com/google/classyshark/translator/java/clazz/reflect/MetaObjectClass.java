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

package com.google.classyshark.translator.java.clazz.reflect;

import com.google.classyshark.reducer.Reducer;
import com.google.classyshark.translator.java.MetaObject;
import com.google.classyshark.translator.java.clazz.TypesToNamesMapper;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * Meta object for class format
 */
public class MetaObjectClass extends MetaObject {
    private Class clazz;

    public MetaObjectClass(Class clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public AnnotationInfo[] getAnnotations() {
        return convertAnnotations(clazz.getAnnotations());
    }

    @Override
    public int getModifiers() {
        return clazz.getModifiers();
    }

    @Override
    public String getSuperclass() {
        if (clazz.getSuperclass() == null) {
            return null;
        }
        return clazz.getSuperclass().getName();
    }

    @Override
    public String getName() {
        return clazz.getName();
    }

    @Override
    public String getClassGenerics(String name) {
        TypeVariable[] tv = clazz.getTypeParameters();
        if (tv.length != 0) {
            String result = getClassGenericsString(tv);
            return result;
        } else {
            return "";
        }
    }

    @Override
    public String getSuperclassGenerics() {
        if (clazz.getSuperclass() != null) {
            TypeVariable[] tv = clazz.getSuperclass().getTypeParameters();
            if (tv.length != 0) {
                String result = getClassGenericsString(tv);
                return result;
            } else {
                return "";
            }
        }
        return "";
    }

    @Override
    public InterfaceInfo[] getInterfaces() {
        List<InterfaceInfo> result = new ArrayList<>();
        for (Class iface : clazz.getInterfaces()) {
            InterfaceInfo ii = new InterfaceInfo();
            ii.interfaceStr = iface.getName();

            TypeVariable[] tv = iface.getTypeParameters();
            if (tv.length != 0) {
                ii.genericsStr = getClassGenericsString(tv);
            }

            result.add(ii);
        }

        InterfaceInfo[] array = new InterfaceInfo[result.size()];
        return result.toArray(array);
    }

    @Override
    public FieldInfo[] getDeclaredFields() {
        Field[] implFields = clazz.getDeclaredFields();
        List<FieldInfo> result = new ArrayList<>();

        for (Field field : implFields) {
            FieldInfo fi = new FieldInfo();
            fi.typeName = field.getType().getName();
            fi.modifiers = field.getModifiers();
            fi.annotations = convertAnnotations(field.getAnnotations());
            fi.name = field.getName();

            Type type = field.getGenericType();
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                fi.genericStr = getFieldGenericsString(pType.getActualTypeArguments());
            }

            result.add(fi);
        }

        FieldInfo[] array = new FieldInfo[result.size()];
        return result.toArray(array);
    }

    @Override
    public ConstructorInfo[] getDeclaredConstructors() {
        Constructor[] implConstructors = clazz.getDeclaredConstructors();
        List<ConstructorInfo> result = new ArrayList<>();

        for (Constructor constructor : implConstructors) {
            ConstructorInfo ci = new ConstructorInfo();
            ci.parameterTypes = convertParameters(constructor.getParameterTypes(), constructor.getGenericParameterTypes());
            ci.annotations = convertAnnotations(constructor.getAnnotations());
            ci.modifiers = constructor.getModifiers();

            result.add(ci);
        }

        ConstructorInfo[] array = new ConstructorInfo[result.size()];
        return result.toArray(array);
    }

    @Override
    public MethodInfo[] getDeclaredMethods() {
        Method[] orMethods = clazz.getDeclaredMethods();
        List<MethodInfo> result = new ArrayList<>();

        for (Method method : orMethods) {
            MethodInfo mi = new MethodInfo();
            mi.parameterTypes = convertParameters(method.getParameterTypes(), method.getGenericParameterTypes());
            mi.annotations = convertAnnotations(method.getAnnotations());
            mi.modifiers = method.getModifiers();
            mi.name = method.getName();
            mi.exceptionTypes = convertExceptions(method.getExceptionTypes());
            mi.returnType = method.getReturnType().getName();

            Type returnType = method.getGenericReturnType();

            if (returnType instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) returnType;
                Type[] typeArguments = type.getActualTypeArguments();

                mi.genericReturnType = getFieldGenericsString(typeArguments) + " ";
            }
            result.add(mi);
        }

        MethodInfo[] array = new MethodInfo[result.size()];
        return result.toArray(array);
    }

    private String getClassGenericsString(TypeVariable[] tv) {
        String result = "<";

        for (TypeVariable t : tv) {
            result += t.getName() + ", ";
        }

        result = result.substring(0, result.length() - 2) + ">";
        return result;
    }

    private AnnotationInfo[] convertAnnotations(Annotation[] annotations) {
        List<AnnotationInfo> result = new ArrayList<>();
        for (Annotation anot : annotations) {
            AnnotationInfo ai = new AnnotationInfo();
            ai.annotationStr = anot.annotationType().getSimpleName();
            result.add(ai);
        }

        AnnotationInfo[] array = new AnnotationInfo[result.size()];
        return result.toArray(array);
    }

    private ParameterInfo[] convertParameters(Class<?>[] parameterTypes, Type[] genericParameterTypes) {
        List<ParameterInfo> result = new ArrayList<>();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class param = parameterTypes[i];
            ParameterInfo pi = new ParameterInfo();
            pi.parameterStr = param.getName();

            if (genericParameterTypes != null && i < genericParameterTypes.length) {
                Type genericParameterType = genericParameterTypes[i];
                if (genericParameterType instanceof ParameterizedType) {
                    ParameterizedType aType = (ParameterizedType) genericParameterType;
                    Type[] parameterArgTypes = aType.getActualTypeArguments();
                    pi.genericStr = getFieldGenericsString(parameterArgTypes);
                }
            }
            result.add(pi);
        }
        ParameterInfo[] array = new ParameterInfo[result.size()];
        return result.toArray(array);
    }

    private String getFieldGenericsString(Type[] actualTypeArguments) {
        if (actualTypeArguments == null || actualTypeArguments.length == 0) {
            return "";
        }
        String result = " <";
        for (Type t : actualTypeArguments) {
            // TODO not sure in java generics spec
            // TODO are generic params evaluated with class params
            result += TypesToNamesMapper.decodeAndStore(t.toString(), null) + ", ";
        }
        result = result.substring(0, result.length() - 2) + ">";
        return result;
    }

    private ExceptionInfo[] convertExceptions(Class<?>[] exceptionTypes) {
        List<ExceptionInfo> result = new ArrayList<>();
        for (Class param : exceptionTypes) {
            ExceptionInfo ei = new ExceptionInfo();
            ei.exceptionStr = param.getName();
            result.add(ei);
        }

        ExceptionInfo[] array = new ExceptionInfo[result.size()];
        return result.toArray(array);
    }

    public static void main(String[] args) throws Exception {
        MetaObjectClass moc = new MetaObjectClass(Reducer.class);
        System.out.println(moc.getAnnotations());
    }
}