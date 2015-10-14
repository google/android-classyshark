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

import com.google.classyshark.reducer.ArchiveReader;
import com.google.classyshark.reducer.Reducer;
import com.google.classyshark.translator.Translator;
import com.google.classyshark.translator.TranslatorFactory;
import com.google.classyshark.ui.ClassySharkFrame;
import com.google.classyshark.ui.tabs.displayarea.DisplayArea;
import com.google.classyshark.ui.tabs.displayarea.FileStubGenerator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

/**
 * individual tab
 */
public class TabPanel extends JPanel implements KeyListener {

    private static final boolean IS_CLASSNAME_FROM_MOUSE_CLICK = true;
    private static final boolean VIEW_TOP_CLASS = true;

    private final JTabbedPane tabbedPane;
    private final int myIndexAtTabbedPane;

    private final DisplayArea displayArea;
    private final ToolBar toolBar;

    private Reducer reducer;
    private Translator translator;

    private boolean isDataLoaded = false;
    private File loadedFile;
    private List<String> displayedClassNames;

    public TabPanel(JTabbedPane tabbedPane, int myIndex) {
        super(false);

        BorderLayout borderLayout = new BorderLayout();
        this.setLayout(borderLayout);
        this.tabbedPane = tabbedPane;
        this.myIndexAtTabbedPane = myIndex;

        setBackground(ClassySharkFrame.ColorScheme.BLACK);

        toolBar = new ToolBar(this);
        add(toolBar, BorderLayout.NORTH);

        toolBar.addKeyListenerToTypingArea(this);
        toolBar.setTypingArea();

        displayArea = new DisplayArea(this);
        JScrollPane scrollPane = new JScrollPane(displayArea.onAddComponentToPane());
        scrollPane.setPreferredSize(new Dimension(800, 700));

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

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
        final boolean isAutoComplete = isAutoComplete(e);

        fillDisplayArea(textFromTypingArea, isAutoComplete,
                !IS_CLASSNAME_FROM_MOUSE_CLICK);
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    public void openArchive() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else {
                    return ArchiveReader.isSupportedArchiveFile(f);
                }
            }

            @Override
            public String getDescription() {
                return "dex, jar, apk, class";
            }
        });

        fc.setCurrentDirectory(new File(System.getProperty("user.home")));

        int returnVal = fc.showOpenDialog(this);
        toolBar.setText("");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File resultFile = fc.getSelectedFile();
            updateUiAfterFileRead(tabbedPane, resultFile, myIndexAtTabbedPane);
        }
    }

    public void onGoBackPressed() {
        displayArea.displayAllClassesNames(displayedClassNames);
        toolBar.setText("");
        reducer.reduce("");
    }

    public void onViewFilePressed() {
        final String textFromTypingArea = toolBar.getText();
        fillDisplayArea(textFromTypingArea, VIEW_TOP_CLASS,
                !IS_CLASSNAME_FROM_MOUSE_CLICK);
    }

    public void onShowInfoPressed() {
        displayArea.displayInfo();
        toolBar.setText("");
    }

    public void onChangedTextFromTypingArea(String selectedLine) {
        fillDisplayArea(selectedLine, !VIEW_TOP_CLASS, !IS_CLASSNAME_FROM_MOUSE_CLICK);
    }

    public void onSelectedImportFromMouseClick(String className) {
        if (!displayArea.isDisplayingClassesList() && loadedFile.getName().endsWith("jar")) {
            className += ".class";
        }

        if (reducer.getAllClassesNames().contains(className)) {
            onSelectedClassNameFromMouseClick(className);
        }
    }

    public void onSelectedTypeClassfromMouseClick(String key) {

        System.out.println(key);

        for(String clazz : translator.getDependencies()) {
            if(clazz.contains(key)) {
                System.out.println(clazz);
                onSelectedImportFromMouseClick(clazz);
                return;
            }
        }

        for(String clazz : reducer.getAllClassesNames()) {
            if(clazz.contains(key)) {
                System.out.println(clazz);
                onSelectedImportFromMouseClick(clazz);
                return;
            }
        }
    }

    public void onSelectedClassNameFromMouseClick(String className) {
        fillDisplayArea(className, VIEW_TOP_CLASS, IS_CLASSNAME_FROM_MOUSE_CLICK);
    }

    public void updateUiAfterFileRead(JTabbedPane tabbedPane, File resultFile,
                                      int myIndexAtTabbedPane) {
        String tabName = fitArchiveNameToTab(resultFile);
        tabbedPane.setTitleAt(myIndexAtTabbedPane, tabName);
        loadAndFillDisplayArea(resultFile);
        isDataLoaded = true;
        toolBar.activateNavigationButtons();
    }

    private String fitArchiveNameToTab(File resultFile) {
        String tabName = resultFile.getName();

        if (tabName.length() > 7) {
            tabName = tabName.substring(0, 7) + "...";
        }
        return tabName;
    }

    private void loadAndFillDisplayArea(File file) {
        TabPanel.this.loadedFile = file;
        reducer = new Reducer(TabPanel.this.loadedFile);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                long start = System.currentTimeMillis();
                displayedClassNames = reducer.reduce("");
                System.out.println("Archive Reading "
                        + (System.currentTimeMillis() - start) + " ms ");
                return null;
            }

            protected void done() {
                if (!displayedClassNames.isEmpty()) {
                    displayArea.displayAllClassesNames(displayedClassNames);
                } else {
                    displayArea.displayError();
                }
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
        if (e.getKeyCode() == KeyEvent.VK_1) {
            this.tabbedPane.setSelectedIndex(0);
        } else if (e.getKeyCode() == KeyEvent.VK_2) {
            this.tabbedPane.setSelectedIndex(1);
        } else if (e.getKeyCode() == KeyEvent.VK_3) {
            this.tabbedPane.setSelectedIndex(2);
        } else if (e.getKeyCode() == KeyEvent.VK_4) {
            this.tabbedPane.setSelectedIndex(3);
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            FileStubGenerator.generateStubFile(translator.getClassName(),
                    translator.toString());
        }
    }

    private static boolean isOpenFile(KeyEvent e) {
        return (e.getKeyCode() == 37);
    }

    private static boolean isAutoComplete(KeyEvent e) {
        return (e.getKeyCode() == 39);
    }
}