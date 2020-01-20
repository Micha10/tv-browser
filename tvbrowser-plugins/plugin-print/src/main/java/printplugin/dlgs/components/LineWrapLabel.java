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
import javax.swing.UIManager;

public class LineWrapLabel extends JPanel {

  private static final long serialVersionUID = -5181467312906987989L;

  private AttributedString attributedString;

  @SuppressWarnings("nls")
  public LineWrapLabel(final String text) {
    super();
    setFont(UIManager.getLookAndFeelDefaults().getFont("Label.font"));
    setForeground(UIManager.getLookAndFeelDefaults().getColor("Label.foreground"));
    attributedString = new AttributedString(text);
  }

  @Override
  public void paintComponent(final Graphics g) {

    super.paintComponent(g);

    final Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    final AttributedCharacterIterator attributedCharacterIterator = attributedString.getIterator();
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

  @Override
  public Dimension getPreferredSize() {
    float x = 0f;
    float y = 0f;
    final AttributedCharacterIterator attributedCharacterIterator = attributedString.getIterator();
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