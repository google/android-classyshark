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

package com.google.classyshark.ui.panel.io;

import com.google.classyshark.contentreader.ContentReader;
import com.google.classyshark.ui.panel.reducer.Reducer;
import com.google.classyshark.translator.Translator;
import com.google.classyshark.translator.TranslatorFactory;
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

    public static void writeAllClassNames(Reducer reducer, File loadedFile)
            throws IOException {
        List<String> arr = reducer.getAllClassNames();
        FileWriter writer = new FileWriter(loadedFile.getName() + "_dump.txt");
        for (String str : arr) {
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

    public static void writeAllClassContents(Reducer reducer, File loadedFile)
            throws IOException {
        FileWriter writer = new FileWriter("all_dump.txt");
        for (String currentClass : reducer.getAllClassNames()) {
            Translator sourceGenerator = TranslatorFactory.createTranslator(currentClass,
                    loadedFile);
            sourceGenerator.apply();
            writer.write(sourceGenerator.toString());
        }
        writer.close();
    }

    public static void main(String[] args) throws Exception {

        String allAndroid = System.getProperty("user.home") +
                "/Desktop/Scenarios/2 Samples/android.jar";

        ContentReader loader = new ContentReader(new File(allAndroid));
        loader.load();

        Reducer reducer = new Reducer(loader.getAllClassNames());
        reducer.reduce("");

        writeAllClassContents(reducer, new File(allAndroid));
    }
}
