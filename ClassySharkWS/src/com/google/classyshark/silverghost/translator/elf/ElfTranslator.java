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

package com.google.classyshark.silverghost.translator.elf;


import com.google.classyshark.silverghost.io.SherlockHash;
import com.google.classyshark.silverghost.tokensmapper.ProguardMapper;
import com.google.classyshark.silverghost.translator.Translator;
import nl.lxtreme.binutils.elf.Elf;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.google.classyshark.silverghost.translator.jar.JarInfoTranslator.readableFileSize;

/**
 * translator for the elf binary format
 */
public class ElfTranslator implements Translator {

    private File archiveFile;
    private File resource;
    private String elfName;
    private String dependencies;
    private StringBuilder dynamicSymbols;

    public ElfTranslator(String className, File archiveFile) {
        this.archiveFile = archiveFile;
        this.elfName = className;
        dynamicSymbols = new StringBuilder();
    }

    @Override
    public String getClassName() {
        return archiveFile.getName();
    }

    @Override
    public void addMapper(ProguardMapper reverseMappings) {

    }

    @Override
    public void apply() {
        dependencies = "";
        resource = extractElf(elfName, archiveFile);

        try {
            Elf dependenciesReader = new Elf(resource);
            List<String> libraryDependencies = dependenciesReader.getSharedDependencies();
            for (String dependency : libraryDependencies) {
                dependencies += "    " + dependency + "\n";
            }

            ElfReader dynamicSymbolsReader = ElfReader.read(resource);
            for (String dynVal : dynamicSymbolsReader.getDynamicSymbols()) {
                dynamicSymbols.append("    --  " + dynVal + "\n");
            }

        } catch (Exception e) {

        }
    }

    @Override
    public List<ELEMENT> getElementsList() {
        LinkedList<ELEMENT> result = new LinkedList<>();

        result.add(new ELEMENT("File size - ", TAG.DOCUMENT));
        result.add(new ELEMENT(readableFileSize(resource.length()), TAG.DOCUMENT));

        result.add(new ELEMENT("\n\nNative Dependencies\n\n", TAG.IDENTIFIER));
        result.add(new ELEMENT(this.dependencies, TAG.DOCUMENT));

        result.add(new ELEMENT("\n\n\n\nDynamic Symbols\n\n", TAG.IDENTIFIER));
        result.add(new ELEMENT(this.dynamicSymbols.toString(), TAG.DOCUMENT));
        return result;
    }

    @Override
    public List<String> getDependencies() {
        return new LinkedList<>();
    }

    // TODO currently support only dexes, here is how to do for jar
    // TODO https://github.com/adamheinrich/native-utils/blob/master/NativeUtils.java
    private static File extractElf(String elfName,
                                   File apkFile) {
        File file = new File("classes.dex");
        ZipInputStream zipFile;
        try {
            zipFile = new ZipInputStream(new FileInputStream(apkFile));
            ZipEntry zipEntry;

            while (true) {
                zipEntry = zipFile.getNextEntry();

                if (zipEntry == null) {
                    break;
                }

                if (zipEntry.getName().equals(elfName)) {
                    String fName = elfName;
                    String ext = "so";
                    file = SherlockHash.INSTANCE.getFileFromZipStream(apkFile, zipFile, fName, ext);
                    break;
                }
            }
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
