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
 * object.
 * The component supports scrolling, zooming and selecting a specific page of
 * the printable instance.
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

  @Override
  public Dimension getMinimumSize() {
    return new Dimension(
        (int) (mInitialSize.width * MIN_ZOOM),
        (int) (mInitialSize.height * MIN_ZOOM));
  }

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
   * The zoom level is taken into account.
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

  public void setZoom(final double zoom) {
    mZoom = zoom;
    updateSize();
    revalidate();
    repaint(getVisibleRect());
  }

  public Dimension getInitialSize() {
    return mInitialSize;
  }

  public double getZoom() {
    return mZoom;
  }

  public boolean minZoom() {
    return mZoom <= MIN_ZOOM;
  }

  public boolean maxZoom() {
    return mZoom >= MAX_ZOOM;
  }

  public void zoomIn() {
    if (mZoom < MAX_ZOOM) {
      mZoom += ZOOM_STEP;
      setZoom(mZoom);
    }
  }

  public void zoomOut() {
    if (mZoom > MIN_ZOOM) {
      mZoom -= ZOOM_STEP;
      setZoom(mZoom);
    }
  }

  public void next() {
    if (mPageIndex < mNumberOfPages - 1) {
      mPageIndex++;
      repaint(getVisibleRect());
    }
  }

  public void previous() {
    if (mPageIndex > 0) {
      mPageIndex--;
      repaint(getVisibleRect());
    }
  }

  public int getNumberOfPages() {
    return mNumberOfPages;
  }

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