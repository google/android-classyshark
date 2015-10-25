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

package com.google.classyshark.ui;

import com.google.classyshark.ui.tabs.ClassySharkTabsFrame;
import com.google.classyshark.ui.viewer.ClassySharkPanel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/**
 *  the driver class of the app
 */
public class Main {

    private static void setParamsForOtherPlatforms() throws Exception {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException | IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        final List<String> cmdLineArgs = Arrays.asList(args);

        UIManager.put("swing.boldMetal", Boolean.FALSE);
        UIManager.put("Button.select", Color.GRAY);
        UIManager.put("ToggleButton.select", Color.GRAY);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                buildAndShowClassySharkFrame(cmdLineArgs);
            }
        });
    }

    private static void buildAndShowClassySharkFrame(List<String> cmdLineArgs) {
        JFrame frame;

        if(isMultiTab(cmdLineArgs)) {
           frame = new ClassySharkTabsFrame("ClassyShark Browser", cmdLineArgs);
        } else {
            frame = new JFrame();
            frame.getContentPane().add(new ClassySharkPanel(null, 1));
        }

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static boolean isMultiTab(List<String> cmdLineArgs) {
        if(cmdLineArgs.contains("-t")) {
            return true;
        }

        boolean moreThanOneFile = cmdLineArgs.size() >= 2;
        return moreThanOneFile;
    }
}
