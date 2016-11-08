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

import java.util.ArrayList;
import java.util.List;

public class JavaDependenciesInspector {

    private final List<String> allClasses;

    private int imageLoading = 0;
    private boolean hasGlide;
    private boolean hasPicasso;
    private boolean hasFresco;

    private int asyncHttp = 0;
    private boolean hasOkHttp;
    private boolean hasVolley;
    private boolean hasLoopj;

    private int jsonParsing = 0;
    private boolean hasJackson;
    private boolean hasGson;

    // other dependencies
    private boolean hasGuava;
    private boolean hasDeprecatedHttp;
    private boolean hasActionBarSherlock;
    private boolean hasPullToRefresh;

    public JavaDependenciesInspector(List<String> allClasses) {
        this.allClasses = allClasses;
    }

    public List<String> getInspections() {
        List result = new ArrayList<>();

        for (String cName : allClasses) {
            updateLogic(cName);
        }

        if (imageLoading > 1) {
            result.add("\n* Duplicate image loading libraries - ");
            if (hasPicasso) result.add("picasso ");
            if (hasGlide) result.add("glide ");
            if (hasFresco) result.add("fresco ");
        }

        if (asyncHttp > 1) {
            result.add("\n* Duplicate async http libraries - ");
            if (hasOkHttp) result.add("okhttp ");
            if (hasVolley) result.add("volley ");
            if (hasLoopj) result.add("loopj ");
        }

        if (jsonParsing > 1) {
            result.add("\n* Duplicate json parsing - ");
            if (hasJackson) result.add("jackson ");
            if (hasGson) result.add("gson ");
        }

        if (hasGuava) {
            result.add("\n* Guava (server side library)usage");
        }

        if (hasDeprecatedHttp) {
            result.add("\n* Apache Http is deprecated");
        }

        if (hasActionBarSherlock) {
            result.add("\n* ActionBar Sherlock is deprecated");
        }

        if (hasPullToRefresh) {
            result.add("\n* PullToRefresh is deprecated");
        }

        return result;
    }

    private void updateLogic(String cName) {

        if (cName.contains("glide") && !hasGlide) {
            hasGlide = true;
            imageLoading++;
        } else if (cName.contains("picasso") && !hasPicasso) {
            hasPicasso = true;
            imageLoading++;
        } else if (cName.contains("fresco") && !hasFresco) {
            hasFresco = true;
            imageLoading++;
        } else if (cName.contains("okhttp") && !hasOkHttp) {
            hasOkHttp = true;
            asyncHttp++;
        } else if (cName.contains("volley") && !hasVolley) {
            hasVolley = true;
            asyncHttp++;
        } else if (cName.contains("loopj") && !hasLoopj) {
            hasLoopj = true;
            asyncHttp++;
        } else if (cName.contains("fasterxml.jackson") && !hasJackson) {
            hasJackson = true;
            jsonParsing++;
        } else if (cName.contains("google.code.gson") && !hasGson) {
            hasGson = true;
            jsonParsing++;
        } else if (cName.contains("google.common") && !hasGuava) {
            hasGuava = true;
        } else if (cName.contains("apache.http") && !hasDeprecatedHttp) {
            hasDeprecatedHttp = true;
        } else if (cName.contains("'com.actionbarsherlock") && !hasActionBarSherlock) {
            hasActionBarSherlock = true;
        } else if (cName.contains("chrisbanes.pulltorefresh") && !hasPullToRefresh) {
            hasPullToRefresh = true;
        }
    }
}
