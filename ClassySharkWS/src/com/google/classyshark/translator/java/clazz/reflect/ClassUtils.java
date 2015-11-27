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

package com.google.classyshark.translator.java.clazz.reflect;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Helper method for class loading
 */
public class ClassUtils {

    private ClassUtils() {
    }

    // TODO add logic when the className ends with .class
    public static Class loadClassFromJar(String jarAbsolutePath, String className) throws
            MalformedURLException, ClassNotFoundException {
        Class result;
        URL[] classLoaderUrls = new URL[]{new File(jarAbsolutePath).toURI().toURL()};
        URLClassLoader child = new URLClassLoader(classLoaderUrls);

        result = child.loadClass(className);

        return result;
    }
}
