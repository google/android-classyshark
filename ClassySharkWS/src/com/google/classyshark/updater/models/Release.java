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


import com.google.classyshark.Version;
import com.google.classyshark.updater.networking.GitHubApi;
import com.google.gson.annotations.SerializedName;
import com.sun.istack.internal.Nullable;

/**
 * This class represents the response that GitHub returns when queried with {@link GitHubApi#getLatestRelease()}.
 * Since we do not need all the fields from the response, we just add those we are gonna use.
 * At the moment, the {@link #preRelease} field is not used, but it will most probably be in the future in case we want to
 * only update to stable releases.
 */
public class Release {

    private final String name;
    @SerializedName("prerelease")
    private final boolean preRelease;
    private final String body;
    private final ReleaseDownloadData[] assets;
    @SerializedName("created_at")
    private final String createdAt;


    private Release(String name, boolean preRelease, String body, ReleaseDownloadData[] assets, String createdAt) {
        this.name = name;
        this.preRelease = preRelease;
        this.body = body;
        this.assets = assets;
        this.createdAt = createdAt;
    }

    public Release() {
        this(String.format("%d.%d", Version.MAJOR, Version.MINOR), false, "", null, "");
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Release)) {
            return false;
        }

        return name.equals(((Release) other).name);
    }

    private int getMajorVersion() {
        return getVersionField(0);
    }

    private int getVersionField(int field) {
        String cleanedReleaseName = name.split("\\s")[0];
        return Integer.parseInt(cleanedReleaseName.split("\\.")[field]);
    }

    private int getMinorVersion() {
        return getVersionField(1);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "REL:\t" + name + "\nCHANGELOG:\n" + body;
    }

    public boolean isNewerThan(Release other) {
        return getMajorVersion() > other.getMajorVersion() ||
                getMajorVersion() == other.getMajorVersion() && getMinorVersion() > other.getMinorVersion();
    }

    @Nullable
    public String getDownloadURL() {
        if (assets != null && assets.length > 0) {
            return assets[0].getURL();
        } else {
            return null;
        }
    }

    public String getReleaseName() {
        return name;
    }

    public boolean isPreRelease() {
        return preRelease;
    }

    public String getChangelog() {
        return body;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
