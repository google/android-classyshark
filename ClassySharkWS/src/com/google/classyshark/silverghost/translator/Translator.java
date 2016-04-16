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

package com.google.classyshark.silverghost.translator;

import java.util.List;

/**
 * Is a function : (binary data, name) --> list of elements <String, Tag>,
 * with human readable semantics
 */
public interface Translator {

    /**
     * TAG representing type
     */
     enum TAG {
        MODIFIER, IDENTIFIER, ANNOTATION, DOCUMENT,
        XML_TAG, XML_ATTR_NAME, XML_ATTR_VALUE, XML_CDATA, XML_COMMENT, XML_DEFAULT
    }

    /**
     * element = word with tag
     */
    class ELEMENT {
        public ELEMENT(String word, TAG tag) {
            this.text = word;
            this.tag = tag;
        }

        public final String text;
        public final TAG tag;
    }

    String getClassName();

    void apply();

    List<ELEMENT> getElementsList();

    List<String> getDependencies();
}
