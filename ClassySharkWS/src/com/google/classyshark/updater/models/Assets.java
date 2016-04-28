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
