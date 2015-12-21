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

package com.google.classyshark.ui.panel.methodscount;

import com.google.classyshark.treewalker.methodsperpackage.Analyzer;
import com.google.classyshark.treewalker.methodsperpackage.Node;
import com.google.classyshark.ui.panel.ColorScheme;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.File;

public class MethodsCountPanel extends JPanel {
    private DefaultTreeModel treeModel;
    private JTree jTree;

    public MethodsCountPanel(File file) throws HeadlessException {
        this();
        loadFile(file);
    }

    public MethodsCountPanel() throws HeadlessException {
        setup();
    }

    public void loadFile(File file) {
        new NodeWorker(file).execute();
    }

    private void setup() {
        this.setLayout(new BorderLayout());
        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode(null));
        jTree = new JTree(treeModel);
        jTree.setBackground(ColorScheme.BACKGROUND);
        jTree.setRootVisible(false);

        DefaultTreeCellRenderer cellRenderer = (DefaultTreeCellRenderer) jTree.getCellRenderer();
        cellRenderer.setBackground(ColorScheme.BACKGROUND);
        cellRenderer.setBackgroundNonSelectionColor(ColorScheme.BACKGROUND);
        cellRenderer.setTextNonSelectionColor(ColorScheme.FOREGROUND_CYAN);
        cellRenderer.setFont(new Font("Menlo", Font.PLAIN, 18));
        jTree.setCellRenderer(cellRenderer);

        JScrollPane jScrollPane = new JScrollPane(jTree);
        this.setBorder(new EmptyBorder(0,0,0,0));
        this.add(jScrollPane, BorderLayout.CENTER);
    }

    private void addNodes(Node parent, DefaultMutableTreeNode jTreeParent) {
        for (Node n: parent.getChildNodes().values()) {
            DefaultMutableTreeNode newJTreeNode = new DefaultMutableTreeNode(n);
            jTreeParent.add(newJTreeNode);
            addNodes(n, newJTreeNode);
        }
    }

    private DefaultMutableTreeNode createDefaultMutableTreeNode(Node rootNode) {
        DefaultMutableTreeNode jTreeRootNode = new DefaultMutableTreeNode(rootNode);
        addNodes(rootNode, jTreeRootNode);
        return jTreeRootNode;
    }

    class NodeWorker extends SwingWorker<Node, Void> {
        private File file;

        public NodeWorker(File file) {
            this.file = file;
        }

        @Override
        protected Node doInBackground() throws Exception {
            Analyzer analyzer = new Analyzer();
            return analyzer.analyze(file);
        }

        @Override
        protected void done() {
            try {
                TreeNode root = createDefaultMutableTreeNode(get());
                treeModel.setRoot(root);
                jTree.setRootVisible(true);
            } catch (Exception ex) {

            }
        }
    }

    public static void main(String[] args) {
        String userHome = System.getProperty("user.home");
        String fileName = userHome + "/Downloads/test.apk";

        MethodsCountPanel panel = new MethodsCountPanel(new File(fileName));
        JFrame jFrame = new JFrame(fileName);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.getContentPane().add(panel);
        jFrame.setPreferredSize(new Dimension(800, 600));
        jFrame.setVisible(true);
    }
}
