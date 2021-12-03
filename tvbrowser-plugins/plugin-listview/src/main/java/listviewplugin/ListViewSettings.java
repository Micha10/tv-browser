/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */
package listviewplugin;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.factories.CC;

import compat.VersionCompat;
import devplugin.SettingsTab;
import util.ui.EnhancedPanelBuilder;
import util.ui.PluginsPictureSettingsPanel;

/**
 * Configuration Dialog for the ListView Plugin
 * 
 * @author bodum
 */
public class ListViewSettings implements SettingsTab {
  /** Translator */
  private static final util.ui.Localizer LOCALIZER = util.ui.Localizer.getLocalizerFor(ListViewSettings.class);
  
  public static final String SHOW_AT_STARTUP = "showAtStartup";
  public static final String PROVIDE_TAB = "provideTab";
  public static final String REACT_ONLY_IF_TAB_VISIBLE = "reactOnlyIfVisible";
  public static final String PICTURE_SETTINGS = "pictureSettings";
  public static final String CHANNEL_LOGO_NAME_TYPE = "channelLogoNameType";
  public static final String CHANGE_WIDTH_AUTOMATICALLY = "changeWidthAutomatically";
  
  public static final int SHOW_CHANNEL_LOGO_AND_NAME = 0;
  public static final int SHOW_CHANNEL_LOGO = 1;
  public static final int SHOW_CHANNEL_NAME = 2;
  
  /** The Settings */
  private Properties mSettings;
  /** Checkbox for showing at startup*/
  private JCheckBox mShowAtStart;
  /** Picture settings */
  private PluginsPictureSettingsPanel mPictureSettings;
  
  private JCheckBox mProvideTab;
  private JCheckBox mReactOnlyIfVisible;
  private JCheckBox mChangeWidthAutomatically;
  
  private JRadioButton mShowChannelLogoAndName;
  private JRadioButton mShowChannelLogo;
  private JRadioButton mShowChannelName;
  
  /**
   * Create the SettingsTab
   * @param settings Settings
   */
  public ListViewSettings(Properties settings) {
    mSettings = settings;
  }
  
  /**
   * Create the Panel
   */
  public JPanel createSettingsPanel() {
    EnhancedPanelBuilder panel = new EnhancedPanelBuilder("5dlu,10dlu,default:grow");
    
    mShowAtStart = new JCheckBox(LOCALIZER.msg("showAtStart", "Show at startup"));
    mShowAtStart.setSelected(mSettings.getProperty(SHOW_AT_STARTUP, "false").equals("true"));

    mProvideTab = new JCheckBox(LOCALIZER.msg("provideTab", "Provide tab in TV-Browser main window"));
    mProvideTab.setSelected(mSettings.getProperty(PROVIDE_TAB,"true").equals("true"));
    
    mChangeWidthAutomatically = new JCheckBox(LOCALIZER.msg("changeWidthAutomatically","Automatically change width with TV-Browser window"));
    mChangeWidthAutomatically.setSelected(mSettings.getProperty(CHANGE_WIDTH_AUTOMATICALLY,"false").equals("true"));
    mChangeWidthAutomatically.setEnabled(mProvideTab.isSelected());
    
    mReactOnlyIfVisible = new JCheckBox(LOCALIZER.msg("reactOnlyIfVisible", "React to TV-Browser user interaction only if visible"));
    mReactOnlyIfVisible.setSelected(mSettings.getProperty(REACT_ONLY_IF_TAB_VISIBLE,"false").equals("true"));
    mReactOnlyIfVisible.setEnabled(mProvideTab.isSelected());
    
    mProvideTab.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        mChangeWidthAutomatically.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        mReactOnlyIfVisible.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    
    mPictureSettings = new PluginsPictureSettingsPanel(ListViewPlugin.getInstance().getPictureSettings(), false);
    
    panel.addRow();
    panel.add(mShowAtStart, CC.xyw(2,panel.getRow(),2));
    
    if(VersionCompat.isCenterPanelSupported()) {
      panel.addRow();
      panel.add(mProvideTab, CC.xyw(2,panel.getRow(),2));
      panel.addRow("default");
      panel.add(mChangeWidthAutomatically, CC.xy(3,panel.getRow()));
      panel.addRow("default");
      panel.add(mReactOnlyIfVisible, CC.xy(3,panel.getRow()));
    }
    
    mShowChannelLogoAndName = new JRadioButton(LOCALIZER.msg("showIconAndName","Show channel icon and channel name"));
    mShowChannelLogo = new JRadioButton(LOCALIZER.msg("showOnlyIcon","Show channel icon"));
    mShowChannelName = new JRadioButton(LOCALIZER.msg("showOnlyName","Show channel name"));
    
    ButtonGroup channelLogoAndNameType = new ButtonGroup();
    
    channelLogoAndNameType.add(mShowChannelLogoAndName);
    channelLogoAndNameType.add(mShowChannelLogo);
    channelLogoAndNameType.add(mShowChannelName);

    switch(Integer.parseInt(mSettings.getProperty(CHANNEL_LOGO_NAME_TYPE, String.valueOf(SHOW_CHANNEL_LOGO_AND_NAME)))) {
      case SHOW_CHANNEL_LOGO_AND_NAME: mShowChannelLogoAndName.setSelected(true);break;
      case SHOW_CHANNEL_LOGO: mShowChannelLogo.setSelected(true);break;
      case SHOW_CHANNEL_NAME: mShowChannelName.setSelected(true);break;
      
      default: mShowChannelLogoAndName.setSelected(true);break;
    }
    
    panel.addParagraph(LOCALIZER.msg("logoNameTitle","Channel icons and names"));
    panel.addRow();
    panel.add(mShowChannelLogoAndName, CC.xyw(2, panel.getRow(), 2));
    panel.addRow();
    panel.add(mShowChannelLogo, CC.xyw(2, panel.getRow(), 2));
    panel.addRow();
    panel.add(mShowChannelName, CC.xyw(2, panel.getRow(), 2));
    
    panel.addParagraph(PluginsPictureSettingsPanel.getTitle());
    
    panel.addGrowingRow();
    panel.add(mPictureSettings, CC.xyw(2,panel.getRow(),2));
        
    return panel.getPanel();
  }

  /**
   * Save the Settings
   */
  public void saveSettings() {
    mSettings.setProperty(SHOW_AT_STARTUP, String.valueOf(mShowAtStart.isSelected()));
    mSettings.setProperty(PROVIDE_TAB, String.valueOf(mProvideTab.isSelected()));
    mSettings.setProperty(CHANGE_WIDTH_AUTOMATICALLY, String.valueOf(mChangeWidthAutomatically.isSelected()));
    mSettings.setProperty(REACT_ONLY_IF_TAB_VISIBLE, String.valueOf(mReactOnlyIfVisible.isSelected()));
    mSettings.setProperty(PICTURE_SETTINGS, String.valueOf(mPictureSettings.getSettings().getType()));
    
    if(mShowChannelLogoAndName.isSelected()) {
      mSettings.setProperty(CHANNEL_LOGO_NAME_TYPE, String.valueOf(SHOW_CHANNEL_LOGO_AND_NAME));
    }
    else if(mShowChannelLogo.isSelected()) {
      mSettings.setProperty(CHANNEL_LOGO_NAME_TYPE, String.valueOf(SHOW_CHANNEL_LOGO));
    }
    else if(mShowChannelName.isSelected()) {
      mSettings.setProperty(CHANNEL_LOGO_NAME_TYPE, String.valueOf(SHOW_CHANNEL_NAME));
    }
    
    ListViewPlugin.getInstance().addPanel();
  }

  /**
   * Icon for the SettingsTab
   */
  public Icon getIcon() {
    return ListViewPlugin.getInstance().createImageIcon("actions", "view-list", 16);
  }

  /**
   * Get the Title
   */
  public String getTitle() {
    return LOCALIZER.msg("settingsTabName", "ListView Plugin");
  }
}