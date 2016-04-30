package com.google.classyshark.updater.utils;

import com.google.classyshark.updater.models.Release;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.google.classyshark.updater.utils.NamingUtils.extractCurrentPath;
import static javax.script.ScriptEngine.FILENAME;

public class FileUtils {

    public static File downloadFileFrom(Release release) throws IOException {
        URL url = new URL(release.getDownloadURL());
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        File file = new File(NamingUtils.buildNameFrom(release));
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
}
