/*
 * TV-Browser
 * Copyright (C) 2022 TV-Browser-Team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.core.settings;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.logging.Logger;

import tvbrowser.core.Settings;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.i18n.Localizer;

/**
 * Central class for handling of properties storage for plugins.
 * 
 * @author René Mach
 * @since 4.2.5
 */
public final class PluginSettings {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(PluginSettings.class);
  private static final Logger LOG = Logger.getLogger(PluginSettings.class.getName());
  
  public static interface Preferences {
    public void loadSettings(Properties prop);
    public Properties storeSettings();
    public String getBaseFileName();
    public String toString();
    public boolean hasToSaveSettings();
  }
  
  public static interface Data {
    public boolean hasToSaveSettings();
    /**
     * Called by the host-application during start-up.
     * <p>
     * Override this method to load any objects from the file system.
     *
     * @param in The stream to read the objects from.
     * @throws IOException If reading failed.
     * @throws ClassNotFoundException If an object could not be casted correctly.
     *
     * @see #writeData(ObjectOutputStream)
     */
    public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException;
    /**
     * Counterpart to loadData. Called when the application shuts down.
     * <p>
     * Override this method to store any objects to the file system.
     * ATTENTION: Don't use any logger, thread or access to Frames in this method.
     *
     * @param out The stream to write the objects to
     * @throws IOException If writing failed.
     *
     * @see #readData(ObjectInputStream)
     */
    public void writeData(ObjectOutputStream out) throws IOException;
    public String getBaseFileName();
    public String toString();
  }
  
  private static boolean readDataInternal(final File file, final Data data) throws TvBrowserException {
    boolean result = false;
  
    try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file), 0x4000))) {
      data.readData(in);
      result = true;
    }
    catch (Throwable thr) {
      throw new TvBrowserException(data.getClass(), "error.data.load",
          "Loading data for plugin {0} failed.\n({1})",
          data.toString(), file.getAbsolutePath(), thr);
    }
    
    return result;
  }
  
  public static void readData(final Data data) throws TvBrowserException {
    final File userDirectory = new File(Settings.getUserSettingsDirName());
    
    final File datFile = new File(userDirectory, data.getBaseFileName() + ".dat");
    final File datFileBackup = new File(userDirectory, data.getBaseFileName() + ".dat_old");
    
    // load plugin data
    if (datFile.exists() && datFile.length() > 0) {
      if(!readDataInternal(datFile, data) && datFileBackup.isFile() && datFileBackup.length() > 0) {
        if(readDataInternal(datFileBackup, data)) {
          LOG.severe("Date file '" + datFile.getAbsolutePath() + "' could not be read. Read old file instead: '" + datFileBackup.getAbsolutePath() + "'.");
        }
      }
    }
    else if(datFileBackup.isFile() && datFileBackup.length() > 0) {
      readDataInternal(datFileBackup, data);
    }
  }
  
  public static void writeData(final Data data, boolean log) throws TvBrowserException {
    final File userDirectory = new File(Settings.getUserSettingsDirName());
    
    if(!userDirectory.isDirectory()) {
      userDirectory.mkdirs();
    }
    
    if(log) {
      LOG.info("Storing plugin settings for " + data.toString() + "...");
    }

    // save the plugin data in a temp file
    File tmpDatFile = new File(userDirectory, data.getBaseFileName() + ".dat.temp");
    File oldDatFile = new File(userDirectory, data.getBaseFileName() + ".dat_old");
    
    try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tmpDatFile))) {
      data.writeData(out);
      
      // Saving succeeded -> Delete the old file and rename the temp file
      File datFile = new File(userDirectory, data.getBaseFileName() + ".dat");
      
      if(oldDatFile.isFile()) {
        oldDatFile.delete();
      }
      
      datFile.renameTo(oldDatFile);
      tmpDatFile.renameTo(datFile);
    }
    catch(Throwable thr) {
      throw new TvBrowserException(data.getClass(), "error.data.write",
          "Saving data for plugin {0} failed.\n({1})",
          data.toString(), tmpDatFile.getAbsolutePath(), thr);
    }
  }
  
  private static String getExtensionFor(Preferences settings) {
    String result = ".prop";
    
    if(settings instanceof TvDataServiceProxy) {
      result = ".service";
    }
    
    return result;
  }
  
  public static boolean storeSettings(Preferences settings) {
    boolean result = false;
    
    final Properties prop = settings.storeSettings();
    // don't ever delete settings file if prop is null
    // since stored data might be needed but not saved again
    if (prop!=null) {
      String dir=Settings.getUserSettingsDirName();
      File propFile = new File(dir);
      
      if (!propFile.exists()) {
        propFile.mkdir();
      }
      propFile = new File(dir,settings.getBaseFileName() + getExtensionFor(settings));
      
      File oldPropFile = new File(propFile.getAbsolutePath()+"_old");
      File tmpPropFile = new File(propFile.getAbsolutePath()+"_temp");
      
      try(FileOutputStream outputStream = new FileOutputStream(tmpPropFile)) {
        prop.store(outputStream, "Settings for plugin " + settings.toString());
        
        if(tmpPropFile.isFile() && tmpPropFile.length() > 0) {
          result = true;
          
          if(oldPropFile.isFile()) {
            oldPropFile.delete();
          }
          
          propFile.renameTo(oldPropFile);
          tmpPropFile.renameTo(propFile);
        }
      } catch (IOException exc) {
        String msg = LOCALIZER.msg("error.write", "Saving settings for plugin {0} failed!\n({1})",
            settings.toString(), propFile.getAbsolutePath(), exc);
        ErrorHandler.handle(msg, exc);
      }
    }
    
    return result;
  }
  
  public static void loadSettings(Preferences settings) {
    final File propFile = new File(Settings.getUserSettingsDirName(),settings.getBaseFileName()+getExtensionFor(settings));
    final File oldPropFile = new File(propFile.getAbsolutePath()+"_old");
    
    if (propFile.exists() && propFile.length() > 0) {
      if(loadProperties(propFile, settings) == null && oldPropFile.isFile()) {
        loadProperties(oldPropFile, settings);
      }
    } else if(oldPropFile.isFile() && oldPropFile.length() > 0) {
      loadProperties(oldPropFile, settings);
    } else {
      settings.loadSettings(new Properties());
    }
  }
  
  private static Properties loadProperties(final File file, final Preferences settings) {
    Properties p=new Properties();
    
    try {
      try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(file), 0x1000)) {
        p.load(in);
      }
      
      settings.loadSettings(p);
    } catch (IOException exc) {
      p = null;
      String msg = LOCALIZER.msg("error.load", "Loading settings for plugin {0} failed!\n({1})",
          settings.toString(), file.getAbsolutePath(), exc);
      ErrorHandler.handle(msg, exc);
    }
    
    return p;
  }
}
