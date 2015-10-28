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

package com.google.classyshark.translator.xml;

import com.google.classyshark.translator.Translator;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Is a function : (apk file) --> ,manifest XML text, as list of tokens with tag
 * based om code posted on StackOverflow by Ribo:
 * http://stackoverflow.com/a/4761689/496992
 * <p/>
 * There is a bug that some manifests can't be shown, added a fallback case to display
 * all strings
 */
public class Translator2AndroidXml implements Translator {

    private final File archiveFile;
    private String xml;

    private XmlHighlighter xmlHighlighter = new XmlHighlighter();
    private XmlDecompressor xmlDecompressor = new XmlDecompressor();

    public Translator2AndroidXml(File archiveFile) {
        this.archiveFile = archiveFile;
    }

    @Override
    public String getClassName() {
        return "AndroidManifest.xml";
    }

    @Override
    public void apply() {
        InputStream is = null;
        ZipFile zip = null;
        ByteArrayOutputStream bout = null;
        try {
            long size;

            if (archiveFile.getName().endsWith(".apk")
                    || archiveFile.getName().endsWith(".zip")) {
                zip = new ZipFile(archiveFile);
                ZipEntry mft = zip.getEntry("AndroidManifest.xml");
                size = mft.getSize();
                is = zip.getInputStream(mft);
            } else {
                size = archiveFile.length();
                is = new FileInputStream(archiveFile);
            }

            if (size > Integer.MAX_VALUE) {
                throw new IOException("File larger than " + Integer.MAX_VALUE + " bytes not supported");
            }

            bout = new ByteArrayOutputStream((int)size);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) > 0) {
                bout.write(buffer, 0 , bytesRead);
            }

            this.xml = xmlDecompressor.decompressXml(bout.toByteArray());
        } catch (Exception e) {
            System.err.println("Error reading AndroidManifext.xml " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            closeResource(is);
            closeResource(zip);
            closeResource(bout);
        }
    }

    private void closeResource(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException ex) {
            System.err.println("Error closing resource: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public List<ELEMENT> getElementsList() {
        return xmlHighlighter.getElements(this.xml);
    }

    @Override
    public List<String> getDependencies() {
        // TODO fuzzy logic for permissions etc
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return xml;
    }

    public static void main(String[] args) throws Exception {
        String archiveName = System.getProperty("user.home") +
                "/Desktop/Scenarios/2 Samples/app-debug.apk";
        Translator2AndroidXml t2ax = new Translator2AndroidXml(new File(archiveName));
        t2ax.apply();
        System.out.print(t2ax.toString());
    }
}