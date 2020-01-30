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

package printplugin.dlgs.printfromqueuedialog;

import devplugin.PluginTreeNode;
import devplugin.Program;

import java.awt.Component;
import java.awt.Frame;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Arrays;

import javax.swing.JTabbedPane;

import printplugin.PrintPlugin;
import printplugin.dlgs.DialogContent;
import printplugin.printer.DefaultColumnModel;
import printplugin.printer.DefaultPageModel;
import printplugin.printer.PrintJob;
import printplugin.printer.queueprinter.QueuePrintJob;
import printplugin.settings.QueuePrinterSettings;
import printplugin.settings.QueueScheme;
import printplugin.settings.Scheme;
import printplugin.util.Utils;

import util.program.ProgramUtilities;
import util.ui.Localizer;

@SuppressWarnings("nls")
public class PrintFromQueueDialogContent implements DialogContent<QueuePrinterSettings> {

  /** The localizer for this class. */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PrintFromQueueDialogContent.class);

  private GeneralTab mGeneralTab;
  private LayoutTab mLayoutTab;
  private ExtrasTab mExtrasTab;
  private PluginTreeNode mRootNode;

  public PrintFromQueueDialogContent(PluginTreeNode rootNode) {
    mRootNode = rootNode;
  }

  @Override
  public void printingDone() {
    if (mGeneralTab.emptyQueueAfterPrinting()) {
      Program[] progs = mRootNode.getPrograms();
      for (Program prog : progs) {
        prog.unmark(PrintPlugin.getInstance());
      }
      mRootNode.removeAllChildren();
      mRootNode.update();
    }
  }

  @Override
  public Component getContent(final Frame parentFrame) {
    JTabbedPane tab = new JTabbedPane();
    mGeneralTab = new GeneralTab(parentFrame, getDialogTitle(), mRootNode);
    mLayoutTab = new LayoutTab();
    mExtrasTab = new ExtrasTab(parentFrame);
    tab.add(mLocalizer.msg("listingsTab", "Data"), mGeneralTab);
    tab.add(mLocalizer.msg("layoutTab", "Layout"), mLayoutTab);
    tab.add(mLocalizer.msg("miscTab", "Extras"), mExtrasTab);
    Utils.setOpaque(tab, false);
    return tab;
  }

  @Override
  public String getDialogTitle() {
    return mLocalizer.msg("dialogTitle", "Print from queue");
  }

  @Override
  public QueuePrinterSettings getSettings() {
    return new QueuePrinterSettings(mGeneralTab.emptyQueueAfterPrinting(), mLayoutTab.getColumnsPerPage(),
        mExtrasTab.getProgramIconSettings(), mExtrasTab.getDateFont());
  }

  @Override
  public void setSettings(QueuePrinterSettings settings) {
    mGeneralTab.setEmptyQueueAfterPrinting(settings.emptyQueueAfterPrinting());
    mLayoutTab.setColumnsPerPage(settings.getColumnsPerPage());
    mExtrasTab.setProgramIconSettings(settings.getProgramIconSettings());
    mExtrasTab.setDateFont(settings.getDateFont());
  }

  @Override
  public PrintJob createPrintJob(PageFormat format) {

    final Program[] programs = mRootNode.getPrograms();

    if (programs.length == 0) {
      return createEmptyJob(format);
    }
    Arrays.sort(programs, ProgramUtilities.getProgramComparator());

    DefaultPageModel pageModel = new DefaultPageModel();
    DefaultColumnModel colModel = new DefaultColumnModel("Column");
    pageModel.addColumn(colModel);
    for (Program program : programs) {
      colModel.addProgram(program);
    }

    return new QueuePrintJob(pageModel, getSettings(), format);
  }

  @Override
  public Scheme<QueuePrinterSettings> createNewScheme(String schemeName) {
    return new QueueScheme(schemeName);
  }

  private static PrintJob createEmptyJob(final PageFormat format) {
    return new PrintJob() {

      @Override
      public Printable getPrintable() {
        return (graphics, pageFormat, pageIndex) -> Printable.NO_SUCH_PAGE;
      }

      @Override
      public int getNumOfPages() {
        return 0;
      }

      @Override
      public PageFormat getPageFormat() {
        return format;
      }
    };
  }

  @Override
  public void storeSchemes(final Scheme<QueuePrinterSettings>[] schemes) {
    QueueScheme.storeSchemes(schemes);
  }

  @Override
  public Scheme<QueuePrinterSettings>[] loadSchemes() {
    return QueueScheme.loadSchemes();
  }
}