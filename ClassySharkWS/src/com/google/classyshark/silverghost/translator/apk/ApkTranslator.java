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

package com.google.classyshark.silverghost.translator.apk;

import com.google.classyshark.silverghost.TokensMapper;
import com.google.classyshark.silverghost.contentreader.dex.DexlibLoader;
import com.google.classyshark.silverghost.io.SherlockHash;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.elf.ElfTranslator;
import java.io.File;
import java.io.FileInputStream;
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
import nl.lxtreme.binutils.elf.Elf;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;
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
    public void addMapper(TokensMapper reverseMappings) {

    }

    @Override
    public void apply() {
        apkAnalysis = doInspect(archiveFile);

        for (DexData dexData : apkAnalysis.dexes) {
            ELEMENT element = new ELEMENT("\nclasses" + dexData.index + ".dex", TAG.DOCUMENT);
            elements.add(element);

            element = new ELEMENT(
                    "\nall methods: "
                            + dexData.allMethods
                            + "\nnative methods: "
                            + dexData.nativeMethodsCount
                            + "\n",
                    TAG.DOCUMENT);
            elements.add(element);
        }

        ELEMENT element = new ELEMENT("\nDynamic Symbol Errors", TAG.DOCUMENT);
        elements.add(element);

        for (String error : apkAnalysis.errors) {
            element = new ELEMENT("\n" + error, TAG.DOCUMENT);
            elements.add(element);
        }

        element = new ELEMENT("\n\n\nNative Libraries\n",
                TAG.DOCUMENT);

        elements.add(element);

        Collections.sort(apkAnalysis.nativeLibs);
        for (String nativeLib : apkAnalysis.nativeLibs) {
            element = new ELEMENT(nativeLib, TAG.DOCUMENT);
            elements.add(element);
        }

        element = new ELEMENT("\nNative Dependencies\n",
                TAG.DOCUMENT);

        elements.add(element);

        List<String> sortedNativeDependencies = new LinkedList<>(apkAnalysis.nativeDependencies);
        Collections.sort(sortedNativeDependencies);
        List<String> nativeLibNames = extractLibNames(apkAnalysis.nativeLibs);
        for (String nativeLib : sortedNativeDependencies) {
            element = new ELEMENT(nativeLib + " " +
                    PrivateNativeLibsInspector.check(nativeLib, nativeLibNames) +
                    "\n", TAG.DOCUMENT);
            elements.add(element);
        }


    }

    private static List<String> extractLibNames(List<String> nativeLibs) {
        // libs/x86/lib.so\n ==> libs.so
        LinkedList<String> result = new LinkedList<>();

        for (String nativeLib : nativeLibs) {
            String simpleNativeLibName = nativeLib;

            if (nativeLib.contains("/")) {
                simpleNativeLibName = simpleNativeLibName.substring(simpleNativeLibName.lastIndexOf("/") + 1,
                        simpleNativeLibName.length() - 1);
            }

            result.add(simpleNativeLibName);
        }

        return result;
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
        public Set<String> nativeMethodsClasses = new TreeSet<>();
        public int allMethods = 0;

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
                            + "\nclasses with native methods"
                            + nativeMethodsClasses;
        }
    }

    public static DexData fillAnalysis(int dexIndex, File file) {
        DexData dexData = new DexData(dexIndex);

        try {
            InputStream is = new FileInputStream(file);
            ApplicationVisitor av = new ApkInspectVisitor(dexData);
            ApplicationReader ar = new ApplicationReader(Opcodes.ASM4, is);
            ar.accept(av, 0);

            DexFile dxFile = DexlibLoader.loadDexFile(file);
            DexBackedDexFile dataPack = (DexBackedDexFile) dxFile;
            dexData.allMethods = dataPack.getMethodCount();
        } catch (Exception e) {
        }
        return dexData;
    }

    public String toString() {
        return apkAnalysis.toString();
    }

    private static class ApkAnalysis {
        public List<String> nativeLibs = new ArrayList<>();
        public TreeSet<DexData> dexes = new TreeSet<>();
        public TreeSet<String> nativeDependencies = new TreeSet<>();
        public List<String> errors = new LinkedList<>();

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
                    String fName = "ANALYZER_classes" + dexIndex;
                    String ext = "dex";

                    File file = SherlockHash.INSTANCE.getFileFromZipStream(binaryArchiveFile,
                            zipFile, fName, ext);

                    result.dexes.add(fillAnalysis(dexIndex, file));

                    dexIndex++;
                } else {
                    if (zipEntry.getName().startsWith("lib")) {

                        File nativeLib = ElfTranslator.extractElf(zipEntry.getName(), binaryArchiveFile);

                        Elf dependenciesReader = new Elf(nativeLib);
                        List<String> libraryDependencies = dependenciesReader.getSharedDependencies();
                        for (String dependency : libraryDependencies) {
                            result.nativeDependencies.add(dependency);
                        }

                        DynamicSymbolsInspector dsi = new DynamicSymbolsInspector(dependenciesReader);

                        if (dsi.areErrors()) {
                            result.errors.add(zipEntry.getName() + " " + dsi.getErrors());
                        }

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

                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
            };
        }
    }
}
