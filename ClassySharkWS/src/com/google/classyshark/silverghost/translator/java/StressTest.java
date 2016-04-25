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

package com.google.classyshark.silverghost.translator.java;

import com.google.classyshark.silverghost.contentreader.ContentReader;
import com.google.classyshark.silverghost.contentreader.dex.DexlibLoader;
import com.google.classyshark.silverghost.contentreader.jar.JarReader;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.TranslatorFactory;
import com.google.classyshark.silverghost.translator.java.dex.DexlibAdapter;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Stress test for classes
 */
public class StressTest {
    public static void runAllClassesInJar(String jarCanonicalPath) throws Exception {
        List<String> allStuff = JarReader.readClassNamesFromJar(
                new File(jarCanonicalPath), new LinkedList<ContentReader.Component>());

        for (String currentClass : allStuff) {
            Translator sourceGenerator = TranslatorFactory.createTranslator(currentClass,
                    new File(jarCanonicalPath));
            sourceGenerator.apply();
            System.out.println(sourceGenerator.toString());
        }
    }

    public static void runAllClassesInDex(String jarCanonicalPath) throws Exception {
        DexFile dexFile = DexlibLoader.loadDexFile(new File(jarCanonicalPath));
        Set<? extends ClassDef> allClassesInDex = dexFile.getClasses();

        for (ClassDef currentClass : allClassesInDex) {
            String normType = DexlibAdapter.getClassStringFromDex(currentClass.getType());
            Translator sourceGenerator = TranslatorFactory.createTranslator(
                    normType, new File(jarCanonicalPath));
            sourceGenerator.apply();
            System.out.println(sourceGenerator.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        String allAndroid = System.getProperty("user.home") +
                "/Desktop/Scenarios/2 Samples/android.jar";

        runAllClassesInJar(allAndroid);
    }
}
