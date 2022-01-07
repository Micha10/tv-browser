/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.filter.dlgs;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.ShowAllFilter;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.programtable.ProgramTableModel;
import util.ui.ProgramPanel;
import util.ui.SendToPluginDialog;
import util.ui.UiUtilities;
import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * Creates the Buttons for the Filters
 */
public class FilterButtons implements ActionListener {
    /** The localizer for this class. */
    public static final util.i18n.Localizer LOCALIZER
      = util.i18n.Localizer.getLocalizerFor(FilterButtons.class);

    /** Menu-Items */
    private JMenuItem mEditFilterMI, mEditFilterComponentsMI, mSendFilterMI;

    
    /**
     * Constructor
     * @param mainFrame MainFrame
     */
    private FilterButtons(JMenu filterMenu, MainFrame mainFrame) {
      createFilterMenuItems(filterMenu,mainFrame);
    }
    
    public static void createFilterButtons(JMenu filterMenu, MainFrame mainFrame) {
      new FilterButtons(filterMenu,mainFrame);
    }

    /**
     * Returns the Menus for Filtering
     * @return Menus for Filtering
     */
    private void createFilterMenuItems(JMenu filterMenu, MainFrame mainFrame) {
        ProgramFilter curFilter = mainFrame.getProgramFilter();
                
        mEditFilterMI = new JMenuItem(LOCALIZER.ellipsisMsg("createFilter", "Edit filter"));
        mEditFilterMI.addActionListener(this);
        
        filterMenu.add(mEditFilterMI);
        
        mEditFilterComponentsMI = new JMenuItem(FilterComponentsDlg.LOCALIZER.ellipsisMsg("title", "Edit filter components"));
        mEditFilterComponentsMI.addActionListener(this);
        
        filterMenu.add(mEditFilterComponentsMI);
        
        if ((curFilter != null) && !(curFilter instanceof ShowAllFilter)){
            mSendFilterMI = new JMenuItem(LOCALIZER.msg("sendPrograms", "Send visible Programs to another Plugin"));
            mSendFilterMI.addActionListener(this);
            filterMenu.add(mSendFilterMI);
        }
        
        filterMenu.addSeparator();
        
        FilterList.getInstance().createFilterMenu(filterMenu,curFilter);
        
        mEditFilterMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
        mEditFilterComponentsMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mEditFilterMI) {
          MainFrame.getInstance().showFilterDialog();
        }
        else if (e.getSource() == mEditFilterComponentsMI) {
          new FilterComponentsDlg(UiUtilities.getLastModalChildOf(MainFrame.getInstance()));
        } else if (e.getSource() == mSendFilterMI) {
          sendPrograms();
        }
    }

    /**
     * Sends the visible Programs to another Plugin
     */
    private void sendPrograms() {
        
        Program[] prgs = collectPrograms();
        
        SendToPluginDialog sendTo = new SendToPluginDialog(null, (Window)MainFrame.getInstance(), prgs);
        sendTo.setVisible(true);
    }

    /**
     * Collects all visible Programs
     * @return visible Programs
     */
    private Program[] collectPrograms() {
        ArrayList<Program> array = new ArrayList<Program>();
        
        ProgramTableModel model = MainFrame.getInstance().getProgramTableModel();
        
        int columnCount = model.getColumnCount();
		for (int col = 0; col < columnCount; col++) {
            int rowCount = model.getRowCount(col);
			for (int row = 0; row < rowCount; row++) {
                // Get the program
                ProgramPanel panel = model.getProgramPanel(col, row);
                
                array.add(panel.getProgram());
            }
        }
        
        Program[] prg = new Program[array.size()];
        
        for (int i = 0; i < array.size(); i++) {
            prg[i] = array.get(i);
        }
        
        return prg;
    }
}