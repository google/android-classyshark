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

package com.google.classyshark.silverghost.exporter;

import com.google.classyshark.silverghost.contentreader.ContentReader;
import com.google.classyshark.silverghost.methodscounter.ClassNode;
import com.google.classyshark.silverghost.methodscounter.RootBuilder;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.TranslatorFactory;
import com.google.classyshark.silverghost.translator.dex.DexMethodsDumper;
import com.google.classyshark.silverghost.translator.dex.DexStringsDumper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * exports the data to text files
 */
public class Exporter {

    private Exporter() {
    }

    public static void writeArchive(File archive, List<String> allClasses) throws Exception {
        Exporter.writeManifest(archive);
        Exporter.writeClassNames(allClasses);
        Exporter.writeMethods(archive);
        Exporter.writeStringTables(archive);
        Exporter.writeMethodCounts(archive);
    }

    public static void writeCurrentClass(String className, String content)
            throws IOException {
        writeString(className + "_dump", content);
    }

    public static void writeMethodCounts(File archive) {
        File outputFile = new File("method_counts.txt");
        System.out.println(outputFile.toString());
        try (PrintWriter pw = new PrintWriter(outputFile)){
            RootBuilder rootBuilder = new RootBuilder();
            ClassNode classNode = rootBuilder.fillClassesWithMethods(archive);
            MethodCountExporter methodCountExporter = new TreeMethodCountExporter(pw);
            methodCountExporter.exportMethodCounts(classNode);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void writeManifest(File apk) throws IOException {
        if (apk.getName().endsWith(".apk")) {
            Translator translator =
                    TranslatorFactory.createTranslator("AndroidManifest.xml", apk);
            translator.apply();
            writeString(translator.getClassName() + "_dump", translator.toString());
        }
    }

    public static void writeClassNames(List<String> allClassNames)
            throws IOException {
        writeListStrings(new File("all_classes.txt"), allClassNames);
    }

    public static void writeMethods(File archiveFile) {
        if (!archiveFile.getName().endsWith(".apk")) {
            return;
        }

        List<String> allMethods = DexMethodsDumper.dumpMethods(archiveFile);
        writeListStrings(new File("all_methods.txt"), allMethods);
    }

    public static void writeStringTables(File archiveFile) {
        if (!archiveFile.getName().endsWith(".apk")) {
            return;
        }

        List<String> allStrings = DexStringsDumper.dumpStrings(archiveFile);
        writeListStringsChannel(new File("all_strings.txt"), allStrings);
    }

    private static void writeString(String to, String what) throws IOException {
        FileWriter writer;
        writer = new FileWriter(to);
        writer.write(what);
        writer.close();
    }

    private static void writeListStringsChannel(File to, List<String> allStrings) {
        try {
            byte[] buffer = ("                                                     "
                    + "  \n").getBytes();

            FileChannel rwChannel = new RandomAccessFile(to, "rw").getChannel();
            ByteBuffer wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, allStrings.size() * buffer.length);
            for (int i = 0; i < allStrings.size(); i++) {
                wrBuf.put(allStrings.get(i).getBytes());
            }

            rwChannel.close();

        } catch (IOException ioe) {

        }
    }

    private static void writeListStrings(File to, List<String> allStrings) {
        try {
            FileWriter writer = new FileWriter(to);
            for (String str : allStrings) {
                writer.write("\n" + str);
            }
            writer.close();
        } catch (IOException ioe) {

        }
    }

    public static void main(String[] args) throws Exception {

        String allAndroid = System.getProperty("user.home") +
                "/Desktop/Scenarios/2 Samples/android.jar";

        ContentReader loader = new ContentReader(new File(allAndroid));
        loader.load();

        writeClassNames(loader.getAllClassNames());
    }
}
