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

package com.google.classyshark.ui.viewer.toolbar;

import com.google.classyshark.ui.ColorScheme;
import com.google.classyshark.ui.viewer.ClassySharkPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 *  recent files button
 */
public class RecentArchivesButton extends JButton {

    private JPopupMenu popup;
    private ClassySharkPanel panel;

    public RecentArchivesButton() {
        popup = new JPopupMenu();
        popup.setPreferredSize(new Dimension(200, 100));
        popup.addPopupMenuListener(new PopupPrintListener());
        buildPopup();

        setBorderPainted(false);
        setFocusPainted(true);
        setForeground(ColorScheme.FOREGROUND_YELLOW);
        setBackground(ColorScheme.BLACK);
        setFont(new Font("Menlo", Font.BOLD, 18));
        setText("፨");
        addMouseListener(new MousePopupListener());
    }

    public void setPanel(ClassySharkPanel panel) {
        this.panel = panel;
    }

    private void buildPopup() {
        popup.removeAll();
        JMenuItem item;

        for (String archiveName : RecentFilesConfig.INSTANCE.getRecentArchiveNames()) {
            item = new JMenuItem(archiveName);
            item.setFont(new Font("Menlo", Font.BOLD, 16));
            popup.add(item);
            item.setHorizontalTextPosition(JMenuItem.RIGHT);
            item.addActionListener(new RecentFilesListener(archiveName));
        }

        popup.addSeparator();
        final JMenuItem clearRecentArchivesItem = new JMenuItem("Clear");
        clearRecentArchivesItem.setFont(new Font("Menlo", Font.BOLD, 16));
        popup.add(clearRecentArchivesItem);
        clearRecentArchivesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RecentFilesConfig.INSTANCE.clear();
                popup.removeAll();
                popup.add(clearRecentArchivesItem);
            }
        });
    }

    private class RecentFilesListener implements ActionListener {
        private String archiveName;

        public RecentFilesListener(String archiveName) {
            this.archiveName = archiveName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            panel.updateUiAfterFileRead(
                    new File(RecentFilesConfig.INSTANCE.getFilePath(archiveName),
                            archiveName));
            buildPopup();
        }
    }

    private class MousePopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            checkPopup(e);
        }

        public void mouseClicked(MouseEvent e) {
            checkPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            checkPopup(e);
        }

        private void checkPopup(MouseEvent e) {
            popup.show(RecentArchivesButton.this, e.getX() - 200, e.getY());
        }
    }

    private class PopupPrintListener implements PopupMenuListener {
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            buildPopup();
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }
}