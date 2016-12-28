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

package com.google.classyshark.silverghost.translator;

import com.google.classyshark.silverghost.FullArchiveReader;
import com.google.classyshark.silverghost.plugins.EmptyFullArchiveReader;
import com.google.classyshark.silverghost.translator.apk.ApkTranslator;
import com.google.classyshark.silverghost.translator.dex.DexInfoTranslator;
import com.google.classyshark.silverghost.translator.elf.ElfTranslator;
import com.google.classyshark.silverghost.translator.jar.JarInfoTranslator;
import com.google.classyshark.silverghost.translator.java.JavaTranslator;
import com.google.classyshark.silverghost.translator.xml.AndroidXmlTranslator;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Creates translators based on class names and archives
 */
public class TranslatorFactory {

    public static Translator createTranslator(String className, File archiveFile) {
        return createTranslator(className, archiveFile, new LinkedList<String>(), null);
    }


    public static Translator createTranslator(String className, File archiveFile,
                                              List<String> allClassNames) {
        return createTranslator(className, archiveFile, allClassNames, null);
    }


    public static Translator createTranslator(String className, File archiveFile,
                                              List<String> allClassNames, FullArchiveReader fullArchiveReader) {
        if (className.endsWith(".xml")) {
            return new AndroidXmlTranslator(className, archiveFile);
        }

        if (className.endsWith(".dex")) {
            return new DexInfoTranslator(className, archiveFile);
        }

        if (className.endsWith(".jar")) {
            // TODO: does it make any sense to check for jayce subvariant?
            // size does not make much sense as jayce jar may include other files beyond the code
            return new JarInfoTranslator(archiveFile, allClassNames);
        }

        if (className.endsWith(".apk")) {
            return new ApkTranslator(archiveFile);
        }

        if (className.endsWith(".so")) {
            return new ElfTranslator(className, archiveFile);
        }

        if (fullArchiveReader != null &&
                !(fullArchiveReader instanceof EmptyFullArchiveReader)) {
            return fullArchiveReader.buildTranslator(className, archiveFile);
        }

        return new JavaTranslator(className, archiveFile);
    }
}
