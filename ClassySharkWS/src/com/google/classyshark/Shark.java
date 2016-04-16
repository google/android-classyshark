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

package com.google.classyshark;

import com.google.classyshark.silverghost.contentreader.ContentReader;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.TranslatorFactory;
import com.google.classyshark.silverghost.translator.dex.DexMethodsDumper;
import com.google.classyshark.silverghost.translator.dex.DexStringsDumper;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * The ClassyShark API usually used by build & continues integration toolchains
 */
public class Shark {

    private File archiveFile;

    private Shark(File archiveFile) {
        this.archiveFile = archiveFile;
    }

    public static Shark with(File archiveFile) {
        return new Shark(archiveFile);
    }

    /**
     * @param className class name to generate such as "com.bumptech.glide.request.target.BaseTarget"
     * @return
     */
    public String getGeneratedClass(String className) {
        String result;
        ContentReader loader = new ContentReader(archiveFile);
        loader.load();

        Translator translator =
                TranslatorFactory.createTranslator(className, archiveFile,
                        loader.getAllClassNames());
        try {
            translator.apply();
            result = translator.toString();
        } catch (NullPointerException npe) {
            System.out.println("Class doesn't exist in the writeArchive");
            return new String();
        }
        return result;
    }

    /**
     * @return list of class names
     */
    public List<String> getAllClassNames() {
        ContentReader loader = new ContentReader(archiveFile);
        loader.load();
        return loader.getAllClassNames();
    }

    /**
     * @return manifest
     */
    public String getManifest() {
        if (!archiveFile.getName().endsWith(".apk")) {
            return new String();
        }
        Translator translator =
                TranslatorFactory.createTranslator("AndroidManifest.xml", archiveFile);
        translator.apply();
        return translator.toString();
    }

    /**
     * @return all methods
     */
    public List<String> getAllMethods() {
        if (!archiveFile.getName().endsWith(".apk")) {
            return new LinkedList<>();
        }
        List<String> allMethods = DexMethodsDumper.dumpMethods(archiveFile);
        return allMethods;
    }

    /**
     * @return all strings from all string tables
     */
    public List<String> getAllStrings() {
        if (!archiveFile.getName().endsWith(".apk")) {
            return new LinkedList<>();
        }

        List<String> allStrings = DexStringsDumper.dumpStrings(archiveFile);
        return allStrings;
    }

    public static void main(String[] args) {
        File apk =
                new File("/Users/bfarber/Desktop/Scenarios/4 APKs/"
                        + "com.google.samples.apps.iosched-333.apk");
        Shark shark = Shark.with(apk);

        System.out.println(
                shark.getGeneratedClass("com.bumptech.glide.request.target.BaseTarget"));
        System.out.println(shark.getAllClassNames());
        System.out.println(shark.getManifest());
        System.out.println(shark.getAllMethods());
        System.out.println(shark.getAllStrings());
    }
}