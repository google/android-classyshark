package com.google.classyshark.silverghost.translator.java.proguard;

/**
 * Created by bfarber on 11/04/2016.
 */
public class ClassySharkMethodProcessor implements MappingProcessor {
    @Override
    public boolean processClassMapping(String className, String newClassName) {

        System.out.println("plain " + className + " obfuscated " + newClassName);

        return false;
    }

    @Override
    public void processFieldMapping(String className, String fieldType, String fieldName, String newFieldName) {

    }

    @Override
    public void processMethodMapping(String className, int firstLineNumber, int lastLineNumber, String methodReturnType, String methodName, String methodArguments, String newMethodName) {

    }
}
