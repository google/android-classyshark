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


package com.google.classyshark.silverghost.translator.apk.dashboard;

import com.google.classyshark.silverghost.translator.java.dex.DexlibAdapter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;

public class SyntheticAccessorsInspector {
    private final DexFile dxFile;

    public SyntheticAccessorsInspector(DexFile dxFile) {
        this.dxFile = dxFile;
    }

    public List<String> getSyntheticAccessors() {

        LinkedList<String> result = new LinkedList<>();

        Set<? extends ClassDef> allClasses = dxFile.getClasses();

        for (ClassDef classDef : allClasses) {


            Iterator<? extends Method> allMethodsIter = classDef.getMethods().iterator();

            while (allMethodsIter.hasNext()) {

                Method element = allMethodsIter.next();

                String name = element.getName();

                String nClassName = classDef.getType();

                if (name.contains("access$")) {

                    String cleanClassName = DexlibAdapter.getClassStringFromDex(nClassName);

                    if (!result.contains(cleanClassName)) {
                        result.add(cleanClassName);
                    }
                }
            }
        }

        return result;
    }
}
