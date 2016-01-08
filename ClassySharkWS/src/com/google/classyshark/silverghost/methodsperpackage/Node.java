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

package com.google.classyshark.silverghost.methodsperpackage;

import java.util.HashMap;
import java.util.Map;

public class Node {
    private Map<String, Node> childNodes = new HashMap<>();
    private String key;
    private int methodCount = 0;

    public Node(String key) {
        this.key = key;
    }

    public Node() {
    }

    public Map<String, Node> getChildNodes() {
        return childNodes;
    }

    public String getKey() {
        return key;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public void add(ClassInfo classInfo) {
        String[] packages = classInfo.getPackageName().split("\\.");
        int pos = 0;
        add(pos, packages, classInfo);
    }

    private void add(int currentPost, String[] packages, ClassInfo classInfo) {
        methodCount = methodCount + classInfo.getMethodCount();
        if (currentPost >= packages.length) {
            return;
        }

        Node child = childNodes.get(packages[currentPost]);
        if (child == null) {
            child = new Node();
            child.key = packages[currentPost];
            childNodes.put(packages[currentPost], child);
        }
        child.add(currentPost + 1, packages, classInfo);
    }

    @Override
    public String toString() {
        return key + ": "+ methodCount;
    }
}
