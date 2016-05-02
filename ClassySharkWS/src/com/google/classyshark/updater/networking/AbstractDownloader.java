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
import com.google.classyshark.updater.utils.FileUtils;
import retrofit2.Call;

import java.io.File;
import java.io.IOException;

/**
 * This class is the skeleton of both the {@link GuiDownloader} and {@link CliDownloader}: it contains the core logic
 * needed to download the new JAR and a couple of callbacks that helps in managing the different outputs between the
 * two logics
 */
public abstract class AbstractDownloader extends AbstractReleaseCallback{
    private final Release current = new Release();

    public void checkNewVersion() {
        Call<Release> call = NetworkManager.getGitHubApi().getLatestRelease();
        call.enqueue(this);
    }

    @Override
    public void onReleaseReceived(Release release) {
        if (release.isNewerThan(current)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (warnAboutNew(release)) {
                        obtainNew(release);
                    }
                }
            }).start();

        }
    }

    abstract boolean warnAboutNew(Release release);

    private void obtainNew(Release release) {
        try {
            onReleaseDownloaded(FileUtils.downloadFileFrom(release), release);
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    abstract void onReleaseDownloaded(File file, Release release);
}
