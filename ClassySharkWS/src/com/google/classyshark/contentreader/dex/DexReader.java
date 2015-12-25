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

package com.google.classyshark.contentreader.dex;

import com.google.classyshark.contentreader.BinaryContentReader;
import com.google.classyshark.contentreader.ContentReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

public class DexReader implements BinaryContentReader {
    private File binaryArchive;
    private List<String> allClassNames = new ArrayList<>();

    public DexReader(File binaryArchive) {
        this.binaryArchive = binaryArchive;
    }

    @Override
    public void read() {
        try {
            allClassNames =
                    readClassNamesFromDex(binaryArchive);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(allClassNames);
    }

    @Override
    public List<String> getClassNames() {
        return allClassNames;
    }

    @Override
    public List<ContentReader.Component> getComponents() {
        return new ArrayList<>();
    }

    public static List<String> readClassNamesFromDex(File binaryArchiveFile) throws Exception {
        DexFile dexFile = DexlibLoader.loadDexFile(binaryArchiveFile);
        List<String> result = new ArrayList<>();

        for (ClassDef classDef : dexFile.getClasses()) {
            result.add(classDef.getType().replaceAll("/", ".").
                    substring(1, classDef.getType().length() - 1));
        }

        Collections.sort(result);
        return result;
    }
}
