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
package com.google.classyshark.silverghost.exporter;

import com.google.classyshark.silverghost.methodscounter.ClassNode;

import java.io.PrintWriter;
import java.util.Iterator;

public class TreeMethodCountExporter implements MethodCountExporter {
    private PrintWriter pw;

    public TreeMethodCountExporter(PrintWriter pw) {
        this.pw = pw;
    }

    public void exportMethodCounts(ClassNode rootNode) {
        printNode(rootNode, new boolean[]{true});
        pw.flush();
    }

    private void printNode(ClassNode classNode, boolean[] isFinalLevel) {
        renderTreeStructure(isFinalLevel);

        pw.println(classNode.getKey() + " - " + classNode.getMethodCount());

        Iterator<ClassNode> it = classNode.getChildNodes().values().iterator();
        boolean[] isFinalLevel2 = new boolean[isFinalLevel.length + 1];
        System.arraycopy(isFinalLevel, 0, isFinalLevel2, 0, isFinalLevel.length);
        while (it.hasNext()) {
            ClassNode child = it.next();
            isFinalLevel2[isFinalLevel2.length - 1] = !it.hasNext();
            printNode(child, isFinalLevel2);
        }
    }

    private void renderTreeStructure(boolean[] levels) {
        if (levels.length == 1) {
            return;
        }

        for (int i = 1; i < levels.length; i++) {
            if (i == levels.length - 1) {
                if (levels[i]) {
                    pw.print(" \u255A");
                } else {
                    pw.print(" \u2560");
                }
            } else {
                if (levels[i]) {
                    pw.print("  ");
                    //Add another space to compensate the equals sign
                    if (i > 0) {
                        pw.print(" ");
                    }
                } else {
                    pw.print(" \u2551");
                }
            }
        }
        pw.print('\u2550');
    }
}
