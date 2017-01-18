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

package com.google.classyshark.silverghost.translator.apk.dashboard;

import com.google.classyshark.silverghost.contentreader.dex.DexlibLoader;
import com.google.classyshark.silverghost.translator.apk.dashboard.manifest.ManifestInspector;
import com.google.classyshark.silverghost.translator.java.dex.MultidexReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;
import org.ow2.asmdex.ApplicationReader;
import org.ow2.asmdex.ApplicationVisitor;
import org.ow2.asmdex.Opcodes;

public class ApkDashboard implements Iterable<ClassesDexDataEntry> {

    public ArrayList<ClassesDexDataEntry> classesDexEntries = new ArrayList<>();
    public ArrayList<ClassesDexDataEntry> customClassesDexEntries = new ArrayList<>();
    public List<String> nativeLibs = new ArrayList<>();
    public ArrayList<String> nativeDependencies = new ArrayList<>();
    public List<String> nativeErrors = new ArrayList<>();
    public List<String> allClasses = new ArrayList<>();
    public File apkFile;

    public ApkDashboard(File apkFile) {
        this.apkFile = apkFile;
    }

    public void inspect() {
        // TODO add exception for not calling inspect
        MultidexReader.fillApkDashboard(apkFile, this);
    }

    public Iterator<ClassesDexDataEntry> iterator() {
        return new ClassesDexIterator();
    }

    public List<String> getNativeErrors() {
        return nativeErrors;
    }

    public List<String> getFullPathNativeLibNamesSorted() {
        Collections.sort(nativeLibs);
        return nativeLibs;
    }

    public List<String> getNativeLibNamesSorted() {
        Set<String> uniqueDependencies = new LinkedHashSet<>(nativeDependencies);
        LinkedList<String> sortedNativeDependencies = new LinkedList<>(uniqueDependencies);
        Collections.sort(sortedNativeDependencies);

        List<String> nativeLibNames =
                extractLibNamesFromFullPaths(getFullPathNativeLibNamesSorted());

        return nativeLibNames;
    }

    public String getPrivateLibErrorTag(String nativeLib) {
        boolean isPrivate = PrivateNativeLibsInspector.isPrivate(nativeLib, getNativeLibNamesSorted());

        if (isPrivate) {
            return " -- private api!";
        } else {
            return "";
        }
    }

    public static Set<String> getClassesWithNativeMethodsPerDexIndex(int dexIndex,
                                                                     File classesDex) {
        ClassesDexDataEntry dexInspectionsData =
                ApkDashboard.fillAnalysisPerClassesDexIndex(dexIndex, classesDex);

        return dexInspectionsData.classesWithNativeMethods;
    }

    public String getJavaDependenciesErrorsAsString() {
        List<String> javaDependenciesErrors = getJavaDependenciesErrors();

        StringBuilder builder = new StringBuilder();

        for (String javaDepError : javaDependenciesErrors) {
            builder.append(javaDepError);
        }

        return builder.toString();
    }

    public List<String> getJavaDependenciesErrors() {
        JavaDependenciesInspector ddi = new JavaDependenciesInspector(allClasses);
        List<String> result = ddi.getInspections();

        return result;
    }

    public List<String> getJavaInternalAPIsErrors() {
        JavaInternalAPIsInspector unsafeInspector = new JavaInternalAPIsInspector(apkFile);
        List<String> result = unsafeInspector.getInspections();

        return result;
    }

    public static ClassesDexDataEntry fillAnalysisPerClassesDexIndex(int dexIndex, File classesDex) {
        ClassesDexDataEntry dexData = new ClassesDexDataEntry(dexIndex);

        try {
            InputStream is = new FileInputStream(classesDex);
            ApplicationVisitor av = new ApkNativeMethodsVisitor(dexData);
            ApplicationReader ar = new ApplicationReader(Opcodes.ASM4, is);
            ar.accept(av, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            DexFile dxFile = DexlibLoader.loadDexFile(classesDex);

            DexBackedDexFile dataPack = (DexBackedDexFile) dxFile;
            dexData.allMethods = dataPack.getMethodCount();

            dexData.syntheticAccessors =
                    new SyntheticAccessorsInspector(dxFile).getSyntheticAccessors();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dexData;
    }

    private static List<String> extractLibNamesFromFullPaths(List<String> nativeLibs) {
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

    public List<String> getManifestErrors() {
        ManifestInspector mi = new ManifestInspector(apkFile);

        List<String> result = mi.getInspections();

        return result;
    }

    private class ClassesDexIterator implements Iterator<ClassesDexDataEntry> {
        private int currentClassesDex;
        private int currentCustomClassesDex;

        public ClassesDexIterator() {
            currentClassesDex = 0;
            currentCustomClassesDex = 0;
        }

        public boolean hasNext() {
            return currentClassesDex < classesDexEntries.size() || currentCustomClassesDex < customClassesDexEntries.size();
        }

        public ClassesDexDataEntry next() {
            ClassesDexDataEntry result = new ClassesDexDataEntry(0);

            if (currentClassesDex < classesDexEntries.size()) {
                for (ClassesDexDataEntry classesDex : classesDexEntries) {
                    int dexIndex = currentToDexIndex(currentClassesDex);
                    if (dexIndex == classesDex.index) {
                        result = classesDex;
                        currentClassesDex++;
                        break;
                    }
                }
            } else {
                currentCustomClassesDex++;
                result = customClassesDexEntries.get(0);
            }

            return result;
        }

        public void remove() {

        }

        private int currentToDexIndex(int current) {
            /*
              0 --> classes.dex
              1 --> classes2.dex
              2 --> classes3.dex
             */

            if (current == 0) return 0;

            return ++current;
        }
    }
}
