/*
* TV-Browser
* Copyright (C) 2022 TV-Browser team (dev@tvbrowser.org)
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
* SVN information:
*     $Date$
*   $Author$
* $Revision$
*/
package tvbrowser.extras.reminderplugin;

import java.util.Properties;

import util.settings.PropertyBasedSettings;

/**
 * A class with all settings of the ReminderPlugin.
 * 
 * @author René Mach
 * @since 4.2.7
 */
public class ReminderSettings extends PropertyBasedSettings {
  static final String KEY_DIALOG_TIME_SELECTION_SHOW = "showTimeSelectionDialog";
  static final String KEY_DIALOG_REMOVED_SHOW = "showRemovedDialog";
  static final String KEY_DATE_SEPARATORS_SHOW = "showDateSeparators";
  static final String KEY_TAB_PROVIDE = "provideTab";
  static final String KEY_REMINDERS_STICKY = "stickyReminders";
  static final String KEY_REMINDERS_STICKY_MINUTES = "stickyMinutes";
  static final String KEY_SOUND_USE = "usesound";
  static final String KEY_BEEP_USE = "usebeep";
  static final String KEY_EXECUTE_FILE = "execfile";
  static final String KEY_EXECUTE_PARAMETERS = "execparam";
  static final String KEY_EXECUTE_USE = "useexec";
  static final String KEY_PREFILTER = "prefilter";
  static final String KEY_MARK_PRIORITY = "markPriority";
  
  static final String KEY_DIALOG_POS_X = "dlgXPos";
  static final String KEY_DIALOG_POS_Y = "dlgYPos";
  static final String KEY_DIALOG_WIDTH = "dlgWidth";
  static final String KEY_DIALOG_HEIGHT = "dlgHeight";
  
  static final String KEY_REMINDER_AUTO_CLOSE_TIME = "autoCloseReminderTime";
  static final String KEY_REMINDER_AUTO_CLOSE_BEHAVIOUR = "autoCloseBehaviour";
  static final String VALUE_REMINDER_AUTO_CLOSE_ON_END = "onEnd";
  static final String VALUE_REMINDER_AUTO_CLOSE_ON_TIME = "onTime";
  static final String VALUE_REMINDER_AUTO_CLOSE_NEVER = "never";
  
  static final String KEY_TIME_COUNTER_SHOW = "showTimeCounter";
  static final String KEY_REMINDER_ENTRY_DEFAULT = "defaultReminderEntry";
  
  static final String VALUE_AUTO_RESIZE_TYPE_TOP = "top";
  static final String VALUE_AUTO_RESIZE_TYPE_BOTTOM = "bottom";
  
  static final String KEY_AUTO_RESIZE_ENABLED = "autoResizeEnabled";
  static final String KEY_AUTO_RESIZE_TYPE = "typeAutoResize";
  static final String KEY_AUTO_CLOSE_BEHAVIOUR = "autoCloseBehaviour";
  static final String KEY_AUTO_CLOSE_REMINDER_TIME = "autoCloseReminderTime";
  static final String KEY_AUTO_CLOSE_FRAME_REMINDERS_IF_EMTPY = "autoCloseFrameRemindersIfEmpty";
  static final String KEY_FRAME_REMINDERS_TO_FRONT_WHEN_REMINDER_ADDED = "frameRemindersToFrontOnAdd";
  
  static final String KEY_FRAME_REMINDERS_XPOS = "frameRemindersXpos";
  static final String KEY_FRAME_REMINDERS_WIDTH = "frameRemindersWidth";
  
  static final String KEY_SOUNDFILE = "soundfile";
  static final String KEY_FRAME_REMINDERS_SHOW = "showFrameReminders";
  static final String KEY_REMINDER_WINDOW_SHOW = "usemsgbox";
  static final String KEY_REMINDER_WINDOW_ALWAYS_ON_TOP = "alwaysOnTop";
  static final String KEY_REMINDER_WINDOW_POSITION = "reminderWindowPosition";
  static final String KEY_SCROLL_TIME_TYPE_NEXT = "scrollTimeTypeNext";
  
  public ReminderSettings(Properties properties) {
    super(properties);
  }
  
  public boolean isAutoCloseOnEnd() {
    return get(KEY_REMINDER_AUTO_CLOSE_BEHAVIOUR, VALUE_REMINDER_AUTO_CLOSE_ON_END).equals(VALUE_REMINDER_AUTO_CLOSE_ON_END);
  }
  
  public boolean isAutoCloseOnTime() {
    return get(KEY_REMINDER_AUTO_CLOSE_BEHAVIOUR, VALUE_REMINDER_AUTO_CLOSE_ON_END).equals(VALUE_REMINDER_AUTO_CLOSE_ON_TIME);
  }
  
  public boolean isAutoCloseNever() {
    return get(KEY_REMINDER_AUTO_CLOSE_BEHAVIOUR, VALUE_REMINDER_AUTO_CLOSE_ON_END).equals(VALUE_REMINDER_AUTO_CLOSE_NEVER);
  }
  
  public boolean isAutoResizeTop() {
    return get(KEY_AUTO_RESIZE_TYPE).equals(VALUE_AUTO_RESIZE_TYPE_TOP);
  }
  
  public boolean isAutoResizeBottom() {
    return get(KEY_AUTO_RESIZE_TYPE).equals(VALUE_AUTO_RESIZE_TYPE_BOTTOM);
  }
  
  @Override
  protected void set(String key, boolean value) {
    super.set(key, value);
  }
  
  @Override
  protected void set(String key, int value) {
    super.set(key, value);
  }
  
  @Override
  protected void set(String key, String value) {
    super.set(key, value);
  }
  
  protected boolean isSet(String key) {
    String defaultValue = ReminderPropertyDefaults.getPropertyDefaults().getDefaultValueForKey(key);
    
    return super.get(key, defaultValue != null && defaultValue.equals("true"));
  }
  
  protected int getAsInt(String key) {
    int defaultValue = Integer.parseInt(ReminderPropertyDefaults.getPropertyDefaults().getDefaultValueForKey(key));
    
    return super.get(key, defaultValue);
  }
  
  @Override
  protected String get(String key) {
    String defaultValue = ReminderPropertyDefaults.getPropertyDefaults().getDefaultValueForKey(key);
    
    return super.get(key, defaultValue != null ? defaultValue : "");
  }
  
  @Override
  protected String get(String key, String defaultValue) {
    return super.get(key, defaultValue);
  }
  
  public boolean hasProperty(String key) {
    return storeSettings().getProperty(key) != null;
  }
}
