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
