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

package printplugin.dlgs.printdayprogramsdialog;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import printplugin.util.Utils;

import util.ui.Localizer;

@SuppressWarnings({"boxing", "nls"})
public class LayoutTab extends JPanel {

  private static final long serialVersionUID = 7646149339776767561L;

  /** The localizer for this class. */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(LayoutTab.class);

  private JComboBox<Integer> mChannelsPerPageCB;
  private JComboBox<LayoutOption> mLayoutCB;
  private DefaultComboBoxModel<LayoutOption> mLayoutCBModel;

  public LayoutTab() {
    CellConstraints cc = new CellConstraints();
    mLayoutCBModel = new DefaultComboBoxModel<>();

    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,10dlu,pref:grow",
        "pref,5dlu,pref,2dlu,pref,10dlu"), this);
    pb.border(Borders.DIALOG);

    pb.addSeparator(mLocalizer.msg("channelsAndColumns", "Channels and columns"), cc.xyw(1, 1, 4));
    pb.addLabel(mLocalizer.msg("channelsPerPage", "Channels per page") + ":", cc.xy(2, 3));
    pb.add(mChannelsPerPageCB = new JComboBox<>(Utils.createIntegerArray(2, 22)), cc.xy(4, 3));
    pb.addLabel(mLocalizer.msg("columnsPerPage", "columns") + ":", cc.xy(2, 5));
    pb.add(mLayoutCB = new JComboBox<>(mLayoutCBModel), cc.xy(4, 5));

    mChannelsPerPageCB.addItemListener(e -> {
      int val = (Integer) mChannelsPerPageCB.getSelectedItem();
      updateLayoutCombobox(val);
    });
  }

  public void setColumnLayout(int columnsPerPage, int channelsPerColumn) {
    int channelsPerPage = columnsPerPage * channelsPerColumn;
    mChannelsPerPageCB.setSelectedItem(channelsPerPage);
    for (int i = 0; i < mLayoutCBModel.getSize(); i++) {
      LayoutOption option = mLayoutCBModel.getElementAt(i);
      if (channelsPerColumn == option.getChannelsPerColumn()) {
        mLayoutCB.setSelectedItem(option);
        break;
      }
    }
  }

  public int getColumnsPerPage() {
    LayoutOption option = (LayoutOption) mLayoutCB.getSelectedItem();
    return option.getChannelsPerPage() / option.getChannelsPerColumn();
  }

  public int getChannelsPerColumn() {
    LayoutOption option = (LayoutOption) mLayoutCB.getSelectedItem();
    return option.getChannelsPerColumn();
  }

  private void updateLayoutCombobox(int val) {
    mLayoutCBModel.removeAllElements();
    int[] primes = Utils.getPrimes(val);
    for (int prime : primes) {
      mLayoutCBModel.addElement(new LayoutOption(val, prime));
    }
  }

  private static class LayoutOption {

    private int mChannelsPerPage, mChannelsPerColumn;

    public LayoutOption(int channelsPerPage, int channelsPerColumn) {
      mChannelsPerPage = channelsPerPage;
      mChannelsPerColumn = channelsPerColumn;
    }

    public int getChannelsPerColumn() {
      return mChannelsPerColumn;
    }

    public int getChannelsPerPage() {
      return mChannelsPerPage;
    }

    @Override
    public String toString() {
      int columns = mChannelsPerPage / mChannelsPerColumn;
      String s = mLocalizer.msg("layoutString",
          "{0} ({1} channels per column))", columns, mChannelsPerColumn);
      return s;
    }
  }
}