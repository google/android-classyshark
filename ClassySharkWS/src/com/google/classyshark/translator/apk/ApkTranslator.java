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

package com.google.classyshark.translator.apk;

import com.google.classyshark.translator.Translator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.ow2.asmdex.ApplicationReader;
import org.ow2.asmdex.ApplicationVisitor;
import org.ow2.asmdex.ClassVisitor;
import org.ow2.asmdex.MethodVisitor;
import org.ow2.asmdex.Opcodes;

/**
 * Translator for the .apk entry
 */
public class ApkTranslator implements Translator {
    private File archiveFile;
    private ApkAnalysis apkAnalysis;
    private List<ELEMENT> elements = new ArrayList<>();

    public ApkTranslator(File archiveFile) {
        this.archiveFile = archiveFile;
    }

    @Override
    public String getClassName() {
        return "";
    }

    @Override
    public void apply() {
        apkAnalysis = doInspect(archiveFile);

        for (DexData dexData : apkAnalysis.dexes) {
            ELEMENT element = new ELEMENT("\nclasses" + dexData.index + ".dex", TAG.DOCUMENT);
            elements.add(element);

            element = new ELEMENT("\nnative methods: "
                    + dexData.nativeMethodsCount
                    + "\nabstract methods: "
                    + dexData.abstractMethodsCount + "\n",
                    TAG.ANNOTATION);
            elements.add(element);
        }

        ELEMENT element = new ELEMENT("\nNative Libraries\n",
                TAG.DOCUMENT);

        elements.add(element);

        Collections.sort(apkAnalysis.nativeLibs);
        for (String nativeLib : apkAnalysis.nativeLibs) {
            element = new ELEMENT(nativeLib, TAG.ANNOTATION);
            elements.add(element);
        }
    }

    @Override
    public List<ELEMENT> getElementsList() {
        return elements;
    }

    @Override
    public List<String> getDependencies() {
        return new LinkedList<>();
    }

    public static class DexData implements Comparable {
        public int index;
        public int nativeMethodsCount = 0;
        public int abstractMethodsCount = 0;
        public Set<String> nativeMethodsClasses = new TreeSet<>();
        public Set<String> abstractClasses = new TreeSet<>();

        public DexData(int index) {
            this.index = index;
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof DexData)) {
                return -1;
            }

            return Integer.valueOf(index).compareTo(((DexData) o).index);
        }

        public String toString() {
            return
                    "\nclasses" + index + ".dex"
                            + "\nnative methods: "
                            + nativeMethodsCount
                            + "\nabstract methods: "
                            + abstractMethodsCount
                            + "\nclasses with native methods"
                            + nativeMethodsClasses
                            + "\nclasses with abstract methods"
                            + abstractClasses;
        }
    }

    public static DexData fillAnalysis(int dexIndex, File file) throws IOException {
        DexData dexData = new DexData(dexIndex);

        InputStream is = new FileInputStream(file);
        ApplicationVisitor av = new ApkInspectVisitor(dexData);
        ApplicationReader ar = new ApplicationReader(Opcodes.ASM4, is);
        ar.accept(av, 0);

        return dexData;
    }

    public String toString() {
        return apkAnalysis.toString();
    }

    private static class ApkAnalysis {
        public List<String> nativeLibs = new ArrayList<>();
        public TreeSet<DexData> dexes = new TreeSet<>();

        public String toString() {
            return dexes + "\n\n"
                    + nativeLibs;
        }
    }

    private static ApkAnalysis doInspect(File binaryArchiveFile) {
        ApkAnalysis result = new ApkAnalysis();

        try {
            ZipInputStream zipFile = new ZipInputStream(new FileInputStream(
                    binaryArchiveFile));

            ZipEntry zipEntry;

            int dexIndex = 0;
            while (true) {
                zipEntry = zipFile.getNextEntry();

                if (zipEntry == null) {
                    break;
                }

                if (zipEntry.getName().endsWith(".dex")) {
                    File file = File.createTempFile("ANALYZER_classes" + dexIndex, "dex");
                    file.deleteOnExit();

                    FileOutputStream fos =
                            new FileOutputStream(file);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = zipFile.read(bytes)) >= 0) {
                        fos.write(bytes, 0, length);
                    }

                    fos.close();

                    result.dexes.add(fillAnalysis(dexIndex, file));

                    dexIndex++;
                } else {
                    if (zipEntry.getName().startsWith("lib")) {
                        result.nativeLibs.add(zipEntry.getName() + "\n");
                    }
                }
            }
            zipFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private static class ApkInspectVisitor extends ApplicationVisitor {
        private DexData dexData;

        public ApkInspectVisitor(DexData dexData) {
            super(Opcodes.ASM4);
            this.dexData = dexData;
        }

        public ClassVisitor visitClass(int access, String name, String[] signature,
                                       String superName, String[] interfaces) {

            final String mName = name;

            return new ClassVisitor(Opcodes.ASM4) {
                private String className = mName.replaceAll("\\/", "\\.").substring(1, mName.length() - 1);

                @Override
                public void visit(int version, int access, String name, String[] signature,
                                  String superName, String[] interfaces) {
                    super.visit(version, access, name, signature, superName, interfaces);
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String desc,
                                                 String[] signature, String[] exceptions) {
                    if (Modifier.isNative(access)) {
                        dexData.nativeMethodsCount++;
                        dexData.nativeMethodsClasses.add(this.className);
                    }
                    if (Modifier.isAbstract(access)) {
                        dexData.abstractMethodsCount++;
                        dexData.abstractClasses.add(this.className);
                    }

                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
            };
        }
    }
}
