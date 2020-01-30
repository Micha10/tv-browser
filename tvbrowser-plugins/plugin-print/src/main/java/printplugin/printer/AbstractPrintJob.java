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

package printplugin.printer;

import java.awt.Color;
import java.awt.Font;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;

import printplugin.PrintPlugin;
import printplugin.dlgs.PreviewDlg;

import util.ui.Localizer;
import util.ui.UiUtilities;

@SuppressWarnings("nls")
public abstract class AbstractPrintJob implements PrintJob {

  private static final Font FOOTER_FONT = PrintPlugin.getInstance().getPluginSettings().deriveDefaultFont(Font.PLAIN,
      6);

  private Page[] mPages;
  private PageModel[] mPageModelArr;
  private PageFormat mPageFormat;

  protected AbstractPrintJob(PageModel[] pageModelArr, PageFormat pageFormat) {
    mPageModelArr = pageModelArr;
    mPageFormat = pageFormat;
  }

  @Override
  public PageFormat getPageFormat() {
    return mPageFormat;
  }

  private void prepare() {
    List<Page> pages = new ArrayList<>();
    for (PageModel element : mPageModelArr) {
      Page[] p = createPages(element);
      for (Page element2 : p) {
        pages.add(element2);
      }
    }
    mPages = new Page[pages.size()];
    pages.toArray(mPages);
  }

  protected abstract Page[] createPages(PageModel pageModel);

  @Override
  public int getNumOfPages() {
    if (mPages == null) {
      prepare();
    }
    return mPages.length;
  }

  @SuppressWarnings("boxing")
  @Override
  public Printable getPrintable() {
    return (graphics, f, pageIndex) -> {
      if (mPages == null) {
        prepare();
      }
      if (pageIndex >= mPages.length) {
        return Printable.NO_SUCH_PAGE;
      }
      mPages[pageIndex].printPage(graphics);

      String pageInfo = Localizer.getLocalizerFor(PreviewDlg.class)
          .msg("pageInfo", "Page {0} of {1}", pageIndex + 1, mPages.length);
      int w = UiUtilities.getStringWidth(FOOTER_FONT, pageInfo);
      graphics.setFont(FOOTER_FONT);
      graphics.setColor(Color.black);
      PageFormat pageFormat = mPages[pageIndex].getPageFormat();
      graphics.drawString(pageInfo, (int) pageFormat.getImageableX() + (int) pageFormat.getImageableWidth() - w - 5,
          (int) pageFormat.getImageableY() + (int) pageFormat.getImageableHeight() - 3);

      return Printable.PAGE_EXISTS;
    };
  }
}