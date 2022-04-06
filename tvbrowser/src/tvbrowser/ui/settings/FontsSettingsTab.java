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
package tvbrowser.ui.settings;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import util.ui.EnhancedPanelBuilder;
import util.ui.FontChooserPanel;

public class FontsSettingsTab implements devplugin.SettingsTab {

  /** The localizer for this class. */
  private static final util.i18n.Localizer LOCALIZER = util.i18n.Localizer.getLocalizerFor(FontsSettingsTab.class);

  private JCheckBox mUseUserDefindedFontsCB;

  private JCheckBox mEnableAntialiasingCB;

  private FontChooserPanel mTitleFontPanel, mInfoFontPanel, mChannelNameFontPanel, mTimeFontPanel;

  private JLabel mTimeFontLabel;

  private JLabel mChannelNameFontLabel;

  private JLabel mInfoFontLabel;

  private JLabel mTitleFontLabel;
  
  private JComboBox<String> mTextLineGap;

  public JPanel createSettingsPanel() {
    EnhancedPanelBuilder mainPanel = new EnhancedPanelBuilder(new FormLayout("5dlu, 10dlu, default, 3dlu, default, fill:3dlu:grow"));
    mainPanel.border(Borders.DIALOG);
    
    mainPanel.addSeparatorRowFull(false, LOCALIZER.msg("Fonts", "Fonts"));
    
    mEnableAntialiasingCB = new JCheckBox(LOCALIZER.msg("EnableAntialiasing", "Enable antialiasing"));
    mEnableAntialiasingCB.setSelected(Settings.Fonts.ANTIALIASING_ENABLED.getBoolean());

    mainPanel.addRowFull(mEnableAntialiasingCB, 2);

    mUseUserDefindedFontsCB = new JCheckBox(LOCALIZER.msg("UserDefinedFonts", "Use userdefined fonts"));
    mUseUserDefindedFontsCB.setSelected(!Settings.Fonts.USE_DEFAULT.getBoolean());
    
    mainPanel.addRowFull(false, mUseUserDefindedFontsCB, 2);
    
    mChannelNameFontLabel = mainPanel.addLabelRow(LOCALIZER.msg("ChannelNames", "Channel name"), 3);
    mainPanel.add(mChannelNameFontPanel = new FontChooserPanel(Settings.Fonts.CHANNEL_NAME.getFont()), 5);
    mTitleFontLabel = mainPanel.addLabelRow("13dlu, default", LOCALIZER.msg("ProgramTitle", "Program title"), 3);
    mainPanel.add(mTitleFontPanel = new FontChooserPanel(Settings.Fonts.PROGRAM_TITLE.getFont()), 5);
    
    mInfoFontLabel = mainPanel.addLabelRow("3dlu, default", LOCALIZER.msg("ProgramInfo", "Program information"), 3);
    mainPanel.add(mInfoFontPanel = new FontChooserPanel(Settings.Fonts.PROGRAM_INFO.getFont()), 5);
    
    mTimeFontLabel = mainPanel.addLabelRow("3dlu, default", LOCALIZER.msg("Time", "Time"), 3);
    mainPanel.add(mTimeFontPanel = new FontChooserPanel(Settings.Fonts.PROGRAM_TIME.getFont()), 5);
    
    mainPanel.addLabelRow("10dlu, default", LOCALIZER.msg("lineSpacing", "Line spacing:"), 2, 2);
    mTextLineGap = new JComboBox<>(new String[] {
        LOCALIZER.msg("lineSpacing.singleLine", "Single line"),
        LOCALIZER.msg("lineSpacing.oneAndAQuaterLine", "1.25 line"),
        LOCALIZER.msg("lineSpacing.oneAndAHalfLine", "1.5 line"),
        LOCALIZER.msg("lineSpacing.oneAndThreeQuaterLine", "1.75 line"),
        LOCALIZER.msg("lineSpacing.doubleLine", "Double line")
        });
    mTextLineGap.setSelectedIndex(Settings.Fonts.PROGRAM_TEX_TLINE_GAP.getInt());
    mainPanel.add(mTextLineGap, 5);
    
    mUseUserDefindedFontsCB.addActionListener(e -> {
      enableFontFields(mUseUserDefindedFontsCB.isSelected());
    });
    
    enableFontFields(mUseUserDefindedFontsCB.isSelected());
    return mainPanel.getPanel();
  }

  private void enableFontFields(boolean enable) {
    mTitleFontLabel.setEnabled(enable);
    mTitleFontPanel.setEnabled(enable);
    mInfoFontLabel.setEnabled(enable);
    mInfoFontPanel.setEnabled(enable);
    mChannelNameFontLabel.setEnabled(enable);
    mChannelNameFontPanel.setEnabled(enable);
    mTimeFontLabel.setEnabled(enable);
    mTimeFontPanel.setEnabled(enable);
  }
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Settings.Fonts.PROGRAM_TITLE.setFont(mTitleFontPanel.getChosenFont());
    Settings.Fonts.PROGRAM_INFO.setFont(mInfoFontPanel.getChosenFont());
    Settings.Fonts.CHANNEL_NAME.setFont(mChannelNameFontPanel.getChosenFont());
    Settings.Fonts.PROGRAM_TIME.setFont(mTimeFontPanel.getChosenFont());
    Settings.Fonts.USE_DEFAULT.setBoolean(!mUseUserDefindedFontsCB.isSelected());
    Settings.Fonts.ANTIALIASING_ENABLED.setBoolean(mEnableAntialiasingCB.isSelected());
    Settings.Fonts.PROGRAM_TEX_TLINE_GAP.setInt(mTextLineGap.getSelectedIndex());
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("apps", "preferences-desktop-font", 16);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return LOCALIZER.msg("Fonts", "Fonts");
  }

}