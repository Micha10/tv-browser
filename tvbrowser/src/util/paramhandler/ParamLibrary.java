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
package util.paramhandler;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

import devplugin.Program;
import devplugin.ProgramFieldType;
import util.browserlauncher.Launch;
import util.i18n.Localizer;
import util.misc.TextLineBreakerStringWidth;

/**
 * The default ParamLibrary. If you want to add new parameters or functions for your
 * plugin, extend this class and override the public methods. For an example
 * see the code in the CapturePlugin
 *
 * @author bodum
 */
public class ParamLibrary {
  private static final String KEY_TITLE = "title";
  private static final String KEY_ORIGINAL_TITLE = "original_title";
  private static final String KEY_START_DAY = "start_day";
  private static final String KEY_START_MONTH = "start_month";
  private static final String KEY_START_YEAR = "start_year";
  private static final String KEY_START_HOUR = "start_hour";
  private static final String KEY_START_MINUTE = "start_minute";
  private static final String KEY_END_MONTH = "end_month";
  private static final String KEY_END_YEAR = "end_year";
  private static final String KEY_END_DAY = "end_day";
  private static final String KEY_END_HOUR = "end_hour";
  private static final String KEY_END_MINUTE = "end_minute";
  private static final String KEY_LENGTH_MINUTES = "length_minutes";
  private static final String KEY_LENGTH_SECONDS = "length_sec";
  private static final String KEY_SHORT_INFO = "short_info";
  private static final String KEY_DESCRIPTION = "description";
  private static final String KEY_EPISODE = "episode";
  private static final String KEY_EPISODE_ORIGINAL = "original_episode";
  private static final String KEY_EPISODE_NUMBER = "episode_number";
  private static final String KEY_CHANNEL_NAME = "channel_name";
  private static final String KEY_CHANNEL_SORT_NUMBER = "channel_sort_number";
  private static final String KEY_URL = "url";
  private static final String KEY_START_DAY_OF_WEEK = "start_day_of_week";
  private static final String KEY_START_MONTH_NAME = "start_month_name";
  private static final String KEY_GENRE = "genre";
  private static final String KEY_START_UNIX = "start_unix";
  private static final String KEY_END_UNIX = "end_unix";
  private static final String KEY_CUSTOM = "custom";
  private static final String KEY_PRODUCTION_YEAR = "production_year";
  private static final String KEY_ACTORS = "actors";
  private static final String KEY_ORIGIN = "origin";
  private static final String KEY_SEASON_NUMBER = "season_number";
  
  private static final String FUNCTION_ISSET = "isset";
  private static final String FUNCTION_URLENCODE = "urlencode";
  private static final String FUNCTION_CONCAT = "concat";
  private static final String FUNCTION_CLEAN = "clean";
  private static final String FUNCTION_CLEAN_LESS = "cleanLess";
  private static final String FUNCTION_LEADING_ZERO = "leadingZero";
  private static final String FUNCTION_SPLIT_AT = "splitAt";
  private static final String FUNCTION_TESTPARAM = "testparam";
  private static final String FUNCTION_MAX_LENGTH = "maxlength";
  private static final String FUNCTION_REPLACE = "replace";
  private static final String FUNCTION_REPLACE_LINE_FEED = "replaceNewline";
  private static final String FUNCTION_ESCAPE_QUOTES = "escapeQuotes";
  
  private static final String[] KEY_ARRAY = { KEY_TITLE, KEY_ORIGINAL_TITLE, KEY_START_DAY, KEY_START_MONTH, KEY_START_YEAR, KEY_START_HOUR, KEY_START_MINUTE,
      KEY_END_MONTH, KEY_END_YEAR, KEY_END_DAY, KEY_END_HOUR, KEY_END_MINUTE, KEY_LENGTH_MINUTES, KEY_LENGTH_SECONDS, KEY_SHORT_INFO,
      KEY_DESCRIPTION, KEY_EPISODE, KEY_EPISODE_ORIGINAL, KEY_EPISODE_NUMBER, KEY_CHANNEL_NAME, KEY_CHANNEL_SORT_NUMBER, KEY_URL,
      KEY_START_DAY_OF_WEEK, KEY_START_MONTH_NAME, KEY_GENRE, KEY_START_UNIX, KEY_END_UNIX, KEY_CUSTOM, KEY_PRODUCTION_YEAR, KEY_ACTORS,
      KEY_ORIGIN,KEY_SEASON_NUMBER};
  
  private static final String[] FUNCTION_ARRAY = { FUNCTION_ISSET, FUNCTION_URLENCODE, FUNCTION_CONCAT, FUNCTION_CLEAN, FUNCTION_CLEAN_LESS, FUNCTION_LEADING_ZERO,
      FUNCTION_SPLIT_AT, FUNCTION_TESTPARAM, FUNCTION_MAX_LENGTH, FUNCTION_REPLACE, FUNCTION_REPLACE_LINE_FEED, FUNCTION_ESCAPE_QUOTES};
  
  /** Translator */
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(ParamLibrary.class);

  /** True if an Error occurred */
  private boolean mError = false;

  /** The Error */
  private String mErrorString = "";

  /**
   * Has an Error occurred ?
   *
   * @return true if an error occurred
   */
  public boolean hasErrors() {
    return mError;
  }

  /**
   * Set the Error-Boolean
   *
   * @param errors True, if an error occurred
   */
  public void setErrors(boolean errors) {
    mError = errors;
  }

  /**
   * Returns the Error, empty if none occurred
   * @return error message
   */
  public String getErrorString() {
    return mErrorString;
  }

  /**
   * Set the Error
   *
   * @param error the Error
   */
  public void setErrorString(String error) {
    mErrorString = error;
  }

  /**
   * Get the possible Keys
   *
   * @return Array with possible Keys
   */
  public String[] getPossibleKeys() {
    return KEY_ARRAY;
  }

  /**
   * Get the description for one Key
   * @param key The key
   *
   * @return description for one key
   */
  public String getDescriptionForKey(String key) {
    String translation = LOCALIZER.msg("parameter_" + key, "");
    if (translation.startsWith("[ParamLibrary.parameter")) {
      return LOCALIZER.msg("noDescription", "No Description available");
    }
    return translation;
  }

  /**
   * Get the List of possible Functions
   *
   * @return List of possible Functions
   */
  public String[] getPossibleFunctions() {
    return FUNCTION_ARRAY;
  }

  /**
   * Get the description for a specific Function
   * @param function parameter function
   *
   * @return localized description string
   */
  public String getDescriptionForFunctions(String function) {

    String translation = LOCALIZER.msg("function_" + function, "");
    if (translation.startsWith("[ParamLibrary.function")) {
      return LOCALIZER.msg("noDescription", "No Description available");
    }

    return translation;
  }

  /**
   * Get the String for a key
   *
   * @param program Program to use
   * @param key Key to use
   * @return Value of key in program
   */
  public String getStringForKey(Program program, String key) {
    if (key.equalsIgnoreCase(KEY_TITLE)) {
      return program.getTitle();
    } else if (key.equalsIgnoreCase(KEY_START_DAY)) {
      return String.valueOf(program.getDate().getDayOfMonth());
    } else if (key.equalsIgnoreCase(KEY_START_MONTH)) {
      return String.valueOf(program.getDate().getMonth());
    } else if (key.equalsIgnoreCase(KEY_START_YEAR)) {
      return String.valueOf(program.getDate().getYear());
    } else if (key.equalsIgnoreCase(KEY_END_DAY)) {
      return String.valueOf(getEndTimeFieldInProgram(program, Calendar.DAY_OF_MONTH));
    } else if (key.equalsIgnoreCase(KEY_END_MONTH)) {
      return String.valueOf(getEndTimeFieldInProgram(program, Calendar.MONTH) + 1);
    } else if (key.equalsIgnoreCase(KEY_END_YEAR)) {
      return String.valueOf(getEndTimeFieldInProgram(program, Calendar.YEAR));
    } else if (key.equalsIgnoreCase(KEY_START_HOUR)) {
      return String.valueOf(program.getHours());
    } else if (key.equalsIgnoreCase(KEY_START_MINUTE)) {
      return String.valueOf(program.getMinutes());
    } else if (key.equalsIgnoreCase(KEY_END_HOUR)) {
      return String.valueOf(getEndTimeFieldInProgram(program, Calendar.HOUR_OF_DAY));
    } else if (key.equalsIgnoreCase(KEY_END_MINUTE)) {
      return String.valueOf(getEndTimeFieldInProgram(program, Calendar.MINUTE));
    } else if (key.equalsIgnoreCase(KEY_LENGTH_MINUTES)) {
      return String.valueOf(program.getLength());
    } else if (key.equalsIgnoreCase(KEY_LENGTH_SECONDS)) {
      return String.valueOf(program.getLength() * 60);
    } else if (key.equalsIgnoreCase(KEY_SHORT_INFO)) {
      return removeNull(program.getShortInfo());
    } else if (key.equalsIgnoreCase(KEY_DESCRIPTION)) {
      String res = removeNull(program.getDescription());
      String copyright = program.getChannel().getCopyrightNotice();
	    if (copyright != null) {
        return new StringBuilder(res).append('\n').append(copyright).toString();
      }
      return res;
    } else if (key.equalsIgnoreCase(KEY_CHANNEL_NAME)) {
      return removeNull(program.getChannel().getName());
    } else if (key.equalsIgnoreCase(KEY_CHANNEL_SORT_NUMBER)) {
      return removeNull(program.getChannel().getSortNumber());
    } else if (key.equalsIgnoreCase(KEY_START_DAY_OF_WEEK)) {
      SimpleDateFormat format = new SimpleDateFormat("EEEE");
      return format.format(new java.util.Date(program.getDate().getCalendar().getTimeInMillis()));
    } else if (key.equalsIgnoreCase(KEY_START_MONTH_NAME)) {
      SimpleDateFormat format = new SimpleDateFormat("MMMM");
      return format.format(new java.util.Date(program.getDate().getCalendar().getTimeInMillis()));
    } else if (key.equalsIgnoreCase(KEY_START_UNIX)) {
      return Long.toString(createStartTime(program).getTimeInMillis() / 1000);
    } else if (key.equalsIgnoreCase(KEY_END_UNIX)) {
      return Long.toString(createEndTime(program).getTimeInMillis() / 1000);
    } else if (key.equalsIgnoreCase(KEY_EPISODE_NUMBER)) {
      int epNum = program.getIntField(ProgramFieldType.EPISODE_NUMBER_TYPE);
      if (epNum == -1) {
        return "";
      }
      return Integer.toString(epNum);
    } else if (key.equalsIgnoreCase(KEY_PRODUCTION_YEAR)) {
      int productionYear = program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE);
      if (productionYear < 1800) {
        return "";
      }
      return Integer.toString(productionYear);
    } else if (key.equalsIgnoreCase(KEY_GENRE)) {
      return removeNull(program.getTextField(ProgramFieldType.GENRE_TYPE));
    } else if (key.equalsIgnoreCase(KEY_ORIGIN)) {
      return removeNull(program.getTextField(ProgramFieldType.ORIGIN_TYPE));
    } else if (key.equalsIgnoreCase(KEY_ORIGINAL_TITLE)) {
      return removeNull(program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE));
    } else if (key.equalsIgnoreCase(KEY_URL)) {
      return removeNull(program.getTextField(ProgramFieldType.URL_TYPE));
    } else if (key.equalsIgnoreCase(KEY_EPISODE)) {
      return removeNull(program.getTextField(ProgramFieldType.EPISODE_TYPE));
    } else if (key.equalsIgnoreCase(KEY_EPISODE_ORIGINAL)) {
      return removeNull(program.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE));
    } else if (key.equalsIgnoreCase(KEY_CUSTOM)) {
      return removeNull(program.getTextField(ProgramFieldType.CUSTOM_TYPE));
    } else if (key.equalsIgnoreCase(KEY_SEASON_NUMBER)) {
      return removeNull(program.getIntFieldAsString(ProgramFieldType.SEASON_NUMBER_TYPE));
    } else if (key.equalsIgnoreCase(KEY_ACTORS)) {
      return removeNull(program.getTextField(ProgramFieldType.ACTOR_LIST_TYPE));
    } else {
      try {
        Field f = ProgramFieldType.class.getDeclaredField(key.toUpperCase());
        f.setAccessible(true);
        ProgramFieldType value = (ProgramFieldType)f.get(null);
        
        if(value != null) {
          switch(value.getFormat()) {
            case ProgramFieldType.FORMAT_INT: return removeNull(program.getIntFieldAsString(value));
            case ProgramFieldType.FORMAT_TEXT: return removeNull(program.getTextField(value));
          }
        }
      } catch (Exception e) {
        // ignore
      }
    }

    mError = true;
    mErrorString = LOCALIZER.msg("unkownParam", "Unknown Parameter") + ": '" + key + "'";

    return null;
  }

  /**
   * If the string is null, it returns "".
   *
   * @param str String
   * @return "" if str is null, otherwise str
   */
  private String removeNull(String str) {
    if (str == null) {
      str = "";
    }
    return str;
  }

  /**
   * Returns a Calendar-Field of the End-Time from a Program
   *
   * @param prg Program
   * @param field Calendar-Field to return
   * @return int-Value
   */
  private int getEndTimeFieldInProgram(Program prg, int field) {
    Calendar c = createEndTime(prg);
    return c.get(field);
  }

  /**
   * Creates a calendar instance containing the start time
   *
   * @param prg get Start-Time of this Item
   * @return Start-Time
   */
  private Calendar createStartTime(Program prg) {
    Calendar c = (Calendar) prg.getDate().getCalendar().clone();
    c.set(Calendar.HOUR_OF_DAY, prg.getHours());
    c.set(Calendar.MINUTE, prg.getMinutes());
    c.set(Calendar.SECOND, 0);
    return c;
  }

  /**
   * Creates a calendar instance containing the end time
   *
   * @param prg get End-Time of this Item
   * @return End-Time
   */
  private Calendar createEndTime(Program prg) {
    Calendar c = (Calendar) prg.getDate().getCalendar().clone();

    c.set(Calendar.HOUR_OF_DAY, prg.getHours());
    c.set(Calendar.MINUTE, prg.getMinutes());
    c.add(Calendar.MINUTE, prg.getLength());
    c.set(Calendar.SECOND, 0);
    return c;
  }

  /**
   * Returns the Value of a function
   *
   * @param prg Program to use
   * @param function Function to use
   * @param params Params for the Function
   * @return Return-Value of Function
   */
  public String getStringForFunction(Program prg, String function, String[] params) {
    if (function.equalsIgnoreCase(FUNCTION_ISSET)) {
      if (params.length != 2) {
        mError = true;
        mErrorString = LOCALIZER.msg("isset2Params", "isset needs 2 Parameters");
        return null;
      }

      if ((params[0] != null) && (params[0].length() > 0)) {
        return params[0];
      }

      return params[1];
    } else if (function.equalsIgnoreCase(FUNCTION_TESTPARAM)) {
      if ((params.length < 2) || ((params.length > 3))) {
        mError = true;
        mErrorString = LOCALIZER.msg("testparam2Params", "testparam needs 2-3 Parameters");
        return null;
      }

      if ((params[0] != null) && (params[0].length() > 0)) {
        return params[1];
      }

      if (params.length == 3) {
        return params[2];
      }

      return "";
    } else if (function.equalsIgnoreCase(FUNCTION_URLENCODE)) {
      if (params.length != 2) {
        mError = true;
        mErrorString = LOCALIZER.msg("urlencode2Params", "urlencode needs 2 Parameters");
        return null;
      }

      try {
        return URLEncoder.encode(params[0], params[1]);
      } catch (Exception e) {
        mError = true;
        mErrorString = LOCALIZER.msg("urlencodeProblems", "Problems with encoding : ") + e.toString();
        return null;
      }
    } else if (function.equalsIgnoreCase(FUNCTION_CONCAT)) {
      StringBuilder buffer = new StringBuilder();

      for (String param : params) {
        buffer.append(param);
      }
      return buffer.toString();
    } else if (function.equalsIgnoreCase(FUNCTION_CLEAN)) {
      StringBuilder buffer = new StringBuilder();

      for (String param : params) {
        buffer.append(clean(param));
      }
      return buffer.toString();
    } else if (function.equalsIgnoreCase(FUNCTION_CLEAN_LESS)) {
      StringBuilder buffer = new StringBuilder();

      for (String param : params) {
        buffer.append(cleanLess(param));
      }
      return buffer.toString();
    } else if (function.equalsIgnoreCase(FUNCTION_LEADING_ZERO)) {
      if (params.length > 2) {
        mError = true;
        mErrorString = LOCALIZER.msg("leadingZero2Params", "leadingZero has max. 2 Parameters");
        return null;
      }

      int num = 2;

      if (params.length == 2) {
        try {
          num = Integer.parseInt(params[1]);
        } catch (Exception ex) {
          mError = true;
          mErrorString = LOCALIZER.msg("leadingZeroProblems", "Could not parse Number") + " : " + params[1];
          return null;
        }
      }

      return addLeadingZeros(params[0], num);
    } else if (function.equalsIgnoreCase(FUNCTION_SPLIT_AT)) {
      if (params.length != 2) {
        mError = true;
        mErrorString = LOCALIZER.msg("splitAt2Params", "splitAt needs 2 Parameters");
        return null;
      }

      int num = 2;

      try {
        num = Integer.parseInt(params[1]);
      } catch (Exception ex) {
        mError = true;
        mErrorString = LOCALIZER.msg("splitAtNumberProblems", "Could not parse Number") + " : " + params[1];
        return null;
      }

      TextLineBreakerStringWidth breaker = new TextLineBreakerStringWidth();

      StringBuilder result = new StringBuilder();

      try {
        String[] lines = breaker.breakLines(new StringReader(params[0]), num);

        for (String line : lines) {
          result.append(line);
          result.append('\n');
        }

      } catch (Exception ex) {
        mError = true;
        mErrorString = LOCALIZER.msg("splitAtSplitProblems", "Could not split String") + " :\n " + ex.toString();
        return null;
      }

      return result.toString().trim();
    } else if (function.equalsIgnoreCase(FUNCTION_MAX_LENGTH)) {
      if (params.length != 2) {
        mError = true;
        mErrorString = LOCALIZER.msg("maxlength2Params", "maxlength needs 2 Parameters");
        return null;
      }

      int num = -1;

      try {
        num = Integer.parseInt(params[1]);
      } catch (Exception ex) {
        mError = true;
        mErrorString = LOCALIZER.msg("maxlengthNumberProblems", "Could not parse Number") + " : " + params[1];
        return null;
      }

      String result = params[0];

      if (result.length() > num) {
        result = result.substring(0, num);
      }

      return result;
    } else if(function.equalsIgnoreCase(FUNCTION_REPLACE)) {
    	if(params.length != 2) {
    		mError = true;
            mErrorString = LOCALIZER.msg("replace2Params", "replace needs 2 Parameters");
            return null;
    	}
    	
    	String haystack = params[0];
    	
    	String[] replaceValues = params[1].split(",");
    	
    	for(String replace : replaceValues) {
    		if(!replace.contains("::")) {
    		  mError = true;
    		  mErrorString = LOCALIZER.msg("replaceMissingColon", "Replace values need to contain two following colons");
              
    		  return null;
    		}
    		
    		String[] parts = replace.split("::");
    		
    		if(parts.length == 1) {
    		  haystack = haystack.replace(parts[0], "");
    		}
    		else {
    		  haystack = haystack.replace(parts[0], parts[1]);
    		}
    	}
    	return haystack;
    }
    else if(function.equalsIgnoreCase(FUNCTION_REPLACE_LINE_FEED)) {
      return params[0].replaceAll("\\r*\\n", " ").strip();
    }
    else if(function.equalsIgnoreCase(FUNCTION_ESCAPE_QUOTES)) {
      return Launch.getOs() == Launch.OS_WINDOWS ? params[0].replace("\"", "\\\"\\\"") : params[0].replace("\"", "\\\"");
    }

    mError = true;
    mErrorString = LOCALIZER.msg("unknownFunction", "Unknown function : {0}", function);

    return null;
  }

  /**
   * Adds leading Zeros to the String
   *
   * @param string
   * @param num
   * @return
   */
  private String addLeadingZeros(String string, int num) {
    StringBuilder buffer = new StringBuilder(string);

    while (buffer.length() < num) {
      buffer.insert(0, '0');
    }

    return buffer.toString();
  }

  /**
   * Clean a String. Replace every non A-Za-z0-9 Char into a "_"
   *
   * @param clean String to clean
   * @return cleaned String
   */
  private String clean(String clean) {
    StringBuilder buffer = new StringBuilder();
    char[] chars = clean.trim().toCharArray();

    for (char c : chars) {
      if ((c >= 'A') && (c <= 'Z')) {
        buffer.append(c);
      } else if ((c >= 'a') && (c <= 'z')) {
        buffer.append(c);
      } else if ((c >= '0') && (c <= '9')) {
        buffer.append(c);
      } else {
        buffer.append('_');
      }
    }

    String retStr = buffer.toString();

    while (retStr.indexOf("__") >= 0) {
      retStr = StringUtils.replace(retStr, "__", "_");
    }

    return retStr;
  }

  /**
   * Clean a String. Replace every non non Char/Digit into "_". ÄÖÜ and
   * other Locale Letters will remain.
   *
   * @param clean String to clean
   * @return cleaned String
   */
  private String cleanLess(String clean) {
    StringBuilder buffer = new StringBuilder();
    char[] chars = clean.trim().toCharArray();

    for (char c : chars) {
      if (Character.isDigit(c) || Character.isLetter(c)) {
        buffer.append(c);
      } else {
        buffer.append('_');
      }
    }

    String retStr = buffer.toString();

    while (retStr.indexOf("__") >= 0) {
      retStr = StringUtils.replace(retStr, "__", "_");
    }

    return retStr;
  }

}