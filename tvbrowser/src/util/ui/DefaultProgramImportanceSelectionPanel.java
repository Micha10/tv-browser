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
 *     $Date: 2008-02-26 20:55:40 +0100 (Di, 26 Feb 2008) $
 *   $Author: ds10 $
 * $Revision: 4312 $
 */
package util.ui;

import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;
import devplugin.SettingsItem;
import tvbrowser.ui.settings.SettingsDialog;
import util.i18n.Localizer;

/**
 * A class that is a panel that allows selection of the program importance.
 * 
 * @author René Mach
 * @since 3.0
 */
public class DefaultProgramImportanceSelectionPanel extends JPanel {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(DefaultProgramImportanceSelectionPanel.class);
  private JComboBox<String> mProgramImportanceSelection;
  private JEditorPane mHelpLabel;
  
  private DefaultProgramImportanceSelectionPanel(byte importance, boolean showTitle, boolean withDefaultDialogBorder) {
    
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder(new FormLayout("5dlu,default,5dlu,default,0dlu:grow"),this);
    
    if(withDefaultDialogBorder) {
      pb.border(Borders.DIALOG);
    }
    
    mProgramImportanceSelection = new JComboBox<>(getProgramImportanceNames(true));
    mProgramImportanceSelection.setSelectedIndex(getIndexForImportance(importance));
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("help","The selected importance is used to determinate the transparency of a program. It's calculated over all plugins as mean value. Lower importance leads to higher transparency. This works only if the plugins are allowed to set the transparency at <a href=\"#link\">program panel settings</a>."), e -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        SettingsDialog.getInstance().showSettingsTab(SettingsItem.PROGRAMPANELLOOK);
      }
    });
    
    if(showTitle) {
      pb.addSeparatorRowFull(false, getTitle());
      pb.addLineGap();
    }
    
    pb.addLabelRow(false, LOCALIZER.msg("color","Program importance:"), 2);
    pb.add(mProgramImportanceSelection, 4);
    pb.addRow("fill:0dlu:grow",false);
    pb.addRowFull("10dlu,default",mHelpLabel, 2);
  }
  
  /**
   * Creates an instance of this class.
   * 
   * @param importance The current selected importance.
   * @param showTitle If the title should be shown.
   * @param withDefaultDialogBorder If the panel should show the default dialog border of FormLayouts PanelBuilder.
   * @return The created instance of this class.
   */
  public static DefaultProgramImportanceSelectionPanel createPanel(byte importance, boolean showTitle, boolean withDefaultDialogBorder) {
    return new DefaultProgramImportanceSelectionPanel(importance, showTitle, withDefaultDialogBorder);
  }
  
  /**
   * Gets the selected program importance.
   * 
   * @return The selected marking priority.
   */
  public byte getSelectedImportance() {
    switch(mProgramImportanceSelection.getSelectedIndex()) {
      case 1: return Program.IMPORTANCE_PROGRAM_MIN;
      case 2: return Program.IMPORTANCE_PROGRAM_MEDIUM_LOWER;
      case 3: return Program.IMPORTANCE_PROGRAM_MEDIUM;
      case 4: return Program.IMPORTANCE_PROGRAM_MEDIUM_HIGHER;
      case 5: return Program.IMPORTANCE_PROGRAM_MAX;
      
      default: return Program.IMPORTANCE_PROGRAM_DEFAULT;
    }
  }
  
  private int getIndexForImportance(byte importance) {
    switch(importance) {
    case Program.IMPORTANCE_PROGRAM_MIN: return 1;
    case Program.IMPORTANCE_PROGRAM_MEDIUM_LOWER: return 2;
    case Program.IMPORTANCE_PROGRAM_MEDIUM: return 3;
    case Program.IMPORTANCE_PROGRAM_MEDIUM_HIGHER: return 4;
    case Program.IMPORTANCE_PROGRAM_MAX: return 5;
    
    default: return 0;
  }
  }
  
  /**
   * Gets the title of this settings panel.
   * 
   * @return The title of this settings panel.
   */
  public static String getTitle() {
    return LOCALIZER.msg("title","Program transparency");
  }
  
  /**
   * Gets the names of the importance values in an array sorted from the lowest to the highest importance.
   * <p>
   * @param withDefaultImportance If the array should contain the default importance name.
   * @return The names of the importance values in an array sorted from the lowest to the highest importance.
   */
  public static String[] getProgramImportanceNames(boolean withDefaultImportance) {
    if(withDefaultImportance) {
      return new String[] {LOCALIZER.msg("color.default","Default importance"),LOCALIZER.msg("color.min","Mininum importance"),LOCALIZER.msg("color.lowerMedium","Lower medium importance"),LOCALIZER.msg("color.medium","Medium importance"),LOCALIZER.msg("color.higherMedium","Higher medium importance"),LOCALIZER.msg("color.max","Maximum importance")};
    }
    else {
      return new String[] {LOCALIZER.msg("color.min","Mininum importance"),LOCALIZER.msg("color.lowerMedium","Lower medium importance"),LOCALIZER.msg("color.medium","Medium importance"),LOCALIZER.msg("color.higherMedium","Higher medium importance"),LOCALIZER.msg("color.max","Maximum importance")};
    }
  }
}
