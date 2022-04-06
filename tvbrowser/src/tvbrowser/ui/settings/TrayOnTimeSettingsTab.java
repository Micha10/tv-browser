package tvbrowser.ui.settings;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.HyperlinkEvent;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsItem;
import devplugin.SettingsTab;
import tvbrowser.core.Settings;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.i18n.Localizer;
import util.ui.EnhancedPanelBuilder;
import util.ui.UiUtilities;

/**
 * The settings tab for the ON_TIME_TYPE of the ProgramMenuItem.
 * 
 * @author René Mach
 *
 */
public class TrayOnTimeSettingsTab implements SettingsTab {

  private JCheckBox mIsEnabled, mShowTime, mShowToolTip, mShowProgress;
  private JRadioButton mShowInSubMenu, mShowInTray;
  private static final Localizer LOCALIZER = TrayBaseSettingsTab.LOCALIZER;
  private JLabel mIconSeparator, mSeparator1, mSeparator2, mDarkLabel, mLightLabel;
  private static boolean mTrayIsEnabled = Settings.Tray.ENABLED.getBoolean();
  
  private JEditorPane mHelpLabel, mInfo, mTimeHelp;
  private JRadioButton mShowIconAndName, mShowName, mShowIcon;
  
  private JCheckBox mShowSortNumber;
  
  private ColorLabel mLightColorLb,mDarkColorLb;
  private ColorButton mLight, mDark;
  
  private static TrayOnTimeSettingsTab mInstance;
  
  public JPanel createSettingsPanel() {
    mInstance = this;
    
    EnhancedPanelBuilder builder = new EnhancedPanelBuilder(new FormLayout("5dlu,12dlu,pref:grow,5dlu"));
    
    builder.border(Borders.DIALOG);
    
    mIsEnabled = new JCheckBox(LOCALIZER.msg("onTimeEnabled","Show programs at..."),Settings.Tray.OnTime.ENABLED.getBoolean());
    
    ButtonGroup bg = new ButtonGroup();
    
    mShowInSubMenu = new JRadioButton(LOCALIZER.msg("inSubMenu","in a sub menu"),Settings.Tray.OnTime.IN_SUB_MENU.getBoolean());
    mShowInTray = new JRadioButton(LOCALIZER.msg("inTray","in the tray menu"), !mShowInSubMenu.isSelected());
    
    bg.add(mShowInSubMenu);
    bg.add(mShowInTray);
    
    mShowIconAndName = new JRadioButton(LOCALIZER.msg("showIconName","Show channel icon and channel name"),Settings.Tray.OnTime.CONTAINS_NAME.getBoolean() && Settings.Tray.OnTime.CONTAINS_ICON.getBoolean());
    mShowIcon = new JRadioButton(LOCALIZER.msg("showIcon","Show channel icon"),Settings.Tray.OnTime.CONTAINS_ICON.getBoolean() && !Settings.Tray.OnTime.CONTAINS_NAME.getBoolean());
    mShowName = new JRadioButton(LOCALIZER.msg("showName","Show channel name"),!Settings.Tray.OnTime.CONTAINS_ICON.getBoolean() && Settings.Tray.OnTime.CONTAINS_NAME.getBoolean());
    
    ButtonGroup bg1 = new ButtonGroup();
    bg1.add(mShowIconAndName);
    bg1.add(mShowIcon);
    bg1.add(mShowName);
    
    mShowSortNumber = new JCheckBox(LOCALIZER.msg("showChannelNumber", "Show sort number"),Settings.Tray.OnTime.SORT_NUMBER_SHOW.getBoolean());
    
    mShowTime = new JCheckBox(LOCALIZER.msg("showTime","Show start time"),Settings.Tray.OnTime.CONTAINS_TIME.getBoolean());
    mShowToolTip = new JCheckBox(LOCALIZER.msg("showToolTip","Show additional information of the program in a tool tip"),Settings.Tray.OnTime.CONTAINS_TOOL_TIP.getBoolean());
    mShowToolTip.setToolTipText(LOCALIZER.msg("toolTipTip","Tool tips are small helper to something, like this one."));
    mShowProgress = new JCheckBox(LOCALIZER.msg("showProgress","Show progress bar"), Settings.Tray.OnTime.PROGRESS_SHOW.getBoolean());
    
    mLightColorLb = new ColorLabel(Settings.Tray.OnTime.COLOR_PROGRESS_BACKGROUND_LIGHT.getColor());
    mLightColorLb.setStandardColor(Settings.Tray.OnTime.COLOR_PROGRESS_BACKGROUND_LIGHT.getDefaultColor());
    mDarkColorLb = new ColorLabel(Settings.Tray.OnTime.COLOR_PROGRESS_BACKGROUND_DARK.getColor());
    mDarkColorLb.setStandardColor(Settings.Tray.OnTime.COLOR_PROGRESS_BACKGROUND_DARK.getDefaultColor());
    
    mTimeHelp =  UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("helpTime","If you want to change the times of this view, you simply have to change the times of the <a href=\"#link\">time buttons</a>."), e -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        SettingsDialog.getInstance().showSettingsTab(SettingsItem.TIMEBUTTONS);
      }
    });
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("help","The Tray is deactivated. To activate these settings activate the option <b>Tray activated</b> in the <a href=\"#link\">Tray Base settings</a>."), e -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        SettingsDialog.getInstance().showSettingsTab(SettingsItem.TRAY);
      }
    });
    
    mInfo = UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("trayProgressInfo","The progress bar simulates the progress of the program if the time would be reached."));
    
    mLight = new ColorButton(mLightColorLb);
    mDark = new ColorButton(mDarkColorLb);
    
    EnhancedPanelBuilder colors = new EnhancedPanelBuilder(new FormLayout("default,5dlu,default,5dlu,default"),"2dlu");
    
    mDarkLabel = colors.addLabelRow(false,LOCALIZER.msg("progressLight","Background color of the programs at..."), 1);
    colors.add(mLightColorLb, 3);
    colors.add(mLight, 5);

    mLightLabel = colors.addLabelRow(LOCALIZER.msg("progressDark","Progress color of the programs at..."), 1);
    colors.add(mDarkColorLb, 3);
    colors.add(mDark, 5);
    
    JPanel c = (JPanel) builder.addSeparatorRowFull(false, LOCALIZER.msg("onTime","Programs at..."));
    builder.addRow(mIsEnabled, 2, 2);
    builder.addRow(false, mShowInTray, 3);
    builder.addRow(false, mShowInSubMenu, 3);
    builder.addRow(mTimeHelp, 2, 2);
    
    JPanel c1 = (JPanel) builder.addParagraph(LOCALIZER.msg("iconNameSeparator","Channel icons/channel name"));
    builder.addRow(mShowIconAndName, 2, 2);
    builder.addRow(false, mShowIcon, 2, 2);
    builder.addRow(false, mShowName, 2, 2);
    
    builder.addRow(mShowSortNumber, 2, 2);
    
    JPanel c2 = (JPanel) builder.addParagraph(LOCALIZER.msg("settings","Settings"));
    builder.addRow(mShowTime, 2, 2);
    builder.addRow(false, mShowToolTip, 2, 2);
    builder.addRow(false, mShowProgress, 2, 2);
    builder.addRow("3dlu,default", colors.getPanel(), 3);
    builder.addRow(mInfo, 2, 2);
    builder.addRowFull("fill:pref:grow,default", mHelpLabel);
    
    mSeparator1 = (JLabel)c.getComponent(0);
    mIconSeparator = (JLabel)c1.getComponent(0);
    mSeparator2 = (JLabel)c2.getComponent(0);
    
    setEnabled(true);
    
    mIsEnabled.addActionListener(e -> {
      setEnabled(false);
    });
    
    mShowProgress.addActionListener(e -> {
      mLightColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
      mDarkColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
      mLight.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
      mDark.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
      mDarkLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
      mLightLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
      mInfo.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
    });
    
    return builder.getPanel();
  }
  
  private void setEnabled(boolean trayStateChange) {
    mHelpLabel.setVisible(!mTrayIsEnabled);
    
    if(trayStateChange) {
      mSeparator1.setEnabled(mTrayIsEnabled);
      mIsEnabled.setEnabled(mTrayIsEnabled);
    }
    
    TrayProgramsChannelsSettingsTab.setOnTimeIsEnabled(mIsEnabled.isSelected());
    mSeparator2.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowInSubMenu.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowInTray.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mIconSeparator.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mTimeHelp.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIconAndName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIcon.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowTime.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowProgress.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mLightColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
    mDarkColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
    mLight.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
    mDark.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
    mDarkLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
    mLightLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
    mInfo.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
    mShowSortNumber.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
  }

  public void saveSettings() {
    if(mIsEnabled != null) {
      Settings.Tray.OnTime.ENABLED.setBoolean(mIsEnabled.isSelected());
    }
    if(mShowInSubMenu != null) {
      Settings.Tray.OnTime.IN_SUB_MENU.setBoolean(mShowInSubMenu.isSelected());
    }
    if(mShowIconAndName != null && mShowName != null && mShowIcon != null) {
      Settings.Tray.OnTime.CONTAINS_NAME.setBoolean(mShowIconAndName.isSelected() || mShowName.isSelected());
      Settings.Tray.OnTime.CONTAINS_ICON.setBoolean(mShowIconAndName.isSelected() || mShowIcon.isSelected());
    }
    if(mShowTime != null) {
      Settings.Tray.OnTime.CONTAINS_TIME.setBoolean(mShowTime.isSelected());
    }
    if(mShowToolTip != null) {
      Settings.Tray.OnTime.CONTAINS_TOOL_TIP.setBoolean(mShowToolTip.isSelected());
    }
    if(mShowProgress != null) {
      Settings.Tray.OnTime.PROGRESS_SHOW.setBoolean(mShowProgress.isSelected());
    }
    if(mLightColorLb != null) {
      Settings.Tray.OnTime.COLOR_PROGRESS_BACKGROUND_LIGHT.setColor(mLightColorLb.getColor());
    }
    if(mDarkColorLb != null) {
      Settings.Tray.OnTime.COLOR_PROGRESS_BACKGROUND_DARK.setColor(mDarkColorLb.getColor());
    }
    if(mShowSortNumber != null) {
      Settings.Tray.OnTime.SORT_NUMBER_SHOW.setBoolean(mShowSortNumber.isSelected());
    }
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return getName();
  }
  
  /**
   * Gets the name of this settings tab.
   * 
   * @return The name of this settings tab.
   */
  public static String getName() {
    return LOCALIZER.msg("onTime","Programs at...");
  }
  
  protected static void setTrayIsEnabled(boolean value) {
    mTrayIsEnabled = value;
    if(mInstance != null) {
      mInstance.setEnabled(true);
    }
  }
}
