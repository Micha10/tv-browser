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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.logging.Logger;

import tvbrowser.core.Settings;
import tvbrowser.core.settings.PluginSettings;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;


/**
 * ConfigurationHandler is used to load and store configurations.
 */
public class ConfigurationHandler implements PluginSettings.Storing {
  private static final Logger LOGGER = Logger.getLogger(ConfigurationHandler.class.getName());

  private String mFilePrefix;
  private String mName;
  private Properties mProp;

  public ConfigurationHandler(String name, String filePrefix) {
    mName = name;
    mFilePrefix = filePrefix;
  }

  public void loadData(DataDeserializer deserializer) throws IOException {
    String userDirectoryName = Settings.getUserSettingsDirName();
     File userDirectory = new File(userDirectoryName);
     File datFile = new File(userDirectory, "java."+mFilePrefix + ".dat");

     if (datFile.exists()) {
       ObjectInputStream in = null;
       try {
         in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(datFile), 0x4000));
         deserializer.read(in);
       }
       catch (IOException e) {
         File oldFile = new File(userDirectory, "java."+mFilePrefix + ".dat_old");
         
         if(oldFile.isFile()) {
           try {
             in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(oldFile), 0x4000));
             deserializer.read(in);
             LOGGER.severe("Data file '" + datFile.getAbsolutePath() + "' could not be read. Read old version instead '" +oldFile.getAbsolutePath() + "'");
           }
           catch(ClassNotFoundException e1) {
             throw new IOException("Could not read file "+datFile.getAbsolutePath(), e1);
           }
         }
         else {
           throw e;
         }
       }
       catch (ClassNotFoundException e) {
         throw new IOException("Could not read file "+datFile.getAbsolutePath(), e);
       }
       finally {
         if (in != null) {
           try { in.close(); } catch (IOException exc) {
             // ignore
           }
         }
       }
     }

  }

  public synchronized void storeData(final DataSerializer serializer) throws IOException {
    String userDirectoryName = Settings.getUserSettingsDirName();
    File userDirectory = new File(userDirectoryName);

    File tmpDatFile = new File(userDirectory, mFilePrefix + ".dat.temp");
    File datFile = new File(userDirectory, "java." + mFilePrefix + ".dat");
    File oldVersion = new File(userDirectory, "java." + mFilePrefix + ".dat_old");

    StreamUtilities.objectOutputStream(tmpDatFile,
        new ObjectOutputStreamProcessor() {
          public void process(ObjectOutputStream out) throws IOException {
            serializer.write(out);
            out.close();
          }
        });

    // Saving succeeded -> Delete the old file and rename the temp file
    if(oldVersion.isFile()) {
      oldVersion.delete();
    }
    
    datFile.renameTo(oldVersion);
    tmpDatFile.renameTo(datFile);
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
  public String getFileName() {
    return "java." + mFilePrefix + ".prop";
  }
  
  @Override
  public String toString() {
    return mName;
  }
}
