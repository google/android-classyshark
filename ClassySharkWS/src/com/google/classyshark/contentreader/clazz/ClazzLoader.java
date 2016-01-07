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

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.ProtectionDomain;

/**
 *
 */
public class ClazzLoader {

    private ClazzLoader() {}

    private static class ByteArrayClassLoader extends ClassLoader {
        private final ByteBuffer bb;

        public ByteArrayClassLoader(ByteBuffer bb) {
            this.bb = bb;
        }

        public Class findClass(String name) {
            return defineClass((String) null, bb, (ProtectionDomain) null);
        }
    }

    public static Class loadClassFromClassFile(File file) {
        Class clazz = Exception.class;
        try {
            FileChannel roChannel = new RandomAccessFile(file, "r").getChannel();
            ByteBuffer bb =
                    roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) roChannel.size());
            ClazzLoader.ByteArrayClassLoader bacLoader =
                    new ClazzLoader.ByteArrayClassLoader(bb);
            clazz = bacLoader.findClass(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clazz;
    }
}
