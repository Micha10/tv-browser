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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package util.ui.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashSet;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

import devplugin.ActionMenu;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Plugin;
import devplugin.Program;
import util.ui.ScrollableMenu;
import util.ui.html.HTMLTextHelper;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 02.01.2005
 * Time: 19:07:40
 */
public class MenuUtil {

  public static final Font CONTEXT_MENU_PLAINFONT = UIManager.getFont("MenuItem.font").deriveFont(Font.PLAIN);
  public static final Font CONTEXT_MENU_BOLDFONT = UIManager.getFont("MenuItem.font").deriveFont(Font.BOLD);
  public static final Font CONTEXT_MENU_ITALICFONT = UIManager.getFont("MenuItem.font").deriveFont(Font.ITALIC);
  public static final Font CONTEXT_MENU_BOLDITALICFONT = UIManager.getFont("MenuItem.font").deriveFont(Font.PLAIN + Font.ITALIC);


  public static JMenuItem createMenuItem(String title) {
    JMenuItem result = new JMenuItem(title);
    result.setFont(CONTEXT_MENU_PLAINFONT);
    return result;
  }

  public static JMenuItem createMenuItem(ActionMenu menu) {
    return createMenuItem(menu, null);
  }
  
  /** @since 4.2.5 */
  public static JMenuItem createMenuItem(ActionMenu menu, final HashSet<Integer> disabledItems) {
    return createMenuItem(menu, true, disabledItems);
  }
  
  public static JMenuItem createMenuItem(ActionMenu menu, boolean setFont) {
    return createMenuItem(menu, setFont, null);
  }
  
  /** @since 4.2.5 */
  public static JMenuItem createMenuItem(ActionMenu menu, boolean setFont, final HashSet<Integer> disabledItems) {
    if (menu == null) {
      return null;
    }
    JMenuItem result = null;
    if (menu.hasSubItems()) {
      result = new ScrollableMenu(menu.getAction());
      
      ActionMenu[] subItems = menu.getSubItems();
      for (ActionMenu subItem : subItems) {
        if(disabledItems == null || !disabledItems.contains(subItem.getActionId())) {
          JMenuItem item = createMenuItem(subItem, setFont, disabledItems);
  
          if (item == null) {
            ((JMenu) result).addSeparator();
          } else {
            result.add(item);
          }
        }
      }
    }
    else {
      if(ContextMenuSeparatorAction.getInstance().equals(menu.getAction())) {
        return null;
      }
      else if (menu.getAction()!=null) {
    	  if(menu.isSelected()) {
        	  result = new ColoredCheckBoxMenuItem(menu.getAction());
        	  result.setSelected(true);
          }
    	  else {
    		  result = new ColoredMenuItem(menu.getAction());
    	  }
      }
    }
    if (result != null && setFont) {
      result.setFont(CONTEXT_MENU_PLAINFONT);
    }
    return result;
  }
  
  private static final class ColoredMenuItem extends JMenuItem {
	private Color mBackground;
	private String mText;
	
	public ColoredMenuItem(Action action) {
		super(action);
		mText = HTMLTextHelper.checkTextForTextFormatingTags(getText());
		
		Object o = action.getValue(Program.MARK_PRIORITY);
		 
		if(o != null && o instanceof Integer) {
			mBackground = Plugin.getPluginManager().getTvBrowserSettings().getColorForMarkingPriority((Integer)o);
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if(isArmed() && mText.startsWith("<html>")) {
			setText("<html><div style=\"color:"+HTMLTextHelper.getCssRgbColorEntry(UIManager.getColor("MenuItem.selectionForeground"))+"\">" + mText.replace("<html>", "").replace("</html>", "<html>") + "</div><html>");
		}
		else if(mText.startsWith("<html>")) {
			setText(mText);
		}
		
		if(mBackground != null) {
			if(!isArmed()){
				setOpaque(false);
				Color old = g.getColor();
				g.clearRect(0, 0, getWidth(), getHeight());
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(mBackground);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(old);
			}
			else {
				setOpaque(true);
			}			
		}
		
		super.paintComponent(g);
	}
  }
  
  private static final class ColoredCheckBoxMenuItem extends JCheckBoxMenuItem {
		private Color mBackground;
		private String mText;
		
		public ColoredCheckBoxMenuItem(Action action) {
		  super(action);
		  mText = HTMLTextHelper.checkTextForTextFormatingTags(getText());
		  
		  Object o = action.getValue(Program.MARK_PRIORITY);
		 
		  if(o != null && o instanceof Integer) {
			mBackground = Plugin.getPluginManager().getTvBrowserSettings().getColorForMarkingPriority((Integer)o);
		  }
		}
		
		@Override
		public void setSelected(boolean b) {
			if(b) {
				setIcon(null);
			}
			
			super.setSelected(b);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			if(isArmed() && mText.startsWith("<html>")) {
				setText("<html><div style=\"color:"+HTMLTextHelper.getCssRgbColorEntry(UIManager.getColor("MenuItem.selectionForeground"))+"\">" + mText.replace("<html>", "").replace("</html>", "<html>") + "</div><html>");
			}
			else if(mText.startsWith("<html>")) {
				setText(mText);
			}
		
			if(mBackground != null) {
				if(!isArmed()){
					setOpaque(false);
					Color old = g.getColor();
					g.clearRect(0, 0, getWidth(), getHeight());
					g.setColor(getBackground());
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(mBackground);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(old);
				}
				else {
					setOpaque(true);
				}			
			}
			
			super.paintComponent(g);
		}
	  }
}