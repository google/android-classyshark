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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RingChart {
    private static final int MARGIN = 20;
    private static final int DEFAULT_MAX_DEPTH = 2;
    private Color OTHERS_COLOR = Color.GRAY;
    private static final Color[] PALETTE = new Color[]{
            new Color(0x5DA5DA),
            new Color(0xFAA43A),
            new Color(0x60BD68),
            new Color(0xF17CB0),
            new Color(0xB2912F),
            new Color(0xB276B2),
            new Color(0xDECF3F),
            new Color(0xF15854),
            new Color(0x4D4D4D)};
    private static final Color[][] L2_PALLETES = new Color[][] {
            {
                new Color(0x5dc1d9),
                new Color(0x5db8d9),
                new Color(0x5dafd9),
                new Color(0x5da5d9),
                new Color(0x5d9cd9),
                new Color(0x5d93d9),
                new Color(0x5d89d9)
            },
            {
                new Color(0xfa7839),
                new Color(0xfa8639),
                new Color(0xfa9539),
                new Color(0xfaa339),
                new Color(0xfab239),
                new Color(0xfac039),
                new Color(0xfacf39)
            },
            {
                new Color(0x6dbd60),
                new Color(0x66bd60),
                new Color(0x60bd61),
                new Color(0x60bd68),
                new Color(0x60bd6f),
                new Color(0x60bd76),
                new Color(0x60bd7d)
            },
            {
                new Color(0xf27ccc),
                new Color(0xf27cc3),
                new Color(0xf27cba),
                new Color(0xf27cb1),
                new Color(0xf27ca8),
                new Color(0xf27c9f),
                new Color(0xf27c96)
            },
            {
                new Color(0xb3742e),
                new Color(0xb37e2e),
                new Color(0xb3882e),
                new Color(0xb3912e),
                new Color(0xb39b2e),
                new Color(0xb3a52e),
                new Color(0xb3af2e)
            },
            {
                new Color(0xa576b3),
                new Color(0xa976b3),
                new Color(0xae76b3),
                new Color(0xb376b3),
                new Color(0xb376ae),
                new Color(0xb376a9),
                new Color(0xb376a5)
            },
            {
                new Color(0xdeaa3e),
                new Color(0xdeb63e),
                new Color(0xdec23e),
                new Color(0xdece3e),
                new Color(0xdeda3e),
                new Color(0xd6de3e),
                new Color(0xcade3e)
            },
            {
                new Color(0xf25573),
                new Color(0xf25567),
                new Color(0xf2555b),
                new Color(0xf25a55),
                new Color(0xf26655),
                new Color(0xf27255),
                new Color(0xf27d55)
            },
            {
                new Color(0x5C5C5C),
                new Color(0x666666),
                new Color(0x707070),
                new Color(0x7A7A7A),
                new Color(0x858585),
                new Color(0x8F8F8F),
                new Color(0x999999)
            }
    };

    private int maxDepth;
    private Map<Integer, ClassNode> colorClassNodeMap = new HashMap<>();
    private BufferedImage image;
    private ClassNode selectedNode;

    public RingChart() {
        this(DEFAULT_MAX_DEPTH);
    }

    public RingChart(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setSelectedNode(ClassNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public ClassNode getSelectedNode() {
        return selectedNode;
    }

    public void render(int width, int height, ClassNode rootNode, Graphics g) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D imageG2d = (Graphics2D)image.getGraphics();
        imageG2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        imageG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        imageG2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        imageG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        imageG2d.setComposite(AlphaComposite.Src);
        imageG2d.fillRect(0, 0, width, height);

        int graphWidth = width - MARGIN * 2;
        int graphHeight = height - MARGIN * 2;
        imageG2d.translate(MARGIN, MARGIN);
        int size = Math.min(graphWidth, graphHeight);
        renderNode(graphWidth, graphHeight, size, 0, 360, rootNode, imageG2d, 1, PALETTE);
        imageG2d.dispose();

        g.drawImage(image, 0, 0, null);
        g.dispose();
    }

    public ClassNode getClassNodeAt(int x, int y) {
        if (image == null) {
            return null;
        }
        int color = image.getRGB(x,y);
        return colorClassNodeMap.get(color);
    }

    private void renderNode(int width, int height, int radius, int startAngle, int endAngle,
                            ClassNode rootNode, Graphics2D g2d, int depth, Color[] pallete) {
        if (rootNode.getChildNodes().isEmpty()) {
            return;
        }

        int nodeStartAngle;
        int nodeEndAngle = startAngle;
        int angleSize = endAngle - startAngle;
        int r = (radius / maxDepth) * depth;
        int x = (width - r) / 2;
        int y = (height - r) / 2;
        int currentNode = 0;
        int currentColor = 0;

        List<ClassNode> nodes = new ArrayList<>(rootNode.getChildNodes().values());
        Collections.sort(nodes, new Comparator<ClassNode>() {
            @Override
            public int compare(ClassNode o1, ClassNode o2) {
                return Integer.compare(o2.getMethodCount(), o1.getMethodCount());
            }
        });

        while (nodeEndAngle < endAngle) {
            ClassNode node = nodes.get(currentNode);
            nodeStartAngle = nodeEndAngle;
            String title = node.getKey();
            Color color = pallete[currentColor];

            nodeEndAngle = (int) ((double) node.getMethodCount()
                    / rootNode.getMethodCount() * angleSize + nodeEndAngle);

            if (currentNode == nodes.size() - 1) {
                nodeEndAngle = endAngle;
            } else if (currentColor == pallete.length - 1 || 360 - nodeEndAngle < 5) {
                currentColor = pallete.length - 1;
                nodeEndAngle = endAngle;
                title = "Others";
                color = OTHERS_COLOR;
            }

            if (selectedNode != null && node == selectedNode) {
                color = getHighlightColor(color);
            }

            if (color != OTHERS_COLOR) {
                colorClassNodeMap.put(color.getRGB(), node);
            }

            if (depth < maxDepth && currentColor != pallete.length - 1) {
                Color[] newpallete = L2_PALLETES[currentColor];
                renderNode(
                        width, height, radius, nodeStartAngle, nodeEndAngle, node, g2d, depth + 1, newpallete);
            }

            g2d.setColor(color);

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

            g2d.drawLine(0, 0, px, py);

            //Render text
            int r2 = (radius / maxDepth) * (depth - 1);
            r2 = r + (r2 - r)/2;
            rads = Math.toRadians(nodeStartAngle + (nodeEndAngle - nodeStartAngle) / 2);
            py = (int)Math.round(Math.sin(rads) * (r2 / 2))* -1;
            px = (int)Math.round(Math.cos(rads) * (r2 / 2));
            g2d.drawString(title, px, py);

            g2d.setTransform(saved);

            currentNode++;
            currentColor++;
        }
    }

    private Color getHighlightColor(Color color) {
        float hsbVals[] = Color.RGBtoHSB(
                color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsbVals[0], hsbVals[1] * 0.7f, hsbVals[2]);
    }
}


