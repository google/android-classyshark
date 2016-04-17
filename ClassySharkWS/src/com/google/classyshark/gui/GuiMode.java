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

package com.google.classyshark.gui;

import com.google.classyshark.gui.panel.ClassySharkPanel;
import com.google.classyshark.gui.theme.Theme;
import com.google.classyshark.gui.theme.light.LightTheme;

import java.io.File;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/**
 * GUI mode
 */
public class GuiMode {

    private static Theme theme = new LightTheme();

    private GuiMode() {
    }

    public static void with(final List<String> argsAsArray) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                buildAndShowClassyShark(argsAsArray);
            }
        });
    }

    public static Theme getTheme(){
        return theme;
    }

    private static void buildAndShowClassyShark(List<String> cmdLineArgs) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | ClassNotFoundException
                | InstantiationException | SecurityException ex) {
            ex.printStackTrace();
        }

        JFrame frame = buildClassySharkFrame(cmdLineArgs);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static JFrame buildClassySharkFrame(List<String> cmdLineArgs) {
        JFrame result = new JFrame("ClassyShark");

        // no arguments
        if (cmdLineArgs.size() == 0) {
            result.getContentPane().add(new ClassySharkPanel(result));
            return result;
        }

        // only archive
        if (cmdLineArgs.size() == 2) {
            result.getContentPane().add(
                    new ClassySharkPanel(result, new File(cmdLineArgs.get(1))));
            return result;
        }

        // archive and a class file
        if (cmdLineArgs.size() == 3) {
            result.getContentPane().add(
                    new ClassySharkPanel(result, new File(cmdLineArgs.get(1)),
                            cmdLineArgs.get(2)));
            return result;
        }

        return result;
    }
}
