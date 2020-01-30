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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JTextArea;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.text.DefaultCaret;

/**
 * A simple label to display line wrapped text. It uses the font and
 * foreground color of the {@link UIDefaults} for the current
 * {@link LookAndFeel}.
 *
 * @author tgiesecke
 * @see JTextArea
 * @since 3.0.2.6 beta
 */
@SuppressWarnings("nls")
public class LineWrapLabel extends JTextArea {

  private static final long serialVersionUID = -5181467312906987989L;

  /**
   * Creates a new instance.
   */
  public LineWrapLabel() {
    super();
    init();
  }

  /**
   * Creates a new instance with the given text.
   *
   * @param text
   *               the text to display
   */
  public LineWrapLabel(final String text) {
    super(text);
    init();
  }

  private void init() {
    setBackground(null);
    setCaret(new DefaultCaret() {

      private static final long serialVersionUID = -8708022951336324026L;

      @Override
      protected void adjustVisibility(final Rectangle rectangle) {}
    });
    setEditable(false);
    setFocusable(false);
    final Font font = getFont();
    if (font == null || font instanceof UIResource) {
      setFont(UIManager.getFont("Label.font"));
    }
    final Color foregroundColor = getForeground();
    if (foregroundColor == null || foregroundColor instanceof UIResource) {
      setForeground(UIManager.getColor("Label.foreground"));
    }
    setLineWrap(true);
    setOpaque(false);
    setRequestFocusEnabled(false);
    setWrapStyleWord(true);
  }

  @Override
  public void updateUI() {
    super.updateUI();
    LookAndFeel.installBorder(this, "Label.border");
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }
}