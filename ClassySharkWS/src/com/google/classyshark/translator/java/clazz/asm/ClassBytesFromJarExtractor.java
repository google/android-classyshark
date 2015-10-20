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

package com.google.classyshark.translator.java.clazz.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Extractor of class bytes from jar
 * based on
 * http://stackoverflow.com/questions/31482847/read-bytes-from-a-class-file-within-a-jar-file
 */
public class ClassBytesFromJarExtractor {
    public static byte[] getBytes(String fullClassName, String jar) throws IOException {
        // ... inputs check omitted ...
        try (JarFile jarFile = new JarFile(jar)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.getName().endsWith(".class")) {

                    String qualiName = entry.getName().replaceAll("/", "\\.");
                    if (qualiName.equalsIgnoreCase(fullClassName)) {
                        try (InputStream inputStream = jarFile.getInputStream(entry)) {
                            return getBytes(inputStream);
                        } catch (IOException ioException) {
                            System.out.println("Could not obtain class entry for " + entry.getName());
                            throw ioException;
                        }
                    }
                }
            }
        }
        throw new IOException("File not found");
    }

    public static byte[] getBytes(InputStream is) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[0xFFFF];
            for (int len; (len = is.read(buffer)) != -1; )
                os.write(buffer, 0, len);
            os.flush();
            return os.toByteArray();
        }
    }

    private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void main(String[] args) {
        try {
            byte[] bytes = getBytes("jd.cli.Main.class", System.getProperty("user.home") +
                    "/Desktop/BytecodeViewer.jar");
            System.out.println(bytesToHex(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
