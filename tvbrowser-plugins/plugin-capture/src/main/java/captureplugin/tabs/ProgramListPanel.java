/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *     $Date: 2011-04-25 09:04:52 +0200 (Mo, 25 Apr 2011) $
 *   $Author: bananeweizen $
 * $Revision: 6996 $
 */
package captureplugin.tabs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import captureplugin.CapturePlugin;
import captureplugin.CapturePluginData;
import captureplugin.drivers.DeviceIf;
import captureplugin.utils.ProgramTimeComparator;
import devplugin.Program;
import util.programmouseevent.ProgramMouseAndContextMenuListener;
import util.programmouseevent.ProgramMouseEventHandler;
import util.settings.PluginPictureSettings;
import util.ui.Localizer;
import util.ui.ProgramTableCellRenderer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

/**
 * Panel with List of Recordings
 *
 * @author bodum
 */
public class ProgramListPanel extends JPanel implements ProgramMouseAndContextMenuListener {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramListPanel.class);

    /** Config **/
    private CapturePluginData mData;

    /** JList for Programs */
    private JTable mProgramTable;
    /** List of Programs */
    private DeviceTableModel mProgramTableModel;

    private Window mParent;

    /**
     * Creates the Panel
     * @param parent Parent-Frame
     * @param data Configuration
     */
    public ProgramListPanel(Window parent, CapturePluginData data) {
        mParent =parent;
        mData = data;
        mProgramTableModel = new DeviceTableModel();
        createListData();
        createPanel();
    }

    /**
     * Creates the Data for the List
     */
    private void createListData() {
        mProgramTableModel.clearTable();

        for (DeviceIf dev : mData.getDevices()) {
            Program[] prgList = dev.getProgramList();

            if (prgList != null) {
                Arrays.sort(prgList, new ProgramTimeComparator());
                for (Program program : prgList) {
                    mProgramTableModel.addProgram(dev, program);
                }
            }
        }

    }

    /**
     * Creates the GUI
     */
    void createPanel() {
      removeAll();
        setLayout(new BorderLayout());

        mProgramTable = new JTable(mProgramTableModel);
        mProgramTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mProgramTable.getColumnModel().getColumn(0).setCellRenderer(new DeviceTableCellRenderer());
        mProgramTable.getColumnModel().getColumn(0).setPreferredWidth(mData.getWidthProgramTableColum1());
        mProgramTable.getColumnModel().getColumn(1).setCellRenderer(new ProgramTableCellRenderer(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE)));
        mProgramTable.getColumnModel().getColumn(1).setPreferredWidth(mData.getWidthProgramTableColum2());
        mProgramTable.getTableHeader().addMouseListener(new MouseAdapter() {
          @Override
          public void mouseReleased(MouseEvent e) {
            if(mProgramTable.getColumnCount() == 2) {
              mData.setWidthProgramTableColum1(mProgramTable.getColumnModel().getColumn(0).getWidth());
              mData.setWidthProgramTableColum2(mProgramTable.getColumnModel().getColumn(1).getWidth());
              CapturePlugin.getInstance().save();
            }
          }
        });
        
        if (CapturePlugin.getInstance().getCapturePluginData().getDevices().size() < 2) {
          mProgramTable.getColumnModel().removeColumn(mProgramTable.getColumnModel().getColumn(0));
        }

        mProgramTable.addMouseListener(new ProgramMouseEventHandler(this, CapturePlugin.getInstance()));

        JScrollPane scroll = new JScrollPane(mProgramTable);

        add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton delete = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deletePressed();
            }

        });

        btnPanel.add(delete);

        add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * Shows the Popup
     * @param e Mouse-Event
     */
    private void showPopup(MouseEvent e) {
      int row = mProgramTable.rowAtPoint(e.getPoint());

      mProgramTable.changeSelection(row, 0, false, false);

      Program p = (Program) mProgramTableModel.getValueAt(row, 1);

      JPopupMenu menu = devplugin.Plugin.getPluginManager().createPluginContextMenu(p, CapturePlugin.getInstance());
      menu.show(mProgramTable, e.getX() - 15, e.getY() - 15);
    }

    /**
     * Delete was pressed
     */
    private void deletePressed() {
       int row = mProgramTable.getSelectedRow();

       if ((row > mProgramTableModel.getRowCount()) || (row < 0)) {
           return;
       }

       DeviceIf dev = (DeviceIf) mProgramTableModel.getValueAt(row, 0);
       Program prg = (Program) mProgramTableModel.getValueAt(row, 1);

       int ret = JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(mParent),
               mLocalizer.msg("ReallyDelete","Really delete recording?"),
               Localizer.getLocalization(Localizer.I18N_DELETE)+"?",
               JOptionPane.YES_NO_OPTION);

       if (ret == JOptionPane.YES_OPTION) {
           dev.remove(UiUtilities.getLastModalChildOf(mParent), prg, false);

           mProgramTableModel.removeRow(row);

           createListData();
       }

    }

    @Override
    public Program getProgramForMouseEvent(MouseEvent e) {
      int row = mProgramTable.rowAtPoint(e.getPoint());
      //mProgramTable.changeSelection(row, 0, false, false);
      
      return (Program)mProgramTable.getValueAt(row, 1);
    }

    @Override
    public void mouseEventActionFinished() {
      
    }

    @Override
    public void showContextMenu(MouseEvent e) {
      showPopup(e);
    }

}