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

import java.io.File;
import java.util.Scanner;

/**
 * This class is the one taking care of downloading the update from the CLI, asking the user if they want to update
 * to the new version when one is found.
 */
public class CliDownloader extends AbstractDownloader{
    private static final AbstractDownloader instance = new CliDownloader();

    private CliDownloader() {}

    @Override
    boolean warnAboutNew(Release release) {
        String message = "New ClassyShark version available!\n" +
                release.getReleaseName() + "\n" +
                release.getChangelog() + "\n" +
                "Do you wish to download it? (y/N)";

        System.out.println(message);
        Scanner scanner = new Scanner(System.in);

        return scanner.next().equalsIgnoreCase("y");
    }

    @Override
    void onReleaseDownloaded(File file, Release release) {

        String message = "New ClassyShark version available offline!\n" +
                "The new release " +
                release.getReleaseName() +
                " has been downloaded to " +
                file.getAbsolutePath();

        System.out.println(message);
    }

    public static AbstractDownloader getInstance() {
        return instance;
    }
}
