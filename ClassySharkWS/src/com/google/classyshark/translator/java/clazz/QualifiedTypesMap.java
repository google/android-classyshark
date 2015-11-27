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

package com.google.classyshark.translator.java.clazz;

import com.google.classyshark.translator.java.dex.DexlibAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Data structure for handling fully qualified class dependencies
 */
public class QualifiedTypesMap {

    private HashMap<String, String> full2types;

    public QualifiedTypesMap() {
        full2types = new HashMap<>();
    }

    public List<String> getFullTypes() {
        List result = new ArrayList(full2types.keySet());
        Collections.sort(result);
        return result;
    }

    public void addType(String type) {
        if (type == null || type.isEmpty()) {
            return;
        }

        decodeAndStore(type, full2types);
    }

    public String getType(String type) {
        if (type == null || type.isEmpty()) {
            return "";
        }

        return decodeAndStore(type, full2types);
    }

    public String getTypeNull(String type) {
        if (type == null || type.isEmpty()) {
            return "";
        }

        return decodeAndStore(type, null);
    }

    public void removeType(String name) {
        full2types.remove(name);
    }

    public static String decodeAndStore(String typeName, HashMap<String, String> hashMap) {
        String result;
        String arr;

        if (!isArray(typeName.charAt(0))) {
            int i = typeName.lastIndexOf(".");
            if (i == -1) {
                return typeName;
            } else {
                result = typeName.substring(i + 1);
                if (hashMap != null) {
                    hashMap.put(typeName, result);
                }
                return result;
            }
        }
        arr = "[]";
        if (isArray(typeName.charAt(1))) {
            result = decodeAndStore(typeName.substring(1), hashMap);
        } else {
            if (typeName.charAt(1) == 'L') {
                result = decodeAndStore(extractReference(typeName), hashMap);
            } else {
                result =
                        DexlibAdapter.primitiveTypes.get(
                                String.valueOf(typeName.charAt(1)));
            }
        }
        return result + arr;
    }

    private static boolean isArray(char typeName) {
        return typeName == '[';
    }

    private static String extractReference(String param) {
        return param.substring(param.indexOf("L") + 1, param.indexOf(";"));
    }
}