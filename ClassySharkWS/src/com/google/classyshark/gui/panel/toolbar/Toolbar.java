/*
 * Copyright 2015 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.classyshark.gui.panel.toolbar;

import com.google.classyshark.gui.panel.ColorScheme;
import com.google.classyshark.gui.panel.IconSchemes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.Border;

/**
 * toolbar = buttons + command line
 */
public class Toolbar extends JToolBar {

    private final JTextField typingArea;
    private final ToolbarController toolbarController;

    private JButton openBtn;
    private JButton viewBtn;
    private JButton backBtn;
    private JButton exportButton;
    private JButton recentArchivesBtn;
    private JButton mappingBtn;
    private JToggleButton leftPanelToggleBtn;

    public Toolbar(final ToolbarController toolbarController) {
        super();

        this.toolbarController = toolbarController;

        typingArea = buildTypingArea();
        openBtn = buildOpenButton();
        backBtn = buildBackButton();
        viewBtn = buildViewButton();
        //mappingBtn = buildMappingsButton();
        exportButton = buildExportButton();
        recentArchivesBtn = buildRecentArchivesButton();
        leftPanelToggleBtn = buildLeftPanelToggleButton();

        add(leftPanelToggleBtn);
        add(openBtn);
        add(backBtn);
        add(viewBtn);
        add(typingArea);
        //add(mappingBtn);
        add(exportButton);
        add(recentArchivesBtn);

        setFloatable(false);
        setTypingArea();
        setBorder(BorderFactory.createEmptyBorder());
    }

    public void addKeyListenerToTypingArea(KeyListener kl) {
        typingArea.addKeyListener(kl);
    }

    public void setTypingArea() {

        typingArea.setForeground(ColorScheme.NAMES);
        setTypingAreaCaret();
    }

    public void setTypingAreaCaret() {
        int len = typingArea.getDocument().getLength();
        typingArea.setCaretPosition(len);
        typingArea.setCaretColor(ColorScheme.IDENTIFIERS);
    }

    public String getText() {
        return typingArea.getText();
    }

    public void setText(String text) {
        typingArea.setText(text);
    }

    public void activateNavigationButtons() {
        viewBtn.setEnabled(true);
        backBtn.setEnabled(true);
        exportButton.setEnabled(true);
    }

    private JTextField buildTypingArea() {
        final JTextField result = new JTextField(50);

        result.setEnabled(false);

        result.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (result.getSelectedText() != null) {
                    String textToDelete = typingArea.getSelectedText();
                    String selectedLine = result.getText().substring(0,
                            result.getText().lastIndexOf(textToDelete));

                    result.setText(selectedLine);
                    toolbarController.onChangedTextFromTypingArea(result.getText());
                }
            }
        });

        return result;
    }

    private JButton buildOpenButton() {
        JButton result =
                new JButton(new ImageIcon(getClass().getResource((IconSchemes.OPEN_ICON_PATH))));
        result.setToolTipText("Open file");

        result.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolbarController.openArchive();
            }
        });

        result.setBorderPainted(false);
        result.setFocusPainted(true);

        return result;
    }

    private JButton buildBackButton() {
        JButton result =
                new JButton(
                        new ImageIcon(getClass().getResource(IconSchemes.BACK_ICON_PATH)));
        result.setToolTipText("Back");

        result.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolbarController.onGoBackPressed();
            }
        });

        result.setBorderPainted(false);
        result.setFocusPainted(true);
        result.setEnabled(false);

        return result;
    }

    private JButton buildViewButton() {
        JButton result =
                new JButton(new ImageIcon(getClass().getResource(IconSchemes.NEXT_ICON_PATH)));
        result.setToolTipText("Next");

        result.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolbarController.onViewTopClassPressed();
            }
        });

        result.setBorderPainted(false);
        result.setFocusPainted(true);
        result.setEnabled(false);

        return result;
    }

    private JButton buildExportButton() {
        JButton result =
                new JButton(new ImageIcon(getClass().getResource(IconSchemes.EXPORT_ICON_PATH)));

        result.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolbarController.onExportButtonPressed();
            }
        });

        result.setToolTipText("Export");
        result.setBorderPainted(false);
        result.setEnabled(false);

        return result;
    }

    /*
    private JButton buildMappingsButton() {

        JButton result = new JButton("#");
        result.setFont(new Font("Menlo", Font.PLAIN, 18));

        result.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolbarController.onMappingsButtonPressed();
            }
        });

        result.setToolTipText("Export");
        result.setBorderPainted(false);
        result.setForeground(ColorScheme.FOREGROUND_YELLOW);
        result.setBackground(ColorScheme.BLACK);
        result.setEnabled(true);

        return result;
    }
    */

    private JButton buildRecentArchivesButton() {
        RecentArchivesButton result = new RecentArchivesButton();
        result.setPanel(toolbarController);
        return result;
    }

    private JToggleButton buildLeftPanelToggleButton() {
        final ImageIcon toggleIcon = new ImageIcon(getClass().getResource((IconSchemes.TOGGLE_ICON_PATH)));
        final JToggleButton jToggleButton = new JToggleButton(toggleIcon, true);
        jToggleButton.setToolTipText("Show/hide navigation tree");
        jToggleButton.setBorderPainted(false);
        jToggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolbarController.onChangeLeftPaneVisibility(jToggleButton.isSelected());
            }
        });
        return jToggleButton;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ClassyShark");
        Toolbar toolbar = new Toolbar(null);
        toolbar.addKeyListenerToTypingArea(null);

        frame.getContentPane().add(toolbar);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}