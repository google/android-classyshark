package com.google.classyshark.ui;

import com.google.classyshark.ui.viewer.ClassySharkPanel;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class ExperimentalBlackFrame {
    public static void Driver (String args[]) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1000, 800);
        f.setLocationRelativeTo(null);

        f.setUndecorated(true);
        f.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

        File testFile = new File(System.getProperty("user.home") +
                "/Desktop/Scenarios/2 Samples/android.jar");

        ClassySharkPanel tabPanel = new ClassySharkPanel(testFile);
        f.setContentPane(tabPanel);

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