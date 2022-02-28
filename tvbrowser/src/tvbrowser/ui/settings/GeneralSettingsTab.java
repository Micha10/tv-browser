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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings;

import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import tvbrowser.TVBrowser;
import tvbrowser.core.JREUpdater;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.protocolhandler.ProtocolHandler;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.PeriodItem;
import util.browserlauncher.Launch;
import util.i18n.Localizer;
import util.io.windows.registry.RegistryKey;
import util.ui.UiUtilities;
import util.ui.WideComboBox;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class GeneralSettingsTab implements devplugin.SettingsTab {
  public static final int VALUE_AUTO_CHANNEL_UPDATE_DISABLED = -1;
  private static final DayPeriod VALUE_AUTO_CHANNEL_UPDATE_PERIOD_DEFAULT = new DayPeriod(14);
  
  /** The localizer for this class. */
  public static final util.i18n.Localizer LOCALIZER = util.i18n.Localizer
      .getLocalizerFor(GeneralSettingsTab.class);

  private JPanel mSettingsPn;

  private JCheckBox mShowStartScreenChB, mMinimizeAfterStartUpChB, mStartFullscreen,
      mAutostart, mServerForRestore, mProtocolHandler;
  
  private File mLinkFileFile;
  private LinkFile mLinkFile;

  /* Refresh settings */
  private static final String[] AUTO_DOWNLOAD_MSG_ARR = new String[] {
    LOCALIZER.msg("autoDownload.daily", "Once a day"),
    LOCALIZER.msg("autoDownload.every3days", "Every three days"), LOCALIZER.msg("autoDownload.weekly", "Weekly")
  };

  private JCheckBox mAutoDownload;
  private JCheckBox mAutoDownloadPrimeTime;
  private JCheckBox mAutoChannelDownload;
  private JCheckBox mAutoJREUpdate;
  private JCheckBox mInformTestVersions;

  private JRadioButton mStartDownload;
  private JRadioButton mRecurrentDownload;

  private JComboBox<String> mAutoDownloadCombo;
  private WideComboBox<DayPeriod> mAutoChannelDownloadPeriod;
  
  private JComboBox<PeriodItem> mAutoDownloadPeriodCB;

  private JRadioButton mAskBeforeDownloadRadio;

  private JRadioButton mAskTimeRadio;

  private JLabel mHowOften;

  private JCheckBox mDateCheck;

  private JCheckBox mAutoDownloadWaitingTime;
  private JSpinner mAutoDownloadWaitingTimeSp;

  /* Close settings */
  private JCheckBox mOnlyMinimizeWhenWindowClosingChB;
  private JCheckBox mAskForExitConfirmation;
  private JCheckBox mShowFinishDialog;
  private JLabel mSecondsLabel;

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout(
        "5dlu, default, 3dlu, default, fill:3dlu:grow, 3dlu",
        "default, 5dlu, default, 1dlu, default, 1dlu, default, 1dlu, default, 1dlu, default, 10dlu, default, 10dlu, default, 5dlu, default, default");
    mSettingsPn = new JPanel(layout);
    mSettingsPn.setBorder(Borders.DIALOG);
    
    int y = 1;

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(
        LOCALIZER.msg("title", "Startup")), CC.xyw(1, y++, 5));

    mMinimizeAfterStartUpChB = new JCheckBox(LOCALIZER.msg(
        "minimizeAfterStartup", "Minimize main window after start up"),
        Settings.General.MINIMIZE_AFTER_STARTUP.getBoolean());
    mSettingsPn.add(mMinimizeAfterStartUpChB, CC.xy(2, ++y));

    y++;

    mStartFullscreen = new JCheckBox(LOCALIZER.msg(
        "startFullscreen","Start in fullscreen mode"),
        Settings.General.IS_USING_FULLSCREEN.getBoolean());
    mSettingsPn.add(mStartFullscreen, CC.xy(2,++y));

    mMinimizeAfterStartUpChB.addItemListener(e -> {
      if(e.getStateChange() == ItemEvent.SELECTED) {
        mStartFullscreen.setSelected(false);
      }
    });

    mStartFullscreen.addItemListener(e -> {
      if(e.getStateChange() == ItemEvent.SELECTED) {
        mMinimizeAfterStartUpChB.setSelected(false);
      }
    });

    y++;

    mShowStartScreenChB = new JCheckBox(LOCALIZER.msg("showStartScreen",
        "Show TV-Browser start screen during start up"), Settings.General.START_SCREEN_SHOW
        .getBoolean());
    mSettingsPn.add(mShowStartScreenChB, CC.xy(2, ++y));
    
    y++;
    
    mServerForRestore = new JCheckBox(LOCALIZER.msg("serverForRestore",
        "Provide server port for restore running TV-Browser/handling protocol messages"), Settings.General.SERVER_RESTORE_ENABLED.getBoolean());
    mSettingsPn.add(mServerForRestore, CC.xy(2, ++y));
    
    if(!TVBrowser.isTransportable() || Launch.getOs() != Launch.OS_MAC) {
      mProtocolHandler = new JCheckBox(LOCALIZER.msg("protocolHandler", "Allow handling of tvb:// protocol messages"), Settings.General.CAN_RECEIVE_PROTOCOL_MESSAGE.getBoolean() && mServerForRestore.isSelected());
      mProtocolHandler.setEnabled(mServerForRestore.isSelected());
      mSettingsPn.add(mProtocolHandler, CC.xy(2, y+=2));
    }
    
    mServerForRestore.addItemListener(e -> {
    	mProtocolHandler.setEnabled(ItemEvent.SELECTED == e.getStateChange());
    });
    
    if (Launch.getOs() == Launch.OS_WINDOWS && !TVBrowser.isTransportable()) {
      layout.insertRow(++y, RowSpec.decode("1dlu"));
      layout.insertRow(++y, RowSpec.decode("pref"));

      try {
        RegistryKey shellFolders = new RegistryKey(RegistryKey.HKEY_CURRENT_USER, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders");
        String path = shellFolders.getValue("Startup").getData().toString();
        
        if(path == null || path.length() < 1 || !(new File(path)).isDirectory()) {
          throw new Exception();
        }

        mLinkFileFile = new File(path,"TV-Browser.url");

        try {
          mLinkFile = new LinkFile(mLinkFileFile);

          if(mLinkFileFile.isFile()) {
            try {
              if (!mLinkFile.hasTarget((new File("tvbrowser.exe")).getAbsoluteFile())) {
                createLink();
              }
            }catch(Exception linkException) {
              mLinkFileFile.delete();
            }
          }
        }catch(FileNotFoundException fe) {}

        mAutostart = new JCheckBox(LOCALIZER.msg("autostart","Start TV-Browser with Windows"),
            mLinkFileFile.isFile());

        mSettingsPn.add(mAutostart, CC.xy(2, y));
      } catch (Throwable e) {e.printStackTrace();}
    }
    else if(Launch.getOs() == Launch.OS_LINUX && !TVBrowser.isTransportable()) {
      layout.insertRow(++y, RowSpec.decode("1dlu"));
      layout.insertRow(++y, RowSpec.decode("pref"));
      
      mLinkFileFile = new File(System.getProperty("user.home")+"/.config/autostart/tvbrowser.desktop");
      
      if(mLinkFileFile.isFile()) {
        File starter = new File("");
        starter = new File(starter.getAbsolutePath(),"tvbrowser.sh");
        
        try(BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(mLinkFileFile), "UTF-8"))) {
          String line = null;
          
          while((line = in.readLine()) != null) {
            if(line.startsWith("Exec")) {
              line = line.substring(line.indexOf("=")+1);
              
              if((line.startsWith("/") && !line.startsWith(starter.getAbsolutePath())) || (starter.equals("/usr/share/tvbrowser/tvbrowser.sh") && line.startsWith("tvbrowser"))) {
                try {
                  createLink();
                } catch (Exception e1) {
                  // TODO Auto-generated catch block
                  e1.printStackTrace();
                }
              }
              
              break;
            }
          }
        }catch(IOException ioe) {}
      }
      
      mAutostart = new JCheckBox(LOCALIZER.msg("autostartLinux","Start TV-Browser after login"), mLinkFileFile.isFile());

      mSettingsPn.add(mAutostart, CC.xy(2, y));
    }

    y++;

    mSettingsPn.add(createRefreshPanel(), CC.xyw(1,++y,5));
    
    mAutoJREUpdate = new JCheckBox(LOCALIZER.msg("autoJREUpdate","Search and download updates for TV-Browser JRE regularly"),Settings.General.JRE_UPDATE_ENABLED.getBoolean());
    
    if(JREUpdater.hasTvBrowserJRE()) {
      layout.insertRow(++y, RowSpec.decode("5dlu"));
      layout.insertRow(++y, RowSpec.decode("pref"));
    
      mSettingsPn.add(mAutoJREUpdate, CC.xyw(2,y,4));
    }
    
    mInformTestVersions = new JCheckBox(LOCALIZER.msg("informTestVersion", "Show info when test versions of TV-Browser are available"),!Settings.General.INFORM_TEST_VERSIONS.isHidden());

    layout.insertRow(++y, RowSpec.decode("5dlu"));
    layout.insertRow(++y, RowSpec.decode("default"));
    
    mSettingsPn.add(mInformTestVersions, CC.xyw(2,y,4));
    
    y++;

    String msg = LOCALIZER.msg("onlyMinimizeWhenWindowClosing",
    "When closing the main window only minimize TV-Browser, don't quit.");

    mOnlyMinimizeWhenWindowClosingChB = new JCheckBox(msg, Settings.General.ONLY_MINIMIZE_WHEN_WINDOW_CLOSING.getBoolean());
    mAskForExitConfirmation = new JCheckBox(LOCALIZER.msg("askForExitConfirmation","Ask for confirmation on TV-Browser exit"), !Settings.General.ASK_FOR_EXIT_CONFIRMATION.isHidden());

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(LOCALIZER.msg("closing","Closing")), CC.xyw(1,++y,5));

    y++;

    mSettingsPn.add(mOnlyMinimizeWhenWindowClosingChB, CC.xyw(2,++y,4));
    mSettingsPn.add(mAskForExitConfirmation, CC.xyw(2,++y,4));
    
    return mSettingsPn;
  }
  
  private void createLink() throws Exception {
    if(Launch.getOs() == Launch.OS_LINUX) {
      ProtocolHandler.createDesktopFile(mLinkFileFile, "TV-Browser", false);
    }
    else if(Launch.getOs() == Launch.OS_WINDOWS) {
      File tvb = new File("tvbrowser.exe");
  
      if(tvb.getAbsoluteFile().isFile()) {
        mLinkFile = new LinkFile(mLinkFileFile, tvb, new File(tvb.getAbsoluteFile().getParent() + "\\imgs\\desktop.ico"),0);
      }
    }
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Settings.General.MINIMIZE_AFTER_STARTUP.setBoolean(mMinimizeAfterStartUpChB
        .isSelected());
    Settings.General.START_SCREEN_SHOW.setBoolean(mShowStartScreenChB.isSelected());
    Settings.General.IS_USING_FULLSCREEN.setBoolean(mStartFullscreen.isSelected());
    Settings.General.SERVER_RESTORE_ENABLED.setBoolean(mServerForRestore.isSelected());
    Settings.General.CAN_RECEIVE_PROTOCOL_MESSAGE.setBoolean(mServerForRestore.isSelected() && mProtocolHandler.isSelected());
    TVBrowser.updateLockGlobalToggle();
    
    if(mAutoChannelDownload.isSelected()) {
      Settings.General.AUTO_CHANNEL_UPDATE_PERIOD.setInt(((DayPeriod)mAutoChannelDownloadPeriod.getSelectedItem()).mDays);
    }
    else {
      Settings.General.AUTO_CHANNEL_UPDATE_PERIOD.setInt(VALUE_AUTO_CHANNEL_UPDATE_DISABLED);
    }

    Settings.General.AUTO_UPDATE_PRIME_TIME.setBoolean(mAutoDownloadPrimeTime.isSelected());
    
    if(mAutostart != null) {
        if (mAutostart.isSelected()) {
          if(!mLinkFileFile.isFile()) {
            try {
              createLink();
            } catch (Exception createLink) {}

            if (!mLinkFileFile.isFile()) {
              mAutostart.setSelected(false);
              JOptionPane.showMessageDialog(
                  UiUtilities.getLastModalChildOf(MainFrame.getInstance()),
                  LOCALIZER.msg("creationError","Couldn't create autostart shortcut.\nMaybe your have not the right to write in the autostart directory."),
                  Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);
            }
          }
        } else if (mLinkFileFile.isFile() && !mLinkFileFile.delete()) {
            mAutostart.setSelected(true);
            JOptionPane.showMessageDialog(
                UiUtilities.getLastModalChildOf(MainFrame.getInstance()),
                LOCALIZER.msg("deletionError","Couldn't delete autostart shortcut.\nMaybe your have not the right to write in the autostart directory."),
                LOCALIZER.msg("error","Error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /* Refresh settings*/
    int inx = mAutoDownloadCombo.getSelectedIndex();

    if (!mAutoDownload.isSelected()) {
      Settings.General.AUTO_DOWNLOAD_TYPE.setString("never");
    } else if (inx == 0) {
      Settings.General.AUTO_DOWNLOAD_TYPE.setString("daily");
    } else if (inx == 1) {
      Settings.General.AUTO_DOWNLOAD_TYPE.setString("every3days");
    } else if (inx == 2) {
      Settings.General.AUTO_DOWNLOAD_TYPE.setString("weekly");
    }
    
    Settings.General.DOWNLOAD_DONE.setHidden(!mShowFinishDialog.isSelected());
    
    Settings.General.AUTO_DATA_DOWNLOAD_ENABLED.setBoolean(mRecurrentDownload.isSelected() && mAutoDownload.isSelected());
    Settings.General.ASK_FOR_AUTO_DOWNLOAD.setBoolean(mAskBeforeDownloadRadio.isSelected());

    PeriodItem periodItem = (PeriodItem) mAutoDownloadPeriodCB.getSelectedItem();
    Settings.General.AUTO_DOWNLOAD_PERIOD.setInt(periodItem.getDays());
    Settings.General.AUTO_DOWNLOAD_WAITING_TIME.setShort(((Integer)mAutoDownloadWaitingTimeSp.getValue()).shortValue());
    Settings.General.AUTO_DOWNLOAD_WAITING_ENABLED.setBoolean(mAutoDownloadWaitingTime.isSelected());

    Settings.General.NTP_TIME_CHECK.setBoolean(mDateCheck.isSelected());

    /* Close settings */
    if (mOnlyMinimizeWhenWindowClosingChB != null) {
      boolean checked = mOnlyMinimizeWhenWindowClosingChB.isSelected();
      Settings.General.ONLY_MINIMIZE_WHEN_WINDOW_CLOSING.setBoolean(checked);
    }

    Settings.General.ASK_FOR_EXIT_CONFIRMATION.setHidden(!mAskForExitConfirmation.isSelected());
    
    Settings.General.JRE_UPDATE_ENABLED.setBoolean(mAutoJREUpdate.isSelected());
    Settings.General.INFORM_TEST_VERSIONS.setHidden(!mInformTestVersions.isSelected());
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("actions", "document-properties", 16);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return LOCALIZER.msg("general", "General settings");
  }

  private JPanel createRefreshPanel() {
    PanelBuilder refreshSettings = new PanelBuilder(new FormLayout("5dlu, 9dlu, default, 3dlu, default, fill:3dlu:grow, 3dlu",
    "default, 5dlu, default, 3dlu, default, default, 3dlu, default, default, 5dlu, default, 3dlu, default,10dlu,default,3dlu,default"));

    CellConstraints cc = new CellConstraints();

    int y = 1;
    
    refreshSettings.addSeparator(LOCALIZER.msg("titleRefresh", "Refresh"), cc.xyw(
        1, y, 6));

    mAutoDownload = new JCheckBox(LOCALIZER.msg("autoUpdate","Automatically update TV listings"));

    mStartDownload = new JRadioButton(LOCALIZER.msg("onStartUp", "Only on TV-Browser startup"));
    mRecurrentDownload = new JRadioButton(LOCALIZER.msg("recurrent","Recurrent"));

    ButtonGroup bg = new ButtonGroup();

    bg.add(mStartDownload);
    bg.add(mRecurrentDownload);

    y += 2;
    
    refreshSettings.add(mAutoDownload, cc.xyw(2, y, 5));

    y += 2;
    
    refreshSettings.add(mStartDownload, cc.xyw(3, y++, 4));
    refreshSettings.add(mRecurrentDownload, cc.xyw(3, y, 4));

    mAutoDownloadCombo = new JComboBox<>(AUTO_DOWNLOAD_MSG_ARR);
    String dlType = Settings.General.AUTO_DOWNLOAD_TYPE.getString();
    if (dlType.equals("daily")) {
      mAutoDownloadCombo.setSelectedIndex(0);
    } else if (dlType.equals("every3days")) {
      mAutoDownloadCombo.setSelectedIndex(1);
    } else if (dlType.equals("weekly")) {
      mAutoDownloadCombo.setSelectedIndex(2);
    }

    JPanel panel = new JPanel(new FormLayout("10dlu, pref, 3dlu, pref", "pref, 3dlu, pref, 3dlu, pref, 5dlu, pref"));

    mStartDownload.setSelected(!dlType.equals("never") && !Settings.General.AUTO_DATA_DOWNLOAD_ENABLED.getBoolean());
    mRecurrentDownload.setSelected(Settings.General.AUTO_DATA_DOWNLOAD_ENABLED.getBoolean());

    mAutoDownload.setSelected(mStartDownload.isSelected() || mRecurrentDownload.isSelected());
    mStartDownload.setSelected(!mAutoDownload.isSelected() || mStartDownload.isSelected());

    mStartDownload.setEnabled(mAutoDownload.isSelected());
    mRecurrentDownload.setEnabled(mAutoDownload.isSelected());

    mHowOften = new JLabel(LOCALIZER.msg("autoDownload.howOften", "How often?"));
    panel.add(mHowOften, cc.xy(2, 1));
    panel.add(mAutoDownloadCombo, cc.xy(4, 1));

    mAskBeforeDownloadRadio = new JRadioButton(LOCALIZER.msg("autoDownload.ask", "Ask before downloading"));
    mAutoDownloadPeriodCB = new JComboBox<>(PeriodItem.getPeriodItems());

    int autoDLPeriod = Settings.General.AUTO_DOWNLOAD_PERIOD.getInt();
    PeriodItem pi = new PeriodItem(autoDLPeriod);
    mAutoDownloadPeriodCB.setSelectedItem(pi);

    panel.add(mAskBeforeDownloadRadio, cc.xyw(2, 3, 3));

    mAskTimeRadio = new JRadioButton(LOCALIZER.msg("autoDownload.duration", "Automatically refresh for"));
    panel.add(mAskTimeRadio, cc.xy(2, 5));
    panel.add(mAutoDownloadPeriodCB, cc.xy(4, 5));

    ButtonGroup group = new ButtonGroup();
    group.add(mAskBeforeDownloadRadio);
    group.add(mAskTimeRadio);

    mAskBeforeDownloadRadio.setSelected(Settings.General.ASK_FOR_AUTO_DOWNLOAD.getBoolean());
    mAskTimeRadio.setSelected(!Settings.General.ASK_FOR_AUTO_DOWNLOAD.getBoolean());

    mAutoDownloadWaitingTime = new JCheckBox(LOCALIZER.msg("autoDownload.waiting","Delay auto update for"),Settings.General.AUTO_DOWNLOAD_WAITING_ENABLED.getBoolean());
    mAutoDownloadWaitingTimeSp = new JSpinner(new SpinnerNumberModel(
        Settings.General.AUTO_DOWNLOAD_WAITING_TIME.getShort(), 1, 300, 1));
    mSecondsLabel = new JLabel(LOCALIZER.msg("autoDownload.seconds","seconds"));

    mAutoDownload.addItemListener(e -> {
      setAutoDownloadEnabled(e.getStateChange() == ItemEvent.SELECTED);
    });

    mAskBeforeDownloadRadio.addItemListener(e -> {
      setAutoDownloadEnabled(mAutoDownload.isSelected());
    });

    mAskTimeRadio.addActionListener(e -> {
      setAutoDownloadEnabled(mAskTimeRadio.isSelected());
    });

    mAutoDownloadWaitingTime.addItemListener(e -> {
      mAutoDownloadWaitingTimeSp.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
    });

    JPanel waitingPanel = new JPanel(new FormLayout("pref,2dlu,pref,2dlu,pref","pref"));

    waitingPanel.add(mAutoDownloadWaitingTime, cc.xy(1, 1));
    waitingPanel.add(mAutoDownloadWaitingTimeSp, cc.xy(3, 1));
    waitingPanel.add(mSecondsLabel, cc.xy(5,1));

    panel.add(waitingPanel, cc.xyw(1,7,4));

    y += 2;
    
    refreshSettings.add(panel, cc.xyw(3, y++, 4));
    
    mAutoDownloadPrimeTime = new JCheckBox(LOCALIZER.msg("autoUpdatePrimeTime","Daily auto update prime time data in the evening"));
    mAutoDownloadPrimeTime.setSelected(Settings.General.AUTO_UPDATE_PRIME_TIME.getBoolean());
    
    refreshSettings.add(mAutoDownloadPrimeTime, cc.xyw(2, y, 5));

    mDateCheck = new JCheckBox(LOCALIZER.msg("checkDate", "Check date via NTP if data download fails"));
    mDateCheck.setSelected(Settings.General.NTP_TIME_CHECK.getBoolean());

    y += 2;
    
    refreshSettings.add(mDateCheck, cc.xyw(2, y, 5));

    mShowFinishDialog = new JCheckBox(LOCALIZER.msg("showFinishDialog", "Show dialog when update is done"));
    mShowFinishDialog.setSelected(!Settings.General.DOWNLOAD_DONE.isHidden());

    y += 2;
    
    refreshSettings.add(mShowFinishDialog, cc.xyw(2, y, 5));

    setAutoDownloadEnabled(mAutoDownload.isSelected());
    
    mAutoChannelDownload = new JCheckBox(LOCALIZER.msg("autoChannelUpdate","Automatically update available channels"));
    mAutoChannelDownloadPeriod = new WideComboBox<>();
    
    mAutoChannelDownloadPeriod.addItem(new DayPeriod(1));
    mAutoChannelDownloadPeriod.addItem(new DayPeriod(7));
    mAutoChannelDownloadPeriod.addItem(VALUE_AUTO_CHANNEL_UPDATE_PERIOD_DEFAULT);
    mAutoChannelDownloadPeriod.addItem(new DayPeriod(30));
    mAutoChannelDownloadPeriod.addItem(new DayPeriod(61));
    mAutoChannelDownloadPeriod.addItem(new DayPeriod(183));
    
    if(Settings.General.AUTO_CHANNEL_UPDATE_PERIOD.getInt() > VALUE_AUTO_CHANNEL_UPDATE_DISABLED) {
      mAutoChannelDownload.setSelected(true);
      mAutoChannelDownloadPeriod.setSelectedItem(new DayPeriod(Settings.General.AUTO_CHANNEL_UPDATE_PERIOD.getInt()));
    }
    else {
      mAutoChannelDownloadPeriod.setSelectedItem(VALUE_AUTO_CHANNEL_UPDATE_PERIOD_DEFAULT);
      mAutoChannelDownloadPeriod.setEnabled(false);
    }
    
    mAutoChannelDownload.addItemListener(e -> {
      mAutoChannelDownloadPeriod.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
    });
    
    y += 2;
    
    refreshSettings.add(mAutoChannelDownload, cc.xyw(2, y, 5));
    
    y += 2;
    
    refreshSettings.add(mAutoChannelDownloadPeriod, cc.xyw(3, y, 1));
    
    return refreshSettings.getPanel();
  }

  private void setAutoDownloadEnabled(boolean enabled) {
    mRecurrentDownload.setEnabled(enabled);
    mStartDownload.setEnabled(enabled);

    mAskBeforeDownloadRadio.setEnabled(enabled);

    mHowOften.setEnabled(enabled);
    mAutoDownloadCombo.setEnabled(enabled);
    mAskTimeRadio.setEnabled(enabled);

    mAutoDownloadWaitingTime.setEnabled(enabled);
    mAutoDownloadWaitingTimeSp.setEnabled(enabled && mAutoDownloadWaitingTime.isSelected());
    mSecondsLabel.setEnabled(enabled);

    enabled = !(mAskBeforeDownloadRadio.isSelected() || !enabled);

    mAutoDownloadPeriodCB.setEnabled(enabled);
  }

  /**
   * Used to create autostart link for Windows.
   *
   * @author René Mach
   */
  private static class LinkFile {
    private String mTarget;

    private LinkFile(File linkFile, File target, File icon, int iconIndex) throws IOException {
      RandomAccessFile write = new RandomAccessFile(linkFile, "rw");

      write.getChannel().truncate(0);

      write.writeBytes("[InternetShortcut]\r\n");
      write.writeBytes("URL=" + target.getAbsoluteFile().toURI().toURL() + "\r\n");
      write.writeBytes("WorkingDirectory=" + target.getParent());

      if(icon != null && icon.isFile()) {
        write.writeBytes("\r\nIconFile=" + icon.getAbsolutePath() + "\r\n");
        write.writeBytes("IconIndex=" + iconIndex);
      }

      write.close();
    }

    /**
     * @param linkFile The file the link is stored in.
     * @throws IOException Thrown if something went wrong.
     */
    public LinkFile(File linkFile) throws IOException {
      RandomAccessFile read = new RandomAccessFile(linkFile,"r");

      String line = null;

      while((line = read.readLine()) != null) {
        if(line.startsWith("URL")) {
          mTarget = line.substring(line.indexOf(":/")+2);
        }
      }

      read.close();
    }

    /**
     * If the link target equals the given file.
     *
     * @param file The file to check the target for.
     * @return <code>True</code> if the target matches the link of the file.
     */
    public boolean hasTarget(File file) {
      return new File(mTarget).equals(file);
    }
  }
  
  private static final class DayPeriod {
    int mDays;
    
    DayPeriod(int days) {
      mDays = days;
    }
    
    @Override
    public String toString() {
      return LOCALIZER.msg("autoChannelUpdate.every"+mDays+"days", "Every " + mDays + " days");
    }
    
    @Override
    public boolean equals(Object obj) {
      if(obj instanceof DayPeriod) {
        return mDays == ((DayPeriod)obj).mDays;
      }
      
      return super.equals(obj);
    }
  }
}