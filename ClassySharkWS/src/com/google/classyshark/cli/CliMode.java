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

import com.google.classyshark.silverghost.exporter.Exporter;
import com.google.classyshark.silverghost.contentreader.ContentReader;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.TranslatorFactory;
import com.google.classyshark.silverghost.translator.apk.ApkTranslator;
import java.io.File;
import java.util.List;

/**
 * Command line mode
 */
public class CliMode {

    private CliMode() {
    }

    public static void with(List<String> args) {
        File archiveFile = new File(args.get(1));
        if (!archiveFile.exists()) {
            System.err.println("File doesn't exist ==> " + archiveFile);
            return;
        }

        final String operand = args.get(0).toLowerCase();
        switch (operand) {
            case "-export":
                if (args.size() == 2) {
                    exportArchive(args);
                } else {
                    exportClassFromApk(args);
                }
                break;
            case "-inspect":
                inspectApk(args);
                break;
            default:
                System.err.println("wrong operand ==> " + operand);
        }
    }

    private static void exportArchive(List<String> args) {
        File apk = new File(args.get(1));
        ContentReader loader = new ContentReader(apk);
        loader.load();

        try {
            Exporter.writeArchive(apk, loader.getAllClassNames());
        } catch (Exception e) {
            System.out.println("Internal error - couldn't write file");
        }
    }

    private static void exportClassFromApk(List<String> args) {
        File apk = new File(args.get(1));
        String className = args.get(2);

        ContentReader loader = new ContentReader(apk);
        loader.load();

        Translator translator =
                TranslatorFactory.createTranslator(className, apk,
                        loader.getAllClassNames());
        try {
            translator.apply();
        } catch (NullPointerException npe) {
            System.out.println("Class doesn't exist in the writeArchive");
            return;
        }

        try {
            Exporter.writeCurrentClass(translator);
        } catch (Exception e) {
            System.out.println("Internal error - couldn't write file");
        }
    }

    private static void inspectApk(List<String> args) {
        if (!new File(args.get(1)).getName().endsWith(".apk")) {
            System.out.println("Not an apk file ==> " +
                    "java -jar ClassyShark.jar " + "-inspect APK_FILE");
            return;
        }

        Translator translator = new ApkTranslator(new File(args.get(1)));
        translator.apply();
        System.out.print(translator);
    }
}
