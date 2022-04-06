/*
 * TV-Browser
 * Copyright (C) 2012 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.PluginCenterPanel;
import devplugin.PluginCenterPanelWrapper;
import devplugin.SettingsTab;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.ui.DontShowAgainOptionBox;
import tvbrowser.ui.mainframe.MainFrame;
import util.i18n.Localizer;
import util.ui.EnhancedPanelBuilder;
import util.ui.OrderChooser;
import util.ui.UiUtilities;

public class CenterPanelSettingsTab implements SettingsTab {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(CenterPanelSettingsTab.class);

  private OrderChooser<PluginCenterPanel> mPanelChooser;
  private JCheckBox mTabBarAlwaysVisible,mShowPluginActionsInMenu;
  private ArrayList<PluginCenterPanel> mAllPanelList;
  
  private JRadioButton mNameOnly, mIconOnly, mNameAndIcon;
  
  @Override
  public JPanel createSettingsPanel() {
    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList.getInstance().getAvailableProxys();
    
    mAllPanelList = new ArrayList<PluginCenterPanel>();
    ArrayList<PluginCenterPanel> currentOrderList = new ArrayList<PluginCenterPanel>(); 
    
    mAllPanelList.add(MainFrame.getInstance().getProgramTableScrollPaneWrapper());
    mAllPanelList.add(MainFrame.getInstance().getPluginViewWrapper());

    for(PluginProxy plugin : plugins) {
      try {
        PluginCenterPanelWrapper wrapper = plugin.getPluginCenterPanelWrapper();
        
        if(wrapper != null) {
          PluginCenterPanel[] panels = wrapper.getCenterPanels();
          
          for(PluginCenterPanel panel : panels) {
            if(panel != null && panel.getName() != null && panel.getPanel() != null && panel.getId() != null) {
              mAllPanelList.add(panel);
            }
          }
        }
        // Prevent problems from Plugins
      }catch(Throwable t) {}
    }
    
    for(InternalPluginProxyIf internalPlugin : internalPlugins) {
      try {
        PluginCenterPanelWrapper wrapper = internalPlugin.getPluginCenterPanelWrapper();
        
        if(wrapper != null) {
          PluginCenterPanel[] panels = wrapper.getCenterPanels();
          
          for(PluginCenterPanel panel : panels) {
            if(panel != null && panel.getName() != null && panel.getPanel() != null && panel.getId() != null) {
              mAllPanelList.add(panel);
            }
          }
        }
        // Prevent problems from Plugins
      }catch(Throwable t) {}
    }
    
    for(String id : Settings.CenterPanels.CENTER_PANEL_ARR.getStringArray()) {
      for(PluginCenterPanel centerPanel : mAllPanelList) {
        if(id.equals(centerPanel.getId())) {
          currentOrderList.add(centerPanel);  
        }
      }
    }
    
    mPanelChooser = new OrderChooser<>(currentOrderList.toArray(new PluginCenterPanel[currentOrderList.size()]), mAllPanelList.toArray(new PluginCenterPanel[mAllPanelList.size()]));
    mTabBarAlwaysVisible = new JCheckBox(LOCALIZER.msg("alwaysShowTabs", "Always show tabs"), Settings.CenterPanels.ALWAYS_SHOW_TAB_BAR_FOR_CENTER_PANEL.getBoolean());
    mShowPluginActionsInMenu = new JCheckBox(ToolBarDragAndDropSettings.LOCALIZER.msg("showPluginAction", "Show plugin actions in context menu"), Settings.CenterPanels.PLUGIN_FUNCTIONS_IN_MENU_SHOW.getBoolean());
    
    mNameOnly = new JRadioButton(LOCALIZER.msg("nameOnly", "Name only"), Settings.CenterPanels.TAB_BAR_CENTER_PANEL_NAME_ICON_CONFIG.getInt() == Settings.IconAndNames.VALUE_NAME_ONLY);
    mIconOnly = new JRadioButton(LOCALIZER.msg("iconOnly", "Icon only (if available)"), Settings.CenterPanels.TAB_BAR_CENTER_PANEL_NAME_ICON_CONFIG.getInt() == Settings.IconAndNames.VALUE_ICON_ONLY);
    mNameAndIcon = new JRadioButton(LOCALIZER.msg("nameAndIcon", "Name and icon"), Settings.CenterPanels.TAB_BAR_CENTER_PANEL_NAME_ICON_CONFIG.getInt() == Settings.IconAndNames.VALUE_NAME_AND_ICON);
    
    final ButtonGroup bg = new ButtonGroup();
    
    bg.add(mNameOnly);
    bg.add(mIconOnly);
    bg.add(mNameAndIcon);
    
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder(new FormLayout("5dlu,default:grow,5dlu"));
    pb.border(Borders.DIALOG);

    pb.addSeparatorRowFull(false, LOCALIZER.msg("info", "Shown tabs in the main window"));
    pb.addGrowingRow(mPanelChooser, 2);
    pb.addRow(mTabBarAlwaysVisible, 2);
    pb.addRow("1dlu,default", mShowPluginActionsInMenu, 2);
    
    pb.addParagraph(LOCALIZER.msg("nameAndIconSep", "Name and icon display"));
    pb.addRow(mNameOnly, 2);
    pb.addRow("1dlu,default", mIconOnly, 2);
    pb.addRow("1dlu,default", mNameAndIcon, 2);
    
    return pb.getPanel();
  }

  @Override
  public void saveSettings() {
    List<PluginCenterPanel> order = mPanelChooser.getOrderList();
    ArrayList<String> idList = new ArrayList<String>(order.size());
    
    for(PluginCenterPanel panel : order) {
      if(panel != null) {
        mAllPanelList.remove(panel);
        idList.add(panel.getId());
      }
    }
    
    if(!idList.contains(MainFrame.getInstance().getProgramTableScrollPaneWrapper().getId())) {
      String[] options = new String[] {LOCALIZER.msg("programTableTabKeepDeactivated", "Keep deactivated"),LOCALIZER.msg("programTableTabActivate", "Activate program table tab again")};
      
      if(DontShowAgainOptionBox.showOptionDialog("CenterPanelSettings.programTableTabdeselected", UiUtilities.getLastModalChildOf(MainFrame.getInstance()), LOCALIZER.msg("programTableDeselected", "You have deselected the program table you might miss some programs in the future.\nAre you sure?"),LOCALIZER.msg("programTableDeselectedTitle", "Program table deselected"),JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_OPTION,options,options[1],null) == JOptionPane.NO_OPTION) {
        idList.add(0, MainFrame.getInstance().getProgramTableScrollPaneWrapper().getId());
      }
    }
    
    if(idList.isEmpty()) {
      idList.add(MainFrame.getInstance().getProgramTableScrollPaneWrapper().getId());
    }
    
    ArrayList<String> disabledIdList = new ArrayList<String>(mAllPanelList.size());
    
    for(PluginCenterPanel centerPanel :  mAllPanelList) {
      disabledIdList.add(centerPanel.getId());
    }
    
    Settings.CenterPanels.CENTER_PANEL_ARR.setStringArray(idList.toArray(new String[idList.size()]));
    Settings.CenterPanels.ALWAYS_SHOW_TAB_BAR_FOR_CENTER_PANEL.setBoolean(mTabBarAlwaysVisible.isSelected());
    Settings.CenterPanels.PLUGIN_FUNCTIONS_IN_MENU_SHOW.setBoolean(mShowPluginActionsInMenu.isSelected());
    Settings.CenterPanels.DISABLED_CENTER_PANEL_ARR.setStringArray(disabledIdList.toArray(new String[0]));
    
    int selection = Settings.IconAndNames.VALUE_NAME_AND_ICON;
    
    if(mNameOnly.isSelected()) {
      selection = Settings.IconAndNames.VALUE_NAME_ONLY;
    }
    else if(mIconOnly.isSelected()) {
      selection = Settings.IconAndNames.VALUE_ICON_ONLY;
    }
    
    Settings.CenterPanels.TAB_BAR_CENTER_PANEL_NAME_ICON_CONFIG.setInt(selection);
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getTitle() {
    return LOCALIZER.msg("title", "Main window");
  }
}
