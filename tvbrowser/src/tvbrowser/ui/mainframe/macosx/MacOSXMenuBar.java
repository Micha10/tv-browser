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

package tvbrowser.ui.mainframe.macosx;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.KeyStroke;

import tvbrowser.TVBrowser;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.MenuBar;

//import com.apple.eawt.*;

public class MacOSXMenuBar extends MenuBar {
  //private final static Logger LOGGER = Logger.getLogger(MacOSXMenuBar.class.getName());

  public MacOSXMenuBar(MainFrame mainFrame, JLabel label) {
    super(mainFrame, label);

    Thread toAddMenus = new Thread() {
      public void run() {
        if(!Desktop.isDesktopSupported()) {
          JMenu fileMenu = createMenu("menu.main", "&File", true);
          add(fileMenu);
          
          if (TVBrowser.restartEnabled()) {
            fileMenu.add(mRestartMI);
          }
          
          fileMenu.addSeparator();
          fileMenu.add(mQuitMI);
        }
        else if (TVBrowser.restartEnabled()) {
        	JMenu fileMenu = createMenu("menu.main", "&File", true);
            add(fileMenu);
            fileMenu.add(mRestartMI);
        }
    
        createCommonMenus(!Desktop.isDesktopSupported());
        
        if(mEditMenu != null) {
          mEditMenu.add(mSettingsMI);
        }
    
        int commandModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    
        // shortcuts as defined in the Apple guidelines
        // command Q
        mQuitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, commandModifier));
    
        // command ,
        mSettingsMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, commandModifier));
    
        // alt command T
        mToolbarMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, commandModifier | KeyEvent.ALT_DOWN_MASK));
      }
    };
    
    addAdditionalMenus(toAddMenus);
  }
}