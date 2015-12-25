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

package com.google.classyshark.translator.elf;

import com.google.classyshark.translator.Translator;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ElfTranslator implements Translator {

    private File archiveFile;

    public ElfTranslator(File archiveFile) {
        this.archiveFile = archiveFile;
    }

    @Override
    public String getClassName() {
        return archiveFile.getName();
    }

    @Override
    public void apply() {

    }

    @Override
    public List<ELEMENT> getElementsList() {
        LinkedList<ELEMENT> result = new LinkedList<>();
        result.add(new ELEMENT("Stay tuned implementing", TAG.ANNOTATION));
        return result;
    }

    @Override
    public List<String> getDependencies() {
        return new LinkedList<>();
    }
}
