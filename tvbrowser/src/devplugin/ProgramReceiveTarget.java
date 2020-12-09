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
package devplugin;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JOptionPane;

import tvbrowser.ui.mainframe.MainFrame;
import util.misc.HashCodeUtilities;
import util.i18n.Localizer;
import util.ui.UiUtilities;

/**
 * Is a target for receiving of program from other plugins. <br>
 * <br>
 * If your plugin should be able to receive programs from other plugins and
 * handle those programs in a specific way you have to create
 * ProgramReceiveTargets for the plugin. <br>
 * <br>
 * To do this use the constructor
 * <code>public ProgramReceiveTarget(ProgramReceiveIf receiveIf, String name, String targetId)</code>
 * . The <code>receiveIf</code> is you Plugin, the <code>name</code> is the name
 * the user will be see for selection of the target and the
 * <code>targetId</code> is a unique id which is used for identifying the
 * target.<br>
 * <br>
 * If your plugin only supports one target simply don't override the method
 * <code>getProgramReceiveTargets()</code>. <br>
 * <br>
 * If you want to compare two targets always use the equals method. <br>
 * <br>
 * Example:<br>
 * Plugin name MyPlugin. <br>
 * <br>
 * MyPlugin is a plugin which want to receive programs for two types of targets.
 * One target will be used to show the received programs in a dialog window, the
 * other target will be used to mark the programs for the plugin. <br>
 * <br>
 * MyPlugin overrides the methods used for identifying it as a receiveable
 * plugin:<br>
 * <br>
 * <code>public boolean canReceiveProgramsWithTarget() {<br>
  &nbsp;&nbsp;return true;<br>
  }<br>
  <br>
  public boolean receivePrograms(int eventType, Program[] programArr, ProgramReceiveTarget receiveTarget) {<br>
  &nbsp;&nbsp;ProgramReceiveTarget[] targets = getSupportedTargets();<br>
    <br>
  &nbsp;&nbsp;if(targets[0].equals(receiveTarget)<br>
  &nbsp;&nbsp;&nbsp;&nbsp;showProgramsInDialog(programArr);<br>
  &nbsp;&nbsp;else if (targets[1].equals(receiveTarget)<br>
  &nbsp;&nbsp;&nbsp;&nbsp;for(Program p : programArr)<br>
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;p.mark(this);<br>
  }<br>
  <br>
  public ProgramReceiveTarget[] getProgramReceiveTargets() {<br>
  &nbsp;&nbsp;return getSupportedTargets();<br>
  }</code><br>
 * <br>
 * The method <code>getSupportedTargets()</code> looks like this:<br>
 * <br>
 * <code>private ProgramReceiveTarget[] getSupportedTargets() {<br>
  &nbsp;&nbsp;ProgramReceiveTarget target1 = new ProgramReceiveTarget(this,"Show programs in dialog","showDialog");<br>
  &nbsp;&nbsp;ProgramReceiveTarget target2 = new ProgramReceiveTarget(this,"Mark programs for MyPlugin","markPrograms");<br>
    <br>
  &nbsp;&nbsp;return new ProgramReceiveTarget[] {target1,target2};<br>
  }</code> <br>
 * <br>
 * With this code the plugin MyPlugin is able to receive the programs from other
 * plugins and handle it in the specified manner. <br>
 * <br>
 *
 * @author René Mach
 * @since 2.5
 */
public final class ProgramReceiveTarget implements Comparable<ProgramReceiveTarget> {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(ProgramReceiveTarget.class);
  
  /**
   * Type for sending programs to plugins with
   * no information about the handling of the send
   * programs.
   * @since 4.2.2
   */
  public static final int TYPE_EVENT_UNDIFINED = 0;
  
  /**
   * Type used to signalize the receiving plugin
   * that the programs were added by the sender.
   * @since 4.2.2
   */
  public static final int TYPE_EVENT_ADDED = 1;
  
  /**
   * Type used to signalize the receiving plugin
   * that the programs were removed by the sender.
   * @since 4.2.2
   */
  public static final int TYPE_EVENT_REMOVED = 2;
  
  private String mReceiveIfId;
  private String mTargetId;
  private String mTargetName;

  private int mSupportedEventType;
  private int mUsedEventType;

  /**
   * Creates the default target for a ProgramReceiveIf.
   *
   * @param receiveIf The ProgramReceiveIf to create the null target for.
   * @return The default target for ProgramReceiveIf.
   */
  public static ProgramReceiveTarget[] createDefaultTargetArrayForProgramReceiveIf(ProgramReceiveIf receiveIf) {
    return new ProgramReceiveTarget[] {new ProgramReceiveTarget(receiveIf, LOCALIZER.msg("defaultTarget","Default target"), "NULL")};
  }

  /**
   * Creates the default target for an id of a ProgramReceiveIf.
   *
   * @param receiveIfId
   *          The id of the ProgramReceiveIf to create for.
   * @return The default target for the id of the ProgramReceiveIf.
   */
  public static ProgramReceiveTarget createDefaultTargetForProgramReceiveIfId(String receiveIfId) {
    return new ProgramReceiveTarget(TYPE_EVENT_UNDIFINED, receiveIfId, LOCALIZER.msg("defaultTarget","Default target"), "NULL");
  }
  
  /**
   * Creates an instance of the ProgramReceiveTarget.
   * Use this to create a target from the plugin for the other plugins to read.
   *
   * @param eventTypeSupported The event type supported by this target.
   * @param receiveIfId The ProgramReceiveIf id (your Plugin) to create for.
   * @param name The name of the target.
   * @param targetId The unique id of the target.
   */
  private ProgramReceiveTarget(int eventTypeSupported, String receiveIfId, String name, String targetId) {
    mSupportedEventType = eventTypeSupported;
    mReceiveIfId = receiveIfId;
    mTargetName = name;
    mTargetId = targetId;
    mUsedEventType = TYPE_EVENT_UNDIFINED;
  }

  /**
   * Creates an instance of the ProgramReceiveTarget.
   * Use this to create a target from the plugin for the other plugins to read.
   *
   * @param receiveIf The ProgramReceiveIf (your Plugin) to create for.
   * @param name The name of the target.
   * @param targetId The unique id of the target.
   */
  public ProgramReceiveTarget(ProgramReceiveIf receiveIf, String name, String targetId) {
    this(TYPE_EVENT_UNDIFINED, receiveIf.getId(), name, targetId);
  }
  
  /**
   * Creates an instance of the ProgramReceiveTarget.
   * Use this to create a target from the plugin for the other plugins to read.
   * NOTE: If a target should support {@link #TYPE_EVENT_ADDED} and {@link #TYPE_EVENT_REMOVED}
   * the eventTypeSupported has to be set to {@link #TYPE_EVENT_ADDED} + {@link #TYPE_EVENT_REMOVED}
   * 
   * @param eventTypeSupported The event type supported by this target.
   * @param receiveIf The ProgramReceiveIf (your Plugin) to create for.
   * @param name The name of the target.
   * @param targetId The unique id of the target.
   */
  public ProgramReceiveTarget(int eventTypeSupported, ProgramReceiveIf receiveIf, String name, String targetId) {
    this(eventTypeSupported, receiveIf.getId(), name, targetId);
  }

  /**
   * Creates an instance of the ProgramReceiveTarget.
   * Use this to load an target previously saved with {@link #writeData(ObjectOutputStream)}.
   *
   * @param in The input stream.
   * @throws IOException Thrown if an IO operation went wrong.
   * @throws ClassNotFoundException Thrown if a class could not be found.
   */
  public ProgramReceiveTarget(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt(); // version
    mReceiveIfId = in.readUTF();
    mTargetId = in.readUTF();
    mTargetName = in.readUTF();
    
    if(version >= 2) {
      mUsedEventType = in.readInt();
    }
    else {
      mUsedEventType = TYPE_EVENT_UNDIFINED;
    }
    
    if(version >= 3) {
      mSupportedEventType = in.readInt();
    }
    else {
      mSupportedEventType = TYPE_EVENT_UNDIFINED;
    }
  }

  /**
   * Use this to save a target.
   *
   * @param out The stream to write to.
   * @throws IOException Thrown if an IO operation went wrong.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(3); //version
    out.writeUTF(mReceiveIfId);
    out.writeUTF(mTargetId);
    out.writeUTF(mTargetName);
    out.writeInt(mUsedEventType);
    out.writeInt(mSupportedEventType);
  }

  /**
   * get the target name (for display)
   *
   * @return target name
   * @since 3.0
   */
  public String getTargetName() {
    return mTargetName;
  }

  public String toString() {
    return getTargetName();
  }

  /**
   * @return The id of the ProgramReceiveIf of this target.
   */
  public String getReceiveIfId() {
    return mReceiveIfId;
  }

  /**
   * @return The id of this target.
   */
  public String getTargetId() {
      return mTargetId;
  }

  public boolean equals(Object o) {
    if(o == null) {
      return false;
    } else if(o instanceof ProgramReceiveTarget) {
      return ((ProgramReceiveTarget)o).mReceiveIfId.compareTo(mReceiveIfId) == 0 && ((ProgramReceiveTarget)o).mTargetId.compareTo(mTargetId) == 0;
    }

    return false;
  }

  @Override
  public int hashCode() {
    int result = HashCodeUtilities.hash(mReceiveIfId);
    result = HashCodeUtilities.hash(result, mTargetId);
    return result;
  }

  /**
   * @return The ProgramReceiveIf for the ProgramReceiveIfId of this target.
   */
  public ProgramReceiveIf getReceifeIfForIdOfTarget() {
    return Plugin.getPluginManager().getReceiceIfForId(mReceiveIfId);
  }

  /**
   * Checks if the given target is the default target for the given ProgramReceiveIf.
   *
   * @param receiveIf The ProgramReceiveIf to check.
   * @param receiveTarget The ProgramReceiveTarget to check.
   * @return True if the receiveTarget is the default ProgramReceiveTarget for the ProgramReceiveIf.
   */
  public static boolean isDefaultProgramReceiveTargetForProgramReceiveIf(ProgramReceiveIf receiveIf, ProgramReceiveTarget receiveTarget) {
    if(receiveIf == null || receiveTarget == null) {
      return false;
    }

    return receiveIf.getId().compareTo(receiveTarget.mReceiveIfId) == 0 && receiveTarget.mTargetId.compareTo("NULL") == 0;
  }

  /**
   * Checks if the given id is used by this instance of ProgramReceiveTarget
   * and contains to the given ProgramReceiveIf.
   *
   * @param id The id to check.
   * @param receiveIf The receive if to which the ProgramReceiveTarget is connected.
   * @return True if this instance is using the id and contains to the given ProgramReceiveIf.
   */
  public boolean isReceiveTargetWithIdOfProgramReceiveIf(ProgramReceiveIf receiveIf, String id) {
    if(receiveIf == null || id == null) {
      return false;
    }

    return receiveIf.getId().compareTo(mReceiveIfId) == 0 && id.compareTo(mTargetId) == 0;
  }

  @Override
  public int compareTo(ProgramReceiveTarget other) {
    return getTargetName().compareTo(other.getTargetName());
  }

  /**
   * Send the programs to the receive target
   *
   * @param programs programs to send
   * @since 3.0
   * @deprecated since 4.2.2 use {@link #receivePrograms(int, Program[])} instead.
   */
  @Deprecated(since="4.2.2") public void receivePrograms(Program[] programs) {
    receivePrograms(TYPE_EVENT_UNDIFINED, programs);
  }
  
  /**
   * Send the programs to the receive target
   *
   * @param type The type of the send programs.
   * @param programs programs to send
   * @since 4.2.2
   */
  public boolean receivePrograms(int type, Program[] programs) {
    boolean result = false;
    
    ProgramReceiveIf plugin = getReceifeIfForIdOfTarget();
    if (plugin != null && plugin.canReceiveProgramsWithTarget()) {
      result = plugin.receivePrograms(type, programs, this);
    }
    
    return result;
  }
  

  /**
   * Send the programs to the receive target
   *
   * @param type The type of the send programs.
   * @param values values to send
   * @since 4.2.2
   */
  public boolean receiveValues(int type, String[] values) {
    boolean result = false;
    
    ProgramReceiveIf plugin = getReceifeIfForIdOfTarget();
    if (plugin != null && plugin.canReceiveProgramsWithTarget()) {
      result = plugin.receiveValues(type, values, this);
    }
    
    return result;
  }
  
  /**
   * Gets the supported event type of this target.
   * NOTE: If a target supports {@link #TYPE_EVENT_ADDED} and {@link #TYPE_EVENT_REMOVED} it will
   * return a value of {@link #TYPE_EVENT_ADDED} + {@link #TYPE_EVENT_REMOVED}
   * @return The supported event type.
   * @since 4.2.2
   * @see {@link #TYPE_EVENT_UNDIFINED}, {@link #TYPE_EVENT_ADDED} and {@link #TYPE_EVENT_REMOVED}
   */
  public int getSupportedEventType() {
    return mSupportedEventType;
  }
  
  /**
   * Gets the event type the sending plugin to handle.
   * 
   * @return The event type 
   * @since 4.2.2
   * @see {@link #TYPE_EVENT_UNDIFINED}, {@link #TYPE_EVENT_ADDED}, {@link #TYPE_EVENT_REMOVED}
   */
  public int getEventType() {
    return mUsedEventType;
  }
  
  /**
   * Sets the event type to use when using this target.
   * NOTE: If this target should support adding and removing of programs, set the
   * type to {@link #TYPE_EVENT_ADDED} + {@link #TYPE_EVENT_REMOVED}
   * 
   * @param eventType
   * @since 4.2.2
   * @see {@link ProgramReceiveIf#TYPE_SENDING_UNDIFINED}, {@link ProgramReceiveIf#TYPE_SENDING_ADDED}, {@link ProgramReceiveIf#TYPE_SENDING_REMOVED}
   */
  public void setEventType(int eventType) {
    mUsedEventType = eventType;
  }
  

  /**
   * Asks the user to select the type of sending of the programs.
   * 
   * @param parent The parent component for the option dialog.
   * @param target The receive target to get the event type for.
   * @return The type to use for sending of the programs
   * @since 4.2.2
   */
  public static int getEventTypeForSendingAction(Component parent, ProgramReceiveTarget target) {
    if(parent == null) {
      parent = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    }
    
    int result = target.getSupportedEventType();
    
    if(result == ProgramReceiveTarget.TYPE_EVENT_ADDED + ProgramReceiveTarget.TYPE_EVENT_REMOVED) {
      final String[] options = {
          LOCALIZER.msg("undefined", "I don't know"),
          LOCALIZER.msg("added", "Added"),
          LOCALIZER.msg("removed", "Removed")
      };
      
      int selection = JOptionPane.showOptionDialog(parent, LOCALIZER.msg("message", "Were the programs to send added or removed?"), LOCALIZER.msg("title", "Type of sending?"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
      
      switch(selection) {
        case JOptionPane.CANCEL_OPTION: result = TYPE_EVENT_REMOVED;break;
        case JOptionPane.NO_OPTION: result = TYPE_EVENT_ADDED;break;
        case JOptionPane.YES_OPTION: result = TYPE_EVENT_UNDIFINED;break;
      }
    }
    
    return result;
  }
}
