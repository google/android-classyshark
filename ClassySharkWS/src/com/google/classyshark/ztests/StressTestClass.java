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

import com.google.classyshark.reducer.ArchiveReader;
import com.google.classyshark.translator.Translator;
import com.google.classyshark.translator.TranslatorFactory;
import java.io.File;
import java.util.List;

/**
 *  Stress test for classes
 */
public class StressTestClass {
    public static void runAllClassesInArchive(String jarCanonicalPath) throws Exception {
        List<String> allStuff = ArchiveReader.readClassNamesFromJar(
                jarCanonicalPath);

        for (String currentClass : allStuff) {
            Translator sourceGenerator = TranslatorFactory.createTranslator(currentClass,
                    new File(jarCanonicalPath));
            sourceGenerator.apply();
            System.out.println(sourceGenerator.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        String allAndroid = System.getProperty("user.home") +
                "/Desktop/Scenarios/2 Samples/android.jar";

        runAllClassesInArchive(allAndroid);
    }
}
