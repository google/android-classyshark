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

package com.google.classyshark.gui.panel;

import com.google.classyshark.gui.GuiMode;
import com.google.classyshark.gui.panel.chart.RingChartPanel;
import com.google.classyshark.gui.panel.displayarea.DisplayArea;
import com.google.classyshark.gui.panel.io.CurrentFolderConfig;
import com.google.classyshark.gui.panel.io.FileChooserUtils;
import com.google.classyshark.gui.panel.io.RecentArchivesConfig;
import com.google.classyshark.gui.panel.methodscount.MethodsCountPanel;
import com.google.classyshark.gui.panel.toolbar.KeyUtils;
import com.google.classyshark.gui.panel.toolbar.Toolbar;
import com.google.classyshark.gui.panel.toolbar.ToolbarController;
import com.google.classyshark.gui.panel.tree.FilesTree;
import com.google.classyshark.gui.settings.SettingsFrame;
import com.google.classyshark.gui.theme.Theme;
import com.google.classyshark.silverghost.SilverGhost;
import com.google.classyshark.silverghost.exporter.Exporter;
import com.google.classyshark.silverghost.methodscounter.ClassNode;
import com.google.classyshark.silverghost.tokensmapper.ProguardMapper;
import com.google.classyshark.silverghost.translator.Translator;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * App controller, general app structure MVM ==> Model - View - Mediator (this class)
 */
public class ClassySharkPanel extends JPanel
        implements ToolbarController, ViewerController, KeyListener {

    private static final boolean IS_CLASSNAME_FROM_MOUSE_CLICK = true;
    private static final boolean VIEW_TOP_CLASS = true;

    private JFrame parentFrame;
    private Toolbar toolbar;
    private JSplitPane jSplitPane;
    private MethodsCountPanel methodsCountPanel;
    private int dividerLocation = 0;
    private DisplayArea displayArea;
    private FilesTree filesTree;
    private RingChartPanel ringChartPanel;
    private boolean isDataLoaded = false;

    private final Theme theme = GuiMode.getTheme();

    private SilverGhost silverGhost = new SilverGhost();

    public ClassySharkPanel(JFrame frame, File archive, String fullClassName) {
        this(frame);
        silverGhost.setBinaryArchive(archive);
        updateUiAfterArchiveReadAndLoadClass(fullClassName);
    }

    public ClassySharkPanel(JFrame frame, File archive) {
        this(frame);
        silverGhost.setBinaryArchive(archive);
        displayArchive(silverGhost.getBinaryArchive());
    }

    public ClassySharkPanel(JFrame frame) {
        super(false);
        buildUI();
        parentFrame = frame;
        toolbar.setText("");
        theme.applyTo(this);
    }

    @Override
    public void onSelectedTypeClassFromMouseClick(String selectedClass) {
        for (String clazz : silverGhost.getImportsForCurrentClass()) {
            if (clazz.contains(selectedClass)) {
                onSelectedImportFromMouseClick(clazz);
                return;
            }
        }

        for (String clazz : silverGhost.getAllClassNames()) {
            if (clazz.contains(selectedClass)) {
                onSelectedImportFromMouseClick(clazz);
                return;
            }
        }
    }

    @Override
    public void onSelectedImportFromMouseClick(String className) {
        if (silverGhost.getAllClassNames().contains(className)) {
            onSelectedClassName(className);
        }
    }

    @Override
    public void onSelectedClassName(String className) {
        fillDisplayArea(className, VIEW_TOP_CLASS, IS_CLASSNAME_FROM_MOUSE_CLICK);
    }

    @Override
    public void openArchive() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return FileChooserUtils.acceptFile(f);
            }

            @Override
            public String getDescription() {
                return FileChooserUtils.getFileChooserDescription();
            }
        });

        fc.setCurrentDirectory(CurrentFolderConfig.INSTANCE.getCurrentDirectory());
        int returnVal = fc.showOpenDialog(this);
        toolbar.setText("");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File resultFile = fc.getSelectedFile();
            CurrentFolderConfig.INSTANCE.setCurrentDirectory(fc.getCurrentDirectory());
            RecentArchivesConfig.INSTANCE.addArchive(resultFile.getName(),
                    fc.getCurrentDirectory());
            displayArchive(resultFile);
        }
    }

    @Override
    public void onGoBackPressed() {
        toolbar.setText("");
        displayArea.displayClassNames(silverGhost.getAllClassNames(), "");
        silverGhost.initClassNameFiltering();
    }

    @Override
    public void onViewTopClassPressed() {
        final String textFromTypingArea = toolbar.getText();
        fillDisplayArea(textFromTypingArea, VIEW_TOP_CLASS,
                !IS_CLASSNAME_FROM_MOUSE_CLICK);
    }

    @Override
    public void onChangedTextFromTypingArea(String selectedLine) {
        fillDisplayArea(selectedLine, !VIEW_TOP_CLASS, !IS_CLASSNAME_FROM_MOUSE_CLICK);
    }

    @Override
    public void onMappingsButtonPressed() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return true;
            }

            @Override
            public String getDescription() {
                return "";
            }
        });

        fc.setCurrentDirectory(CurrentFolderConfig.INSTANCE.getCurrentDirectory());

        int returnVal = fc.showOpenDialog(this);
        toolbar.setText("");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File resultFile = fc.getSelectedFile();
            readMappingFile(resultFile);
        }
    }

    @Override
    public void onExportButtonPressed() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Exporter.writeCurrentClass(silverGhost.getCurrentClassName(),
                        silverGhost.getCurrentClassContent());
                Exporter.writeArchive(silverGhost.getBinaryArchive(),
                        silverGhost.getAllClassNames());
                return null;
            }

            protected void done() {
            }
        };

        worker.execute();
    }

    @Override
    public void onChangeLeftPaneVisibility(boolean visible) {
        if (visible) {
            jSplitPane.setDividerLocation(dividerLocation);
        } else {
            dividerLocation = jSplitPane.getDividerLocation();
        }
        jSplitPane.getLeftComponent().setVisible(visible);
        jSplitPane.updateUI();
    }

    @Override
    public void onSettingsButtonPressed() {
        SettingsFrame frame = new SettingsFrame();
    }

    @Override
    public void displayArchive(File binaryArchive) {
        silverGhost.setBinaryArchive(binaryArchive);

        if (parentFrame != null) {
            parentFrame.setTitle(silverGhost.getBinaryArchive().getName());
        }

        readArchiveAndFillDisplayArea(null);
        toolbar.activateNavigationButtons();
        filesTree.setVisibleRoot();
        methodsCountPanel.loadFile(silverGhost.getBinaryArchive());
    }

    @Override
    public void onSelectedMethodCount(ClassNode rootNode) {
        ringChartPanel.setRootNode(rootNode);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!isDataLoaded) {
            openArchive();
            return;
        }

        if (KeyUtils.isLeftArrowPressed(e)) {
            openArchive();
            return;
        }

        final String textFromTypingArea =
                processKeyPressWithTypedText(e, toolbar.getText());
        final boolean isViewTopClassKeyPressed = KeyUtils.isRightArrowPressed(e)
                || KeyUtils.isCommandKeyPressed(e);

        fillDisplayArea(textFromTypingArea, isViewTopClassKeyPressed,
                !ClassySharkPanel.IS_CLASSNAME_FROM_MOUSE_CLICK);
    }

    private static String processKeyPressWithTypedText(KeyEvent e, String text) {
        String result = text;

        if (KeyUtils.isDeletePressed(e)) {
            if (!text.isEmpty()) {
                result = text.substring(0, text.length() - 1);
                return result;
            }
        }

        if (KeyUtils.isLetterOrDigit(e)) {
            result += e.getKeyChar();
        }

        return result;
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private void buildUI() {
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);

        ringChartPanel = new RingChartPanel(this);

        toolbar = new Toolbar(this);
        add(toolbar, BorderLayout.NORTH);
        toolbar.addKeyListenerToTypingArea(this);

        displayArea = new DisplayArea(this);
        final JScrollPane rightScrollPane = new JScrollPane(displayArea.onAddComponentToPane());
        theme.applyTo(rightScrollPane);

        filesTree = new FilesTree(this);
        JTabbedPane jTabbedPane = new JTabbedPane();
        JScrollPane leftScrollPane = new JScrollPane(filesTree.getJTree());
        theme.applyTo(leftScrollPane);

        jTabbedPane.addTab("Classes", leftScrollPane);
        methodsCountPanel = new MethodsCountPanel(this);
        jTabbedPane.addTab("Methods count", methodsCountPanel);
        theme.applyTo(jTabbedPane);

        jTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int dividerLocation1 = jSplitPane.getDividerLocation();
                JTabbedPane jTabbedPane1 = (JTabbedPane) e.getSource();
                if (jTabbedPane1.getSelectedIndex() == 0) {
                    jSplitPane.setRightComponent(rightScrollPane);
                } else {
                    jSplitPane.setRightComponent(ringChartPanel);
                }
                jSplitPane.setDividerLocation(dividerLocation1);
            }
        });

        jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        jSplitPane.setDividerSize(3);
        jSplitPane.setPreferredSize(new Dimension(1000, 700));

        jSplitPane.add(jTabbedPane, JSplitPane.LEFT);
        jSplitPane.add(rightScrollPane, JSplitPane.RIGHT);
        jSplitPane.getLeftComponent().setVisible(true);
        jSplitPane.setDividerLocation(300);

        theme.applyTo(jSplitPane);

        add(jSplitPane, BorderLayout.CENTER);
    }

    private void updateUiAfterArchiveReadAndLoadClass(String className) {
        if (parentFrame != null) {
            parentFrame.setTitle(silverGhost.getBinaryArchive().getName());
        }

        readArchiveAndFillDisplayArea(className);
        toolbar.activateNavigationButtons();
        filesTree.setVisibleRoot();
        methodsCountPanel.loadFile(silverGhost.getBinaryArchive());
    }

    private void readArchiveAndFillDisplayArea(final String className) {

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                silverGhost.readContents();
                return null;
            }

            @Override
            protected void done() {
                if (silverGhost.isArchiveError()) {
                    filesTree.fillArchive(new File("ERROR"), new ArrayList<String>(),
                            silverGhost.getComponents());
                    displayArea.displayError();
                    return;
                }

                filesTree.fillArchive(silverGhost.getBinaryArchive(),
                        silverGhost.getAllClassNames(),
                        silverGhost.getComponents());

                if (className != null) {
                    onSelectedClassName(className);
                } else {
                    displayArea.displaySharkey();
                }
                isDataLoaded = true;
            }
        };

        worker.execute();
    }

    private void fillDisplayArea(final String textFromTypingArea,
                                 final boolean viewTopClass,
                                 final boolean viewMouseClickedClass) {
        toolbar.setTypingAreaCaret();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<Translator.ELEMENT> displayedClassTokens;
            private List<String> filteredClassNames;
            private String className = "";

            @Override
            protected Void doInBackground() throws Exception {
                if (viewMouseClickedClass) {
                    className = textFromTypingArea;
                    silverGhost.translateArchiveElement(className);
                    displayedClassTokens = silverGhost.getArchiveElementTokens();
                } else if (viewTopClass) {
                    className = silverGhost.getAutoCompleteClassName();
                    silverGhost.translateArchiveElement(className);
                    displayedClassTokens = silverGhost.getArchiveElementTokens();
                } else {
                    filteredClassNames = silverGhost.filter(textFromTypingArea);
                    if (filteredClassNames.size() == 1) {
                        silverGhost.translateArchiveElement(filteredClassNames.get(0));
                        displayedClassTokens = silverGhost.getArchiveElementTokens();
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                if (viewTopClass || viewMouseClickedClass) {
                    toolbar.setText(className);
                    displayArea.displayClass(displayedClassTokens);
                } else {
                    if (filteredClassNames.size() == 1) {
                        displayArea.displayClass(displayedClassTokens);
                    } else if (filteredClassNames.size() == 0) {
                        displayArea.displayError();
                    } else {
                        displayArea.displayClassNames(filteredClassNames,
                                textFromTypingArea);
                    }
                }
            }
        };

        worker.execute();
    }

    private void readMappingFile(final File resultFile) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private ProguardMapper reverseMappings;

            @Override
            protected Void doInBackground() throws Exception {
                reverseMappings = SilverGhost.readMappingFile(resultFile);
                return null;
            }

            protected void done() {
                silverGhost.addMappings(reverseMappings);
            }
        };

        worker.execute();
    }
}