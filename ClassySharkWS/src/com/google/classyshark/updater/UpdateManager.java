package com.google.classyshark.updater;

import com.google.classyshark.updater.models.Release;
import com.google.classyshark.updater.networking.AbstractReleaseCallback;
import com.google.classyshark.updater.networking.MessageRunnable;
import com.google.classyshark.updater.networking.NetworkManager;
import retrofit2.Call;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class UpdateManager{
    private static final UpdateManager instance = new UpdateManager();

    private final AbstractReleaseCallback releaseCallback;
    private final Release currentRelease = new Release("1.0");
    private boolean gui = false;

    private final String TEMP_FILENAME = "ClassyShark_new.jar";
    private final String FILENAME = "ClassyShark.jar";

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
            File file = downloadFileFrom(release);
            overwriteOld(file);
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }

    }

    private File downloadFileFrom(Release release) throws IOException {
        URL url = new URL(release.getDownloadURL());
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        File file = new File(extractCurrentPath() + File.separator + TEMP_FILENAME);
        file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        return file;
    }

    private void overwriteOld(File file) throws IOException {
        Path path = new File(extractCurrentPath() + File.separator + FILENAME).toPath();
        Files.copy(file.toPath(), path, StandardCopyOption.REPLACE_EXISTING);
        file.delete();
    }

    private String extractCurrentPath() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }

}
