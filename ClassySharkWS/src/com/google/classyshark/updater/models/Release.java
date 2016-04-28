package com.google.classyshark.updater.models;


import com.sun.istack.internal.Nullable;

public class Release {

    private final String name;
    private final boolean prerelease;
    private final String body;
    private final Assets[] assets;


    private Release(String name, boolean prerelease, String body, Assets[] assets) {
        this.name = name;
        this.prerelease = prerelease;
        this.body = body;
        this.assets = assets;
    }

    public Release(String name) {
        this(name, false, "", null);
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

    public boolean isPrerelease() {
        return prerelease;
    }

    public String getChangelog() {
        return body;
    }
}
