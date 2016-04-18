/*
 * Copyright 2016 Google, Inc.
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

package com.google.classyshark.silverghost;

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
import com.google.classyshark.silverghost.translator.dex.DexMethodsDumper;
import com.google.classyshark.silverghost.translator.dex.DexStringsDumper;
import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class SilverGhostFacade {
    private SilverGhostFacade() {

    }

    public static List<String> getAllClassNames(File archiveFile) {
        ContentReader loader = new ContentReader(archiveFile);
        loader.load();
        return loader.getAllClassNames();
    }

    public static void exportClassFromApk(List<String> args) {
        File apk = new File(args.get(1));
        String className = args.get(2);

        Translator translator =
                TranslatorFactory.createTranslator(className, apk,
                        getAllClassNames(apk));
        try {
            translator.apply();
        } catch (NullPointerException npe) {
            System.err.println("Class doesn't exist in the writeArchive");
            return;
        }

        try {
            Exporter.writeCurrentClass(translator.getClassName(), translator.toString());
        } catch (Exception e) {
            System.err.println("Internal error - couldn't write file");
        }
    }

    public static String getGeneratedClassString(String className, File archiveFile) {
        String result;
        Translator translator =
                TranslatorFactory.createTranslator(className, archiveFile,
                        getAllClassNames(archiveFile));
        try {
            translator.apply();
            result = translator.toString();
        } catch (NullPointerException npe) {
            System.out.println("Class doesn't exist in the writeArchive");
            return "";
        }
        return result;
    }

    public static void inspectApk(List<String> args) {
        if (!new File(args.get(1)).getName().endsWith(".apk")) {
            System.err.println("Not an apk file ==> " +
                    "java -jar ClassyShark.jar " + "-inspect APK_FILE");
            return;
        }

        Translator translator = new ApkTranslator(new File(args.get(1)));
        translator.apply();
        System.out.print(translator);
    }

    public static String getManifest(File archiveFile) {
        if (!archiveFile.getName().endsWith(".apk")) {
            return "";
        }
        Translator translator =
                TranslatorFactory.createTranslator("AndroidManifest.xml", archiveFile);
        translator.apply();
        return translator.toString();
    }

    public static List<String> getAllMethods(File archiveFile) {
        if (!archiveFile.getName().endsWith(".apk")) {
            return new LinkedList<>();
        }
        return DexMethodsDumper.dumpMethods(archiveFile);
    }

    public static void inspectPackages(List<String> args) {
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

    public static List<String> getAllStrings(File archiveFile) {
        if (!archiveFile.getName().endsWith(".apk")) {
            return new LinkedList<>();
        }

        return DexStringsDumper.dumpStrings(archiveFile);
    }

    public static void exportArchive(List<String> args) {
        File apk = new File(args.get(1));

        try {
            Exporter.writeArchive(apk, getAllClassNames(apk));
        } catch (Exception e) {
            System.err.println("Internal error - couldn't write file");
        }
    }
}
