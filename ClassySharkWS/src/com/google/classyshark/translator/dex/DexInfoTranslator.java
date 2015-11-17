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

package com.google.classyshark.translator.dex;

import com.google.classyshark.reducer.ArchiveReader;
import com.google.classyshark.translator.Translator;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;

public class DexInfoTranslator implements Translator {
    private final File archiveFile;
    private String className;
    private List<ELEMENT> elements = new ArrayList<>();

    public DexInfoTranslator(String className, File archiveFile) {
        this.className = className;
        this.archiveFile = archiveFile;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public void apply() {
        try {
            DexFile dxFile = ArchiveReader.get(new File(className));
            DexBackedDexFile dataPack = (DexBackedDexFile) dxFile;

            ELEMENT element = new ELEMENT("\nclasses: " + dataPack.getClassCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\nstrings: " + dataPack.getStringCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\ntypes: " + dataPack.getTypeCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\nprotos: " + dataPack.getProtoCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\nfields: " + dataPack.getFieldCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\nmethods: " + dataPack.getMethodCount(), TAG.DOCUMENT);
            elements.add(element);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ELEMENT> getElementsList() {
        return elements;
    }

    @Override
    public List<String> getDependencies() {
        return new LinkedList<>();
    }
}
