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

package com.google.classyshark.ui.panel.displayarea;

import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.java.JavaTranslator;
import com.google.classyshark.ui.panel.ColorScheme;
import com.google.classyshark.ui.panel.ViewerController;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.Utilities;

/**
 * the area to display lists of classes and individual class
 */
public class DisplayArea {

    private enum DisplayDataState {
        SHARKEY, CLASSES_LIST, INSIDE_CLASS, ERROR
    }

    private final JTextPane jTextPane;
    private Style style;

    private DisplayDataState displayDataState;

    public DisplayArea(final ViewerController viewerController) {
        jTextPane = new JTextPane();
        jTextPane.setEditable(false);
        jTextPane.setBackground(ColorScheme.BACKGROUND);

        jTextPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (displayDataState == DisplayDataState.SHARKEY) {
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

                    if (displayDataState == DisplayDataState.CLASSES_LIST || selectedLine.endsWith(".dex")) {
                        viewerController.onSelectedClassName(selectedLine);
                    } else if (displayDataState == DisplayDataState.INSIDE_CLASS) {
                        if (selectedLine.contains("import")) {
                            viewerController.onSelectedImportFromMouseClick(
                                    getClassNameFromImportStatement(selectedLine));
                        } else {
                            rowStart = Utilities.getWordStart(jTextPane, offset);
                            rowEnd = Utilities.getWordEnd(jTextPane, offset);
                            String word = jTextPane.getText().substring(rowStart, rowEnd);
                            viewerController.onSelectedTypeClassFromMouseClick(word);
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

        jTextPane.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                int ctrlModifier = toolkit.getMenuShortcutKeyMask();

                //check if the modifier: ctrl for linux, windows or command for mac is pressed
                // with the'c' key
                if (e.getKeyChar() == 'c' &&
                        (e.getModifiers() & ctrlModifier) == ctrlModifier) {

                    String copyText = jTextPane.getSelectedText();

                    //if there is no selection, copy the entire text
                    if (copyText == null) {
                        copyText = jTextPane.getText();
                    }

                    //Add the text to the clipboard
                    toolkit.getSystemClipboard().setContents(new StringSelection(copyText), null);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        displaySharkey();
    }

    public Component onAddComponentToPane() {
        return this.jTextPane;
    }

    public void displayReducedClassNames(List<String> classNamesToShow,
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
                switch (e.tag) {
                    case MODIFIER:
                        StyleConstants.setForeground(style, ColorScheme.FOREGROUND_ORANGE);
                        break;
                    case DOCUMENT:
                        StyleConstants.setForeground(style, ColorScheme.FOREGROUND_YELLOW);
                        break;
                    case IDENTIFIER:
                        StyleConstants.setForeground(style, ColorScheme.FOREGROUND_CYAN);
                        break;
                    case ANNOTATION:
                        StyleConstants.setForeground(style, ColorScheme.FOREGROUND_YELLOW_ANNOTATIONS);
                        break;
                    case XML_TAG:
                        StyleConstants.setForeground(style, ColorScheme.FOREGROUND_YELLOW);
                        break;
                    case XML_ATTR_NAME:
                        StyleConstants.setForeground(style, ColorScheme.FOREGROUND_CYAN);
                        break;
                    case XML_ATTR_VALUE:
                        StyleConstants.setForeground(style, ColorScheme.FOREGROUND_YELLOW_ANNOTATIONS);
                        break;
                    default:
                        StyleConstants.setForeground(style, Color.LIGHT_GRAY);
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
            StyleConstants.setFontSize(style, 13);
            StyleConstants.setFontFamily(style, "Menlo");

            doc.insertString(doc.getLength(), "\n\n\n\t\t\t There was a problem loading the class  ", style);
            doc.insertString(doc.getLength(), SharkBG.SHARKEY, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        jTextPane.setDocument(doc);
    }

    private void clearText() {
        jTextPane.setText(null);
    }

    private static String getOneColorFormattedOutput(String data) {
        return data + "\n";
    }

    public static void main(String[] args) {
        DisplayArea da = new DisplayArea(null);

        Translator emitter = new JavaTranslator(StringTokenizer.class);
        emitter.apply();

        da.displayClass(emitter.getElementsList());

        JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(da.onAddComponentToPane());
        frame.pack();
        frame.setVisible(true);
    }
}
