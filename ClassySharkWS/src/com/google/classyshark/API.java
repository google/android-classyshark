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
public class API {

    /**
     *
     * @param archive jar/apk
     * @param className class name to generate such as "com.bumptech.glide.request.target.BaseTarget"
     * @return
     */
    public static String getGeneratedClass(File archive, String className) {
        String result;
        ContentReader loader = new ContentReader(archive);
        loader.load();

        Translator translator =
                TranslatorFactory.createTranslator(className, archive,
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
     *
     * @param archive jar/apk
     * @return list of class names
     */
    public static List<String> getAllClassNames(File archive) {
        ContentReader loader = new ContentReader(archive);
        loader.load();
        return loader.getAllClassNames();
    }

    /**
     *
     * @param apk apk
     * @return manifest
     */
    public static String getManifest(File apk) {
        if (!apk.getName().endsWith(".apk")) {
            return new String();
        }
        Translator translator =
                TranslatorFactory.createTranslator("AndroidManifest.xml", apk);
        translator.apply();
        return translator.toString();
    }

    /**
     *
     * @param apk apk
     * @return all methods
     */
    public static List<String> getAllMethods(File apk) {
        if (!apk.getName().endsWith(".apk")) {
            return new LinkedList<>();
        }
        List<String> allMethods = DexMethodsDumper.dumpMethods(apk);
        return allMethods;
    }

    /**
     *
     * @param apk apk
     * @return all strings from all string tables
     */
    public static List<String> getAllStrings(File apk) {
        if (!apk.getName().endsWith(".apk")) {
            return new LinkedList<>();
        }

        List<String> allStrings = DexStringsDumper.dumpStrings(apk);
        return allStrings;
    }

    public static void main(String[] args) {
        File apk =
                new File( "/Users/bfarber/Desktop/Scenarios/4 APKs/"
                        + "com.google.samples.apps.iosched-333.apk");

        System.out.println(API.getGeneratedClass(apk,
                "com.bumptech.glide.request.target.BaseTarget"));
        System.out.println(API.getAllClassNames(apk));
        System.out.println(API.getManifest(apk));
        System.out.println(API.getAllMethods(apk));
        System.out.println(API.getAllStrings(apk));
    }
}