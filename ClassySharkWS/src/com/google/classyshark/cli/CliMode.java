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
import com.google.classyshark.silverghost.exporter.Exporter;
import com.google.classyshark.silverghost.exporter.FlatMethodCountExporter;
import com.google.classyshark.silverghost.exporter.MethodCountExporter;
import com.google.classyshark.silverghost.exporter.TreeMethodCountExporter;
import com.google.classyshark.silverghost.methodscounter.ClassNode;
import com.google.classyshark.silverghost.methodscounter.RootBuilder;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.TranslatorFactory;
import com.google.classyshark.silverghost.translator.apk.ApkTranslator;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

/**
 * Command line mode
 */
public class CliMode {

    // TODO fix the message with packages dump
    // TODO introduce SilverGhost api

    private static final String ERROR_MESSAGE = "Usage: java -jar ClassyShark.jar [-options] <archive> [args...]\n" +
            "           (to execute a ClassyShark on binary archive jar/apk/dex/class)\n" +
            "where options include:\n" +
            "    -open\t  open an archive\n" +
            "    -export\t  export to file \n" +
            "    -inspect  experimental prints apk analysis" +
            "\nwhere args is an optional classname\n";

    private CliMode() {
    }

    public static void with(List<String> args) {
        if (args.size() < 2) {
            System.err.println("missing command line arguments " + "\n\n\n" + ERROR_MESSAGE);
            return;
        }

        File archiveFile = new File(args.get(1));
        if (!archiveFile.exists()) {
            System.err.println("File doesn't exist ==> " + archiveFile + "\n\n\n" + ERROR_MESSAGE);
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
            case "-methodcounts":
                inspectPackages(args);
                break;
            default:
                System.err.println("wrong operand ==> " + operand + "\n\n\n" + ERROR_MESSAGE);
        }
    }

    private static void exportArchive(List<String> args) {
        File apk = new File(args.get(1));
        ContentReader loader = new ContentReader(apk);
        loader.load();

        try {
            Exporter.writeArchive(apk, loader.getAllClassNames());
        } catch (Exception e) {
            System.err.println("Internal error - couldn't write file" + "\n\n\n" + ERROR_MESSAGE);
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
            System.err.println("Class doesn't exist in the writeArchive" + "\n\n\n" +
                    ERROR_MESSAGE);
            return;
        }

        try {
            Exporter.writeCurrentClass(translator.getClassName(), translator.toString());
        } catch (Exception e) {
            System.err.println("Internal error - couldn't write file" + "\n\n\n" + ERROR_MESSAGE);
        }
    }

    private static void inspectApk(List<String> args) {
        if (!new File(args.get(1)).getName().endsWith(".apk")) {
            System.err.println("Not an apk file ==> " +
                    "java -jar ClassyShark.jar " + "-inspect APK_FILE" + "\n\n\n" + ERROR_MESSAGE);
            return;
        }

        Translator translator = new ApkTranslator(new File(args.get(1)));
        translator.apply();
        System.out.print(translator);
    }

    private static void inspectPackages(List<String> args) {
        String fileName = args.get(1);
        File file = new File(fileName);

        if (!file.exists()) {
            System.err.printf("File '%s' does not exist", fileName);
            return;
        }

        RootBuilder rootBuilder = new RootBuilder();

        MethodCountExporter methodCountExporter =
                new TreeMethodCountExporter(new PrintWriter(System.out));
        if (args.size() > 2) {
            for (int i = 2; i < args.size(); i++ ) {
                if (args.get(i).equals("-flat")) {
                    methodCountExporter = new FlatMethodCountExporter(new PrintWriter(System.out));
                }
            }
        }
        ClassNode rootNode = rootBuilder.fillClassesWithMethods(fileName);
        methodCountExporter.exportMethodCounts(rootNode);
    }
}