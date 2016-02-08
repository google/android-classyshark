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

package com.google.classyshark.silverghost.translator.dex;

import com.google.classyshark.silverghost.translator.java.dex.DexlibAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.objectweb.asm.Type;
import org.ow2.asmdex.ApplicationReader;
import org.ow2.asmdex.ApplicationVisitor;
import org.ow2.asmdex.ClassVisitor;
import org.ow2.asmdex.MethodVisitor;
import org.ow2.asmdex.Opcodes;

/**
 *
 */
public class DexMethodsDumper {

    public static List<String> dumpMethods(File archiveFile) {
        ArrayList<String> result = new ArrayList<>();

        try {
            ZipInputStream zipFile = new ZipInputStream(new FileInputStream(
                    archiveFile));

            ZipEntry zipEntry;

            int dexIndex = 0;
            while (true) {
                zipEntry = zipFile.getNextEntry();

                if (zipEntry == null) {
                    break;
                }

                if (zipEntry.getName().endsWith(".dex")) {
                    File file = File.createTempFile("DUMPER_METHODS_classes" + dexIndex, "dex");
                    file.deleteOnExit();

                    FileOutputStream fos =
                            new FileOutputStream(file);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = zipFile.read(bytes)) >= 0) {
                        fos.write(bytes, 0, length);
                    }

                    fos.close();
                    List<String> methodsList = fillAnalysis(dexIndex, file);
                    result.addAll(methodsList);

                    dexIndex++;
                } else {

                }
            }
            zipFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private static List<String> fillAnalysis(int dexIndex, File file) throws IOException {
        ArrayList result = new ArrayList();

        InputStream is = new FileInputStream(file);
        ApplicationVisitor av = new ApkInspectVisitor(result);
        ApplicationReader ar = new ApplicationReader(Opcodes.ASM4, is);
        ar.accept(av, 0);

        return result;
    }

    public static void writeAllMethods(File file, List<String> allStrings) {
        try {
            FileWriter writer = new FileWriter(file);
            for (String str : allStrings) {
                writer.write(str);
            }
            writer.close();
        } catch (IOException ioe) {

        }
    }

    private static class ApkInspectVisitor extends ApplicationVisitor {
        private List<String> methodsList;

        public ApkInspectVisitor(List<String> methodsList) {
            super(Opcodes.ASM4);
            this.methodsList = methodsList;
        }

        static String getDecName(String dexType) {
            if (dexType.startsWith("[")) {
                return getDecName(dexType.substring(1)) + "[]";
            }
            if (dexType.startsWith("L")) {
                String name = dexType.substring(1, dexType.length() - 1);

                return name.replace('/', '.');
            }

            if(DexlibAdapter.primitiveTypes.containsKey(dexType)) {
                return DexlibAdapter.primitiveTypes.get(dexType);
            } else {
                return "void";
            }
        }

        static String popType(String desc) {
            return desc.substring(nextTypePosition(desc, 0));
        }

        static String popReturn(String desc) {
            return desc.substring(0, desc.indexOf(popType(desc)));
        }

        static int nextTypePosition(String desc, int pos) {
            while (desc.charAt(pos) == '[') pos++;
            if (desc.charAt(pos) == 'L') pos = desc.indexOf(';', pos);
            pos++;
            return pos;
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

                    // class format (XYZ)R
                    // dex format RXYZ
                    StringBuilder builder = new StringBuilder();
                    builder.append(Modifier.toString(access));

                    builder.append(" " + ApkInspectVisitor.getDecName(popReturn(desc)));

                    builder.append(" " + name);

                    // using java class convert + types from ASM
                    Type[] parameterTypes = Type.getArgumentTypes("(" + popType(desc) + ")");

                    builder.append("(");

                    String prefix = "";
                    for (Type pType : parameterTypes) {
                        builder.append(prefix);
                        prefix = ",";
                        builder.append(ApkInspectVisitor.getDecName(pType.toString()));
                    }

                    builder.append(")\n");
                    methodsList.add(builder.toString());

                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
            };
        }
    }
}