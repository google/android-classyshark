package com.google.classyshark.updater.models;


import com.google.gson.annotations.SerializedName;
import com.sun.istack.internal.Nullable;

public class Release {

    private final String name;
    @SerializedName("prerelease")
    private final boolean preRelease;
    private final String body;
    private final Assets[] assets;
    @SerializedName("created_at")
    private final String createdAt;


    private Release(String name, boolean preRelease, String body, Assets[] assets, String createdAt) {
        this.name = name;
        this.preRelease = preRelease;
        this.body = body;
        this.assets = assets;
        this.createdAt = createdAt;
    }

    public Release(String name) {
        this(name, false, "", null, "");
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
