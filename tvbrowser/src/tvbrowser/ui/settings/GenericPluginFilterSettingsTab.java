/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.GenericFilterMap;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.filter.dlgs.EditFilterDlg;
import tvbrowser.ui.mainframe.MainFrame;
import tvdataservice.MarkedProgramsMap;
import util.i18n.Localizer;
import util.ui.EnhancedPanelBuilder;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.customizableitems.SelectableItem;
import util.ui.customizableitems.SelectableItemList;

public class GenericPluginFilterSettingsTab implements SettingsTab {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(GenericPluginFilterSettingsTab.class); 

  private SelectableItemList<PluginProxy> mGenericPluginFilterList;
  private ArrayList<PluginProxy> mCurrentlySelecteedList; 
  private HashSet<PluginProxy> mDeletedFilterList; 
  private Hashtable<PluginProxy, UserFilter> mToUpdateList;
  
  @Override
  public JPanel createSettingsPanel() {
    mToUpdateList = new Hashtable<PluginProxy, UserFilter>();
    
    PluginProxy[] currentlySelected = GenericFilterMap.getInstance().getActivatedGenericPluginFilterProxies();
    PluginProxy[] allPlugins = PluginProxyManager.getInstance().getActivatedPlugins();
    
    Arrays.sort(allPlugins, new Comparator<PluginProxy>() {
      @Override
      public int compare(PluginProxy o1, PluginProxy o2) {
        return o1.getInfo().getName().compareTo(o2.getInfo().getName());
      }
    });
    
    mDeletedFilterList = new HashSet<PluginProxy>();
    mCurrentlySelecteedList = new ArrayList<PluginProxy>();
    mCurrentlySelecteedList.addAll(Arrays.asList(currentlySelected));
    
    mGenericPluginFilterList = new SelectableItemList<>(currentlySelected, allPlugins);
    mGenericPluginFilterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    JScrollPane scrollPane = new JScrollPane(mGenericPluginFilterList);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    
    final JButton edit = new JButton(Localizer.getLocalization(Localizer.I18N_EDIT), TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
    edit.addActionListener(e -> {
      SelectableItem<PluginProxy> item = mGenericPluginFilterList.getSelectedValue();
      PluginProxy proxy = (PluginProxy)item.getItem();
      
      UserFilter filter = mToUpdateList.get(proxy);
       
      if(filter == null) {
        filter = GenericFilterMap.getInstance().getGenericPluginFilter(proxy, false);
        
        if(filter == null) {
          filter = new UserFilter(proxy.getInfo().getName());
        }
      }
      
      EditFilterDlg editFilter = new EditFilterDlg(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), FilterList.getInstance(), filter, false, true);
      
      if(editFilter.getOkWasPressed()) {
        mDeletedFilterList.remove(proxy);
        mToUpdateList.put(proxy, filter);
      }
      else if(editFilter.getDeleteWasPressed()) {
        mDeletedFilterList.add(proxy);
        mGenericPluginFilterList.removeSelection(item);
        mToUpdateList.remove(proxy);
      }
    });
    edit.setEnabled(false);
    
    mGenericPluginFilterList.addListSelectionListener(e -> {
      if(!e.getValueIsAdjusting()) {
        edit.setEnabled(e.getFirstIndex() >= 0);
        
        if(edit.isEnabled()) {
          SelectableItem<PluginProxy> item = mGenericPluginFilterList.getSelectedValue();
          PluginProxy proxy = (PluginProxy)item.getItem();
          
          if(!mCurrentlySelecteedList.contains(proxy)) {
            mCurrentlySelecteedList.add(proxy);
          }
        }
      }
    });
    
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder(new FormLayout("10dlu,100dlu:grow,default,5dlu"));
    pb.border(Borders.DIALOG);
    //
    pb.addSeparatorRowFull(false, getTitle());
    pb.addRow(UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("help", 
        "Activate and setup filter for each plugin to pre filter the highlightings and the context menu of the plugin. (Only for programs that are accepted by an activated filter can be hightlighted by the plugin and context menu can be shown.)")),
        2, 2);
    pb.addGrowingRow(scrollPane, 2, 2);
    pb.addRow("3dlu,default", edit, 3);
    
    return pb.getPanel();
  }

  @Override
  public void saveSettings() {
    List<PluginProxy> selectedPlugins = mGenericPluginFilterList.getSelectionList();
    ArrayList<PluginProxy> newSelection = new ArrayList<PluginProxy>();
    
    for(PluginProxy pluginProxy : mDeletedFilterList) {
      GenericFilterMap.getInstance().updateGenericPluginFilter(pluginProxy, null, false);
    }
    
    for(PluginProxy pluginProxy : mToUpdateList.keySet()) {
      GenericFilterMap.getInstance().updateGenericPluginFilter(pluginProxy, mToUpdateList.get(pluginProxy), true);
    }
    
    for(PluginProxy pluginProxy : selectedPlugins) {
      GenericFilterMap.getInstance().updateGenericPluginFilterActivated(pluginProxy, true);
      mCurrentlySelecteedList.remove(pluginProxy);
      newSelection.add((PluginProxy)pluginProxy);
    }
    
    for(PluginProxy unselected : mCurrentlySelecteedList) {
      GenericFilterMap.getInstance().updateGenericPluginFilterActivated((PluginProxy)unselected, false);
    }
    
    mCurrentlySelecteedList.clear();
    mCurrentlySelecteedList = null;
    mCurrentlySelecteedList = newSelection;
    
    GenericFilterMap.getInstance().storeGenericFilters();
    MarkedProgramsMap.getInstance().validateMarkings();
  }

  @Override
  public Icon getIcon() {
    return TVBrowserIcons.filter(TVBrowserIcons.SIZE_SMALL);
  }

  @Override
  public String getTitle() {
    return LOCALIZER.msg("title", "Highlighting filters");
  }

}
