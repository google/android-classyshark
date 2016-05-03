package com.google.classyshark.gui.settings;

import com.google.classyshark.gui.theme.Theme;
import com.google.classyshark.gui.theme.ThemeManager;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ThemeChosenListener implements ActionListener {
    private final JFrame root;
    private final JComboBox comboBox;

    public ThemeChosenListener(JFrame root, JComboBox comboBox) {
        this.root = root;
        this.comboBox = comboBox;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Theme theme = ThemeManager.getThemeFrom(comboBox.getSelectedIndex());
        ThemeManager.saveCurrentTheme(theme);

        root.setVisible(false);
        root.dispose();
    }
}
