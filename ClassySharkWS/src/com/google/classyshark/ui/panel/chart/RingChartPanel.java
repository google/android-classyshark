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

package com.google.classyshark.ui.panel.chart;

import com.google.classyshark.gui.panel.ColorScheme;
import com.google.classyshark.silverghost.methodscounter.ClassNode;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;

public class RingChartPanel extends JPanel {
    private RingChart ringChart = new RingChart();
    private ClassNode rootNode;

    public void setRootNode(ClassNode rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(ColorScheme.BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());
        int color = 20;
        if (rootNode != null) {
            color = color + 20;
            g.setColor(new Color(color));
            ringChart.render(getWidth(), getHeight(), rootNode, g);
        }
    }
}
