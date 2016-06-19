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

package com.google.classyshark.silverghost.translator.apk;

import java.io.IOException;
import nl.lxtreme.binutils.elf.Elf;

public class DynamicSymbolsInspector {

    private Elf elf;
    private String errors = "";
    private boolean areErrors = false;

    public DynamicSymbolsInspector(Elf elf) {
        this.elf = elf;
        inspect();
    }

    public boolean areErrors() {
        return areErrors;
    }

    public String getErrors() {
        return this.errors;
    }

    private void inspect() {
        try {
            if (!elf.isSoname()) {
                errors += " missing SONAME ";
                areErrors = true;
            }

            if (elf.isTextRel()) {
                errors += " text relocations found ";
                areErrors = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
