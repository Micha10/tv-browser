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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import tvbrowser.core.Settings;
import util.ui.DefaultMarkingPrioritySelectionPanel.PriortiyLabel;

/**
 * A renderer class for the mark priority selection combo box.
 * 
 * @author René Mach
 * @since 2.6
 */
public class MarkPriorityComboBoxRenderer extends CustomComboBoxRenderer {
  private BackgroundPanel mPanel;
  
  public MarkPriorityComboBoxRenderer() {
    this(null);
  }
  
  public MarkPriorityComboBoxRenderer(ListCellRenderer<Object> backendRenderer) {
    super(backendRenderer);
    mPanel = new BackgroundPanel(new Color(0,0,0,0), false);
    mPanel.setBackground(Color.white);
  }
  
  public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c = getBackendRenderer() == null ? getSuperListCellRendererComponent(list,value,index,isSelected,cellHasFocus) : getBackendRenderer().getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
    
    if(!isSelected) {
      ((JLabel)c).setOpaque(false);
      
      Color color = null;
      
      if(value instanceof PriortiyLabel) {
        color = ((PriortiyLabel) value).getColor();
      }
      else {
        int colorIndex = index;
        
        if(index == -1) {
          colorIndex = list.getSelectedIndex();
        }
        
        if(list.getModel().getElementAt(0).equals(DefaultMarkingPrioritySelectionPanel.getNoPriorityColorName())) {
          colorIndex--;
        }
        
        color = Plugin.getPluginManager().getTvBrowserSettings().getColorForMarkingPriority(colorIndex);
      }
      
      if(color != null) {
        mPanel.setInternalBackground(color);
        mPanel.setSelected(index == -1);
        mPanel.removeAll();
        mPanel.add(c, new CellConstraints().xy(1,1));        
        c = mPanel;  
      }
    }
    else {
      ((JLabel)c).setOpaque(true);
    }
    
    return c;
  }
  
  private static final class BackgroundPanel extends JPanel {
    private Color mBackgroundColor;
    private boolean mRoundedCorners;
    private boolean mSelected;
    private static Border BORDER_EMPTY;
    private static Border BORDER_SELECTED;
    private int mArcWidth = 0;
    private int mArcHeight = 0;
    
    public BackgroundPanel(Color background, boolean isSelected) {
      super(new FormLayout("default:grow","fill:default:grow"));
      
      mBackgroundColor = background;
      mRoundedCorners = isSelected;
      
      if(UiUtilities.isGTKLookAndFeel() && BORDER_EMPTY == null) {
        BORDER_EMPTY = BorderFactory.createEmptyBorder();
        BORDER_SELECTED = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0), BorderFactory.createMatteBorder(1, 1, 1, 0, UiUtilities.getBorderColorForBackground(UIManager.getColor("Panel.background"))));
      }
      
      if(UiUtilities.isNimbusLookAndFeel()) {
        mArcWidth = 4;
        mArcHeight = 4;
      }
      else if(UiUtilities.isFlatLafLookAndFeel()) {
        mArcWidth = 20;
        mArcHeight = 20;
      }
    }
    
    public void setInternalBackground(Color background) {
      mBackgroundColor = background;
      setBackground(background);
    }
    
    public void setSelected(boolean selected) {
      mRoundedCorners = selected && ((UiUtilities.isFlatLafLookAndFeel() && Settings.LookAndFeel.FLATLAF_BUTTONS_ROUNDED.getBoolean()) 
          || UiUtilities.isNimbusLookAndFeel());
      mSelected = selected;
      
      if(selected) {
        setBorder(BORDER_SELECTED);
      }
      else {
        setBorder(BORDER_EMPTY);
      }
    }
    
    @Override
    public boolean isOpaque() {
      return true;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
      Color c = g.getColor();
      int widthH1 = getWidth()/2;
      int widthH2 = getWidth() - widthH1;
      
      int xOffset = 0;
      int yOffset = 0;
      int heightOffset = 0;
      
      if(mSelected && BORDER_EMPTY != null) {
        xOffset = 1;
        yOffset = 2;
        heightOffset = 2;
      }
      
      g.setColor(mBackgroundColor);
      
      if(mRoundedCorners) {
        g.setClip(0, 0, widthH1, getHeight());
        g.fillRoundRect(0, 0, widthH1+20, getHeight(), mArcWidth, mArcHeight);
      }
      else {
        widthH1 = 0;
        widthH2 = getWidth();
      }
      
      g.setClip(0, 0, getWidth(), getHeight());
      g.fillRect(widthH1+xOffset, 0+yOffset, widthH2, getHeight()-yOffset-heightOffset);
      g.setColor(c);
    }
  }
}
