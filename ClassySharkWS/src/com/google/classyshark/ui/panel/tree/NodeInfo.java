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

package com.google.classyshark.ui.panel.tree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeInfo {

    public String fullname;

    public NodeInfo(String fullClassName) {
        this.fullname = fullClassName;
    }

    public String toString() {
        return extractClassName(fullname);
    }

    public static String extractClassName (String fullname) {
        Pattern p = Pattern.compile(".*?([^.]+)$");
        Matcher m = p.matcher(fullname);

        return (m.find()) ? m.group(1) : "";
    }
}
