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

package com.google.classyshark.ztests;

import com.google.classyshark.contentreader.dex.DexlibLoader;
import com.google.classyshark.translator.Translator;
import com.google.classyshark.translator.TranslatorFactory;
import com.google.classyshark.translator.java.dex.DexlibAdapter;
import java.io.File;
import java.util.Set;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

/**
 * stress test for dex archive
 */
public class StressTestDex {
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
        String sampleClassesDex = "classes1.dex";

        runAllClassesInDex(sampleClassesDex);
    }
}
