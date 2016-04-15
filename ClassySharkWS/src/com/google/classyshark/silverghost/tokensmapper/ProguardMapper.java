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

package com.google.classyshark.silverghost.tokensmapper;

import java.util.Map;
import java.util.TreeMap;

public class ProguardMapper implements MappingProcessor {

    public static final ProguardMapper IDENTITY = new ProguardMapper();
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
