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

package com.google.classyshark.gui.panel.chart;

import com.google.classyshark.gui.panel.ColorScheme;
import com.google.classyshark.silverghost.methodscounter.ClassNode;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class RingChartPanel extends JPanel {
    private RingChart ringChart = new RingChart();
    private ClassNode rootNode;

    public RingChartPanel() {
        super();
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        ClassNode classNode = ringChart.getClassNodeAt(x, y);
        if (classNode == null) return null;
        return classNode.getKey() + ": " + classNode.getMethodCount();
    }

    public void setRootNode(ClassNode rootNode) {
        this.rootNode = rootNode;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        if (rootNode != null) {
            ringChart.render(getWidth(), getHeight(), rootNode, g);
        }
    }
}
