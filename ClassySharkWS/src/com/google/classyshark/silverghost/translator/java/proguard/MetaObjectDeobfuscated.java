package com.google.classyshark.silverghost.translator.java.proguard;

import com.google.classyshark.silverghost.translator.java.MetaObject;

public class MetaObjectDeobfuscated extends MetaObject {

    private MetaObject metaObject;

    public MetaObjectDeobfuscated(MetaObject metaObject) {
        this.metaObject = metaObject;
    }

    @Override
    public String getClassGenerics(String name) {
        return metaObject.getClassGenerics(name);
    }

    @Override
    public String getName() {
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
