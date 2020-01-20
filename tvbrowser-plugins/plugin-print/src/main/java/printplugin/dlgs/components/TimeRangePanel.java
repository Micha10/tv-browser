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
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */

package printplugin.dlgs.components;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import printplugin.util.Utils;

import util.ui.Localizer;

@SuppressWarnings({"boxing", "nls"})
public class TimeRangePanel extends JPanel {

  private static final long serialVersionUID = 2937608743469208025L;

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TimeRangePanel.class);

  private JComboBox<Integer> mDayEndCb;
  private JComboBox<Integer> mDayStartCb;

  public TimeRangePanel() {
    CellConstraints cc = new CellConstraints();

    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,10dlu,pref:grow",
        "pref,5dlu,pref,2dlu,pref,10dlu"), this);
    pb.addSeparator(mLocalizer.msg("dayBoundaries", "Day boundaries"), cc.xyw(1, 1, 4));
    pb.addLabel(mLocalizer.msg("startOfDay", "Start of day") + ":", cc.xy(2, 3));
    pb.add(mDayStartCb = new JComboBox<>(Utils.createIntegerArray(0, 23, 1)), cc.xy(4, 3));
    pb.addLabel(mLocalizer.msg("endOfDay", "End of day") + ":", cc.xy(2, 5));
    pb.add(mDayEndCb = new JComboBox<>(Utils.createIntegerArray(12, 36, 1)), cc.xy(4, 5));

    mDayStartCb.setRenderer(new TimeListCellRenderer());
    mDayEndCb.setRenderer(new TimeListCellRenderer());

    mDayStartCb.setSelectedItem(Integer.valueOf(6));
    mDayEndCb.setSelectedItem(Integer.valueOf(26));
  }

  public void setRange(int from, int to) {
    mDayStartCb.setSelectedItem(from);
    mDayEndCb.setSelectedItem(to);
  }

  public int getFromTime() {
    return (Integer) mDayStartCb.getSelectedItem();
  }

  public int getToTime() {
    return (Integer) mDayEndCb.getSelectedItem();
  }

  private static class TimeListCellRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = -7167571293439305573L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {

      JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
          index, isSelected, cellHasFocus);
      if (value instanceof Integer) {
        int val = (Integer) value;
        if (val < 24) {
          label.setText(val + ":00");
        } else {
          label.setText(val - 24 + ":00 (" + mLocalizer.msg("nextDay", "next day") + ")");
        }
      }
      return label;
    }
  }
}