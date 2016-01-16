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

package com.google.classyshark.gui.panel.io;

import java.io.File;

public class FileChooserUtils {
    private FileChooserUtils(){}

    public static boolean acceptFile(File f) {
        if (f.isDirectory()) {
            return true;
        } else {
            return isSupportedArchiveFile(f);
        }
    }

    public static String getFileChooserDescription() {
        return "dex, jar, apk, class, aar";
    }

    private static boolean isSupportedArchiveFile(File f) {
        String filename = f.getName().toLowerCase();
        return filename.endsWith(".dex")
                || filename.endsWith(".jar")
                || filename.endsWith(".apk")
                || filename.endsWith(".class")
                || filename.endsWith(".aar");
    }
}
