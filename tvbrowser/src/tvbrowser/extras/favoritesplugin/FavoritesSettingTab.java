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

package tvbrowser.extras.favoritesplugin;

import java.awt.Window;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.dlgs.ExclusionPanel;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteTreeModel;
import tvbrowser.extras.favoritesplugin.dlgs.ManageFavoritesPanel;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.DefaultMarkingPrioritySelectionPanel;
import util.ui.EnhancedPanelBuilder;
import util.ui.FilterableProgramListPanel;
import util.ui.PluginChooserDlg;
import util.ui.UiUtilities;

/**
 * The settings tab for the favorites plugin.
 *
 * @author Til Schneider, www.murfman.de
 */
public class FavoritesSettingTab implements SettingsTab {

  /** The localizer for this class. */
  private static final util.i18n.Localizer LOCALIZER
    = util.i18n.Localizer.getLocalizerFor(FavoritesSettingTab.class);

  private ProgramReceiveTarget[] mClientPluginTargets, mCurrentClientPluginTargets;
  private JLabel mPluginLabel;
  private JCheckBox mExpertMode, mShowTypeSelection, mShowRepetitions, mAutoSelectRemider, mProvideTab, mShowDateSeparators;
  private JRadioButton mScrollTimeNext, mScrollTimeDay;
  private JRadioButton mFilterStartAll, mFilterStartDefault, mFilterStartCurrent, mFilterStartLast;
  private JCheckBox mFilterReactOnChange;

  private DefaultMarkingPrioritySelectionPanel mMarkingsPanel;
  private ExclusionPanel mExclusionPanel;

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    EnhancedPanelBuilder builder = new EnhancedPanelBuilder(new FormLayout("5dlu,min(150dlu;pref):grow,5dlu,pref,5dlu"));
    builder.border(Borders.DIALOG);

    mPluginLabel = new JLabel();
    JButton choose = new JButton(LOCALIZER.msg("selectPlugins","Choose Plugins"));
    mExpertMode = new JCheckBox(LOCALIZER.msg("expertMode","Always show advanced favorite edit dialog"),FavoritesPlugin.getInstance().isUsingExpertMode());
    mShowTypeSelection = new JCheckBox(LOCALIZER.msg("showTypeSelection","Show selection for creation of filter favorite"),FavoritesPlugin.getInstance().showTypeSelection());
    mShowTypeSelection.setEnabled(mExpertMode.isSelected());
    mShowRepetitions = new JCheckBox(LOCALIZER.msg("showRepetitions","Show repetitions in context menu of a favorite program"),FavoritesPlugin.getInstance().isShowingRepetitions());
    mAutoSelectRemider = new JCheckBox(LOCALIZER.msg("autoSelectReminder","Automatically remind of new favorite programs"),FavoritesPlugin.getInstance().isAutoSelectingReminder());
    mShowDateSeparators = new JCheckBox(LOCALIZER.msg("showDateSeparator","Show date separator in found programs list"),FavoritesPlugin.getInstance().showDateSeparators());
    mProvideTab = new JCheckBox(LOCALIZER.msg("provideTab","Provide tab in TV-Browser main window"),FavoritesPlugin.getInstance().provideTab());

    ProgramReceiveTarget[] targetsArr
    = FavoritesPlugin.getInstance().getClientPluginTargetIds();

    ArrayList<ProgramReceiveTarget> clientPlugins = new ArrayList<ProgramReceiveTarget>();

    for (ProgramReceiveTarget target : targetsArr) {
      ProgramReceiveIf plugin = target.getReceifeIfForIdOfTarget();
      if(plugin != null) {
        clientPlugins.add(target);
      }
    }

    mCurrentClientPluginTargets = mClientPluginTargets = clientPlugins.toArray(new ProgramReceiveTarget[clientPlugins.size()]);

    mExpertMode.addItemListener(e -> {
      mShowTypeSelection.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
    });
    
    handlePluginSelection();

    choose.addActionListener(e -> {
      Window parent = UiUtilities
          .getLastModalChildOf(MainFrame.getInstance());
      PluginChooserDlg chooser = null;
      chooser = new PluginChooserDlg(parent, mClientPluginTargets, null,
          ReminderPluginProxy.getInstance());
      
      chooser.setVisible(true);

      if(chooser.getReceiveTargets() != null) {
        mClientPluginTargets = chooser.getReceiveTargets();
      }

      handlePluginSelection();
    });
    
    EnhancedPanelBuilder timeButtonSettings = new EnhancedPanelBuilder(new FormLayout("10dlu,default:grow"));
    
    final JLabel timeButtonBehaviour = new JLabel(LOCALIZER.msg("timeButtonBehaviour", "Time buttons behaviour:"));
    
    mScrollTimeNext = new JRadioButton(LOCALIZER.msg("timeButtonScrollNext", "Scroll to next occurence of time from shown programs onward"), FavoritesPlugin.getInstance().timeButtonsScrollToNextTimeInTab());
    mScrollTimeDay = new JRadioButton(LOCALIZER.msg("timeButtonScrollDay", "Scroll to occurence of time on shown day in list"), !mScrollTimeNext.isSelected());
    
    ButtonGroup bg = new ButtonGroup();
    
    bg.add(mScrollTimeDay);
    bg.add(mScrollTimeNext);
    
    timeButtonBehaviour.setEnabled(mProvideTab.isSelected());
    mScrollTimeDay.setEnabled(mProvideTab.isSelected());
    mScrollTimeNext.setEnabled(mProvideTab.isSelected());
    
    mProvideTab.addItemListener(e -> {
      timeButtonBehaviour.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      mScrollTimeDay.setEnabled(timeButtonBehaviour.isEnabled());
      mScrollTimeNext.setEnabled(timeButtonBehaviour.isEnabled());
    });
    
    timeButtonSettings.addRow(timeButtonBehaviour, 2);
    timeButtonSettings.addRow(mScrollTimeNext, 2);
    timeButtonSettings.addRow(mScrollTimeDay, 2);
    
    int filterStartType = FavoritesPlugin.getInstance().getFilterStartType();
    
    EnhancedPanelBuilder filterSettingsPanel = new EnhancedPanelBuilder(new FormLayout("10dlu,default:grow"));
    
    mFilterStartAll = new JRadioButton(LOCALIZER.msg("filterStartAll", "Show all filter"), filterStartType == FilterableProgramListPanel.FILTER_START_ALL_TYPE);
    mFilterStartDefault = new JRadioButton(LOCALIZER.msg("filterStartDefault", "Default filter"), filterStartType == FilterableProgramListPanel.FILTER_START_DEFAULT_TYPE);
    mFilterStartCurrent = new JRadioButton(LOCALIZER.msg("filterStartCurrent", "Current TV-Browser filter"), filterStartType == FilterableProgramListPanel.FILTER_START_CURRENT_TYPE);
    mFilterStartLast = new JRadioButton(LOCALIZER.msg("filterStartLast", "Last used filter"), filterStartType == ManageFavoritesPanel.FILTER_START_LAST_TYPE);
    
    ButtonGroup filterStartGroup = new ButtonGroup();
    
    filterStartGroup.add(mFilterStartAll);
    filterStartGroup.add(mFilterStartDefault);
    filterStartGroup.add(mFilterStartCurrent);
    filterStartGroup.add(mFilterStartLast);
    
    mFilterReactOnChange = new JCheckBox(LOCALIZER.msg("filterReactOnChange", "React on changes of selected filter of TV-Browser"), FavoritesPlugin.getInstance().reactOnFilterChange());
    
    filterSettingsPanel.addLabelRowFull(false, LOCALIZER.msg("filterStart", "Start with:"));
    filterSettingsPanel.addRow("2dlu,default", mFilterStartAll, 2);
    filterSettingsPanel.addRow("1dlu,default", mFilterStartDefault, 2);
    filterSettingsPanel.addRow("1dlu,default", mFilterStartCurrent, 2);
    filterSettingsPanel.addRow("1dlu,default", mFilterStartLast, 2);
    filterSettingsPanel.addRowFull("3dlu,default", mFilterReactOnChange);
    
    builder.addSeparatorRowFull(false,LOCALIZER.msg("passTo", "Pass favorite programs to"));
    
    builder.addRow(mPluginLabel, 2);
    builder.add(choose, 4);
    
    builder.addParagraph(LOCALIZER.msg("expertSettings","Expert mode"));
    builder.addRowFull(mExpertMode, 2);
    builder.addRowFull(false, mShowTypeSelection, 2);
    
    builder.addParagraph(LOCALIZER.msg("repetitionSettings","Repetitions"));
    builder.addRowFull(mShowRepetitions, 2);
    
    builder.addParagraph(LOCALIZER.msg("reminderSettings","Automatic reminder"));
    builder.addRowFull(mAutoSelectRemider, 2);
    
    builder.addParagraph(LOCALIZER.msg("exclusions","Global exclusion criterions"));
    builder.addRowFull(mExclusionPanel = new ExclusionPanel(FavoritesPlugin.getInstance().getGlobalExclusions(), UiUtilities.getLastModalChildOf(MainFrame.getInstance()), null), 2);
    
    builder.addParagraph(LOCALIZER.msg("miscSettings","Miscellaneous"));
    builder.addRowFull(mShowDateSeparators, 2);
    builder.addRowFull(false, mProvideTab, 2);
    builder.addRowFull(false, timeButtonSettings.getPanel(), 2);
    
    builder.addParagraph(LOCALIZER.msg("filter", "Program Filter"));
    builder.addRowFull(filterSettingsPanel.getPanel(), 2);
    
    builder.addParagraph(DefaultMarkingPrioritySelectionPanel.getTitle());
    builder.addRowFull(mMarkingsPanel = DefaultMarkingPrioritySelectionPanel.createPanel(FavoritesPlugin.getInstance().getMarkPriority(),false,false), 2);
    
    return builder.getPanel();
  }

  private void handlePluginSelection() {
    ArrayList<ProgramReceiveIf> plugins = new ArrayList<ProgramReceiveIf>();

    if(mClientPluginTargets != null) {
      for (ProgramReceiveTarget element : mClientPluginTargets) {
        if(!plugins.contains(element.getReceifeIfForIdOfTarget())) {
          plugins.add(element.getReceifeIfForIdOfTarget());
        }
      }

      ProgramReceiveIf[] mClientPlugins = plugins.toArray(new ProgramReceiveIf[plugins.size()]);

      if(mClientPlugins.length > 0) {
        mPluginLabel.setText(mClientPlugins[0].toString());
        mPluginLabel.setEnabled(true);
      }
      else {
        mPluginLabel.setText(LOCALIZER.msg("noPlugins","No Plugins choosen"));
        mPluginLabel.setEnabled(false);
      }

      for (int i = 1; i < (mClientPlugins.length > 4 ? 3 : mClientPlugins.length); i++) {
        mPluginLabel.setText(mPluginLabel.getText() + ", " + mClientPlugins[i]);
      }

      if(mClientPlugins.length > 4) {
        mPluginLabel.setText(mPluginLabel.getText() + " (" + (mClientPlugins.length - 3) + " " + LOCALIZER.ellipsisMsg("otherPlugins","others") + ")");
      }
    }
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    if(!Arrays.equals(mCurrentClientPluginTargets, mClientPluginTargets)) {
      FavoritesPlugin.getInstance().setClientPluginTargets(mClientPluginTargets);

      Favorite[] favoriteArr = FavoriteTreeModel.getInstance().getFavoriteArr();

      for(Favorite favorite : favoriteArr) {
        favorite.handleNewGlobalReceiveTargets(mCurrentClientPluginTargets);
      }
    }
    FavoritesPlugin.getInstance().setIsUsingExpertMode(mExpertMode.isSelected());
    FavoritesPlugin.getInstance().setShowTypeSelection(mShowTypeSelection.isSelected());
    FavoritesPlugin.getInstance().setShowRepetitions(mShowRepetitions.isSelected());
    FavoritesPlugin.getInstance().setAutoSelectingReminder(mAutoSelectRemider.isSelected());
    FavoritesPlugin.getInstance().setMarkPriority(mMarkingsPanel.getSelectedPriority());
    FavoritesPlugin.getInstance().setShowDateSeparators(mShowDateSeparators.isSelected());
    FavoritesPlugin.getInstance().setProvideTab(mProvideTab.isSelected());
    FavoritesPlugin.getInstance().setTimeButtonsScrollToNextTimeInTab(mScrollTimeNext.isSelected());
    FavoritesPlugin.getInstance().setReactOnFilterChange(mFilterReactOnChange.isSelected());
    
    if(mFilterStartAll.isSelected()) {
      FavoritesPlugin.getInstance().setFilterStartType(FilterableProgramListPanel.FILTER_START_ALL_TYPE);
    }
    else if(mFilterStartDefault.isSelected()) {
      FavoritesPlugin.getInstance().setFilterStartType(FilterableProgramListPanel.FILTER_START_DEFAULT_TYPE);
    }
    else if(mFilterStartCurrent.isSelected()) {
      FavoritesPlugin.getInstance().setFilterStartType(FilterableProgramListPanel.FILTER_START_CURRENT_TYPE);
    }
    else if(mFilterStartLast.isSelected()) {
      FavoritesPlugin.getInstance().setFilterStartType(ManageFavoritesPanel.FILTER_START_LAST_TYPE);
    }

    if(mExclusionPanel.wasChanged()) {
      FavoritesPlugin.getInstance().setGlobalExclusions(mExclusionPanel.getExclusions(),mExclusionPanel.wasAdded() && !mExclusionPanel.wasEditedOrDeleted());
    }
    else {
      FavoritesPlugin.getInstance().saveFavorites();
    }
  }

  /**
   * Returns the icon of the tab-sheet.
   */
  public Icon getIcon() {
    return FavoritesPlugin.getFavoritesIcon(16);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return LOCALIZER.msg("name", "Favorite programs");
  }

}
