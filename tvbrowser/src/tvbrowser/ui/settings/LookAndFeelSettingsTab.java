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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings;

import java.awt.Component;
import java.io.File;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.CancelableSettingsTab;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.icontheme.IconTheme;
import tvbrowser.core.icontheme.InfoIconTheme;
import tvbrowser.core.icontheme.InfoThemeLoader;
import tvbrowser.core.icontheme.ThemeDownloadDlg;
import tvbrowser.core.icontheme.ThemeDownloadItem;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.looksSettings.JGoodiesLNFSettings;
import tvbrowser.ui.settings.looksSettings.SkinLNFSettings;
import util.i18n.Localizer;
import util.ui.CustomComboBoxRenderer;
import util.ui.LinkButton;
import util.ui.UiUtilities;
import util.ui.persona.Persona;
import util.ui.persona.PersonaInfo;

public final class LookAndFeelSettingsTab implements CancelableSettingsTab {

  public static final util.i18n.Localizer LOCALIZER = util.i18n.Localizer.getLocalizerFor(LookAndFeelSettingsTab.class);

  private JComboBox<LookAndFeelObj> mLfComboBox;

  private JPanel mSettingsPn;

  private JButton mConfigBtn;

  private JComboBox<Object> mIconThemes;

  private JComboBox<String> mPluginViewPosition;

  private JComboBox<String> mDateLayout;
  
  private JComboBox<Object> mPersonaSelection;
  
  private JComboBox<InfoIconTheme> mInfoIconThemes;
  
  private static int START_LOOK_AND_FEEL_INDEX = -1;
  private static int START_ICON_INDEX;
  private static int START_PLUGIN_VIEW_POSITION_INDEX;
  private static int START_INFO_ICON_THEME_INDEX;

  private static String JOODIES_START_THEME;
  private static boolean JGOODIES_START_SHADOW;

  private static String SKIN_LF_START_THEME;
  
  private static class LookAndFeelObj implements Comparable<LookAndFeelObj> {
    private UIManager.LookAndFeelInfo info;

    public LookAndFeelObj(UIManager.LookAndFeelInfo info) {
      this.info = info;
    }

    @Override
    public String toString() {
      return info.getName();
    }

    public String getLFClassName() {
      return info.getClassName();
    }

    public int compareTo(LookAndFeelObj other) {
      return this.toString().compareTo(other.toString());
    }
  }

  private LookAndFeelObj[] getLookAndFeelObjs() {
    UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
    LookAndFeelObj[] result = new LookAndFeelObj[info.length];
    for (int i = 0; i < info.length; i++) {
      result[i] = new LookAndFeelObj(info[i]);
    }

    return result;
  }
  
  public LookAndFeelSettingsTab() {}
  
  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout("5dlu, pref, 3dlu, fill:default:grow, 3dlu, pref, 5dlu", "");
    
    mSettingsPn = new JPanel(layout);
    mSettingsPn.setBorder(Borders.DIALOG);

    layout.appendRow(RowSpec.decode("pref"));
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(LOCALIZER.msg("lookAndFeel", "Look and Feel")), CC.xyw(1, 1, 7));

    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    mSettingsPn.add(new JLabel(LOCALIZER.msg("channelPosition", "Channel list position") +":"), CC.xy(2, 3));

    mPluginViewPosition = new JComboBox<>(new String[] {Localizer.getLocalization(Localizer.I18N_LEFT),Localizer.getLocalization(Localizer.I18N_RIGHT)});

    if(Settings.propPluginViewIsLeft.getBoolean()) {
      mPluginViewPosition.setSelectedIndex(1);
    }
    else {
      mPluginViewPosition.setSelectedIndex(0);
    }

    mPluginViewPosition.addActionListener(e -> {
      updateRestartMessage();
    });

    mSettingsPn.add(mPluginViewPosition, CC.xy(4,3));

    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    mSettingsPn.add(new JLabel(LOCALIZER.msg("dateFormat", "Layout of Datelist")+":"), CC.xy(2, 5));

    mDateLayout = new JComboBox<>(new String[] {
            LOCALIZER.msg("dateFormat.datelist", "List"),
            LOCALIZER.msg("dateFormat.calendarTable", "Calendar (Table)"),
            LOCALIZER.msg("dateFormat.calendarButtons", "Calendar (Buttons)")
    });

    mDateLayout.setSelectedIndex(Settings.propViewDateLayout.getInt());

    mDateLayout.addActionListener(e -> {
      updateRestartMessage();
    });

    mSettingsPn.add(mDateLayout, CC.xy(4,5));

    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    mSettingsPn.add(new JLabel(LOCALIZER.msg("theme", "Theme") +":"), CC.xy(2, 7));

    LookAndFeelObj[] lfObjects = getLookAndFeelObjs();
    Arrays.sort(lfObjects);
    mLfComboBox = new JComboBox<>(lfObjects);

    selectLookAndFeelFromSettings();

    mLfComboBox.addActionListener(e -> {
      lookChanged();
    });

    mSettingsPn.add(mLfComboBox, CC.xy(4, 7));

    mConfigBtn = new JButton(LOCALIZER.msg("config", "Config"));
    mConfigBtn.addActionListener(e -> {
      configTheme();
    });

    mSettingsPn.add(mConfigBtn, CC.xy(6, 7));
    
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    
    mSettingsPn.add(new JLabel(LOCALIZER.msg("persona", "Persona") + ":"), CC.xy(2, 9));
    
    PersonaInfo[] installedPersonas = Persona.getInstance().getInstalledPersonas();
    
    mPersonaSelection = new JComboBox<>(installedPersonas);
    
    final LinkButton personaDetails = new LinkButton(LOCALIZER.msg("personaDetails","Persona details"),
    "https://www.tvbrowser.org/");
    
    for(PersonaInfo info : installedPersonas) {
      if(Settings.propRandomPersona.getBoolean()) {
        if(PersonaInfo.isRandomPersona(info)) {
          mPersonaSelection.setSelectedItem(info);
          personaDetails.setUrl(info.getDetailURL());
          break;          
        }
      }
      else if(Settings.propSelectedPersona.getString().equals(info.getId())) {
        mPersonaSelection.setSelectedItem(info);
        personaDetails.setUrl(info.getDetailURL());
        break;
      }
    }
    
    mPersonaSelection.setRenderer(new CustomComboBoxRenderer(mPersonaSelection.getRenderer()) {
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel)getBackendRenderer().getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value != null) {
          label.setText(((PersonaInfo)value).getName());
          label.setToolTipText(((PersonaInfo)value).getDescription());
          
          if(((PersonaInfo)value).isSelectedPersona() && PersonaInfo.isRandomPersona((PersonaInfo)value)) {
            label.setText(label.getText() + ": " + Persona.getInstance().getPersonaInfo(Persona.getInstance().getId()).getName());
          }
        }
        return label;
      }
    });
    
    mPersonaSelection.addItemListener(e -> {
      personaDetails.setUrl(((PersonaInfo)mPersonaSelection.getSelectedItem()).getDetailURL());
    });
    
    mSettingsPn.add(mPersonaSelection, CC.xy(4,9));
    mSettingsPn.add(personaDetails, CC.xy(6,9));
    
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    mSettingsPn.add(new JLabel(LOCALIZER.msg("icons", "Icons") + ":"), CC.xy(2, 11));

    mIconThemes = new JComboBox<>();
    mIconThemes.setRenderer(new CustomComboBoxRenderer(mIconThemes.getRenderer()) {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel)getBackendRenderer().getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value != null) {
          label.setText(((IconTheme)value).getName());
          label.setToolTipText(((IconTheme)value).getComment());
        }
        return label;
      }
    });
    
    fillThemeBox();

    JButton downloadThemes = new JButton(LOCALIZER.msg("downloadMore", "Download more"));
    downloadThemes.addActionListener(e -> {
      downloadIcons(ThemeDownloadDlg.THEME_ICON_TYPE);
    });
    
    mSettingsPn.add(mIconThemes, CC.xy(4, 11));
    mSettingsPn.add(downloadThemes, CC.xy(6, 11));
    
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    
    mSettingsPn.add(new JLabel(LOCALIZER.msg("infoIcons", "Program info icons") + ":"), CC.xy(2, 13));
        
    mInfoIconThemes = new JComboBox<>();
    fillInfoThemeBox();
    
    JButton downloadInfoThemes = new JButton(LOCALIZER.msg("downloadMore", "Download more"));
    downloadInfoThemes.addActionListener(e -> {
      downloadIcons(ThemeDownloadDlg.INFO_ICON_TYPE);
    });
    
    mSettingsPn.add(mInfoIconThemes, CC.xy(4, 13));
    mSettingsPn.add(downloadInfoThemes, CC.xy(6, 13));
    
    layout.appendRow(RowSpec.decode("fill:3dlu:grow"));
    layout.appendRow(RowSpec.decode("pref"));
    
    if(START_LOOK_AND_FEEL_INDEX == -1) {
      START_LOOK_AND_FEEL_INDEX = mLfComboBox.getSelectedIndex();
      START_ICON_INDEX = mIconThemes.getSelectedIndex();
      START_PLUGIN_VIEW_POSITION_INDEX = mPluginViewPosition.getSelectedIndex();
      JOODIES_START_THEME = Settings.propJGoodiesTheme.getString();
      JGOODIES_START_SHADOW = Settings.propJGoodiesShadow.getBoolean();
      SKIN_LF_START_THEME = Settings.propSkinLFThemepack.getString();
      START_INFO_ICON_THEME_INDEX = mInfoIconThemes.getSelectedIndex();
    }

    mIconThemes.addActionListener(e -> {
      updateRestartMessage();
    });

    mInfoIconThemes.addActionListener(e -> {
      updateRestartMessage();
    });
    
    lookChanged();

    return mSettingsPn;
  }
  
  private void selectLookAndFeelFromSettings() {
    String lfName = Settings.propLookAndFeel.getString();
    
    for (int i = 0; i < mLfComboBox.getItemCount(); i++) {
      if (mLfComboBox.getItemAt(i).getLFClassName().equals(lfName)) {
        mLfComboBox.setSelectedIndex(i);
        break;
      }
    }
  }
  
  private void downloadIcons(int type) {
    if(JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), LOCALIZER.msg("downloadMessage", "To download more icons an Internet connection is needed.\nDo you want to load the list with the available icons now?"), Localizer.getLocalization(Localizer.I18N_INFO), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      ThemeDownloadDlg themeDlg = new ThemeDownloadDlg(UiUtilities.getLastModalChildOf(MainFrame.getInstance()),type);
      themeDlg.setVisible(true);
      
      if(themeDlg.downloadSuccess()) {
        if(type == ThemeDownloadDlg.THEME_ICON_TYPE) {
          fillThemeBox();
        }
        else {
          ThemeDownloadItem[] successItems = themeDlg.getSuccessItems();
          
          for(ThemeDownloadItem success : successItems) {
            File theme = new File(InfoThemeLoader.USER_ICON_DIR, success.toString());
            InfoThemeLoader.getInstance().addIconTheme(theme);
          }
          
          fillInfoThemeBox();
        }
      }
    }
  }
  
  private void fillInfoThemeBox() {
    InfoIconTheme[] infoIconThemes = InfoThemeLoader.getInstance().getAvailableInfoIconThemes();
    String currentInfoIconTheme = Settings.propInfoIconThemeID.getString();
    String startIconID = null;

    if(mInfoIconThemes.getSelectedIndex() != -1) {
      startIconID = ((InfoIconTheme)mInfoIconThemes.getItemAt(START_INFO_ICON_THEME_INDEX)).getID();
      currentInfoIconTheme = ((InfoIconTheme)mInfoIconThemes.getSelectedItem()).getID();
    }
    
    mInfoIconThemes.removeAllItems();
    
    for(int i = 0; i < infoIconThemes.length; i++) {
      mInfoIconThemes.addItem(infoIconThemes[i]);
      
      if(startIconID != null && startIconID.equals(infoIconThemes[i].getID())) {
        START_INFO_ICON_THEME_INDEX = i;
      }
      
      if(infoIconThemes[i].getID().equals(currentInfoIconTheme)) {
        mInfoIconThemes.setSelectedIndex(i);
      }
    }
  }
  
  private void fillThemeBox() {
    String startIconName = null;
    String selectedName = Settings.propIcontheme.getString();
    
    if(mIconThemes.getSelectedIndex() != -1) {
      startIconName = "icons/" + ((IconTheme)mIconThemes.getItemAt(START_ICON_INDEX)).getBase().getName();
      selectedName = "icons/" + ((IconTheme)mIconThemes.getSelectedItem()).getBase().getName();
    }
    
    mIconThemes.removeAllItems();
    
    IconTheme[] available = IconLoader.getInstance().getAvailableThemes();
    
    Arrays.sort(available);
    
    for(int i = 0; i < available.length; i++) {
      mIconThemes.addItem(available[i]);
      
      if(startIconName != null && ("icons/" + available[i].getBase().getName()).equals(startIconName)) {
        START_ICON_INDEX = i;
      }
    }
        
    if (selectedName != null) {
      IconTheme theme = IconLoader.getInstance().getIconTheme(IconLoader.getInstance().getIconThemeFile(selectedName));
      
      if (theme.loadTheme()) {
        mIconThemes.setSelectedItem(theme);
      } else {
        mIconThemes.setSelectedItem(IconLoader.getInstance().getDefaultTheme());
      }
    } else {
      mIconThemes.setSelectedItem(IconLoader.getInstance().getDefaultTheme());
    }
  }

  private void updateRestartMessage() {
    Settings.setRestartInfo(LocaleSettingsTab.class.getCanonicalName(), 
        mLfComboBox.getSelectedIndex() != START_LOOK_AND_FEEL_INDEX ||
        mIconThemes.getSelectedIndex() != START_ICON_INDEX ||
        JOODIES_START_THEME.compareTo(Settings.propJGoodiesTheme.getString()) != 0 ||
        JGOODIES_START_SHADOW != Settings.propJGoodiesShadow.getBoolean() ||
        SKIN_LF_START_THEME.compareTo(Settings.propSkinLFThemepack.getString()) != 0 ||
        mPluginViewPosition.getSelectedIndex() != START_PLUGIN_VIEW_POSITION_INDEX ||
        START_INFO_ICON_THEME_INDEX != mInfoIconThemes.getSelectedIndex());
  }

  void configTheme() {
    String classname = ((LookAndFeelObj)mLfComboBox.getSelectedItem()).getLFClassName();

    if (classname.startsWith("com.jgoodies")) {
      JGoodiesLNFSettings settings = new JGoodiesLNFSettings((JDialog) UiUtilities.getBestDialogParent(mSettingsPn));
      UiUtilities.centerAndShow(settings);
    } else if(classname.startsWith("com.l2fprod.gui.plaf.skin.SkinLookAndFeel")) {
      SkinLNFSettings settings = new SkinLNFSettings((JDialog) UiUtilities.getBestDialogParent(mSettingsPn));
      UiUtilities.centerAndShow(settings);
    }

    updateRestartMessage();
  }

  void lookChanged() {
    String classname = ((LookAndFeelObj)mLfComboBox.getSelectedItem()).getLFClassName();

    if ((classname.startsWith("com.jgoodies") || classname.startsWith("com.l2fprod")) && !classname.startsWith("com.jgoodies.looks.windows.WindowsLookAndFeel")) {
      mConfigBtn.setEnabled(true);
    } else {
      mConfigBtn.setEnabled(false);
    }

    updateRestartMessage();
  }

  public void saveSettings() {
    LookAndFeelObj obj = (LookAndFeelObj) mLfComboBox.getSelectedItem();
    Settings.propLookAndFeel.setString(obj.getLFClassName());

    IconTheme theme = (IconTheme) mIconThemes.getSelectedItem();
    Settings.propIcontheme.setString("icons/" + theme.getBase().getName());


    Settings.propPluginViewIsLeft.setBoolean(mPluginViewPosition.getSelectedIndex() == 1);
    Settings.propViewDateLayout.setInt(mDateLayout.getSelectedIndex());
    
    if(PersonaInfo.isRandomPersona(((PersonaInfo)mPersonaSelection.getSelectedItem()))) {
      Settings.propRandomPersona.setBoolean(true);
    }
    else {
      Settings.propRandomPersona.setBoolean(false);
      Settings.propSelectedPersona.setString(((PersonaInfo)mPersonaSelection.getSelectedItem()).getId());
    }
    
    Settings.propInfoIconThemeID.setString(((InfoIconTheme)mInfoIconThemes.getSelectedItem()).getID());
  }
  
  @Override
  public void cancel() {try {
    
    selectLookAndFeelFromSettings();
    
    String selectedName = Settings.propIcontheme.getString();
    
    if(selectedName != null) {
      IconTheme theme = IconLoader.getInstance().getIconTheme(IconLoader.getInstance().getIconThemeFile(selectedName));
      
      if (theme.loadTheme()) {
        mIconThemes.setSelectedItem(theme);
      } else {
        mIconThemes.setSelectedItem(IconLoader.getInstance().getDefaultTheme());
      }
    }
    
    String currentInfoIconTheme = Settings.propInfoIconThemeID.getString();
    
    for(int i = 0; i < mInfoIconThemes.getItemCount(); i++) {
      if(mInfoIconThemes.getItemAt(i).getID().contentEquals(currentInfoIconTheme)) {
        mInfoIconThemes.setSelectedIndex(i);
        break;
      }
    }
    
    Settings.setRestartInfo(LocaleSettingsTab.class.getCanonicalName(), 
        mLfComboBox.getSelectedIndex() != START_LOOK_AND_FEEL_INDEX ||
        mIconThemes.getSelectedIndex() != START_ICON_INDEX ||
        JOODIES_START_THEME.compareTo(Settings.propJGoodiesTheme.getString()) != 0 ||
        JGOODIES_START_SHADOW != Settings.propJGoodiesShadow.getBoolean() ||
        SKIN_LF_START_THEME.compareTo(Settings.propSkinLFThemepack.getString()) != 0 ||
        (Settings.propPluginViewIsLeft.getBoolean() ? 1 : 0) != START_PLUGIN_VIEW_POSITION_INDEX ||
        START_INFO_ICON_THEME_INDEX != mInfoIconThemes.getSelectedIndex());
  }catch(Throwable t) {t.printStackTrace();}
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("apps", "preferences-desktop-theme", 16);
  }

  public String getTitle() {
    return LOCALIZER.msg("graphical", "Graphical settings");
  }
}