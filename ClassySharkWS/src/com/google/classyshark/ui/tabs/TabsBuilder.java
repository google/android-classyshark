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

package com.google.classyshark.ui.tabs;

import com.google.classyshark.reducer.ArchiveReader;
import com.google.classyshark.ui.tabs.TabsFrame.ColorScheme;
import com.google.classyshark.ui.tabs.tabpanel.TabPanel;
import java.awt.Font;
import java.io.File;
import java.util.List;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

/**
 * Builds the tabbed pane
 */
public class TabsBuilder {

    public static final int NUMBER_OF_TABS = 6;

    public static JTabbedPane build(List<String> cmdLineArgFiles) {
        UIManager.put("TabbedPane.contentAreaColor ", ColorScheme.LIGHT_GRAY);
        UIManager.put("TabbedPane.selected", ColorScheme.BLACK);
        UIManager.put("TabbedPane.background", ColorScheme.LIGHT_GRAY);
        UIManager.put("TabbedPane.shadow", ColorScheme.LIGHT_GRAY);

        final JTabbedPane result = new JTabbedPane();

        result.setBackground(ColorScheme.BACKGROUND);

        Font tabFont = new Font("SansSerif", Font.PLAIN, 20);
        result.setFont(tabFont);

        for (int i = 0; i < cmdLineArgFiles.size(); i++) {
            TabPanel panel = new TabPanel(result, i);
            result.addTab("Opening", panel);
            result.setForegroundAt(i, ColorScheme.FOREGROUND_CYAN);

            File cmdFile = new File(cmdLineArgFiles.get(i));
            if (cmdFile.exists() && ArchiveReader.isSupportedArchiveFile(cmdFile)) {
                panel.updateUiAfterFileRead(result, new File(cmdLineArgFiles.get(i)), i);
            }
        }

        for (int i = 0; i < NUMBER_OF_TABS - cmdLineArgFiles.size(); i++) {
            TabPanel panel = new TabPanel(result, i + cmdLineArgFiles.size());
            result.addTab("Open ...", panel);
            result.setForegroundAt(i + cmdLineArgFiles.size(), ColorScheme.FOREGROUND_CYAN);
        }

        return result;
    }
}
