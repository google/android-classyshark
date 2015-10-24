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

package com.google.classyshark.ui.tabs.tabpanel.displayarea;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Writes the actual file to the file system
 */
public class FileStubGenerator {

    private FileStubGenerator() {}

    public static void generateStubFile(String className, String classBody) {
       try {
           Files.write(Paths.get("./" + className + ".java"), classBody.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
