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

package com.google.classyshark.updater.networking;

import com.google.classyshark.updater.models.Release;

import javax.swing.*;
import java.io.File;

/**
 * This class is the one being called when ClassyShark shall be updated from the GUI.
 * Its callback is informing the user about the new version and the new changelog
 */
public class GuiDownloader extends AbstractDownloader{
    private final static AbstractDownloader instance = new GuiDownloader();

    private GuiDownloader() {}

    @Override
    boolean warnAboutNew(Release release) {
        return true;
    }

    @Override
    void onReleaseDownloaded(File file, Release release) {
        SwingUtilities.invokeLater(new MessageRunnable(release.getReleaseName(), release.getChangelog()));
    }

    public static AbstractDownloader getInstance() {
        return instance;
    }
}
