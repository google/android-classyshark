package com.google.classyshark.silverghost.translator.java;

import com.google.classyshark.silverghost.tokensmapper.ProguardMapper;
import java.util.Map;
import java.util.TreeMap;

public class MetaObjectWithMapper extends MetaObject {

    private Map<String, String> reverseMappingClasses;
    private MetaObject metaObject;

    public MetaObjectWithMapper(MetaObject metaObject, ProguardMapper reverseMappings) {
        super();

        this.metaObject = metaObject;
        this.reverseMappingClasses = reverseMappings.classes;
    }

    @Override
    public String getClassGenerics(String name) {
        return metaObject.getClassGenerics(name);
    }

    @Override
    public String getName() {
        // TODO not clear why is it null
        if(reverseMappingClasses == null) {
            reverseMappingClasses = new TreeMap<>();
        }

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
