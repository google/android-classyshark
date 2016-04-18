package com.google.classyshark.gui.theme.light;

import com.google.classyshark.gui.theme.Theme;

import javax.swing.*;
import java.awt.*;

import static com.google.classyshark.gui.theme.light.LightColorScheme.*;
import static com.google.classyshark.gui.theme.light.LightIconScheme.*;

public class LightTheme implements Theme {
    private final ImageIcon toggleIcon;
    private final ImageIcon recentIcon;
    private final ImageIcon backIcon;
    private final ImageIcon forwardIcon;
    private final ImageIcon openIcon;
    private final ImageIcon exportIcon;
    private final ImageIcon mappingsIcon;
    private final ImageIcon settingsIcon;

    public LightTheme() {
        toggleIcon = new ImageIcon(getClass().getResource(TOGGLE_ICON_PATH));
        recentIcon = new ImageIcon(getClass().getResource(RECENT_ICON_PATH));
        backIcon = new ImageIcon(getClass().getResource(BACK_ICON_PATH));
        forwardIcon = new ImageIcon(getClass().getResource(NEXT_ICON_PATH));
        openIcon = new ImageIcon(getClass().getResource(OPEN_ICON_PATH));
        exportIcon = new ImageIcon(getClass().getResource(EXPORT_ICON_PATH));
        mappingsIcon = new ImageIcon(getClass().getResource(MAPPING_ICON_PATH));
        settingsIcon = new ImageIcon(getClass().getResource(SETTINGS_ICON_PATH));
    }

    @Override
    public ImageIcon getToggleIcon() {
        return toggleIcon;
    }

    @Override
    public ImageIcon getRecentIcon() {
        return recentIcon;
    }

    @Override
    public ImageIcon getBackIcon() {
        return backIcon;
    }

    @Override
    public ImageIcon getForwardIcon() {
        return forwardIcon;
    }

    @Override
    public ImageIcon getOpenIcon() {
        return openIcon;
    }

    @Override
    public ImageIcon getExportIcon() {
        return exportIcon;
    }

    @Override
    public ImageIcon getMappingIcon() {
        return mappingsIcon;
    }

    @Override
    public ImageIcon getSettingsIcon() {
        return settingsIcon;
    }

    @Override
    public Color getDefaultColor() {
        return DEFAULT;
    }

    @Override
    public Color getKeyWordsColor() {
        return KEYWORDS;
    }

    @Override
    public Color getIdentifiersColor() {
        return IDENTIFIERS;
    }

    @Override
    public Color getAnnotationsColor() {
        return ANNOTATIONS;
    }

    @Override
    public Color getSelectionBgColor() {
        return SELECTION_BG;
    }

    @Override
    public Color getNamesColor() {
        return NAMES;
    }

    @Override
    public Color getBackgroundColor() {
        return NAMES;
    }

    @Override
    public void applyTo(Component component) {
        /**
         * Do nothing as we don't want to override system defaults for the light theme
         */
    }
}
