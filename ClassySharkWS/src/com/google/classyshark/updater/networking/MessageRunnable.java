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
