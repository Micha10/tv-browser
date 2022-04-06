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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;
import tvbrowser.core.Settings;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.i18n.Localizer;
import util.i18n.PooledLocalizer;
import util.ui.DefaultMarkingPrioritySelectionPanel;
import util.ui.EnhancedPanelBuilder;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

/**
 * A settings tab for the marking colors
 * 
 * @author René Mach
 * @since 2.5.1
 */
public class MarkingsSettingsTab implements SettingsTab {
  /** The localizer for this class */
  public static final Localizer LOCALIZER = PooledLocalizer.getLocalizerFor(MarkingsSettingsTab.class);

  private JCheckBox mProgramItemWithMarkingsIsShowingBorder, mProgramPanelUsesExtraSpaceForMarkIcons;
  private JEditorPane mHelpLabel;
  private int mPriorityCount;
  private JPanel mHighlightings;
  private DefaultMarkingPrioritySelectionPanel mDefaultColors;
  
  public JPanel createSettingsPanel() {
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder(new FormLayout("5dlu,default:grow,default"));
    pb.border(Borders.DIALOG);
    
    EnhancedPanelBuilder defaultMarkings = new EnhancedPanelBuilder(new FormLayout("default:grow"),"2dlu");
    
    defaultMarkings.addRow(false, mProgramPanelUsesExtraSpaceForMarkIcons = new JCheckBox(LOCALIZER.msg("panel.extraSpace","Use additional space for the mark icons"), Settings.Markings.USES_EXTRA_SPACE_FOR_MARK_ICONS.getBoolean()), 1);
    defaultMarkings.addRow(mProgramItemWithMarkingsIsShowingBorder = new JCheckBox(LOCALIZER.msg("color.showBorder","Show border for highlighted programs"), Settings.Markings.WITH_MARKINGS_SHOWING_BORDER.getBoolean()), 1);
    
    mDefaultColors = DefaultMarkingPrioritySelectionPanel.createPanel(new int[] {Settings.Markings.MARK_PRIORITY_DEFAULT.getInt(),Settings.Markings.MARK_PRIORITY_FILTERS.getInt()}, new String[] {LOCALIZER.msg("color.highlightedByPlugins","Highlighted by plugins:"),LOCALIZER.msg("color.highlightedByFilters","Highlighted by filters:")}, false, false, false);
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("color.help","The priority that a plugin uses for a program is used to decide which color have to be used for the marking. A higher priority color replaces a lower priority color. The setting for the default color is only for plugins that do not care about the priority. But it works like for plugins that uses the priorities, so if you select the highest priority color there, all marking of plugin which do not care about the priority will replace lower marking colors."), e -> {
    });
    
    mHighlightings = new JPanel();
    mHighlightings.setLayout(new BoxLayout(mHighlightings, BoxLayout.Y_AXIS));
    
    final ActionListener delete = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        HighlightPanel lastPanel = (HighlightPanel)mHighlightings.getComponent(mHighlightings.getComponentCount()-1);
        
        if(!lastPanel.isDefaultColor()) {
          mHighlightings.remove(lastPanel);
          mPriorityCount--;
        
          lastPanel = (HighlightPanel)mHighlightings.getComponent(mHighlightings.getComponentCount()-1);
          
          if(!lastPanel.isDefaultColor()) {
            lastPanel.setDeleteEnabled(true);
          }
          
          
          mHighlightings.revalidate();
        }
      }
    };
    
    final int[] currentColors = Settings.Markings.HIGHLIGHTING_COLORS.getIntArray();
    final int[] currentDefaultColors = Settings.Markings.HIGHLIGHTING_COLORS.getDefault();
    
    for(mPriorityCount = 0; mPriorityCount < currentColors.length; mPriorityCount++) {
      Color defaultColor = new Color(currentDefaultColors[mPriorityCount < currentDefaultColors.length ? mPriorityCount : 0],true);
      
      mHighlightings.add(new HighlightPanel(mPriorityCount+1, new Color(currentColors[mPriorityCount],true), defaultColor, false, mPriorityCount >= 5 ? delete : null));
    }

    JButton addColor = new JButton(LOCALIZER.msg("color.add","Add color/priority"));
    addColor.addActionListener(e -> {
      HighlightPanel lastPanel = (HighlightPanel)mHighlightings.getComponent(mHighlightings.getComponentCount()-1);
      lastPanel.setDeleteEnabled(false);
      
      mHighlightings.add(new HighlightPanel(mPriorityCount++ +1, new Color(currentDefaultColors[0],true), new Color(currentDefaultColors[0],true), true, delete));
      mHighlightings.updateUI();
    });
    
    HighlightPanel lastPanel = (HighlightPanel)mHighlightings.getComponent(mHighlightings.getComponentCount()-1);
    lastPanel.setDeleteEnabled(!lastPanel.isDefaultColor());
    
    pb.addSeparatorRowFull(false, LOCALIZER.msg("color.programMarked","Highlighting by plugins"));
    pb.addRowFull(defaultMarkings.getPanel(), 2);
    pb.addParagraph(LOCALIZER.msg("color.default","Default colors"));
    pb.addRowFull(mDefaultColors, 2);
    pb.addParagraph(LOCALIZER.msg("color.programMarkedAdditional","Additional colors (replacing default color)"));
    pb.addRowFull(mHighlightings);
    pb.addRow("fill:10dlu:grow,default", addColor, 3);
    pb.addRowFull(mHelpLabel, 2);
    
    return pb.getPanel();
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return LOCALIZER.msg("title","Highlighting");
  }

  public void saveSettings() {
    Settings.Markings.USES_EXTRA_SPACE_FOR_MARK_ICONS.setBoolean(mProgramPanelUsesExtraSpaceForMarkIcons.isSelected());
    Settings.Markings.WITH_MARKINGS_SHOWING_BORDER.setBoolean(mProgramItemWithMarkingsIsShowingBorder.isSelected());
    Settings.Markings.MARK_PRIORITY_DEFAULT.setInt(mDefaultColors.getSelectedPriority(0));
    Settings.Markings.MARK_PRIORITY_FILTERS.setInt(mDefaultColors.getSelectedPriority(1));
    
    int[] colors = new int[mHighlightings.getComponentCount()];
    
    for(int i = 0; i < colors.length; i++) {
      final HighlightPanel panel = (HighlightPanel)mHighlightings.getComponent(i);
      colors[i] = panel.getColor().getRGB();
    }
    
    Settings.Markings.HIGHLIGHTING_COLORS.setIntArray(colors);
  }
  
  private static final class HighlightPanel extends JPanel {
    private ColorLabel mColorLabel;
    private Color mDefaultColor;
    private int mPriority;
    private JButton mDelete;
    
    private HighlightPanel(final int priortiy, final Color color, final Color defaultColor, final boolean deleteEnabled, final ActionListener delete) {
      mPriority = priortiy;
      mColorLabel = new ColorLabel(color);
      mDefaultColor = defaultColor;
      mDelete = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
      mDelete.setToolTipText(LOCALIZER.msg("color.deleteTooltip","Delete color/priority"));
      mDelete.setEnabled(deleteEnabled);
      mDelete.setVisible(deleteEnabled);
      mDelete.addActionListener(delete);
      createGui();
    }
    
    private void createGui() {
      EnhancedPanelBuilder pb = new EnhancedPanelBuilder(new FormLayout("5dlu,10dlu,1dlu,default,5dlu,default,5dlu,default,5dlu:grow,default"),this);
      
      pb.addLabelRow(mPriority+".", 2).setHorizontalAlignment(JLabel.RIGHT);
      pb.labelAdd(LOCALIZER.msg("color.colorPriority", "Color/priority"), 4);
      pb.add(mColorLabel, 6);
      mColorLabel.setStandardColor(mDefaultColor);
      pb.add(new ColorButton(mColorLabel), 8);
      pb.add(mDelete, 10);
    }
    
    private void setDeleteEnabled(final boolean enabled) {
      mDelete.setEnabled(enabled);
      mDelete.setVisible(enabled);
    }
    
    private Color getColor() {
      return mColorLabel.getColor();
    }
    
    private boolean isDefaultColor() {
      return mPriority <= 5;
    }
  }
}
