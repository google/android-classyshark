package com.google.classyshark.gui.panel.tree;

import com.google.classyshark.gui.GuiMode;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Color;
import java.awt.Component;

public class CellRenderer extends DefaultTreeCellRenderer{

    @Override
    public Color getBackgroundNonSelectionColor() {
        return (null);
    }

    @Override
    public Color getBackgroundSelectionColor() {
        return GuiMode.getTheme().getSelectionBgColor();
    }

    @Override
    public Color getBackground() {
        return (null);
    }

    @Override
    public Color getTextNonSelectionColor() {
        return GuiMode.getTheme().getDefaultColor();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        final Component component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        this.setText(value.toString());
        return component;
    }
}
