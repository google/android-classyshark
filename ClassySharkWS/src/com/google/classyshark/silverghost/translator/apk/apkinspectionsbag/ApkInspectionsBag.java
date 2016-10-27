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

package com.google.classyshark.silverghost.translator.apk.apkinspectionsbag;

import com.google.classyshark.silverghost.contentreader.dex.DexlibLoader;
import com.google.classyshark.silverghost.io.SherlockHash;
import com.google.classyshark.silverghost.translator.elf.ElfTranslator;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
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

public class ApkInspectionsBag {

    private File apkFile;
    private ApkInspection apkAnalysis;

    public ApkInspectionsBag(File apkFile) {
        this.apkFile = apkFile;
    }

    public void inspect() {
        // TODO add exception for not calling inspect
        this.apkAnalysis = inspectApk(apkFile);
    }

    public int getNumberOfDexes() {
        return apkAnalysis.dexes.size();
    }

    public String getAllMethodsCountPerDex(int i) {

        // no such thing classes1.dex
        if(i >= 1) { i++;}

        int result = 0;

        for(int j = 0; j < apkAnalysis.dexes.size(); j++ ) {
            if(apkAnalysis.dexes.get(j).index == i) {
                result = apkAnalysis.dexes.get(j).allMethods;
                break;
            }
        }

        return result + "";
    }

    public String getAllNativeMethodsCountPerDex(int i) {

        // no such thing classes1.dex
        if(i >= 1) { i++;}

        int result = 0;

        for(int j = 0; j < apkAnalysis.dexes.size(); j++ ) {
            if(apkAnalysis.dexes.get(j).index == i) {
                result = apkAnalysis.dexes.get(j).nativeMethodsCount;
                break;
            }
        }

        return result + "";
    }

    public List<String> getSyntheticAccessors(int i) {
        return apkAnalysis.dexes.get(i).syntheticAccessors;
    }

    public List<String> getNativeErrors() {
        return apkAnalysis.nativeErrors;
    }

    public List<String> getFullPathNativeLibNamesSorted() {
        Collections.sort(apkAnalysis.nativeLibs);
        return apkAnalysis.nativeLibs;
    }

    public List<String> getNativeLibNamesSorted() {

        Set<String> uniqueDependencies = new LinkedHashSet<>(apkAnalysis.nativeDependencies);
        LinkedList<String> sortedNativeDependencies = new LinkedList<>(uniqueDependencies);
        Collections.sort(sortedNativeDependencies);

        List<String> nativeLibNames =
                extractLibNamesFromFullPaths(getFullPathNativeLibNamesSorted());

        return nativeLibNames;
    }

    public String getPrivateLibErrorTag(String nativeLib) {
        boolean isPrivate = PrivateNativeLibsInspector.isPrivate(nativeLib, getNativeLibNamesSorted());

        if(isPrivate) {
            return " -- private api!";
        } else {
            return "";
        }

    }

    public static Set<String> getClassesWithNativeMethodsPerDexIndex(int dexIndex, File classesDex) {
        ApkInspectionsBag.ClassesDexInspectionsData dexInspectionsData =
                ApkInspectionsBag.fillAnalysisPerClassesDex(dexIndex, classesDex);

        return dexInspectionsData.classesWithNativeMethods;
    }

    public static List<String> extractLibNamesFromFullPaths(List<String> nativeLibs) {
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

    private static ClassesDexInspectionsData fillAnalysisPerClassesDex(int dexIndex, File classesDex) {
        ClassesDexInspectionsData dexData = new ClassesDexInspectionsData(dexIndex);

        try {
            InputStream is = new FileInputStream(classesDex);
            ApplicationVisitor av = new ApkInspectVisitor(dexData);
            ApplicationReader ar = new ApplicationReader(Opcodes.ASM4, is);
            ar.accept(av, 0);

            DexFile dxFile = DexlibLoader.loadDexFile(classesDex);
            DexBackedDexFile dataPack = (DexBackedDexFile) dxFile;
            dexData.allMethods = dataPack.getMethodCount();

            dexData.syntheticAccessors =
                    new SyntheticAccessorsInspector(dxFile).getSyntheticAccessors();
        } catch (Exception e) {
        }
        return dexData;
    }

    private static ApkInspection inspectApk(File binaryArchiveFile) {
        ApkInspection result = new ApkInspection();

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

                    dexIndex = Character.getNumericValue(zipEntry.getName().charAt(zipEntry.getName().length() - 5));

                    String fName = "ANALYZER_classes";
                    if(dexIndex != 28) {
                        fName = "ANALYZER_classes" + dexIndex;
                    } else {
                        dexIndex = 0;
                    }

                    String ext = "dex";

                    File file = SherlockHash.INSTANCE.getFileFromZipStream(binaryArchiveFile,
                            zipFile, fName, ext);

                    result.dexes.add(fillAnalysisPerClassesDex(dexIndex, file));


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
                            result.nativeErrors.add(zipEntry.getName() + " " + dsi.getErrors());
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

    private static class ApkInspection {
        public List<String> nativeLibs = new ArrayList<>();
        public ArrayList<ClassesDexInspectionsData> dexes = new ArrayList<>();
        public ArrayList<String> nativeDependencies = new ArrayList<>();
        public List<String> nativeErrors = new LinkedList<>();

        public String toString() {
            return dexes + "\n\n"
                    + nativeLibs;
        }
    }

    private static class ApkInspectVisitor extends ApplicationVisitor {
        private ClassesDexInspectionsData dexData;

        public ApkInspectVisitor(ClassesDexInspectionsData dexData) {
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
                        dexData.classesWithNativeMethods.add(this.className);
                    }

                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
            };
        }
    }

    private static class ClassesDexInspectionsData implements Comparable {
        public int index;
        public int nativeMethodsCount = 0;
        public Set<String> classesWithNativeMethods = new TreeSet<>();
        public int allMethods = 0;
        public List<String> syntheticAccessors;

        public ClassesDexInspectionsData(int index) {
            this.index = index;
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof ClassesDexInspectionsData)) {
                return -1;
            }

            return Integer.valueOf(index).compareTo(((ClassesDexInspectionsData) o).index);
        }

        public String toString() {
            return
                    "\nclasses" + index + ".dex"
                            + "\nnative methods: "
                            + nativeMethodsCount
                            + "\nclasses with native methods"
                            + classesWithNativeMethods;
        }
    }
}
