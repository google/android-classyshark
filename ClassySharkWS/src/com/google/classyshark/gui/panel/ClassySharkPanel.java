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

import com.google.classyshark.silverghost.contentreader.ContentReader;
import com.google.classyshark.silverghost.methodscounter.ClassNode;
import com.google.classyshark.silverghost.reducer.Reducer;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.TranslatorFactory;
import com.google.classyshark.gui.panel.methodscount.MethodsCountPanel;
import com.google.classyshark.gui.panel.displayarea.DisplayArea;
import com.google.classyshark.gui.panel.io.CurrentFolderConfig;
import com.google.classyshark.gui.panel.io.Export2FileWriter;
import com.google.classyshark.gui.panel.io.FileChooserUtils;
import com.google.classyshark.gui.panel.io.RecentArchivesConfig;
import com.google.classyshark.gui.panel.toolbar.KeyUtils;
import com.google.classyshark.gui.panel.toolbar.Toolbar;
import com.google.classyshark.gui.panel.toolbar.ToolbarController;
import com.google.classyshark.gui.panel.tree.FilesTree;
import com.google.classyshark.gui.panel.chart.RingChartPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

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

    private Reducer reducer;
    private Translator translator;
    private boolean isDataLoaded = false;
    private File binaryArchive;
    private List<String> allClassNamesInArchive;
    private RingChartPanel ringChartPanel = new RingChartPanel();

    public ClassySharkPanel(JFrame frame, File archive, String fullClassName) {
        this(frame);
        updateUiAfterArchiveReadAndLoadClass(archive, fullClassName);
    }

    public ClassySharkPanel(JFrame frame, File archive) {
        this(frame);
        updateUiAfterArchiveRead(archive);
    }

    public ClassySharkPanel(JFrame frame) {
        super(false);
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        UIManager.put("Button.select", Color.GRAY);
        UIManager.put("ToggleButton.select", Color.GRAY);

        UIManager.put("TabbedPane.contentAreaColor ", ColorScheme.LIGHT_GRAY);
        UIManager.put("TabbedPane.selected", ColorScheme.BLACK);
        UIManager.put("TabbedPane.background", ColorScheme.LIGHT_GRAY);
        UIManager.put("TabbedPane.shadow", ColorScheme.FOREGROUND_CYAN);
        UIManager.put("TabbedPane.darkShadow", ColorScheme.LIGHT_GRAY);
        UIManager.put("TabbedPane.foreground", ColorScheme.FOREGROUND_CYAN);
        UIManager.put("TabbedPane.unselectedTabForeground", ColorScheme.FOREGROUND_CYAN);
        UIManager.put("TabbedPane.selectedForeground", ColorScheme.FOREGROUND_CYAN);
        UIManager.put("TabbedPane.tabInsets", new Insets(5, 10, 5, 10));
        UIManager.put("TabbedPane.font", new Font("SansSerif", Font.BOLD, 16));

        buildUI();
        parentFrame = frame;
        toolbar.setText("");
    }

    @Override
    public void onSelectedTypeClassFromMouseClick(String selectedClass) {
        for (String clazz : translator.getDependencies()) {
            if (clazz.contains(selectedClass)) {
                onSelectedImportFromMouseClick(clazz);
                return;
            }
        }

        for (String clazz : reducer.getAllClassNames()) {
            if (clazz.contains(selectedClass)) {
                onSelectedImportFromMouseClick(clazz);
                return;
            }
        }
    }

    @Override
    public void onSelectedImportFromMouseClick(String className) {
        if (reducer.getAllClassNames().contains(className)) {
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
            updateUiAfterArchiveRead(resultFile);
        }
    }

    @Override
    public void onGoBackPressed() {
        displayArea.displayAllClassesNames(allClassNamesInArchive);
        toolbar.setText("");
        reducer.reduce("");
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
    public void onExportButtonPressed() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Export2FileWriter.writeAllClassNames(reducer.getAllClassNames(),
                        binaryArchive);
                Export2FileWriter.writeCurrentClass(translator);
                Export2FileWriter.writeAllClassContents(reducer.getAllClassNames(),
                        binaryArchive);
                Export2FileWriter.writeAllDexStringTables(binaryArchive);
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
    public void updateUiAfterArchiveRead(File binaryArchive) {
        methodsCountPanel.loadFile(binaryArchive);
        if (parentFrame != null) {
            parentFrame.setTitle(binaryArchive.getName());
        }

        loadAndFillDisplayArea(binaryArchive, null);
        isDataLoaded = true;
        toolbar.activateNavigationButtons();
        filesTree.setVisibleRoot();
    }

    @Override
    public void onSelectedMethodCount(ClassNode rootNode) {
        ringChartPanel.setRootNode(rootNode);
        jSplitPane.setRightComponent(ringChartPanel);
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

        setBackground(ColorScheme.BLACK);

        toolbar = new Toolbar(this);
        add(toolbar, BorderLayout.NORTH);
        toolbar.addKeyListenerToTypingArea(this);

        displayArea = new DisplayArea(this);
        JScrollPane rightScrollPane = new JScrollPane(displayArea.onAddComponentToPane());

        filesTree = new FilesTree(this);
        JTabbedPane jTabbedPane = new JTabbedPane();
        jTabbedPane.setBackground(ColorScheme.BACKGROUND);
        JScrollPane leftScrollPane = new JScrollPane(filesTree.getJTree());

        jTabbedPane.addTab("Archive", leftScrollPane);
        methodsCountPanel = new MethodsCountPanel(this);
        jTabbedPane.addTab("Packages", methodsCountPanel);

        jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        jSplitPane.setBackground(ColorScheme.BACKGROUND);
        jSplitPane.setDividerSize(3);
        jSplitPane.setPreferredSize(new Dimension(1000, 700));

        jSplitPane.add(jTabbedPane, JSplitPane.LEFT);
        jSplitPane.add(rightScrollPane, JSplitPane.RIGHT);
        jSplitPane.getLeftComponent().setVisible(true);
        jSplitPane.setDividerLocation(300);

        add(jSplitPane, BorderLayout.CENTER);
    }

    private void updateUiAfterArchiveReadAndLoadClass(File binaryArchive, String className) {
        if (parentFrame != null) {
            parentFrame.setTitle(binaryArchive.getName());
        }

        loadAndFillDisplayArea(binaryArchive, className);
        isDataLoaded = true;
        toolbar.activateNavigationButtons();
        filesTree.setVisibleRoot();
    }

    private void loadAndFillDisplayArea(final File binaryArchive,
                                        final String className) {
        this.binaryArchive = binaryArchive;
        final ContentReader loader = new ContentReader(this.binaryArchive);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                long start = System.currentTimeMillis();
                loader.load();
                allClassNamesInArchive = loader.getAllClassNames();
                reducer = new Reducer(allClassNamesInArchive);
                System.out.println("Archive Reading "
                        + (System.currentTimeMillis() - start) + " ms ");
                return null;
            }

            @Override
            protected void done() {
                if (isArchiveError()) {
                    filesTree.fillArchive(new File("ERROR"), new ArrayList<String>(), loader.getAllComponents());
                    displayArea.displayError();
                    return;
                }

                filesTree.fillArchive(ClassySharkPanel.this.binaryArchive,
                        allClassNamesInArchive,
                        loader.getAllComponents());

                if (className != null) {
                    onSelectedClassName(className);
                } else {
                    displayArea.displaySharkey();
                }
            }

            private boolean isArchiveError() {
                boolean noJavaClasses = allClassNamesInArchive.isEmpty();
                boolean noAndroidClasses = allClassNamesInArchive.size() == 1
                        && allClassNamesInArchive.contains("AndroidManifest.xml");

                return noJavaClasses || noAndroidClasses;
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
            private List<String> reducedClassNames;
            private String className = "";

            @Override
            protected Void doInBackground() throws Exception {
                if (viewMouseClickedClass) {
                    className = textFromTypingArea;
                    displayedClassTokens = translateClass(className);
                } else if (viewTopClass) {
                    className = reducer.getAutocompleteClassName();
                    displayedClassTokens = translateClass(className);
                } else {
                    reducedClassNames = reducer.reduce(textFromTypingArea);
                    if (reducedClassNames.size() == 1) {
                        displayedClassTokens =
                                translateClass(reducedClassNames.get(0));
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
                    if (reducedClassNames.size() == 1) {
                        displayArea.displayClass(displayedClassTokens);
                    } else if (reducedClassNames.size() == 0) {
                        displayArea.displayError();
                    } else {
                        displayArea.displayReducedClassNames(reducedClassNames,
                                textFromTypingArea);
                    }
                }
            }

            private List<Translator.ELEMENT> translateClass(String name) {
                translator =
                        TranslatorFactory.createTranslator(name, binaryArchive, reducer);
                translator.apply();
                return translator.getElementsList();
            }
        };

        worker.execute();
    }
}