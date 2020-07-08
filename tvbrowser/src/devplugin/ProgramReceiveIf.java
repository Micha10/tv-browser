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
 *     $Date: 2005-08-20 17:18:20 +0200 (Sa, 20 Aug 2005) $
 *   $Author: darras $
 * $Revision: 1443 $
 */
package devplugin;

import java.awt.Component;

import javax.swing.JOptionPane;

import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * A interface for an object that supports receiving programs.
 * 
 * @author René Mach
 *
 */
public interface ProgramReceiveIf extends Comparable<ProgramReceiveIf> {
  static final Localizer LOCALIZER = Localizer.getLocalizerFor(ProgramReceiveIf.class);
  
  /**
   * Type for sending programs to plugins with
   * no information about the handling of the send
   * programs.
   * @since 4.2.2
   */
  public static final int TYPE_SENDING_UNDIFINED = 0;
  
  /**
   * Type used to signalize the receiving plugin
   * that the programs were added by the sender.
   * @since 4.2.2
   */
  public static final int TYPE_SENDING_ADDED = 1;
  
  /**
   * Type used to signalize the receiving plugin
   * that the programs were removed by the sender.
   * @since 4.2.2
   */
  public static final int TYPE_SENDING_REMOVED = 2;
  
  /**
   * Gets whether the ProgramReceiveIf supports receiving programs from other plugins with a special target.
   * 
   * @return Whether the ProgramReceiveIf supports receiving programs from other plugins with a special target.
   * 
   * @see #receivePrograms(Program[],ProgramReceiveTarget)
   * @since 2.5
   * @deprecated since 4.2.2
   */
  public boolean canReceiveProgramsWithTarget();
  
  /**
   * Gets the type of the receive action supported by this plugin.
   * @see #TYPE_PROGRAM_RECEIVE_DEFAULT, {@link #TYPE_PROGRAM_RECEIVE_ADD_REMOVE}
   * 
   * @return The type of the supported program receive actions.
   * @since 4.2.2
   */
  public int getSupportedProgramRecieveType();

  /**
   * Receives a list of programs from another plugin with a target.
   * 
   * @param programArr
   *          The programs passed from the other plugin.
   * @param receiveTarget
   *          The receive target of the programs.
   * @return <code>true</code>, if the programs were correctly received and the
   *         target really exists.
   * 
   * @see #canReceiveProgramsWithTarget()
   * @since 2.5
   * @deprecated since 4.2.2
   */
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget);

  /**
   * Method called when programs are send to this plugin.
   * 
   * @param type The type of the programs send by other plugin.
   * @param programArr
   *          The programs passed from the other plugin.
   * @param receiveTarget
   *          The receive target of the programs.
   * @return <code>true</code>, if the programs were correctly received and the
   *         target really exists.
   * @see #getSupportedProgramRecieveType()
   * 
   * @since 4.2.2
   */
  public boolean receivePrograms(int receiveType, Program[] programArr, ProgramReceiveTarget receiveTarget);
  
  /**
   * Returns an array of receive targets or <code>null</code> if there is no
   * target
   * 
   * @return The supported receive targets.
   * @see #canReceiveProgramsWithTarget()
   * @see #receivePrograms(Program[],ProgramReceiveTarget)
   * @since 2.5
   */
  public ProgramReceiveTarget[] getProgramReceiveTargets();
  
  /**
   * Receives a list of Strings from another plugin with a target.
   * 
   * @param values The value array passed from the other plugin.
   * @param receiveTarget The receive target of the programs.
   * @return <code>True</code> if the value array was handled correct,
   * <code>false</code> otherwise.
   * 
   * @see #canReceiveProgramsWithTarget()
   * @since 2.7
   * @deprecated since 4.2.2
   */
  public boolean receiveValues(String[] values, ProgramReceiveTarget receiveTarget);
  
  /**
   * Receives a list of Strings from another plugin with a target.
   *
   * @param type The type of the programs send by other plugin.
   * @param values
   *          The value array passed from the other plugin.
   * @param receiveTarget
   *          The receive target of the programs.
   * @return <code>true</code> if the value array was handled correct,
   *         <code>false</code> otherwise.
   *
   * @see #getSupportedProgramRecieveType()
   * @since 4.2.2
   */
  public boolean receiveValues(int type, String[] values, ProgramReceiveTarget receiveTarget);
  
  public String getId();
  
  /**
   * Asks the user to select the type of sending of the programs.
   * 
   * @param parent The parent component for the option dialog.
   * @return The type to use for sending of the programs
   * @since 4.2.2
   */
  public static int getTypeForSendingAction(Component parent) {
    if(parent == null) {
      parent = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    }
    
    int result = TYPE_SENDING_UNDIFINED;
    
    final String[] options = {
        LOCALIZER.msg("undefined", "I don't know"),
        LOCALIZER.msg("added", "Added"),
        LOCALIZER.msg("removed", "Removed")
    };
    
    int selection = JOptionPane.showOptionDialog(parent, LOCALIZER.msg("message", "Were the programs to send added or removed?"), LOCALIZER.msg("title", "Type of sending?"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    
    switch(selection) {
      case JOptionPane.CANCEL_OPTION: result = TYPE_SENDING_REMOVED;break;
      case JOptionPane.NO_OPTION: result = TYPE_SENDING_ADDED;break;
    }
    
    return result;
  }
}