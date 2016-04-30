package com.google.classyshark.updater.utils;


import com.google.classyshark.updater.models.Release;
import com.sun.istack.internal.NotNull;

import java.io.File;
import java.nio.file.Paths;

public class NamingUtils {

    private static final String PREFIX = "ClassyShark";
    private static final String SUFFIX = ".jar";

    public static String buildNameFrom(@NotNull Release release) {
        String[] creationDates = release.getCreatedAt().split("T");
        String timeStamp = "";
        if (creationDates.length > 0) {
            timeStamp = "_" + creationDates[0];
        }

        return extractCurrentPath() + File.separator + PREFIX + timeStamp + SUFFIX;
    }

    private static String extractCurrentPath() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }
}
