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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.extras.common;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import tvbrowser.core.Settings;
import tvbrowser.core.settings.PluginSettings;
import tvbrowser.core.settings.PluginSettings.Data;
import util.exc.TvBrowserException;


/**
 * ConfigurationHandler is used to load and store configurations.
 */
public class ConfigurationHandler implements PluginSettings.Preferences {
  private String mFilePrefix;
  private String mName;
  private Properties mProp;

  public ConfigurationHandler(String name, String filePrefix) {
    mName = name;
    mFilePrefix = filePrefix;
  }

  public void loadData(Data data) throws IOException {
    try {
      PluginSettings.readData(new File(Settings.getUserSettingsDirName()), data);
    } catch (TvBrowserException e) {
      throw new IOException(e.getMessage(),e);
    }
  }

  public synchronized void storeData(final Data data) throws IOException {
    try {
      PluginSettings.writeData(new File(Settings.getUserSettingsDirName()), data, true);
    } catch (TvBrowserException e) {
      throw new IOException(e.getMessage(),e);
    }
  }
  
  public Properties loadSettings() throws IOException {
    mProp = null;
    PluginSettings.loadSettings(this);
    return mProp;
  }

  public synchronized void storeSettings(Properties settings) throws IOException {
    mProp = settings;
    PluginSettings.storeSettings(this);
  }

  @Override
  public Properties storeSettings() {
    return mProp;
  }

  @Override
  public void loadSettings(Properties prop) {
    mProp = prop;
  }

  @Override
  public String getBaseFileName() {
    return "java." + mFilePrefix;
  }
  
  @Override
  public String toString() {
    return mName;
  }

  @Override
  public boolean hasToSaveSettings() {
    return true;
  }
}
