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

import java.lang.reflect.Modifier;
import org.ow2.asmdex.ApplicationVisitor;
import org.ow2.asmdex.ClassVisitor;
import org.ow2.asmdex.MethodVisitor;
import org.ow2.asmdex.Opcodes;

public  class ApkNativeMethodsVisitor extends ApplicationVisitor {
    private ApkReader.ClassesDexEntry dexData;

    public ApkNativeMethodsVisitor(ApkReader.ClassesDexEntry dexData) {
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