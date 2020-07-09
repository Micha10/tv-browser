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
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import simplemarkerplugin.MarkList;
import simplemarkerplugin.SimpleMarkerPlugin;
import simplemarkerplugin.WideComboBox;

/**
 * The cell editor for the event type column
 * 
 * @author René Mach
 */
public class MarkListEventTypeCellEditor extends AbstractCellEditor implements
    TableCellEditor {

  private static final long serialVersionUID = 1L;

  final static String[] EVENT_VALUES = {
      SimpleMarkerPlugin.getLocalizer().msg("settings.eventType.undefined","Undefined"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.eventType.added","Added"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.eventType.removed","Removed"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.eventType.all","Added and removed")
  };

  private WideComboBox<String> mComboBox;
  private MarkList mItem;
  
  /**
   * Creates an instance of this class.
   */
  public MarkListEventTypeCellEditor() {
    mComboBox = new WideComboBox<String>(EVENT_VALUES);
  }

  public boolean isCellEditable(EventObject evt) {
    return !(evt instanceof MouseEvent) || ((MouseEvent) evt).getClickCount() >= 2;
  }

  public Object getCellEditorValue() {
    switch(mComboBox.getSelectedIndex()) {
      case 1: mItem.setSupportedEventType(1);break;
      case 2: mItem.setSupportedEventType(2);break;
      case 3: mItem.setSupportedEventType(3);break;
      
      default: mItem.setSupportedEventType(0);
    }
    
    return mItem.getSupportedEventType();
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
    boolean isSelected, int row, int column) {
    mItem = (MarkList)table.getValueAt(table.getSelectedRow(),0);
    
    switch(mItem.getSupportedEventType()) {
      case 1: mComboBox.setSelectedIndex(1);break;
      case 2: mComboBox.setSelectedIndex(2);break;
      case 3: mComboBox.setSelectedIndex(3);break;
      
      default: mComboBox.setSelectedIndex(0);
    }
    
    return mComboBox;
  }
}
