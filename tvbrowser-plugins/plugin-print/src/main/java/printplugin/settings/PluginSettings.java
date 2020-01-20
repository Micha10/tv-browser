package printplugin.settings;

import java.awt.Font;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.util.Locale;
import java.util.Properties;

import javax.print.PrintService;

import printplugin.PrintPlugin;
import printplugin.PrintPluginSettingsTab;
import util.settings.PropertyBasedSettings;

/**
 * A {@link PropertyBasedSettings} implementation that holds and manages the
 * settings of the {@link PrintPlugin}.
 * Some settings can be changed by the user via the
 * {@link PrintPluginSettingsTab}.
 *
 * @author Thorsten Giesecke
 */
@SuppressWarnings("nls")
public final class PluginSettings extends PropertyBasedSettings {

  public static final String ROBOTO_CONDENSED = "Roboto Condensed";

  private static final String ANTIALIAS = "antialias";
  private static final String DEFAULT_FONT = "defaultFont";
  private static final String DEFAULT_PRINTER = "defaultPrinter";
  private static final String DISPLAY_FONT = "displayFont";
  private static final String MARK_PRIORITY = "markPriority";
  private static final String PAGE_FORMAT_TYPE = "pageFormatType";
  private static final String PAGE_ORIENTATION = "pageOrientation";
  private static final String PROGRAM_INFO_DIRECT_PRINTING_MENU_ITEM = "programInfoDirectPrinting";

  public static final String PREVIEW_DLG_ZOOM = "PreviewDlg.Zoom";
  public static final String PREVIEW_DLG_HEIGHT = "PreviewDlg.Height";
  public static final String PREVIEW_DLG_WIDTH = "PreviewDlg.Width";
  public static final String PREVIEW_DLG_Y = "PreviewDlg.Y";
  public static final String PREVIEW_DLG_X = "PreviewDlg.X";

  public PluginSettings(final Properties properties) {
    super(properties);
  }

  public boolean isAntialias() {
    return get(ANTIALIAS, true);
  }

  public void setAntialias(final boolean antialias) {
    set(ANTIALIAS, antialias);
  }

  public void setMarkPriority(final int markPriority) {
    set(MARK_PRIORITY, markPriority);
  }

  public int getMarkPriority() {
    // get(MARK_PRIORITY, ProgramCompat.PRIORITY_MARK_MIN)
    return get(MARK_PRIORITY, 0);
  }

  public void setPageFormatType(final PageFormatType pageFormatType) {
    set(PAGE_FORMAT_TYPE, pageFormatType.name());
  }

  public PageFormat getPageFormat(final PrinterJob printerJob) {
    return PageFormatType.valueOf(get(PAGE_FORMAT_TYPE, PageFormatType.NORMAL.name())).getPageFormat(printerJob, this);
  }

  public PageFormatType getPageFormatType() {
    return PageFormatType.valueOf(get(PAGE_FORMAT_TYPE, PageFormatType.NORMAL.name()));
  }

  /**
   * @param pageOrientation
   *                          the default orientation of a page
   * @see PageFormat
   */
  public void setPageOrientation(final int pageOrientation) {
    set(PAGE_ORIENTATION, pageOrientation);
  }

  /**
   * @return the default orientation of a page
   * @see PageFormat
   */
  public int getPageOrientation() {
    return get(PAGE_ORIENTATION, -1);
  }

  // FIXME
  @SuppressWarnings("static-method")
  public Paper getPaper() {
    Paper paper = new Paper();
    return paper;
  }

  public void setDefaultFontName(final String fontName) {
    set(DEFAULT_FONT, fontName);
  }

  public String getDefaultFontName() {
    return get(DEFAULT_FONT, ROBOTO_CONDENSED);
  }

  /**
   * Returns the default font (defaults to Roboto Condensed and falls back to
   * Dialog) with style {@link Font#PLAIN} and a font size of 12.
   * <p>
   * Use {@link Font#deriveFont(int, float)} to apply a style and size.
   *
   * @return a {@link Font} instance of the default font
   */
  public Font getDefaultFont() {
    System.err.println(String.format(Locale.getDefault(), "Print default font: %s", getDefaultFontName()));
    return Font.decode(String.format(Locale.getDefault(), "%s", getDefaultFontName()));
  }

  /**
   * Returns a derivation of the default font (defaults to Roboto Condensed and
   * falls back to Dialog) using the given parameters.
   *
   * @param style
   *                one of {@link Font#BOLD}, {@link Font#PLAIN},
   *                {@link Font#ITALIC},
   *                or a combination of {@link Font#BOLD} and {@link Font#ITALIC}
   * @param size
   *                the font size
   *
   * @return a {@link Font} instance derived from the default font
   * @see Font#deriveFont(int, float)
   */
  public Font deriveDefaultFont(final int style, final float size) {
    return getDefaultFont().deriveFont(style, size);
  }

  public void setDisplayFont(final boolean selected) {
    set(DISPLAY_FONT, selected);
  }

  public boolean isDisplayFont() {
    return get(DISPLAY_FONT, true);
  }

  public String getPrintServiceName() {
    final PrintService printService = PrinterJob.getPrinterJob().getPrintService();
    return get(DEFAULT_PRINTER, printService == null ? null : String.valueOf(printService));
  }

  public void setPrintServiceName(final String printServiceName) {
    set(DEFAULT_PRINTER, printServiceName);
  }

  public boolean showProgramInfoDirectPrintingMenuItem() {
    return get(PROGRAM_INFO_DIRECT_PRINTING_MENU_ITEM, false);
  }

  public void setProgramInfoDirectPrintingMenuItem(final boolean programInfoDirectPrintingMenuItem) {
    set(PROGRAM_INFO_DIRECT_PRINTING_MENU_ITEM, programInfoDirectPrintingMenuItem);
  }
}