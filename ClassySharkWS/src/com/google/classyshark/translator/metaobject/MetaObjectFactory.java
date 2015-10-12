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

package com.google.classyshark.translator.metaobject;

import com.google.classyshark.reducer.ArchiveReader;
import com.google.classyshark.translator.metaobject.clazz.ClassUtils;
import com.google.classyshark.translator.metaobject.clazz.MetaObjectClass;
import com.google.classyshark.translator.metaobject.dex.DexlibAdapter;
import com.google.classyshark.translator.metaobject.dex.MetaObjectDex;
import java.io.File;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

/**
 *  Factory for creating meta-objects by the format (class/dex)
 */
public class MetaObjectFactory {
    private MetaObjectFactory() {}

    public static MetaObject buildMetaObject(String className, File archiveFile) {
        MetaObject result;

        if (archiveFile.getName().toLowerCase().endsWith(".jar")) {
            String trimmedName = className.substring(0, className.lastIndexOf('.'));
            Class clazz;
            try {
                clazz = ClassUtils.load(archiveFile.getPath(), trimmedName);
            } catch (Exception e) {
                clazz = Exception.class;
            }

            result = new MetaObjectClass(clazz);

        } else if (archiveFile.getName().toLowerCase().endsWith(".dex")) {
            try {
                DexFile dexFile = ArchiveReader.get(archiveFile);
                ClassDef classDef = DexlibAdapter.getClassDefByName(className, dexFile);
                result = new MetaObjectDex(classDef);
            } catch (Exception e) {
                result = new MetaObjectClass(Exception.class);
            }

        } else if (archiveFile.getName().toLowerCase().endsWith(".apk")) {
            try {
                File classesDex =
                        ArchiveReader.extractClassesDexFromApk(archiveFile.getAbsolutePath());
                DexFile dexFile = ArchiveReader.get(classesDex);
                ClassDef classDef = DexlibAdapter.getClassDefByName(className, dexFile);
                result = new MetaObjectDex(classDef);
            } catch (Exception e) {
                result = new MetaObjectClass(Exception.class);
            }

        } else if (archiveFile.getName().toLowerCase().endsWith(".class")) {
            try {
                Class clazz;
                try {
                    clazz = ArchiveReader.loadClassFromClassFile(archiveFile);
                } catch (Exception e) {
                    clazz = Exception.class;
                }
                result = new MetaObjectClass(clazz);
            } catch (Exception e) {
                result = new MetaObjectClass(Exception.class);
            }
        } else {
            result = new MetaObjectClass(Exception.class);
        }

        return result;
    }
}
