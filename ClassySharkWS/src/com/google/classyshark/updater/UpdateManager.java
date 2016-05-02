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

package com.google.classyshark.updater;

import com.google.classyshark.updater.networking.AbstractDownloader;
import com.google.classyshark.updater.networking.CliDownloader;
import com.google.classyshark.updater.networking.GuiDownloader;

/**
 * This class is the core point for the update process: based on the response
 * received from GitHub, it will download the new release and, if ClassyShark
 * has been started as desktop app, when the download finishes, it will show a
 * dialog containing the changelog of the new version
 */
public class UpdateManager{
    private static final UpdateManager instance = new UpdateManager();

    private UpdateManager() {

    }

    public static UpdateManager getInstance() {
        return instance;
    }

    public void checkVersionConsole() {
        checkVersion(false);
    }

    public void checkVersionGui() {
        checkVersion(true);
    }

    private void checkVersion(boolean isGui) {
        getDownloaderFrom(isGui).checkNewVersion();
    }

    private AbstractDownloader getDownloaderFrom(boolean isGui) {
        if (isGui) {
            return GuiDownloader.getInstance();
        } else {
            return CliDownloader.getInstance();
        }
    }
}
