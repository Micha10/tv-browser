/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import tvbrowser.core.Settings;

/**
 * A helper class to show option dialogs
 * with a JCheckBox to disable the showing
 * of the dialog.
 */
public class DontShowAgainOptionBox {

  /** The localizer for this class. */
  private static final util.i18n.Localizer mLocalizer
      = util.i18n.Localizer.getLocalizerFor(DontShowAgainOptionBox.class);

  /**
   * Creates an option dialog with JOptionPane.
   * <p>
   * @param messageBoxId The id for this message box.
   * @param parent The parent component of this dialog.
   * @param message The message to show the user. Can be an array if more than one component should be shown.
   * @param title The title of the option dialog.
   * @param messageType The message type of the option dialog, value are {#javax.swing.JOptionPane.ERROR_MESSAGE}, {#javax.swing.JOptionPane.INFORMATION_MESSAGE}, {#javax.swing.JOptionPane.WARNING_MESSAGE}.
   * @param optionType The option type of the option dialog, values are {@link JOptionPane#YES_OPTION}, {@link JOptionPane#YES_NO_OPTION}, {@link JOptionPane#YES_NO_CANCEL_OPTION}, {@link JOptionPane#OK_OPTION},
   *                   {@link JOptionPane#OK_CANCEL_OPTION}, {@link JOptionPane#DEFAULT_OPTION}.
   * @param options The options to show on the option buttons.
   * @param initialValue The option initially selected.
   * @param dontShowAgainLabel The label for the check box or, <code>null</code> for the default label.
   * @return The result of the option dialog, values are the possible values for optionType.
   * @since 4.2.2
   */
  public static int showOptionDialog(String messageBoxId, Component parent,
      Object message, String title, int messageType, int optionType, Object[] options,
      Object initialValue, String dontShowAgainLabel) {

    if (Settings.propHiddenMessageBoxes.containsItem(messageBoxId)) {
      return JOptionPane.YES_OPTION;
    }
    
    final JCheckBox askAgain = new JCheckBox(dontShowAgainLabel == null ? mLocalizer.msg("dontShowAgain", "Don't show this message again") : dontShowAgainLabel);
    final ArrayList<Object> shownObjects = new ArrayList<>();

    if(message.getClass().isArray()) {
      final Object[] arr = (Object[])message;
      
      for(Object o : arr) {
        shownObjects.add(o);
      }
    }
    else {
      shownObjects.add(message);
    }
    
    // have some space between message and checkbox
    if (!(shownObjects.get(shownObjects.size()-1) instanceof String) || !((String)shownObjects.get(shownObjects.size()-1)).endsWith("\n\n")) {
      shownObjects.add("\n");
    }
    
    shownObjects.add(askAgain);
    
    int result = JOptionPane.showOptionDialog(parent, shownObjects.toArray(), title, optionType, messageType, null, options, initialValue);

    if (askAgain.isSelected()) {
      Settings.propHiddenMessageBoxes.addItem(messageBoxId);
    }

    return result;
  }

  /**
   * Creates an option dialog with JOptionPane.
   * <p>
   * @param messageBoxId The id for this message box.
   * @param parent The parent component of this dialog.
   * @param message The message to show the user.
   * @param title The title of the option dialog.
   * @param messageType The message type of the option dialog, value are {#javax.swing.JOptionPane.ERROR_MESSAGE}, {#javax.swing.JOptionPane.INFORMATION_MESSAGE}, {#javax.swing.JOptionPane.WARNING_MESSAGE}.
   * @param optionType The option type of the option dialog, values are {@link JOptionPane#YES_OPTION}, {@link JOptionPane#YES_NO_OPTION}, {@link JOptionPane#YES_NO_CANCEL_OPTION}, {@link JOptionPane#OK_OPTION},
   *                   {@link JOptionPane#OK_CANCEL_OPTION}, {@link JOptionPane#DEFAULT_OPTION}.
   * @param options The options to show on the option buttons.
   * @param initialValue The option initially selected.
   * @param dontShowAgainLabel The label for the check box or, <code>null</code> for the default label.
   * @return The result of the option dialog, values are the possible values for optionType.
   */
  public static int showOptionDialog(String messageBoxId, Component parent,
      String message, String title, int messageType, int optionType, Object[] options,
      Object initialValue, String dontShowAgainLabel) {
    return showOptionDialog(messageBoxId, parent, (Object)message, title, messageType, optionType, options, initialValue, dontShowAgainLabel);
  }
  
  /**
   * Creates an option dialog with JOptionPane.
   * <p>
   * @param messageBoxId The id for this message box.
   * @param parent The praent component of this dialog.
   * @param message The message to show the user.
   * @param title The title of the option dialog.
   * @param messageType The message type of the option dialog, value are {#javax.swing.JOptionPane.ERROR_MESSAGE}, {#javax.swing.JOptionPane.INFORMATION_MESSAGE}, {#javax.swing.JOptionPane.WARNING_MESSAGE}.
   *
   * @return The result of the option dialog, values are the possible values for optionType.
   */
  public static int showOptionDialog(String messageBoxId, Component parent,
      String message, String title, int messageType) {
    return showOptionDialog(messageBoxId, parent, message, title, messageType, JOptionPane.DEFAULT_OPTION, null, null, null);
  }

  /**
   * Creates an option dialog with JOptionPane.
   * <p>
   * @param messageBoxId The id for this message box.
   * @param parentComponent The praent component of this dialog.
   * @param message The message to show the user.
   * @param title The title of the option dialog.
   *
   * @return The result of the option dialog, values are the possible values for optionType.
   */
  public static int showOptionDialog(String messageBoxId, Component parentComponent,
      String message, String title) {
    return showOptionDialog(messageBoxId, parentComponent, message, title,
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Creates an option dialog with JOptionPane.
   * <p>
   * @param messageBoxId The id for this message box.
   * @param parentComponent The praent component of this dialog.
   * @param message The message to show the user.
   *
   * @return The result of the option dialog, values are the possible values for optionType.
   */
  public static int showOptionDialog(String messageBoxId, Component parentComponent,
      String message) {
    return showOptionDialog(messageBoxId, parentComponent, message, UIManager
        .getString("OptionPane.messageDialogTitle"));
  }
  
  /**
   * @param messageBoxId The id of the message box to check if it is currently hidden.
   * @return <code>true</code> if the message box with the given id is currently hidden, <code>false</code> otherwise.
   * @since 4.2.2
   */
  public static boolean isHiddenMessageBox(final String messageBoxId) {
    return Settings.propHiddenMessageBoxes.containsItem(messageBoxId);
  }
}