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

package com.google.classyshark.silverghost.plugins;

import com.google.classyshark.silverghost.FullArchiveReader;
import com.google.classyshark.silverghost.TokensMapper;
import com.google.classyshark.silverghost.translator.Translator;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class EmptyFullArchiveReader implements FullArchiveReader{
    @Override
    public void readAsyncArchive(File file) {

    }

    @Override
    public Translator buildTranslator(String className, File archiveFile) {
        return new Translator() {
            @Override
            public String getClassName() {
                return "Empty";
            }

            @Override
            public void addMapper(TokensMapper reverseMappings) {

            }

            @Override
            public void apply() {

            }

            @Override
            public List<ELEMENT> getElementsList() {
                return new LinkedList<>();
            }

            @Override
            public List<String> getDependencies() {
                return new LinkedList<>();
            }
        };
    }
}
