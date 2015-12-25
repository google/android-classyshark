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

package com.google.classyshark.ui.panel.reducer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Is a function : (key, list of classes) --> list of classes, reduced by key
 */
public class Reducer {

    private List<String> allClassNames;
    private List<String> reducedClassNames;

    public Reducer(List<String> allClassNames) {
        this.allClassNames = allClassNames;
        reducedClassNames = new ArrayList<>();
    }

    public List<String> reduce(String key) {
        List<String> result;

        if (key.isEmpty()) {
            result = allClassNames;
            reducedClassNames.clear();
            return result;
        } else {
            reducedClassNames = fuzzyReduceClassNames(key, allClassNames);
            result = reducedClassNames;
            return result;
        }
    }

    public String getAutocompleteClassName() {
        if (!reducedClassNames.isEmpty()) {
            return reducedClassNames.get(0);
        }

        return allClassNames.get(0);
    }

    public List<String> getAllClassNames() {
        return Collections.unmodifiableList(allClassNames);
    }

    private static List<String> fuzzyReduceClassNames(String key,
                                                      List<String> list) {
        List<String> result = new ArrayList<>();

        int foundEntryIndex;
        String camelSearchKey;

        for (String entry : list) {
            camelSearchKey = entry;
            camelSearchKey = camelSearchKey.replaceAll("[^A-Z]", "");

            foundEntryIndex = entry.indexOf(key);

            if ((camelSearchKey.equalsIgnoreCase(key))
                    || (foundEntryIndex > -1)) {
                result.add(entry);
            }
        }

        return result;
    }
}