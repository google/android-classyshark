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

package com.google.classyshark.gui.panel.io;

import com.google.classyshark.silverghost.contentreader.ContentReader;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.TranslatorFactory;
import com.google.classyshark.silverghost.translator.dex.DexStringsDumper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Is a function : (contentreader|translator) --> text file
 */
public class Export2FileWriter {

    private Export2FileWriter() {
    }

    public static void writeAllClassNames(List<String> allClassNames, File loadedFile)
            throws IOException {
        FileWriter writer = new FileWriter(loadedFile.getName() + "_dump.txt");
        for (String str : allClassNames) {
            writer.write("\n" + str);
        }
        writer.close();
    }

    public static void writeCurrentClass(Translator translator)
            throws IOException {
        FileWriter writer;
        if (translator != null) {
            writer = new FileWriter(translator.getClassName() + "_dump");
            writer.write(translator.toString());
            writer.close();
        }
    }

    public static void writeManifest(File loadedFile)
            throws IOException {

        FileWriter writer;
        if (loadedFile.getName().endsWith(".apk")) {

            Translator translator =
                    TranslatorFactory.createTranslator("AndroidManifest.xml", loadedFile);
            translator.apply();

            writer = new FileWriter(translator.getClassName() + "_dump");
            writer.write(translator.toString());
            writer.close();
        }
    }

    public static void writeAllDexStringTables(File archiveFile) throws Exception {
        if(archiveFile.getName().endsWith(".dex")) {
            return;
        }

        List<String> allStrings = DexStringsDumper.dumpStrings(archiveFile);
        DexStringsDumper.writeAllStrings(new File("all_strings.txt"), allStrings);
    }

    public static void main(String[] args) throws Exception {

        String allAndroid = System.getProperty("user.home") +
                "/Desktop/Scenarios/2 Samples/android.jar";

        ContentReader loader = new ContentReader(new File(allAndroid));
        loader.load();

        writeAllClassNames(loader.getAllClassNames(), new File(allAndroid));
    }
}
