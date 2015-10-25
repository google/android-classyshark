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

import com.google.classyshark.ui.ColorScheme;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.LayoutFocusTraversalPolicy;

/**
 *  ClassyShark window with tabs
 */
public class ClassySharkTabsFrame extends JFrame {
    private JTabbedPane tabbedPane;

    public ClassySharkTabsFrame(String name, List<String> cmdLineArgs) {
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