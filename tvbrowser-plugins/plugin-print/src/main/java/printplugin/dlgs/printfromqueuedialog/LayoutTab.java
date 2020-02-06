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

package printplugin.dlgs.printfromqueuedialog;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import java.awt.Frame;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import printplugin.util.Utils;

import util.ui.Localizer;

@SuppressWarnings({"boxing", "nls"})
public class LayoutTab extends JPanel {

  private static final long serialVersionUID = 6656351212228826366L;

  /** The localizer for this class. */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(LayoutTab.class);

  private JComboBox<Integer> mColumnsPerPageCB;
  private ExtrasTab mExtrasTab;

  public LayoutTab(final Frame parent, final boolean includeExtras) {

    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,2dlu,pref,0dlu:grow",
        "pref,5dlu,pref,10dlu"), this);
    pb.border(Borders.DIALOG);

    pb.addSeparator(mLocalizer.msg("columns", "Columns"), CC.xyw(1, 1, 5));
    pb.addLabel(mLocalizer.msg("columnsPerPage", "Columns per page:"), CC.xy(2, 3));
    pb.add(mColumnsPerPageCB = new JComboBox<>(Utils.createIntegerArray(1, 4)), CC.xy(4, 3));

    if (includeExtras) {
      mExtrasTab = new ExtrasTab(parent, false);
      pb.getLayout().appendRow(RowSpec.decode("pref"));
      pb.getLayout().appendRow(RowSpec.decode("10dlu"));
      pb.add(mExtrasTab, CC.xyw(1, 5, 5));
    }
  }

  public int getColumnsPerPage() {
    return (Integer) mColumnsPerPageCB.getSelectedItem();
  }

  public void setColumnsPerPage(int columns) {
    mColumnsPerPageCB.setSelectedItem(columns);
  }

  public ExtrasTab extrasTab() {
    return mExtrasTab;
  }
}