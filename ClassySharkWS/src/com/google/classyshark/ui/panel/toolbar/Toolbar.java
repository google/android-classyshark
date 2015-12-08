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

package com.google.classyshark.ui.panel.toolbar;

import com.google.classyshark.ui.panel.ColorScheme;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

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
    private JButton analyzerBtn;
    private JToggleButton leftPanelToggleBtn;

    public Toolbar(final ToolbarController toolbarController) {
        super();
        UIManager.put("ToolBar.background", ColorScheme.BACKGROUND);
        UIManager.put("ToolBar.foreground", ColorScheme.BACKGROUND);

        UIManager.put("Button.background", ColorScheme.BACKGROUND);
        UIManager.put("Button.foreground", ColorScheme.WHITE);

        Font f = new Font("Menlo", Font.PLAIN, 18);
        UIManager.put("Button.font", f);

        this.toolbarController = toolbarController;

        typingArea = buildTypingArea();
        openBtn = buildOpenButton();
        backBtn = buildBackButton();
        viewBtn = buildViewButton();
        exportButton = buildExportButton();
        recentArchivesBtn = buildRecentArchivesButton();
        leftPanelToggleBtn = buildLeftPanelToggleButton();
        analyzerBtn = buildAnalyzerButton();

        this.setBackground(ColorScheme.BLACK);

        add(leftPanelToggleBtn);
        add(openBtn);
        add(backBtn);
        add(viewBtn);
        add(typingArea);
        add(exportButton);
        add(analyzerBtn);
        add(recentArchivesBtn);

        setFloatable(false);

        Border roundedBorder = new LineBorder(ColorScheme.BLACK, 5);
        setBorder(roundedBorder);
        setTypingArea();
    }

    public void addKeyListenerToTypingArea(KeyListener kl) {
        typingArea.addKeyListener(kl);
    }

    public void setTypingArea() {
        typingArea.setBackground(ColorScheme.LIGHT_GRAY);

        Font typingAreaFont = new Font("Menlo", Font.PLAIN, 18);
        typingArea.setFont(typingAreaFont);
        typingArea.setForeground(ColorScheme.FOREGROUND_CYAN);

        setTypingAreaCaret();
    }

    public void setTypingAreaCaret() {
        int len = typingArea.getDocument().getLength();
        typingArea.setCaretPosition(len);
        typingArea.setCaretColor(ColorScheme.FOREGROUND_CYAN);
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
        analyzerBtn.setEnabled(true);
    }

    private JTextField buildTypingArea() {
        final JTextField result = new JTextField(50) {
            @Override
            public void setBorder(Border border) {
            }
        };

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
        JButton result = new JButton("Open");

        result.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolbarController.openArchive();
            }
        });

        result.setBorderPainted(false);
        result.setFocusPainted(true);
        result.setForeground(ColorScheme.WHITE);
        result.setBackground(ColorScheme.BLACK);

        return result;
    }

    private JButton buildBackButton() {
        JButton result = new JButton(" <== ");

        result.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolbarController.onGoBackPressed();
            }
        });

        result.setBorderPainted(false);
        result.setFocusPainted(true);
        result.setForeground(ColorScheme.FOREGROUND_YELLOW);
        result.setBackground(ColorScheme.BLACK);
        result.setEnabled(false);

        return result;
    }

    private JButton buildViewButton() {
        JButton result = new JButton(" ==> ");

        result.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolbarController.onViewTopClassPressed();
            }
        });

        result.setBorderPainted(false);
        result.setFocusPainted(true);
        result.setForeground(ColorScheme.FOREGROUND_YELLOW);
        result.setBackground(ColorScheme.BLACK);
        result.setEnabled(false);

        return result;
    }

    private JButton buildExportButton() {
        JButton result = new JButton("\u2551");
        result.setFont(new Font("Menlo", Font.PLAIN, 18));

        result.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolbarController.onExportButtonPressed();
            }
        });

        result.setToolTipText("Export");
        result.setBorderPainted(false);
        result.setForeground(ColorScheme.FOREGROUND_YELLOW);
        result.setBackground(ColorScheme.BLACK);
        result.setEnabled(false);

        return result;
    }

    private JButton buildRecentArchivesButton() {
        RecentArchivesButton result = new RecentArchivesButton();
        result.setPanel(toolbarController);
        return result;
    }

    private JButton buildAnalyzerButton() {
        JButton jButton = new JButton("#");
        jButton.setFont(new Font("Menlo", Font.PLAIN, 18));

        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolbarController.startAnalyzer();
            }
        });

        jButton.setToolTipText("Analyzer");
        jButton.setBorderPainted(false);
        jButton.setForeground(ColorScheme.FOREGROUND_YELLOW);
        jButton.setBackground(ColorScheme.BLACK);
        jButton.setEnabled(false);

        return jButton;
    }

    private JToggleButton buildLeftPanelToggleButton() {
        final JToggleButton jToggleButton = new JToggleButton("\u2592", true);
        jToggleButton.setBorderPainted(false);
        jToggleButton.setFocusPainted(true);
        jToggleButton.setForeground(ColorScheme.FOREGROUND_YELLOW);
        jToggleButton.setBackground(ColorScheme.BLACK);
        jToggleButton.setFont(new Font("Menlo", Font.PLAIN, 18));
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