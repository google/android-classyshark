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

import com.google.classyshark.silverghost.methodscounter.ClassNode;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class RingChart {
    private static final int DEFAULT_MAX_DEPTH = 2;

    private int maxDepth;
    private Stroke lineStroke = new BasicStroke(3);
    private Stroke defaultStroke;

    public RingChart() {
        this(DEFAULT_MAX_DEPTH);
    }

    public RingChart(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void render(int width, int height, ClassNode rootNode, Graphics g) {
        int size = Math.min(width, height);
        Graphics2D g2d = (Graphics2D)g;
        defaultStroke = g2d.getStroke();
        renderNode(width, height, size, 0, 360, rootNode, g2d, 1);
    }

    private void renderNode(int width, int height, int radius, int startAngle, int endAngle,
                            ClassNode rootNode, Graphics2D g2d, int depth) {
        int nodeStartAngle;
        int nodeEndAngle = startAngle;
        int angleSize = endAngle - startAngle;
        int r = (radius / maxDepth) * depth;
        int x = (width - r) / 2;
        int y = (height - r) / 2;

        List<ClassNode> nodes = new ArrayList<>(rootNode.getChildNodes().values());
        Collections.sort(nodes, new Comparator<ClassNode>() {
            @Override
            public int compare(ClassNode o1, ClassNode o2) {
                return Integer.compare(o2.getMethodCount(), o1.getMethodCount());
            }
        });
        Iterator<ClassNode> it = nodes.iterator();
        while (it.hasNext()) {
            ClassNode node = it.next();
            nodeStartAngle = nodeEndAngle;

            if (it.hasNext()) {
                nodeEndAngle = (int) ((double) node.getMethodCount()
                        / rootNode.getMethodCount() * angleSize + nodeEndAngle);
            } else {
                nodeEndAngle = endAngle;
            }

            if (depth < maxDepth) {
                renderNode(width, height, radius, nodeStartAngle, nodeEndAngle, node, g2d, depth + 1);
            }
            
            g2d.setColor(new Color((int)(Math.random() * Integer.MAX_VALUE)));
            g2d.fillArc(x, y, r, r, nodeStartAngle, nodeEndAngle - nodeStartAngle);
            g2d.setColor(Color.BLACK);
            g2d.drawArc(x, y, r, r, nodeStartAngle, nodeEndAngle - nodeStartAngle);

            //Render Lines between angles
            AffineTransform saved = g2d.getTransform();
            int cx = width / 2;
            int cy = height / 2;
            g2d.translate(cx, cy);

            double rads = Math.toRadians(nodeEndAngle);
            int py = (int)Math.round(Math.sin(rads) * (r / 2)) * -1;
            int px = (int)Math.round(Math.cos(rads) * (r / 2));

            g2d.setStroke(lineStroke);
            g2d.drawLine(0, 0, px, py);
            g2d.setStroke(defaultStroke);

            //Render text
            int r2 = (radius / maxDepth) * (depth - 1);
            r2 = r + (r2 - r)/2;
            rads = Math.toRadians(nodeStartAngle + (nodeEndAngle - nodeStartAngle) / 2);
            py = (int)Math.round(Math.sin(rads) * (r2 / 2))* -1;
            px = (int)Math.round(Math.cos(rads) * (r2 / 2));
            g2d.drawString(node.getKey(), px, py);

            g2d.setTransform(saved);
        }
    }
}


