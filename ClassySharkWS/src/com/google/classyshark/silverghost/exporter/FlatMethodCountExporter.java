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

public class FlatMethodCountExporter implements MethodCountExporter {
    private PrintWriter pw;

    public FlatMethodCountExporter(PrintWriter pw) {
        this.pw = pw;
    }

    public void exportMethodCounts(ClassNode rootNode) {
        printNode(rootNode, new String[]{});
        pw.flush();
    }

    private void printNode(ClassNode classNode, String[] path) {
        for (String p: path) {
            pw.print(p);
            pw.print('.');
        }
        pw.println(classNode.getKey() + " - " + classNode.getMethodCount());

        Iterator<ClassNode> it = classNode.getChildNodes().values().iterator();
        String[] newPath = new String[path.length + 1];
        System.arraycopy(path, 0, newPath, 0, path.length);
        newPath[newPath.length - 1] = classNode.getKey();
        while (it.hasNext()) {
            ClassNode child = it.next();
            printNode(child, newPath);
        }
    }
}
