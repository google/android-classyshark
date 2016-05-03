package com.google.classyshark.gui.theme.dark;

import com.google.classyshark.gui.theme.Theme;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;

import static com.google.classyshark.gui.theme.dark.DarkColorScheme.ANNOTATIONS;
import static com.google.classyshark.gui.theme.dark.DarkColorScheme.BACKGROUND;
import static com.google.classyshark.gui.theme.dark.DarkColorScheme.BACKGROUND_LIGHT;
import static com.google.classyshark.gui.theme.dark.DarkColorScheme.DEFAULT;
import static com.google.classyshark.gui.theme.dark.DarkColorScheme.IDENTIFIERS;
import static com.google.classyshark.gui.theme.dark.DarkColorScheme.KEYWORDS;
import static com.google.classyshark.gui.theme.dark.DarkColorScheme.NAMES;
import static com.google.classyshark.gui.theme.dark.DarkColorScheme.SELECTION_BG;
import static com.google.classyshark.gui.theme.dark.DarkIconScheme.BACK_ICON_PATH;
import static com.google.classyshark.gui.theme.dark.DarkIconScheme.EXPORT_ICON_PATH;
import static com.google.classyshark.gui.theme.dark.DarkIconScheme.MAPPING_ICON_PATH;
import static com.google.classyshark.gui.theme.dark.DarkIconScheme.NEXT_ICON_PATH;
import static com.google.classyshark.gui.theme.dark.DarkIconScheme.OPEN_ICON_PATH;
import static com.google.classyshark.gui.theme.dark.DarkIconScheme.RECENT_ICON_PATH;
import static com.google.classyshark.gui.theme.dark.DarkIconScheme.SETTINGS_ICON_PATH;
import static com.google.classyshark.gui.theme.dark.DarkIconScheme.TOGGLE_ICON_PATH;

public class DarkTheme implements Theme{

    private final ImageIcon toggleIcon;
    private final ImageIcon recentIcon;
    private final ImageIcon backIcon;
    private final ImageIcon forwardIcon;
    private final ImageIcon openIcon;
    private final ImageIcon exportIcon;
    private final ImageIcon mappingsIcon;
    private final ImageIcon settingsIcon;

    public DarkTheme() {
        toggleIcon = new ImageIcon(getClass().getResource(TOGGLE_ICON_PATH));
        recentIcon = new ImageIcon(getClass().getResource(RECENT_ICON_PATH));
        backIcon = new ImageIcon(getClass().getResource(BACK_ICON_PATH));
        forwardIcon = new ImageIcon(getClass().getResource(NEXT_ICON_PATH));
        openIcon = new ImageIcon(getClass().getResource(OPEN_ICON_PATH));
        exportIcon = new ImageIcon(getClass().getResource(EXPORT_ICON_PATH));
        mappingsIcon = new ImageIcon(getClass().getResource(MAPPING_ICON_PATH));
        settingsIcon = new ImageIcon(getClass().getResource(SETTINGS_ICON_PATH));

        // TODO was default need to change names
        UIManager.put("MenuItem.foreground", IDENTIFIERS);
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
        return BACKGROUND;
    }

    @Override
    public void applyTo(Component component) {
        if (shallBeLighter(component)) {
            component.setBackground(BACKGROUND_LIGHT);
        } else if (component instanceof JLabel) {
            component.setForeground(IDENTIFIERS);
        }else {
            component.setBackground(BACKGROUND);
        }

        if(component instanceof JTextField) {
            component.setForeground(IDENTIFIERS);
        }
    }

    private boolean shallBeLighter(Component component) {
        return component instanceof JTree || component instanceof JTextField ||
                component instanceof JMenuItem || component instanceof JPopupMenu;
    }
}
