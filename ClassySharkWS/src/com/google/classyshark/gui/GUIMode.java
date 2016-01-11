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
import java.io.File;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/**
 * UI mode that loads the GUI
 */
public class GuiMode {

    private GuiMode(){
    }

    public static void with(final List<String> argsAsArray) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                buildAndShowClassySharkFrame(argsAsArray);
            }
        });
    }

    private static void setParamsForOtherPlatforms() throws Exception {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
    }

    private static void buildAndShowClassySharkFrame(List<String> cmdLineArgs) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException | IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("ClassyShark");

        if (cmdLineArgs.size() == 0) {
            frame.getContentPane().add(new ClassySharkPanel(frame));
        } else if (cmdLineArgs.size() == 1) {
            frame.getContentPane().add(
                    new ClassySharkPanel(frame, new File(cmdLineArgs.get(0))));
        } else {
            frame.getContentPane().add(
                    new ClassySharkPanel(frame, new File(cmdLineArgs.get(1)),
                            cmdLineArgs.get(2)));
        }

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
