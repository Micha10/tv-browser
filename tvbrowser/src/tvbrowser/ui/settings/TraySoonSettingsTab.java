package tvbrowser.ui.settings;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.HyperlinkEvent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsItem;
import devplugin.SettingsTab;
import tvbrowser.core.Settings;
import util.i18n.Localizer;
import util.ui.UiUtilities;

/**
 * The settings tab for the SOON_TYPE of the ProgramMenuItem.
 * 
 * @author René Mach
 *
 */
public class TraySoonSettingsTab implements SettingsTab {

  private static final Localizer LOCALIZER = TrayBaseSettingsTab.LOCALIZER;
  private JCheckBox mIsEnabled, mShowTime, mShowToolTip;
  private JLabel mIconSeparator,mSeparator1, mSeparator2;
  private static boolean mTrayIsEnabled = Settings.Tray.ENABLED.getBoolean();
  
  private JEditorPane mHelpLabel;
  private JRadioButton mShowIconAndName, mShowName, mShowIcon;
  
  private JCheckBox mShowSortNumber;
  
  private static TraySoonSettingsTab mInstance;
  
  public JPanel createSettingsPanel() {
    mInstance = this;
    
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,pref:grow,5dlu",
        "pref,5dlu,pref,10dlu,pref,5dlu,pref,pref,pref,5dlu,pref," +
        "10dlu,pref,5dlu,pref,pref,fill:default:grow,pref"));
    builder.border(Borders.DIALOG);
    
    mIsEnabled = new JCheckBox(LOCALIZER.msg("soonEnabled","Show Soon running programs"),Settings.Tray.Soon.ENABLED.getBoolean());
    
    mShowIconAndName = new JRadioButton(LOCALIZER.msg("showIconName","Show channel icon and channel name"),Settings.Tray.Soon.CONTAINS_NAME.getBoolean() && Settings.Tray.Soon.CONTAINS_ICON.getBoolean());
    mShowName = new JRadioButton(LOCALIZER.msg("showName","Show channel name"),Settings.Tray.Soon.CONTAINS_NAME.getBoolean() && !Settings.Tray.Soon.CONTAINS_ICON.getBoolean());
    mShowIcon = new JRadioButton(LOCALIZER.msg("showIcon","Show channel icon"),!Settings.Tray.Soon.CONTAINS_NAME.getBoolean() && Settings.Tray.Soon.CONTAINS_ICON.getBoolean());
    
    ButtonGroup bg = new ButtonGroup();
    bg.add(mShowIconAndName);
    bg.add(mShowIcon);
    bg.add(mShowName);
    
    mShowSortNumber = new JCheckBox(LOCALIZER.msg("showChannelNumber", "Show sort number"),Settings.Tray.Soon.SORT_NUMBER_SHOW.getBoolean());
    
    mShowTime = new JCheckBox(LOCALIZER.msg("showTime","Show start time"),Settings.Tray.Soon.CONTAINS_TIME.getBoolean());
    mShowToolTip = new JCheckBox(LOCALIZER.msg("showToolTip","Show additional information of the program in a tool tip"),Settings.Tray.Soon.CONTAINS_TOOL_TIP.getBoolean());
    mShowToolTip.setToolTipText(LOCALIZER.msg("toolTipTip","Tool tips are small helper to something, like this one."));
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("help","The Tray is deactivated. To activate these settings activate the option <b>Tray activated</b> in the <a href=\"#link\">Tray Base settings</a>."), e -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        SettingsDialog.getInstance().showSettingsTab(SettingsItem.TRAY);
      }
    });
        
    JPanel c = (JPanel) builder.addSeparator(LOCALIZER.msg("soon","Soon running programs"), CC.xyw(1,1,3));
    builder.add(mIsEnabled, CC.xy(2,3));
    
    JPanel c1 = (JPanel) builder.addSeparator(LOCALIZER.msg("iconNameSeparator","Channel icons/channel name"), CC.xyw(1,5,3));
    
    builder.add(mShowIconAndName, CC.xy(2,7));
    builder.add(mShowIcon, CC.xy(2,8));
    builder.add(mShowName, CC.xy(2,9));
    
    builder.add(mShowSortNumber, CC.xy(2,11));
    
    JPanel c2 = (JPanel) builder.addSeparator(LOCALIZER.msg("settings","Settings"), CC.xyw(1,13,3));
    
    builder.add(mShowTime, CC.xy(2,15));
    builder.add(mShowToolTip, CC.xy(2,16));
    builder.add(mHelpLabel, CC.xyw(1,18,3));
    
    mSeparator1 = (JLabel)c.getComponent(0);
    mIconSeparator = (JLabel)c1.getComponent(0);
    mSeparator2 = (JLabel)c2.getComponent(0);
    
    setEnabled(true);
    
    mIsEnabled.addActionListener(e -> {
      setEnabled(false);
    });
    
    return builder.getPanel();
  }

  private void setEnabled(boolean trayStateChange) {
    mHelpLabel.setVisible(!mTrayIsEnabled);
    
    if(trayStateChange) {
      mSeparator1.setEnabled(mTrayIsEnabled);
      mIsEnabled.setEnabled(mTrayIsEnabled);
    }
    
    TrayProgramsChannelsSettingsTab.setSoonIsEnabled(mIsEnabled.isSelected());
    mIconSeparator.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mSeparator2.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIcon.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIconAndName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowSortNumber.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    
    mShowTime.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
  }
  
  public void saveSettings() {
    if(mIsEnabled != null) {
      Settings.Tray.Soon.ENABLED.setBoolean(mIsEnabled.isSelected());
    }
    if(mShowIconAndName != null && mShowName != null && mShowIcon != null) {
      Settings.Tray.Soon.CONTAINS_NAME.setBoolean(mShowIconAndName.isSelected() || mShowName.isSelected());
      Settings.Tray.Soon.CONTAINS_ICON.setBoolean(mShowIconAndName.isSelected() || mShowIcon.isSelected());
    }
    if(mShowTime != null) {
      Settings.Tray.Soon.CONTAINS_TIME.setBoolean(mShowTime.isSelected());
    }
    if(mShowToolTip != null) {
      Settings.Tray.Soon.CONTAINS_TOOL_TIP.setBoolean(mShowToolTip.isSelected());
    }
    if(mShowSortNumber != null) {
      Settings.Tray.Soon.SORT_NUMBER_SHOW.setBoolean(mShowSortNumber.isSelected());
    }
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return LOCALIZER.msg("soon","Soon running programs");
  }
  
  protected static void setTrayIsEnabled(boolean value) {
    mTrayIsEnabled = value;
    if(mInstance != null) {
      mInstance.setEnabled(true);
    }
  }
}
