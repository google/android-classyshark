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

package com.google.classyshark.ui.panel.displayarea;

import com.google.classyshark.reducer.Reducer;
import com.google.classyshark.translator.Translator;
import com.google.classyshark.translator.TranslatorFactory;
import java.io.File;
import java.io.FileWriter;

/**
 * export writer
 */
public class ExportFileWriter {

    public static void writeExport(Reducer reducer, File loadedFile) throws Exception {
        FileWriter writer = new FileWriter("all_dump.txt");
        for (String currentClass : reducer.getAllClassesNames()) {
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

        Reducer reducer = new Reducer(new File(allAndroid));
        reducer.reduce("");

        writeExport(reducer,
                new File(allAndroid));
    }
}
