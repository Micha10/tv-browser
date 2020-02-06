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

import devplugin.Plugin;
import devplugin.ProgramFieldType;

import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import printplugin.PrintPlugin;

import util.exc.ErrorHandler;
import util.io.stream.StreamUtilities;

@SuppressWarnings("nls")
public class QueueScheme extends Scheme<QueuePrinterSettings> {

  private static final String SCHEME_FILE = "printplugin.queue.schemes";

  public QueueScheme(String name) {
    super(name);
  }

  @Override
  public void store(ObjectOutputStream out) throws IOException {
    QueuePrinterSettings settings = getSettings();
    boolean emptyQueuAfterPrinting = settings.emptyQueueAfterPrinting();
    int columnsPerPage = settings.getColumnsPerPage();
    out.writeInt(1); // version
    out.writeBoolean(emptyQueuAfterPrinting);
    out.writeInt(columnsPerPage);
    writeProgramIconSettings(settings.getProgramIconSettings(), out);
    writeFont(settings.getDateFont(), out);
  }

  @Override
  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); // version
    boolean emptyQueueAfterPrinting = in.readBoolean();
    int columnsPerPage = in.readInt();
    ProgramIconSettings programIconSettings = readProgramIconSettings(in);
    Font dateFont = readFont(in);
    QueuePrinterSettings settings = new QueuePrinterSettings(emptyQueueAfterPrinting, columnsPerPage,
        programIconSettings, dateFont);
    setSettings(settings);
  }

  public static Scheme<QueuePrinterSettings>[] loadSchemes() {
    String home = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome();
    File schemeFile = new File(home, SCHEME_FILE);
    try (ObjectInputStream in = new ObjectInputStream(
        new BufferedInputStream(new FileInputStream(schemeFile), 0x4000))) {
      return readSchemesFromStream(in);
    } catch (Exception e) {
      return getDefaultScheme();
    }
  }

  private static Scheme<QueuePrinterSettings>[] getDefaultScheme() {
    QueueScheme scheme = new QueueScheme(PrintPlugin.mLocalizer.msg("defaultScheme", "DefaultScheme"));
    scheme.setSettings(new QueuePrinterSettings(
        true,
        1,
        PrinterProgramIconSettings.create(
            new ProgramFieldType[] {
                ProgramFieldType.EPISODE_TYPE,
                ProgramFieldType.ORIGIN_TYPE,
                ProgramFieldType.PRODUCTION_YEAR_TYPE,
                ProgramFieldType.SHORT_DESCRIPTION_TYPE
            }, false),
        PrintPlugin.settings().deriveDefaultFont(Font.BOLD, 12)));

    return new QueueScheme[] {scheme};
  }

  private static Scheme<QueuePrinterSettings>[] readSchemesFromStream(ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    in.readInt();  // read version
    int cnt = in.readInt();
    Scheme<QueuePrinterSettings>[] schemes = new QueueScheme[cnt];
    for (int i = 0; i < cnt; i++) {
      String name = (String) in.readObject();
      schemes[i] = new QueueScheme(name);
      schemes[i].read(in);
    }
    return schemes;
  }

  public static void storeSchemes(final Scheme<QueuePrinterSettings>[] schemes) {
    String home = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome();
    File schemeFile = new File(home, SCHEME_FILE);
    try {
      StreamUtilities.objectOutputStream(schemeFile,
          out -> {
            out.writeInt(1); // version
            out.writeInt(schemes.length);
            for (Scheme<QueuePrinterSettings> scheme : schemes) {
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