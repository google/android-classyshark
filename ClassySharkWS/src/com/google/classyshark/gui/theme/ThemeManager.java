package com.google.classyshark.gui.theme;

import com.google.classyshark.gui.theme.dark.DarkTheme;
import com.google.classyshark.gui.theme.light.LightTheme;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class ThemeManager {

    private static final String PROP_FILE = "classyshark_ui.properties";
    private static final String THEME_KEY = "Theme";
    private static final String[] themes = {"Light", "Dark"};

    private static final int LIGHT = 0;
    private static final int DARK = 1;

    public static void saveCurrentTheme(Theme theme) {
        try {
            Properties properties = new Properties();
            properties.setProperty(THEME_KEY, theme.getClass().getName());
            FileWriter writer = new FileWriter(getPropertyFile());
            properties.store(writer, "Theme stored");
            writer.close();
        } catch (IOException e) {
        }

    }

    private static File getPropertyFile() throws IOException {
        File configFile = new File(PROP_FILE);

        if (!configFile.exists()) {
            configFile.createNewFile();
        }
        return configFile;
    }

    public static Theme getCurrentTheme() {
        try {
            FileReader reader = new FileReader(getPropertyFile());
            Properties properties = new Properties();
            properties.load(reader);
            final String theme = properties.getProperty(THEME_KEY);
            Class<Theme> c = (Class<Theme>) Class.forName(theme);
            return c.newInstance();
        } catch (Exception e) {
            return new DarkTheme();
        }
    }

    public static String[] getThemes() {
        return themes;
    }

    public static int getThemeIndexFrom(Theme theme) {
        if (theme instanceof DarkTheme) {
            return DARK;
        } else {
            return LIGHT;
        }
    }

    public static Theme getThemeFrom(final int index) {
        switch (index) {
            case LIGHT:
                return new LightTheme();
            case DARK:
            default:
                return new DarkTheme();
        }
    }
}
