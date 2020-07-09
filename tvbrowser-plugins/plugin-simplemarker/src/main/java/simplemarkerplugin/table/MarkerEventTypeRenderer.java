/*
 * SimpleMarkerPlugin by René Mach
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * SVN information:
 *     $Date: 2017-06-13 17:25:57 +0200 (Di, 13 Jun 2017) $
 *   $Author: ds10 $
 * $Revision: 8720 $
 */
package simplemarkerplugin.table;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import simplemarkerplugin.MarkList;

/**
 * The cell renderer for the importance column
 * 
 * @author René Mach
 */
public class MarkerEventTypeRenderer extends DefaultTableCellRenderer {

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(table, value,
        isSelected, hasFocus, row, column);
    ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
    ((JLabel)c).setText(MarkListEventTypeCellEditor.EVENT_VALUES[((MarkList)value).getSupportedEventType()]);
    
    return c;
  }
}
