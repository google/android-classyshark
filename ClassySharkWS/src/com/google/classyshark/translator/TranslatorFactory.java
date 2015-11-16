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

package com.google.classyshark.translator;

import com.google.classyshark.translator.java.Translator2Java;
import com.google.classyshark.translator.dex.DexInfoTranslator;
import com.google.classyshark.translator.xml.Translator2AndroidXml;
import java.io.File;

/**
 *  Creates translators based on class names and archives
 */
public class TranslatorFactory {
    public static Translator createTranslator(String className, File archiveFile) {
        if(className.endsWith(".xml")) {
            return new Translator2AndroidXml(archiveFile);
        }

        if (className.endsWith(".dex")) {
           return new DexInfoTranslator(className, archiveFile);
        }

        return new Translator2Java(className, archiveFile);
    }
}
