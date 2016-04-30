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

package com.google.classyshark.updater.utils;


import com.google.classyshark.updater.models.Release;
import com.sun.istack.internal.NotNull;

import java.io.File;
import java.nio.file.Paths;

public class NamingUtils {

    private static final String PREFIX = "ClassyShark";
    private static final String SUFFIX = ".jar";

    final String FILENAME = "ClassyShark.jar";

    static String buildNameFrom(@NotNull Release release) {
        String[] creationDates = release.getCreatedAt().split("T");
        String timeStamp = "";
        if (creationDates.length > 0) {
            timeStamp = "_" + creationDates[0];
        }

        return extractCurrentPath() + File.separator + PREFIX + timeStamp + SUFFIX;
    }

    static String extractCurrentPath() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }
}
