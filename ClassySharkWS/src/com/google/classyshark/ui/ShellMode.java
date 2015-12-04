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

package com.google.classyshark.ui;

import com.google.classyshark.reducer.Reducer;
import com.google.classyshark.ui.panel.io.Export2FileWriter;
import java.io.File;
import java.util.List;

public class ShellMode {

    private ShellMode() {
    }

    public static void workInShellMode(List<String> args) {
        if (args.size() > 2) {
            System.out.println("Too many arguments ==> java -jar ClassyShark.jar -dump FILE");
            return;
        }

        if (args.get(0).equalsIgnoreCase("-dump")) {
            System.out.println("Wrong -dump argument ==> java -jar ClassyShark.jar -dump FILE");
            return;
        }

        File archiveFile = new File(args.get(1));
        if (!archiveFile.exists()) {
            System.out.println("File doesn't exist ==> java -jar ClassyShark.jar -dump FILE");
            return;
        }

        Reducer reducer = new Reducer(archiveFile);
        reducer.reduce("");

        try {
            Export2FileWriter.writeAllClassContents(reducer, archiveFile);
        } catch (Exception e) {
            System.out.println("Internal error - couldn't write file");
        }
    }
}
