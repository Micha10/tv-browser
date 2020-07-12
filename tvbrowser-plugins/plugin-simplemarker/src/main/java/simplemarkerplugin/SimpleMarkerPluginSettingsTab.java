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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package simplemarkerplugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import compat.ProgramListCompat;
import devplugin.Plugin;
import devplugin.SettingsItem;
import devplugin.SettingsTab;
import devplugin.Version;
import simplemarkerplugin.table.DeleteShowSelectionRenderer;
import simplemarkerplugin.table.MarkListEventTypeCellEditor;
import simplemarkerplugin.table.MarkListPriorityCellEditor;
import simplemarkerplugin.table.MarkListProgramImportanceCellEditor;
import simplemarkerplugin.table.MarkListSendToPluginCellEditor;
import simplemarkerplugin.table.MarkListTableModel;
import simplemarkerplugin.table.MarkerEventTypeRenderer;
import simplemarkerplugin.table.MarkerIDRenderer;
import simplemarkerplugin.table.MarkerIconRenderer;
import simplemarkerplugin.table.MarkerPriorityRenderer;
import simplemarkerplugin.table.MarkerProgramImportanceRenderer;
import simplemarkerplugin.table.MarkerSendToPluginRenderer;
import util.io.IOUtilities;
import util.ui.ExtensionFileFilter;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

/**
 * SimpleMarkerPlugin 1.4 Plugin for TV-Browser since version 2.3 to only mark
 * programs and add them to the Plugin tree.
 *
 * (Formerly known as Just_Mark ;-))
 *
 * The SettingsTab for the SimpleMarkerPlugin.
 *
 * @author René Mach
 *
 */
public class SimpleMarkerPluginSettingsTab implements SettingsTab,
    MouseListener, ActionListener, KeyListener {
  private static final Color COLOR_CUE_LINE = new Color(255,0,0,180);

  private JTable mListTable;
  private JButton mAdd, mDelete;
  private MarkListTableModel mModel;
  private JEditorPane mHelpLabel;
  private JCheckBox mShowDateSeparators;
  private ArrayList<MarkList> mMarkLists;
  private JCheckBox mShowInContextMenu;
  
  private Rectangle2D mCueLine = new Rectangle2D.Float();

  public JPanel createSettingsPanel() {try {
    final FormLayout layout = new FormLayout("5dlu,default:grow,5dlu",
        "default,3dlu,fill:default:grow,default,3dlu,pref,10dlu,pref,5dlu");
    
    final JPanel panel = new JPanel(layout);
    
    mShowDateSeparators = new JCheckBox(SimpleMarkerPlugin.getLocalizer().msg(
        "settings.showDateSeparator",
        "Show date separator in marked programs list"),
        SimpleMarkerPlugin.getInstance().getSettings().isShowingDateSeperators());
    
    int y = 1;
    
    if(ProgramListCompat.isDateSeparatorSupported()) {
      panel.add(mShowDateSeparators, CC.xy(2,y));
    }
    else {
      y = -1;
      layout.removeRow(1);
      layout.removeRow(1);
    }

    if(Plugin.getPluginManager().getTVBrowserVersion().compareTo(new Version(3,44,50,false)) >= 0) {
      layout.insertRow(3, RowSpec.decode("3dlu"));
      layout.insertRow(3, RowSpec.decode("default"));
      
      mShowInContextMenu = new JCheckBox(SimpleMarkerPlugin.getLocalizer().msg("showInContext", "For more than one list, show actions in submenu of TV-Browser context menu"), !SimpleMarkerPlugin.getInstance().getSettings().isShowingInContextMenu());
      
      y += 2;
      
      panel.add(mShowInContextMenu, CC.xy(2, y));
    }
    
    mMarkLists = new ArrayList<MarkList>();
    MarkList[] lists = SimpleMarkerPlugin.getInstance().getMarkLists();
    for (MarkList m:lists) {
      mMarkLists.add((MarkList)m.clone());
    }

    mModel = new MarkListTableModel(mMarkLists);

    mListTable = new JTable(mModel) {
      // Stupid hack to prevent selection change for drag events.
      @Override
      public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        StackTraceElement[] els = Thread.currentThread().getStackTrace();
        boolean drag = false;
        
        for(int i = 0; i < 5; i++) {
          if(els[i].getClassName().equals("javax.swing.plaf.basic.BasicTableUI$Handler") && els[i].getMethodName().contentEquals("mouseDragged")) {
            drag = true;
          }
        }
        
        if(!drag) {
          super.changeSelection(rowIndex, columnIndex, toggle, extend);
        }
      }
    };
    mListTable.getTableHeader().setReorderingAllowed(false);
    mListTable.getTableHeader().setResizingAllowed(false);
    mListTable.getTableHeader().setPreferredSize(new Dimension(50,50));
    
    mListTable.getColumnModel().getColumn(0).setCellRenderer(new MarkerIDRenderer());
    mListTable.getColumnModel().getColumn(0).setMinWidth(100);
    
    mListTable.getColumnModel().getColumn(1).setCellRenderer(new MarkerIconRenderer());
    mListTable.getColumnModel().getColumn(1).setMinWidth(setColumnWidth(mListTable.getColumnModel().getColumn(1),mModel.getColumnName(1)));

    mListTable.getColumnModel().getColumn(2).setCellRenderer(new MarkerPriorityRenderer());
    mListTable.getColumnModel().getColumn(2).setMinWidth(setColumnWidth(mListTable.getColumnModel().getColumn(2),mModel.getColumnName(2)));

    mListTable.getColumnModel().getColumn(3).setCellRenderer(new MarkerProgramImportanceRenderer());
    mListTable.getColumnModel().getColumn(3).setMinWidth(setColumnWidth(mListTable.getColumnModel().getColumn(3),mModel.getColumnName(3)));

    mListTable.getColumnModel().getColumn(4).setCellRenderer(new MarkerSendToPluginRenderer());
    mListTable.getColumnModel().getColumn(4).setMinWidth(setColumnWidth(mListTable.getColumnModel().getColumn(4),mModel.getColumnName(4)));

    mListTable.getColumnModel().getColumn(5).setCellRenderer(new DeleteShowSelectionRenderer());
    setColumnWidth(mListTable.getColumnModel().getColumn(5),mModel.getColumnName(5));
    mListTable.getColumnModel().getColumn(5).setMinWidth(30);
    
    if(SimpleMarkerPlugin.supportsEventTypes()) {
      mListTable.getColumnModel().getColumn(6).setCellRenderer(new MarkerEventTypeRenderer());
      mListTable.getColumnModel().getColumn(6).setMinWidth(setColumnWidth(mListTable.getColumnModel().getColumn(6),mModel.getColumnName(6)));
      mListTable.getColumnModel().getColumn(6).setCellEditor(new MarkListEventTypeCellEditor());
    }
    
    
    mListTable.setRowHeight(25);

    mListTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
          mDelete.setEnabled(mListTable.getSelectionModel().getMinSelectionIndex() > 0);
        }
      }
    });

    mListTable.addMouseListener(this);
    mListTable.addKeyListener(this);
    mListTable.getColumnModel().getColumn(2).setCellEditor(new MarkListPriorityCellEditor());
    mListTable.getColumnModel().getColumn(3).setCellEditor(new MarkListProgramImportanceCellEditor());
    mListTable.getColumnModel().getColumn(4).setCellEditor(new MarkListSendToPluginCellEditor());
    mListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    new DropTarget(mListTable, new DropTargetListener() {
      @Override
      public void dropActionChanged(DropTargetDragEvent dtde) {}
      
      @Override
      public void drop(DropTargetDropEvent dtde) {
        DataFlavor[] flavors = dtde.getCurrentDataFlavors();
        
        if(flavors != null && flavors.length == 1 && flavors[0].getHumanPresentableName() != null &&
            flavors[0].getHumanPresentableName().equals("tableRowMove")) {
          
          try {
            Object o = dtde.getTransferable().getTransferData(flavors[0]);
            
            if(o instanceof Integer) {
              int row = (Integer)o;
              int index = mListTable.rowAtPoint(dtde.getLocation());
              Rectangle rect = mListTable.getCellRect(index, 0, true);
              
              if(dtde.getLocation().y > (rect.y + rect.height*2/3)) {
                index++;
              }
              
              if(row != index) {
                ((DefaultTableModel)mListTable.getModel()).moveRow(row, row, index);
                
                if(row < index) {
                  index--;
                }
                
                mListTable.getSelectionModel().addSelectionInterval(index, index);
              }
              
              dtde.dropComplete(true);
            }
            else {
              dtde.dropComplete(false);
            }
          } catch (Exception e) {
            dtde.dropComplete(false);
          }
          
        }
        else {
          dtde.dropComplete(false);
        }
      }
      
      @Override
      public void dragOver(DropTargetDragEvent dtde) {
        if(dtde.getCurrentDataFlavors() != null && dtde.getCurrentDataFlavors()[0].getHumanPresentableName() != null && dtde.getCurrentDataFlavors()[0].getHumanPresentableName().contentEquals("tableRowMove")) {
          int index = mListTable.rowAtPoint(dtde.getLocation());
          Rectangle rect = mListTable.getCellRect(index, 0, true);
          
          if(dtde.getLocation().y > (rect.y + rect.height*2/3)) {
            rect.y += rect.height;
            
            if(index == mListTable.getRowCount()-1) {
              rect.y--;
            }
          }
          else if(index == 0) {
            rect.y++;
          }
          
          mListTable.paintImmediately(mCueLine.getBounds());
          mCueLine.setRect(0,rect.y-1,mListTable.getWidth(),2);
          
          Graphics2D g2 = (Graphics2D) mListTable.getGraphics();
          g2.setColor(COLOR_CUE_LINE);
          g2.fill(mCueLine);
          dtde.acceptDrag(dtde.getDropAction());
        }
        else {
          mListTable.paintImmediately(mCueLine.getBounds());
          dtde.rejectDrag();
        }
      }
      
      @Override
      public void dragExit(DropTargetEvent dte) {
        mListTable.paintImmediately(mCueLine.getBounds());
      }
      
      @Override
      public void dragEnter(DropTargetDragEvent dtde) {
        if(dtde.getCurrentDataFlavors() != null && dtde.getCurrentDataFlavors()[0].getHumanPresentableName() != null && dtde.getCurrentDataFlavors()[0].getHumanPresentableName().contentEquals("tableRowMove")) {
          dtde.acceptDrag(dtde.getDropAction());
        }
        else {
          dtde.rejectDrag();
        }
      }
    });
    
    DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(mListTable, DnDConstants.ACTION_MOVE, new DragGestureListener() {
        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
          dge.startDrag(null,new Transferable() {
            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
              return flavor.getHumanPresentableName() != null && flavor.getHumanPresentableName().equals("tableRowMove");
            }
            
            @Override
            public DataFlavor[] getTransferDataFlavors() {
              return new DataFlavor[] {new DataFlavor(JTable.class, "tableRowMove")};
            }
            
            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
              return isDataFlavorSupported(flavor) ? mListTable.getSelectedRow() : null;
            }
          });
        }
    });
    
    JScrollPane pane = new JScrollPane(mListTable);
    pane.setPreferredSize(new Dimension(200,150));
    pane.getViewport().setBackground(UIManager.getColor("List.background"));
    
    y += 2;
    
    panel.add(pane, CC.xy(2, y));
    
    y++;
    
    panel.add(UiUtilities.createHtmlHelpTextArea(SimpleMarkerPlugin.getLocalizer().msg("settings.informAboutDeletedPrograms","¹ Inform about programs of that list that were deleted during a data update")+(SimpleMarkerPlugin.supportsEventTypes() ? "<br>"+SimpleMarkerPlugin.getLocalizer().msg("settings.eventTypeHelp","² Which type of received programs are processed by the list") : "")), CC.xy(2, y));

    JPanel south = new JPanel();
    south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));
    south.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

    mAdd = new JButton(SimpleMarkerPlugin.getLocalizer().msg("settings.add",
        "Add new list"));
    mAdd.setIcon(TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    mAdd.addActionListener(this);

    mDelete = new JButton(SimpleMarkerPlugin.getLocalizer().msg("settings.delete",
        "Delete selected list"));
    mDelete.setIcon(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mDelete.setEnabled(false);
    mDelete.addActionListener(this);

    south.add(mAdd);
    south.add(Box.createHorizontalGlue());
    south.add(mDelete);

    y += 2;
    
    panel.add(south, CC.xy(2, y));
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(SimpleMarkerPlugin.getLocalizer().msg("settings.prioHelp","The mark priority is used for selecting the marking color. The marking colors of the priorities can be change in the <a href=\"#link\">program panel settings</a>. If a program is marked by more than one plugin/list the color with the highest priority given by the marking plugins/lists is used."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          if(e.getDescription() != null) {
            if(e.getDescription().equals("#link1")) {
              Plugin.getPluginManager().showSettings(SettingsItem.PROGRAMPANELMARKING);
            }
            else if(e.getDescription().equals("#link2")) {
              Plugin.getPluginManager().showSettings(SettingsItem.PROGRAMPANELLOOK);
            }
          }
        }
      }
    });

    y += 2;
    
    panel.add(mHelpLabel, CC.xy(2,y));


    JPanel p = new JPanel(new FormLayout("450dlu:grow","5dlu,fill:default:grow"));
    p.add(panel, CC.xy(1,2));

    return p;
  }catch(Throwable t) {
    t.printStackTrace();
  }
  
  return new JPanel();
  }
  
  private int setColumnWidth(TableColumn column, String name) {
    int columnWidth = 0;
    
    String[] nameParts = name.replace("<html>", "").split("<br>");
    
    for(String part : nameParts) {
      columnWidth = Math.max(columnWidth,UiUtilities.getStringWidth(mListTable.getFont(),part) + 10);
    }
    
    column.setMaxWidth(columnWidth);
    column.setPreferredWidth(columnWidth);
    
    return columnWidth;
  }

  public void saveSettings() {
    SimpleMarkerPlugin.getInstance().getSettings().setShowingDateSeperators(
        mShowDateSeparators.isSelected());

    if (mListTable.isEditing()) {
      mListTable.getCellEditor().stopCellEditing();
    }

    if(mShowInContextMenu != null) {
      SimpleMarkerPlugin.getInstance().getSettings().setShowingInContextMenu(!mShowInContextMenu.isSelected());
    }
    
    SimpleMarkerPlugin.getInstance().setMarkLists(mMarkLists.toArray(new MarkList[mMarkLists.size()]));
    SimpleMarkerPlugin.getInstance().save(true);
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return null;
  }

  public void mouseClicked(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      final int column = mListTable.columnAtPoint(e.getPoint());
      
      if(column == 1 && e.getClickCount() >= 2) {
        chooseIcon(mListTable.rowAtPoint(e.getPoint()));
      }
      else if(column == 5) {
        final int row = mListTable.rowAtPoint(e.getPoint());
        
        mListTable.getModel().setValueAt(!((MarkList)mListTable.getValueAt(row,column)).isShowingDeletedPrograms(),row,5);
        mListTable.repaint();
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (mListTable.isEditing()) {
      mListTable.getCellEditor().cancelCellEditing();
    }

    if (e.getSource() == mAdd) {
      int n = mListTable.getRowCount() + 1;

      String name = SimpleMarkerPlugin.getLocalizer().msg("settings.listName","List {0}", n);

      for(int i = 0; i < mListTable.getRowCount(); i++) {
        if (name.equals(mListTable.getValueAt(i, 0).toString())) {
          name = SimpleMarkerPlugin.getLocalizer().msg("settings.listName","List {0}", ++n);
          i = -1;
        }
      }

      MarkList list = new MarkList(name, SimpleMarkerPlugin.getAndIncrementActionIdCount());
      mModel.addRow(list);
      mListTable.setRowSelectionInterval(mListTable.getRowCount()-1,mListTable.getRowCount()-1);
    }
    if (e.getActionCommand().equals(SimpleMarkerPlugin.getLocalizer().msg("settings.delete",
    "Delete selected list"))) {
      deleteSelectedRows();
    }
  }

  private void deleteSelectedRows() {
    int selectedIndex = mListTable.getSelectedRow();
    int[] rows = mListTable.getSelectedRows();
    for (int i = rows.length - 1; i >= 0; i--) {
      mModel.removeRow(rows[i]);
    }
    if ((selectedIndex > 0) && (selectedIndex<mListTable.getRowCount())) {
      mListTable.setRowSelectionInterval(selectedIndex,selectedIndex);
    }

    mDelete.setEnabled(mListTable.getSelectedRowCount() > 0);
  }

  public void keyPressed(KeyEvent e) {
    mListTable.getRootPane().dispatchEvent(e);

    if (mListTable.getSelectionModel().getMinSelectionIndex() > 0) {
      mDelete.setEnabled(true);
    }

    if(e.getKeyCode() == KeyEvent.VK_DELETE) {
      deleteSelectedRows();
      e.consume();
    }
    else if(e.getKeyCode() == KeyEvent.VK_F2 && mListTable.getSelectedColumn() == 1) {
      chooseIcon(mListTable.getSelectedRow());
    }
    else if(e.getKeyCode() == KeyEvent.VK_F2 || e.getKeyCode() == KeyEvent.VK_SPACE) {
      final int row = mListTable.getSelectedRow();
      
      if(mListTable.getSelectedColumn() == 5) {
        mListTable.getModel().setValueAt(!((MarkList)mListTable.getValueAt(mListTable.getSelectedRow(),5)).isShowingDeletedPrograms(),row,5);
        mListTable.repaint();
      }
    }
  }

  public void keyReleased(KeyEvent e) {}

  public void keyTyped(KeyEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {
    if(e.isPopupTrigger() && mListTable.rowAtPoint(e.getPoint()) > -1) {
      int row = mListTable.rowAtPoint(e.getPoint());
      mListTable.setRowSelectionInterval(row,row);
      showPopupMenu(e.getPoint(), row);
    }
  }

  public void mouseReleased(MouseEvent e) {
    if(e.isPopupTrigger() && mListTable.rowAtPoint(e.getPoint()) > -1) {
      int row = mListTable.rowAtPoint(e.getPoint());
      mListTable.setRowSelectionInterval(row,row);
      showPopupMenu(e.getPoint(), row);
    }
  }

  private void showPopupMenu(Point p, final int row) {
    JPopupMenu popupMenu = new JPopupMenu();

    JMenuItem item = new JMenuItem(SimpleMarkerPlugin.getLocalizer().msg("settings.delete",
    "Delete selected list"));
    item.setIcon(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    item.addActionListener(this);

    if(mListTable.getSelectionModel().getMinSelectionIndex() > 0) {
      popupMenu.add(item);
    }

    item = new JMenuItem(SimpleMarkerPlugin.getLocalizer().msg("settings.changeIcon",
    "Change list icon"));
    item.setIcon(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        chooseIcon(row);
      }
    });

    popupMenu.add(item);

    popupMenu.show(mListTable, p.x, p.y);
  }

  private void chooseIcon(int row) {
    MarkList markList = (MarkList) mListTable.getValueAt(row, 0);

    String iconPath = markList.getMarkIconPath();

    JFileChooser chooser = new JFileChooser(iconPath == null ? new File("")
        : (new File(iconPath)).getParentFile());
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    String msg = SimpleMarkerPlugin.getLocalizer().msg("iconFiles",
        "Icon Files ({0})", "*.png,*.jpg, *.gif");
    String[] extArr = { ".png", ".jpg", ".gif" };

    chooser.setFileFilter(new ExtensionFileFilter(extArr, msg));
    chooser.setDialogTitle(SimpleMarkerPlugin.getLocalizer().msg("chooseIcon",
        "Choose icon for '{0}'", markList.getName()));

    Window w = UiUtilities.getLastModalChildOf(SimpleMarkerPlugin
        .getInstance().getSuperFrame());

    if (chooser.showDialog(w, Localizer.getLocalization(Localizer.I18N_SELECT)) == JFileChooser.APPROVE_OPTION) {
      if (chooser.getSelectedFile() != null) {
        File dir = new File(Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome(),"simplemarkericons");

        if(!dir.isDirectory()) {
          dir.mkdir();
        }

        String ext =  chooser.getSelectedFile().getName();
        ext = ext.substring(ext.lastIndexOf('.'));

        Icon icon = SimpleMarkerPlugin.getInstance().getIconForFileName(
            chooser.getSelectedFile().getAbsolutePath());

        if (icon.getIconWidth() != 16 || icon.getIconHeight() != 16) {
          JOptionPane.showMessageDialog(w, SimpleMarkerPlugin.getLocalizer().msg(
              "iconSize", "The icon has to be 16x16 in size."));
          return;
        }

        if(!new File(dir, mListTable.getValueAt(row, 0).toString() + ext).equals(chooser.getSelectedFile())) {
          try {
            IOUtilities.copy(chooser.getSelectedFile(),new File(dir,markList.getName() + ext));
          } catch (IOException e1) {
            e1.printStackTrace();
          }
        }

        SimpleMarkerPlugin.getInstance().getIconForFileName(dir + "/" + markList.getName() + ext);
        mListTable.setValueAt(markList.getName() + ext, row, 1);
      }
    }
  }
}
