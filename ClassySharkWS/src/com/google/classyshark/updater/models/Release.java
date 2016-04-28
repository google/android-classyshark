package com.google.classyshark.updater.models;

public class Release {

    private final String tag_name;
    private final boolean prerelease;
    private final String body;
    private final Assets[] assets;

    public Release(String tag_name, boolean prerelease, String body, Assets[] assets) {
        this.tag_name = tag_name;
        this.prerelease = prerelease;
        this.body = body;
        this.assets = assets;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Release)) {
            return false;
        }

        return tag_name.equals(((Release) other).tag_name);
    }

    @Override
    public int hashCode() {
        return tag_name.hashCode();
    }

    @Override
    public String toString() {
        return "REL:\t" + tag_name + "\n\tCHANGELOG:\t" + body + "\n\tURL:\t" + assets[0].getURL();
    }
}
