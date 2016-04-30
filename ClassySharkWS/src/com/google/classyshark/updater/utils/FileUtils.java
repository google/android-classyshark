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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.google.classyshark.updater.utils.NamingUtils.extractCurrentPath;
import static javax.script.ScriptEngine.FILENAME;

/**
 * This class is the one taking care of managing the files: it will download the new version and, when needed, replace
 * the old one with the new.
 *
 * @see #downloadFileFrom(Release) Implementation details
 */
public class FileUtils {

    /**
     * This method will download the new version if and only if it has not been downloaded already.
     * In case the software detects that the same new JAR is already present, it will skip the download and just
     * notify the user about the new version
     */
    public static File downloadFileFrom(Release release) throws IOException {
        File file = new File(NamingUtils.buildNameFrom(release));
        if (!file.exists()) {
            obtainNewJarFrom(release, file);
        }
        return file;
    }

    private static void obtainNewJarFrom(Release release, File file) throws IOException {
        URL url = new URL(release.getDownloadURL());
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
    }

    private void overwriteOld(File file) throws IOException {
        Path path = new File(extractCurrentPath() + File.separator + FILENAME).toPath();
        Files.copy(file.toPath(), path, StandardCopyOption.REPLACE_EXISTING);
        file.delete();
    }
}
