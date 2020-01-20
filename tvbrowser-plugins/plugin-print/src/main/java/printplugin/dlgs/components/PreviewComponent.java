/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
 * Copyright (c) 2020 Thorsten Giesekce (tvbrowser@giesecke.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package printplugin.dlgs.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JComponent;

/**
 * A {@link JComponent} that displays a preview of a given {@link Printable}
 * object. The component supports scrolling, zooming, and the selection of
 * specific pages of the printable object.
 *
 * Mouse wheel support, page centering, text antialias, and dynamic crop mark
 * support is available since version 3.0.2.5 beta (r9077 2020-01-20).
 *
 * @author bananeweizen
 * @author tgiesecke
 * @since 2010-06-28 19:33:48
 */
public class PreviewComponent extends JComponent {

  private static final long serialVersionUID = -1939987141382335672L;

  private static final double CROP_MARK_LENGTH = 14.17322835; // 0,5cm (2,54 cm = 72 dpi)

  private static final double MAX_ZOOM = 6D;
  private static final double MIN_ZOOM = .5D;
  private static final double DEFAULT_ZOOM = .7D;
  private static final double ZOOM_STEP = .1D;

  private final Dimension mInitialSize;
  private final Printable mPrintable;
  private final PageFormat mPageFormat;
  private final int mNumberOfPages;

  private int mPageIndex;

  private double mZoom = DEFAULT_ZOOM;
  private double mPreviousZoom = mZoom;

  private double mScrollX = 0D;
  private double mScrollY = 0D;

  private boolean mMouseOver;
  private boolean mAntialias = true;

  private final MouseAdapter mMouseAdapter = new MouseAdapter() {

    private Point mMouseDragPoint;

    @Override
    public void mousePressed(final MouseEvent evt) {
      setCursor(new Cursor(Cursor.HAND_CURSOR));
      mMouseDragPoint = evt.getPoint();
    }

    @Override
    public void mouseReleased(final MouseEvent evt) {
      mMouseDragPoint = null;
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
      mMouseOver = true;
      repaint(getVisibleRect());
    }

    @Override
    public void mouseExited(final MouseEvent e) {
      mMouseOver = false;
      repaint(getVisibleRect());
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
      if (mMouseDragPoint != null) {
        final int deltaX = mMouseDragPoint.x - e.getX();
        final int deltaY = mMouseDragPoint.y - e.getY();
        final Rectangle view = getVisibleRect();
        view.x += deltaX;
        view.y += deltaY;
        scrollRectToVisible(view);
      }
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
      final double zoomFactor = -ZOOM_STEP * e.getPreciseWheelRotation() * mZoom;
      mZoom = Math.abs(mZoom + zoomFactor);
      updateSize();
      centerRelativeToMouse(e.getPoint());
      mPreviousZoom = mZoom;
    }
  };

  /**
   * Creates a new instance using the given {@link Printable}.
   *
   * @param printable
   *                        the {@link Printable} instance that delivers the data
   * @param pageFormat
   *                        the {@link PageFormat} instance that holds the paper
   *                        boundaries and margins
   * @param numberOfPages
   *                        the number of pages that are available
   * @see Printable#print(Graphics, PageFormat, int)
   */
  public PreviewComponent(final Printable printable, final PageFormat pageFormat, final int numberOfPages) {

    // mAntialias = PrintPlugin.getInstance().getSettings().isAntialias();
    mInitialSize = new Dimension((int) pageFormat.getWidth(), (int) pageFormat.getHeight());
    mPageFormat = pageFormat;
    mPrintable = printable;
    mNumberOfPages = numberOfPages;

    setAutoscrolls(true);
    updateSize();
    centerRelativeToMouse(null);

    addMouseListener(mMouseAdapter);
    addMouseMotionListener(mMouseAdapter);
    addMouseWheelListener(mMouseAdapter);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Dimension getMinimumSize() {
    return new Dimension(
        (int) (mInitialSize.width * MIN_ZOOM),
        (int) (mInitialSize.height * MIN_ZOOM));
  }

  /**
   * Validates the zoom boundaries, and computes and applies
   * the preferred size of the component (multiplies the
   * initial size with the current zoom factor).
   */
  public void updateSize() {
    if (mZoom < MIN_ZOOM) {
      mZoom = MIN_ZOOM;
    } else if (mZoom > MAX_ZOOM) {
      mZoom = MAX_ZOOM;
    }
    final Dimension dimension = new Dimension(
        (int) (mInitialSize.width * mZoom),
        (int) (mInitialSize.height * mZoom));
    setPreferredSize(dimension);
    setSize(dimension);
  }

  /**
   * Centers the component relative to a given point or its center point with
   * inside the visible rectangle.
   *
   * The zoom level is taken into account.
   *
   * @param point
   *                the reference point for centering the component relatively
   *                (e. g. zoom in)
   */
  private void centerRelativeToMouse(final Point2D point) {
    final Rectangle visibleRect = getVisibleRect();
    if (point != null) {
      mScrollX = point.getX() / mPreviousZoom * mZoom - (point.getX() - visibleRect.getX());
      mScrollY = point.getY() / mPreviousZoom * mZoom - (point.getY() - visibleRect.getY());
    } else {
      final Rectangle size = getBounds();
      mScrollX = size.getCenterX();
      mScrollY = size.getCenterY();
    }
    visibleRect.setRect(mScrollX, mScrollY, visibleRect.getWidth(), visibleRect.getHeight());
    scrollRectToVisible(visibleRect);
  }

  /**
   * Sets the zoom factor to the given value in a defined range. If the value is
   * outside the range, the value is corrected to the minimum or maximum zoom
   * level.
   *
   * @param zoom
   *               the zoom factor (negative values zoom out, positive values zoom
   *               in)
   */
  public void setZoom(final double zoom) {
    mZoom = zoom;
    updateSize();
    revalidate();
    repaint(getVisibleRect());
  }

  /**
   * Returns the initial size of the component.
   *
   * @return initial size of the component
   */
  public Dimension getInitialSize() {
    return mInitialSize;
  }

  /**
   * Returns the current zoom factor.
   *
   * @return current zoom factor
   */
  public double getZoom() {
    return mZoom;
  }

  /**
   * Returns <code>true</code> if the current zoom factor is
   * below the minimum zoom factor.
   *
   * @return <code>true</code> if the current zoom factor is
   *           below the minimum zoom factor
   */
  public boolean minZoom() {
    return mZoom <= MIN_ZOOM;
  }

  /**
   * Returns <code>true</code> if the current zoom factor is
   * above the maximum zoom factor.
   *
   * @return <code>true</code> if the current zoom factor is
   *           above the maximum zoom factor
   */
  public boolean maxZoom() {
    return mZoom >= MAX_ZOOM;
  }

  /**
   * Zooms in programmatically with a fixed step width.
   *
   * @see PreviewComponent#setZoom(double)
   */
  public void zoomIn() {
    if (mZoom < MAX_ZOOM) {
      mZoom += ZOOM_STEP;
      setZoom(mZoom);
    }
  }

  /**
   * Zooms out programmatically with a fixed step width.
   *
   * @see PreviewComponent#setZoom(double)
   */
  public void zoomOut() {
    if (mZoom > MIN_ZOOM) {
      mZoom -= ZOOM_STEP;
      setZoom(mZoom);
    }
  }

  /**
   * Selects the next page index of the given printable object
   * and repaints the visible part of the component.
   *
   * Will do nothing if the index does not exist.
   */
  public void next() {
    if (mPageIndex < mNumberOfPages - 1) {
      mPageIndex++;
      repaint(getVisibleRect());
    }
  }

  /**
   * Selects the previous page index of the given printable object
   * and repaints the visible part of the component.
   *
   * Will do nothing if the index does not exist.
   */
  public void previous() {
    if (mPageIndex > 0) {
      mPageIndex--;
      repaint(getVisibleRect());
    }
  }

  /**
   * Returns the page count of the given printable object.
   *
   * @return page count of the printable object
   */
  public int getNumberOfPages() {
    return mNumberOfPages;
  }

  /**
   * Returns the current selected index of the printable object.
   *
   * @return selected index of the printable object
   */
  public int getPageIndex() {
    return mPageIndex;
  }

  /**
   * Sets the zero-based index of the page to display and repaints the component.
   * If the index is different from the previous index and if the index is in a
   * valid range,
   * the visible rectangle of the component will be repainted.
   *
   * @param pageIndex
   *                    the index of the page to display
   */
  public void setPageIndex(final int pageIndex) {
    if (pageIndex != mPageIndex && pageIndex >= 0 && pageIndex < mNumberOfPages) {
      mPageIndex = pageIndex;
      repaint(getVisibleRect());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void paintComponent(final Graphics graphics) {
    try {
      super.paintComponent(graphics);

      final Graphics2D g = (Graphics2D) graphics.create();
      g.scale(mZoom, mZoom);

      // Center within the parent view
      final Rectangle bounds = getBounds();
      double tx = (bounds.getWidth() - mPageFormat.getWidth() * mZoom) / 2
          / mZoom;
      double ty = (bounds.getHeight() - mPageFormat.getHeight() * mZoom) / 2
          / mZoom;
      g.translate(tx, ty);

      g.setColor(Color.white);
      g.fillRect(0, 0, (int) mPageFormat.getWidth(), (int) mPageFormat.getHeight());

      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          mAntialias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

      if (mMouseOver) {
        g.setColor(Color.lightGray);
        drawCropMarks(g, mPageFormat);
        // g.setColor(Color.magenta.brighter());
        // g.fillRect(0, 0, (int) mPageFormat.getWidth(), (int)
        // mPageFormat.getHeight());
        // g.setColor(Color.white);
        // final PageFormat minPageFormat = getMinimumMarginPageFormat();
        // g.fillRect((int) minPageFormat.getImageableX(), (int)
        // minPageFormat.getImageableY(),
        // (int) minPageFormat.getImageableWidth(), (int)
        // minPageFormat.getImageableHeight());
        // g.fillRect(0, 0, (int) mPageFormat.getWidth(), (int)
        // mPageFormat.getHeight());
        // g.setColor(Color.lightGray);
        // g.drawRect((int) mPageFormat.getImageableX(), (int)
        // mPageFormat.getImageableY(), (int) mPageFormat.getImageableWidth(), (int)
        // mPageFormat.getImageableHeight());
        // @SuppressWarnings("nls")
        // final String string = String.format(getLocale(), "%d x %d mm",
        // Math.round(mm(mPageFormat.getWidth())),
        // Math.round(mm(mPageFormat.getHeight())));
        // final AttributedString attributedString = new AttributedString(string);
        // attributedString.addAttribute(TextAttribute.BACKGROUND, Color.black);
        // attributedString.addAttribute(TextAttribute.FOREGROUND, Color.white);
        // final TextLayout textLayout = new TextLayout(attributedString.getIterator(),
        // g.getFontRenderContext());
        // textLayout.draw(g, 0, (int) textLayout.getBounds().getHeight());
        // final Rectangle bounds = textLayout.getPixelBounds(g.getFontRenderContext(),
        // 15, 15);
      }

      g.setColor(Color.black);
      try {
        mPrintable.print(g, mPageFormat, mPageIndex);
      } catch (final PrinterException e) {
        e.printStackTrace();
      }
      g.dispose();
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   * Draws crop marks in the boundaries of the imageable area for the given page
   * format.
   */
  private static void drawCropMarks(final Graphics2D graphics2d, final PageFormat pageFormat) {
    final double imageableWidth = pageFormat.getImageableWidth();
    final double imageableHeight = pageFormat.getImageableHeight();
    graphics2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
    graphics2d.draw(new Line2D.Double(0, 0, 0, CROP_MARK_LENGTH));
    graphics2d.draw(new Line2D.Double(0, 0, CROP_MARK_LENGTH, 0));
    graphics2d.draw(new Line2D.Double(imageableWidth, 0, imageableWidth, CROP_MARK_LENGTH));
    graphics2d.draw(new Line2D.Double(imageableWidth, 0, imageableWidth - CROP_MARK_LENGTH, 0));
    graphics2d.draw(new Line2D.Double(0, imageableHeight, 0, imageableHeight - CROP_MARK_LENGTH));
    graphics2d.draw(new Line2D.Double(0, imageableHeight, CROP_MARK_LENGTH, imageableHeight));
    graphics2d
        .draw(new Line2D.Double(imageableWidth, imageableHeight, imageableWidth, imageableHeight - CROP_MARK_LENGTH));
    graphics2d
        .draw(new Line2D.Double(imageableWidth, imageableHeight, imageableWidth - CROP_MARK_LENGTH, imageableHeight));
    graphics2d.translate(-pageFormat.getImageableX(), -pageFormat.getImageableY());
  }
}