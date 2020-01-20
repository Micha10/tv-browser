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

import devplugin.Plugin;
import devplugin.ProgramFilter;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import util.ui.Localizer;

/**
 * A {@link JPanel} that let the user choose from a list of
 * {@link ProgramFilter} entries to filter program data.
 *
 * @author René Mach
 * @since 2.5
 */
@SuppressWarnings("nls")
public class FilterSelectionPanel extends JPanel {

  private static final long serialVersionUID = 7206178753809968986L;

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(FilterSelectionPanel.class);

  private final JComboBox<ProgramFilter> mFilterSelection;

  public FilterSelectionPanel() {
    final PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,10dlu,30dlu:grow",
        "pref,5dlu,pref,2dlu,pref,10dlu"), this);
    pb.addSeparator(mLocalizer.msg("filters", "Filters"), CC.xyw(1, 1, 4));
    pb.addLabel(mLocalizer.msg("toUseFilter", "Program filter to use:"), CC.xy(2, 3));

    mFilterSelection = new JComboBox<>(Plugin.getPluginManager().getFilterManager().getAvailableFilters());
    mFilterSelection.setSelectedItem(Plugin.getPluginManager().getFilterManager().getCurrentFilter());

    pb.add(mFilterSelection, CC.xy(4, 3));
  }

  public ProgramFilter getSelectedFilter() {
    return (ProgramFilter) mFilterSelection.getSelectedItem();
  }
}