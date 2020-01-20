package printplugin.settings;

import static printplugin.util.Utils.CENTIMETER;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;

public enum PageFormatType {

  CUSTOM(-1),
  MINIMAL(0),
  NARROW(1),
  NORMAL(2),
  SYSTEM(-2);

  private final int mType;

  private PageFormatType(final int type) {
    mType = type;
  }

  // https://stackoverflow.com/questions/10455268/java-printing-creating-a-pageformat-with-minimum-acceptable-margin
  public static PageFormat getMinimumMarginPageFormat(final PrinterJob printerJob) {
    final PageFormat pf0 = printerJob.defaultPage();
    final PageFormat pf1 = (PageFormat) pf0.clone();
    final Paper p = pf0.getPaper();
    p.setImageableArea(0, 0, pf0.getWidth(), pf0.getHeight());
    pf1.setPaper(p);
    return printerJob.validatePage(pf1);
  }

  /**
   * Returns a validated {@link PageFormat} for the current page format type.
   * <p>
   * If the <code>defaultPaper</code> is not <code>null</code> and the type
   * is set to {@link #CUSTOM}, the values of the given paper are applied.
   * The orientation will be ignored, if the given value is not one of
   * {@link PageFormat#LANDSCAPE}, {@link PageFormat#PORTRAIT}, or
   * {@link PageFormat#REVERSE_LANDSCAPE}.
   * <p>
   * This is a convenient method to load paper format and paper orientation from
   * a given {@link PluginSettings} instance.
   *
   * @param printerJob
   *                     the {@link PrinterJob} that is utilized to setup
   *                     a printer-specific default page, and to validate the
   *                     {@link PageFormat}.
   * @param settings
   *                     {@link PluginSettings} instance to load paper format and
   *                     page orientation from. Only applied if the page format
   *                     type
   *                     is set to
   *                     {@link #CUSTOM} and the orientation is valid.
   * @return a validated {@link PageFormat} instance for the printer defined
   *           by the given {@link PrinterJob}
   */
  public PageFormat getPageFormat(final PrinterJob printerJob, final PluginSettings settings) {
    return getPageFormat(printerJob, settings.getPaper(), settings.getPageOrientation());
  }

  /**
   * Returns a validated {@link PageFormat} for the current page format type.
   * <p>
   * If the <code>defaultPaper</code> is not <code>null</code> and the type
   * is set to {@link #CUSTOM}, the values of the given paper are applied.
   * The orientation will be ignored, if the given value is not one of
   * {@link PageFormat#LANDSCAPE}, {@link PageFormat#PORTRAIT}, or
   * {@link PageFormat#REVERSE_LANDSCAPE}.
   *
   * @param printerJob
   *                       the {@link PrinterJob} that is utilized to setup
   *                       a printer-specific default page, and to validate the
   *                       {@link PageFormat}.
   * @param defaultPaper
   *                       default values for a {@link #CUSTOM} page format
   * @param orientation
   *                       the orientation of the page (only applied if the value
   *                       is in the correct range)
   * @return a validated {@link PageFormat} instance for the printer defined
   *           by the given {@link PrinterJob}
   */
  @SuppressWarnings("incomplete-switch")
  public PageFormat getPageFormat(final PrinterJob printerJob, final Paper defaultPaper, final int orientation) {
    PageFormat pageFormat = printerJob.defaultPage();
    switch (mType) {
      case 0:
        pageFormat = getMinimumMarginPageFormat(printerJob);
        break;
      case 1:
      case 2:
        final double ppcm = mType * CENTIMETER;
        final Paper paper = pageFormat.getPaper();
        paper.setImageableArea(ppcm, ppcm, paper.getWidth() - 2 * ppcm, paper.getHeight() - 2 * ppcm);
        pageFormat.setPaper(paper);
        break;
      case -1:
        if (defaultPaper != null) {
          pageFormat.setPaper(defaultPaper);
        }
        break;
      case -2:
      default:
        break;
    }
    switch (orientation) {
      case PageFormat.LANDSCAPE:
      case PageFormat.PORTRAIT:
      case PageFormat.REVERSE_LANDSCAPE:
        pageFormat.setOrientation(orientation);
        break;
    }
    return printerJob.validatePage(pageFormat);
  }

  /**
   * Returns a {@link PageFormat} describing a portrait orientated page
   * in format DIN A4 with a margin of two centimeters on each side.
   *
   * @return a DIN A4 page with a two centimeter margin around.
   */
  public static PageFormat createDefaultPageFormat() {
    final Paper paper = new Paper();
    paper.setSize(21 * CENTIMETER, 29.7 * CENTIMETER);
    paper.setImageableArea(
        2 * CENTIMETER, 2 * CENTIMETER,
        21 * CENTIMETER - 2 * 2 * CENTIMETER, 29.7 * CENTIMETER - 2 * 2 * CENTIMETER);
    final PageFormat pageFormat = new PageFormat();
    pageFormat.setOrientation(PageFormat.PORTRAIT);
    pageFormat.setPaper(paper);
    return pageFormat;
  }
}