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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;
import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import util.i18n.Localizer;
import util.misc.JavaVersion;
import util.misc.OperatingSystem;
import util.ui.EnhancedPanelBuilder;

/**
 * The base settings for the tray.
 *
 * @author René Mach
 *
 */
public class TrayBaseSettingsTab implements SettingsTab {

  protected static final Localizer LOCALIZER = Localizer
  .getLocalizerFor(TrayBaseSettingsTab.class);

  private JCheckBox mTrayIsEnabled, mMinimizeToTrayChb, mNowOnRestore, mTrayIsAnialiasing;
  private boolean mOldState;
  private static boolean mIsEnabled = Settings.Tray.ENABLED.getBoolean();
  private JRadioButton mFilterAll,mNoMarkedFiltering,mNoFiltering;

  public JPanel createSettingsPanel() {

    final EnhancedPanelBuilder builder = new EnhancedPanelBuilder(new FormLayout("5dlu, 50dlu:grow, 5dlu"));
    builder.border(Borders.DIALOG);

    String msg = LOCALIZER.msg("trayIsEnabled", "Tray activated");
    mOldState = Settings.Tray.ENABLED.getBoolean();
    mTrayIsEnabled = new JCheckBox(msg, mOldState);

    msg = LOCALIZER.msg("minimizeToTray", "Minimize to Tray");
    boolean checked = Settings.Tray.MINIMIZE_TO.getBoolean();
    mMinimizeToTrayChb = new JCheckBox(msg, checked && mOldState);
    mMinimizeToTrayChb.setEnabled(mTrayIsEnabled.isSelected());
    
    msg = LOCALIZER.msg("nowOnDeIconify", "Jump to now when restoring application");
    checked = Settings.Tray.NOW_ON_RESTORE.getBoolean();
    mNowOnRestore = new JCheckBox(msg, checked);

    msg = LOCALIZER.msg("trayAntialiasing", "Antialiasing enabled");
    checked = Settings.Tray.ANTIALIASING.getBoolean();
    mTrayIsAnialiasing = new JCheckBox(msg, checked);
    
    if(System.getProperty("os.name").toLowerCase().startsWith("linux") && (JavaVersion.getVersion() < JavaVersion.VERSION_1_6 || OperatingSystem.isKDE())) {
      mMinimizeToTrayChb.addActionListener(e -> {
        if(mMinimizeToTrayChb.isSelected()) {
          JOptionPane.showMessageDialog(builder.getPanel(),LOCALIZER.msg("minimizeToTrayWarning","This function might not work as expected on Unix systems like KDE or Gnome.\nSo it's recommended not to select this checkbox."),LOCALIZER.msg("warning","Warning"), JOptionPane.WARNING_MESSAGE);
        }
      });
    }
    
    //filter settings
    ButtonGroup filter = new ButtonGroup();
    
    msg = LOCALIZER.msg("trayFilterAll", "Filter all programs");
    mFilterAll = new JRadioButton(msg);

    msg = LOCALIZER.msg("trayFilterNotMarked", "Filter programs, if not marked");
    checked = Settings.Tray.FILTER_NOT_MARKED.getBoolean();
    mNoMarkedFiltering = new JRadioButton(msg, checked);

    msg = LOCALIZER.msg("trayFilterNot", "Don't filter programs");
    checked = Settings.Tray.FILTER_NOT.getBoolean();
    mNoFiltering = new JRadioButton(msg, checked);
    
    if(!mNoFiltering.isSelected() && !mNoMarkedFiltering.isSelected()) {
      mFilterAll.setSelected(true);
    }
    
    filter.add(mFilterAll);
    filter.add(mNoMarkedFiltering);
    filter.add(mNoFiltering);
    
    //create panel
    builder.addSeparatorRowFull(false, LOCALIZER.msg("basics", "Basic settings"));
    builder.addRow(mTrayIsEnabled, 2);
    builder.addRow(false, mTrayIsAnialiasing, 2);
    builder.addRow(false, mMinimizeToTrayChb, 2);
    
    builder.addParagraph(LOCALIZER.msg("filter", "Filter settings"));
    builder.addRow(mFilterAll, 2);
    builder.addRow(false, mNoMarkedFiltering, 2);
    builder.addRow(false, mNoFiltering, 2);

    mTrayIsEnabled.addActionListener(e -> {
      mIsEnabled = mTrayIsEnabled.isSelected();
      TrayImportantSettingsTab.setTrayIsEnabled(mIsEnabled);
      TrayNowSettingsTab.setTrayIsEnabled(mIsEnabled);
      TrayOnTimeSettingsTab.setTrayIsEnabled(mIsEnabled);
      TraySoonSettingsTab.setTrayIsEnabled(mIsEnabled);
      TrayProgramsChannelsSettingsTab.setTrayIsEnabled(mIsEnabled);
      mMinimizeToTrayChb.setEnabled(mTrayIsEnabled.isSelected());
      mNowOnRestore.setEnabled(mTrayIsEnabled.isSelected());
      mTrayIsAnialiasing.setEnabled(mTrayIsEnabled.isSelected());
      mFilterAll.setEnabled(mTrayIsEnabled.isSelected());
      mNoMarkedFiltering.setEnabled(mTrayIsEnabled.isSelected());
      mNoFiltering.setEnabled(mTrayIsEnabled.isSelected());
    });

    return builder.getPanel();
  }

  public void saveSettings() {
    if (mTrayIsEnabled != null) {
      Settings.Tray.ENABLED.setBoolean(mTrayIsEnabled.isSelected());
      if(mTrayIsEnabled.isSelected() && !mOldState) {
        TVBrowser.loadTray();
      } else if(!mTrayIsEnabled.isSelected() && mOldState) {
        TVBrowser.removeTray();
      }
    }
    if (mMinimizeToTrayChb != null) {
      boolean checked = mMinimizeToTrayChb.isSelected() && mTrayIsEnabled.isSelected();
      Settings.Tray.MINIMIZE_TO.setBoolean(checked);
    }
    
    Settings.Tray.NOW_ON_RESTORE.setBoolean(mNowOnRestore.isSelected());
    Settings.Tray.ANTIALIASING.setBoolean(mTrayIsAnialiasing.isSelected());
    Settings.Tray.FILTER_NOT_MARKED.setBoolean(mNoMarkedFiltering.isSelected());
    Settings.Tray.FILTER_NOT.setBoolean(mNoFiltering.isSelected());
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("actions", "document-properties", 16);
  }

  public String getTitle() {
    return LOCALIZER.msg("title","Tray settings");
  }

  protected static boolean isTrayEnabled() {
    return mIsEnabled;
  }
}
