package com.google.classyshark.ui.viewer.tree;

import com.google.classyshark.reducer.Reducer;
import com.google.classyshark.ui.ColorScheme;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class ExperimentalBlackTree {
    public static void Driver (String args[]) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(600, 800);
        f.setLocationRelativeTo(null);

        f.setUndecorated(true);
        f.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

        DefaultMetalTheme blackSchemeForMetal =
                new DefaultMetalTheme() {
                    public ColorUIResource getWindowTitleInactiveBackground() {
                        return new ColorUIResource
                                (ColorScheme.BLACK);
                    }

                    public ColorUIResource getWindowTitleBackground() {
                        return new ColorUIResource
                                (ColorScheme.BLACK);
                    }

                    public ColorUIResource getPrimaryControlHighlight() {
                        return new ColorUIResource
                                (ColorScheme.BLACK);
                    }

                    public ColorUIResource getPrimaryControlDarkShadow() {
                        return new ColorUIResource
                                (ColorScheme.BLACK);
                    }

                    public ColorUIResource getPrimaryControl() {
                        return new ColorUIResource
                                (ColorScheme.BLACK);
                    }

                    public ColorUIResource getControlHighlight() {
                        return new ColorUIResource
                                (ColorScheme.BLACK);
                    }

                    public ColorUIResource getControlDarkShadow() {
                        return new ColorUIResource
                                (ColorScheme.BLACK);
                    }


                    public ColorUIResource getControl() {
                        return new ColorUIResource
                                (ColorScheme.BLACK);
                    }
                };

        MetalLookAndFeel.setCurrentTheme(blackSchemeForMetal);

        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.updateComponentTreeUI(f);

        ////
        File testFile = new File(System.getProperty("user.home") +
                "/Desktop/Scenarios/2 Samples/android.jar");
        FilesTree filesTree = new FilesTree(null);

        Reducer reducer = new Reducer(testFile);
        reducer.reduce("");
        filesTree.fillArchive(testFile, reducer.getAllClassesNames());
        JScrollPane scrolledTree = new JScrollPane(filesTree.getJTree());

        f.setContentPane(scrolledTree);
        ///


        f.setVisible(true);
    }

    public static void main(final String args[]) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        Driver(args);
                    }
                }
        );
    }
}