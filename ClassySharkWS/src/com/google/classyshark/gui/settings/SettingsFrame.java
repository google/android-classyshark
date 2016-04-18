package com.google.classyshark.gui.settings;

import com.google.classyshark.gui.GuiMode;
import com.google.classyshark.gui.theme.Theme;
import com.google.classyshark.gui.theme.ThemeManager;
import com.google.classyshark.gui.theme.dark.DarkTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsFrame extends JFrame{
    private final Theme theme = GuiMode.getTheme();
    private final String[] themes = {"Light", "Dark"};

    public SettingsFrame() throws HeadlessException {
        super("Settings");
        initUI();

        JPanel panel = buildThemeUI();
        getContentPane().add(panel);

    }

    private JPanel buildThemeUI() {
        JPanel panel = buildOutPanel();
        JLabel label = buildThemeLabel();
        panel.add(label, BorderLayout.NORTH);
        JComboBox<String> comboBox = buildComboBox();
        panel.add(comboBox, BorderLayout.CENTER);
        return panel;
    }

    private JComboBox<String> buildComboBox() {
        JComboBox<String> comboBox =  new JComboBox(ThemeManager.getThemes());
        comboBox.setSelectedIndex(ThemeManager.getThemeIndexFrom(theme));
        comboBox.addActionListener(new ThemeChosenListener(this, comboBox));
        return comboBox;
    }

    private JLabel buildThemeLabel() {
        JLabel label = new JLabel("Theme:");
        label.setToolTipText("It will be applied the next time ClassyShark is started");
        theme.applyTo(label);
        return label;
    }

    private JPanel buildOutPanel() {
        JPanel panel = new JPanel(new BorderLayout(8,8));
        theme.applyTo(panel);
        return panel;
    }

    private void initUI() {
        setVisible(true);
        setSize(200, 80);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

    }
}
