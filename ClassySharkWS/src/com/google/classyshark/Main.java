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

package com.google.classyshark;

import com.google.classyshark.cli.CliMode;
import com.google.classyshark.gui.GuiMode;
import java.util.Arrays;
import java.util.List;

/**
 * the driver class of the app
 */
public class Main {

    private Main() {
    }

    private static boolean isGui(List<String> argsAsArray) {
        return argsAsArray.isEmpty() || argsAsArray.get(0).equalsIgnoreCase("-open");
    }

    public static void main(final String[] args) {
        final List<String> argsAsArray = Arrays.asList(args);

        if (isGui(argsAsArray)) {
            GuiMode.with(argsAsArray);
        } else {
            CliMode.with(argsAsArray);
        }
    }
}