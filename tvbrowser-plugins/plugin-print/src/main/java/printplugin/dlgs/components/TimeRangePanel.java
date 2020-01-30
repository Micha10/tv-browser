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
 */

package printplugin.dlgs.components;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import printplugin.util.Utils;

import util.ui.Localizer;

/**
 * A {@link JPanel} that let the user define a time range.
 * The end date can overlap the day boundary (i. e. 23:00 - 6:00)
 *
 * @author Bananeweizen
 * @since 2010-06-28 19:33:48 +0200
 */
@SuppressWarnings({"boxing", "nls"})
public class TimeRangePanel extends JPanel {

  private static final long serialVersionUID = 2937608743469208025L;

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TimeRangePanel.class);

  private final JComboBox<Integer> mDayEndCb;
  private final JComboBox<Integer> mDayStartCb;

  /**
   * Creates a new instance with default values.
   */
  public TimeRangePanel() {

    final PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,10dlu,pref:grow",
        "pref,5dlu,pref,2dlu,pref,10dlu"), this);
    pb.addSeparator(mLocalizer.msg("dayBoundaries", "Day boundaries"), CC.xyw(1, 1, 4));
    pb.addLabel(mLocalizer.msg("startOfDay", "Start of day") + ":", CC.xy(2, 3));
    pb.add(mDayStartCb = new JComboBox<>(Utils.createIntegerArray(0, 23, 1)), CC.xy(4, 3));
    pb.addLabel(mLocalizer.msg("endOfDay", "End of day") + ":", CC.xy(2, 5));
    pb.add(mDayEndCb = new JComboBox<>(Utils.createIntegerArray(12, 36, 1)), CC.xy(4, 5));

    mDayStartCb.setRenderer(new TimeListCellRenderer());
    mDayEndCb.setRenderer(new TimeListCellRenderer());

    mDayStartCb.setSelectedItem(Integer.valueOf(6));
    mDayEndCb.setSelectedItem(Integer.valueOf(26));
  }

  public void setRange(final int from, final int to) {
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
    public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
        final boolean isSelected,
        boolean cellHasFocus) {

      final JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
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