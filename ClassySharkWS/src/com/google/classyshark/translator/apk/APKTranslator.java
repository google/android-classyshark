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
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
    private APKAnalysis apkAnalysis;
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
        ELEMENT element = new ELEMENT("\nmethods: " + apkAnalysis.toString(),
                TAG.ANNOTATION);
        elements.add(element);
    }

    @Override
    public List<ELEMENT> getElementsList() {
        return elements;
    }

    @Override
    public List<String> getDependencies() {
        return new LinkedList<>();
    }

    public String toString() {
        return apkAnalysis.toString();
    }

    private static class DexData implements Comparable {
        public int index;
        public int nativeMethodsCount = 0;
        public int abstractMethodsCount = 0;

        public DexData(int index) {
            this.index = index;
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof DexData)) {
                return -1;
            }

            return new Integer(this.index).compareTo(new Integer(((DexData) o).index));
        }

        public String toString() {
            return
                    "\nclasses" + index + ".dex"
                            + "\nnative methods: "
                            + nativeMethodsCount
                            + nativeRecommendation()
                            + "\nabstract methods: "
                            + abstractMethodsCount
                            + abstractRecommendation()
                            + "\n\n";
        }

        private String abstractRecommendation() {
            if (abstractMethodsCount > 0) {
                return " too many abstract methods might cause LinearAlloc";
            }

            return "";
        }

        private String nativeRecommendation() {
            if (nativeMethodsCount > 0) {
                return " native methods in dexes - "
                        + "check native methods calls to Java code in secondary"
                        + " dexes (if you have)";
            }

            return "";
        }
    }

    private static class APKAnalysis {
        public List<String> nativeLibs = new ArrayList<>();
        public TreeSet<DexData> dexes = new TreeSet<>();

        public String toString() {
            return dexes + "\n\n\n"
                    + "Take a look at native libs, make sure you don't miss anything \n"
                    + nativeLibs;
        }
    }

    private static APKAnalysis doInspect(File binaryArchiveFile) {
        APKAnalysis result = new APKAnalysis();

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

                    DexData dexData = new DexData(dexIndex);
                    result.dexes.add(dexData);
                    InputStream is = new FileInputStream(file);
                    ApplicationVisitor av = new ApkInspectVisitor(dexData);
                    ApplicationReader ar = new ApplicationReader(Opcodes.ASM4, is);
                    ar.accept(av, 0);

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

            return new ClassVisitor(Opcodes.ASM4) {
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
                    }
                    if (Modifier.isAbstract(access)) {
                        dexData.abstractMethodsCount++;
                    }

                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
            };
        }
    }
}
