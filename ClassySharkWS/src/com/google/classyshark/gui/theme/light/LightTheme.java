package com.google.classyshark.gui.theme.light;

import com.google.classyshark.gui.theme.Theme;

import javax.swing.*;
import java.awt.*;

public class LightTheme implements Theme {
    private final ImageIcon toggleIcon;
    private final ImageIcon recentIcon;
    private final ImageIcon backIcon;
    private final ImageIcon forwardIcon;
    private final ImageIcon openIcon;
    private final ImageIcon exportIcon;
    private final ImageIcon mappingsIcon;

    public LightTheme() {
        toggleIcon = new ImageIcon(getClass().getResource(LightIconScheme.TOGGLE_ICON_PATH));
        recentIcon = new ImageIcon(getClass().getResource(LightIconScheme.RECENT_ICON_PATH));
        backIcon = new ImageIcon(getClass().getResource(LightIconScheme.BACK_ICON_PATH));
        forwardIcon = new ImageIcon(getClass().getResource(LightIconScheme.NEXT_ICON_PATH));
        openIcon = new ImageIcon(getClass().getResource(LightIconScheme.OPEN_ICON_PATH));
        exportIcon = new ImageIcon(getClass().getResource(LightIconScheme.EXPORT_ICON_PATH));
        mappingsIcon = new ImageIcon(getClass().getResource(LightIconScheme.MAPPING_ICON_PATH));
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
    public Color getDefaultColor() {
        return LightColorScheme.DEFAULT;
    }

    @Override
    public Color getKeyWordsColor() {
        return LightColorScheme.KEYWORDS;
    }

    @Override
    public Color getIdentifiersColor() {
        return LightColorScheme.IDENTIFIERS;
    }

    @Override
    public Color getAnnotationsColor() {
        return LightColorScheme.ANNOTATIONS;
    }

    @Override
    public Color getSelectionBgColor() {
        return LightColorScheme.SELECTION_BG;
    }

    @Override
    public Color getNamesColor() {
        return LightColorScheme.NAMES;
    }
}
