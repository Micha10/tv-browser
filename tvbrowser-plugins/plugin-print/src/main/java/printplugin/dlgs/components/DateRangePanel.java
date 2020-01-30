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

import devplugin.Date;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import printplugin.util.Utils;

import util.ui.Localizer;

/**
 * {@link JPanel} that let the user define a date range (start date + n days).
 *
 * @author bananeweizen
 * @since 2010-06-28 19:33:48 +0200
 */
@SuppressWarnings({"boxing", "nls"})
public class DateRangePanel extends JPanel {

  private static final long serialVersionUID = 286497444395791988L;

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(DateRangePanel.class);

  private final JComboBox<Date> mDateCb;
  private final JSpinner mDayCountSpinner;

  public DateRangePanel() {
    final PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,2dlu,pref,2dlu,pref,2dlu,pref,2dlu,pref:grow",
        "pref,5dlu,pref,10dlu"), this);
    pb.addSeparator(mLocalizer.msg("period", "Period"), CC.xyw(1, 1, 10));
    pb.addLabel(mLocalizer.msg("from", "From"), CC.xy(2, 3));
    pb.add(mDateCb = new JComboBox<>(Utils.createDateObjects(21)), CC.xy(4, 3));
    pb.addLabel(mLocalizer.msg("TVlistingsFor", "Program for"), CC.xy(6, 3));
    pb.add(mDayCountSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 28, 1)), CC.xy(8, 3));
    pb.addLabel(mLocalizer.msg("days", "Days"), CC.xy(10, 3));
  }

  public void setFromDate(final Date date) {
    if (date != null) {
      mDateCb.setSelectedItem(date);
    }
  }

  public void setNumberOfDays(final int days) {
    mDayCountSpinner.setValue(days);
  }

  public Date getFromDate() {
    return (Date) mDateCb.getSelectedItem();
  }

  public int getNumberOfDays() {
    return (Integer) mDayCountSpinner.getValue();
  }
}