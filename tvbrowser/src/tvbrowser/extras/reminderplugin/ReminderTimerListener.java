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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */


package tvbrowser.extras.reminderplugin;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import util.exc.ErrorHandler;
import util.io.ExecutionHandler;
import util.paramhandler.ParamParser;

public class ReminderTimerListener {

  private static final util.i18n.Localizer mLocalizer
      = util.i18n.Localizer.getLocalizerFor(ReminderTimerListener.class );

  private static final Logger mLog = Logger.getLogger(ReminderTimerListener.class.getName());

  private ReminderSettings mSettings;
  private ReminderList mReminderList;

  public ReminderTimerListener(ReminderSettings settings, ReminderList reminderList) {
    mSettings = settings;
    mReminderList = reminderList;
  }

  public void timeEvent(ArrayList<ReminderListItem> reminders) {
    // filter expired items, just for safety
    ArrayList<ReminderListItem> notExpired = new ArrayList<ReminderListItem>(
        reminders.size());
    for (ReminderListItem item : reminders) {
      if (!item.getProgramItem().getProgram().isExpired()) {
        notExpired.add(item);
      }
    }
    reminders = notExpired;
    if (reminders.isEmpty()) {
      return;
    }

    if (mSettings.isSet(ReminderSettings.KEY_SOUND_USE)) {
      ReminderPlugin.playSound(mSettings.get(ReminderSettings.KEY_SOUNDFILE));
    }
    if (mSettings.isSet(ReminderSettings.KEY_BEEP_USE)) {
      Toolkit.getDefaultToolkit().beep();
    }

    if (mSettings.isSet(ReminderSettings.KEY_FRAME_REMINDERS_SHOW) 
        || mSettings.isSet(ReminderSettings.KEY_REMINDER_WINDOW_SHOW)) {
      // sort reminders by time
      HashMap<Integer, ArrayList<ReminderListItem>> sortedReminders = new HashMap<Integer, ArrayList<ReminderListItem>>(reminders.size());
      for (ReminderListItem reminder : reminders) {
        int time = reminder.getProgram().getStartTime();
        // take all already running programs together because there are no different reminder options for them
        if (reminder.getProgram().isOnAir() || reminder.getProgram().isExpired()) {
          time = -1;
        }
        ArrayList<ReminderListItem> list = sortedReminders.get(time);
        if (list == null) {
          list = new ArrayList<ReminderListItem>();
          sortedReminders.put(time, list);
        }
        list.add(reminder);
      }
      
      Set<Integer> keys = sortedReminders.keySet();
      Integer[] timeSortedKeys = keys.toArray(new Integer[keys.size()]);
      Arrays.sort(timeSortedKeys);
      
      // show reminders at same time in one window
      for (int i = timeSortedKeys.length-1; i >= 0; i--) {
        final ArrayList<ReminderListItem> singleTimeReminders = sortedReminders.get(timeSortedKeys[i]);
        
        if(mSettings.isSet(ReminderSettings.KEY_FRAME_REMINDERS_SHOW)) {
          FrameReminders.getInstance().addReminders(mReminderList, singleTimeReminders);
        }
        else if(mSettings.isSet(ReminderSettings.KEY_REMINDER_WINDOW_SHOW)) {
          new ReminderFrame(mReminderList, singleTimeReminders, getAutoCloseReminderTime(singleTimeReminders));
        }
      }
    } else {
      for (ReminderListItem reminder : reminders) {
        mReminderList.removeWithoutChecking(reminder.getProgramItem());
        mReminderList.blockProgram(reminder.getProgram());
      }
    }
    if (mSettings.isSet(ReminderSettings.KEY_EXECUTE_USE)) {
      String fName = mSettings.get(ReminderSettings.KEY_EXECUTE_FILE).trim();
      if (StringUtils.isNotEmpty(fName)) {
        for (ReminderListItem reminder : reminders) {
          ParamParser parser = new ParamParser();
          String fParam = parser.analyse(
              mSettings.get(ReminderSettings.KEY_EXECUTE_PARAMETERS), reminder.getProgram());

          try {
            ExecutionHandler executionHandler = new ExecutionHandler(fParam,
                fName);
            executionHandler.execute();
          } catch (Exception exc) {
            String msg = mLocalizer.msg("error.2",
                "Error executing reminder program!\n({0})", fName, exc);
            ErrorHandler.handle(msg, exc);
          }
        }
      } else {
        mLog.warning("Reminder program name is not defined!");
      }
    }

    // send to receiving plugins
    ProgramReceiveTarget[] targets = ReminderPlugin.getInstance().getClientPluginsTargets();

    ArrayList<Program> programs = new ArrayList<Program>();
    for (ReminderListItem reminder : reminders) {
      programs.add(reminder.getProgram());
    }

    for (ProgramReceiveTarget target : targets) {
      target.getReceifeIfForIdOfTarget().receivePrograms(ProgramReceiveTarget.TYPE_EVENT_UNDIFINED, programs.toArray(new Program[programs.size()]),target);
    }

    SwingUtilities.invokeLater(() -> new Thread("Update reminder tree") {
      public void run() {
        setPriority(Thread.MIN_PRIORITY);
        ReminderPlugin.getInstance().updateRootNode(true);
      }
    }.start());
  }

  
  private int getAutoCloseReminderTime(ArrayList<ReminderListItem> reminders) {
    int result = 0;
    for (ReminderListItem reminder : reminders) {
      result = Math
          .max(result, ReminderConstants.getAutoCloseReminderTime(reminder.getProgram()));
    }
    return result;
  }

}