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

package com.google.classyshark.silverghost.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipInputStream;

public enum SherlockHash {

    INSTANCE;

    private Map<String, BinaryPack> mapPerBinary = new TreeMap<>();

    private static class BinaryPack {
        private final long timeStamp;
        private Map<String, File> map = new TreeMap<>();

        public BinaryPack(long timeStamp) {
            this.timeStamp = timeStamp;
        }
    }

    public File getFileFromZipStream(File binaryFile, ZipInputStream zipInputStream, String fName,
                                     String ext) throws IOException {
        BinaryPack pack = mapPerBinary.get(binaryFile.getCanonicalPath());
        if (pack == null || pack.timeStamp != binaryFile.lastModified()) {
            pack = new BinaryPack(binaryFile.lastModified());
            mapPerBinary.put(binaryFile.getCanonicalPath(), pack);
        }

        String innerFileName = fName + ext;
        File file = pack.map.get(innerFileName);

        if (file != null) {
            return file;
        }

        file = File.createTempFile(fName, ext);
        file.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] bytes = new byte[1024 * 4];
            int length;
            while ((length = zipInputStream.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
            pack.map.put(innerFileName, file);
        }

        return file;
    }
}
