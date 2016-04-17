package com.google.classyshark.gui.settings;

import com.google.classyshark.gui.GuiMode;
import com.google.classyshark.gui.theme.Theme;

import javax.swing.*;
import java.awt.*;

public class SettingsFrame extends JFrame{
    private final Theme theme = GuiMode.getTheme();
    private final String[] themes = {"Light", "Dark"};

    public SettingsFrame() throws HeadlessException {
        super("Settings");
        initUI();

        JLabel label = new JLabel("Choose the theme you prefer, it will be applied the next time you run ClassyShark");
        add(label);

        JComboBox<String> comboBox = new JComboBox(themes);
        add(comboBox);

    }

    private void initUI() {
        setVisible(true);
        setSize(500, 500); //TODO Replace with pack
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        theme.applyTo(this);
    }
}
