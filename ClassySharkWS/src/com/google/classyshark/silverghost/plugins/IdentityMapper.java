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

package com.google.classyshark.silverghost.plugins;

import com.google.classyshark.silverghost.TokensMapper;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class IdentityMapper implements TokensMapper {

    private Map<String, String> identityMap = new TreeMap<>();

    public IdentityMapper() {

    }

    @Override
    public TokensMapper readMappings(File file) {
        return this;
    }

    @Override
    public Map<String, String> getReverseClasses() {
        return this.identityMap;
    }
}
