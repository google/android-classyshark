package com.google.classyshark.gui.theme;

import com.google.classyshark.gui.GuiMode;
import com.google.classyshark.gui.theme.light.LightTheme;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class ThemeManager {

    private static final String PROP_FILE = "classyshark_ui.properties";

    private static final String THEME_KEY = "Theme";

    public static void saveCurrentTheme() {
        try {
            Properties properties = new Properties();
            properties.setProperty(THEME_KEY, GuiMode.getTheme().getClass().getName());
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
            return new LightTheme();
        }
    }
}
