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
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;
import tvbrowser.core.Settings;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.ui.EnhancedPanelBuilder;
import util.i18n.Localizer;
import util.i18n.PooledLocalizer;
import util.ui.MarkPriorityComboBoxRenderer;
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
  private JComboBox<Object> mDefaultColor;
  private JEditorPane mHelpLabel;
  private int mPriorityCount;
  private JPanel mHighlightings;
  
  public JPanel createSettingsPanel() {
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default:grow,default","pref,5dlu,pref,10dlu,pref,5dlu,pref,fill:10dlu:grow,default,default"));
    pb.border(Borders.DIALOG);
    
    JPanel defaultMarkings = new JPanel(new FormLayout("default, 5dlu, default",
    "default,2dlu,default,2dlu,default"));
    
    defaultMarkings.add(mProgramPanelUsesExtraSpaceForMarkIcons = new JCheckBox(LOCALIZER.msg("panel.extraSpace","Use additional space for the mark icons"), Settings.MarkingsProgramPanel.USES_EXTRA_SPACE_FOR_MARK_ICONS.getBoolean()), CC.xyw(1,1,3));
    defaultMarkings.add(mProgramItemWithMarkingsIsShowingBorder = new JCheckBox(LOCALIZER.msg("color.showBorder","Show border for highlighted programs"), Settings.MarkingsProgramPanel.WITH_MARKINGS_SHOWING_BORDER.getBoolean()), CC.xyw(1,3,3));
    defaultMarkings.add(new JLabel(LOCALIZER.msg("color.showColor","Highlight with color (default color):")), CC.xy(1,5));
    defaultMarkings.add(mDefaultColor = new JComboBox<>(), CC.xy(3,5));
    mDefaultColor.setRenderer(new MarkPriorityComboBoxRenderer(mDefaultColor.getRenderer()));
    
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
          
          mHighlightings.updateUI();
        }
      }
    };
    
    mDefaultColor.addItem(LOCALIZER.msg("color.noPriority","Don't highlight"));
    
    final int[] currentColors = Settings.MarkingsProgramPanel.HIGHLIGHTING_COLORS.getIntArray();
    final int[] currentDefaultColors = Settings.MarkingsProgramPanel.HIGHLIGHTING_COLORS.getDefault();
    
    for(mPriorityCount = 0; mPriorityCount < currentColors.length; mPriorityCount++) {
      Color defaultColor = new Color(currentDefaultColors[mPriorityCount < currentDefaultColors.length ? mPriorityCount : 0],true);
      mDefaultColor.addItem(mPriorityCount+1+". "+LOCALIZER.msg("color.colorPriority", "Color/priority"));
      
      mHighlightings.add(new HighlightPanel(mPriorityCount+1, new Color(currentColors[mPriorityCount],true), defaultColor, false, mPriorityCount >= 5 ? delete : null));
    }

    mDefaultColor.setSelectedIndex(Math.min(Settings.MarkingsProgramPanel.USED_DEFAULT_MARK_PRIORITY.getInt()+1,currentColors.length));
    
    JButton addColor = new JButton(LOCALIZER.msg("color.add","Add color/priority"));
    addColor.addActionListener(e -> {
      HighlightPanel lastPanel = (HighlightPanel)mHighlightings.getComponent(mHighlightings.getComponentCount()-1);
      lastPanel.setDeleteEnabled(false);
      
      mHighlightings.add(new HighlightPanel(mPriorityCount++ +1, new Color(currentDefaultColors[0],true), new Color(currentDefaultColors[0],true), true, delete));
      mHighlightings.updateUI();
    });
    
    HighlightPanel lastPanel = (HighlightPanel)mHighlightings.getComponent(mHighlightings.getComponentCount()-1);
    lastPanel.setDeleteEnabled(!lastPanel.isDefaultColor());
    
    pb.addSeparator(LOCALIZER.msg("color.programMarked","Highlighting by plugins"), CC.xyw(1,1,3));
    pb.add(defaultMarkings, CC.xyw(2,3,2));
    pb.addSeparator(LOCALIZER.msg("color.programMarkedAdditional","Additional colors (replacing default color)"), CC.xyw(1,5,3));
    pb.add(mHighlightings, CC.xyw(1,7,3));
    pb.add(addColor, CC.xy(3, 9));
    pb.add(mHelpLabel, CC.xyw(2,10,2));
    
    return pb.getPanel();
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return LOCALIZER.msg("title","Highlighting");
  }

  public void saveSettings() {
    Settings.MarkingsProgramPanel.USES_EXTRA_SPACE_FOR_MARK_ICONS.setBoolean(mProgramPanelUsesExtraSpaceForMarkIcons.isSelected());
    Settings.MarkingsProgramPanel.WITH_MARKINGS_SHOWING_BORDER.setBoolean(mProgramItemWithMarkingsIsShowingBorder.isSelected());
    Settings.MarkingsProgramPanel.USED_DEFAULT_MARK_PRIORITY.setInt(mDefaultColor.getSelectedIndex() - 1);
    
    int[] colors = new int[mHighlightings.getComponentCount()];
    
    for(int i = 0; i < colors.length; i++) {
      final HighlightPanel panel = (HighlightPanel)mHighlightings.getComponent(i);
      colors[i] = panel.getColor().getRGB();
    }
    
    Settings.MarkingsProgramPanel.HIGHLIGHTING_COLORS.setIntArray(colors);
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
      pb.addRow(true);
      pb.addLabel(mPriority+".", CC.xy(2, pb.getRow())).setHorizontalAlignment(JLabel.RIGHT);
      pb.addLabel(LOCALIZER.msg("color.colorPriority", "Color/priority"), CC.xy(4, pb.getRow()));
      pb.add(mColorLabel, CC.xy(6, pb.getRow()));
      mColorLabel.setStandardColor(mDefaultColor);
      pb.add(new ColorButton(mColorLabel), CC.xy(8, pb.getRow()));
      pb.add(mDelete, CC.xy(10, pb.getRow()));
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
