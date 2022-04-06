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
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.CancelableSettingsTab;
import tvbrowser.core.Settings;
import util.settings.ProgramPanelSettings;
import util.ui.EnhancedPanelBuilder;

/**
 * Settings for the icon and name values in
 * program table and channel list.
 * 
 * @author René Mach
 */
public class ChannelIconAndNameSettingsTab implements CancelableSettingsTab {
  private static final util.i18n.Localizer LOCALIZER = util.i18n.Localizer.getLocalizerFor(ChannelIconAndNameSettingsTab.class);
  
  private JRadioButton mShowIconAndNameInProgramTable;
  private JRadioButton mShowOnlyIconInProgramTable;
  private JRadioButton mShowOnlyNameInProgramTable;

  private JRadioButton mShowIconInProgramPanelPlugins;
  private JRadioButton mShowIconInProgramPanelNever;
  
  private JRadioButton mShowIconAndNameInChannelLists;
  private JRadioButton mShowOnlyIconInChannelLists;
  private JRadioButton mShowOnlyNameInChannelLists;

  private JCheckBox mShowTooltipInProgramTable;
  
  private JCheckBox mShowSortNumberInProgramTable;
  private JCheckBox mShowSortNumberInChannelLists;
  
  private static int INDEX_ICONS_PROGRAM_PANEL = -1;
  
  public ChannelIconAndNameSettingsTab() {
  }
  
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder(new FormLayout("5dlu, default:grow, default, 5dlu"));
    pb.border(Borders.DIALOG);
    
    pb.addSeparatorRowFull(false, LOCALIZER.msg("programTable","Program table"));
    pb.addRow(mShowIconAndNameInProgramTable = new JRadioButton(LOCALIZER.msg("showIconAndName","Show channel icon and channel name"), Settings.IconAndNames.SHOW_ICONS_IN_PROGRAM_TABLE.getBoolean() && Settings.IconAndNames.SHOW_NAMES_IN_PROGRAM_TABLE.getBoolean()), 2, 2);
    pb.addRow(false, mShowOnlyIconInProgramTable = new JRadioButton(LOCALIZER.msg("showOnlyIcon","Show channel icon"), Settings.IconAndNames.SHOW_ICONS_IN_PROGRAM_TABLE.getBoolean() && !Settings.IconAndNames.SHOW_NAMES_IN_PROGRAM_TABLE.getBoolean()), 2, 2);
    pb.addRow(false, mShowOnlyNameInProgramTable = new JRadioButton(LOCALIZER.msg("showOnlyName","Show channel name"), Settings.IconAndNames.SHOW_NAMES_IN_PROGRAM_TABLE.getBoolean() && !Settings.IconAndNames.SHOW_ICONS_IN_PROGRAM_TABLE.getBoolean()), 2, 2);
    
    pb.addRow(mShowSortNumberInProgramTable = new JCheckBox(LOCALIZER.msg("showChannelNumber", "Show sort number"), Settings.IconAndNames.SHOW_SORT_NUMBER_IN_PROGRAM_TABLE.getBoolean()), 2);
    pb.addRow(false, mShowTooltipInProgramTable = new JCheckBox(LOCALIZER.msg("showToolTip","Show large channel icons in tooltip"), Settings.IconAndNames.SHOW_CHANNEL_TOOLTIP_IN_PROGRAM_TABLE.getBoolean()), 2);
    
    mShowTooltipInProgramTable.setEnabled(!mShowOnlyNameInProgramTable.isSelected());
    
    mShowOnlyNameInProgramTable.addItemListener(e -> {
      mShowTooltipInProgramTable.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
    });
    
    ButtonGroup programTable = new ButtonGroup();
    programTable.add(mShowIconAndNameInProgramTable);
    programTable.add(mShowOnlyIconInProgramTable);
    programTable.add(mShowOnlyNameInProgramTable);
    
    pb.addParagraph(LOCALIZER.msg("programPanels", "Program panels"));
    pb.addRow(mShowIconInProgramPanelPlugins = new JRadioButton(LOCALIZER.msg("showLogoPlugins", "Plugins decide showing of channel logos"), Settings.IconAndNames.SHOW_LOGO_FOR_PROGRAM_PANEL.getInt() == ProgramPanelSettings.SHOW_CHANNEL_LOGO_PLUGINS_CONTROL), 2, 2);
    pb.addRow(false, mShowIconInProgramPanelNever = new JRadioButton(LOCALIZER.msg("showLogoNever", "Channel logos are never shown"), Settings.IconAndNames.SHOW_LOGO_FOR_PROGRAM_PANEL.getInt() == ProgramPanelSettings.SHOW_CHANNEL_LOGO_NEVER), 2, 2);
    
    if(INDEX_ICONS_PROGRAM_PANEL == -1) {
      INDEX_ICONS_PROGRAM_PANEL = Settings.IconAndNames.SHOW_LOGO_FOR_PROGRAM_PANEL.getInt();
    }
    
    ButtonGroup programPanels = new ButtonGroup();
    programPanels.add(mShowIconInProgramPanelPlugins);
    programPanels.add(mShowIconInProgramPanelNever);
    
    pb.addParagraph(LOCALIZER.msg("channelLists","Channel lists"), 1, 4);
    pb.addRow(mShowIconAndNameInChannelLists = new JRadioButton(LOCALIZER.msg("showIconAndName","Show channel icon and channel name"), Settings.IconAndNames.SHOW_ICONS_IN_CHANNEL_LIST.getBoolean() && Settings.IconAndNames.SHOW_NAMES_IN_CHANNEL_LIST.getBoolean()), 2, 2);
    pb.addRow(false, mShowOnlyIconInChannelLists = new JRadioButton(LOCALIZER.msg("showOnlyIcon","Show channel icon"), Settings.IconAndNames.SHOW_ICONS_IN_CHANNEL_LIST.getBoolean() && !Settings.IconAndNames.SHOW_NAMES_IN_CHANNEL_LIST.getBoolean()), 2, 2);
    pb.addRow(false, mShowOnlyNameInChannelLists = new JRadioButton(LOCALIZER.msg("showOnlyName","Show channel name"), Settings.IconAndNames.SHOW_NAMES_IN_CHANNEL_LIST.getBoolean() && !Settings.IconAndNames.SHOW_ICONS_IN_CHANNEL_LIST.getBoolean()), 2, 2);
    
    pb.addRow(mShowSortNumberInChannelLists = new JCheckBox(LOCALIZER.msg("showChannelNumber", "Show sort number"), Settings.IconAndNames.SHOW_SORT_NUMBER_IN_PROGRAM_LISTS.getBoolean()), 2, 2);
    
    final ButtonGroup channelLists = new ButtonGroup();
    channelLists.add(mShowIconAndNameInChannelLists);
    channelLists.add(mShowOnlyIconInChannelLists);
    channelLists.add(mShowOnlyNameInChannelLists);
    
    
    final ItemListener pluginProgramPanelLogoListener = e -> {
      if(e.getStateChange() == ItemEvent.SELECTED) {
        Settings.setRestartInfo(ChannelIconAndNameSettingsTab.class.getCanonicalName(), (mShowIconInProgramPanelPlugins.equals(e.getItem()) && INDEX_ICONS_PROGRAM_PANEL != ProgramPanelSettings.SHOW_CHANNEL_LOGO_PLUGINS_CONTROL) || (mShowIconInProgramPanelNever.equals(e.getItem()) && INDEX_ICONS_PROGRAM_PANEL != ProgramPanelSettings.SHOW_CHANNEL_LOGO_NEVER));
      }
    };
    
    mShowIconInProgramPanelNever.addItemListener(pluginProgramPanelLogoListener);
    mShowIconInProgramPanelPlugins.addItemListener(pluginProgramPanelLogoListener);
    
    return pb.getPanel();
  }
  
    /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Settings.IconAndNames.SHOW_ICONS_IN_PROGRAM_TABLE.setBoolean(mShowIconAndNameInProgramTable.isSelected() || mShowOnlyIconInProgramTable.isSelected());
    Settings.IconAndNames.SHOW_NAMES_IN_PROGRAM_TABLE.setBoolean(mShowIconAndNameInProgramTable.isSelected() || mShowOnlyNameInProgramTable.isSelected());
    
    if(mShowIconInProgramPanelPlugins.isSelected()) {
      Settings.IconAndNames.SHOW_LOGO_FOR_PROGRAM_PANEL.setInt(ProgramPanelSettings.SHOW_CHANNEL_LOGO_PLUGINS_CONTROL);
    }
    else {
      Settings.IconAndNames.SHOW_LOGO_FOR_PROGRAM_PANEL.setInt(ProgramPanelSettings.SHOW_CHANNEL_LOGO_NEVER);
    }
    
    Settings.IconAndNames.SHOW_ICONS_IN_CHANNEL_LIST.setBoolean(mShowIconAndNameInChannelLists.isSelected() || mShowOnlyIconInChannelLists.isSelected());
    Settings.IconAndNames.SHOW_NAMES_IN_CHANNEL_LIST.setBoolean(mShowIconAndNameInChannelLists.isSelected() || mShowOnlyNameInChannelLists.isSelected());

    Settings.IconAndNames.SHOW_SORT_NUMBER_IN_PROGRAM_TABLE.setBoolean(mShowSortNumberInProgramTable.isSelected());
    Settings.IconAndNames.SHOW_CHANNEL_TOOLTIP_IN_PROGRAM_TABLE.setBoolean(mShowTooltipInProgramTable.isSelected());
    Settings.IconAndNames.SHOW_SORT_NUMBER_IN_PROGRAM_LISTS.setBoolean(mShowSortNumberInChannelLists.isSelected());
  }
  

  @Override
  public void cancel() {
    Settings.setRestartInfo(ChannelIconAndNameSettingsTab.class.getCanonicalName(),INDEX_ICONS_PROGRAM_PANEL != Settings.IconAndNames.SHOW_LOGO_FOR_PROGRAM_PANEL.getInt());
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return null;
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return LOCALIZER.msg("title", "Channel icons and names");
  }
}