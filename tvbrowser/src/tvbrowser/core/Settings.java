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
package tvbrowser.core;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.ProgramFieldType;
import devplugin.ProgramFilter;
import devplugin.Version;
import tvbrowser.TVBrowser;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.contextmenu.SeparatorMenuItem;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.ParserException;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.core.filters.filtercomponents.SingleChannelFilterComponent;
import tvbrowser.core.plugin.DefaultSettings;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.protocolhandler.ProtocolHandler;
import tvbrowser.core.settings.DeferredFontProperty;
import tvbrowser.core.settings.JGoodiesThemeProperty;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.programinfo.ProgramInfo;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import tvbrowser.ui.programtable.ProgramTableScrollPane;
import tvbrowser.ui.settings.BlockedPluginArrayProperty;
import tvbrowser.ui.waiting.dlgs.CopyWaitingDlg;
import util.browserlauncher.Launch;
import util.exc.TvBrowserException;
import util.i18n.Localizer;
import util.io.IOUtilities;
import util.io.stream.InputStreamProcessor;
import util.io.stream.ObjectInputStreamProcessor;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;
import util.misc.OperatingSystem;
import util.misc.TextLineBreakerStringWidth;
import util.settings.BooleanProperty;
import util.settings.ByteProperty;
import util.settings.ChannelArrayProperty;
import util.settings.ChoiceProperty;
import util.settings.ColorProperty;
import util.settings.ContextMenuMouseActionArrayProperty;
import util.settings.ContextMenuMouseActionSetting;
import util.settings.DateProperty;
import util.settings.EncodedStringProperty;
import util.settings.FontProperty;
import util.settings.HiddenMessagesProperty;
import util.settings.IntArrayProperty;
import util.settings.IntProperty;
import util.settings.MinutesProperty;
import util.settings.PluginPictureSettings;
import util.settings.ProgramFieldTypeArrayProperty;
import util.settings.ProgramPanelSettings;
import util.settings.Property;
import util.settings.PropertyManager;
import util.settings.ShortProperty;
import util.settings.StringArrayProperty;
import util.settings.StringMapProperty;
import util.settings.StringProperty;
import util.settings.VariableIntProperty;
import util.settings.VersionProperty;
import util.settings.WindowSetting;
import util.ui.UiUtilities;
import util.ui.persona.Persona;
import util.ui.view.SplitViewProperty;

/**
 * The Settings class provides access to the settings of the whole application
 * (except the plugins).
 *
 * @author Martin Oberhauser
 */
public class Settings {


  public static final String INFO_ID = "info.id";
  public static final String PICTURE_ID = "picture.id";
  private static final short INFO_DIALOG_WAITING_TIME = 1500;
  
  private static final HashMap<String, Boolean> RESTART_MAP = new HashMap<String, Boolean>();
  private static final ArrayList<ChangeListener> RESTART_LISTENERS = new ArrayList<ChangeListener>();

  public static void addRestartInfoListener(final ChangeListener cl) {
    RESTART_LISTENERS.add(cl);
  }
  
  public static void removeRestartInfoListener(final ChangeListener cl) {
    RESTART_LISTENERS.remove(cl);
  }
  
  public static void setRestartInfo(final String source, final boolean needsRestart) {
    if(needsRestart) {
      RESTART_MAP.put(source, true);
    }
    else {
      RESTART_MAP.remove(source);
    }
    
    for(ChangeListener cl : RESTART_LISTENERS) {
      cl.stateChanged(new ChangeEvent(source));
    }
  }
  
  public static boolean isRestartNeeded() {
    return !RESTART_MAP.isEmpty();
  }
  
  private static final Logger mLog = Logger
      .getLogger(Settings.class.getName());

  private static DefaultSettings mDefaultSettings = new DefaultSettings();

  private static final long PROXY_PASSWORD_SEED = 6528587292713416704L;

  private static final String SETTINGS_FILE = "settings.prop";
  private static final String DEFAULT_USER_DIR = ".tvbrowser";
  private static final String WINDOW_SETTINGS_FILE = "window.settings.dat";

  private static String DEFAULT_FONT_NAME = "Dialog";
  private static Font DEFAULT_PROGRAMTITLEFONT = new VariableFontSizeFont(DEFAULT_FONT_NAME,
      Font.BOLD, 0);
  private static Font DEFAULT_PROGRAMINFOFONT = new VariableFontSizeFont(DEFAULT_FONT_NAME,
      Font.PLAIN, -1);
  private static final Font DEFAULT_CHANNELNAMEFONT = new VariableFontSizeFont(
      DEFAULT_FONT_NAME, Font.BOLD, 0);
  private static Font DEFAULT_PROGRAMTIMEFONT = new VariableFontSizeFont(DEFAULT_FONT_NAME,
      Font.BOLD, 0);

  private static PropertyManager PROP = new PropertyManager();

  private static boolean mShowWaiting;
  private static boolean mShowSettingsCopyWaiting;

  private static HashMap<String,WindowSetting> mWindowSettings;

  private static boolean mCopyToSystem = false;
  
  private static ArrayList<ChangeListener> mListListenerFontChange = new ArrayList<>();
  
  public static void addFontChangeListener(ChangeListener listener) {
    if(!mListListenerFontChange.contains(listener)) {
      mListListenerFontChange.add(listener);
    }
  }
  
  public static void removeFontChangeListener(ChangeListener listener) {
    mListListenerFontChange.remove(listener);
  }

 /**
   * Returns the Default-Settings. These Settings are stored in the mac, windows
   * and linux.properties-Files
   *
   * @return Default-Settings
   */
  public static DefaultSettings getDefaultSettings() {
    return mDefaultSettings;
  }

  /**
   * Enables the export
   */
  public static void copyToSystem() {
    final File currentSettingsDir = new File(getUserSettingsDirName());
    final File currentTvDataDir = new File(getDefaultTvDataDir());

    mCopyToSystem = MainFrame.getInstance().getUserRequestCopyToSystem();

    if(mCopyToSystem) {
      Properties prop = new Properties();

      if(OperatingSystem.isMacOs()) {
        prop.setProperty("userdir","${user.home}/Library/Preferences/TV-Browser");
        prop.setProperty("tvdatadir","${user.home}/Library/Application Support/TV-Browser/tvdata");
        prop.setProperty("pluginsdir","${user.home}/Library/Application Support/TV-Browser/plugins");
      }
      else if(OperatingSystem.isLinux()) {
        prop.setProperty("userdir","${user.home}/.config/tvbrowser");
        prop.setProperty("tvdatadir","${user.home}/.config/tvbrowser/tvdata");
      }
      else if(OperatingSystem.isWindows()) {
        prop.setProperty("userdir","${user.appdata}/TV-Browser");
        prop.setProperty("tvdatadir","${user.appdata}/TV-Browser/tvdata");
      }

      mDefaultSettings = new DefaultSettings(prop);

      final File targetSettingsDir = new File(getUserSettingsDirName());
      final File targetTvDataDir = new File(getDefaultTvDataDir());

      if(new File(getUserSettingsDirName(),SETTINGS_FILE).isFile()) {
        String[] options = {MainFrame.LOCALIZER.msg("continue","Continue"),
                            MainFrame.LOCALIZER.msg("stop","Cancel copying now")};
        String title = MainFrame.LOCALIZER.msg("copyToSystemTitleWarning","Settings already exists");
        String msg = MainFrame.LOCALIZER.msg("copyToSystemWarningMsg","Settings already exist in the system settings directory!\nIf you continue the current settings will be overwritten!");

        mCopyToSystem = JOptionPane.showOptionDialog(MainFrame.getInstance(),msg,title,JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[1]) == JOptionPane.YES_OPTION;
      }
      else if(!targetSettingsDir.isDirectory()){
        mCopyToSystem = targetSettingsDir.mkdirs();
      }

      if(!targetTvDataDir.isDirectory()) {
        mCopyToSystem = targetTvDataDir.mkdirs();
      }

      if(mCopyToSystem) {
        try {
          final JFrame f = new JFrame(IOUtilities.getGraphicsConfigurationForFrame());
          final CopyWaitingDlg waiting = new CopyWaitingDlg(f, CopyWaitingDlg.EXPORT_SETTINGS_MSG);
          
          mLog.info("Copy settings and TV data from TV-Browser transportable to system");

          mShowWaiting = true;

          Thread copyDataThread = new Thread("Copy TV data directory") {
            public void run() {
              try {
                IOUtilities.copy(currentSettingsDir.listFiles(new FilenameFilter() {
                  public boolean accept(File dir, String name) {
                    return !name.equalsIgnoreCase("tvdata")
                        && !name.equals(targetSettingsDir.getName())
                        && !name.equalsIgnoreCase("backup")
                        && !name.equalsIgnoreCase("lang")
                        && !name.equals(".lock");
                  }
                }), targetSettingsDir);
                sleep(5000);
                IOUtilities.copy(currentTvDataDir.listFiles(), targetTvDataDir, true);
              }catch(Exception e) {}

              mShowWaiting = false;
              waiting.dispose();
              f.dispose();
            }
          };
          copyDataThread.start();

          waiting.setVisible(mShowWaiting);
        }catch(Exception e) {
          mCopyToSystem = false;
        }
      }
    }
  }

  /**
   * @return The user directory. (e.g.: ~/.tvbrowser/)
   */
  public static String getUserDirectoryName() {
    String dir = new StringBuilder(System.getProperty("user.home")).append(
        File.separator).append(DEFAULT_USER_DIR).toString();
    return (TVBrowser.isTransportable() && !mCopyToSystem) ? new File("settings").getAbsolutePath() : mDefaultSettings.getProperty("userdir", dir);
  }

  public static String getOSLibraryDirectoryName() {
  	if (OperatingSystem.isMacOs()) {
  		return "/Library/Application Support/TV-Browser/";
  	}
    return "";
  }

  public static String getUserSettingsDirName() {
    String version = TVBrowser.getCurrentVersionString();

    if(version.toLowerCase().indexOf("nightly") != -1) {
      version = StringUtils.substringBefore(version,"-");
    }

    return new StringBuilder(getUserDirectoryName())
        .append(File.separator).append(version).toString();
  }

  /**
   * Store all settings. This method is called on quitting the application.
   * @param log If it should be written into the log.
   * @throws util.exc.TvBrowserException Exception while saving the settings
   */
  public static void storeSettings(boolean log) throws TvBrowserException {
    File f = new File(getUserSettingsDirName());
    if (!f.exists()) {
      f.mkdirs();
    }

    File settingsFile = new File(getUserSettingsDirName(), SETTINGS_FILE);
    File firstSettingsBackupFile = new File(getUserSettingsDirName(), SETTINGS_FILE+ "_backup1");
    File secondSettingsBackupFile = new File(getUserSettingsDirName(), SETTINGS_FILE+ "_backup2");

    // Create backup of settings file backup
    try {
      if(firstSettingsBackupFile.isFile()) {
        secondSettingsBackupFile.delete();
        firstSettingsBackupFile.renameTo(secondSettingsBackupFile);
      }
    }catch(Exception e) {}

    try {
      PROP.writeToFile(settingsFile);

      try {
        if(settingsFile.isFile()) {
          IOUtilities.copy(settingsFile,firstSettingsBackupFile);
        }
      }catch (Exception e) {}

    } catch (IOException exc) {
      throw new TvBrowserException(Settings.class, "error.1",
          "Error when saving settings!\n({0})", settingsFile.getAbsolutePath(),
          exc);
    }

    storeWindowSettings(log);
  }

  /**
   * Stores the window settings for this plugin
   * @param log 
   */
  private static void storeWindowSettings(boolean log) {
    if(log) {
      mLog.info("Storing window settings");
    }
    File windowSettingsFile = new File(Settings.getUserSettingsDirName(),
        WINDOW_SETTINGS_FILE);
    StreamUtilities.objectOutputStreamIgnoringExceptions(windowSettingsFile,
        new ObjectOutputStreamProcessor() {
          public void process(ObjectOutputStream out) throws IOException {
            out.writeInt(1); // write version

            out.writeInt(mWindowSettings.size());

            for(String key : mWindowSettings.keySet()) {
              WindowSetting setting = mWindowSettings.get(key);

              if(setting != null) {
                out.writeUTF(key);
                mWindowSettings.get(key).saveSettings(out);
              }
            }

            out.close();
          }
        });
  }

  private static void startImportWaitingDlg() {
    mShowSettingsCopyWaiting = true;

    new Thread("settings import info thread") {
      public void run() {
        try {
          sleep(INFO_DIALOG_WAITING_TIME);

          if(mShowSettingsCopyWaiting) {
            final CopyWaitingDlg waiting = new CopyWaitingDlg(new JFrame(),CopyWaitingDlg.IMPORT_SETTINGS_MSG);

            new Thread("settings import waiting thread") {
              public void run() {
                while(mShowSettingsCopyWaiting) {
                  try {
                    sleep(200);
                  } catch (InterruptedException e1) {
                    e1.printStackTrace();
                  }
                }

                waiting.setVisible(false);
              }
            }.start();

            waiting.setVisible(mShowSettingsCopyWaiting);
          }
        } catch (InterruptedException e) {
          // ignore
        }
      }
    }.start();
  }

  /**
   * Reads the settings from settings file. If there is no settings file,
   * default settings are used.
   */
  public static void loadSettings() {
    initializeAllSubClasses();
    String oldDirectoryName = System.getProperty("user.home", "")
        + File.separator + ".tvbrowser";
    String newDirectoryName = getUserSettingsDirName();
    
    File settingsFile = new File(newDirectoryName, SETTINGS_FILE);
    File firstSettingsBackupFile = new File(getUserSettingsDirName(), SETTINGS_FILE+ "_backup1");
    File secondSettingsBackupFile = new File(getUserSettingsDirName(), SETTINGS_FILE+ "_backup2");

    if (settingsFile.exists() || firstSettingsBackupFile.exists() || secondSettingsBackupFile.exists()) {
      try {
        PROP.readFromFile(settingsFile);

        if(((PROP.getProperty("subscribedchannels") == null || PROP.getProperty("subscribedchannels").trim().length() < 1) && (PROP.getProperty("channelsWereConfigured") != null && PROP.getProperty("channelsWereConfigured").equals("true")) )
            && (firstSettingsBackupFile.isFile() || secondSettingsBackupFile.isFile())) {
          throw new IOException();
        }
        else {
          mLog.info("Using settings from file " + settingsFile.getAbsolutePath());
        }
      } catch (IOException evt) {

        if(firstSettingsBackupFile.isFile() || secondSettingsBackupFile.isFile()) {
          Localizer localizer = Localizer.getLocalizerFor(Settings.class);
          
          if(UiUtilities.showConfirmDialogOnMouseScreen(localizer.msg("settingBroken","Settings file broken.\nWould you like to load the backup file?\n\n(If you select No, the\ndefault settings are used)"),Localizer.getLocalization(Localizer.I18N_ERROR),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
            boolean loadSecondBackup = !firstSettingsBackupFile.isFile();

            if(firstSettingsBackupFile.isFile()) {
              try {
                PROP.readFromFile(firstSettingsBackupFile);

                if((PROP.getProperty("subscribedchannels") == null || PROP.getProperty("subscribedchannels").trim().length() < 1) && secondSettingsBackupFile.isFile()) {
                  loadSecondBackup = true;
                }
                else {
                  mLog.info("Using settings from file " + firstSettingsBackupFile.getAbsolutePath());
                  loadSecondBackup = false;
                }
              }catch(Exception e) {
                loadSecondBackup = true;
              }
            }
            if(loadSecondBackup && secondSettingsBackupFile.isFile()) {
              try {
                PROP.readFromFile(secondSettingsBackupFile);
                mLog.info("Using settings from file " + secondSettingsBackupFile.getAbsolutePath());
                loadSecondBackup = false;
              }catch(Exception e) {
                loadSecondBackup = true;
              }
            }

            if(loadSecondBackup) {
              mLog.info("Could not read settings - using default user settings");
            } else {
              try {
                loadWindowSettings();
                storeSettings(true);
              }catch(Exception e) {}
            }
          }
        } else {
          mLog.info("Could not read settings - using default user settings");
        }
      }
    }
    /*
     * If the settings file doesn't exist, we try to import the settings created
     * by a previous version of TV-Browser
     */
    else if (!oldDirectoryName.equals(newDirectoryName)) {
      File oldDir = null;

      ArrayList<String> directories = new ArrayList<>();
      directories.add(getUserDirectoryName());
      
      if(Launch.isOsWindowsNtBranch()) {
        File test = new File(System.getenv("appdata"),"TV-Browser");

        if(test.isDirectory()) {
          directories.add(test.getAbsolutePath());
        }
        
        directories.add(System.getProperty("user.home") + "/TV-Browser");
      }
      else if(Launch.getOs() == Launch.OS_LINUX) {
        directories.add(System.getProperty("user.home") + File.separator + ".config" + File.separator + "tvbrowser");
      }
      else if(Launch.isMacOs()) {
        directories.add(System.getProperty("user.home") + "/Library/Preferences/TV-Browser");
      }
      
      directories.add(System.getProperty("user.home") + File.separator + DEFAULT_USER_DIR);
      
      for(int j = 0; j < directories.size(); j++) {
        mLog.info("Search for settings import in: '" + directories.get(j) + "'");
        oldDir = findNewestOldVersionDir(directories.get(j), oldDirectoryName, j != 0, TVBrowser.isTransportable());
        
        if(oldDir != null) {
          break;
        }
      }

      File pluginsDir = null;
      
      if(TVBrowser.isTransportable()) {
        mLog.info("TV-Browser ist transportable version, show import dialog: '" 
           + (oldDir != null && oldDir.isDirectory() && oldDir.exists() && !oldDir.getAbsolutePath().startsWith(new File("settings").getAbsolutePath()))
           + "', show import directory selection dialog: '" + (oldDir == null || !oldDir.isDirectory() || !oldDir.exists()) + "'");
        if (oldDir != null && oldDir.isDirectory() && oldDir.exists() && !oldDir.getAbsolutePath().startsWith(new File("settings").getAbsolutePath())) {
          try {
            UIManager.setLookAndFeel(UiUtilities.getDefaultLookAndFeelClassName(false));
          }catch(Exception e) { /*ignore*/}
  
          String[] options = {MainFrame.LOCALIZER.msg("import","Import settings"),
              MainFrame.LOCALIZER.msg("importTransportable","Select import directory"),
              MainFrame.LOCALIZER.msg("configureNew","Create new configuration")};
          String title = MainFrame.LOCALIZER.msg("importInfoTitle","Import settings?");
          String msg = MainFrame.LOCALIZER.msg("importInfoMsg","TV-Browser has found settings for import.\nShould the settings be imported now?");
  
          
          int answer = UiUtilities.showOptionDialogOnMouseScreen(msg,title,JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[0]);
          
          if(answer == JOptionPane.CANCEL_OPTION) {
            oldDir = null;
          }
          else if(answer == JOptionPane.NO_OPTION) {
            oldDir = loadExternalSettings();
            
            if(oldDir != null) {
              oldDir = findNewestOldVersionDir(oldDir.getAbsolutePath(), oldDirectoryName, true, false);
            }
          }
          else {
            oldDir = findNewestOldVersionDir(oldDir.getParentFile().getAbsolutePath(), oldDirectoryName, true, false);
            
            if(OperatingSystem.isMacOs()) {
              pluginsDir = new File(System.getProperty("user.home"),"Library/Application Support/TV-Browser/plugins");
            }
          }
        }
        else if(oldDir == null || !oldDir.isDirectory() || !oldDir.exists()) {
          try {
            UIManager.setLookAndFeel(UiUtilities.getDefaultLookAndFeelClassName(false));
          }catch(Exception e) { /*ignore*/}
          
          String[] options = {MainFrame.LOCALIZER.msg("importTransportable","Select import directory"),
              MainFrame.LOCALIZER.msg("configureNew","Create new configuration")};
          String title = MainFrame.LOCALIZER.msg("importInfoTitle","Import settings?");
          String msg = MainFrame.LOCALIZER.msg("importInfoMsgTransportable","No settings were found on the system.\nDo you want to select the directory of another\ntransportable version for import of settings?");
          
          if(UiUtilities.showOptionDialogOnMouseScreen(msg,title,JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[1]) == JOptionPane.YES_OPTION) {
            oldDir = loadExternalSettings();
            
            if(oldDir != null) {
              oldDir = findNewestOldVersionDir(oldDir.getAbsolutePath(), oldDirectoryName, true, false);
            }
          }
          else {
            oldDir = null;
          }
        }
      }

      if (oldDir != null && oldDir.isDirectory() && oldDir.exists()) {
        File testFile = new File(oldDir,"settings.prop");
        
        startImportWaitingDlg();
        mLog.info("Try to load settings from a previous version of TV-Browser: " + oldDir);

        final File newDir = new File(getUserSettingsDirName());

        File oldTvDataDir = null;
        File oldIconsDir = null;
        File oldInfoIconsDir = null;

        final Properties prop = new Properties();

        try {
          StreamUtilities.inputStream(testFile, new InputStreamProcessor() {
            public void process(InputStream input) throws IOException {
              prop.load(input);
            }
          });
        }catch(Exception e) {e.printStackTrace();}

        String versionString = prop.getProperty("version",null);
        Version testVersion = null;

        if(versionString != null && !versionString.contains(";")) {
          try {
            int asInt = Integer.parseInt(versionString);
            int major = asInt / 100;
            int minor = asInt % 100;
            testVersion = new Version(major,minor);
          }
          catch(NumberFormatException exc) {
            // Ignore
          }
        }
        else if(versionString != null && versionString.contains(";")) {
          String[] parts = versionString.split(";");
          
          testVersion = new devplugin.Version(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),parts.length>3 ? parts[3].equals("true"): true);
        }
        
        String temp = prop.getProperty("dir.tvdata", null);
        
        boolean versionTest = !TVBrowser.isTransportable() && Launch.isOsWindowsNtBranch() && testVersion != null && testVersion.isOlderThan(new Version(3,0,true))
                               && (temp == null || temp.replace("/","\\").equals(System.getProperty("user.home")+"\\TV-Browser\\tvdata"));
        
        if((TVBrowser.isTransportable() || versionTest || !oldDir.getParentFile().equals(newDir.getParentFile()))
            && !(new File(getUserDirectoryName(),"tvdata").isDirectory())) {
          try {
            if(temp != null) {
              oldTvDataDir = new File(temp);
            } else if(new File(oldDir, "tvdata").isDirectory()) {
              oldTvDataDir = new File(oldDir, "tvdata");
            } else if(new File(oldDir.getParent(), "tvdata").isDirectory()) {
              oldTvDataDir = new File(oldDir.getParent(), "tvdata");
            } else if(OperatingSystem.isMacOs()) {
              File test = new File(System.getProperty("user.home"),"Library/Application Support/TV-Browser/tvdata");
              
              if(test.isDirectory()) {
                oldTvDataDir = test;
              }
            }

          }catch(Exception e) {}
        }
        
        if((TVBrowser.isTransportable())) {
          if(!(new File(getUserDirectoryName(),"icons").isDirectory()) && new File(oldDir.getParent(),"icons").isDirectory()) {
            oldIconsDir = new File(oldDir.getParent(),"icons");
          }
          if(!(new File(getUserDirectoryName(),"infothemes").isDirectory()) && new File(oldDir.getParent(),"infothemes").isDirectory()) {
            oldInfoIconsDir = new File(oldDir.getParent(),"infothemes");
          }
        }
        
        if (newDir.mkdirs()) {
          try {
            IOUtilities.copy(oldDir.listFiles(new FilenameFilter() {
              public boolean accept(File dir, String name) {
                return !name.equalsIgnoreCase("tvdata")
                    && !name.equals(newDir.getName())
                    && !name.equalsIgnoreCase("backup")
                    && !name.equalsIgnoreCase("lang");
              }
            }), newDir);

            if(pluginsDir != null && pluginsDir.isDirectory()) {
              File target = new File(newDir,"plugins");
              
              if(!target.isDirectory()) {
                target.mkdirs();
              }
              
              IOUtilities.copy(pluginsDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File f) {
                  return f.isFile() && f.getName().toLowerCase().endsWith(".jar");
                }
              }), target);
            }
            
            mShowSettingsCopyWaiting = false;
            
            mLog.info("settings from previous version copied successfully");
            File newSettingsFile = new File(newDir, SETTINGS_FILE);
            PROP.readFromFile(newSettingsFile);
            mLog.info("settings from previous version read successfully");

            /*
             * This is the .tvbrowser dir, if there are settings form version
             * 1.0 change the name to start with java.
             */
            if (oldDirectoryName.equals(oldDir.getAbsolutePath())) {
              File[] settings = newDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                  return (name.toLowerCase().endsWith(".prop") && name
                           .toLowerCase().indexOf("settings") == -1)
                         || (name.toLowerCase().endsWith(".dat") && name
                           .toLowerCase().indexOf("tv-data-inventory") == -1);
                }
              });

              boolean version1 = false;

              if (settings != null) {
                for (int i = 0; i < settings.length; i++) {
                  String name = "java." + settings[i].getName();

                  if (!settings[i].getName().toLowerCase().startsWith("java.")) {
                    version1 = true;
                    settings[i].renameTo(new File(settings[i].getParent(), name));
                  }
                }
              }

              if (version1
                  && !(new File(oldDirectoryName, newDir.getName()))
                      .isDirectory()) {
                oldDir.renameTo(new File(System.getProperty("user.home", "")
                    + File.separator + "tvbrowser_BACKUP"));
              }
            }

            /*
             * Test if and copy TV data for the portable version.
             */
            if(oldTvDataDir != null && oldTvDataDir.isDirectory()) {
              final File targetDir = new File(getUserDirectoryName(),"tvdata");

              if(!oldTvDataDir.equals(targetDir)) {
                targetDir.mkdirs();

                final CopyWaitingDlg waiting = new CopyWaitingDlg(new JFrame(), versionTest ? CopyWaitingDlg.APPDATA_MSG : CopyWaitingDlg.IMPORT_MSG);

                mShowWaiting = true;

                final File srcDir = oldTvDataDir;

                Thread copyDataThread = new Thread("Copy TV data directory") {
                  public void run() {
                    try {
                      IOUtilities.copy(srcDir.listFiles(), targetDir, true);
                    }catch(Exception e) {}

                    mShowWaiting = false;
                    waiting.setVisible(false);
                  }
                };
                copyDataThread.start();

                waiting.setVisible(mShowWaiting);
              }
            }
            
            /*
             * Copy old icons directory to new icons directory.
             */
            if(oldIconsDir != null && oldIconsDir.isDirectory()) {
              final File targetDir = new File(getUserDirectoryName(),"icons");

              if(!oldIconsDir.equals(targetDir)) {
                targetDir.mkdirs();

                final CopyWaitingDlg waiting = new CopyWaitingDlg(new JFrame(), versionTest ? CopyWaitingDlg.APPDATA_MSG : CopyWaitingDlg.IMPORT_MSG);

                mShowWaiting = true;

                final File srcDir = oldIconsDir;

                Thread copyIconsThread = new Thread("Copy icons directory") {
                  public void run() {
                    try {
                      IOUtilities.copy(srcDir.listFiles(), targetDir, true);
                    }catch(Exception e) {}

                    mShowWaiting = false;
                    waiting.setVisible(false);
                  }
                };
                copyIconsThread.start();

                waiting.setVisible(mShowWaiting);
              }
            }
            
            /*
             * Copy old icons directory to new icons directory.
             */
            if(oldInfoIconsDir != null && oldInfoIconsDir.isDirectory()) {
              final File targetDir = new File(getUserDirectoryName(),"infothemes");

              if(!oldInfoIconsDir.equals(targetDir)) {
                targetDir.mkdirs();

                final CopyWaitingDlg waiting = new CopyWaitingDlg(new JFrame(), versionTest ? CopyWaitingDlg.APPDATA_MSG : CopyWaitingDlg.IMPORT_MSG);

                mShowWaiting = true;

                final File srcDir = oldInfoIconsDir;

                Thread copyInfoIconsThread = new Thread("Copy info icons directory") {
                  public void run() {
                    try {
                      IOUtilities.copy(srcDir.listFiles(), targetDir, true);
                    }catch(Exception e) {}

                    mShowWaiting = false;
                    waiting.setVisible(false);
                  }
                };
                copyInfoIconsThread.start();

                waiting.setVisible(mShowWaiting);
              }
            }

            /*
             * Test if a settings file exist in the user directory, move the
             * settings to backup.
             */
            if ((new File(getUserDirectoryName(), SETTINGS_FILE)).isFile()) {
              final File backupDir = new File(getUserDirectoryName(), "BACKUP");
              if (backupDir.mkdirs()) {
                mLog.info("moving the settings of old settings dir to backup");
                File[] files = oldDir.listFiles(new FileFilter() {
                  public boolean accept(File pathname) {
                    return pathname.compareTo(newDir) != 0
                        && pathname.getName().compareToIgnoreCase("tvdata") != 0
                        && pathname.compareTo(backupDir) != 0;
                  }
                });

                if (files != null) {
                  for (File file : files) {
                    file.renameTo(new File(backupDir,file.getName()));
                  }
                }
              }
            }
          } catch (IOException e) {
            mLog.log(Level.WARNING, "Could not import user settings from '"
                + oldDir.getAbsolutePath() + "' to '"
                + newDir.getAbsolutePath() + "'", e);
          }
          
          if(testVersion != null && testVersion.isOlderThanOrEqualTo(new Version(4,24))) {
            final File oldReminderDat = new File(newDir,"reminder.dat");
            final File newReminderDat = new File(newDir,"java.reminderplugin.ReminderPlugin.dat");
            
            if(oldReminderDat.isFile() && !newReminderDat.isFile()) {
              oldReminderDat.renameTo(newReminderDat);
            }
          }
        } else {
          mLog.info("Could not create directory '" + newDir.getAbsolutePath()
              + "' - using default user settings");
        }
      } else {
        mLog
            .info("No previous version of TV-Browser found - using default user settings");
      }
    }
    mShowSettingsCopyWaiting = false;

    File settingsDir = new File(newDirectoryName);

    if (!settingsDir.exists()) {
      mLog.info("Creating " + newDirectoryName);
      settingsDir.mkdir();
    }

    loadWindowSettings();
    
    ((DeferredFontProperty)Fonts.PROGRAM_TITLE).resetDefault();
    ((DeferredFontProperty)Fonts.PROGRAM_INFO).resetDefault();
    ((DeferredFontProperty)Fonts.CHANNEL_NAME).resetDefault();
    ((DeferredFontProperty)Fonts.PROGRAM_TIME).resetDefault();
    
    if(ProgramTable.COLUMN_WIDTH.getInt() < ProgramTable.COLUMN_WIDTH_MIN) {
      ProgramTable.COLUMN_WIDTH.setInt(ProgramTable.COLUMN_WIDTH_MIN);
    }
    
    updateColors();
  }
  
  // make sure all static settings classes are initialied for access
  private static void initializeAllSubClasses() {
    Other.initialize();
    Buttons.initialize();
    CenterPanels.initialize();
    IconAndNames.initialize();
    Channels.initialize();
    ContextMenu.initialize();
    DataPostProcessing.initialize();
    Directories.initialize();
    Fonts.initialize();
    General.initialize();
    Locales.initialize();
    LookAndFeel.initialize();
    Markings.initialize();
    Mouse.initialize();
    Network.initialize();
    Pictures.initialize();
    Plugins.initialize();
    ProgramPanel.initialize();
    ProgramTable.initialize();
    Proxy.initialize();
    ToolBar.initialize();
    Tray.initialize();
    WebBrowser.initialize();
    Window.initialize();
    Data.initialize();
  }
  
  private static Color[] mHighlightingColors;
  
  public static void updateColors() {
    int[] colors = Markings.HIGHLIGHTING_COLORS.getIntArray();
    mHighlightingColors = new Color[colors.length];
    
    for(int i = 0; i < colors.length; i++) {
      mHighlightingColors[i] = new Color(colors[i],true);
    }
  }
  
  public static int getHighlightingPriorityMaximum() {
    return mHighlightingColors.length-1;
  }
  
  public static Color getHighlightingColorForPriority(final int priority) throws IndexOutOfBoundsException {
    return mHighlightingColors[priority];
  }
  
  public static void updateContextMenuSettings() {
    ArrayList<ContextMenuMouseActionSetting> leftSingleList = new ArrayList<ContextMenuMouseActionSetting>(2);
    
    if(PROP.getProperty("leftSingleClickIf") != null) {
      StringProperty propLeftSingleClickIf = new StringProperty(
          PROP, "leftSingleClickIf", ProgramInfo.getProgramInfoPluginId());      
      leftSingleList.add(new ContextMenuMouseActionSetting(ContextMenuManager.NO_MOUSE_MODIFIER_EX, propLeftSingleClickIf.getString(), ActionMenu.ID_ACTION_NONE));
      propLeftSingleClickIf.setString(propLeftSingleClickIf.getDefault());
    }
    if(PROP.getProperty("contextmenudefaultplugin") != null) {
      StringProperty propDoubleClickIf = new StringProperty(
          PROP, "contextmenudefaultplugin", ProgramInfo.getProgramInfoPluginId());
      Mouse.LEFT_DOUBLE_CLICK_IF_ARRAY.setContextMenuMouseActionArray(new ContextMenuMouseActionSetting[] {new ContextMenuMouseActionSetting(ContextMenuManager.NO_MOUSE_MODIFIER_EX, propDoubleClickIf.getString(), ActionMenu.ID_ACTION_NONE)});
      propDoubleClickIf.setString(propDoubleClickIf.getDefault());
    }
    if(PROP.getProperty("middleclickplugin") != null) {
      StringProperty propMiddleClickIf = new StringProperty(
          PROP, "middleclickplugin", ReminderPlugin.getReminderPluginId());
      Mouse.MIDDLE_SINGLE_CLICK_IF_ARRAY.setContextMenuMouseActionArray(new ContextMenuMouseActionSetting[] {new ContextMenuMouseActionSetting(ContextMenuManager.NO_MOUSE_MODIFIER_EX, propMiddleClickIf.getString(), ActionMenu.ID_ACTION_NONE)});
      propMiddleClickIf.setString(propMiddleClickIf.getDefault());
    }
    if(PROP.getProperty("middledoubleclickplugin") != null) {
      StringProperty propMiddleDoubleClickIf = new StringProperty(
          PROP, "middledoubleclickplugin", FavoritesPlugin.getFavoritesPluginId());
      Mouse.MIDDLE_DOUBLE_CLICK_IF_ARRAY.setContextMenuMouseActionArray(new ContextMenuMouseActionSetting[] {new ContextMenuMouseActionSetting(ContextMenuManager.NO_MOUSE_MODIFIER_EX, propMiddleDoubleClickIf.getString(), ActionMenu.ID_ACTION_NONE)});
      propMiddleDoubleClickIf.setString(propMiddleDoubleClickIf.getDefault());
    }
    if(PROP.getProperty("leftSingleCtrlClickIf") != null) {
      StringProperty propLeftSingleCtrlClickIf = new StringProperty(
          PROP, "leftSingleCtrlClickIf", null);
      leftSingleList.add(new ContextMenuMouseActionSetting(MouseEvent.CTRL_DOWN_MASK, propLeftSingleCtrlClickIf.getString(), ActionMenu.ID_ACTION_NONE));
      propLeftSingleCtrlClickIf.setString(propLeftSingleCtrlClickIf.getDefault());
    }
    
    if(!leftSingleList.isEmpty()) {
      Mouse.LEFT_SINGLE_CLICK_IF_ARRAY.setContextMenuMouseActionArray(leftSingleList.toArray(new ContextMenuMouseActionSetting[leftSingleList.size()]));
    }
    
    try {
      storeSettings(true);
    } catch (TvBrowserException e) {
      e.printStackTrace();
    }
    
    ContextMenuManager.getInstance().init();
  }
  
  
  private static File findNewestOldVersionDir(String directory, String oldDirectoryName, boolean includeCurrent, boolean quiet) {
    File oldDir = null;
    File testFile = null;
    String[] allVersions = TVBrowser.getAllVersionStrings();
    
    final ArrayList<File> directories = new ArrayList<File>();
    
    for (int i = (includeCurrent ? 0 : 1); i < allVersions.length; i++) {
      testFile = new File(directory + File.separator + allVersions[i], SETTINGS_FILE);
      
      if(testFile.isFile()) {
        oldDir = testFile;
        directories.add(oldDir);
      }
    }

    if(oldDir == null) {
      testFile = new File(directory, SETTINGS_FILE);

      if(testFile.isFile()) {
        oldDir = new File(directory);
      } else {
        testFile = new File(oldDirectoryName, SETTINGS_FILE);

        if(testFile.isFile()) {
          oldDir = new File(oldDirectoryName);
        }
      }
    }
    else if(!directories.isEmpty()) {
      Collections.sort(directories, new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
          int result = 0;
          
          if(o1.lastModified() > o2.lastModified()) {
            result = -1;
          }
          else if(o1.lastModified() < o2.lastModified()) {
            result = 1;
          }
          
          return result;
        }
      });
      
      long threeMonthsBefore = System.currentTimeMillis() - 3 * 30 * 24 * 60 * 60000l;
      
      for(int i = directories.size()-1; i >= 1; i--) {
        if(directories.get(i).lastModified() < threeMonthsBefore) {
          directories.remove(i);
        }
      }
      
      if(directories.size() > 1 && !quiet) {
        Localizer localizer = Localizer.getLocalizerFor(Settings.class);
        final ButtonGroup bg = new ButtonGroup();
        final JRadioButton[] versions = new JRadioButton[directories.size()];
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        final Object[] message = new Object[versions.length+1];
        message[0] = localizer.msg("selectImportDirectoryMessage", "Settings were found of different recently used versions of TV-Browser.\nPlease select the version to import the settings from.\n(If you are unsure, just accept the preselection with OK.)\n\n"); 
        
        for(int i = 0; i < versions.length; i++) {
          final File dir = directories.get(i);
          versions[i] = new JRadioButton(localizer.msg("selectImportDirectoryInfo", "{0} (last used: {1})",dir.getParentFile().getName(),dateFormat.format(new java.util.Date(dir.lastModified()))));
          bg.add(versions[i]);
          message[i+1] = versions[i];
        }
        
        versions[0].setSelected(true);
        
        UiUtilities.showMessageDialogOnMouseScreen(message, localizer.msg("selectImportDirectoryTitle", "TV-Browser - Select settings to import"), JOptionPane.QUESTION_MESSAGE);
        
        for(int i = 0; i < versions.length; i++) {
          if(versions[i].isSelected()) {
            oldDir = directories.get(i).getParentFile();
            break;
          }
        }
      }
      else if(!directories.isEmpty()) {
        oldDir = directories.get(0).getParentFile();
      }
    }
    
    return oldDir;
  }
  
  private static File loadExternalSettings() {
    String msg = MainFrame.LOCALIZER.msg("importTransportableInfo", "To import settings of another transportable version select the program\ndirectory of that other transportable version in the next setp.");
    String title = MainFrame.LOCALIZER.msg("importTransportableTitle", "Import settings from transportable version");
    UiUtilities.showMessageDialogOnMouseScreen(msg, title, JOptionPane.INFORMATION_MESSAGE);
    
    JFileChooser chooseDir = new JFileChooser(System.getProperty("user.home"));
    chooseDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooseDir.setDialogTitle(title);
    chooseDir.setMultiSelectionEnabled(false);
    
    int selection = JFileChooser.CANCEL_OPTION;
    
    do {
      if(selection == JFileChooser.APPROVE_OPTION) {
        String msg2 = MainFrame.LOCALIZER.msg("importTransportableError", "You've selected a directory that don't contains a transportable TV-Browser.\nWould you like to try again?");
        String title2 = MainFrame.LOCALIZER.msg("importTransportableErrorTitle", "Wrong directory selected");
        
        if(UiUtilities.showConfirmDialogOnMouseScreen(msg2, title2, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
          selection = JFileChooser.CANCEL_OPTION;
          break;
        }
      }
      
      selection = chooseDir.showDialog(null, Localizer.getLocalization(Localizer.I18N_SELECT));
    }while(selection != JFileChooser.CANCEL_OPTION && !(chooseDir.getSelectedFile() != null && chooseDir.getSelectedFile().isDirectory() && new File(chooseDir.getSelectedFile(),"settings").isDirectory() && new File(chooseDir.getSelectedFile(),"tvbrowser.jar").isFile()));
    
    if(selection == JFileChooser.APPROVE_OPTION && chooseDir.getSelectedFile() != null) {
      return new File(chooseDir.getSelectedFile(),"settings");
    }
    
    return null;
  }

  private static void loadWindowSettings() {
    File windowSettingsFile = new File(Settings.getUserSettingsDirName(),
        WINDOW_SETTINGS_FILE);

    if (windowSettingsFile.isFile() && windowSettingsFile.canRead()) {
      try {
        StreamUtilities.objectInputStream(windowSettingsFile,
            new ObjectInputStreamProcessor() {
              public void process(ObjectInputStream in) throws IOException {
                if (in.available() > 0) {
                  in.readInt(); // read version

                  int n = in.readInt(); // read number of window settings

                  mWindowSettings = new HashMap<String, WindowSetting>(n);

                  for (int i = 0; i < n; i++) {
                    mWindowSettings.put(in.readUTF(), new WindowSetting(in));
                  }
                }

                in.close();
              }
            });
      }catch(Exception e) {
        // propably defect settings, create new settings
        mWindowSettings = null;
      }
    }

    if (mWindowSettings == null) {
      mWindowSettings = new HashMap<String, WindowSetting>(1);
    }
  }

  public static void handleChangedSettings() {
    updateColors();
    
    Property[] propArr;
    
    MainFrame mainFrame = MainFrame.getInstance();

    propArr = new Property[] { ProgramPanel.BORDER_ON_AIR_PROGRAMS_SHOW,
        Markings.USES_EXTRA_SPACE_FOR_MARK_ICONS,
        Markings.WITH_MARKINGS_SHOWING_BORDER, Markings.MARK_PRIORITY_DEFAULT,
        ProgramPanel.COLOR_ON_AIR_LIGHT, ProgramPanel.COLOR_ON_AIR_DARK, ProgramPanel.COLOR_FOREGROUND,
        ProgramTable.COLOR_BACKGROUND_SINGLE, ProgramPanel.TRANSPARENCY_ALLOW, CenterPanels.ALWAYS_SHOW_TAB_BAR_FOR_CENTER_PANEL,
        ProgramPanel.ORIGINIAL_TITLES_SHOW, Markings.HIGHLIGHTING_COLORS};

    mainFrame.updateCenterPanels();
    
    Property[] propArrFont = new Property[] { Fonts.PROGRAM_TITLE, Fonts.PROGRAM_INFO,
        Fonts.PROGRAM_TIME, Fonts.CHANNEL_NAME, Fonts.USE_DEFAULT,
        Fonts.ANTIALIASING_ENABLED, Fonts.PROGRAM_TEX_TLINE_GAP};
    
    boolean fontChanged = PROP.hasChanged(propArrFont);
    
    if(fontChanged) {
      for(ChangeListener listener : mListListenerFontChange) {
        listener.stateChanged(new ChangeEvent(Settings.class));
      }
    }
    
    if (fontChanged || PROP.hasChanged(propArr)) {
      util.ui.ProgramPanel.updateFonts();
      tvbrowser.ui.programtable.ChannelPanel.fontChanged();
      ProgramTableScrollPane scrollPane = mainFrame.getProgramTableScrollPane();
      scrollPane.forceRepaintAll();
    }

    propArr = new Property[] {Pictures.TYPE, Pictures.TIME_START,
        Pictures.TIME_END, Pictures.DESCRIPTION_SHOW, Pictures.PLUGIN_IDS,
        Pictures.DURATION, ProgramPanel.TITLE_CUT_LINES,
        ProgramPanel.TITLE_CUT, Pictures.DESCRIPTION_LINES,
        ProgramPanel.MAX_LINES, ProgramPanel.DESCRIPTION_LIMIT_BY_DURATION,
        ProgramPanel.DESCRIPTION_LIMIT_BY_DURATION_MINUTES, Pictures.BORDER_SHOW};

    if(PROP.hasChanged(propArr)) {
      mainFrame.getProgramTableScrollPane().forceRepaintAll();
    }

    if(PROP.hasChanged(ProgramPanel.HYPHENATION)) {
      TextLineBreakerStringWidth.resetHyphenator();
      mainFrame.getProgramTableScrollPane().forceRepaintAll();
    }
    
    if (PROP.hasChanged(ProgramTable.COLUMN_WIDTH)) {
      util.ui.ProgramPanel.updateColumnWidth();
      ProgramTableScrollPane scrollPane = mainFrame.getProgramTableScrollPane();
      scrollPane.setColumnWidth(ProgramTable.COLUMN_WIDTH.getInt());
      scrollPane.forceRepaintAll();
    }

    if (PROP.hasChanged(ProgramTable.LAYOUT)) {
      ProgramTableScrollPane scrollPane = mainFrame.getProgramTableScrollPane();
      scrollPane.getProgramTable().setProgramTableLayout(null);
      scrollPane.getProgramTable().updateBackground();
      scrollPane.forceRepaintAll();
    }

    if (PROP.hasChanged(Plugins.DEACTIVATED)) {
      mainFrame.updatePluginsMenu();
      mainFrame.updateToolbar();
    }

    propArr = new Property[] { ProgramTable.STYLE_BACKGROUND,
        ProgramTable.ONE_IMAGE_BACKGROUND, ProgramTable.TIME_BLOCK_SIZE, ProgramTable.TIME_BLOCK_BACKGROUND1,
        ProgramTable.TIME_BLOCK_BACKGROUND2, ProgramTable.TIME_BLOCK_SHOW_WEST,
        ProgramTable.TIME_BLOCK_WEST_IMAGE1, ProgramTable.TIME_BLOCK_WEST_IMAGE2,
        ProgramTable.TIME_OF_DAY_BACKGROUND_EDGE, ProgramTable.TIME_OF_DAY_BACKGROUND_EARLY,
        ProgramTable.TIME_OF_DAY_BACKGROUND_MIDDAY, ProgramTable.TIME_OF_DAY_BACKGROUND_AFTERNOON,
        ProgramTable.TIME_OF_DAY_BACKGROUND_EVENING };
    if (PROP.hasChanged(propArr)) {
      ProgramTableScrollPane scrollPane = mainFrame.getProgramTableScrollPane();
      scrollPane.getProgramTable().updateBackground();
      mainFrame.getProgramTableScrollPane().forceRepaintAll();
    }

    if(PROP.hasChanged(ProgramTable.TIME_BLOCK_SIZE)) {
      mainFrame.getProgramTableScrollPane().forceRepaintAll();
    }

    propArr = new Property[] { ToolBar.BUTTON_STYLE, ToolBar.BUTTONS,
        ToolBar.LOCATION, ToolBar.IS_VISIBLE, ToolBar.BIG_ICONS_USE};
    if (PROP.hasChanged(propArr)) {
      mainFrame.updateToolbar();
    }

    if (PROP.hasChanged(Buttons.TIME_BUTTONS)) {
      mainFrame.updateTimeButtons();
    }

    if (PROP.hasChanged(Channels.SUBSCRIBED)) {
      ChannelList.reload();
      DefaultProgramTableModel model = mainFrame.getProgramTableModel();
      model.setChannels(ChannelList.getSubscribedChannels());
      mainFrame.updateChannellist();
    }
    
    if(PROP.hasChanged(LookAndFeel.PERSONA_RANDOM) && !PROP.hasChanged(LookAndFeel.PERSONA_SELECTED)) {
      Persona.getInstance().applyPersona();
    }
    
    if(PROP.hasChanged(LookAndFeel.PERSONA_SELECTED)) {
      Persona.getInstance().applyPersona();
    }

    propArr = new Property[] { ProgramTable.START_OF_DAY,
        ProgramTable.END_OF_DAY };
    if (PROP.hasChanged(propArr)) {
      DefaultProgramTableModel model = mainFrame.getProgramTableModel();
      int startOfDay = ProgramTable.START_OF_DAY.getInt();
      int endOfDay = ProgramTable.END_OF_DAY.getInt();
      model.setTimeRange(startOfDay, endOfDay);
      model.setDate(mainFrame.getCurrentSelectedDate(), null, null);
    }

    propArr = new Property[] { ProgramPanel.ICON_PLUGINS,
        ProgramPanel.INFO_FIELDS, ProgramPanel.INFO_FIELDS_SEPARATORS,
        ProgramPanel.ICON_PLUGINS_ALTERNATIVE,
        ProgramPanel.INFO_FIELDS_ALTERNATIVE, ProgramPanel.INFO_FIELDS_SEPARATORS_ALTERNATIVE};
    if (PROP.hasChanged(propArr)) {
      // Force a recreation of the table content
      DefaultProgramTableModel model = mainFrame.getProgramTableModel();
      model.setDate(mainFrame.getCurrentSelectedDate(), null, null);
    }

    propArr = new Property[] {
        IconAndNames.SHOW_ICONS_IN_PROGRAM_TABLE, IconAndNames.SHOW_ICONS_IN_CHANNEL_LIST,
        IconAndNames.SHOW_NAMES_IN_PROGRAM_TABLE, IconAndNames.SHOW_NAMES_IN_CHANNEL_LIST,
        IconAndNames.SHOW_SORT_NUMBER_IN_PROGRAM_TABLE, IconAndNames.SHOW_SORT_NUMBER_IN_PROGRAM_LISTS};
    if (PROP.hasChanged(propArr)) {
      mainFrame.getProgramTableScrollPane().updateChannelPanel();
      mainFrame.updateChannelChooser();
    }

    if(PROP.hasChanged(Directories.TV_DATA)) {
      TvDataServiceProxyManager.getInstance().setTvDataDir(new File(Directories.TV_DATA.getString()));

      TvDataBase.getInstance().updateTvDataBase();
      TvDataBase.getInstance().checkTvDataInventory(TvDataBase.DEFAULT_DATA_LIFESPAN);

      MainFrame.getInstance().handleChangedTvDataDir();
    }

    if (PROP.hasChanged(LookAndFeel.VIEW_DATE_LAYOUT)) {
      MainFrame.getInstance().createDateSelector();
      MainFrame.getInstance().setShowDatelist(true, true); // set date list visible (and save), otherwise the setting has no effect on restart
    }

    if(PROP.hasChanged(General.CAN_RECEIVE_PROTOCOL_MESSAGE)) {
      ProtocolHandler.getInstance().handleSettingsChanged();
    }

    PROP.clearChanges();

    try {
      storeSettings(true);
    }catch(Exception e) {}
    
    PluginProxyManager.getInstance().fireTvBrowserSettingsChanged();
  }

  /**
   * @return The Time-Pattern for SimpleFormatter's
   */
  public static String getTimePattern() {
    if (Locales.TWELVE_HOUR_FORMAT.getBoolean()) {
      return "hh:mm a";
    } else {
      return "HH:mm";
    }
  }
  
  private static String getDefaultTvDataDir() {
    return (TVBrowser.isTransportable() && !mCopyToSystem) ? "./settings/tvdata" : getUserDirectoryName() + File.separator + "tvdata";
  }

  private static String getDefaultPluginsDir() {
    return getUserSettingsDirName() + "/plugins";
  }
  
  public static final String getCountry() {
    final String country = Locale.getDefault().getCountry();
    String result = Locale.getDefault().getLanguage();
    
    if(country.equals(new Locale("de_AT", "AT").getCountry()) || result.toLowerCase().equals(new Locale("de_AT", "AT").getLanguage().toLowerCase())) {
      result = "at";
    }
    else if(country.equals(new Locale("de_CH", "CH").getCountry()) || result.toLowerCase().equals(new Locale("de_CH", "CH").getLanguage().toLowerCase())) {
      result = "ch";
    }
    else if(country.equals(new Locale("de_DE", "DE").getCountry()) || result.toLowerCase().equals(new Locale("de_DE", "DE").getLanguage().toLowerCase())) {
      result = "de";
    }
    
    return result.toLowerCase();
  }
  
   /** Color for Programs marked with MIN_PRIORITY 
   * @deprecated since 4.2.2 */
  @Deprecated(since="4.2.2") public static final ColorProperty propProgramPanelMarkedMinPriorityColor = new ColorProperty(
      PROP, "programpanel.ColorMarked", new Color(140, 255, 0, 60));
  /** Color for Programs marked with LOWER_MEDIUM_PRIORITY 
   * @deprecated since 4.2.2 */
  @Deprecated(since="4.2.2") public static final ColorProperty propProgramPanelMarkedLowerMediumPriorityColor = new ColorProperty(
      PROP, "programpanel.ColorMarkedLowerMedium", new Color(0, 255, 255, 50));
  /** Color for Programs marked with MEDIUM_PRIORITY
   * @deprecated since 4.2.2 */
  @Deprecated(since="4.2.2") public static final ColorProperty propProgramPanelMarkedMediumPriorityColor = new ColorProperty(
      PROP, "programpanel.ColorMarkedMedium", new Color(255, 255, 0, 60));
  /** Color for Programs marked with HIGHER_MEDIUM_PRIORITY
   * @deprecated since 4.2.2 */
  @Deprecated(since="4.2.2") public static final ColorProperty propProgramPanelMarkedHigherMediumPriorityColor = new ColorProperty(
      PROP, "programpanel.ColorMarkedHigherMedium", new Color(255, 180, 0, 110));
  /** Color for Programs marked with MAX_PRIORITY
    * @deprecated since 4.2.2 */
  @Deprecated(since="4.2.2") public static final ColorProperty propProgramPanelMarkedMaxPriorityColor = new ColorProperty(
      PROP, "programpanel.ColorMarkedMax", new Color(255, 0, 0, 30));
  /** Color of the foreground of a program panel */
  
  /**
   * Sets the window position and size for the given window with the values of
   * the given id.
   *
   * @param windowId
   *          The id of the values to set.
   * @param window
   *          The window to layout.
   *
   * @since 2.7
   */
  public static final void layoutWindow(String windowId, java.awt.Window window) {
    layoutWindow(windowId, window, null);
  }

  /**
   * Sets the window position and size for the given window with the values of the given id.

   * @param windowId The id of the values to set.
   * @param window The window to layout.
   * @param defaultSize The default size for the window.
   *
   * @since 2.7
   */
  public static final void layoutWindow(String windowId, java.awt.Window window, Dimension defaultSize) {
    layoutWindow(windowId,window,defaultSize,null);
  }
  
  /**
   * Sets the window position and size for the given window with the values of the given id.

   * @param windowId The id of the values to set.
   * @param window The window to layout.
   * @param defaultSize The default size for the window.
   * @param parent The parent window of the window to layout (if not <code>null</code> the window is placed relative to it.)
   *
   * @since 3.3
   */
  public static final void layoutWindow(String windowId, java.awt.Window window, Dimension defaultSize, java.awt.Window parent) {
    layoutWindow(windowId, window, defaultSize, parent, false);
  }

 /**
  * Sets the window position and size for the given window with the values of the given id.
  
  * @param windowId The id of the values to set.
  * @param window The window to layout.
  * @param defaultSize The default size for the window.
  * @param parent The parent window of the window to layout (if not <code>null</code> the window is placed relative to it.)
  * @param ignoreAndMinSizeLocation If the location of the window and the minimum size should be ignored and not be set.
  *
  * @since 4.0.1
  */
  public static final void layoutWindow(String windowId, java.awt.Window window, Dimension defaultSize, java.awt.Window parent, boolean ignoreAndMinSizeLocation) {
    WindowSetting setting = mWindowSettings.get(windowId);

    if(setting == null) {
      setting = new WindowSetting(defaultSize);

      mWindowSettings.put(windowId, setting);
    }

    setting.setIgnoreAndMinSizeLocation(ignoreAndMinSizeLocation);
    setting.layout(window,parent);
  }
  
  /**
   * Update the window settings of a certain window.
   * 
   * @param windowId The id of the values to set.
   * @param defaultSize The new default size for the window or <code>null</code> to delete default size.
   * @param ignoreAndMinSizeLocation If the location of the window and the minimum size should be ignored and not be set.
   */
  public static final void updateWindowSettings(final String windowId, final Dimension defaultSize, final boolean ignoreAndMinSizeLocation) {
    WindowSetting setting = mWindowSettings.get(windowId);

    if(setting == null) {
      setting = new WindowSetting(defaultSize);

      mWindowSettings.put(windowId, setting);
    }
    
    setting.setIgnoreAndMinSizeLocation(ignoreAndMinSizeLocation);
  }
  
  private static final class VariableFontSizeFont extends Font {
    private int mOffset;
    
    public VariableFontSizeFont(String name, int style, int offset) {
      super(name, style, UIManager.getFont("MenuItem.font").getSize()+offset);
      mOffset = offset;
    }
    
    public int getSize() {
      return UIManager.getFont("MenuItem.font").getSize() + mOffset;
    }
  }
  
  public static void updateChannelFilters(Channel[] channelArr) {
    updateChannelFilters(channelArr, true);
  }
  
  public static void updateChannelFilters(Channel[] channelArr, boolean updateAll) {
    final ArrayList<SingleChannelFilterComponent> channelNameUpdateList = FilterComponentList.getInstance().updateChannels(channelArr);
    
    if(!channelNameUpdateList.isEmpty()) {
      final ProgramFilter[] filters = FilterList.getInstance().getFilterArr();
      
      for(SingleChannelFilterComponent scFilter : channelNameUpdateList) {
        for(ProgramFilter filter : filters) {
          if(filter instanceof UserFilter) {
            final String rule = ((UserFilter)filter).getRule();
            try {
              ((UserFilter) filter).setRule(rule.replace(scFilter.getLoadName(), scFilter.getName()));
            } catch (ParserException e) {
              e.printStackTrace();
            }
          }
        }
        
        scFilter.updateName();
      }
    }
    
    if(!channelNameUpdateList.isEmpty() || updateAll) {
      FilterComponentList.getInstance().store();
      
      FilterList.getInstance().updateAvailableChannels(channelArr);
      FilterList.getInstance().store();
    }
    
    MainFrame.updateFilterPanelLabel();
  }
  
  public static final void openSettingsDir() {
    if(Desktop.isDesktopSupported()) {
      try {
        Desktop.getDesktop().open(new File(getUserSettingsDirName()));
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  public static final class Other {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
  
    public static final IntProperty SETTINGS_DIALOG_DIVIDER_LOCATION = new IntProperty(PROP,
      "settingsDialogDividerLocation", 200);
  
    /** the class name of the last settings tab that has been closed with OK before */
    public static final StringProperty SETTINGS_LAST_USED_PATH = new StringProperty(PROP, "lastUsedSettingsTabClassName", "#channels");
  
    /**
     * list of hidden message boxes
     * @since 2.7
     */
    public static final StringArrayProperty MESSAGE_BOXES_HIDDEN = new StringArrayProperty(PROP, "hideMessageBox", new String[] {});
  
    /**
     * Hidden property for blocked filter components for Favorite usage.
     * Add the property favoriteBlockedFilterComponents to the settings.prop
     * to change the blocked filter components.
     * <p>
     * @since 3.4.5 */
    public static final StringArrayProperty FAVORITE_BLOCKED_FILTER_COMPONENTS = new StringArrayProperty(
      PROP, "favoriteBlockedFilterComponents", new String[] {"tvbrowser.core.filters.filtercomponents.BeanShellFilterComponent","tvbrowser.core.filters.filtercomponents.ProgramMarkingPriorityFilterComponent","tvbrowser.core.filters.filtercomponents.ReminderFilterComponent","tvbrowser.core.filters.filtercomponents.PluginFilterComponent"});
  
  
    private Other() {}
  }
  
  public static final class Buttons {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#timebuttons";
  
    public static final IntArrayProperty TIME_BUTTONS = new IntArrayProperty(
      PROP, "timeButtons", new int[] { 6 * 60, 12 * 60, 18 * 60, 20 * 60 + 15 });;
    
    private Buttons() {};
  }
  
  public static final class CenterPanels {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#centerpanelsetup";
    /**
     * Array with the panel IDs shown in the center panel of TV-Browser main window.
     * @since 3.2
     */
    public static final StringArrayProperty CENTER_PANEL_ARR = new StringArrayProperty(
      PROP, "centerPanelArr", new String[] {"tvbrowser.ui.programtable.ProgramTableScrollPaneWrapper"});
    
    /**
     * Array with the deselected IDs of the center panels.
     */
    public static final StringArrayProperty DISABLED_CENTER_PANEL_ARR = new StringArrayProperty(
      PROP, "disabledCenterPanelArr", new String[0]);
    /**
     * If the tab bar in the center of the TV-Browser window should always be shown.
     * @since 3.2
     */
    public static final BooleanProperty ALWAYS_SHOW_TAB_BAR_FOR_CENTER_PANEL = new BooleanProperty(
      PROP, "alwaysShowTabBarForCenterPanel", true);
    
    /**
     * Property of name and icon showing of tab bar in center panel.
     * @since 3.4.5
     */
    public static final IntProperty TAB_BAR_CENTER_PANEL_NAME_ICON_CONFIG = new IntProperty(
      PROP, "pbBarCenterPanelNameIconConfig", 2);
    
    /**
    * @since 4.2.5
    */
   public static final BooleanProperty PLUGIN_FUNCTIONS_IN_MENU_SHOW = new BooleanProperty(
       PROP, "showPluginFunctionsInTabpaneMenu", true);
    
    public CenterPanels() {}
  }
  
  public static final class IconAndNames {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    /** Value for name only settings */
    public static final int VALUE_NAME_ONLY = 0;
  
    /** Value for icon only settings */
    public static final int VALUE_ICON_ONLY = 1;
    
    /** Value for name and icon settings */
    public static final int VALUE_NAME_AND_ICON = 2;
  
    public static final String ID = "#channelIconName";
    
    public static final BooleanProperty SHOW_ICONS_IN_PROGRAM_TABLE = new BooleanProperty(
      PROP, "showChannelIconsInProgramtable", true);
    public static final BooleanProperty SHOW_NAMES_IN_PROGRAM_TABLE = new BooleanProperty(
      PROP, "showChannelNamesInProgramtable", true);
    public static final IntProperty SHOW_LOGO_FOR_PROGRAM_PANEL = new IntProperty(
      PROP, "showChannelLogoForProgramPanel", ProgramPanelSettings.SHOW_CHANNEL_LOGO_PLUGINS_CONTROL);
    public static final BooleanProperty SHOW_ICONS_IN_CHANNEL_LIST = new BooleanProperty(
      PROP, "showChannelIconsInChannellist", true);
    public static final BooleanProperty SHOW_NAMES_IN_CHANNEL_LIST = new BooleanProperty(
      PROP, "showChannelNamesInChannellist", true);
    
    /**
     * show sort number in program table?
     * @since 3.3.4
     */
    public static final BooleanProperty SHOW_SORT_NUMBER_IN_PROGRAM_TABLE = new BooleanProperty(
      PROP, "showSortNumberInProgramTable", true);
    
    /**
     * show tooltip with large channel icon
     * @since 2.7
     */
    public static final BooleanProperty SHOW_CHANNEL_TOOLTIP_IN_PROGRAM_TABLE = new BooleanProperty(
      PROP, "showChannelTooltipInProgramtable", true);
    
    /**
     * show sort number in program lists?
     * @since 3.3.4
     */
    public static final BooleanProperty SHOW_SORT_NUMBER_IN_PROGRAM_LISTS = new BooleanProperty(
      PROP, "showSortNumberInProgramLists", true);
    
    private IconAndNames() {} 
  }
  
  public static final class Channels {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#channels";
    
    /**
     * subscribed channels
     */
    public static final ChannelArrayProperty SUBSCRIBED = new ChannelArrayProperty(
      PROP, "subscribedchannels", new Channel[0]);

    /**
     * the last active channel group for filtering the channel list
     */
    public static final StringProperty GROUP_LAST_USED = new StringProperty(
        PROP, "lastchannelgroup", null);

    /**
     * selected plugin filter in channel settings
     * @since 3.1.1
     */
    public static final StringArrayProperty SUBSCRIBED_SEPARATORS = new StringArrayProperty(
      PROP, "subscribedChannelsSeparators", new String[0]);    

    /** Saves the selected channel category filter index */
    public static final ByteProperty SELECTED_CATEGORY_INDEX = new ByteProperty(
      PROP, "selectedChannelCategoryIndex", (byte)1);
      
    /**
     * selected channel country filter in channel settings
     * @since 3.0
     */
    public static final StringProperty SELECTED_COUNTRY = new StringProperty(
      PROP, "selectedChannelCountry", "");
    
    public static final StringProperty LAST_EXPORT_FILE = new StringProperty(
      PROP, "lastChannelExportFile", System.getProperty("user.home") + "/TVB-channel-export.txt");
    public static final StringProperty SELECTED_PLUGIN = new StringProperty(
      PROP, "selectedChannelPlugin", "");;
    
    public static final StringArrayProperty USED_CHANNEL_GROUPS = new StringArrayProperty(
      PROP, "usedChannelGroups", null);
    
    /**
     * saves if the channels were configured
     * @since 3.0
     */
    public static final BooleanProperty WERE_CONFIGURED = new BooleanProperty(
      PROP, "channelsWereConfigured", false);
    
    public static final DateProperty UPDATE_LAST = new DateProperty(
      PROP, "lastChannelUpdate", null);
    
    /**
     * @since 3.0
     */
    public static final StringArrayProperty DATA_SERVICE_IDS_USED_CURRENTLY = new StringArrayProperty(PROP, "currentDataServices", new String[0]);    
    
    private Channels() {}
  }
  
  public static final class ContextMenu {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#contextmenu";
    
    /**
     * All sub actions of plugins that are disabled
     * @since 3.4.5
     */
    public static final StringArrayProperty DISABLED_SUB_ITEMS = new StringArrayProperty(
      PROP, "propContextMenuDisabledSubItems", null);

    /**
     * Order of the Plugins in the Context-Menu.
     */
    public static final StringArrayProperty MENU_ORDER = new StringArrayProperty(PROP, "contextMenuOrder",
      new String[] { "programinfo.ProgramInfo", "searchplugin.SearchPlugin", "reminderplugin.ReminderPlugin",
          "favoritesplugin.FavoritesPlugin", SeparatorMenuItem.SEPARATOR, "java.webplugin.WebPlugin",
          "java.simplemarkerplugin.SimpleMarkerPlugin", "java.captureplugin.CapturePlugin" });
    
    /**
     * All disabled Items of the ContextMenu
     */
    public static final StringArrayProperty DISABLED_ITEMS = new StringArrayProperty(
      PROP, "contextMenuDisabledItems", null);
    
    public static final StringArrayProperty PLUGINS_KNOWN = new StringArrayProperty(
      PROP, "knownContextMenuPlugins", new String[0]);
    
    private ContextMenu() {}
  }
  
  public static final class DataPostProcessing {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#dataPluginPostProcessing";
    
    public static final StringArrayProperty ORDER = new StringArrayProperty(
      PROP, "dataPluginPostProcessingOrder", new String[0]);
    
    private DataPostProcessing() {}
  }
  
  public static final class Directories {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#directories";
    
    public static final StringProperty TV_DATA = new StringProperty(
      PROP, "dir.tvdata", mDefaultSettings.getProperty("tvdatadir",
          getDefaultTvDataDir()));
    public static final StringProperty PLUGINS = new StringProperty(
      PROP, "dir.plugins", mDefaultSettings.getProperty("pluginsdir",
          getDefaultPluginsDir()));
    public static final StringProperty LOG = new StringProperty(
      PROP, "logdirectory", mDefaultSettings.getProperty("logdirectory", null));
    
    private Directories() {}
  }

  public static final class Fonts {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#fonts";
    
    public static final BooleanProperty ANTIALIASING_ENABLED = new BooleanProperty(
      PROP, "enableantialiasing", true);
    public static final BooleanProperty USE_DEFAULT = new BooleanProperty(
      PROP, "usedefaultfonts", true);
    public static final FontProperty CHANNEL_NAME = new DeferredFontProperty(
      PROP, "font.channelname", DEFAULT_CHANNELNAMEFONT);
    public static final FontProperty PROGRAM_TITLE = new DeferredFontProperty(
      PROP, "font.programtitle", DEFAULT_PROGRAMTITLEFONT);
    public static final FontProperty PROGRAM_INFO = new DeferredFontProperty(
      PROP, "font.programinfo", DEFAULT_PROGRAMINFOFONT);
    public static final FontProperty PROGRAM_TIME = new DeferredFontProperty(
      PROP, "font.programtime", DEFAULT_PROGRAMTIMEFONT);
    public static final IntProperty PROGRAM_TEX_TLINE_GAP = new IntProperty(
      PROP, "font.lineGap", 0);
    
    private Fonts() {}
  }
  
  public static final class General {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#startup";
  
    public static final BooleanProperty MINIMIZE_AFTER_STARTUP = new BooleanProperty(
      PROP, "minimizeAfterStartup", false);
    public static final BooleanProperty START_SCREEN_SHOW = new BooleanProperty(
      PROP, "splash.show", true);
    public static final BooleanProperty IS_USING_FULLSCREEN = new BooleanProperty(
      PROP, "isUsingFullscreen", false);

    /** 
     * Sets the availability of the restore server.
     * <p>
     * @since 4.1
     */
    public static final BooleanProperty SERVER_RESTORE_ENABLED = new BooleanProperty(PROP, "serverRestoreEnabled", true);

    /**
     * Property to allow handling of tvb:// protocol events. 
     * 
     * @since 4.2.3
     */
    public static final BooleanProperty CAN_RECEIVE_PROTOCOL_MESSAGE = new BooleanProperty(PROP, "canReceiveProtocolMessages", true);
    public static final BooleanProperty ONLY_MINIMIZE_WHEN_WINDOW_CLOSING = new BooleanProperty(
      PROP, "onlyMinimizeWhenWindowClosing", false);
    public static final HiddenMessagesProperty ASK_FOR_EXIT_CONFIRMATION = new HiddenMessagesProperty("MainFrame.askForExitConfirm", true);
    
    /** check for channel changes every 14 days by default */
    public static final IntProperty AUTO_CHANNEL_UPDATE_PERIOD = new IntProperty(
      PROP, "autoChannelUpdatePeriod", 14);
    
    /**@since 4.2.2*/
    public static final BooleanProperty AUTO_UPDATE_PRIME_TIME = new BooleanProperty(
      PROP, "autoUpdatePrimeTime", false);
    public static final ChoiceProperty AUTO_DOWNLOAD_TYPE = new ChoiceProperty(
      PROP, "autodownload", "daily", new String[] { "startup", "daily",
          "every3days", "weekly", "never" });
    public static final HiddenMessagesProperty DOWNLOAD_DONE = new HiddenMessagesProperty("downloadDone", true);
    public static final BooleanProperty AUTO_DATA_DOWNLOAD_ENABLED = new BooleanProperty(
      PROP, "autoDataDownloadEnabled", true);
    public static final BooleanProperty ASK_FOR_AUTO_DOWNLOAD = new BooleanProperty(
      PROP, "askForAutoDownload", false);
    public static final IntProperty AUTO_DOWNLOAD_PERIOD = new IntProperty(
      PROP, "autodownloadperiod", 0);
    public static final ShortProperty AUTO_DOWNLOAD_WAITING_TIME = new ShortProperty(
      PROP, "autoDownloadWaitingTime", (short) 5);
    public static final BooleanProperty AUTO_DOWNLOAD_WAITING_ENABLED = new BooleanProperty(
      PROP, "autoDownloadWaitingEnabled", true);
    
    /**
     * enable checking date and time via NTP if no TV data can be downloaded
     */
    public static final BooleanProperty NTP_TIME_CHECK = new BooleanProperty(PROP, "ntpTimeCheckEnabled", true);
    
    /**
     * TV-Browser JRE update package path.
     * <p>
     * @since 4.1
     */
    public static final StringProperty JRE_UPDATE = new StringProperty(PROP, "jreUpdate", "");
    public static final DateProperty JRE_UPDATE_DATE_LAST = new DateProperty(PROP, "jreUpdateDateLast", null);
    public static final BooleanProperty JRE_UPDATE_ENABLED = new BooleanProperty(PROP, "jreUpdateEnabled", true);
    
    public static final VersionProperty TV_BROWSER_VERSION_USED_LAST = new VersionProperty(
      PROP, "version", null);
  
    public static final BooleanProperty TV_BROWSER_VERSION_USED_LAST_IS_STABLE = new BooleanProperty(
      PROP, "versionIsStable", false);
    
    /** Saves the date of the very first TV-Browser start */
    public static final DateProperty DATE_FIRST_START = new DateProperty(
      PROP, "firstStartDate", null);
  
  
    
    /**
     * date of last NTP internet time check
     */
    public static final DateProperty NTP_CHECK_LAST = new DateProperty(PROP, "lastNTPCheck", null);
  
    public static final BooleanProperty LOGGING_VERBOSE = new BooleanProperty(
      PROP, "verboseLogging", false);
    
    /**
     * Date when TV-Browser has searched for settings of old versions of TV-Browser last.
     * @since 4.2.2
     */
    public static final DateProperty DATE_OLD_SETTINGS_CHECKED_LAST = new DateProperty(PROP, "dateOldSettingsCheckedLast", null);
    
    /** The user selected default filter */
    public static final StringProperty FILTER_DEFAULT = new StringProperty(
      PROP, "defaultFilter", "");
    
    /**
     * Current available stable version of TV-Browser.
     * @since 4.2.5
     */
    public static final VersionProperty VERSION_AVAILABLE = new VersionProperty(PROP, "versionAvailable", TVBrowser.VERSION);

    /**
     * Current available test version of TV-Browser.
     * @since 4.2.5
     */
    public static final VersionProperty TEST_VERSION_AVAILABLE = new VersionProperty(PROP, "testVersionAvailable", TVBrowser.VERSION);

    /**
     * Inform about test versions available.
     * @since 4.2.5
     */
    public static final HiddenMessagesProperty INFORM_TEST_VERSIONS = new HiddenMessagesProperty("informTestVersions", false);
    
    private General() {}
  }
  
  public static final class Locales {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#local";

    /**
     * Use 12-Hour Format?
     */
    public static final BooleanProperty TWELVE_HOUR_FORMAT = new BooleanProperty(
      PROP, "uswTwelveHourFormat", false);
    public static final IntProperty FIRST_DAY_OF_WEEK = new IntProperty(
      PROP, "firstDayOfWeek", Calendar.getInstance().getFirstDayOfWeek());
    public static final StringProperty LANGUAGE = new StringProperty(PROP,
      "language", System.getProperty("user.language"));
    public static final StringProperty COUNTRY = new StringProperty(PROP,
      "country", System.getProperty("user.country", ""));
    public static final StringProperty VARIANT = new StringProperty(PROP,
      "variant", System.getProperty("user.variant",""));
    public static final StringProperty TIMEZONE = new StringProperty(PROP,
      "timeZone", null);
    
    private Locales() {}
  }
  
  public static final class LookAndFeel {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#lookandfeel";
    
    /**
     * If the plugin view is on the left side and the channel list on the right side.
     * @since 2.7
     */
    public static final BooleanProperty PLUGIN_VIEW_IS_LEFT = new BooleanProperty(
      PROP, "pluginViewIsLeft", true);
    
    /**
     * if calendar view is active
     *
     * @since 3.0
     */
    public static final IntProperty VIEW_DATE_LAYOUT = new IntProperty(
      PROP, "propViewDateLayout", 1);
      
    /**
     * Stores if the Persona should be selected randomly at start.
     * @since 3.1
     */
    public static final BooleanProperty PERSONA_RANDOM = new BooleanProperty(
      PROP, "randomPersona", false);

    /**
     * Stores the id of the selected Persona.
     * @since 3.1
     */
    public static final StringProperty PERSONA_SELECTED = new StringProperty(
      PROP, "persona", "51b73c81-7d61-4626-b230-89627c9f5ce7");
    public static final StringProperty JGOODIES_THEME = new JGoodiesThemeProperty(
      PROP, "jgoodies.theme");
    public static final BooleanProperty JGOODIES_SHADOW = new BooleanProperty(
      PROP, "jgoodies.dropshadow", false);
    public static final StringProperty SELECTED = new StringProperty(
      PROP, "lookandfeel1_1", mDefaultSettings.getProperty("lookandfeel",
          UiUtilities.getDefaultLookAndFeelClassName(false)));
    public static final StringProperty INFO_ICON_THEME_ID = new StringProperty(
      PROP, "infoIconThemeName", "tvb_default.zip");

    /**
     * The IconTheme
     */
    public static final StringProperty ICON_THEME = new StringProperty(PROP,
      "icontheme", mDefaultSettings.getProperty("icontheme", null));
    
    private LookAndFeel() {}
  }
  
  public static final class Markings {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#programpanelmarking";
    
    /**
     * Used to track if a program panel should use additional space for the mark
     * icons
     */
    public static final BooleanProperty USES_EXTRA_SPACE_FOR_MARK_ICONS = new BooleanProperty(
      PROP, "programpanel.usesExtraSpaceForMarkIcons", true);
      
    /** Used to enable border on marked programs */
    public static final BooleanProperty WITH_MARKINGS_SHOWING_BORDER = new BooleanProperty(
      PROP, "programpanel.markingsShowingBorder", false);
    
    /** Used default mark priority for markings of plugins. */
    public static final IntProperty MARK_PRIORITY_DEFAULT = new IntProperty(
      PROP, "programpanel.defaultMarkPriority", 0);
    
    /** Used mark priority for markings of filters. */
    public static final IntProperty MARK_PRIORITY_FILTERS = new IntProperty(
      PROP, "programpanel.filtersMarkPriority", 0);
    
    /** Array with in representations of the highlighting colors for Programs 
     * @since 4.2.2 */
    public static final IntArrayProperty HIGHLIGHTING_COLORS = new IntArrayProperty(
      PROP, "programpanel.HighlightingColors", new int[] {
          new Color(140, 255, 0, 60).getRGB(),
          new Color(0, 255, 255, 50).getRGB(),
          new Color(255, 255, 0, 60).getRGB(),
          new Color(255, 180, 0, 110).getRGB(),
          new Color(255, 0, 0, 30).getRGB()
      });
    
    public static final StringMapProperty HIGHLIGHTING_FILTERS = new StringMapProperty(
        PROP, "highlightingFilters");
    
    private Markings() {}
  }
  
  public static final class Mouse {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#mouse";
    
    public static final ContextMenuMouseActionArrayProperty LEFT_SINGLE_CLICK_IF_ARRAY = new ContextMenuMouseActionArrayProperty(
      PROP, "leftSingleClickIfArray", new ContextMenuMouseActionSetting[] {new ContextMenuMouseActionSetting(ContextMenuManager.NO_MOUSE_MODIFIER_EX,ProgramInfo.getProgramInfoPluginId(),ActionMenu.ID_ACTION_NONE)});
    public static final ContextMenuMouseActionArrayProperty LEFT_DOUBLE_CLICK_IF_ARRAY = new ContextMenuMouseActionArrayProperty(
      PROP, "leftDoubleClickIfArray", new ContextMenuMouseActionSetting[] {new ContextMenuMouseActionSetting(ContextMenuManager.NO_MOUSE_MODIFIER_EX,ProgramInfo.getProgramInfoPluginId(),ActionMenu.ID_ACTION_NONE)});
    public static final ContextMenuMouseActionArrayProperty MIDDLE_SINGLE_CLICK_IF_ARRAY = new ContextMenuMouseActionArrayProperty(
      PROP, "middleSingleClickIfArray", new ContextMenuMouseActionSetting[] {new ContextMenuMouseActionSetting(ContextMenuManager.NO_MOUSE_MODIFIER_EX,ReminderPlugin.getReminderPluginId(),ActionMenu.ID_ACTION_NONE)});
    public static final ContextMenuMouseActionArrayProperty MIDDLE_DOUBLE_CLICK_IF_ARRAY = new ContextMenuMouseActionArrayProperty(
      PROP, "middleDoubleClickIfArray", new ContextMenuMouseActionSetting[] {new ContextMenuMouseActionSetting(ContextMenuManager.NO_MOUSE_MODIFIER_EX,FavoritesPlugin.getFavoritesPluginId(),1)});
    
    private Mouse() {}
  }
  
  public static final class Network {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#network";
    
    /** If the internet connection should be checked before accessing internet */
    public static final BooleanProperty INTERNET_CONNECTION_CHECK = new BooleanProperty(
      PROP, "internetConnectionCheck", true);
    public static final IntProperty DEFAULT_CONNECTION_TIMEOUT = new IntProperty(
      PROP, "network.defaultConnectionTimeout", 60000);
    public static final IntProperty CHECK_TIMEOUT = new IntProperty(
      PROP, "network.checkTimeout", 10000);
    
    private Network() {}
  }
  
  public static final class Pictures {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#pictures";
    
    public static final IntProperty TYPE = new IntProperty(
      PROP, "pictures.type", ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION);
    public static final MinutesProperty TIME_START = new MinutesProperty(
      PROP, "pictures.startTime", 18 * 60);
    public static final MinutesProperty TIME_END = new MinutesProperty(
      PROP, "pictures.endTime", 23 * 60);
    public static final IntProperty DURATION = new IntProperty(
      PROP, "pictures.duration", 90);
    public static final BooleanProperty DESCRIPTION_SHOW = new BooleanProperty(
      PROP, "pictures.showDescription", true);
    
    /**
     * Stores if the picture borders should be painted.
     * @since 3.1
     */
    public static final BooleanProperty BORDER_SHOW = new BooleanProperty(
      PROP, "showPictureBorder", true);
    public static final StringArrayProperty PLUGIN_IDS = new StringArrayProperty(
      PROP, "pictures.pluginIds", new String[0]);
    
    /** The setting that contains the global picture settings value */
    public static final IntProperty PLUGINS_SETTING = new IntProperty(
      PROP, "pluginsPictureSetting", PluginPictureSettings.PICTURE_AND_DISCRIPTION_TYPE);
    public static final IntProperty DESCRIPTION_LINES = new IntProperty(
    PROP, "pictures.lines", 6);
    
    private Pictures() {}
  }
  
  public static final class Plugins {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#plugins";
    
    @SuppressWarnings("exports")
    public static final BlockedPluginArrayProperty BLOCKED_ARRAY = new BlockedPluginArrayProperty(PROP, "blockedPlugins");
    
    /**
     * some plugins are installed by default, but not activated
     */
    private static final String[] DEFAULT_DISABLED_PLUGINS;
    static {
      ArrayList<String> plugins = new ArrayList<String>();
      plugins.add("java.showviewplugin.ShowviewPlugin"); // no longer available
      plugins.add("java.i18nplugin.I18NPlugin"); // developers only
      if (!OperatingSystem.isMacOs()) {
        plugins.add("java.growlplugin.GrowlPlugin"); // needs Growl for Windows
      }
      plugins.add("java.blogthisplugin.BlogThisPlugin"); // typical users don't blog
      DEFAULT_DISABLED_PLUGINS = plugins.toArray(new String[plugins.size()]);
    }
    
    /**
     * The ID's of the plugins that have been deactivated.
     * <p>
     * NOTE: By remembering the deactivated plugins rather then the activated plugins
     * new plugins are activated automatically.
     */
    public static final StringArrayProperty DEACTIVATED = new StringArrayProperty(
      PROP, "deactivatedPlugins", DEFAULT_DISABLED_PLUGINS);
    
    /** If the plugin updates should be found automatically */
    public static final BooleanProperty AUTO_UPDATE_ENABLED = new BooleanProperty(
      PROP, "autoUpdatePlugins", true);
    
    /**
     * Ids of plugins which settings should be reset on next startup.
     * <p>
     * @since 4.2.3
     */
    public static final StringArrayProperty RESET_IDS = new StringArrayProperty(PROP, "propPluginReset", null);
    
    
    /**
     * The order of the plugin IDs.
     * <p>
     * In former times this property hold the list of plugin class names that
     * should be activated on startup (in the right order). Now it holds IDs, not
     * class names and activation is controlled by {@link #propDeactivatedPlugins}.
     */
    public static final StringArrayProperty PLUGIN_ORDER = new StringArrayProperty(
      PROP, "plugins", null);
    
    public static final StringArrayProperty DELETE_FILES_AT_START = new StringArrayProperty(
      PROP, "deleteFilesAtStart", new String[0]);
    
    public static final DateProperty UPDATE_LAST = new DateProperty(
      PROP, "lastPluginsUpdate", null);

    /**
     * id of the  active program receive target plugin
     * @since 3.0
     */
    public static final StringProperty RECEIVE_PLUGIN_USED_LAST = new StringProperty(
      PROP, "lastusedreceiveplugin", null);
  
    /**
     * id of the last active program receive target
     * @since 3.0
     */
    public static final StringProperty RECEIVE_TARGET_USED_LAST = new StringProperty(
      PROP, "lastusedreceivetarget", null);
  
    /**
     * Stores if beta warining is enabled for plugin update.
     * @since 3.0
     */
    public static final BooleanProperty BETA_WARNING = new BooleanProperty(
      PROP, "pluginBetaWarning", true);
    
    /**
     * Hidden setting for disabling updates for beta Plugins in beta versions of TV-Browser.
     * @since 4.2.7
     */
    public static final BooleanProperty BETA_UPDATE_NO = new BooleanProperty(
      PROP, "pluginNoBetaUpdate", false);
    
    public static final StringArrayProperty ACCESS_CONTROL = new StringArrayProperty(
      PROP, "accessControl", new String[0]);
    
    /** Saves if the plugin info dialog was already shown */
    public static final BooleanProperty INFO_DIALOG_WAS_SHOWN = new BooleanProperty(
      PROP, "pluginInfoDialogWasShown", false);
      
    private Plugins() {}
  }
  
  public static final class ProgramPanel {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#programpanellook";
    
    public static final StringArrayProperty ICON_PLUGINS = new StringArrayProperty(
      PROP, "programpanel.iconPlugins", new String[] { PICTURE_ID,INFO_ID,
          "tvraterplugin.TVRaterPlugin", });
    public static final StringArrayProperty ICON_PLUGINS_ALTERNATIVE = new StringArrayProperty(
      PROP, "programpanel.iconPluginsAlternative", new String[] { PICTURE_ID,INFO_ID,
          "tvraterplugin.TVRaterPlugin", });
    public static final ProgramFieldTypeArrayProperty INFO_FIELDS = new ProgramFieldTypeArrayProperty(
      PROP, "programpanel.infoFields", new ProgramFieldType[] {
          ProgramFieldType.GENRE_TYPE, ProgramFieldType.EPISODE_TYPE,
          ProgramFieldType.ORIGIN_TYPE, ProgramFieldType.PRODUCTION_YEAR_TYPE,
          ProgramFieldType.SHORT_DESCRIPTION_TYPE });
    
    /** Contains the separators for the selected program info filed of a program panel */
    public static final StringArrayProperty INFO_FIELDS_SEPARATORS = new StringArrayProperty(
      PROP, "programpanel.infoFieldsSeparators", new String[] {
         " - "," - ",
         " - "," - "
      });
    public static final ProgramFieldTypeArrayProperty INFO_FIELDS_ALTERNATIVE = new ProgramFieldTypeArrayProperty(
      PROP, "programpanel.infoFieldsAlternative", new ProgramFieldType[] {
          ProgramFieldType.GENRE_TYPE, ProgramFieldType.EPISODE_TYPE,
          ProgramFieldType.ORIGIN_TYPE, ProgramFieldType.PRODUCTION_YEAR_TYPE,
          ProgramFieldType.SHORT_DESCRIPTION_TYPE });
    
    /** Contains the separators for the selected program info filed of a program panel */
    public static final StringArrayProperty INFO_FIELDS_SEPARATORS_ALTERNATIVE = new StringArrayProperty(
      PROP, "programpanel.infoFieldsSeparatorsAlternative", new String[] {
         " - "," - ",
         " - "," - "
      });
    
    /**
     * if the original title show be shown instead of title if original is available
     * @since 3.4.5 
     */
    public static final BooleanProperty ORIGINIAL_TITLES_SHOW = new BooleanProperty(
      PROP, "programpanel.ShowOriginalTitles", false);
    
    /** Used to enable border for on air programs */
    public static final BooleanProperty BORDER_ON_AIR_PROGRAMS_SHOW = new BooleanProperty(
      PROP, "programpanel.onAirProgramsShowingBorder", false);
    
    /**
     * Flag to set highlighting to gradient instead of priority based coloring.
     * @since 4.2.2
     */
    public static final BooleanProperty HIGHLIGHTING_COLOR_GRADIENT = new BooleanProperty(
      PROP, "programpanel.programPanelGradientColorHighlighting", true);
    
    /** Color for Program on Air - This shows how much was shown until now */
    public static final ColorProperty COLOR_ON_AIR_DARK = new ColorProperty(
      PROP, "programpanel.ColorOnAirDark", new Color(0, 0, 255, 60));
    
    /** Color for Program on Air - This shows how much is not shown until now */
    public static final ColorProperty COLOR_ON_AIR_LIGHT = new ColorProperty(
      PROP, "programpanel.ColorOnAirLight", new Color(0, 0, 255, 30));
    
    /** Color for selected Program */
    public static final ColorProperty COLOR_KEYBOARD_SELECTED = new ColorProperty(
      PROP, "programpanel.KeyboardSelectedColor", new Color(130, 255, 0, 120));
    
    /** If plugins are allowed to set the transparency of a program */
    public static final BooleanProperty TRANSPARENCY_ALLOW = new BooleanProperty(
      PROP, "programpanel.AllowTransparency", true);

    /**
     * use hyphenation to break strings in a program panel
     */
    public static final BooleanProperty HYPHENATION = new BooleanProperty(
      PROP, "programpanel.Hyphenation", false);
    public static final BooleanProperty SMOOTHER_SCROLLING = new BooleanProperty(
        PROP, "smootherScrolling", true);
    
    
    /**
     * if a long program title is to be shown in the program table, shall it be
     * cut?
     *
     * @since 3.0
     */
    public static final BooleanProperty TITLE_CUT = new BooleanProperty(
      PROP, "programTableCutTitle", true);
    
    /**
     * how many lines of the title shall be shown if it is cut
     *
     * @since 3.0
     */
    public static final IntProperty TITLE_CUT_LINES = new IntProperty(
      PROP, "programTableCutTitleLines", 2);
    
    /**
     * number of description lines show in program panel
     */
    public static final IntProperty MAX_LINES  = new IntProperty(
      PROP, "programpanel.MaxLines", 3);
    
    /**
     * show less description lines for very short programs
     */
    public static final BooleanProperty DESCRIPTION_LIMIT_BY_DURATION = new BooleanProperty(
      PROP, "programpanel.ShortActive", true);
    
    /**
     * maximum duration in minutes to show no description
     */
    public static final IntProperty DESCRIPTION_LIMIT_BY_DURATION_MINUTES = new IntProperty(
      PROP, "programpanel.ShortMinutes", 10);
    
    public static final ColorProperty COLOR_FOREGROUND = new ColorProperty(
      PROP, "programpanel.ColorForeground", Color.black);
     
    private ProgramPanel() {}
  }
  
  public static final class ProgramTable {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#programtablelook";
    
    /**
     * maximum width of the program table columns
     */
    public static final int COLUMN_WIDTH_MAX = 2000;
  
    /**
     * minimum width of the program table columns
     */
    public static final int COLUMN_WIDTH_MIN = 100;
    
    public static final String VALUE_LAYOUT_COMPACT = "compact";
    public static final String VALUE_LAYOUT_REAL_COMPACT = "realCompact";
    public static final String VALUE_LAYOUT_TIME_SYNCHRONOUS = "timeSynchronous";
    public static final String VALUE_LAYOUT_TIME_BLOCK = "timeBlock";
    public static final String VALUE_LAYOUT_COMPACT_TIME_BLOCK = "compactTimeBlock";
    public static final String VALUE_LAYOUT_OPTIMIZED_COMPACT_TIME_BLOCK = "optimizedCompactTimeBlock";
    public static final String VALUE_LAYOUT_REAL_SYNCHRONOUS = "realSynchronous";
    
    public static final ChoiceProperty STYLE_BACKGROUND = new ChoiceProperty(
      PROP, "tablebackground.style", "uiTimeBlock", new String[] { "singleColor",
          "oneImage", ProgramTable.VALUE_LAYOUT_TIME_BLOCK, "timeOfDay", "uiColor" , "uiTimeBlock" });
    public static final ChoiceProperty LAYOUT = new ChoiceProperty(
      PROP, "table.layout", ProgramTable.VALUE_LAYOUT_OPTIMIZED_COMPACT_TIME_BLOCK, new String[] {
          ProgramTable.VALUE_LAYOUT_TIME_SYNCHRONOUS, ProgramTable.VALUE_LAYOUT_COMPACT, ProgramTable.VALUE_LAYOUT_REAL_SYNCHRONOUS,
          ProgramTable.VALUE_LAYOUT_REAL_COMPACT, ProgramTable.VALUE_LAYOUT_TIME_BLOCK, ProgramTable.VALUE_LAYOUT_COMPACT_TIME_BLOCK, 
          ProgramTable.VALUE_LAYOUT_OPTIMIZED_COMPACT_TIME_BLOCK});
    public static final IntProperty COLUMN_WIDTH = new VariableIntProperty(PROP,
      "columnwidth", 200);
    
    /**
     * start of day in minutes after midnight
     */
    public static final MinutesProperty START_OF_DAY = new MinutesProperty(
      PROP, "programtable.startofday", 0);
    
    /**
     * end of day in minutes after midnight
     */
    public static final MinutesProperty END_OF_DAY = new MinutesProperty(
      PROP, "programtable.endofday", 5 * 60);
    public static final BooleanProperty MOUSE_OVER = new BooleanProperty(
      PROP, "programpanel.MouseOver", true);
    
    /** Color for Mouse-Over */
    public static final ColorProperty COLOR_MOUSE_OVER = new ColorProperty(
      PROP, "programpanel.MouseOverColor", new Color(200, 200, 0, 60));
    public static final BooleanProperty SCROLL_HORIZONTAL = new BooleanProperty(
      PROP, "programpanel.scrollHorizontal", false);
    public static final BooleanProperty AUTO_CHANGE_DATE = new BooleanProperty(
      PROP, "autoScrollToNextDay", true);
    
    /**
     * auto scroll table after panning?
     *
     * @since 3.0
     */
    public static final BooleanProperty MOUSE_AUTO_SCROLL = new BooleanProperty(
      PROP, "programTableMouseAutoScroll", true);
    public static final BooleanProperty SCROLL_TO_TIME_MARKING = new BooleanProperty(
      PROP, "scrollToTimeMarkingActivated", true);
    public static final ColorProperty COLOR_SCROLL_TO_TIME_PROGRAMS_BACKGROUND_LIGHT = new ColorProperty(
      PROP, "scrollToTimeProgramsLightBackground", new Color(255, 150, 0, 40));
    public static final ColorProperty COLOR_SCROLL_TO_TIME_PROGRAMS_BACKGROUND_DARK = new ColorProperty(
      PROP, "scrollToTimeProgramsDarkBackground", new Color(255, 150, 0, 80));
    public static final BooleanProperty HIGHLIGHT_CHANNEL_COLUMN_BY_SCROLLING = new BooleanProperty(
      PROP, "scrollToChannelMarkingActivated", true);
    
    /**
     * @since 4.2.4
     */
    public static final BooleanProperty HIGHLIGHT_CHANNEL_COLUMN_BY_MOUSE = new BooleanProperty(
      PROP, "highlightChannelColumnByMouse", true);
    public static final ColorProperty COLOR_HIGHLIGHT_CHANNEL_PROGRAMS_BACKGROUND = new ColorProperty(
      PROP, "scrollToChannelProgramsBackground", new Color(255, 150, 0, 40));
    
    /**
     * Find as you type in the program table enabled?
     * @since 3.1.1
     */    
    public static final BooleanProperty FIND_AS_YOU_TYPE = new BooleanProperty(
      PROP, "typeAsYouFindEnabled", true);
    
    public static final StringProperty TIME_OF_DAY_BACKGROUND_EDGE = new StringProperty(
      PROP, "tablebackground.timeofday.edge", "imgs/columns_edge.jpg");
    public static final StringProperty TIME_OF_DAY_BACKGROUND_EARLY = new StringProperty(
      PROP, "tablebackground.timeofday.early", "imgs/columns_early.jpg");
    public static final StringProperty TIME_OF_DAY_BACKGROUND_MIDDAY = new StringProperty(
      PROP, "tablebackground.timeofday.midday", "imgs/columns_midday.jpg");
    public static final StringProperty TIME_OF_DAY_BACKGROUND_AFTERNOON = new StringProperty(
      PROP, "tablebackground.timeofday.afternoon",
      "imgs/columns_afternoon.jpg");
    public static final StringProperty TIME_OF_DAY_BACKGROUND_EVENING = new StringProperty(
      PROP, "tablebackground.timeofday.evening", "imgs/columns_evening.jpg");
    
    public static final ColorProperty COLOR_BACKGROUND_SINGLE = new ColorProperty(
      PROP, "backgroundSingleColor", Color.white);
    
    public static final StringProperty ONE_IMAGE_BACKGROUND = new StringProperty(
      PROP, "tablebackground.oneImage.image", "imgs/columns_evening.jpg");
    
    public static final IntProperty TIME_BLOCK_SIZE = new IntProperty(PROP,
      "tablebackground.timeBlock.size", 2);
    public static final StringProperty TIME_BLOCK_BACKGROUND1 = new StringProperty(
      PROP, "tablebackground.timeBlock.image1", "imgs/time_block_white.png");
    public static final StringProperty TIME_BLOCK_BACKGROUND2 = new StringProperty(
      PROP, "tablebackground.timeBlock.image2", "imgs/time_block_gray.png");
    public static final BooleanProperty TIME_BLOCK_SHOW_WEST = new BooleanProperty(
      PROP, "tablebackground.timeBlock.showWest", true);
    public static final StringProperty TIME_BLOCK_WEST_IMAGE1 = new StringProperty(
      PROP, "tablebackground.timeBlock.west1", "imgs/time_block_white.png");
    public static final StringProperty TIME_BLOCK_WEST_IMAGE2 = new StringProperty(
      PROP, "tablebackground.timeBlock.west2", "imgs/time_block_gray.png");
    
    private ProgramTable() {}
  }
  
  public static final class Proxy {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#proxy";
    
    public static final BooleanProperty USE = new BooleanProperty(
      PROP, "proxy.http.useProxy", false);
    public static final StringProperty HOST = new StringProperty(
      PROP, "proxy.http.host", "");
    public static final StringProperty PORT = new StringProperty(
      PROP, "proxy.http.port", "");
    public static final BooleanProperty AUTHENTIFY_AT_PROXY = new BooleanProperty(
      PROP, "proxy.http.authentifyAtProxy", false);
    public static final StringProperty USER = new StringProperty(
      PROP, "proxy.http.user", "");
    public static final EncodedStringProperty PASSWORD = new EncodedStringProperty(
      PROP, "proxy.http.password", "", PROXY_PASSWORD_SEED);
    
    private Proxy() {}
  }
  
  public static final class ToolBar {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#toolbar";
    
    public static final BooleanProperty IS_VISIBLE = new BooleanProperty(
      PROP, "isToolbarVisible", true);
    
    /**
     * Show the SearchField in the Toolbar
     */
    public static final BooleanProperty IS_SEARCH_FIELD_VISIBLE = new BooleanProperty(
      PROP, "isSearchFieldVisible", true);
    public static final StringProperty LOCATION = new StringProperty(
      PROP, "toolbarLocation", "north");
    public static final ChoiceProperty BUTTON_STYLE = new ChoiceProperty(
      PROP, "buttontype", "icon", new String[] { "text&icon", "text", "icon" });
    public static final BooleanProperty BIG_ICONS_USE = new BooleanProperty(
      PROP, "toolbarUseBigIcons", true);
    public static final StringArrayProperty BUTTONS = new StringArrayProperty(
      PROP, "toolbarButtons_2.0", null // we show all buttons, if this property
      // is not set
      );
      
    /**
     * Property to store if additonal space should be inserted above toolbar.
     */
    public static final BooleanProperty ADDITIONAL_TOP_SPACE = new BooleanProperty(
        PROP, "isToolbarAdditonalTopSpace", false);
  
    /**
    * Property to store if additonal space should be inserted below toolbar.
    */
   public static final BooleanProperty ADDITIONAL_BOTTOM_SPACE = new BooleanProperty(
       PROP, "isToolbarAddtionalBottomSpace", false);
   
   /**
    * @since 4.2.5
    */
   public static final BooleanProperty PLUGIN_FUNCTIONS_IN_MENU_SHOW = new BooleanProperty(
       PROP, "showPluginFunctionsInToolbarMenu", true);
    
    private ToolBar() {}
  }
  
  public static final class Tray {
    private static final void initialize() {
      Tray.Channels.initialize();
      Tray.Important.initialize();
      Tray.Now.initialize();
      Tray.OnTime.initialize();
      Tray.Soon.initialize();
    }
    
    public static final String ID = "#tray";
    
    public static final BooleanProperty ENABLED = new BooleanProperty(
      PROP, "trayIsEnabled", true);
    public static final BooleanProperty MINIMIZE_TO = new BooleanProperty(
      PROP, "MinimizeToTray", false);
    public static final BooleanProperty NOW_ON_RESTORE = new BooleanProperty(
      PROP, "jumpNowOnRestore",true);
    public static final BooleanProperty ANTIALIASING = new BooleanProperty(
      PROP, "trayIsAntialiasing", true);
    public static final BooleanProperty FILTER_NOT_MARKED = new BooleanProperty(
      PROP, "trayFilterNotMarked",false);
    public static final BooleanProperty FILTER_NOT = new BooleanProperty(
      PROP, "trayFilterAll",false);
    
    private Tray() {}
    
    public static final class Important {
      private static final void initialize() {
        //does nothing, just call this method to initialize static members of this class
      }
      
      public static final String ID = "#trayImportant";
      
      public static final BooleanProperty ENABLED = new BooleanProperty(
        PROP, "trayImportantProgramsEnabled", true);
      public static final BooleanProperty IN_SUB_MENU = new BooleanProperty(
        PROP, "trayImportantProgramsInSubMenu", false);
      public static final IntProperty SIZE = new IntProperty(
        PROP, "trayImportantProgramsSize", 5);
      public static final BooleanProperty CONTAINS_NAME = new BooleanProperty(
        PROP, "trayImportantProgramsContainsName", true);
      public static final BooleanProperty CONTAINS_ICON = new BooleanProperty(
        PROP, "trayImportantProgramsContainsIcon", true);
      public static final BooleanProperty CONTAINS_DATE = new BooleanProperty(
        PROP, "trayImportantProgramsContainsDate", true);
      public static final BooleanProperty CONTAINS_TIME = new BooleanProperty(
        PROP, "trayImportantProgramsContainsTime", true);
      public static final BooleanProperty CONTAINS_TOOL_TIP = new BooleanProperty(
        PROP, "trayImportantProgramsContainsToolTip", true);
      public static final IntProperty PRIORITY = new IntProperty(
        PROP, "trayImportantProgramsPriority", 0);
      public static final BooleanProperty SORT_NUMBER_SHOW = new BooleanProperty(
        PROP, "trayImportantProgramsShowingSortNumber", true);
      
      private Important() {}
    }
    
    public static final class Now {
      private static final void initialize() {
        //does nothing, just call this method to initialize static members of this class
      }
      
      public static final String ID = "#trayNow";
      
      public static final BooleanProperty ENABLED = new BooleanProperty(
        PROP, "trayNowProgramsEnabled", true);
      public static final BooleanProperty IN_SUB_MENU = new BooleanProperty(
        PROP, "trayNowProgramsInSubMenus", false);
      public static final BooleanProperty CONTAINS_NAME = new BooleanProperty(
        PROP, "trayNowProgramsContainsName", true);
      public static final BooleanProperty CONTAINS_ICON = new BooleanProperty(
        PROP, "trayNowProgramsContainsIcon", true);
      public static final BooleanProperty CONTAINS_TIME = new BooleanProperty(
        PROP, "trayNowProgramsContainsTime", false);
      public static final BooleanProperty CONTAINS_TOOL_TIP = new BooleanProperty(
        PROP, "trayNowProgramsContainsToolTip", true);
      public static final BooleanProperty SORT_NUMBER_SHOW = new BooleanProperty(
        PROP, "trayNowProgramsShowingSortNumber", true);
      
      private Now() {}
    }
    
    public static final class OnTime {
      private static final void initialize() {
        //does nothing, just call this method to initialize static members of this class
      }
      
      public static final String ID = "#trayOnTime";
      
      public static final BooleanProperty ENABLED = new BooleanProperty(
        PROP, "trayOnTimeProgramsEnabled", true);
      public static final BooleanProperty IN_SUB_MENU = new BooleanProperty(
        PROP, "trayOnTimeProgramsInSubMenus", true);
      public static final BooleanProperty CONTAINS_NAME = new BooleanProperty(
        PROP, "trayOnTimeProgramsContainsName", true);
      public static final BooleanProperty CONTAINS_ICON = new BooleanProperty(
        PROP, "trayOnTimeProgramsContainsIcon", true);
      public static final BooleanProperty CONTAINS_TIME = new BooleanProperty(
        PROP, "trayOnTimeProgramsContainsTime", false);
      public static final BooleanProperty CONTAINS_TOOL_TIP = new BooleanProperty(
        PROP, "trayOnTimeProgramsContainsToolTip", true);
      public static final BooleanProperty SORT_NUMBER_SHOW = new BooleanProperty(
        PROP, "trayOnTimeProgramsShowingSortNumber", true);
      public static final BooleanProperty PROGRESS_SHOW = new BooleanProperty(
        PROP, "trayOnTimeProgramsShowProgress", true);
      
      public static final ColorProperty COLOR_PROGRESS_BACKGROUND_LIGHT = new ColorProperty(
        PROP, "trayOnTimeProgramsLightBackground", new Color(255, 150, 0, 40));
      public static final ColorProperty COLOR_PROGRESS_BACKGROUND_DARK = new ColorProperty(
        PROP, "trayOnTimeProgramsDarkBackground", new Color(255, 150, 0, 80));
      
      private OnTime() {}
    }
    
    public static final class Channels {
      private static final void initialize() {
        //does nothing, just call this method to initialize static members of this class
      }
      
      public static final String ID = "#trayChannels";
            
      public static final BooleanProperty SPECIAL_USE = new BooleanProperty(
        PROP, "trayUseSpecialChannels", false);
      public static final ChannelArrayProperty SPECIAL = new ChannelArrayProperty(
        PROP, "traySpecialChannels", new devplugin.Channel[] {});
      public static final IntProperty WIDTH = new IntProperty(
        PROP, "trayChannelWidth", 78);
      
      private Channels() {}
    }
    
    public static final class Soon {
      private static final void initialize() {
        //does nothing, just call this method to initialize static members of this class
      }
      
      public static final String ID = "#traySoon";
      
      public static final BooleanProperty ENABLED = new BooleanProperty(
        PROP, "traySoonProgramsEnabled", true);
      public static final BooleanProperty CONTAINS_NAME = new BooleanProperty(
        PROP, "traySoonProgramsContainsName", true);
      public static final BooleanProperty CONTAINS_ICON = new BooleanProperty(
        PROP, "traySoonProgramsContainsIcon", true);
      public static final BooleanProperty CONTAINS_TIME = new BooleanProperty(
        PROP, "traySoonProgramsContainsTime", true);
      public static final BooleanProperty CONTAINS_TOOL_TIP = new BooleanProperty(
        PROP, "traySoonProgramsContainsToolTip", true);
      public static final BooleanProperty SORT_NUMBER_SHOW = new BooleanProperty(
        PROP, "traySoonProgramsShowingSortNumber", true);
      
      private Soon() {}
    }    
  }
  
  public static final class WebBrowser {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final String ID = "#webbrowser";
    
    public static final StringProperty USER_DEFINED = new StringProperty(
      PROP, "webbrowser", null);  
    public static final StringProperty USER_DEFINED_PARAMS = new StringProperty(
      PROP, "webbrowserParams", "{0}");
    
    /**
     * Show the "The Browser was opened"-Dialog
     */
    public static final BooleanProperty OPEN_BROWSER_DIALOG_SHOW = new BooleanProperty(
      PROP, "showBrowserOpenDialog", true);
    
    private WebBrowser() {}
  }
  
  public static final class Window {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final BooleanProperty MAXIMIZED = new BooleanProperty(
      PROP, "window.isMaximized", false);

    /**
     * Store if TV-Browser window is in native full screen mode of macOS
     * @since 4.2.4
     */
    public static final BooleanProperty MAC_OS_FULL_SCREEN = new BooleanProperty(
          PROP, "window.isInMacOSFullScreen", false);
  
  
    public static final IntProperty WIDTH = new IntProperty(PROP,
        "window.width", 770);
  
    public static final IntProperty HEIGHT = new IntProperty(PROP,
        "window.height", 550);
  
    public static final IntProperty X = new IntProperty(PROP,
        "window.x", -1);
  
    public static final IntProperty Y = new IntProperty(PROP,
        "window.y", -1);
        
   /**
     * Property to store visibility state of menu bar.
     */
    public static final BooleanProperty MENU_BAR_VISIBLE = new BooleanProperty(
        PROP, "isMenubarVisible", true);
  
    
    public static final BooleanProperty STATUS_BAR_VISIBLE = new BooleanProperty(
        PROP, "isStatusbarVisible", true);
    
    public static final BooleanProperty PLUGIN_VIEW_SHOW = new BooleanProperty(
        PROP, "show.pluginview", false);
  
    public static final BooleanProperty TIME_BUTTONS_SHOW = new BooleanProperty(
        PROP, "show.timebuttons", true);
  
    public static final BooleanProperty CHANNEL_SELECTION_SHOW = new BooleanProperty(
        PROP, "show.channels", true);
  
    public static final BooleanProperty DATE_SELECTION_SHOW = new BooleanProperty(
        PROP, "show.datelist", true);
  
    public static final BooleanProperty FILTER_BAR_SHOW = new BooleanProperty(
        PROP, "show.filterbar", true);
        
    public static final BooleanProperty ASSISTANT_SHOW = new BooleanProperty(
      PROP, "showassistant", true);
  
    /**
     * the last active program filter
     */
    public static final StringProperty FILTER_LAST_USED = new StringProperty(
      PROP, "lastusedfilter", null);
  
    public static final SplitViewProperty VIEW_ROOT = new SplitViewProperty(
      PROP, "view.root", false, true, 200);

    public static final SplitViewProperty VIEW_MAIN_FRAME = new SplitViewProperty(
      PROP, "view.mainframe", false, false, 150);

    public static final SplitViewProperty VIEW_NAVIGATION = new SplitViewProperty(
      PROP, "view.navigation", true, true, 150);

    public static final SplitViewProperty VIEW_DATE_CHANNEL = new SplitViewProperty(
      PROP, "view.date_channel", true, true, 150);
    
    public static final IntProperty SCREEN_NUMBER = new IntProperty(
        PROP, "screenNumber", -1);
    
    
    private Window() {}
  }
  
  public static final class Data {
    private static final void initialize() {
      //does nothing, just call this method to initialize static members of this class
    }
    
    public static final IntProperty DOWNLOAD_PERIOD = new IntProperty(PROP,
      "downloadperiod", 1);

    public static final BooleanProperty SAVE_DEFAULT_DATA_UPDATE_VALUES_DEFAULT = new BooleanProperty(
        PROP, "saveDefaultDataUpdateValuesDefault", true);
  
    public static final DateProperty DOWNLOAD_DATE_LAST = new DateProperty(
        PROP, "lastdownload", Date.getCurrentDate().addDays(-100));
        
    public static final StringArrayProperty DATA_SERVICES_FOR_UPDATE = new StringArrayProperty(
      PROP, "tvdataservices.update", null);
    
    /**@since 4.2.2*/
    public static final IntProperty DOWNLOAD_TIME_LAST = new IntProperty(
        PROP, "lastdownloadTime", 0);
        
    /** An array with the ids of the TV data service which license was accepted. */
    public static final StringArrayProperty ACCEPTED_LICENSES = new StringArrayProperty(
      PROP, "licnseIds", new String[] {});
    
    /**
     * The time between auto updates of data services
     * @since 2.7
     */
    public static final IntProperty DATA_SERVICE_AUTO_UPDATE_TIME = new IntProperty(
      PROP, "dataServiceAutoUpdateTime", 30);
    
    private Data() {}
  }
  
  
}