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

package com.google.classyshark.silverghost.translator.jar;

import com.google.classyshark.silverghost.translator.Translator;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Translator for the jar files entry
 */
public class JarInfoTranslator implements Translator {

    private final File jarArchive;
    private final List<String> allClassNames;
    private List<ELEMENT> elements = new ArrayList<>();

    public JarInfoTranslator(File jarArchive, List<String> allClassNames) {
        this.jarArchive = jarArchive;
        this.allClassNames = allClassNames;
    }

    @Override
    public String getClassName() {
        return jarArchive.getName();
    }

    @Override
    public void apply() {
        ELEMENT element =
                new ELEMENT("\nclasses: " + allClassNames.size(), TAG.ANNOTATION);
        elements.add(element);
        element =
                new ELEMENT("\nsize: " + readableFileSize(jarArchive.length()), TAG.ANNOTATION);
        elements.add(element);
    }

    @Override
    public List<ELEMENT> getElementsList() {
        return elements;
    }

    @Override
    public List<String> getDependencies() {
        return new LinkedList<>();
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}