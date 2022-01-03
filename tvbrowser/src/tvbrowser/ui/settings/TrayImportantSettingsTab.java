package tvbrowser.ui.settings;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.HyperlinkEvent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsItem;
import devplugin.SettingsTab;
import tvbrowser.core.Settings;
import util.i18n.Localizer;
import util.ui.DefaultMarkingPrioritySelectionPanel;
import util.ui.MarkPriorityComboBoxRenderer;
import util.ui.UiUtilities;

/**
 * The settings tab for the IMPORTANT_TYPE of the ProgramMenuItem.
 * 
 * @author René Mach
 *
 */
public class TrayImportantSettingsTab implements SettingsTab {
  private static final Localizer LOCALIZER = TrayBaseSettingsTab.LOCALIZER;
  private static final int mMaxSizeTray = 30;
  private static final int mMaxSizeSubmenu = 40;
  
  
  private JCheckBox mIsEnabled, mShowDate, mShowTime, mShowToolTip;
  private JRadioButton mShowInSubMenu, mShowInTray;
  private JSpinner mSize;
  private JLabel mIconSeparator, mSeparator1, mSeparator2, mSizeLabel, mSizeInfo;
  
  private JEditorPane mHelpLabel;
  private JRadioButton mShowIconAndName, mShowName, mShowIcon;
  
  private JCheckBox mShowSortNumber;
  
  private JComboBox<Object> mPriority;
  private JLabel mPriorityText;
  
  private static boolean mTrayIsEnabled = Settings.Tray.ENABLED.getBoolean();
  private static TrayImportantSettingsTab mInstance;
  
  public JPanel createSettingsPanel() {
    mInstance = this;
    
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,12dlu,pref,5dlu,pref,5dlu,pref:grow,5dlu",
        "pref,5dlu,pref,pref,pref,pref,pref,10dlu,pref,5dlu,pref," +
        "pref,pref,5dlu,pref,10dlu,pref,5dlu,pref,pref,pref,fill:pref:grow,pref"));
    builder.border(Borders.DIALOG);
    
    mIsEnabled = new JCheckBox(LOCALIZER.msg("importantEnabled","Show important programs"),Settings.Tray.Important.ENABLED.getBoolean());
    mIsEnabled.setToolTipText(LOCALIZER.msg("importantToolTip","Important programs are all marked programs."));
    
    ButtonGroup bg = new ButtonGroup();
    
    mShowInSubMenu = new JRadioButton(LOCALIZER.msg("inSubMenu","in a sub menu"),Settings.Tray.Important.IN_SUB_MENU.getBoolean());
    mShowInTray = new JRadioButton(LOCALIZER.msg("inTray","in the tray menu"), !mShowInSubMenu.isSelected());
    
    bg.add(mShowInSubMenu);
    bg.add(mShowInTray);
    
    int maxSizeValue = Settings.Tray.Important.IN_SUB_MENU.getBoolean() ? mMaxSizeSubmenu : mMaxSizeTray;
    
    mSize = new JSpinner(new SpinnerNumberModel(Settings.Tray.Important.SIZE.getInt(), 1, maxSizeValue, 1));
    
    mShowIconAndName = new JRadioButton(LOCALIZER.msg("showIconName","Show channel icon and channel name"),Settings.Tray.Important.CONTAINS_NAME.getBoolean() && Settings.Tray.Important.CONTAINS_ICON.getBoolean());
    mShowName = new JRadioButton(LOCALIZER.msg("showName","Show channel name"),Settings.Tray.Important.CONTAINS_NAME.getBoolean() && !Settings.Tray.Important.CONTAINS_ICON.getBoolean());
    mShowIcon = new JRadioButton(LOCALIZER.msg("showIcon","Show channel icon"),!Settings.Tray.Important.CONTAINS_NAME.getBoolean() && Settings.Tray.Important.CONTAINS_ICON.getBoolean());
    
    ButtonGroup bg1 = new ButtonGroup();
    bg1.add(mShowIconAndName);
    bg1.add(mShowIcon);
    bg1.add(mShowName);
    
    mShowSortNumber = new JCheckBox(LOCALIZER.msg("showChannelNumber", "Show sort number"),Settings.Tray.Important.SORT_NUMBER_SHOW.getBoolean());
    
    mShowDate = new JCheckBox(LOCALIZER.msg("showDate","Show date"),Settings.Tray.Important.CONTAINS_DATE.getBoolean());
    mShowTime = new JCheckBox(LOCALIZER.msg("showTime","Show start time"),Settings.Tray.Important.CONTAINS_TIME.getBoolean());
    mShowToolTip = new JCheckBox(LOCALIZER.msg("showToolTip","Show additional information of the program in a tool tip"),Settings.Tray.Important.CONTAINS_TOOL_TIP.getBoolean());
    mShowToolTip.setToolTipText(LOCALIZER.msg("toolTipTip","Tool tips are small helper to something, like this one."));
        
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("help","The Tray is deactivated. To activate these settings activate the option <b>Tray activated</b> in the <a href=\"#link\">Tray Base settings</a>."), e -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        SettingsDialog.getInstance().showSettingsTab(SettingsItem.TRAY);
      }
    });
    
    JPanel priority = new JPanel(new FormLayout("pref,5dlu,pref","1dlu,pref"));
    
    String[] colors = DefaultMarkingPrioritySelectionPanel.getMarkingColorNames(false);
    
    mPriorityText = new JLabel(LOCALIZER.msg("importantMarkPriority","Mark priority higher or the same like:"));
    
    mPriority = new JComboBox<>(colors);
    mPriority.setSelectedIndex(Settings.Tray.Important.PRIORITY.getInt());
    mPriority.setRenderer(new MarkPriorityComboBoxRenderer(mPriority.getRenderer()));

    priority.add(mPriorityText, CC.xy(1,2));
    priority.add(mPriority, CC.xy(3,2));
    
    JPanel c = (JPanel) builder.addSeparator(LOCALIZER.msg("important","Important programs"), CC.xyw(1,1,8));
    builder.add(mIsEnabled, CC.xyw(2,3,6));
    builder.add(mShowInTray, CC.xyw(3,4,5));
    builder.add(mShowInSubMenu, CC.xyw(3,5,5));
    mSizeLabel = builder.addLabel(LOCALIZER.msg("importantSize","Number of shown programs:"), CC.xy(3,6));
    builder.add(mSize, CC.xy(5,6));
    mSizeInfo = builder.addLabel(LOCALIZER.msg("sizeInfo","(maximum: {0})",maxSizeValue), CC.xy(7,6));
    builder.add(priority, CC.xyw(3,7,5));
    
    JPanel c1 = (JPanel) builder.addSeparator(LOCALIZER.msg("iconNameSeparator","Channel icons/channel name"), CC.xyw(1,9,8));
    builder.add(mShowIconAndName, CC.xyw(2,11,6));
    builder.add(mShowIcon, CC.xyw(2,12,6));
    builder.add(mShowName, CC.xyw(2,13,6));
    
    builder.add(mShowSortNumber, CC.xyw(2,15,6));
    
    JPanel c2 = (JPanel) builder.addSeparator(LOCALIZER.msg("settings","Settings"), CC.xyw(1,17,8));
    builder.add(mShowDate, CC.xyw(2,19,6));
    builder.add(mShowTime, CC.xyw(2,20,6));
    builder.add(mShowToolTip, CC.xyw(2,21,6));
    builder.add(mHelpLabel, CC.xyw(1,23,8));
    
    mSeparator1 = (JLabel)c.getComponent(0);
    mIconSeparator = (JLabel)c1.getComponent(0);
    mSeparator2 = (JLabel)c2.getComponent(0);
    
    setEnabled(true);
    
    mShowInSubMenu.addActionListener(e -> {
      mSize.setModel(new SpinnerNumberModel(((Integer)mSize.getValue()).intValue(), 1, mMaxSizeSubmenu, 1));
      mSizeInfo.setText(LOCALIZER.msg("sizeInfo","(maximum: {0})",mMaxSizeSubmenu));
    });

    mShowInTray.addActionListener(e -> {
      int value = ((Integer)mSize.getValue()).intValue();
      mSize.setModel(new SpinnerNumberModel(value > mMaxSizeTray ? mMaxSizeTray : value, 1, mMaxSizeTray, 1));
      mSizeInfo.setText(LOCALIZER.msg("sizeInfo","(maximum: {0})",mMaxSizeTray));
    });
    
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
    
    mIconSeparator.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mSeparator2.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowInSubMenu.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowInTray.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIconAndName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIcon.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowDate.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowTime.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mSizeLabel.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mSize.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mSizeInfo.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mPriority.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mPriorityText.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
  }
  
  public void saveSettings() {
    if(mIsEnabled != null) {
      Settings.Tray.Important.ENABLED.setBoolean(mIsEnabled.isSelected());
    }
    if(mShowInSubMenu != null) {
      Settings.Tray.Important.IN_SUB_MENU.setBoolean(mShowInSubMenu.isSelected());
    }
    if(mSize != null) {
      Settings.Tray.Important.SIZE.setInt(((Integer)mSize.getValue()).intValue());
    }
    if(mShowIconAndName != null && mShowName != null && mShowIcon != null) {
      Settings.Tray.Important.CONTAINS_NAME.setBoolean(mShowIconAndName.isSelected() || mShowName.isSelected());
      Settings.Tray.Important.CONTAINS_ICON.setBoolean(mShowIconAndName.isSelected() || mShowIcon.isSelected());
    }
    if(mShowDate != null) {
      Settings.Tray.Important.CONTAINS_DATE.setBoolean(mShowDate.isSelected());
    }
    if(mShowTime != null) {
      Settings.Tray.Important.CONTAINS_TIME.setBoolean(mShowTime.isSelected());
    }
    if(mShowToolTip != null) {
      Settings.Tray.Important.CONTAINS_TOOL_TIP.setBoolean(mShowToolTip.isSelected());
    }
    if(mPriority != null) {
      Settings.Tray.Important.PRIORITY.setInt(mPriority.getSelectedIndex());
    }
    if(mShowSortNumber != null) {
      Settings.Tray.Important.SORT_NUMBER_SHOW.setBoolean(mShowSortNumber.isSelected());
    }
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return LOCALIZER.msg("important","Important programs");
  }
  
  protected static void setTrayIsEnabled(boolean value) {
    mTrayIsEnabled = value;
    if(mInstance != null) {
      mInstance.setEnabled(true);
    }
  }

}
