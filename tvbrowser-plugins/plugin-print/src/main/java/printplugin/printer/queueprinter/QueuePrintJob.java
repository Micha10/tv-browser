/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package printplugin.printer.queueprinter;

import devplugin.Program;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;

import printplugin.PrintPlugin;
import printplugin.printer.AbstractPrintJob;
import printplugin.printer.ColumnModel;
import printplugin.printer.Page;
import printplugin.printer.PageModel;
import printplugin.settings.QueuePrinterSettings;

public class QueuePrintJob extends AbstractPrintJob {

  private static final Font FOOTER_FONT = PrintPlugin.getInstance().getPluginSettings().deriveDefaultFont(Font.PLAIN,
      6);

  private static final int FOOTER_SPACE = 10;

  private QueuePrinterSettings mSettings;
  private final String mFooterString;

  public QueuePrintJob(final PageModel pageModel, final QueuePrinterSettings settings, final PageFormat pageFormat) {
    super(new PageModel[] {pageModel}, pageFormat);
    mSettings = settings;
    mFooterString = pageModel.getFooter();
  }

  @Override
  protected Page[] createPages(final PageModel pageModel) {
    final List<QueuePage> pages = new ArrayList<>();
    QueuePage currentPage = new QueuePage(mSettings, getPageFormat());
    pages.add(currentPage);
    for (int i = 0; i < pageModel.getColumnCount(); i++) {
      final ColumnModel column = pageModel.getColumnAt(i);
      for (int k = 0; k < column.getProgramCount(); k++) {
        final Program program = column.getProgramAt(k);
        if (!currentPage.addProgram(program)) {
          currentPage = new QueuePage(mSettings, getPageFormat());
          pages.add(currentPage);
          currentPage.addProgram(program, true);
        }
      }
    }

    final Page[] pageArr = new Page[pages.size()];
    pages.toArray(pageArr);
    return pageArr;
  }

  class QueuePage implements Page {

    private final PageFormat mPageFormat;
    private final ProgramTableIcon mTableIcon;

    public QueuePage(final QueuePrinterSettings settings, final PageFormat pageFormat) {
      mSettings = settings;
      mPageFormat = pageFormat;
      final int width = (int) pageFormat.getImageableWidth();
      final int height = (int) pageFormat.getImageableHeight() - FOOTER_SPACE;
      mTableIcon = new ProgramTableIcon(settings.getProgramIconSettings(), settings.getDateFont(), width, height,
          settings.getColumnsPerPage());
    }

    @Override
    public PageFormat getPageFormat() {
      return mPageFormat;
    }

    public boolean addProgram(final Program prog) {
      return addProgram(prog, false);
    }

    public boolean addProgram(final Program prog, final boolean forceAdding) {
      return mTableIcon.add(prog, forceAdding);
    }

    @Override
    public void printPage(final Graphics graphics) {
      final int x0 = (int) mPageFormat.getImageableX();
      final int y0 = (int) mPageFormat.getImageableY();

      graphics.setFont(FOOTER_FONT);
      graphics.drawString(mFooterString, x0, y0 + (int) mPageFormat.getImageableHeight() - 3);

      mTableIcon.paintIcon(null, graphics, x0, y0);
    }
  }
}