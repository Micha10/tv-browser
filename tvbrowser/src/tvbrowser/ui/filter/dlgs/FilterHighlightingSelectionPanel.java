/*
 * TV-Browser
 * Copyright (C) 2021 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.filter.dlgs;

import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ProgramFilter;
import tvbrowser.core.Settings;
import util.ui.DefaultMarkingPrioritySelectionPanel;
import util.ui.DefaultMarkingPrioritySelectionPanel.State;

public class FilterHighlightingSelectionPanel extends JPanel {
  private DefaultMarkingPrioritySelectionPanel mFilterHighlight;
  private State mState;
  private int mPriorityInitial;
  
  public FilterHighlightingSelectionPanel(ProgramFilter filter) {
    mState = new DefaultMarkingPrioritySelectionPanel.State(DefaultMarkingPrioritySelectionPanel.TYPE_SELECTABLE, filter != null && Settings.Markings.HIGHLIGHTING_FILTERS.containsKey(filter.getName()));
    
    mPriorityInitial = Settings.Markings.MARK_PRIORITY_FILTERS.getInt();
    
    if(mState.isActivated()) {
      try {
        mPriorityInitial = Integer.parseInt(Settings.Markings.HIGHLIGHTING_FILTERS.getEntry(filter.getName()));
      }catch(NumberFormatException nfe) {}
    }
    
    mFilterHighlight = DefaultMarkingPrioritySelectionPanel.createPanel(mState, mPriorityInitial, EditFilterDlg.LOCALIZER.msg("highlight","Highlight all matching programs"), false, false, false, true, false);
    
    setOpaque(false);
    
    setLayout(new FormLayout("default:grow","default"));
    add(mFilterHighlight, CC.xy(1, 1));
  }
  
  public State getState() {
    return mState;
  }
  
  public int getSelectedPriority() {
    return mFilterHighlight.getSelectedPriority();
  }
  
  @Override
  public void setEnabled(boolean enabled) {
    mFilterHighlight.setEnabled(enabled);
  }
  
  public void save(ProgramFilter filter) {
    if(getState().isActivated()) {
      Settings.Markings.HIGHLIGHTING_FILTERS.putEntry(filter.getName(), mFilterHighlight.getSelectedPriority());
    }
    else {
      Settings.Markings.HIGHLIGHTING_FILTERS.removeEntry(filter.getName());
    }
  }
  
  public boolean wasChanged() {
    return mPriorityInitial != getSelectedPriority() || mState.wasChanged();
  }
}
