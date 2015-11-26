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

package com.google.classyshark.ui.panel.tree;

import com.google.classyshark.reducer.Reducer;
import com.google.classyshark.ui.panel.ColorScheme;
import com.google.classyshark.ui.panel.ClassySharkPanel;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

public class FilesTree {
    private final ClassySharkPanel viewerPanel;
    private DefaultTreeModel treeModel = null;
    private JTree jTree = null;

    public FilesTree(ClassySharkPanel viewerPanel) {
        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        jTree = new JTree(treeModel);
        configureJTree(jTree);
        this.viewerPanel = viewerPanel;
    }

    public void fillArchive(File loadedFile, List<String> displayedClassNames) {
        if(!loadedFile.getName().contains(".")) {
            TreeNode rootNode = createEmptyJTreeModelClass();
            treeModel.setRoot(rootNode);
            return;
        }

        TreeNode rootNode;
        if (loadedFile.getName().endsWith("dex") ||
                loadedFile.getName().endsWith("apk")) {
            rootNode = createJTreeModelAndroid(loadedFile.getName(), displayedClassNames);
        } else {
            rootNode = createJTreeModelClass(loadedFile.getName(), displayedClassNames);
        }

        treeModel.setRoot(rootNode);
    }

    private TreeNode createJTreeModelAndroid(String fileName, List<String> displayedClassNames) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(fileName);
        DefaultMutableTreeNode classes = new DefaultMutableTreeNode("classes");
        DefaultMutableTreeNode currentClassesDex = classes;
        List<DefaultMutableTreeNode> noPkgNodes = new ArrayList<>();

        String lastPackage = null;
        DefaultMutableTreeNode packageNode = null;
        for (int i = 0; i < displayedClassNames.size(); i++) {
            String resName = displayedClassNames.get(i);
            if (resName.equals("AndroidManifest.xml")) {
                root.add(new DefaultMutableTreeNode(resName));
            } else if (resName.endsWith(".dex")) {
                currentClassesDex = new DefaultMutableTreeNode(resName);
                classes.add(currentClassesDex);
            } else {
                if (resName.lastIndexOf('.') >= 0) {
                    String pkg = resName.substring(0, resName.lastIndexOf('.'));
                    if (lastPackage == null || !pkg.equals(lastPackage)) {
                        if (packageNode != null) {
                            currentClassesDex.add(packageNode);
                        }
                        lastPackage = pkg;
                        packageNode = new DefaultMutableTreeNode(pkg);
                    }
                    packageNode.add(new DefaultMutableTreeNode(resName));
                } else {
                    noPkgNodes.add(new DefaultMutableTreeNode(resName));
                }
            }
        }
        for (DefaultMutableTreeNode node : noPkgNodes) {
            currentClassesDex.add(node);
        }
        root.add(classes);
        return root;
    }

    private TreeNode createJTreeModelClass(String fileName, List<String> displayedClassNames) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(fileName);
        DefaultMutableTreeNode classes = new DefaultMutableTreeNode("classes");
        String lastPackage = null;
        DefaultMutableTreeNode packageNode = null;

        for (int i = 0; i < displayedClassNames.size(); i++) {
            String fullClassName = displayedClassNames.get(i);
            String pkg = fullClassName.substring(0, fullClassName.lastIndexOf('.'));

            if (pkg.lastIndexOf('.') > 0) {
                pkg = pkg.substring(0, pkg.lastIndexOf('.'));
            }

            if (lastPackage == null || !pkg.equals(lastPackage)) {
                lastPackage = pkg;
                packageNode = new DefaultMutableTreeNode(pkg);
                classes.add(packageNode);
            }
            packageNode.add(new DefaultMutableTreeNode(fullClassName));

        }
        root.add(classes);
        return root;
    }

    private TreeNode createEmptyJTreeModelClass() {
        return new DefaultMutableTreeNode("error loading archive");
    }

    public Component getJTree() {
        return jTree;
    }

    private void configureJTree(final JTree jTree) {
        jTree.setRootVisible(false);
        jTree.setBackground(ColorScheme.BACKGROUND);
        DefaultTreeCellRenderer cellRenderer = (DefaultTreeCellRenderer) jTree.getCellRenderer();
        cellRenderer.setBackground(ColorScheme.BACKGROUND);
        cellRenderer.setBackgroundNonSelectionColor(ColorScheme.BACKGROUND);
        cellRenderer.setTextNonSelectionColor(ColorScheme.FOREGROUND_CYAN);
        cellRenderer.setFont(new Font("Menlo", Font.PLAIN, 18));
        jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                Object selection = jTree.getLastSelectedPathComponent();
                if (selection == null) return;

                DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) selection;

                if (selection.toString().endsWith(".dex")) {
                    FilesTree.this.viewerPanel.onSelectedClassName(
                            (String) defaultMutableTreeNode.getUserObject());
                    return;
                }

                if (!defaultMutableTreeNode.isLeaf()) return;

                if (FilesTree.this.viewerPanel != null) {
                    FilesTree.this.viewerPanel.onSelectedClassName(
                            (String) defaultMutableTreeNode.getUserObject());
                }
            }
        });
    }

    public void setVisibleRoot() {
        jTree.setRootVisible(true);
    }

    public static void main(String[] args) {
        File test = new File(System.getProperty("user.home") +
                "/Desktop/Scenarios/2 Samples/android.jar");

        FilesTree filesTree = new FilesTree(null);

        Reducer reducer = new Reducer(test);
        reducer.reduce("");
        filesTree.fillArchive(test, reducer.getAllClassesNames());

        JFrame frame = new JFrame("Test");
        JScrollPane scrolledTree = new JScrollPane(filesTree.getJTree());
        frame.getContentPane().add(scrolledTree);
        frame.pack();
        frame.setVisible(true);
    }
}
