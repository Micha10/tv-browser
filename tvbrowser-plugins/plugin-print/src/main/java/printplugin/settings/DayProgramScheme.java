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

package printplugin.settings;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.ProgramFieldType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import printplugin.PrintPlugin;

import util.exc.ErrorHandler;
import util.io.stream.StreamUtilities;

@SuppressWarnings("nls")
public class DayProgramScheme extends Scheme<DayProgramPrinterSettings> {

  private static final String SCHEME_FILE = "printplugin.dayprog.schemes";

  public DayProgramScheme(String name) {
    super(name);
  }

  @Override
  public void store(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    Date today = new Date();
    DayProgramPrinterSettings settings = getSettings();
    int day = settings.getFromDay().getNumberOfDaysSince(today);
    out.writeInt(day);
    out.writeInt(settings.getNumberOfDays());
    writeChannels(out, settings.getChannelList());
    out.writeInt(settings.getDayStartHour());
    out.writeInt(settings.getDayEndHour());
    out.writeInt(settings.getColumnCount());
    out.writeInt(settings.getChannelsPerColumn());
    writeProgramIconSettings(settings.getProgramIconSettings(), out);
  }

  @Override
  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); // version
    int day = in.readInt();
    Date fromDay = new Date().addDays(day);
    int numberOfDays = in.readInt();
    Channel[] channelArr = readChannels(in);
    int dayStartHour = in.readInt();
    int dayEndHour = in.readInt();
    int colCount = in.readInt();
    int channelsPerColumn = in.readInt();
    ProgramIconSettings programItemSettings = readProgramIconSettings(in);
    DayProgramPrinterSettings settings = new DayProgramPrinterSettings(fromDay, numberOfDays, channelArr, dayStartHour,
        dayEndHour, colCount, channelsPerColumn, programItemSettings,
        Plugin.getPluginManager().getFilterManager().getCurrentFilter());
    setSettings(settings);
  }

  private static void writeChannels(ObjectOutputStream out, Channel[] channels) throws IOException {
    if (channels == null) {
      out.writeInt(-1);
    } else {
      out.writeInt(channels.length);
      for (Channel channel : channels) {
        out.writeObject(channel.getId());
      }
    }
  }

  private static Channel[] readChannels(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int cnt = in.readInt();
    if (cnt < 0) {
      return null;
    }
    Channel[] subscribedChannels = Plugin.getPluginManager().getSubscribedChannels();
    List<Channel> list = new ArrayList<>();
    for (int i = 0; i < cnt; i++) {
      String channelId = (String) in.readObject();
      for (Channel subscribedChannel : subscribedChannels) {
        if (channelId.equals(subscribedChannel.getId())) {
          list.add(subscribedChannel);
          break;
        }
      }
    }
    Channel[] result = new Channel[list.size()];
    list.toArray(result);
    return result;
  }

  public static Scheme<DayProgramPrinterSettings>[] loadSchemes() {
    String home = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome();
    File schemeFile = new File(home, SCHEME_FILE);
    try (ObjectInputStream in = new ObjectInputStream(
        new BufferedInputStream(new FileInputStream(schemeFile), 0x4000))) {
      return readSchemesFromStream(in);
    } catch (Exception e) {
      return getDefaultScheme();
    }
  }

  private static Scheme<DayProgramPrinterSettings>[] getDefaultScheme() {
    DayProgramScheme scheme = new DayProgramScheme(PrintPlugin.mLocalizer.msg("defaultScheme", "DefaultScheme"));
    PrintPlugin.instance();
    scheme.setSettings(new DayProgramPrinterSettings(
        new Date(),
        3,
        null,
        6,
        24 + 3,
        5,
        2,
        PrinterProgramIconSettings.create(
            new ProgramFieldType[] {
                ProgramFieldType.EPISODE_TYPE,
                ProgramFieldType.ORIGIN_TYPE,
                ProgramFieldType.PRODUCTION_YEAR_TYPE,
                ProgramFieldType.SHORT_DESCRIPTION_TYPE
            }, false),
        Plugin.getPluginManager().getFilterManager().getCurrentFilter()));
    return new DayProgramScheme[] {scheme};
  }

  public static Scheme<DayProgramPrinterSettings>[] readSchemesFromStream(ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    in.readInt(); // read version
    int cnt = in.readInt();
    Scheme<DayProgramPrinterSettings>[] schemes = new DayProgramScheme[cnt];
    for (int i = 0; i < cnt; i++) {
      String name = (String) in.readObject();
      schemes[i] = new DayProgramScheme(name);
      schemes[i].read(in);
    }
    return schemes;
  }

  public static void storeSchemes(final Scheme<DayProgramPrinterSettings>[] schemes) {
    String home = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome();
    File schemeFile = new File(home, SCHEME_FILE);
    try {
      StreamUtilities.objectOutputStream(schemeFile,
          out -> {
            out.writeInt(1); // version
            out.writeInt(schemes.length);
            for (Scheme<DayProgramPrinterSettings> scheme : schemes) {
              out.writeObject(scheme.getName());
              scheme.store(out);
            }
            out.close();
          });
    } catch (IOException e) {
      ErrorHandler.handle("Could not store settings.", e);
    }
  }
}