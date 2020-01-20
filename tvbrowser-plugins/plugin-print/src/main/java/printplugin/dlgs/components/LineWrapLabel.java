/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package printplugin.dlgs.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * A simple label to display unformatted text. It uses the font and
 * foreground color of the {@link UIDefaults} for the current
 * {@link LookAndFeel}. Antialiasing is applied.
 *
 * The {@link JPanel} based component uses a {@link LineBreakMeasurer}
 * to wrap text to fit into the width of the parent container.
 *
 * @author tgiesecke
 * @since 3.0.2.5 beta
 */
@SuppressWarnings("nls")
public class LineWrapLabel extends JPanel {

  private static final long serialVersionUID = -5181467312906987989L;

  private final AttributedString mAttributedString;

  /**
   * Creates a new instance with the given text.
   *
   * @param text
   *               the text to display
   */
  public LineWrapLabel(final String text) {
    super();
    setFont(UIManager.getLookAndFeelDefaults().getFont("Label.font"));
    setForeground(UIManager.getLookAndFeelDefaults().getColor("Label.foreground"));
    mAttributedString = new AttributedString(text);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void paintComponent(final Graphics g) {

    super.paintComponent(g);

    final Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    final AttributedCharacterIterator attributedCharacterIterator = mAttributedString.getIterator();
    final int beginIndex = attributedCharacterIterator.getBeginIndex();
    final int endIndex = attributedCharacterIterator.getEndIndex();
    final LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(attributedCharacterIterator,
        g2d.getFontRenderContext());
    final float breakWidth = getSize().width;

    float drawPosY = 0;
    lineMeasurer.setPosition(beginIndex);
    while (lineMeasurer.getPosition() < endIndex) {
      final TextLayout layout = lineMeasurer.nextLayout(breakWidth);
      final float drawPosX = layout.isLeftToRight() ? 0 : breakWidth - layout.getAdvance();
      drawPosY += layout.getAscent();
      layout.draw(g2d, drawPosX, drawPosY);
      drawPosY += layout.getDescent() + layout.getLeading();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Dimension getPreferredSize() {
    float x = 0f;
    float y = 0f;
    final AttributedCharacterIterator attributedCharacterIterator = mAttributedString.getIterator();
    final int beginIndex = attributedCharacterIterator.getBeginIndex();
    final int endIndex = attributedCharacterIterator.getEndIndex();
    final LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(attributedCharacterIterator,
        new FontRenderContext(getFont().getTransform(), true, true));
    lineMeasurer.setPosition(beginIndex);
    while (lineMeasurer.getPosition() < endIndex) {
      final TextLayout layout = lineMeasurer.nextLayout(getWidth());
      x = Math.max(x, layout.getVisibleAdvance());
      y += layout.getAscent() + layout.getDescent() + layout.getLeading();
    }
    return new Dimension((int) x, (int) y);
  }

  @Override
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }
}