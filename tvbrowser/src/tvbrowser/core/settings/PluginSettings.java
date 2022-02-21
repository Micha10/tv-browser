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
 *     $Date: 2007-01-09 18:37:06 +0100 (Di, 09 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2997 $
 */
package tvbrowser.core.settings;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import tvbrowser.core.Settings;
import util.exc.ErrorHandler;
import util.i18n.Localizer;
import util.io.stream.OutputStreamProcessor;
import util.io.stream.StreamUtilities;

/**
 * Central class for handling of properties storage for plugins.
 * 
 * @author René Mach
 * @since 4.2.5
 */
public final class PluginSettings {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(PluginSettings.class);
  
  public static interface Storing {
    public void loadSettings(Properties prop);
    public Properties storeSettings();
    public String getFileName();
    public String toString();
  }
  
  public static boolean storeSettings(Storing settings) {
    final AtomicBoolean result = new AtomicBoolean(false);
    final Properties prop = settings.storeSettings();
    // don't ever delete settings file if prop is null
    // since stored data might be needed but not saved again
    if (prop!=null) {
      String dir=Settings.getUserSettingsDirName();
      final AtomicReference<File> file = new AtomicReference<File>(new File(dir));
      
      if (!file.get().exists()) {
        file.get().mkdir();
      }
      file.set(new File(dir,settings.getFileName()));
      
      final AtomicReference<File> old = new AtomicReference<File>(new File(file.get().getAbsolutePath()+"_old"));
      final AtomicReference<File> temp = new AtomicReference<File>(new File(file.get().getAbsolutePath()+"_temp"));
      
      try {
        StreamUtilities.outputStream(temp.get(), new OutputStreamProcessor() {
          public void process(OutputStream outputStream) throws IOException {
            prop.store(outputStream, "Settings for plugin " + settings.toString());
            
            if(temp.get().isFile()) {
              result.set(true);
              
              if(old.get().isFile()) {
                old.get().delete();
              }
              
              if(!old.get().isFile()) {
                file.get().renameTo(old.get());
              }
              
              if(!file.get().isFile()) {
                temp.get().renameTo(file.get());
              }
              else if(file.get().delete()) {
                temp.get().renameTo(file.get());
              }
            }
          }
        });
      } catch (IOException exc) {
        String msg = LOCALIZER.msg("error.write", "Saving settings for plugin {0} failed!\n({1})",
            settings.toString(), file.get().getAbsolutePath(), exc);
        ErrorHandler.handle(msg, exc);
      }
    }
    
    return result.get();
  }
  
  public static void loadSettings(Storing settings) {
    final File file = new File(Settings.getUserSettingsDirName(),settings.getFileName());
    final File old = new File(Settings.getUserSettingsDirName(),settings.getFileName()+"_old");
    
    if (file.exists() && file.length() > 0) {
      if(loadProperties(file, settings) == null && old.isFile()) {
        loadProperties(old, settings);
      }
    } else if(old.isFile() && old.length() > 0) {
      loadProperties(old, settings);
    } else {
      settings.loadSettings(new Properties());
    }
  }
  
  private static Properties loadProperties(final File file, final Storing settings) {
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
