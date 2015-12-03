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

import com.google.classyshark.reducer.Reducer;
import com.google.classyshark.ui.panel.ClassySharkPanel;
import com.google.classyshark.ui.panel.io.Export2FileWriter;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/**
 * the driver class of the app
 */
public class Main {

    private Main() {
    }

    private static void setParamsForOtherPlatforms() throws Exception {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
    }

    public static void buildAndShowClassySharkFrame(List<String> cmdLineArgs) {
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

    private static void workInShellMode(List<String> args) {
        if (args.size() > 2) {
            System.out.println("Too many arguments ==> java -jar ClassyShark.jar -dump FILE");
            return;
        }

        if (args.get(0).equalsIgnoreCase("-dump")) {
            System.out.println("Wrong -dump argument ==> java -jar ClassyShark.jar -dump FILE");
            return;
        }

        File archiveFile = new File(args.get(1));
        if (!archiveFile.exists()) {
            System.out.println("File doesn't exist ==> java -jar ClassyShark.jar -dump FILE");
            return;
        }

        Reducer reducer = new Reducer(archiveFile);
        reducer.reduce("");

        try {
            Export2FileWriter.writeAllClassContents(reducer, archiveFile);
        } catch (Exception e) {
            System.out.println("Internal error - couldn't write file");
        }
    }

    private static boolean isInUIMode(List<String> argsAsArray) {
        return argsAsArray.isEmpty() || argsAsArray.size() == 1
                || argsAsArray.get(0).equalsIgnoreCase("-open");
    }

    public static void main(final String[] args) {
        final List<String> argsAsArray = Arrays.asList(args);

        if (isInUIMode(argsAsArray)) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    buildAndShowClassySharkFrame(argsAsArray);
                }
            });
        } else {
            workInShellMode(argsAsArray);
        }
    }
}