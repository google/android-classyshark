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

package com.google.classyshark.translator.java;

/**
 * Meta object representation for class output
 */
public abstract class MetaObject {

    /**
     * data class for interfaces
     */
    public class InterfaceInfo {
        public String interfaceStr;
        public String genericsStr = "";
    }

    /**
     * data class for fields
     */
    public static class FieldInfo {
        public String typeName;
        public int modifiers;
        public AnnotationInfo[] annotations;
        public String name;
        public String genericStr = "";
    }

    /**
     * data class for constructors
     */
    public static class ConstructorInfo {
        public AnnotationInfo[] annotations;
        public ParameterInfo[] parameterTypes;
        public int modifiers;
    }

    /**
     * data class for methods
     */
    public static class MethodInfo {
        public AnnotationInfo[] annotations;
        public ParameterInfo[] parameterTypes;
        public int modifiers;
        public String name;
        public ExceptionInfo[] exceptionTypes;
        public String returnType;
        public String genericReturnType = "";
    }

    /**
     * data class for annotations
     */
    public static class AnnotationInfo {
        public String annotationStr;
    }

    /**
     * data class for parameters
     */
    public static class ParameterInfo {
        public String parameterStr;
        public String genericStr = "";
    }

    /**
     * data class for exceptions
     */
    public class ExceptionInfo {
        public String exceptionStr;
    }

    public abstract String getClassGenerics(String name);

    public abstract String getName();

    public abstract AnnotationInfo[] getAnnotations();

    public abstract int getModifiers();

    public abstract String getSuperclass();

    public abstract String getSuperclassGenerics();

    public abstract InterfaceInfo[] getInterfaces();

    public abstract FieldInfo[] getDeclaredFields();

    public abstract ConstructorInfo[] getDeclaredConstructors();

    public abstract MethodInfo[] getDeclaredMethods();
}
