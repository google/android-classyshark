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

import com.google.classyshark.updater.models.Release;
import com.google.classyshark.updater.networking.AbstractReleaseCallback;
import com.google.classyshark.updater.networking.MessageRunnable;
import com.google.classyshark.updater.networking.NetworkManager;
import com.google.classyshark.updater.utils.FileUtils;
import retrofit2.Call;

import javax.swing.*;
import java.io.IOException;

public class UpdateManager{
    private static final UpdateManager instance = new UpdateManager();
    private final AbstractReleaseCallback releaseCallback;
    private final Release currentRelease = new Release();
    private boolean gui = false;

    private UpdateManager() {
        releaseCallback = new AbstractReleaseCallback() {
            @Override
            public void onReleaseReceived(Release release) {
                onReleaseResponse(release);
            }
        };
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

    private void checkVersion(boolean gui) {
        this.gui = gui;
        Call<Release> call = NetworkManager.getGitHubApi().getLatestRelease();
        call.enqueue(releaseCallback);
    }

    private void onReleaseResponse(Release release) {
        if (release.isNewerThan(currentRelease)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    obtainNew(release);
                    SwingUtilities.invokeLater(new MessageRunnable(release.getReleaseName(), release.getChangelog(), gui));
                }
            }).start();

        }
    }

    private void obtainNew(Release release) {
        try {
            FileUtils.downloadFileFrom(release);
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

}
