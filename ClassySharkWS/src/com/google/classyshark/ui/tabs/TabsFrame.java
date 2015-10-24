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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.LayoutFocusTraversalPolicy;

/**
 *  main application form
 */
public class TabsFrame extends JFrame {

    /**
     * application color scheme
     */
    public static class ColorScheme {

        private ColorScheme() {}

        public static final Color FOREGROUND_CYAN = new Color(0xd8, 0xd8, 0xd8);
        public static final Color FOREGROUND_ORANGE = new Color(0xFF, 0x99, 0x33);
        public static final Color FOREGROUND_YELLOW = new Color(0xFF, 0xFF, 0x80);
        public static final Color FOREGROUND_YELLOW_ANNOTATIONS = new Color(0xBB, 0xB5, 0x29);
        public static final Color LIGHT_GRAY = new Color(0x58, 0x58, 0x58);
        public static final Color BACKGROUND = new Color(0x2b, 0x2b, 0x2b);
        public static final Color SELECTION_BG = new Color(0x21, 0x42, 0x83);
        public static final Color BLACK = Color.black;
        public static final Color WHITE = Color.white;
    }

    private JTabbedPane tabbedPane;

    public TabsFrame(String name, List<String> cmdLineArgs) {
        super(name);
        setPreferredSize(new Dimension(1000, 800));
        getContentPane().setBackground(ColorScheme.BACKGROUND);
        addTabbedPane(cmdLineArgs);
    }

    private void addTabbedPane(List<String> args) {
        setBackground(ColorScheme.BACKGROUND);
        tabbedPane = TabsBuilder.build(args);

        getContentPane().add(tabbedPane);
        applyWorkaroundForLoosingActiveTabFocusAfterDialog();
    }

    private void applyWorkaroundForLoosingActiveTabFocusAfterDialog() {
        setFocusCycleRoot(true);
        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
            public Component getDefaultComponent(Container cont) {
                return tabbedPane.getSelectedComponent();
            }
        });
    }
}