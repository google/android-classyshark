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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ClassesDexEntry implements Comparable {
    public int index;
    public int nativeMethodsCount = 0;
    public Set<String> classesWithNativeMethods = new TreeSet<>();
    public int allMethods = 0;
    public List<String> syntheticAccessors;
    public boolean isCustomLoad = false;

    public ClassesDexEntry(int index) {
        this.index = index;
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof ClassesDexEntry)) {
            return -1;
        }

        return -1 * Integer.valueOf(index).compareTo(((ClassesDexEntry) o).index);
    }

    public String getName() {
        if (index == 0) {
            return "classes.dex";
        }

        if(index < 10) {
            return "classes" + index + ".dex";
        }

        return "custom - classes.dex";
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