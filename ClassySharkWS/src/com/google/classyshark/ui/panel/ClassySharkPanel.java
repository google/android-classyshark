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

package com.google.classyshark.ui.panel;

import com.google.classyshark.reducer.Reducer;
import com.google.classyshark.translator.Translator;
import com.google.classyshark.translator.TranslatorFactory;
import com.google.classyshark.ui.panel.displayarea.DisplayArea;
import com.google.classyshark.ui.panel.toolbar.RecentArchivesConfig;
import com.google.classyshark.ui.panel.toolbar.Toolbar;
import com.google.classyshark.ui.panel.tree.FilesTree;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

/**
 * App controller and view
 */
public class ClassySharkPanel extends JPanel implements KeyListener {

    private static final boolean IS_CLASSNAME_FROM_MOUSE_CLICK = true;
    private static final boolean VIEW_TOP_CLASS = true;

    private JFrame jFrame;

    private Toolbar toolBar;
    private JSplitPane jSplitPane;
    private int dividerLocation = 0;
    private DisplayArea displayArea;
    private FilesTree filesTree;

    private Reducer reducer;
    private Translator translator;
    private boolean isDataLoaded = false;
    private File loadedFile;
    private List<String> allClassesInArchive;

    public ClassySharkPanel(JFrame frame, File archive) {
        super(false);
        buildUI();
        jFrame = frame;
        toolBar.setText("");
        updateUiAfterFileRead(archive);
    }

    public ClassySharkPanel(JFrame frame) {
        super(false);
        buildUI();
        jFrame = frame;
        toolBar.setText("");
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isControlDown()) {
            handleControlPress(e);
            return;
        }

        if (!isDataLoaded) {
            openArchive();
            return;
        }

        if (isOpenFile(e)) {
            openArchive();
            return;
        }

        final String textFromTypingArea =
                processKeyPressWithTypedText(e, toolBar.getText());
        final boolean isViewTopClassKeyPressed = isViewTopClassKeyPressed(e);

        fillDisplayArea(textFromTypingArea, isViewTopClassKeyPressed,
                !IS_CLASSNAME_FROM_MOUSE_CLICK);
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public void openArchive() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return ClassySharkPanelUtils.acceptFile(f);
            }

            @Override
            public String getDescription() {
                return ClassySharkPanelUtils.getFileChooserDescription();
            }
        });

        fc.setCurrentDirectory(CurrentFolderConfig.INSTANCE.getCurrentDirectory());

        int returnVal = fc.showOpenDialog(this);
        toolBar.setText("");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File resultFile = fc.getSelectedFile();
            CurrentFolderConfig.INSTANCE.setCurrentDirectory(fc.getCurrentDirectory());
            RecentArchivesConfig.INSTANCE.addArchive(resultFile.getName(), fc.getCurrentDirectory());
            updateUiAfterFileRead(resultFile);
        }
    }

    public void onGoBackPressed() {
        displayArea.displayAllClassesNames(allClassesInArchive);
        toolBar.setText("");
        reducer.reduce("");
    }

    public void onViewTopClassPressed() {
        final String textFromTypingArea = toolBar.getText();
        fillDisplayArea(textFromTypingArea, VIEW_TOP_CLASS,
                !IS_CLASSNAME_FROM_MOUSE_CLICK);
    }

    public void onChangedTextFromTypingArea(String selectedLine) {
        fillDisplayArea(selectedLine, !VIEW_TOP_CLASS, !IS_CLASSNAME_FROM_MOUSE_CLICK);
    }

    public void onSelectedTypeClassFromMouseClick(String selectedClass) {
        for (String clazz : translator.getDependencies()) {
            if (clazz.contains(selectedClass)) {
                onSelectedImportFromMouseClick(clazz);
                return;
            }
        }

        for (String clazz : reducer.getAllClassesNames()) {
            if (clazz.contains(selectedClass)) {
                onSelectedImportFromMouseClick(clazz);
                return;
            }
        }
    }

    public void onSelectedImportFromMouseClick(String className) {
        if (!displayArea.isDisplayingClassesList() && loadedFile.getName().endsWith("jar")) {
            className += ".class";
        }

        if (reducer.getAllClassesNames().contains(className)) {
            onSelectedClassName(className);
        }
    }

    public void onExportButtonPressed() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String> arr = reducer.getAllClassesNames();
                FileWriter writer = new FileWriter(loadedFile.getName() + "_dump.txt");
                for(String str: arr) {
                    writer.write("\n" + str);
                }
                writer.close();

                writer = new FileWriter(translator.getClassName() + "_dump");
                writer.write(translator.toString());
                writer.close();

                return null;
            }

            protected void done() {
            }
        };

        worker.execute();
    }

    public void onSelectedClassName(String className) {
        fillDisplayArea(className, VIEW_TOP_CLASS, IS_CLASSNAME_FROM_MOUSE_CLICK);
    }

    public void onChangeLeftPaneVisibility(boolean visible) {
        if (visible) {
            jSplitPane.setDividerLocation(dividerLocation);
        } else {
            dividerLocation = jSplitPane.getDividerLocation();
        }
        jSplitPane.getLeftComponent().setVisible(visible);
        jSplitPane.updateUI();
    }

    public void updateUiAfterFileRead(File resultFile) {
        if (jFrame != null) {
            jFrame.setTitle(resultFile.getName());
        }

        loadAndFillDisplayArea(resultFile);
        isDataLoaded = true;
        toolBar.activateNavigationButtons();
        filesTree.setVisibleRoot();
    }

    private void buildUI() {
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);

        setBackground(ColorScheme.BLACK);

        toolBar = new Toolbar(this);
        add(toolBar, BorderLayout.NORTH);

        toolBar.addKeyListenerToTypingArea(this);
        toolBar.setTypingArea();

        displayArea = new DisplayArea(this);
        JScrollPane rightScrollPane = new JScrollPane(displayArea.onAddComponentToPane());

        filesTree = new FilesTree(this);
        JScrollPane leftScrollPane = new JScrollPane(filesTree.getJTree());

        jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        jSplitPane.setDividerSize(3);
        jSplitPane.setPreferredSize(new Dimension(1000, 700));

        jSplitPane.add(leftScrollPane, JSplitPane.LEFT);
        jSplitPane.add(rightScrollPane, JSplitPane.RIGHT);
        jSplitPane.getLeftComponent().setVisible(true);
        jSplitPane.setDividerLocation(300);

        add(jSplitPane, BorderLayout.CENTER);
    }

    private void loadAndFillDisplayArea(final File file) {
        loadedFile = file;
        reducer = new Reducer(loadedFile);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                long start = System.currentTimeMillis();
                allClassesInArchive = reducer.reduce("");
                System.out.println("Archive Reading "
                        + (System.currentTimeMillis() - start) + " ms ");
                return null;
            }

            protected void done() {
                if (allClassesInArchive.isEmpty()
                        || (allClassesInArchive.size() == 1
                        && allClassesInArchive.contains("AndroidManifest.xml"))) {
                    filesTree.fillArchive(new File("XXXXX"), new ArrayList<String>());
                    displayArea.displayError();
                    return;
                }

                filesTree.fillArchive(loadedFile, allClassesInArchive);
                displayArea.displaySharkey();
            }
        };

        worker.execute();
    }

    private String processKeyPressWithTypedText(KeyEvent e, String text) {
        int code = e.getKeyCode();
        String result = text;

        // delete
        if (code == 8) {
            if (!text.isEmpty()) {
                result = text.substring(0, text.length() - 1);
            }
        } else {
            // TODO handle only letters
            result += e.getKeyChar();
        }
        return result;
    }

    private void fillDisplayArea(final String textFromTypingArea,
                                 final boolean viewTopClass,
                                 final boolean viewMouseClickedClass) {
        toolBar.setTypingAreaCaret();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<Translator.ELEMENT> displayedClassTokens;
            private List<String> reducedClassesNames;
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
                    reducedClassesNames = reducer.reduce(textFromTypingArea);
                    if (reducedClassesNames.size() == 1) {
                        displayedClassTokens =
                                translateClass(reducedClassesNames.get(0));
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                if (viewTopClass || viewMouseClickedClass) {
                    toolBar.setText(className);
                    displayArea.displayClass(displayedClassTokens);
                } else {
                    if (reducedClassesNames.size() == 1) {
                        displayArea.displayClass(displayedClassTokens);
                    } else if (reducedClassesNames.size() == 0) {
                        displayArea.displayError();
                    } else {
                        displayArea.displayReducedClassesNames(reducedClassesNames,
                                textFromTypingArea);
                    }
                }
            }

            private List<Translator.ELEMENT> translateClass(String name) {
                translator =
                        TranslatorFactory.createTranslator(name, loadedFile);
                translator.apply();
                return translator.getElementsList();
            }
        };

        worker.execute();
    }

    private void handleControlPress(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_S) {
            ClassySharkPanelUtils.generateStubFile(translator);
            return;
        }
    }

    private static boolean isOpenFile(KeyEvent e) {
        return (e.getKeyCode() == 37);
    }

    private static boolean isViewTopClassKeyPressed(KeyEvent e) {
        return (e.getKeyCode() == 39);
    }

    public static void main(String[] args) {
        File testFile = new File(System.getProperty("user.home") +
                "/Desktop/Scenarios/2 Samples/android.jar");
        JFrame frame = new JFrame(testFile.getName());
        ClassySharkPanel tabPanel = new ClassySharkPanel(frame, testFile);
        frame.getContentPane().add(tabPanel);
        frame.pack();
        frame.setVisible(true);
    }
}