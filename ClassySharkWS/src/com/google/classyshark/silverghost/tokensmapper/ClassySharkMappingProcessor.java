package com.google.classyshark.silverghost.tokensmapper;

import java.util.Map;
import java.util.TreeMap;

public class ClassySharkMappingProcessor implements MappingProcessor {

    // TODO enlarge to object with class as a key with
    // TODO methods/fields list
    public Map<String, String> classes = new TreeMap();

    @Override
    public boolean processClassMapping(String className, String newClassName) {

        System.out.println("plain " + className + " obfuscated " + newClassName);
        classes.put(newClassName, className);

        return false;
    }

    @Override
    public void processFieldMapping(String className, String fieldType,
                                    String fieldName, String newFieldName) {

    }

    @Override
    public void processMethodMapping(String className, int firstLineNumber,
                                     int lastLineNumber, String methodReturnType,
                                     String methodName, String methodArguments,
                                     String newMethodName) {

    }
}
