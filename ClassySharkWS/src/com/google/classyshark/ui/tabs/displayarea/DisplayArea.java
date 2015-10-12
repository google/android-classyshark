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

package com.google.classyshark.ui.tabs.displayarea;

import com.google.classyshark.translator.Translator;
import com.google.classyshark.translator.Translator2Java;
import com.google.classyshark.ui.ClassySharkFrame.ColorScheme;
import com.google.classyshark.ui.tabs.TabPanel;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.Utilities;

/**
 *  the area to display lists of classes and individual class
 */
public class DisplayArea {

    private enum DisplayDataState {
        SHARKEY, INFO, CLASSES_LIST, INSIDE_CLASS, ERROR
    }

    private final JTextPane jTextPane;
    private Style style;

    private JPopupMenu popup;
    private DisplayDataState displayDataState;

    public DisplayArea(final TabPanel tabPanel) {
        popup = setupPopup();

        jTextPane = new JTextPane();
        jTextPane.setEditable(false);
        jTextPane.setBackground(ColorScheme.BACKGROUND);

        jTextPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(displayDataState == DisplayDataState.SHARKEY
                   || displayDataState == DisplayDataState.INFO) {
                    return;
                }

                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                if (e.getClickCount() != 2) {
                    return;
                }

                int offset = jTextPane.viewToModel(e.getPoint());

                try {
                    int rowStart = Utilities.getRowStart(jTextPane, offset);
                    int rowEnd = Utilities.getRowEnd(jTextPane, offset);
                    String selectedLine = jTextPane.getText().substring(rowStart, rowEnd);
                    System.out.println(selectedLine);

                    if (displayDataState == DisplayDataState.CLASSES_LIST) {
                        tabPanel.onSelectedClassNameFromMouseClick(selectedLine);
                    } else if (displayDataState == DisplayDataState.INSIDE_CLASS) {
                        if (selectedLine.contains("import")) {
                            tabPanel.onSelectedImportFromMouseClick(
                                    getClassNameFromImportStatement(selectedLine));
                        } else {

                            rowStart = Utilities.getWordStart(jTextPane, offset);
                            rowEnd = Utilities.getWordEnd(jTextPane, offset);
                            String word = jTextPane.getText().substring(rowStart, rowEnd);

                            tabPanel.onSelectedTypeClassfromMouseClick(word);
                            // TODO might not need the pop up
                            //popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

            public String getClassNameFromImportStatement(String selectedLine) {
                final String IMPORT = "import ";
                int start = selectedLine.indexOf(IMPORT) + IMPORT.length();
                String result = selectedLine.trim().substring(start,
                        selectedLine.indexOf(";"));
                return result;
            }
        });

        displaySharkey();
    }

    public void displayInfo() {
        displayDataState = DisplayDataState.INFO;

        clearText();
        style = jTextPane.addStyle("STYLE", null);
        Document doc = jTextPane.getStyledDocument();

        try {
            StyleConstants.setForeground(style, ColorScheme.FOREGROUND_CYAN);
            StyleConstants.setFontSize(style, 16);
            StyleConstants.setFontFamily(style, "Menlo");

            doc.insertString(doc.getLength(), InfoBG.INFO, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        jTextPane.setDocument(doc);
    }

    public Component onAddComponentToPane() {
        return this.jTextPane;
    }

    public void displayReducedClassesNames(List<String> classNamesToShow,
                                           String inputText) {
        displayDataState = DisplayDataState.CLASSES_LIST;

        clearText();

        int matchIndex;

        String beforeMatch = "";
        String match;
        String afterMatch = "";

        StyleConstants.setFontSize(style, 18);
        StyleConstants.setForeground(style, ColorScheme.FOREGROUND_CYAN);

        Document doc = jTextPane.getDocument();

        for (String className : classNamesToShow) {
            matchIndex = className.indexOf(inputText);

            if (matchIndex > -1) {
                beforeMatch = className.substring(0, matchIndex);
                match = className.substring(matchIndex, matchIndex + inputText.length());
                afterMatch = className.substring(matchIndex + inputText.length(),
                        className.length());
            } else {
                // we are here by camel match
                // i.e. 2-3 letters that fits
                // to class name
                match = className;
            }

            try {
                doc.insertString(doc.getLength(), beforeMatch, style);
                StyleConstants.setBackground(style, ColorScheme.SELECTION_BG);
                doc.insertString(doc.getLength(), match, style);
                StyleConstants.setBackground(style, ColorScheme.BACKGROUND);
                doc.insertString(doc.getLength(), afterMatch + "\n", style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        jTextPane.setDocument(doc);
    }

    public void displayAllClassesNames(List<String> classNames) {
        long start = System.currentTimeMillis();

        displayDataState = DisplayDataState.CLASSES_LIST;
        StyleConstants.setFontSize(style, 18);
        StyleConstants.setForeground(style, ColorScheme.FOREGROUND_CYAN);

        clearText();

        BatchDocument blank = new BatchDocument();
        jTextPane.setDocument(blank);

        for (String className : classNames) {
            blank.appendBatchStringNoLineFeed(className, style);
            blank.appendBatchLineFeed(style);
        }

        try {
            blank.processBatchUpdates(0);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        jTextPane.setDocument(blank);

        System.out.println("UI update " + (System.currentTimeMillis() - start) + " ms");
    }

    public void displayClass(String classString) {
        displayDataState = DisplayDataState.INSIDE_CLASS;
        try {
            String currentText =
                    jTextPane.getDocument().getText(0, jTextPane.getDocument().getLength());
            if (currentText.equals(getOneColorFormattedOutput(classString))) {
                return;
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        clearText();
        StyleConstants.setFontSize(style, 18);

        Document doc = new DefaultStyledDocument();

        try {
            doc.insertString(doc.getLength(), getOneColorFormattedOutput(classString), style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        jTextPane.setDocument(doc);
    }

    public void displayClass(List<Translator.ELEMENT> elements) {
        displayDataState = DisplayDataState.INSIDE_CLASS;
        clearText();
        StyleConstants.setFontSize(style, 18);

        Document doc = new DefaultStyledDocument();

        try {
            for (Translator.ELEMENT e : elements) {
                if (e.tag == Translator.TAG.MODIFIER) {
                    StyleConstants.setForeground(style, ColorScheme.FOREGROUND_ORANGE);
                } else if (e.tag == Translator.TAG.DOCUMENT) {
                    StyleConstants.setForeground(style, ColorScheme.FOREGROUND_YELLOW);
                } else if (e.tag == Translator.TAG.IDENTIFIER) {
                    StyleConstants.setForeground(style, ColorScheme.FOREGROUND_CYAN);
                } else if (e.tag == Translator.TAG.ANNOTATION) {
                    StyleConstants.setForeground(style, ColorScheme.FOREGROUND_YELLOW_ANNOTATIONS);
                }

                doc.insertString(doc.getLength(), e.text, style);

            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        StyleConstants.setForeground(style, ColorScheme.FOREGROUND_CYAN);

        jTextPane.setDocument(doc);
    }

    public void displaySharkey() {
        displayDataState = DisplayDataState.SHARKEY;
        clearText();
        style = jTextPane.addStyle("STYLE", null);
        Document doc = jTextPane.getStyledDocument();

        try {
            StyleConstants.setForeground(style, ColorScheme.FOREGROUND_YELLOW);
            StyleConstants.setFontSize(style, 13);
            StyleConstants.setFontFamily(style, "Menlo");

            doc.insertString(doc.getLength(), SharkBG.SHARKEY, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        jTextPane.setDocument(doc);
    }

    public void displayError() {
        displayDataState = DisplayDataState.ERROR;
        clearText();

        style = jTextPane.addStyle("STYLE", null);
        Document doc = jTextPane.getStyledDocument();

        try {
            StyleConstants.setForeground(style, ColorScheme.FOREGROUND_YELLOW);
            StyleConstants.setFontSize(style, 16);
            StyleConstants.setFontFamily(style, "Menlo");

            doc.insertString(doc.getLength(), "", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        jTextPane.setDocument(doc);
    }

    public boolean isDisplayingClassesList() {
        return displayDataState == DisplayDataState.CLASSES_LIST;
    }

    private JPopupMenu setupPopup() {
        Action[] textActions = { new DefaultEditorKit.CopyAction() };
        JPopupMenu result = new JPopupMenu();

        for (Action textAction : textActions) {
            JMenuItem jmi = new JMenuItem(textAction);
            jmi.setForeground(ColorScheme.FOREGROUND_YELLOW);
            jmi.setBackground(ColorScheme.BLACK);
            jmi.setFont(new Font("Menlo", Font.PLAIN, 14));
            result.add(jmi);
        }

        result.setForeground(ColorScheme.FOREGROUND_YELLOW);
        result.setBackground(ColorScheme.BLACK);

        return result;
    }

    private void clearText() {
        jTextPane.setText(null);
    }

    private static String getOneColorFormattedOutput(String data) {
        return data + "\n";
    }

    public static void main(String[] args) {
        DisplayArea da = new DisplayArea(null);

        Translator emitter = new Translator2Java(StringTokenizer.class);
        emitter.apply();

        da.displayClass(emitter.getElementsList());

        JFrame frame = new JFrame("Test");
        frame.getContentPane().add(da.onAddComponentToPane());
        frame.pack();
        frame.setVisible(true);
    }
}
