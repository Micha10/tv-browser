package printplugin.settings;

import java.awt.BorderLayout;
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
  private static final String DEFAULT_FONT_NAME = "defaultFont";
  private static final String DEFAULT_PRINTER = "defaultPrinter";
  private static final String DISPLAY_FONT = "displayFont";
  private static final String MARK_PRIORITY = "markPriority";
  private static final String PAGE_FORMAT_TYPE = "pageFormatType";
  private static final String PAGE_ORIENTATION = "pageOrientation";
  private static final String PROGRAM_INFO_DIRECT_PRINTING_MENU_ITEM = "programInfoDirectPrinting";

  private static final String PRINT_PAGE_NUMBERS = "printPageNumbers";
  private static final String TOOLBAR_POSITION = "toolbarPosition";
  private static final String SHOW_IMAGEABLE_AREA = "showImageableArea";
  private static final String INTEGRATE_EXTRAS_TAB = "integrateExtras";

  public static final int SHOW_IMAGEABLE_AREA_ON_MOUSE_OVER = 0;
  public static final int SHOW_IMAGEABLE_AREA_ALWAYS = 1;
  public static final int SHOW_IMAGEABLE_AREA_NEVER = 2;

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
    return getPageFormatType().getPageFormat(printerJob, this);
  }

  public PageFormatType getPageFormatType() {
    PageFormatType result;
    try {
      result = PageFormatType.valueOf(get(PAGE_FORMAT_TYPE, PageFormatType.NORMAL.name()));
    } catch (Exception e) {
      result = PageFormatType.NORMAL;
    }
    return result;
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
    set(DEFAULT_FONT_NAME, fontName);
  }

  public String getDefaultFontName() {
    return get(DEFAULT_FONT_NAME, ROBOTO_CONDENSED);
  }

  /**
   * Returns the default font (defaults to Roboto Condensed if available and falls
   * back to
   * Dialog - the system default font) with style {@link Font#PLAIN} and a font
   * size of 12.
   * <p>
   * Use {@link Font#deriveFont(int, float)} to apply a style and size.
   *
   * @return a {@link Font} instance of the default font
   */
  public Font getDefaultFont() {
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

  /**
   * Returns the stored print service name, or the system's default print service
   * name as fallback. If neither a print service name was stored, nor the system
   * has a default printer, <code>null</code> is returned.
   *
   * @return the stored print service name, the system's default print service
   *           name, or <code>null</code>
   * @see PrinterJob#getPrinterJob()
   * @see PrinterJob#getPrintService()
   */
  public String getPrintServiceName() {
    return getPrintServiceName(PrinterJob.getPrinterJob());
  }

  /**
   * Returns the stored print service name, or the print service name of the given
   * {@link PrinterJob}. If neither a print service name was stored, nor the
   * printer job has a valid associated print service, <code>null</code> is
   * returned.
   *
   * @param printerJob
   *                     the printer job that is used as fallback to get a print
   *                     service name (can be <code>null</code>)
   * @return the stored print service name, the print service name of the printer
   *           job, or <code>null</code>
   * @see PrinterJob#getPrintService()
   */
  public String getPrintServiceName(final PrinterJob printerJob) {
    PrintService printService;
    try {
      printService = printerJob.getPrintService();
    } catch (Exception e) {
      printService = null;
    }
    return get(DEFAULT_PRINTER, printService == null ? null : printService.getName());
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

  public void printPageNumbers(final boolean printPageNumbers) {
    set(PRINT_PAGE_NUMBERS, printPageNumbers);
  }

  public boolean printPageNumbers() {
    return get(PRINT_PAGE_NUMBERS, true);
  }

  public void toolbarPosition(final String pos) {
    switch (pos) {
      case BorderLayout.PAGE_START:
      case BorderLayout.LINE_START:
      case BorderLayout.LINE_END:
        set(TOOLBAR_POSITION, pos);
        break;
      default:
        set(TOOLBAR_POSITION, BorderLayout.LINE_END);
        break;
    }
  }

  public String toolbarPosition() {
    return get(TOOLBAR_POSITION, BorderLayout.LINE_END);
  }

  public void showImageableArea(final int showImageableArea) {
    set(SHOW_IMAGEABLE_AREA,
        showImageableArea >= SHOW_IMAGEABLE_AREA_ON_MOUSE_OVER && showImageableArea <= SHOW_IMAGEABLE_AREA_NEVER
            ? showImageableArea
            : SHOW_IMAGEABLE_AREA_ON_MOUSE_OVER);

  }

  public int showImageableArea() {
    return get(SHOW_IMAGEABLE_AREA, SHOW_IMAGEABLE_AREA_ON_MOUSE_OVER);
  }

  public void integrateExtras(final boolean integrateExtras) {
    set(INTEGRATE_EXTRAS_TAB, integrateExtras);
  }

  public boolean integrateExtras() {
    return get(INTEGRATE_EXTRAS_TAB, false);
  }
}