/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package tvbrowser;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Authenticator;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.SocketException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.lang3.StringUtils;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.looks.LookUtils;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.Version;
import tvbrowser.core.ChannelList;
import tvbrowser.core.DummyChannel;
import tvbrowser.core.PendingMarkings;
import tvbrowser.core.PluginLoader;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.GenericFilterMap;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.programformating.GlobalPluginProgramFormatingManager;
import tvbrowser.core.protocolhandler.ProtocolHandler;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.programinfo.ProgramInfo;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import tvbrowser.extras.searchplugin.SearchPlugin;
import tvbrowser.ui.DontShowAgainOptionBox;
import tvbrowser.ui.configassistant.TvBrowserPictureSettingsUpdateDialog;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.SoftwareUpdater;
import tvbrowser.ui.mainframe.UpdateDlg;
import tvbrowser.ui.settings.GeneralSettingsTab;
import tvbrowser.ui.settings.LookAndFeelSettingsTab;
import tvbrowser.ui.settings.MarkingsSettingsTab;
import tvbrowser.ui.settings.ProgramPanelSettingsTab;
import tvbrowser.ui.splashscreen.DummySplash;
import tvbrowser.ui.splashscreen.Splash;
import tvbrowser.ui.splashscreen.SplashScreen;
import tvbrowser.ui.tray.SystemTray;
import tvbrowser.ui.update.PluginAutoUpdater;
import tvbrowser.ui.update.SoftwareUpdateDlg;
import tvbrowser.ui.update.SoftwareUpdateItem;
import tvbrowser.ui.update.TvBrowserVersionChangeDlg;
import tvdataservice.MarkedProgramsMap;
import util.browserlauncher.Launch;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.i18n.Localizer;
import util.io.IOUtilities;
import util.io.Mirror;
import util.io.windows.registry.RegistryKey;
import util.io.windows.registry.RegistryValue;
import util.misc.BooleanResult;
import util.misc.OperatingSystem;
import util.ui.EnhancedPanelBuilder;
import util.ui.ImageUtilities;
import util.ui.ProgramPanel;
import util.ui.ScrollableJPanel;
import util.ui.UIThreadRunner;
import util.ui.UiUtilities;
import util.ui.textcomponentpopup.TextComponentPopupEventQueue;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class TVBrowser {
  private static final class LockFileResult {
    private boolean mResult;
    private String[] mLines;
    
    public LockFileResult(boolean result, String[] lines) {
      mResult = result;
      mLines = lines;
    }
  }
  
 // private static final String SUN_JAVA_WARNING = "TV-Browser was developed for Sun Java and may not run correctly with your Java implementation.";

  private static final Logger LOG
    = Logger.getLogger(TVBrowser.class.getName());

  /** The localizer for this class. */
  private static Localizer LOCALIZER;

  private static String CUR_LOOK_AND_FEEL;
  
  public static final ArrayList<Image> ICONS_WINDOW = new ArrayList<Image>(4);

  private static final boolean IS_STABLE = false;
  private static final int MAJRO_VERSION = 4;
  private static final int MINOR_VERSION = 24;
  private static final int SUB_MINOR_VERSION = 51;

  /* If you want to change the version string, add it to the beginning of this array.
     We need the old version strings to import the settings.
     
     !!!!!!!!!!!!
     
     ATTENTION: NEVER USE - IN THE NAME OF A VERSION. IT WILL CAUSE PROBLEMS FOR NIGHTLY
                USERS!!!
                
     !!!!!!!!!!!!
  */
  /** The string array with the names of the earlier versions. */
  private static final String[] ALL_VERSIONS = new String[] {
      "4.2.4.51 SVN",
      "4.2.4.50 SVN",
          "4.2.4", "4.2.3.95 Beta1", "4.2.3.50 SVN",
          "4.2.3", "4.2.2.96 RC1", "4.2.2.95 Beta1", "4.2.2.52 SVN", "4.2.2.51 SVN", "4.2.2.50 SVN",
          "4.2.2", "4.2.1.96 RC1", "4.2.1.95 Beta1", "4.2.1.52 SVN", "4.2.1.51 SVN", "4.2.1.50 SVN", 
          "4.2.1", "4.2.0.97 RC1", "4.2.0.51 SVN", "4.2.0.50 SVN",
		      "4.2", "4.0.9.98 RC3", "4.0.9.98 RC2", "4.0.9.97 RC1", "4.0.9.96 Beta2", "4.0.9.95 Beta1", "4.0.1.50 SVN",
          "4.0.1", "4.0.0.97 RC1", "4.0.0.96 Beta2", "4.0.0.95 Beta1", "4.0.0.50 SVN",
          "4", "3.4.4.98 RC3", "3.4.4.97 RC2", "3.4.4.95 Beta1", "3.4.4.50 SVN",
          "3.4.4", "3.4.3.96 RC1", "3.4.3.95 Beta1", "3.4.3.52 SVN", "3.4.3.51 SVN", "3.4.3.50 SVN",
	        "3.4.3", "3.4.2.50 SVN", "3.4.2", "3.4.1.96 RC1", "3.4.1.95 Beta1", "3.4.1.50-SVN",
          "3.4.1a", "3.4.1", "3.4.0.99 RC", "3.4.0.98 RC", "3.4.0.97 RC", "3.4.0.96 RC", "3.4.0.95 Beta", "3.4.0.50-SVN",
          "3.4.0.1 Hotfix",
          "3.4", "3.3.97 RC", "3.3.96 Beta", "3.3.95 Beta", "3.3.3.51 SVN", "3.3.3.50 SVN",
          "3.3.3", "3.3.3beta1", "3.3.2.50 SVN",
          "3.3.2", "3.3.2beta1", "3.3.1.50 SVN", 
          "3.3.1", "3.3.1RC1", "3.3.1beta1", "3.3.0.51 SVN", "3.3.0.50 SVN",
	      "3.3a", "3.3", "3.3RC2", "3.3RC1", "3.3beta1", "3.2.1.51 SVN", "3.2.1.50 SVN",
          "3.2.1", "3.2.1RC1", "3.2.1beta2", "3.2.1beta1", "3.2.0.50 SVN",
          "3.2", "3.2RC1", "3.2beta2", "3.2beta1", "3.1.0.50 SVN",
          "3.1", "3.1RC2", "3.1RC1", "3.1beta2", "3.1beta1",
          "3.0.2.99 SVN", "3.0.2", "3.0.2 RC2", "3.0.2 RC1", "3.0.2beta1", "3.0.1.99 SVN",
          "3.0.1",
          "3.0", "3.0 RC3", "3.0 RC2", "3.0 RC1", "3.0beta2", "3.0beta1", "3.0 (alpha2)", "3.0 (alpha1)", "3.0 (alpha)", "3.0 (SVN)",
          "2.7.6",
          "2.7.5", "2.7.5 (SVN)",
          "2.7.4", "2.7.4 (SVN)",
          "2.7.3", "2.7.3beta", "2.7.3 (SVN)",
          "2.7.2", "2.7.2 RC3", "2.7.2 RC2", "2.7.2 RC1", "2.7.2beta", "2.7.2 (SVN)",
          "2.7.1", "2.7.1 RC1", "2.7.1beta1",
          "2.7.x (SVN)",
          "2.7", "2.7 RC2", "2.7 RC1", "2.7beta2", "2.7beta1", "2.7 (SVN)",
          "2.6.3", "2.6.3beta",
          "2.6.2",
          "2.6.1", "2.6.1beta", "2.6.1 (SVN)",
          "2.6", "2.6beta2", "2.6beta1", "2.6alpha3", "2.6alpha2", "2.6alpha1", "2.6 (alpha)",
          "2.5.3", "2.5.3beta3", "2.5.3beta2", "2.5.3beta1", "2.5.3 (alpha)",
          "2.5.2",
          "2.5.1", "2.5.1beta3", "2.5.1beta2", "2.5.1beta1",
          "2.5", "2.5beta3", "2.5beta2", "2.5beta1", "2.5 alpha",
          "2.2.5",
          "2.2.4",
          "2.2.3",
          "2.2.2", "2.2.2beta2", "2.2.2beta1",
          "2.2.1", "2.2.1beta3",
          "2.2", "2.2beta2", "2.2beta1", "2.2 (SVN)"
  };

  static {
    ICONS_WINDOW.add(ImageUtilities.createImage("imgs/tvbrowser128.png"));
    ICONS_WINDOW.add(ImageUtilities.createImage("imgs/tvbrowser48.png"));
    ICONS_WINDOW.add(ImageUtilities.createImage("imgs/tvbrowser32.png"));
    ICONS_WINDOW.add(ImageUtilities.createImage("imgs/tvbrowser16.png"));
    
    File nightlyValues = new File("NIGHTLY_VALUES");

    if(!IS_STABLE && nightlyValues.isFile()) {
      try {
        RandomAccessFile in = new RandomAccessFile(nightlyValues, "r");

        String versionAppendix = "-" + in.readLine();
        ALL_VERSIONS[0] += versionAppendix;

        in.close();
      } catch (Exception e) {
        // ignore
      }
    }
  }

  /** The current version. */

  private static final boolean mIsTransportable = new File("settings").isDirectory();
  public static final devplugin.Version VERSION=new devplugin.Version(MAJRO_VERSION,MINOR_VERSION,SUB_MINOR_VERSION,IS_STABLE,ALL_VERSIONS[0] + (mIsTransportable ? " transportable" : ""));

  /** The title bar string. */
  public static final String MAINWINDOW_TITLE="TV-Browser "+VERSION.toString();

  private static SystemTray mTray;

  private static MainFrame mainFrame;

  private static AtomicReference<UdpThread> mToggleSocket = new AtomicReference<>(null);
  
  private static AtomicReference<RandomAccessFile> mLockFile = new AtomicReference<>(null);
  private static AtomicReference<RandomAccessFile> mToggleLockFile = new AtomicReference<>(null);
  
  private static AtomicReference<FileLock> mLock = new AtomicReference<>(null);
  private static AtomicReference<FileLock> mToggleLock = new AtomicReference<>(null);

  private static WindowAdapter mMainWindowAdapter;

  /**
   * Specifies whether the save thread should stop. The save thread saves every
   * 5 minutes the settings.
   */
  private static boolean mSaveThreadShouldStop;
  
  private static boolean mSaveThreadIsRunning;

  /**
   * Show the SplashScreen during startup
   */
  private static boolean mShowStartScreen = true;

  /**
   * Show TV-Browser in fullscreen
   */
  private static boolean mFullscreen = false;

  /**
   * Show only minimized
   */
  private static boolean mMinimized = false;
  
  /**
   * Start TV-Browser in safe mode (no external plugins will be loaded).
   */
  private static boolean mSafeMode = false;

  /**
   * avoid initializing the look and feel multiple times
   */
  private static boolean lookAndFeelInitialized = false;

  private static Timer mAutoDownloadWaitingTimer;

  /**
   * restart functionality
   */
  private static String[] RESTART_CMD = null;
  
  private static String mProtocolMessage = null;
  
  private static Version VERSION_LAST = null;
  private static boolean IS_TVB_UPDATE = false;
  
  /**
   * Entry point of the application
   * @param args The arguments given in the command line.
   */
  public static void main(String[] args) {
    // Set the String to use for indicating the user agent in http requests
    System.setProperty("http.agent", MAINWINDOW_TITLE);

    // Read the command line parameters
    parseCommandline(args);
    
    try {
      Toolkit.getDefaultToolkit().setDynamicLayout((Boolean) Toolkit.getDefaultToolkit().getDesktopProperty("awt.dynamicLayoutSupported"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    LOCALIZER = util.i18n.Localizer.getLocalizerFor(TVBrowser.class);

    // Check whether the TV-Browser was started in the right directory
    if ( !new File("imgs").exists()) {
      String msg = "Please start TV-Browser in the TV-Browser directory!";
      if (LOCALIZER != null) {
        msg = LOCALIZER.msg("error.2",
          "Please start TV-Browser in the TV-Browser directory!");
      }
      UiUtilities.showMessageDialogOnMouseScreen(msg, Localizer.getLocalization(Localizer.I18N_INFO),JOptionPane.INFORMATION_MESSAGE);
      System.exit(1);
    }

    if(mIsTransportable) {
      System.getProperties().remove("propertiesfile");
    }

    // setup logging

    // Get the default Logger
    final Logger mainLogger = Logger.getLogger("");

    // Use a even simpler Formatter for console logging
    mainLogger.getHandlers()[0].setFormatter(createFormatter());
    
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread thread, Throwable t) {
        mainLogger.log(Level.SEVERE, "UNCAUGHT EXCEPTION IN THREAD '" + thread.getName() + "'", t);
      }
    });
    
    if(mIsTransportable) {
      File settingsDir = new File("settings");
      try {
        File test = File.createTempFile("write","test",settingsDir);
        test.delete();
      } catch (IOException e) {
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
          //ignore
        }

        JTextArea area = new JTextArea(LOCALIZER.msg("error.noWriteRightsText","You are using the transportable version of TV-Browser but you have no writing rights in the settings directory:\n\n{0}'\n\nTV-Browser will be closed.",settingsDir.getAbsolutePath()));
        area.setFont(new JLabel().getFont());
        area.setFont(area.getFont().deriveFont((float)14).deriveFont(Font.BOLD));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setPreferredSize(new Dimension(500,100));
        area.setEditable(false);
        area.setBorder(null);
        area.setOpaque(false);

        UiUtilities.showMessageDialogOnMouseScreen(area,LOCALIZER.msg("error.noWriteRightsTitle","No write rights in settings directory"),JOptionPane.ERROR_MESSAGE);
        System.exit(1);
      }
    }
    
    RESTART_CMD = generateRestartCMD();
    
    // Load the settings
    Settings.loadSettings();
    Locale.setDefault(new Locale(Settings.Locales.LANGUAGE.getString(), Settings.Locales.COUNTRY.getString()));

    if (Settings.General.DATE_FIRST_START.getDate() == null) {
      Settings.General.DATE_FIRST_START.setDate(Date.getCurrentDate());
    }
    
    if (!createLockFile(mLockFile,mLock,".lock").mResult) {
      LockFileResult resultLockFile = createLockGlobalToggle();
      int port = Integer.MIN_VALUE;
      
      if(mProtocolMessage != null) {
        if(resultLockFile.mLines != null && resultLockFile.mLines.length == 1) {
          try {
            port = Integer.parseInt(resultLockFile.mLines[0]);
          }catch(NumberFormatException nfe) {}
        }
        
        if (!resultLockFile.mResult && port != Integer.MIN_VALUE) {
          try(DatagramSocket socket = new DatagramSocket()) {
            byte[] buf = mProtocolMessage.getBytes();
            
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), port);
            socket.send(packet);
            System.exit(0);
          } catch (Exception e) {e.printStackTrace();
            System.exit(-1);
          }
        }
        
        System.exit(-1);
      }
      
      updateLookAndFeel();
      showTVBrowserIsAlreadyRunningMessageBox(resultLockFile);
    }
    else {
      createLockGlobalToggle();
    }
    
    String logDirectory = Settings.Directories.LOG.getString();
    if (logDirectory != null) {
      try {
        File logDir = new File(logDirectory);
        logDir.mkdirs();
        mainLogger.addHandler(new FileLoggingHandler(logDir.getAbsolutePath()+"/tvbrowser.log", createFormatter()));
      } catch (IOException exc) {
        String msg = LOCALIZER.msg("error.4", "Can't create log file.");
        ErrorHandler.handle(msg, exc);
      }
    }
    else {
      Handler handlerObj = new ConsoleHandler();
      handlerObj.setLevel(Level.OFF);
      mainLogger.addHandler(handlerObj);
      
      // if no logging is configured, show WARNING or worse for normal usage, show everything for unstable versions
      if (TVBrowser.isStable()) {
        mainLogger.setLevel(Level.WARNING);
      }
      mainLogger.setUseParentHandlers(false);
    }

    // log warning for OpenJDK users
    /*if (!isJavaImplementationSupported()) {
      mainLogger.warning(SUN_JAVA_WARNING);
    }*/
        
    /* Set the proxy settings
     * 
     * ATTENTION: This has to be done before all Internet connections
     */
    updateProxySettings();
    
    VERSION_LAST = Settings.General.TV_BROWSER_VERSION_USED_LAST.getVersion();
    
    //Update plugin on version change
    if(VERSION.isNewerThan(VERSION_LAST)) {
      updateLookAndFeel();
      updatePluginsOnVersionChange();
      IS_TVB_UPDATE = true;
    }
    else if(VERSION_LAST != null && (Settings.General.DATE_OLD_SETTINGS_CHECKED_LAST.getDate() == null || Settings.General.DATE_OLD_SETTINGS_CHECKED_LAST.getDate().addDays(180).compareTo(Date.getCurrentDate()) < 0)) {
      updateLookAndFeel();
      seachForOldVersionFiles();
    }

    String timezone = Settings.Locales.TIMEZONE.getString();
    if (timezone != null) {
      TimeZone.setDefault(TimeZone.getTimeZone(timezone));
    }
    
    LOG.info("Using timezone "+TimeZone.getDefault().getDisplayName());

    // refresh the localizers because we know the language now
    Localizer.emptyLocalizerCache();
    LOCALIZER = Localizer.getLocalizerFor(TVBrowser.class);
    ProgramInfo.resetLocalizer();
    ReminderPlugin.resetLocalizer();
    Date.resetLocalizer();
    ProgramFieldType.resetLocalizer();

    Version tmpVer = Settings.General.TV_BROWSER_VERSION_USED_LAST.getVersion();
    final Version currentVersion = tmpVer != null ? new Version(tmpVer.getMajor(),tmpVer.getMinor(),tmpVer.getSubMinor(),Settings.General.TV_BROWSER_VERSION_USED_LAST_IS_STABLE.getBoolean()) : tmpVer;

    /*TODO Create an update service for installed TV data services that doesn't
     *     work with TV-Browser 3.0 and updates for them are known.
     */
    if(!isTransportable() && Launch.isOsWindowsNtBranch() && new Version(3,0,true).isNewerThan(currentVersion)) {
      String tvDataDir = Settings.Directories.TV_DATA.getString().replace("/",File.separator);

      if(!tvDataDir.startsWith(System.getenv("appdata"))) {
        StringBuilder oldDefaultTvDataDir = new StringBuilder(System.getProperty("user.home")).append(File.separator).append("TV-Browser").append(File.separator).append("tvdata");

        if(oldDefaultTvDataDir.toString().equals(tvDataDir)) {
          Settings.Directories.TV_DATA.setString(Settings.Directories.TV_DATA.getDefault());
        }
      }
    }
    
    Settings.General.TV_BROWSER_VERSION_USED_LAST.setVersion(VERSION);
    Settings.General.TV_BROWSER_VERSION_USED_LAST_IS_STABLE.setBoolean(VERSION.isStable());

    final AtomicReference<Splash> splashRef = new AtomicReference<Splash>();

    if (mShowStartScreen && Settings.General.START_SCREEN_SHOW.getBoolean()) {
      splashRef.set(new SplashScreen());
      splashRef.get().showSplash();
    }
    else {
      if(java.awt.SplashScreen.getSplashScreen() != null && java.awt.SplashScreen.getSplashScreen().isVisible()) {
        java.awt.SplashScreen.getSplashScreen().close();
      }
      splashRef.set(new DummySplash());
    }
    
    LOG.info("Deleting expired TV listings...");
    TvDataBase.getInstance().deleteExpiredFiles(TvDataBase.DEFAULT_DATA_LIFESPAN, false);

    /* Initialize the MarkedProgramsMap */
    MarkedProgramsMap.getInstance();

    if(!mSafeMode) {
      /*Maybe there are tvdataservices to install (.jar.inst files)*/
      PluginLoader.getInstance().installPendingPlugins();
    }
    
    PluginProxyManager.getInstance();
    
    if(!mSafeMode) {
      PluginLoader.getInstance().loadAllPlugins();
    }
    
    SearchPlugin.getInstance();

    LOG.info("Loading TV listings service...");
    splashRef.get().setMessage(LOCALIZER.msg("startScreen.dataService", "Loading TV listings service..."));
    
    TvDataServiceProxyManager.getInstance().init();
    
    if(!Settings.Window.ASSISTANT_SHOW.getBoolean() && TvDataServiceProxyManager.getInstance().getDataServices().length < 1 && !mSafeMode) {
      splashRef.get().hideSplash();
      updateLookAndFeel();
      loadDataServicesAtStartup();
    }
    else {
      ChannelList.createForTvBrowserStart();
      ChannelList.initSubscribedChannels();
    }
    
    ChannelList.checkForJointChannels();
    
    //Preload generic filters 
    GenericFilterMap.getInstance();
    
    if (!lookAndFeelInitialized) {
      LOG.info("Loading Look&Feel...");
      splashRef.get().setMessage(LOCALIZER.msg("startScreen.laf", "Loading look and feel..."));
      updateLookAndFeel();
    }
    
    LOG.info("Loading plugins...");
    splashRef.get().setMessage(LOCALIZER.msg("startScreen.plugins", "Loading plugins..."));
    
    try {
      InternalPluginProxyList.getInstance();
      PluginProxyManager.getInstance().init();
    } catch(TvBrowserException exc) {
      ErrorHandler.handle(exc);
    }
    
    // Initialize filters of generic filter map
    GenericFilterMap.getInstance().initializeFilters();
    // Mark pending markings
    PendingMarkings.markMapEntries();
    
    splashRef.get().setMessage(LOCALIZER.msg("startScreen.tvData", "Checking TV database..."));

    LOG.info("Checking TV listings inventory...");
    TvDataBase.getInstance().checkTvDataInventory(TvDataBase.DEFAULT_DATA_LIFESPAN);

    LOG.info("Starting up...");
    splashRef.get().setMessage(LOCALIZER.msg("startScreen.ui", "Starting up..."));
    
    Toolkit.getDefaultToolkit().getSystemEventQueue().push(new TextComponentPopupEventQueue());
    
    // Init the UI
    final boolean fStartMinimized = Settings.General.MINIMIZE_AFTER_STARTUP.getBoolean() || mMinimized;
    SwingUtilities.invokeLater(() -> {
      initUi(splashRef.get(), fStartMinimized);

      new Thread("Start finished callbacks") {
        public void run() {
          setPriority(Thread.MIN_PRIORITY);
          
          // first reset "starting" flag of mainframe
          mainFrame.handleTvBrowserStartFinished();
          
          // first initialize the internal plugins
          InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList.getInstance().getAvailableProxys();
          
          for(InternalPluginProxyIf internalPlugin : internalPlugins) {
            internalPlugin.handleTvBrowserStartFinished();
          }
          
          // now handle all plugins and services
          GlobalPluginProgramFormatingManager.getInstance();
          
          if(IS_TVB_UPDATE) {
            PluginProxyManager.getInstance().fireTvBrowserVersionUpdate(VERSION_LAST);
            TvDataServiceProxyManager.getInstance().fireTvBrowserVersionUpdate(VERSION_LAST);
          }
          
          PluginProxyManager.getInstance().fireTvBrowserStartFinished();
          TvDataServiceProxyManager.getInstance().fireTvBrowserStartFinished();

          // finally submit plugin caused updates to database
          TvDataBase.getInstance().handleTvBrowserStartFinished();
          
          mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowIconified(java.awt.event.WindowEvent e) {
              mSaveThreadShouldStop = true;
              flushSettings(true);
            }
            
            public void windowDeiconified(java.awt.event.WindowEvent e) {
              mSaveThreadShouldStop = false;
              if (mSaveThreadIsRunning == false) {
                startPeriodicSaveSettings();   
              }
            }
          });

          startPeriodicSaveSettings();

          ChannelList.completeChannelLoading();
          initializeAutomaticDownload();
          
          ProtocolHandler.getInstance().handleMessage(mProtocolMessage);
        }
      }.start();
      SwingUtilities.invokeLater(() -> {
        if (Launch.isOsWindowsNtBranch()) {
          try {
            RegistryKey desktopSettings = new RegistryKey(
                RegistryKey.HKEY_CURRENT_USER, "Control Panel\\Desktop");
            RegistryValue autoEnd = desktopSettings
                .getValue("AutoEndTasks");
            
            if (autoEnd.getData().equals("1")) {
              RegistryValue killWait = desktopSettings
                  .getValue("WaitToKillAppTimeout");

              if(!killWait.isUnknown()) {
	              int i1 = Integer.parseInt(killWait.getData());
	
	              if (i1 < 5000) {
	                JOptionPane pane = new JOptionPane();
	
	                String cancel = LOCALIZER.msg("registryCancel",
	                    "Close TV-Browser");
	                String dontDoIt = LOCALIZER.msg("registryJumpOver",
	                    "Not this time");
	
	                pane.setOptions(new String[] {
	                    Localizer.getLocalization(Localizer.I18N_OK), dontDoIt,
	                    cancel });
	                pane.setOptionType(JOptionPane.YES_NO_CANCEL_OPTION);
	                pane.setMessageType(JOptionPane.WARNING_MESSAGE);
	                pane
	                    .setMessage(LOCALIZER
	                        .msg(
	                            "registryWarning",
	                            "The fast shutdown of Windows is activated.\nThe timeout to wait for before Windows is closing an application is too short,\nto give TV-Browser enough time to save all settings.\n\nThe setting hasn't the default value. It was changed by a tool or by you.\nTV-Browser will now try to change the timeout.\n\nIf you don't want to change this timeout select 'Not this time' or 'Close TV-Browser'."));
	
	                pane.setInitialValue(LOCALIZER.msg("registryCancel",
	                    "Close TV-Browser"));
	
	                JDialog d = pane.createDialog(UiUtilities
	                    .getLastModalChildOf(mainFrame), UIManager
	                    .getString("OptionPane.messageDialogTitle"));
	                d.setModalityType(ModalityType.DOCUMENT_MODAL);
	                UiUtilities.centerAndShow(d);
	
	                if (pane.getValue() == null
	                    || pane.getValue().equals(cancel)) {
	                  mainFrame.quit();
	                } else if (!pane.getValue().equals(dontDoIt)) {
	                  try {
	                    
	                    killWait.setData("5000");
	                    boolean result = desktopSettings.setValue(killWait);
	                    
	                    if(!result) {
	                      throw new Exception("Registry Value could not be set.");
	                    }
	                    
	                    JOptionPane
	                        .showMessageDialog(
	                            UiUtilities.getLastModalChildOf(mainFrame),
	                            LOCALIZER
	                                .msg("registryChanged",
	                                    "The timeout was changed successfully.\nPlease reboot Windows!"));
	                  } catch (Exception registySetting) {
	                    JOptionPane
	                        .showMessageDialog(
	                            UiUtilities.getLastModalChildOf(mainFrame),
	                            LOCALIZER
	                                .msg(
	                                    "registryNotChanged",
	                                    "<html>The Registry value couldn't be changed. Maybe you haven't the right to do it.<br>If it is so contact you Administrator and let him do it for you.<br><br><b><Attention:/b> The following description is for experts. If you change or delete the wrong value in the Registry you could destroy your Windows installation.<br><br>To get no warning on TV-Browser start the Registry value <b>WaitToKillAppTimeout</b> in the Registry path<br><b>HKEY_CURRENT_USER\\Control Panel\\Desktop</b> have to be at least <b>5000</b> or the value for <b>AutoEndTasks</b> in the same path have to be <b>0</b>.</html>"),
	                            Localizer.getLocalization(Localizer.I18N_ERROR),
	                            JOptionPane.ERROR_MESSAGE);
	                  }
	                }
	              }
              }
            }
          } catch (Throwable registry) {
          }
        }
        
        // check if user should select picture settings
        if (currentVersion != null
            && currentVersion.compareTo(new Version(2, 22)) < 0) {
          TvBrowserPictureSettingsUpdateDialog.createAndShow(mainFrame);
        } 
        
        if (currentVersion != null
            && currentVersion.compareTo(new Version(2, 51, true)) < 0) {
          Settings.Data.ACCEPTED_LICENSES.setStringArray(new String[0]);
        } 
        
        if(currentVersion != null && currentVersion.compareTo(new Version(4, 21, 96, false)) < 0) {
          final String refresh = GeneralSettingsTab.LOCALIZER.msg("titleRefresh", "Refresh");
          
          JCheckBox gradient = new JCheckBox(ProgramPanelSettingsTab.LOCALIZER.msg("color.programGradientHighlighting",
              "Highlight programs with gradient colors"), Settings.ProgramPanel.HIGHLIGHTING_COLOR_GRADIENT.getBoolean());
          JCheckBox update = new JCheckBox(LOCALIZER.msg("update.primeTimeActivate","Activate prime time update"), Settings.General.AUTO_UPDATE_PRIME_TIME.getBoolean());
          
          EnhancedPanelBuilder pb = new EnhancedPanelBuilder("5dlu,10dlu,default,default:grow");
          
          pb.addParagraph(refresh);
          pb.addRowFull(UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("update.primeTimeUpdate","Independent of the configuration of the automatically data update, TV-Browser since 4.2.2 provides the option to activate an automatically data update of the prime time (after 6 pm). If activated TV-Browser will download the data for today and tomorrow each day right before the prime time. You can configure this options under <b><i>{0}, {1}</i></b>&nbsp;&nbsp;or directly here.",GeneralSettingsTab.LOCALIZER.msg("general","General settings"), refresh)), 2);
          pb.addRowFull(update, 2);
          pb.addParagraph(MarkingsSettingsTab.LOCALIZER.msg("title","Highlighting"));              
          pb.addRowFull(UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("update.gradientColors","Since TV-Browser 4.2.2 programs can be highlighted by more than one color at the same time. This is done by showing a color. The previous highlighting with only the color of the highest priority is still available and can be configured right here or anytime under <b><i>{0}->{1}, {2}</i></b>.",LookAndFeelSettingsTab.LOCALIZER.msg("graphical","Graphical settings"),ProgramPanelSettingsTab.LOCALIZER.msg("title","Program display"),ProgramPanelSettingsTab.LOCALIZER.msg("Colors", "Colors"))), 2);
          pb.addRowFull(gradient, 2);
          
          int fMarkPriorityOld = FavoritesPluginProxy.getInstance().getMarkPriorityMaxForProgram(null);
          int rMarkPriorityOld = ReminderPluginProxy.getInstance().getMarkPriorityMaxForProgram(null);
          
          Method fSetMarkPriority = null;
          Method rSetMarkPriority = null;
          
          try {
            fSetMarkPriority = FavoritesPlugin.class.getDeclaredMethod("setMarkPriority",int.class);
            rSetMarkPriority = ReminderPlugin.class.getDeclaredMethod("setMarkPriority",int.class);
            fSetMarkPriority.setAccessible(true);
            rSetMarkPriority.setAccessible(true);
            fSetMarkPriority.invoke(FavoritesPlugin.getInstance(), 4);
            rSetMarkPriority.invoke(ReminderPlugin.getInstance(), Program.PRIORITY_MARK_MIN);
          } catch (NoSuchMethodException | SecurityException | IllegalAccessException | 
              IllegalArgumentException | InvocationTargetException e1) {} 
          final ProgramPanel p = new ProgramPanel(PluginManagerImpl.getInstance().getExampleProgram());
          pb.addRow(p, 3);
          
          p.getProgram().mark(FavoritesPluginProxy.getInstance());
          p.getProgram().mark(ReminderPluginProxy.getInstance());
          
          gradient.addItemListener(e -> {
            Settings.ProgramPanel.HIGHLIGHTING_COLOR_GRADIENT.setBoolean(ItemEvent.SELECTED == e.getStateChange());
            p.repaint();
          });
          
          pb.getPanel().setBorder(Borders.DIALOG);
          pb.getPanel().setPreferredSize(new Dimension(Sizes.dialogUnitXAsPixel(450, pb.getPanel()), Sizes.dialogUnitYAsPixel(210, pb.getPanel())));
          
          UiUtilities.showMessageDialogOnMouseScreen(pb.getPanel(), LOCALIZER.msg("update.title","Changed functionality since TV-Browser {0}","4.2.2"), JOptionPane.PLAIN_MESSAGE);
          
          if(fSetMarkPriority != null && rSetMarkPriority != null)
          try {
            fSetMarkPriority.invoke(FavoritesPlugin.getInstance(), fMarkPriorityOld);
            rSetMarkPriority.invoke(ReminderPlugin.getInstance(), rMarkPriorityOld);
            fSetMarkPriority.setAccessible(false);
            rSetMarkPriority.setAccessible(false);
          } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {}
          
          p.getProgram().unmark(FavoritesPluginProxy.getInstance());
          p.getProgram().unmark(ReminderPluginProxy.getInstance());
          
          Settings.ProgramPanel.HIGHLIGHTING_COLOR_GRADIENT.setBoolean(gradient.isSelected());
          Settings.General.AUTO_UPDATE_PRIME_TIME.setBoolean(update.isSelected());
        }

        if (currentVersion != null
            && currentVersion.compareTo(new Version(2, 60, true)) < 0) {
          int startOfDay = Settings.ProgramTable.START_OF_DAY.getInt();
          int endOfDay = Settings.ProgramTable.END_OF_DAY.getInt();

          if (endOfDay - startOfDay < -1) {
            Settings.ProgramTable.END_OF_DAY.setInt(startOfDay);

            JOptionPane
                .showMessageDialog(
                    UiUtilities.getLastModalChildOf(mainFrame),
                    LOCALIZER
                        .msg(
                            "timeInfoText",
                            "The time range of the program table was corrected because the defined day was shorter than 24 hours.\n\nIf the program table should show less than 24h use a time filter for that. That time filter can be selected\nto be the default filter by selecting it in the filter settings and pressing on the button 'Default'."),
                    LOCALIZER.msg("timeInfoTitle", "Times corrected"),
                    JOptionPane.INFORMATION_MESSAGE);
            Settings.handleChangedSettings();
          }
        }
        
        if(currentVersion != null 
            && currentVersion.compareTo(new Version(3,43,52,false)) < 0) {
          FilterComponentList.getInstance().store();
        }
        
        if(currentVersion != null
            && currentVersion.compareTo(new Version(3,30,51,false)) < 0) {
          Settings.updateContextMenuSettings();
        }

        if(currentVersion != null
            && currentVersion.compareTo(new Version(3,33,51,false)) < 0) {
          Settings.Channels.SUBSCRIBED.setChannelArray(ChannelList.getSubscribedChannels());
        }
        
        if(currentVersion != null
            && currentVersion.compareTo(new Version(3,39,7,false)) < 0) {
          ProgramFieldType[] typeArr = Settings.ProgramPanel.INFO_FIELDS.getProgramFieldTypeArray();
          String[] separators = Settings.ProgramPanel.INFO_FIELDS_SEPARATORS.getStringArray();
          
          ArrayList<String> separatorList = new ArrayList<String>();
          
          for(int i2 = 0; i2 < typeArr.length - 1; i2++) {
            if(i2 < separators.length - 1 && separators[i2].equals("\n")) {
              separatorList.add(separators[i2]);
            }
            else {
              separatorList.add(" - ");
            }
          }
          
          Settings.ProgramPanel.INFO_FIELDS_SEPARATORS.setStringArray(separatorList.toArray(new String[separatorList.size()]));
        }
        
        if(currentVersion != null
            && currentVersion.compareTo(new Version(4,21,51,false)) < 0) {
          @SuppressWarnings("deprecation")
          int[] colors = {
              Settings.propProgramPanelMarkedMinPriorityColor.getColor().getRGB(),
              Settings.propProgramPanelMarkedLowerMediumPriorityColor.getColor().getRGB(),
              Settings.propProgramPanelMarkedMediumPriorityColor.getColor().getRGB(),
              Settings.propProgramPanelMarkedHigherMediumPriorityColor.getColor().getRGB(),
              Settings.propProgramPanelMarkedMaxPriorityColor.getColor().getRGB()
          };
          Settings.Markings.HIGHLIGHTING_COLORS.setIntArray(colors);
          Settings.updateColors();
        }
        
        MainFrame.getInstance().getProgramTableScrollPane()
            .requestFocusInWindow();
      });
    });

     // register the shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread("Shutdown hook") {
      public void run() {
        deleteLockFile(mLockFile.get(),mLock.get(),".lock");
        deleteLockGlobalToggle();
        MainFrame.getInstance().quit(false);
      }
     });
  }
  
  private static String[] generateRestartCMD(){
		try {
		final String SUN_JAVA_COMMAND = "sun.java.command";
		// init the command to execute, add the vm args
		final List<String> cmd = new ArrayList<String>();
		
		// java binary
		String java = System.getProperty("java.home");
		
		if(java == null) { 
			return null;
		}
		
		if(Launch.getOs() == Launch.OS_WINDOWS) {
			java = java.concat("\\bin\\javaw");
		}
		else {
			java = java.concat("/bin/java");
		}
		cmd.add(java);
		
		boolean splash = false;
		// vm arguments
		List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
		for (String arg : vmArguments) {
			if(arg.startsWith("-splash:")) {
				splash = true;
			}
			// if it's the agent argument : we ignore it otherwise the
			// address of the old application and the new one will be in
			// conflict
			if (!arg.contains("-agentlib")) {
				cmd.add(arg);
			}
		}

		// program main and program arguments
		if (System.getProperty(SUN_JAVA_COMMAND) == null) return null;
		
		String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
		// program main is a jar
		//StringBuilder sb = new StringBuilder(mainCommand[0]);
/*		int mainCommandSize;
		for (mainCommandSize=1; mainCommandSize < mainCommand.length && !mainCommand[mainCommandSize-1].endsWith(".jar"); mainCommandSize++) {
		  sb.append(' ').append(mainCommand[mainCommandSize]);
		}*/
		/*String jarFile = sb.toString();
		if (jarFile.endsWith(".jar")) {
			// if it's a jar, add -jar mainJar
			cmd.add("-jar");
			cmd.add(new File(jarFile).getPath());
		} else {*/
			// else it's a .class, add the classpath and mainClass
		    if(System.getProperty("java.class.path") != null && !System.getProperty("java.class.path").isBlank()) {
		      cmd.add("-cp");
			  cmd.add(System.getProperty("java.class.path"));
		    }
		    
		    if(!splash) {
		    	cmd.add("-splash:imgs/splash.png");
		    }
		    
			cmd.add("-m");
			cmd.add(mainCommand[0]);
	/*    mainCommandSize = 1;
		//}
    for (int i= mainCommandSize; i < mainCommand.length; i++) {
      cmd.add(mainCommand[i]);
    }*/
		// finally add program arguments		
		String[] cmdarr = new String[cmd.size()];
		for(int i=0;i<cmd.size();++i){
			cmdarr[i] = cmd.get(i);
		}
		
		return cmdarr;
		} catch (Exception e) {			// something went wrong
			e.printStackTrace();
			return null;
		}
	  
  }
  
  public static boolean restartEnabled(){
	  return (RESTART_CMD!=null);
  }
  
	public static void addRestart() {
		try{
			// execute the command in a shutdown hook, to be sure that all the
			// resources have been disposed before restarting the application
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						Thread.sleep(250);
						Runtime.getRuntime().exec(RESTART_CMD);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {			// something went wrong
			e.printStackTrace();
		}

	}
/*
  private static boolean isJavaImplementationSupported() {
    if (mIgnoreJVM) {
      return true;
    }
    String vendor = System.getProperty("java.vendor");
    if (!StringUtils.containsIgnoreCase(vendor, "sun") && !StringUtils.containsIgnoreCase(vendor, "oracle")) {
      return false;
    }
    String implementation = System.getProperty("java.vm.name");
    if (!StringUtils.containsIgnoreCase(implementation, "openjdk")) {
      return false;
    }
    return true;
  }*/
	
	private static void seachForOldVersionFiles() {
	  final String messageId = "TVBrowser#DeleteOldVersionFiles";
	  
	  if(!DontShowAgainOptionBox.isHiddenMessageBox(messageId)) {
  	  final File settingsDir = new File(Settings.getUserSettingsDirName()).getParentFile();
  	  final long cutoff = System.currentTimeMillis() - 6 * 30 * 24 * 60 * 60000l;
  	  final ArrayList<File> oldDirs = new ArrayList<File>();
  	  
  	  for(int i = ALL_VERSIONS.length-1; i > 0; i--) {
  	    File test = new File(settingsDir + File.separator + ALL_VERSIONS[i] + File.separator + "settings.prop");
  	    
  	    if(test.isFile() && test.lastModified() < cutoff) {
  	      oldDirs.add(test);
  	    }
  	  }
  	  
  	  if(!oldDirs.isEmpty()) {
    	  Collections.sort(oldDirs, new Comparator<File>() {
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
    	  
    	  try {
          UIThreadRunner.invokeAndWait(() -> {
            final JButton selectAll = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT_ALL));
            selectAll.setEnabled(true);
            final JButton clearSelection = new JButton(Localizer.getLocalization(Localizer.I18N_CLEAR_SELECTION));
            clearSelection.setEnabled(false);
            final JButton delete = new JButton(LOCALIZER.msg("deleteOldSettingsDelete", "Delete selected settings"));
            delete.setEnabled(false);
            delete.addActionListener(e -> {
              Container container = delete.getParent();
            
              do {
                container = container.getParent();
              }while(container != null && !(container instanceof JOptionPane));
              
              if(container != null && container instanceof JOptionPane) {
                JOptionPane p = (JOptionPane)container;
                p.setValue(delete);
              }
            });
            final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
            Localizer settingsLocalizer = Localizer.getLocalizerFor(Settings.class);
            final ScrollableJPanel boxPanel = new ScrollableJPanel();
            boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
            final AtomicInteger count = new AtomicInteger(0);
            
            final ItemListener listener = e -> {
              if(e.getStateChange() == ItemEvent.SELECTED) {
                count.incrementAndGet();
              }
              else if(e.getStateChange() == ItemEvent.DESELECTED) {
                count.decrementAndGet();
              }
              
              delete.setEnabled(count.get() > 0);
              selectAll.setEnabled(count.get() != oldDirs.size());
              clearSelection.setEnabled(delete.isEnabled());
            };
            
            final JCheckBox[] selection = new JCheckBox[oldDirs.size()];
            
            for(int i = 0; i < selection.length; i++) {
              final File dir = oldDirs.get(i);
              selection[i] = new JCheckBox(settingsLocalizer.msg("selectImportDirectoryInfo", "{0} (last used: {1})",dir.getParentFile().getName(),dateFormat.format(new java.util.Date(dir.lastModified()))));
              selection[i].addItemListener(listener);
              boxPanel.add(selection[i]);
            }
            
            final JScrollPane scroll = new JScrollPane(boxPanel);
            scroll.setBorder(null);
            scroll.setViewportBorder(null);
            scroll.getViewport().setOpaque(false);
            
            selectAll.addActionListener(e -> {
              for(JCheckBox box : selection) {
                box.setSelected(true);
              }
            });
            clearSelection.addActionListener(e -> {
              for(JCheckBox box : selection) {
                box.setSelected(false);
              }
            });
            
            final JPanel buttons = new JPanel(new FormLayout("default,60dlu,default","default"));
            buttons.add(clearSelection, CC.xy(1, 1));
            buttons.add(selectAll, CC.xy(3, 1));
            
            
            final ArrayList<Object> message = new ArrayList<>();
            message.add(LOCALIZER.msg("deleteOldSettingsMessage", "TV-Browser has found settings of old versions of TV-Browser\nthat were not used for at least half a year.\n\nYou can select the versions of TV-Browser you no longer use,\nfor which the old setttings should be deleted now.\n\n"));
            message.add(scroll);
            message.add(buttons);
            
            final Object[] options = {
                delete,
                Localizer.getLocalization(Localizer.I18N_CANCEL)
            };
            
            int option = DontShowAgainOptionBox.showOptionDialog(messageId, null, message.toArray(), LOCALIZER.msg("deleteOldSettingsTitle", "TV-Browser: Delete old versions settings files"), JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, options, options[1], null);
            
            if(JOptionPane.YES_OPTION == option) {
              for(int i = 0; i < selection.length; i++) {
                if(selection[i].isSelected()) {
                  eraseDirectory(oldDirs.get(i).getParentFile());
                }
              }
            }
          });
        } catch (InvocationTargetException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
  	  }
  	  
  	  Settings.General.DATE_OLD_SETTINGS_CHECKED_LAST.setDate(Date.getCurrentDate());
	  }
	}
	
	private static void eraseDirectory(final File directory) {
	  if(directory.isDirectory()) {
	    final File[] files = directory.listFiles();
	    
	    if(files != null) {
	      for(File file : files) {
	        if(file.isFile() && !file.delete()) {
	          file.deleteOnExit();
	        }
	        else if(file.isDirectory()) {
	          eraseDirectory(file);
	        }
	      }
	    }
	    
	    if(!directory.delete()) {
	      directory.deleteOnExit();
	    }
	  }
	}

  private static void startPeriodicSaveSettings() {
    // Every 5 minutes we store all the settings so they are stored in case of
    // an unexpected failure
    Thread saveThread = new Thread("Store settings periodically") {
      public void run() {
        mSaveThreadIsRunning = true;
        mSaveThreadShouldStop = false;
        while (! mSaveThreadShouldStop) {
          try {
            Thread.sleep(5 * 60 * 1000);
          }
          catch (Exception exc) {
            // ignore
          }

          if(!mSaveThreadShouldStop && !TvDataUpdater.getInstance().isDownloading()) {
            flushSettings(true);
          }
        }
        mSaveThreadIsRunning = false;
      }
    };
    saveThread.setPriority(Thread.MIN_PRIORITY);
    saveThread.start();
  }

  private static void showUsage(String[] args) {
    System.out.println("command line options:");
    System.out.println("    -minimized      The main window will be minimized after start up");
    System.out.println("    -nostartscreen  No start screen during start up");
    System.out.println("    -fullscreen     Start in fullscreen-mode");
    System.out.println("    -ignorejvm      Don't check for Sun Java");
    System.out.println("    -safemode       Don't load Plugins");
    System.out.println();
  }

  private static void parseCommandline(String[] args) {
    showUsage(args);
    for (String argument : args) {
      if (argument.equalsIgnoreCase("-help") || argument.equalsIgnoreCase("-h")) {
        System.exit(0);
      } else if (argument.equalsIgnoreCase("-minimized") || argument.equalsIgnoreCase("-m")) {
        mMinimized = true;
      } else if (argument.equalsIgnoreCase("-nostartscreen") || argument.equalsIgnoreCase("-n")) {
        mShowStartScreen = false;
      } else if (argument.equalsIgnoreCase("-fullscreen") || argument.equalsIgnoreCase("-f")) {
        mFullscreen = true;
      } else if (argument.equalsIgnoreCase("-safemode") || argument.equalsIgnoreCase("-s")) {
        mSafeMode = true;
      } else if (argument.startsWith("tvb://")) {
        mProtocolMessage = argument;
      } else if (argument.startsWith("-D")) {
        if (argument.indexOf("=") >= 2) {
          String key = argument.substring(2, argument.indexOf("="));
          String value = StringUtils.substringAfter(argument, "=");
          if (key.equals("user.language")) {
            System.getProperties().setProperty("user.language", value);
            Locale.setDefault(new Locale(value));
          } else {
            System.setProperty(key, value);
          }
        } else {
          LOG.warning("Wrong Syntax in parameter: '" + argument + "'");
        }
      } else if(!argument.trim().isEmpty()) {
        LOG.warning("Unknown command line parameter: '" + argument + "'");
      }
    }
  }


  private static LockFileResult createLockGlobalToggle() {
    LockFileResult result = new LockFileResult(true, null);
    
    if(Settings.General.SERVER_RESTORE_ENABLED.getBoolean()) {
      String[] lines = null;
      
      try {
        mToggleSocket.set(new UdpThread());
        lines = new String[1];
        lines[0] = String.valueOf(mToggleSocket.get().getSocket().getLocalPort());
      } catch (SocketException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      result = createLockFile(mToggleLockFile,mToggleLock,".toggle", lines);
    }
    
    return result;
  }
  
  private static void deleteLockGlobalToggle() {
    if(mToggleSocket.get() != null) {
      mToggleSocket.get().halt();
      mToggleSocket.set(null);
    }
    
    deleteLockFile(mToggleLockFile.get(),mToggleLock.get(),".toggle");
  }
  
  public static void updateLockGlobalToggle() {
    if(Settings.General.SERVER_RESTORE_ENABLED.getBoolean() && mToggleSocket.get() == null) {
      if(createLockGlobalToggle().mResult) {
        mToggleSocket.get().start();
      }
    }
    else if(!Settings.General.SERVER_RESTORE_ENABLED.getBoolean() && mToggleSocket.get() != null) {
      deleteLockGlobalToggle();
    }
  }
  
  private static String[] readLockFileContent(final File lockInfo) {
	String[] readLines = null;
	
	if(lockInfo.isFile()) {
	    final ArrayList<String> readList = new ArrayList<String>();
	    
	    try(RandomAccessFile in = new RandomAccessFile(lockInfo,"r")) {
	      String line = null;
	      
	      while((line = in.readLine()) != null) {
	        readList.add(line);
	      }
	    }catch(Exception ioe) {ioe.printStackTrace();}
	    
	    if(!readList.isEmpty()) {
	      readLines = readList.toArray(new String[0]);
	    }
	}
    
    return readLines;
  }
  
  /**
   * Create the .lock file in the user home directory
   * @return false, if the .lock file exist and is locked or cannot be locked.
   */
  private static LockFileResult createLockFile(final AtomicReference<RandomAccessFile> lockFileAccess, final AtomicReference<FileLock> lockTarget, final String file, final String... lines) {
    String dir = Settings.getUserDirectoryName();

    if(!new File(dir).isDirectory()) {
      new File(dir).mkdirs();
    }

    File lockFile = new File(dir, file);
    File lockInfo = new File(dir, file+"_info");
    
    if(lockFile.exists()) {
      try {
        lockFileAccess.set(new RandomAccessFile(lockFile.toString(),"rw"));
        lockTarget.set(lockFileAccess.get().getChannel().tryLock());

        if(lockTarget.get() == null) {
          return new LockFileResult(false, readLockFileContent(lockInfo));
        }
        
        writeLinesToLogFile(lockInfo, lines);
      }catch(Exception e) {e.printStackTrace();
        return new LockFileResult(false, readLockFileContent(lockInfo));
      }
    }
    else {
      try {
        lockFile.createNewFile();
        lockFileAccess.set(new RandomAccessFile(lockFile.toString(),"rw"));
        lockTarget.set(lockFileAccess.get().getChannel().tryLock());
        writeLinesToLogFile(lockInfo, lines);
      }catch(Exception e){
        if(e instanceof IOException) {
          LOG.log(Level.WARNING, e.getLocalizedMessage(), e);
        }
      }
    }

    return new LockFileResult(true, null);
  }
  
  private static void writeLinesToLogFile(final File lockInfo, final String[] lines) {
    if(lines != null && lines.length > 0) {
      try (RandomAccessFile fileLockAccess = new RandomAccessFile(lockInfo, "rw")) {
        for(final String line : lines) {
          fileLockAccess.writeBytes(line+"\n");
        }
      }catch(IOException ioe) {}
    }
  }

  private static void deleteLockFile(final RandomAccessFile fileLockAccess, final FileLock fileLock,final String file) {
    String dir = Settings.getUserDirectoryName();
    File lockFile = new File(dir, file);
    File lockInfo = new File(dir, file+"_info");

    if(lockFile.isFile()) {
      try {
        fileLock.release();
      }catch(Exception e) {
        // ignore
      }
  
      try {
        fileLockAccess.close();
      }catch(Exception e) {
        // ignore
      }
  
      if(!lockFile.delete()) {
        lockFile.deleteOnExit();
      }
    }
    
    if(lockInfo.isFile() && !lockInfo.delete()) {
    	lockInfo.deleteOnExit();
    }
  }


  private static void showTVBrowserIsAlreadyRunningMessageBox(final LockFileResult resultLockFile) {
    try {
      UIThreadRunner.invokeAndWait(() -> {
        int port = Integer.MIN_VALUE;
        
        if(resultLockFile.mLines != null && resultLockFile.mLines.length == 1) {
          try {
            port = Integer.parseInt(resultLockFile.mLines[0]);
          }catch(NumberFormatException nfe) {}
        }
        
        Object[] options = new Object[!resultLockFile.mResult && port != Integer.MIN_VALUE ? 3 : 2];
        
        int index = 0;
        
        if(!resultLockFile.mResult && port != Integer.MIN_VALUE) {
          options[index++] = LOCALIZER.msg("showTvBrowser", "Open running TV-Browser");
        }
        
        options[index++] = Localizer.getLocalization(Localizer.I18N_CLOSE);
        options[index] = LOCALIZER.msg("startAnyway", "start anyway");
        
        int result = UiUtilities.showOptionDialogOnMouseScreen(LOCALIZER.msg("alreadyRunning", "TV-Browser is already running"),
            LOCALIZER.msg("alreadyRunning", "TV-Browser is already running"), JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        
        if (result == 0 && !resultLockFile.mResult && port != Integer.MIN_VALUE) {
          try(DatagramSocket socket = new DatagramSocket()) {
            byte[] buf = "open_tvb".getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), port);
            socket.send(packet);
            System.exit(0);
          } catch (Exception e) {e.printStackTrace();
            System.exit(-1);
          }
          
        } else if(result == 0 || (!resultLockFile.mResult && result == 1)) {
          System.exit(-1);
        }
      });
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private static void initUi(Splash splash, boolean startMinimized) {
    mainFrame=MainFrame.getInstance();
    PluginProxyManager.getInstance().setParentFrame(mainFrame);
    TvDataServiceProxyManager.getInstance().setParamFrame(mainFrame);
    
    
    
    // Set the program icon
    
    mainFrame.setIconImages(ICONS_WINDOW);

    mTray = new SystemTray();

    if (mTray.initSystemTray()) {
        mTray.createMenus();
        
        if(mToggleSocket.get() != null) {
          mToggleSocket.get().setTray(mTray);
          mToggleSocket.get().start();
        }
    } else {
      if(mToggleSocket.get() != null) {
        mToggleSocket.get().initMainFrame();
      }
      LOG.info("platform independent mode is ON");
      addTrayWindowListener();
    }

    // Set the right size
    LOG.info("Setting frame size and location");
    
    final int windowWidth = Settings.Window.WIDTH.getInt();
    final int windowHeight = Settings.Window.HEIGHT.getInt();
    mainFrame.setSize(windowWidth, windowHeight);
    final int windowX = Settings.Window.X.getInt();
    final int windowY = Settings.Window.Y.getInt();
    final boolean maximized = Settings.Window.MAXIMIZED.getBoolean();
    
    if(!IS_STABLE) {
      LOG.info("Window values: " + windowWidth + "x" + windowHeight + " at " + windowX +"," +windowY+" isMaximized: " + maximized);
    }
    
    final Rectangle screen = mainFrame.getGraphicsConfiguration().getBounds();
    GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(mainFrame.getGraphicsConfiguration());
    screen.x = screen.x + insets.left;
    screen.y = screen.y + insets.top;
    screen.width = screen.width - insets.left - insets.right;
    screen.height = screen.height - insets.top - insets.bottom;
    
    if (maximized || (windowX == -1 && windowY == -1) || windowX + windowWidth < screen.getX() || windowX > (screen.getX() + screen.getWidth() - 30) || windowY + windowHeight < screen.getY() || windowY > (screen.getY() + screen.getHeight() - 30) || windowWidth < 200 || windowHeight < 200) {
      UiUtilities.centerAndShow(mainFrame, false);
    } else {
      mainFrame.setLocation(windowX, windowY);
    }
    
    SwingUtilities.invokeLater(() -> {
      Point p = mainFrame.getLocation();
      
      if((windowX < screen.getX()) || (windowY < screen.getY()) || windowX > (screen.getX() + screen.getWidth() - 30) || windowY > (screen.getY() + screen.getHeight() - 30)) {
        UiUtilities.centerAndShow(mainFrame, false);
      }
      else if(!maximized && (p.x != windowX || windowY != p.y)) {
        mainFrame.setLocation(windowX - Math.abs(p.x-windowX), windowY - Math.abs(p.y-windowY));
      }
    });

    mainFrame.setVisible(true);
    ErrorHandler.setFrame(mainFrame);

    splash.hideSplash();

    mainFrame.repaint();
    
    // maximize the frame if wanted
    if (maximized) {
      SwingUtilities.invokeLater(() -> {
    	mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
    	SwingUtilities.invokeLater(() -> {
          mainFrame.repaint();
        });
      });
    }

    // minimize the frame if wanted
    if (startMinimized) {
      mainFrame.setExtendedState(Frame.ICONIFIED);
    }

    if (mFullscreen || Settings.General.IS_USING_FULLSCREEN.getBoolean()) {
       SwingUtilities.invokeLater(() -> {
          mainFrame.switchFullscreenMode();
       });
    }

    if (Settings.Window.ASSISTANT_SHOW.getBoolean()) {
      LOG.info("Running setup assistant");
      mainFrame.runSetupAssistant();
    }
    
    if(Launch.isMacOs()) {
      try {
    	Class<? extends Object> fullScreenUtilities = Class.forName("com.apple.eawt.FullScreenUtilities");
    	fullScreenUtilities.getMethod("setWindowCanFullScreen", Window.class, Boolean.TYPE).invoke(null, mainFrame, true);
    	
    	if(Settings.Window.MAC_OS_FULL_SCREEN.getBoolean()) {
    	  Class<? extends Object> app = Class.forName("com.apple.eawt.Application");
    	  Object o = app.getMethod("getApplication").invoke(app);
    	  app.getMethod("requestToggleFullScreen", Window.class).invoke(o,mainFrame);
    	}
    	
		Class<? extends Object> fullScreenListenerClass = Class.forName("com.apple.eawt.FullScreenListener");
		Object fullScreenListener = Proxy.newProxyInstance(fullScreenListenerClass.getClassLoader(), new Class<?>[] {fullScreenListenerClass}, (proxy, method, methodArgs) -> {
		  if(method.getName().equals("windowEnteredFullScreen")) {
		    Settings.Window.MAC_OS_FULL_SCREEN.setBoolean(true);
		  }
		  else if(method.getName().equals("windowExitedFullScreen")) {
		    Settings.Window.MAC_OS_FULL_SCREEN.setBoolean(false);
	      }
		  
		  return null;
		});
		
		fullScreenUtilities.getMethod("addFullScreenListenerTo", Window.class, fullScreenListenerClass).invoke(null, mainFrame, fullScreenListener);
	  } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
    }
  }

  /**
   * initialize the automatic download timer
   */
  private static void initializeAutomaticDownload() {
    if (!Settings.Window.ASSISTANT_SHOW.getBoolean()) {
      SwingUtilities.invokeLater(() -> {
        boolean automaticDownloadStarted = handleAutomaticDownload(0);
        
        boolean dataAvailable = TvDataBase.getInstance().dataAvailable(new Date());
        if (!automaticDownloadStarted && (! dataAvailable) && (ChannelList.getNumberOfSubscribedChannels() > 0)) {
          mainFrame.askForDataUpdateNoDataAvailable();
        }
        mainFrame.scrollToNowFirst();
      });
    }
  }

  /**
   * Saves the main settings.
   *
   * @param log If it should be written into the log.
   */
  public static synchronized void flushSettings(boolean log) {
    // don't store settings if mainFrame is not available
    // may happen during debugging sessions
    if ((mainFrame == null) || (mainFrame.getWidth() == 0)) {
      return;
    }
    if(log) {
      LOG.info("Channel Settings (day light saving time corrections/icons)");
    }
    //ChannelList.storeAllSettings();

    InternalPluginProxyList.getInstance().storeData(log);
    mainFrame.storeSettings();

    if(log) {
      LOG.info("Storing window size and location");

      int state = mainFrame.getExtendedState();
      
      if(!mainFrame.isFullScreenMode() && MainFrame.getInstance().isVisible()) {
        boolean maximized = (state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
        
        Settings.Window.MAXIMIZED.setBoolean(maximized);
  
        if (! maximized && (!Launch.isMacOs() || !Settings.Window.MAC_OS_FULL_SCREEN.getBoolean())) {
          // Save the window size and location only when not maximized
          Settings.Window.WIDTH.setInt(mainFrame.getWidth());
          Settings.Window.HEIGHT.setInt(mainFrame.getHeight());
          Settings.Window.X.setInt(mainFrame.getX());
          Settings.Window.Y.setInt(mainFrame.getY());
        }
      }
    }

    if(log) {
      LOG.info("Storing settings");
    }
    try {
      Settings.storeSettings(log);
    } catch (TvBrowserException e) {
      ErrorHandler.handle(e);
    }
  }

  private static void addTrayWindowListener() {
    if(mMainWindowAdapter == null) {
      mMainWindowAdapter = new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          if (Settings.General.ONLY_MINIMIZE_WHEN_WINDOW_CLOSING.getBoolean()) {
            MainFrame.getInstance().setExtendedState(JFrame.ICONIFIED);
          } else {
            mainFrame.quit();
          }
        }
      };
    }
    mainFrame.addWindowListener(mMainWindowAdapter);
  }

  /**
   * Gets if the system tray is used.
   *
   * @return <code>true</code> if the system tray is used, <code>false</code> otherwise.
   */
  public static boolean isUsingSystemTray() {
    return mTray.isTrayUsed();
  }

  /**
   * Loads the tray icon.
   */
  public static void loadTray() {
    if(!mTray.isTrayUsed()) {
      mTray.initSystemTray();
    }
    if(mTray.isTrayUsed()) {
      mTray.createMenus();
      if(mMainWindowAdapter != null) {
        mainFrame.removeWindowListener(mMainWindowAdapter);
      }
    }
  }
  
  /**
   * Remove the tray icon.
   */
  public static void removeTray() {
    if(mTray.isTrayUsed()) {
      mTray.setVisible(false);
      addTrayWindowListener();

      if(!MainFrame.getInstance().isVisible()) {
        SwingUtilities.invokeLater(() -> {
          MainFrame.getInstance().showFromTray(MainFrame.ICONIFIED);
        });
      }
    }
  }

  /**
   * Shows a balloon tip on the TV-Browser tray icon.
   * <p>
   * @param caption The caption of the displayed message.
   * @param message The message to display in the balloon tip.
   * @param messageType The type of the displayed balllon tip.
   * @return If the balloon tip could be shown.
   */
  public static boolean showBalloonTip(String caption, String message, java.awt.TrayIcon.MessageType messageType) {
    if(mTray.isTrayUsed()) {
      return mTray.showBalloonTip(caption,message,messageType);
    }

    return false;
  }

  /**
   * Starts an automatic download if required
   * @return false, if no download got started
   */
  public static boolean handleAutomaticDownload(int autoDownloadTime) {
    final BooleanResult result = isAutomaticDownloadDateReached(autoDownloadTime);
    
    if ((ChannelList.getNumberOfSubscribedChannels() == 0)
      || result.isAllFalse())
    {
      // Nothing to do
      return false;
    }

    if((mAutoDownloadWaitingTimer != null
          && mAutoDownloadWaitingTimer.isRunning())) {
      return true;
    }

    if((autoDownloadTime != -1 && Settings.General.AUTO_DOWNLOAD_WAITING_ENABLED.getBoolean() && Settings.General.AUTO_DOWNLOAD_WAITING_TIME.getShort() > 0) || result.getResultForIndex(1)) {
      final long timerStart = Calendar.getInstance().getTimeInMillis();
      if(mAutoDownloadWaitingTimer == null) {
        mAutoDownloadWaitingTimer = new Timer(1000,
            new ActionListener() {
              private boolean mIsProcessing = false;
              @Override
              public void actionPerformed(ActionEvent e) {
                if(!mIsProcessing) {
                  mIsProcessing = true;
                  try {
                    int seconds = (int) ((Calendar.getInstance().getTimeInMillis() - timerStart) / 1000.0);
                    seconds = (!result.getResultForIndex(0) && result.getResultForIndex(1) ? 40 : Settings.General.AUTO_DOWNLOAD_WAITING_TIME.getShort()) - seconds;
                    
                    if (seconds <= 0) {
                      mAutoDownloadWaitingTimer.stop();
                      mainFrame.getStatusBarLabel().setText("");
                      performAutomaticDownload(result);
                    } else if(mainFrame.isUpdatingData()){
                      mAutoDownloadWaitingTimer.stop();
                      mainFrame.getStatusBarLabel().setText("");
                    } else {
                      mainFrame.getStatusBarLabel().setText(
                          LOCALIZER.msg("downloadwait",
                              "Automatic download starts in {0} seconds.", seconds));
                    }
                  }catch(Throwable t) {t.printStackTrace();}
                  mIsProcessing = false;
                }
              }
            }
        );
        mAutoDownloadWaitingTimer.setRepeats(true);
        mAutoDownloadWaitingTimer.start();
      }
      else {
        mAutoDownloadWaitingTimer.restart();
      }
    }
    else if(autoDownloadTime != -1) {
      return performAutomaticDownload(result);
    }

    return result.getResultForIndex(0) && !result.getResultForIndex(1);
  }
  
  public static boolean isWaitingForUpdateStart() {
    return mAutoDownloadWaitingTimer != null && mAutoDownloadWaitingTimer.isRunning();
  }
  
  private static BooleanResult isAutomaticDownloadDateReached(int autoDownloadTime) {
    String autoDLType = Settings.General.AUTO_DOWNLOAD_TYPE.getString();
    final Date lastDownloadDate = Settings.Data.DOWNLOAD_DATE_LAST.getDate();
    Date today = Date.getCurrentDate();
    
    Date nextDownloadDate;

    if (autoDLType.equals("daily")) {
      nextDownloadDate=lastDownloadDate.addDays(1);
    }
    else if (autoDLType.equals("every3days")) {
      nextDownloadDate=lastDownloadDate.addDays(3);
    }
    else if (autoDLType.equals("weekly")) {
      nextDownloadDate=lastDownloadDate.addDays(7);
    }
    else { // "daily"
      nextDownloadDate=lastDownloadDate;
    }
    
    boolean download = autoDownloadTime != -1 && autoDownloadTime <= IOUtilities.getMinutesAfterMidnight() && !autoDLType.equals("never") && nextDownloadDate.getNumberOfDaysSince(today) <= 0;
    boolean primeTime = Settings.General.AUTO_UPDATE_PRIME_TIME.getBoolean();
    
    if(primeTime) {
      int compare = Date.getCurrentDate().compareTo(Settings.Data.DOWNLOAD_DATE_LAST.getDate());
      
      primeTime = (Math.random() > 0.8 || (IOUtilities.getMinutesAfterMidnight() >= 17*60+50 && IOUtilities.getMinutesAfterMidnight() <= 20*60+15)) && IOUtilities.getMinutesAfterMidnight() >= 60*17+30 && IOUtilities.getMinutesAfterMidnight() <= 60*20+15 && (compare > 0 || (compare == 0 && Settings.Data.DOWNLOAD_TIME_LAST.getInt() < 17*60+30));
    }
    
    BooleanResult result = new BooleanResult(download, primeTime);
    result.setResultNames("AutoDownload","PrimeTime");
    
    return result;
  }

  private static boolean performAutomaticDownload(final BooleanResult resultInfo) {
    boolean result = false;
    
    if(!mainFrame.isUpdatingData()) {
      if (resultInfo.getResultForIndex(0)) {
        if (Settings.General.ASK_FOR_AUTO_DOWNLOAD.getBoolean()) {
          mainFrame.updateTvData();
        }
        else {
          String[] dataServiceIDs = Settings.Data.DATA_SERVICES_FOR_UPDATE.getStringArray();
          TvDataServiceProxy[] proxies;
          if (dataServiceIDs == null) {
            proxies = UpdateDlg.getActiveDataServices();
          }
          else {
            proxies = TvDataServiceProxyManager.getInstance().getTvDataServices(dataServiceIDs);
          }
          if(mainFrame.licenseForTvDataServicesWasAccepted(proxies)) {
            mainFrame.runUpdateThread(Settings.General.AUTO_DOWNLOAD_PERIOD.getInt(), proxies, true);
          }
        }
        
        result = true;
      }
      else if(resultInfo.getResultForIndex(1)) {
        HashSet<TvDataServiceProxy> dataServices = new HashSet<TvDataServiceProxy>();
  
        Channel[] channels = Settings.Channels.SUBSCRIBED.getChannelArray();
  
        for(Channel channel : channels) {
          if(!(channel instanceof DummyChannel) && channel.getDataServiceProxy() != null && !dataServices.contains(channel.getDataServiceProxy())) {
              dataServices.add(channel.getDataServiceProxy());
          }
        }
        
        if(!dataServices.isEmpty()) {
          TvDataServiceProxy[] proxies = dataServices.toArray(new TvDataServiceProxy[0]);
        
          if(mainFrame.licenseForTvDataServicesWasAccepted(proxies)) {
            mainFrame.runUpdateThread(Settings.General.AUTO_DOWNLOAD_PERIOD.getInt(), proxies, true);
          }
        }
        
        result = true;
      }
    }
    
    return result;
  }

  private static void updateLookAndFeel() {
    try {
      if (OperatingSystem.isWindows()) {
        UIManager.installLookAndFeel("Extended Windows",  "com.jgoodies.looks.windows.WindowsLookAndFeel");
      }
      UIManager.installLookAndFeel("Plastic",           "com.jgoodies.looks.plastic.PlasticLookAndFeel");
      UIManager.installLookAndFeel("Plastic 3D",        "com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
      UIManager.installLookAndFeel("Plastic XP",        "com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
      
      //String classPath = System.getProperty("java.class.path","");
      /*if (!isStable() || StringUtils.containsIgnoreCase(classPath, "eclipse") || StringUtils.containsIgnoreCase(classPath, "workspace")) {
        Map<String, SkinInfo> substanceSkins = SubstanceLookAndFeel.getAllSkins();
        if (substanceSkins != null) {
          for (SkinInfo skin : substanceSkins.values()) {
            String className = skin.getClassName();
            UIManager.installLookAndFeel("Substance " + skin.getDisplayName(),
                StringUtils.replace(StringUtils.replace(className, "Skin", "LookAndFeel"), "skin.", "skin.Substance"));
          }
        }
      }*/
    } catch (Exception e1) {
      // ignore any exception for optional skins
      e1.printStackTrace();
    }
    
    /*
     * Workaround for GTK look and feel problems with assistive_technologies=org.GNOME.Accessibility.AtkWrapper
     * for OpenJDK under Linux. The GTK+ look and feel is removed from the selection list, if it is available
     * and OpenJDK is used with the problematic property.
     */
    if (OperatingSystem.isLinux() && System.getProperty("java.runtime.name","").startsWith("OpenJDK")) {
      String[] parts = System.getProperty("java.version","").split("\\.");
      
      if(parts.length > 1) {
        File test = new File("/etc/java-"+parts[1]+"-openjdk/accessibility.properties");
        
        if(test.isFile()) {
          BufferedReader in = null;
          
          try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(test), "UTF-8"));
            
            String line = null;
            
            while((line = in.readLine()) != null) {
              if(!line.trim().startsWith("#") && line.contains("assistive_technologies") && line.contains("org.GNOME.Accessibility.AtkWrapper")) {
                Settings.LookAndFeel.SELECTED.setDefault(UiUtilities.getDefaultLookAndFeelClassName(true));
                
                LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
                
                ArrayList<LookAndFeelInfo> cleanedLooksList = new ArrayList<LookAndFeelInfo>(lnfs.length-1);
                
                if (lnfs != null) {
                  for (LookAndFeelInfo lookAndFeel : lnfs) {
                    if (!lookAndFeel.getClassName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
                      cleanedLooksList.add(lookAndFeel);
                    }
                  }
                }
                
                UIManager.setInstalledLookAndFeels(cleanedLooksList.toArray(new LookAndFeelInfo[cleanedLooksList.size()]));
              }
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
          finally {
            if(in != null) {
              try {
                in.close();
              } catch (IOException e) {}
            }
          }
        }
      }
    }
    
    
    if (Settings.LookAndFeel.SELECTED.getString().equals(
        "com.l2fprod.gui.plaf.skin.SkinLookAndFeel")) {
    	Settings.LookAndFeel.SELECTED.setString(Settings.LookAndFeel.SELECTED.getDefault());
    	/*
      String themepack = Settings.propSkinLFThemepack.getString();
      try {
        File themepackFile = new File(themepack);
        if (!themepackFile.exists()) {
          themepackFile = new File(Settings.getUserDirectoryName(), themepack);
        }

        if (!themepackFile.exists() && OperatingSystem.isMacOs()) {
          themepackFile = new File("/Library/Application Support/TV-Browser/", themepack);
        }

        //SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(themepackFile.getAbsolutePath()));
      } catch (Exception exc) {
        ErrorHandler.handle(
          "Could not load themepack.\nSkinLF is disabled now",
          exc);
        Settings.LookAndFeel.SELECTED.setString(Settings.LookAndFeel.SELECTED.getDefault());
      }*/
    } else if (Settings.LookAndFeel.SELECTED.getString().startsWith("com.jgoodies") && !Settings.LookAndFeel.SELECTED.getString().startsWith("com.jgoodies.looks.windows.WindowsLookAndFeel")) {
      com.jgoodies.looks.Options.setPopupDropShadowEnabled(Settings.LookAndFeel.JGOODIES_SHADOW.getBoolean());
      UIManager.put("jgoodies.popupDropShadowEnabled", Boolean
          .valueOf(Settings.LookAndFeel.JGOODIES_SHADOW.getBoolean()));
      try {
        LookUtils.setLookAndTheme((LookAndFeel) Class.forName(Settings.LookAndFeel.SELECTED.getString()).getConstructor().newInstance(), Class.forName(Settings.LookAndFeel.JGOODIES_THEME.getString()).getConstructor().newInstance());
      } catch (Throwable e) {
        ErrorHandler.handle("Could not load themepack.\nJGoodies is disabled now", e);
        Settings.LookAndFeel.SELECTED.setString(Settings.LookAndFeel.SELECTED.getDefault());
      }
    }

    if (CUR_LOOK_AND_FEEL == null || !CUR_LOOK_AND_FEEL.equals(Settings.LookAndFeel.SELECTED.getString())) {
      try {
        CUR_LOOK_AND_FEEL = Settings.LookAndFeel.SELECTED.getString();
        // check if LnF is still available
        boolean foundCurrent = lookAndFeelExists(CUR_LOOK_AND_FEEL);
        // reset look and feel?
        if (!foundCurrent) {
          if (JOptionPane
              .showConfirmDialog(
                  null,
                  LOCALIZER
                      .msg(
                          "lnfMissing",
                          "The look and feel '{0}' is no longer available,\nso the default look and feel will be used.\n\nDo you want to set the look and feel option to the default look and feel?",
                          CUR_LOOK_AND_FEEL),
                  LOCALIZER.msg("lnfMissing.title", "Look and feel missing"),
                  JOptionPane.WARNING_MESSAGE | JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Settings.LookAndFeel.SELECTED.resetToDefault();
            CUR_LOOK_AND_FEEL = Settings.LookAndFeel.SELECTED.getString();
            foundCurrent = true;
          }
        }
        if (foundCurrent) {
          UIThreadRunner.invokeAndWait(() -> {
            try {
              UIManager.setLookAndFeel(CUR_LOOK_AND_FEEL);
            } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            LOG.info("setting look and feel to " + CUR_LOOK_AND_FEEL);
          });
        }
      } catch (Exception exc) {
        String msg =
          LOCALIZER.msg("error.1", "Unable to set look and feel.", exc);
        ErrorHandler.handle(msg, exc);
      }
    }

    // set colors for action pane at UIManager
    UIManager.put("TaskPane.foreground",UIManager.get("Button.foreground"));

    if(UIManager.getColor("List.selectionBackground") == null) {
      UIManager.put("List.selectionBackground",UIManager.getColor("Tree.selectionBackground"));
    }
    if(UIManager.getColor("List.selectionForeground") == null) {
      UIManager.put("List.selectionForeground",UIManager.getColor("Tree.selectionForeground"));
    }
    if(UIManager.getColor("MenuItem.selectionForeground") == null) {
      UIManager.put("MenuItem.selectionForeground",UIManager.getColor("Tree.selectionForeground"));
    }
    if(UIManager.getColor("ComboBox.disabledForeground") == null) {
      UIManager.put("ComboBox.disabledForeground", Color.gray);
    }

    if (mainFrame != null) {
      SwingUtilities.updateComponentTreeUI(mainFrame);
      mainFrame.validate();
    }
    lookAndFeelInitialized = true;
  }


  /**
   * Creates a very simple Formatter for log formatting
   *
   * @return a very simple Formatter for log formatting.
   */
  private static Formatter createFormatter() {
    return new Formatter() {
      public synchronized String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        DateFormat mTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);

        String message = formatMessage(record);
        sb.append(mTimeFormat.format(new java.util.Date(System.currentTimeMillis())));
        sb.append(' ');
        sb.append(record.getLevel().getLocalizedName());
        sb.append(": ");
        sb.append(message);
        sb.append('\n');
        if (record.getThrown() != null) {
          try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
            pw.close();
            sb.append(sw.toString());
          } catch (Exception ex) {
            // ignore
          }
        }
        return sb.toString();
      }
    };
  }

  /**
   * Called when TV-Browser shuts down.
   * <p>
   * Stops the save thread and saves the settings.
   * @param log <code>true</code> if the log should be written, <code>false</code> if not.
   */
  public static void shutdown(boolean log) {
    mSaveThreadShouldStop = true;
    flushSettings(log);
  }


  /**
   * Updates the proxy settings.
   */
  public static void updateProxySettings() {
    String httpHost = "", httpPort = "", httpUser = "", httpPassword = "";

    if (Settings.Proxy.USE.getBoolean()) {
      httpHost = Settings.Proxy.HOST.getString();
      httpPort = Settings.Proxy.PORT.getString();

      if (Settings.Proxy.AUTHENTIFY_AT_PROXY.getBoolean()) {
        httpUser     = Settings.Proxy.USER.getString();
        httpPassword = Settings.Proxy.PASSWORD.getString();
        if (httpPassword == null) {
          httpPassword="";
        }

        final String user=httpUser;
        final String pw=httpPassword;
        Authenticator.setDefault(
          new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(user, pw.toCharArray());
            }
          }
        );
      }
    }

    System.setProperty("http.proxyHost",     httpHost);
    System.setProperty("http.proxyPort",     httpPort);
    System.setProperty("http.proxyUser",     httpUser);
    System.setProperty("http.proxyPassword", httpPassword);
    System.setProperty("https.proxyHost",     httpHost);
    System.setProperty("https.proxyPort",     httpPort);
    System.setProperty("https.proxyUser",     httpUser);
    System.setProperty("https.proxyPassword", httpPassword);
  }

  /**
   * Gets if TV-Browser runs as portable version.
   *
   * @return If TV-Browser runs as portable version.
   * @since 2.2.2/2.5.1
   */
  public static boolean isTransportable() {
    return mIsTransportable;
  }

  /**
   * get whether this is a development version or a stable build
   * @return if stable
   * @since 2.7
   */
  public static boolean isStable() {
    return IS_STABLE;
  }

  /**
   * get the version string of this version (for use in directories)
   * @return version string suffix
   * @since 3.0
   */
  public static String getCurrentVersionString() {
    return ALL_VERSIONS[0];
  }

  /**
   * get the version names of all released versions
   * (for use in directory names)
   * @return version string suffixes
   * @since 3.0
   */
  public static String[] getAllVersionStrings() {
    return ALL_VERSIONS.clone();
  }

  private static boolean lookAndFeelExists(String lnf) {
    boolean foundLNF = false;
    for (LookAndFeelInfo lnfInfo : UIManager.getInstalledLookAndFeels()) {
      if (lnfInfo.getClassName().equals(lnf)) {
        foundLNF = true;
        break;
      }
    }
    return foundLNF;
  }

  public static void stopAutomaticDownload() {
    if (mAutoDownloadWaitingTimer != null) {
      mAutoDownloadWaitingTimer.stop();
      mainFrame.getStatusBarLabel().setText("");
      mAutoDownloadWaitingTimer = null;
    }
  }
  
  public static void loadDataServicesAtStartup() {
    try {
      SoftwareUpdateItem[] updateItems = PluginAutoUpdater.getDataServicesForFirstStartup();
      
      if(updateItems.length > 0) {
        final boolean oldValue = Settings.Plugins.BETA_WARNING.getBoolean();
        
        Settings.Plugins.BETA_WARNING.setBoolean(false);
        SoftwareUpdateDlg updateDlg = new SoftwareUpdateDlg(UiUtilities.getLastModalChildOf(mainFrame),SoftwareUpdater.ONLY_DATA_SERVICE_TYPE,updateItems,false,null);
        updateDlg.setLocationRelativeTo(null);
        updateDlg.setVisible(true);
        
        Settings.Plugins.BETA_WARNING.setBoolean(oldValue);
        PluginLoader.getInstance().installPendingPlugins();
        PluginLoader.getInstance().loadAllPlugins();
        
        PluginProxy epgPaid = PluginProxyManager.getInstance().getPluginForId("java.epgpaiddata.EPGpaidData");
        
        if(epgPaid != null) {
          try {
            PluginProxyManager.getInstance().activatePlugin(epgPaid);
          } catch (TvBrowserException e) { }
        }
        
        /* Download default channel lists for user country */
        try {
          PluginAutoUpdater.downloadMirrorList();
          
          final Mirror defaultChannelList = PluginAutoUpdater.getPluginUpdatesMirror();
          final File supportedChannelLists = new File(Settings.getUserSettingsDirName(),"channellist_supported.gz");
          
          if(IOUtilities.download(new URL(defaultChannelList.getUrl()+"/"+supportedChannelLists.getName()), supportedChannelLists, 5000)) {
            final String country = Settings.getCountry();
            
            BufferedReader in = null;
            
            try {
              in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(supportedChannelLists))));
              
              String line = null;
              
              while((line = in.readLine()) != null) {
                if(country.equals(line)) {
                  final File defaultCountryChannels = new File(Settings.getUserSettingsDirName(),"channellist_"+country+".gz");
                  IOUtilities.download(new URL(defaultChannelList.getUrl()+"/"+defaultCountryChannels.getName()), defaultCountryChannels, 5000);
                  break;
                }
              }
            }catch(Throwable t) {
              t.printStackTrace();
            }finally {
              IOUtilities.close(in);
            }
          }
        }catch(IOException ioe1) {
          ioe1.printStackTrace();
        }
        
        TvDataServiceProxyManager.getInstance().init();
        ChannelList.createForTvBrowserStart();
        ChannelList.initSubscribedChannels();
        
        String[] deactivatedPlugins = PluginProxyManager.getInstance().getDeactivatedPluginIds();
        
        if(deactivatedPlugins.length > 0) {
          String[] propDeactivatedPlugins = Settings.Plugins.DEACTIVATED.getStringArray();
          
          for(String deactivatedPlugin : deactivatedPlugins) {
            boolean activate = true;
            
            for(String test : propDeactivatedPlugins) {
              if(test.equals(deactivatedPlugin)) {
                activate = false;
                break;
              }
            }
            
            if(activate) {
              PluginProxy deactivated = PluginProxyManager.getInstance().getPluginForId(deactivatedPlugin);
              
              try {
                PluginProxyManager.getInstance().activatePlugin(deactivated);
              } catch (TvBrowserException e) {}
            }
          }
          
          mainFrame.updatePluginsMenu();
        }
      }
    } catch (IOException e1) {
  }
}

  private static void updatePluginsOnVersionChange() {
    final boolean oldBetaWarning = Settings.Plugins.BETA_WARNING.getBoolean();
    try {
      UIThreadRunner.invokeAndWait(() -> {
        Version obligartoryUpdate = new Version(4,24,51,false);
        
        TvBrowserVersionChangeDlg versionChange = new TvBrowserVersionChangeDlg(Settings.General.TV_BROWSER_VERSION_USED_LAST.getVersion(),obligartoryUpdate);
        versionChange.setIconImages(ICONS_WINDOW);
        versionChange.pack();
        versionChange.setLocationRelativeTo(null);
        versionChange.setVisible(true);
        versionChange.toFront();
        versionChange.requestFocus();

        Settings.Plugins.BETA_WARNING.setBoolean(oldBetaWarning);

        if(versionChange.getIsToCloseTvBrowser()) {
          System.exit(0);
        }
      });
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static boolean isSafeMode() {
    return mSafeMode;
  }
}

  class FileLoggingHandler extends Handler {

    private Formatter mFormatter;
    private PrintWriter mWriter;

    /**
     * Creates an instance of FileLoggingHandler.
     *
     * @param fName The name of the log file.
     * @param formatter The formatter for the log file.
     * @throws IOException Is thrown if something goes wrong.
     */
    public FileLoggingHandler(String fName, Formatter formatter) throws IOException {
      mFormatter = formatter;
      File f = new File(fName);
      mWriter = new PrintWriter(new FileOutputStream(f));
    }


    public void close() throws SecurityException {
      mWriter.close();
    }

    public void flush() {
      mWriter.flush();
    }

    public void publish(LogRecord record) {
      mWriter.println(mFormatter.format(record));
      flush();
    }
  }
