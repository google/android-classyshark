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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PrivateNativeLibsInspector {

    private static final String[] apiLibs = {
            "libz.so",
            "libz.a",
            "libvulkan.so",
            "libstdc++.so",
            "libstdc++.a",
            "libmediandk.so",
            "libm.so",
            "libm.a",
            "liblog.so",
            "libjnigraphics.so",
            "libdl.so",
            "libc.so",
            "libc.a",
            "libandroid.so",
            "libOpenSLES.so",
            "libOpenMAXAL.so",
            "libGLESv3.so",
            "libGLESv2.so",
            "libGLESv1_CM.so",
            "libEGL.so",
            "crtend_so.o",
            "crtend_android.o",
            "crtbegin_static.o",
            "crtbegin_so.o",
            "crtbegin_dynamic.o",
            "lsOutput.log"
    };

    private static List<String> APIS_LIB_LIST = new LinkedList<>(Arrays.asList(apiLibs));

    public static boolean isPrivate(String nativeLib, List<String> nativeLibNames) {

        if (!APIS_LIB_LIST.contains(nativeLib) && !nativeLibNames.contains(nativeLib)) {
            return true;
        }

        return false;
    }
}
