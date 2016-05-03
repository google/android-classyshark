package com.google.classyshark.gui.theme;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Component;

/**
 * This class is the one defining the different parameters needed in order to obtain a proper theme across the whole app
 */
public interface Theme extends SwingThemeApplier<Component> {

    ImageIcon getToggleIcon();
    ImageIcon getRecentIcon();
    ImageIcon getBackIcon();
    ImageIcon getForwardIcon();
    ImageIcon getOpenIcon();
    ImageIcon getExportIcon();
    ImageIcon getMappingIcon();
    ImageIcon getSettingsIcon();

    Color getDefaultColor();
    Color getKeyWordsColor();
    Color getIdentifiersColor();
    Color getAnnotationsColor();
    Color getSelectionBgColor();
    Color getNamesColor();
    Color getBackgroundColor();
}
