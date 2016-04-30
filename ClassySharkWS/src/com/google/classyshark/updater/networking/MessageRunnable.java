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

import javax.swing.*;

public class MessageRunnable implements Runnable {
    private final String title;
    private final String changelog;
    private final boolean gui;

    private final String ICON_PATH = "/resources/ic_update.png";

    public MessageRunnable(String title, String changelog, boolean gui) {
        this.title = buildTitleFrom(title);
        this.changelog = buildChangelogFrom(changelog);
        this.gui = gui;
    }

    private String buildTitleFrom(String title) {
        return "New ClassyShark version " + title;
    }

    private String buildChangelogFrom(String changelog) {
        return "CHANGELOG:\n" + changelog;
    }

    @Override
    public void run() {
        if (gui) {
            warnUserAboutNewRelease();
        }
    }

    private void warnUserAboutNewRelease() {
        final Icon icon = new ImageIcon(getClass().getResource(ICON_PATH));
        JOptionPane.showConfirmDialog(null, changelog, title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, icon);
    }
}
