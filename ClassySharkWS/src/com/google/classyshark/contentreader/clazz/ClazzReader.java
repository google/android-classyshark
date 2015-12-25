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

package com.google.classyshark.contentreader.clazz;

import com.google.classyshark.contentreader.BinaryContentReader;
import com.google.classyshark.contentreader.ContentReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClazzReader implements BinaryContentReader {

    private File binaryArchive;
    private List<String> allClassNames = new ArrayList<>();

    public ClazzReader(File binaryArchive) {
        this.binaryArchive = binaryArchive;
    }

    @Override
    public void read() {
        allClassNames.add(ClazzLoader.loadClassFromClassFile(binaryArchive).getName());
    }

    @Override
    public List<String> getClassNames() {
        return allClassNames;
    }

    @Override
    public List<ContentReader.Component> getComponents() {
        return new ArrayList<>();
    }
}
