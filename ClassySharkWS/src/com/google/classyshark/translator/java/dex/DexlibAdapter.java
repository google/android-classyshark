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

package com.google.classyshark.translator.java.dex;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for dexlib to fit the engine APIs
 */
public class  DexlibAdapter {

    public static final Map<String, String> primitiveTypes;
    static {
        primitiveTypes = new HashMap<>();
        primitiveTypes.put("I", "int");
        primitiveTypes.put("V", "void");
        primitiveTypes.put("C", "char");
        primitiveTypes.put("D", "double");
        primitiveTypes.put("F", "float");
        primitiveTypes.put("J", "long");
        primitiveTypes.put("S", "short");
        primitiveTypes.put("Z", "boolean");
        primitiveTypes.put("B", "byte");
    }

    public static String getTypeName(String dexlibType) {
        String result;
        if (dexlibType.length() == 1) {
            result = primitiveTypes.get(dexlibType);
        } else {
            result = getClassStringFromDex(dexlibType);
        }

        return result;
    }

    public static ClassDef getClassDefByName(String className, DexFile dexFile)
            throws Exception {
        ClassDef result = null;
        String dexName;

        for (ClassDef currentClassDef : dexFile.getClasses()) {
            dexName = currentClassDef.getType();
            if (isMatchFromDex(className, dexName)) {
                result = currentClassDef;
                break;
            }
        }

        return result;
    }

    public static boolean isMatchFromDex(String className, String dexName) {
        String convertedDexName = getClassStringFromDex(dexName);
        return convertedDexName.equals(className);
    }

    public static String getClassStringFromDex(String dexName) {
        String convertedDexName = dexName.replaceAll("/", ".");

        if (convertedDexName.startsWith("[")) {
            return convertedDexName;
        }

        convertedDexName = convertedDexName.substring(1, convertedDexName.length() - 1);

        return convertedDexName;
    }
}
