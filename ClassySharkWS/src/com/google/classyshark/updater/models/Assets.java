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

package com.google.classyshark.updater.models;

class Assets {

    private final String browser_download_url;

    Assets(String browser_download_url) {
        this.browser_download_url = browser_download_url;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Assets)) {
            return false;
        }

        return browser_download_url.equals(((Assets) other).browser_download_url);
    }

    @Override
    public int hashCode() {
        return browser_download_url.hashCode();
    }

    String getURL() {
        return browser_download_url;
    }
}
