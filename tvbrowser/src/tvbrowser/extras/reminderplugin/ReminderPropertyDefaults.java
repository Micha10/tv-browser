package tvbrowser.extras.reminderplugin;

import java.util.HashMap;

import devplugin.Program;
import util.misc.PropertyDefaults;

public final class ReminderPropertyDefaults {
  private static final HashMap<String, String> DEFAULT_VALUE_MAP;
  private static final PropertyDefaults DEFAULT_VALUES;
  
  static {
    DEFAULT_VALUE_MAP = new HashMap<String, String>();
    
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_EXECUTE_FILE, "");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_EXECUTE_PARAMETERS, "");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_AUTO_CLOSE_REMINDER_TIME, "10");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_SOUNDFILE, "/");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_TIME_COUNTER_SHOW, "false");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_DIALOG_TIME_SELECTION_SHOW, "true");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_DIALOG_REMOVED_SHOW, "true");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_DIALOG_POS_X, "-1");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_DIALOG_POS_Y, "-1");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_TAB_PROVIDE, "true");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_FRAME_REMINDERS_XPOS, "0");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_REMINDERS_STICKY, "true");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_REMINDERS_STICKY_MINUTES, "15");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_FRAME_REMINDERS_SHOW, "true");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_AUTO_CLOSE_FRAME_REMINDERS_IF_EMTPY, "true");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_FRAME_REMINDERS_TO_FRONT_WHEN_REMINDER_ADDED, "true");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_REMINDER_WINDOW_SHOW, "false");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_REMINDER_WINDOW_ALWAYS_ON_TOP, "true");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_REMINDER_WINDOW_POSITION, "6");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_SCROLL_TIME_TYPE_NEXT, "true");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_DATE_SEPARATORS_SHOW, "true");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_MARK_PRIORITY, String.valueOf(Program.PRIORITY_MARK_MIN));
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_AUTO_RESIZE_ENABLED, "false");
    DEFAULT_VALUE_MAP.put(ReminderSettings.KEY_AUTO_RESIZE_TYPE, ReminderSettings.VALUE_AUTO_RESIZE_TYPE_TOP);
    
    DEFAULT_VALUES = new PropertyDefaults(DEFAULT_VALUE_MAP);
  }
  
  public static final PropertyDefaults getPropertyDefaults() {
    return DEFAULT_VALUES;
  }
}

