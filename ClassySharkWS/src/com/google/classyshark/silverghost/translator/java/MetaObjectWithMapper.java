package com.google.classyshark.silverghost.translator.java;

import java.util.Map;

public class MetaObjectWithMapper extends MetaObject {

    private final Map<String, String> reverseMappingClasses;
    private MetaObject metaObject;

    public MetaObjectWithMapper(MetaObject metaObject,
                                Map<String, String> reverseMappingClasses) {
        this.metaObject = metaObject;
        this.reverseMappingClasses = reverseMappingClasses;
    }

    @Override
    public String getClassGenerics(String name) {
        return metaObject.getClassGenerics(name);
    }

    @Override
    public String getName() {
        if (reverseMappingClasses.containsKey(metaObject.getName())) {
            return reverseMappingClasses.get(metaObject.getName());
        }

        return metaObject.getName();
    }

    @Override
    public AnnotationInfo[] getAnnotations() {
        return metaObject.getAnnotations();
    }

    @Override
    public int getModifiers() {
        return metaObject.getModifiers();
    }

    @Override
    public String getSuperclass() {
        return metaObject.getSuperclass();
    }

    @Override
    public String getSuperclassGenerics() {
        return metaObject.getSuperclassGenerics();
    }

    @Override
    public InterfaceInfo[] getInterfaces() {
        return metaObject.getInterfaces();
    }

    @Override
    public FieldInfo[] getDeclaredFields() {
        return metaObject.getDeclaredFields();
    }

    @Override
    public ConstructorInfo[] getDeclaredConstructors() {
        return metaObject.getDeclaredConstructors();
    }

    @Override
    public MethodInfo[] getDeclaredMethods() {
        return metaObject.getDeclaredMethods();
    }
}
