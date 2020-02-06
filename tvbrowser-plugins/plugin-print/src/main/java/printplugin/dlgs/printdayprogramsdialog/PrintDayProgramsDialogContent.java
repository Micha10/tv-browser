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

package printplugin.dlgs.printdayprogramsdialog;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFilter;

import java.awt.Component;
import java.awt.Frame;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTabbedPane;

import printplugin.PrintPlugin;
import printplugin.dlgs.DialogContent;
import printplugin.dlgs.printfromqueuedialog.ExtrasTab;
import printplugin.printer.DefaultColumnModel;
import printplugin.printer.DefaultPageModel;
import printplugin.printer.PageModel;
import printplugin.printer.PrintJob;
import printplugin.printer.dayprogramprinter.DayProgramPrintJob;
import printplugin.settings.DayProgramPrinterSettings;
import printplugin.settings.DayProgramScheme;
import printplugin.settings.Scheme;
import printplugin.util.Utils;

import util.ui.Localizer;

@SuppressWarnings("nls")
public class PrintDayProgramsDialogContent implements DialogContent<DayProgramPrinterSettings> {

  /** The localizer for this class. */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PrintDayProgramsDialogContent.class);

  private ListingsTab mListingsTab;
  private LayoutTab mLayoutTab;
  private ExtrasTab mExtrasTab;

  @Override
  public void printingDone() {}

  @Override
  public Component getContent(final Frame parentFrame) {
    final boolean integrateExtras = PrintPlugin.settings().integrateExtras();
    final JTabbedPane tab = new JTabbedPane();
    mListingsTab = new ListingsTab(parentFrame);
    mLayoutTab = new LayoutTab(parentFrame, integrateExtras);
    if (integrateExtras) {
      mExtrasTab = mLayoutTab.extrasTab();
    } else {
      mExtrasTab = new ExtrasTab(parentFrame, true);
    }
    tab.add(mLocalizer.msg("listingsTab", "Data"), mListingsTab);
    tab.add(mLocalizer.msg("layoutTab", "Layout"), mLayoutTab);
    if (!integrateExtras) {
      tab.add(mLocalizer.msg("miscTab", "Extras"), mExtrasTab);
    }
    Utils.setOpaque(tab, false);
    return tab;
  }

  @Override
  public String getDialogTitle() {
    return mLocalizer.msg("dialogTitle", "Tagesprogramme drucken");
  }

  @Override
  public DayProgramPrinterSettings getSettings() {
    return new DayProgramPrinterSettings(mListingsTab.getDateFrom(),
        mListingsTab.getNumberOfDays(),
        mListingsTab.getChannels(),
        mListingsTab.getFromTime(),
        mListingsTab.getToTime(),
        mLayoutTab.getColumnsPerPage(),
        mLayoutTab.getChannelsPerColumn(),
        mExtrasTab.getProgramIconSettings(),
        mListingsTab.getSelectedFilter());
  }

  @Override
  public void setSettings(final DayProgramPrinterSettings settings) {
    final int start = settings.getDayStartHour();
    final int end = settings.getDayEndHour();
    final Date from = settings.getFromDay();
    final Channel[] ch = settings.getChannelList();
    mListingsTab.setChannels(ch);
    mListingsTab.setTimeRange(start, end);
    mListingsTab.setDateFrom(from);
    mListingsTab.setDayCount(settings.getNumberOfDays());
    mLayoutTab.setColumnLayout(settings.getColumnCount(), settings.getChannelsPerColumn());
    mExtrasTab.setProgramIconSettings(settings.getProgramIconSettings());
  }

  /**
   * We create the print job in two steps:
   * First we create page models where each page consists of a number of columns.
   * A page model is not a real page.
   * Then we have to split each page model in one or more real pages.
   *
   * @param format
   *                 {@link PageFormat}
   * @return {@link PrintJob}
   */
  @Override
  public PrintJob createPrintJob(final PageFormat format) {

    final DayProgramPrinterSettings settings = getSettings();
    final List<PageModel> pageModelList = new ArrayList<>();
    final int dayCount = settings.getNumberOfDays();
    final Date startDate = settings.getFromDay();
    final int dayStartHour = settings.getDayStartHour();
    final int dayEndHour = settings.getDayEndHour();

    Channel[] channelArr = settings.getChannelList();
    if (channelArr == null) {
      channelArr = Plugin.getPluginManager().getSubscribedChannels();
    }
    for (int dateInx = 0; dateInx < dayCount; dateInx++) {
      final Date date = startDate.addDays(dateInx);
      final DefaultPageModel pageModel = new DefaultPageModel(date.getLongDateString());
      pageModelList.add(pageModel);
      for (Channel element : channelArr) {
        final List<Program> progList = new ArrayList<>();
        addProgramToList(progList, date, element, dayStartHour, dayEndHour, settings.getProgramFilter());
        final Program[] progArr = new Program[progList.size()];
        progList.toArray(progArr);

        if (progArr.length > 0) {
          pageModel.addColumn(new DefaultColumnModel(element.getName(), progArr));
        }
      }
    }

    final PageModel[] pageModel = new PageModel[pageModelList.size()];
    pageModelList.toArray(pageModel);
    return new DayProgramPrintJob(pageModel, settings, format);
  }

  @Override
  public Scheme<DayProgramPrinterSettings> createNewScheme(final String schemeName) {
    return new DayProgramScheme(schemeName);
  }

  private static void addProgramToList(final List<Program> progList, final Date date, final Channel channel,
      final int startHour,
      final int endHour, final ProgramFilter filter) {
    for (int dateOffset = -1; dateOffset <= 1; dateOffset++) {
      for (Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(date.addDays(dateOffset), channel); it
          .hasNext();) {
        Program prog = it.next();
        if (prog.getDate().getNumberOfDaysSince(date) == 0 && prog.getHours() >= startHour && prog.getHours() < endHour
            && filter.accept(prog)) {
          progList.add(prog);
        } else if (prog.getDate().getNumberOfDaysSince(date) == 1 && prog.getHours() >= startHour - 24
            && prog.getHours() < endHour - 24 && filter.accept(prog)) {
          progList.add(prog);
        }
      }
    }
  }

  @Override
  public Scheme<DayProgramPrinterSettings>[] loadSchemes() {
    return DayProgramScheme.loadSchemes();
  }

  @Override
  public void storeSchemes(final Scheme<DayProgramPrinterSettings>[] schemes) {
    DayProgramScheme.storeSchemes(schemes);
  }
}