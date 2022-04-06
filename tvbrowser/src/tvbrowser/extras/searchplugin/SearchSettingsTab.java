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
package tvbrowser.extras.searchplugin;

import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;
import util.ui.EnhancedPanelBuilder;
import util.ui.SearchFormSettings;
import util.ui.TVBrowserIcons;

/**
 * This Class represents the SettingsTab for the Search-Plugin
 *
 * @author bodum
 */
class SearchSettingsTab implements SettingsTab {

  /** The localizer of this class. */
  private static final util.i18n.Localizer LOCALIZER
    = util.i18n.Localizer.getLocalizerFor(SearchSettingsTab.class);

  private JCheckBox mAlwaysExpertMode;
  private JList<SearchFormSettings> mSearchHistory;
  
  /**
   * Create the Settings-Panel
   * @return Settings-Panel
   */
  public JPanel createSettingsPanel() {
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder(new FormLayout("5dlu,5dlu,default,default,0dlu:grow"));
    pb.border(Borders.DIALOG);
    
    mAlwaysExpertMode = new JCheckBox(LOCALIZER.msg("alwaysExpert", "Use expert mode for repetition search also"), SearchPlugin.getAlwaysSearchExpert());
    
    SearchFormSettings[] history = SearchPlugin.getSearchHistory();
    
    final DefaultListModel<SearchFormSettings> mListModel = new DefaultListModel<SearchFormSettings>();
    
    if(history != null) {
      for(SearchFormSettings hist : history) {
        mListModel.addElement(hist);
      }
    }
    
    mSearchHistory = new JList<SearchFormSettings>(mListModel);
    
    final JButton delete = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    delete.setToolTipText(util.i18n.Localizer.getLocalization(util.i18n.Localizer.I18N_DELETE));
    delete.setEnabled(false);
    delete.addActionListener(e -> {
      final int[] indicies = mSearchHistory.getSelectedIndices();
      
      for(int i = indicies.length-1; i >= 0; i--) {
        mListModel.removeElementAt(indicies[i]);
      }
    });
    
    pb.addSeparatorRowFull(false, LOCALIZER.msg("title", "Search"));
    pb.addRowFull(mAlwaysExpertMode, 2);
    pb.addParagraph(LOCALIZER.msg("history", "Search history entries"));
    pb.addRowFull("fill:20dlu:grow", new JScrollPane(mSearchHistory), 3);
    pb.addRow("2dlu,default", delete, 3);
    
    mSearchHistory.addListSelectionListener(e -> delete.setEnabled(mSearchHistory.getSelectedIndices().length != 0));

    return pb.getPanel();
  }

  /**
   * Save Settings
   */
  public void saveSettings() {
    SearchPlugin.setAlwaysSearchExpert(mAlwaysExpertMode.isSelected());
    
    final ArrayList<SearchFormSettings> listHistory = new ArrayList<SearchFormSettings>();
    
    for(int i = 0; i < mSearchHistory.getModel().getSize(); i++) {
      listHistory.add(mSearchHistory.getModel().getElementAt(i));
    }
    
    SearchPlugin.setSearchHistory(listHistory.toArray(new SearchFormSettings[listHistory.size()]));
  }

  /**
   * Get Icon
   * @return the Icon for this SettingsTab
   */
  public Icon getIcon() {
    return TVBrowserIcons.search(TVBrowserIcons.SIZE_SMALL);
  }

  /**
   * Get Title
   * @return the Title for this SettingsTab
   */
  public String getTitle() {
    return LOCALIZER.msg("title", "Search");
  }

}