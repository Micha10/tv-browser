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

package printplugin.dlgs;

import java.awt.Component;
import java.awt.Frame;
import java.awt.print.PageFormat;

import printplugin.printer.PrintJob;
import printplugin.settings.Scheme;
import printplugin.settings.Settings;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 24.04.2005
 * Time: 21:47:27
 *
 * @param <S>
 *              Content specific {@link Settings}
 */
public interface DialogContent<S extends Settings> {

  Component getContent(final Frame parentFrame);

  String getDialogTitle();

  S getSettings();

  void setSettings(final S settings);

  PrintJob createPrintJob(final PageFormat format);

  Scheme<S> createNewScheme(final String schemeName);

  void printingDone();

  void storeSchemes(final Scheme<S>[] schemes);

  Scheme<S>[] loadSchemes();
}