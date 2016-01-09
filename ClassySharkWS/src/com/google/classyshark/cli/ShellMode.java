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

package com.google.classyshark.cli;

import com.google.classyshark.silverghost.contentreader.ContentReader;
import com.google.classyshark.silverghost.reducer.Reducer;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.TranslatorFactory;
import com.google.classyshark.silverghost.translator.apk.ApkTranslator;
import com.google.classyshark.gui.panel.io.Export2FileWriter;
import java.io.File;
import java.util.List;

/**
 * shell mode
 */
public class ShellMode {

    private ShellMode() {
    }

    public static void workInShellMode(List<String> args) {
        if (args.get(0).equalsIgnoreCase("-dump")) {
            if (args.size() == 2) {
                processFullDump(args);
            } else {
                processFileDump(args);
            }
        }
        else {
            processApk(args);
        }
    }

    private static void processFullDump(List<String> args) {
        if (!args.get(0).equalsIgnoreCase("-dump")) {
            System.out.println("Wrong -dump argument ==> "
                    + "java -jar ClassyShark.jar -dump FILE");
            return;
        }

        File archiveFile = new File(args.get(1));
        if (!archiveFile.exists()) {
            System.out.println("File doesn't exist ==> "
                    + "java -jar ClassyShark.jar -dump FILE");
            return;
        }

        ContentReader loader = new ContentReader(archiveFile);
        loader.load();
        Reducer reducer = new Reducer(loader.getAllClassNames());
        reducer.reduce("");

        try {
            Export2FileWriter.writeAllClassContents(reducer, archiveFile);
        } catch (Exception e) {
            System.out.println("Internal error - couldn't write file");
        }
    }

    private static void processFileDump(List<String> args) {
        if (!args.get(0).equalsIgnoreCase("-dump")) {
            System.out.println("Wrong -dump argument ==> java -jar ClassyShark.jar "
                    + "-dump FILE full.class.name");
            return;
        }

        File archiveFile = new File(args.get(1));
        if (!archiveFile.exists()) {
            System.out.println("File doesn't exist ==> java -jar ClassyShark.jar "
                    + "-dump FILE full.class.name");
            return;
        }

        ContentReader loader = new ContentReader(archiveFile);
        loader.load();
        Reducer reducer = new Reducer(loader.getAllClassNames());

        Translator translator =
                TranslatorFactory.createTranslator(args.get(2), archiveFile, reducer);

        try {
            translator.apply();
        }
        catch (NullPointerException npe) {
            System.out.println("Class doesn't exist in the archive");
            return;
        }

        try {
            Export2FileWriter.writeCurrentClass(translator);
        } catch (Exception e) {
            System.out.println("Internal error - couldn't write file");
        }
    }

    private static void processApk(List<String> args) {
        if (!args.get(0).equalsIgnoreCase("-inspect")) {
            System.out.println("Wrong -inspect argument ==> java -jar ClassyShark.jar " +
                    "-inspect APK_FILE");
            return;
        }

        File archiveFile = new File(args.get(1));
        if (!archiveFile.exists()) {
            System.out.println("File doesn't exist ==> java -jar ClassyShark.jar " +
                    "-inspect APK_FILE");
            return;
        }

        if (!archiveFile.getName().endsWith(".apk")) {
            System.out.println("Not an apk file ==> java -jar ClassyShark.jar " +
                    "-inspect APK_FILE");
            return;
        }

        Translator translator = new ApkTranslator(archiveFile);

        translator.apply();

        System.out.print(translator);
    }
}
