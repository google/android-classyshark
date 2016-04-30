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

public class FileUtils {

    public static File downloadFileFrom(Release release) throws IOException {
        URL url = new URL(release.getDownloadURL());
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        File file = new File(NamingUtils.buildNameFrom(release));
        file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        return file;
    }

    private void overwriteOld(File file) throws IOException {
        Path path = new File(extractCurrentPath() + File.separator + FILENAME).toPath();
        Files.copy(file.toPath(), path, StandardCopyOption.REPLACE_EXISTING);
        file.delete();
    }
}
