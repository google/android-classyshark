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

public class ApkDashboard implements Iterable<ApkReader.ClassesDexEntry> {

    ArrayList<ApkReader.ClassesDexEntry> classesDexEntries = new ArrayList<>();
    List<String> nativeLibs = new ArrayList<>();
    ArrayList<String> nativeDependencies = new ArrayList<>();
    List<String> nativeErrors = new LinkedList<>();
    File apkFile;

    public ApkDashboard(File apkFile) {
        this.apkFile = apkFile;
    }

    public void inspect() {
        // TODO add exception for not calling inspect
        ApkReader.fillDashboard(apkFile, this);
    }

    public Iterator<ApkReader.ClassesDexEntry> iterator() {
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
        ApkReader.ClassesDexEntry dexInspectionsData =
                ApkDashboard.fillAnalysisPerClassesDexIndex(dexIndex, classesDex);

        return dexInspectionsData.classesWithNativeMethods;
    }

    public static ApkReader.ClassesDexEntry fillAnalysisPerClassesDexIndex(int dexIndex, File classesDex) {
        ApkReader.ClassesDexEntry dexData = new ApkReader.ClassesDexEntry(dexIndex);

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

    private class ClassesDexIterator implements Iterator<ApkReader.ClassesDexEntry> {
        private int size;
        private int current;

        public ClassesDexIterator() {
            size = classesDexEntries.size();
            current = 0;
        }

        public boolean hasNext() {
            return current < size;
        }

        public ApkReader.ClassesDexEntry next() {
            ApkReader.ClassesDexEntry result = new ApkReader.ClassesDexEntry(0);

            for (ApkReader.ClassesDexEntry classesDex : classesDexEntries) {
                int dexIndex = currentToDexIndex(current);
                if (dexIndex == classesDex.index) {
                    result = classesDex;
                    current++;
                    break;
                }
            }

            return result;
        }

        private int currentToDexIndex(int current) {
            if (current == 0) return 0;

            return ++current;
        }
    }
}
