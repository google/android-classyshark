package com.google.classyshark.updater.networking;

import javax.swing.*;

public class MessageRunnable implements Runnable{
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
        return "ClassyShark version " + title;
    }

    private String buildChangelogFrom(String changelog) {
        return "CHANGELOG:\n" + changelog + "\n\nWould you like to automatically restart ClassyShark to run the update?";
    }

    @Override
    public void run() {
        if (gui) {
            warnUserAboutNewRelease();
        }
    }

    private void warnUserAboutNewRelease() {
        int buttons = JOptionPane.YES_NO_OPTION;
        final Icon icon = new ImageIcon(getClass().getResource(ICON_PATH));
        int dialogResults = JOptionPane.showConfirmDialog(null, changelog, title, buttons, JOptionPane.INFORMATION_MESSAGE, icon);

        if (dialogResults == JOptionPane.YES_OPTION) {
            System.out.print("YES");
        } else {
            System.out.print("NO");
        }
    }
}
