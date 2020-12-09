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

package util.ui;

import java.util.HashMap;
import java.util.Locale;

/**
 * Does the localization of texts.
 * <p>
 * Each class that uses String that reach the user interface has its own
 * Localizer. You can get a localized String by using one of the msg methods.
 * <p>
 * The msg methods have all the same pattern:<br>
 * <code>String msg(key, defaultValue, [args]);</code>
 * <ul>
 * <li>The <CODE>key</CODE> is a String that identifies the message. Each class
 *     has its own namespace.</li>
 * <li>The defaultValue is the value you would expect on an English system. (But
 *     note: Even on an English system you may get a different String!)</li>
 * <li>The optional <CODE>args</CODE> are arguments that will be parsed into the
 *     message. See {@link java.text.MessageFormat} for details.
 * </li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 * @deprecated since 4.2.2 use {@link #util.i18n.Localizer} instead.
 */
@Deprecated(since="4.2.2") public class Localizer {
  /** Contains for a Class (key) a Localizer (value). */
  private static final HashMap<Class<?>, Localizer> mLocalizerCache = new HashMap<Class<?>, Localizer>();
  
  public final static String I18N_OK = util.i18n.Localizer.I18N_OK;
  public final static String I18N_CANCEL = util.i18n.Localizer.I18N_CANCEL;
  public final static String I18N_CLOSE = util.i18n.Localizer.I18N_CLOSE;
  public final static String I18N_COPY = util.i18n.Localizer.I18N_COPY;
  public final static String I18N_DELETE = util.i18n.Localizer.I18N_DELETE;
  public final static String I18N_EDIT = util.i18n.Localizer.I18N_EDIT;
  public final static String I18N_PROGRAM = util.i18n.Localizer.I18N_PROGRAM;
  public final static String I18N_PROGRAMS = util.i18n.Localizer.I18N_PROGRAMS;
  public final static String I18N_CHANNEL = util.i18n.Localizer.I18N_CHANNEL;
  public final static String I18N_CHANNELS = util.i18n.Localizer.I18N_CHANNELS;
  public final static String I18N_HELP = util.i18n.Localizer.I18N_HELP;
  public final static String I18N_FILE = util.i18n.Localizer.I18N_FILE;
  public final static String I18N_ADD = util.i18n.Localizer.I18N_ADD;
  public final static String I18N_SETTINGS = util.i18n.Localizer.I18N_SETTINGS;
  public final static String I18N_UP = util.i18n.Localizer.I18N_UP;
  public final static String I18N_DOWN = util.i18n.Localizer.I18N_DOWN;
  public final static String I18N_LEFT = util.i18n.Localizer.I18N_LEFT;
  public final static String I18N_RIGHT = util.i18n.Localizer.I18N_RIGHT;
  public final static String I18N_BACK = util.i18n.Localizer.I18N_BACK;
  public final static String I18N_NEXT = util.i18n.Localizer.I18N_NEXT;
  public final static String I18N_PICTURES = util.i18n.Localizer.I18N_PICTURES;
  public final static String I18N_OPTIONS = util.i18n.Localizer.I18N_OPTIONS;
  public final static String I18N_SELECT = util.i18n.Localizer.I18N_SELECT;
  public final static String I18N_SELECT_ALL = util.i18n.Localizer.I18N_SELECT_ALL;
  public final static String I18N_CLEAR_SELECTION = util.i18n.Localizer.I18N_CLEAR_SELECTION;
  public final static String I18N_ERROR = util.i18n.Localizer.I18N_ERROR;
  public final static String I18N_DEFAULT = util.i18n.Localizer.I18N_DEFAULT;
  public final static String I18N_STANDARD = util.i18n.Localizer.I18N_STANDARD;
  public final static String I18N_YESTERDAY = util.i18n.Localizer.I18N_YESTERDAY;
  public final static String I18N_TODAY = util.i18n.Localizer.I18N_TODAY;
  public final static String I18N_TOMORROW = util.i18n.Localizer.I18N_TOMORROW;
  public final static String I18N_INFO = util.i18n.Localizer.I18N_INFO;
  public final static String I18N_WARNING = util.i18n.Localizer.I18N_WARNING;
  
  /**
   * ellipsis suffix for use in menus
   */
  private static final String ELLIPSIS = "...";

  
  private util.i18n.Localizer mLocalizer;
  
  /**
   * Creates a new instance of Localizer.
   *
   * @param clazz The Class to create the Localizer for.
   */
  protected Localizer(final Class<?> clazz) {
    initializeForClass(clazz);
  }

  protected void initializeForClass(final Class<?> clazz) {
    mLocalizer = util.i18n.Localizer.getLocalizerFor(clazz);
  }
  
  protected static Localizer getCachedLocalizerFor(final Class<?> clazz) {
    return mLocalizerCache.get(clazz);
  }

  /**
   * Gets the Localizer for the specified Class.
   *
   * @param clazz The Class to get the localizer for.
   * @return the Localizer for the specified Class.
   */
  public static Localizer getLocalizerFor(final Class<?> clazz) {
    Localizer localizer = getCachedLocalizerFor(clazz);

    if (localizer == null) {
      localizer = new Localizer(clazz);
      addLocalizerToCache(clazz, localizer);
    }

    return localizer;
  }

  protected static void addLocalizerToCache(final Class<?> clazz, final Localizer localizer) {
    mLocalizerCache.put(clazz, localizer);
  }


  /**
   * Clears the localizer cache.
   */
  public static void emptyLocalizerCache() {
    mLocalizerCache.clear();
  }

  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message (English)
   * @param arg1 The argument that should replace <CODE>{0}</CODE>.
   * @return a localized message.
   */
  public String msg(final String key, final String defaultMsg, final Object arg1) {
    return mLocalizer.ellipsisMsg(key, defaultMsg, arg1);
  }



  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message. (English)
   * @param arg1 The argument that should replace <CODE>{0}</CODE>.
   * @param arg2 The argument that should replace <CODE>{1}</CODE>.
   * @return a localized message.
   */
  public String msg(final String key, final String defaultMsg, final Object arg1, final Object arg2) {
    return mLocalizer.msg(key, defaultMsg, arg1, arg2);
  }



  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message. (English)
   * @param arg1 The argument that should replace <CODE>{0}</CODE>.
   * @param arg2 The argument that should replace <CODE>{1}</CODE>.
   * @param arg3 The argument that should replace <CODE>{2}</CODE>.
   * @return a localized message.
   */
  public String msg(final String key, final String defaultMsg, final Object arg1, final Object arg2,
    final Object arg3)
  {
    return mLocalizer.msg(key, defaultMsg, arg1, arg2, arg3);
  }



  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message. (English)
   * @param args The arguments that should replace the appropriate place holder.
   *        See {@link java.text.MessageFormat} for details.
   * @return a localized message.
   */
  public String msg(final String key, final String defaultMsg, final Object[] args) {
    return mLocalizer.msg(key, defaultMsg, args);
  }



  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message (English)
   * @return a localized message.
   */
  public String msg(final String key, final String defaultMsg) {
    return mLocalizer.msg(key,defaultMsg,true);
  }

  /**
   * Gets a localized message.
   *
   * @param key The key of the message.
   * @param defaultMsg The default message (English)
   * @param warn If warnings should be logged if key is not found.
   * @return a localized message.
   * @since 2.5.1
   */
  public String msg(String key, final String defaultMsg, final boolean warn) {
    return mLocalizer.msg(key, defaultMsg, warn);
  }

  /**
   * Scans all Language-Directories for different Versions of tvbrowser/tvbrowser.properties.
   *
   * This is faster than analyzing all Files
   *
   * @return all available Locales
   * @since 2.3
   */
  public Locale[] getAllAvailableLocales() {
    return mLocalizer.getAllAvailableLocales();
  }

  /**
   * Get the Locale for a specific String.
   * The String is in this format: "lang_country_variant"
   *
   * @param string String with Locale
   * @return Locale
   */
  public static Locale getLocaleForString(final String string) {
    return util.i18n.Localizer.getLocaleForString(string);
  }

  /**
   * get a standard localization
   * @param key one of the constant values defined in the Localizer class
   * @return localized message for key
   */
  public static String getLocalization(final String key) {
    return util.i18n.Localizer.getLocalization(key);
  }

  /**
   * get a standard localization with ellipsis as suffix
   * @param key one of the constant values defined in the Localizer class
   * @return localized message for key
   */
  public static String getEllipsisLocalization(final String key) {
    return ellipsisSuffix(getLocalization(key));
  }
  
  /**
   * get a localized message with an ellipsis as suffix
   * @param key localization key
   * @param defaultMessage default (English) message
   * @return localized message
   * @since 3.0
   */
  public String ellipsisMsg(final String key, final String defaultMessage) {
    return mLocalizer.ellipsisMsg(key, defaultMessage);
  }

  private static String ellipsisSuffix(String msg) {
    if (msg.endsWith(ELLIPSIS)) {
      msg = msg.substring(0, msg.length() - ELLIPSIS.length()).trim(); // this is done to also correct the space before the ellipsis
    }
    return msg + ELLIPSIS;
  }

  /**
   * Gets a localized message ending with ellipsis suffix
   *
   * @param key The key of the message.
   * @param defaultMsg The default message (English)
   * @param arg1 The argument that should replace <CODE>{0}</CODE>.
   * @return a localized message.
   */
  public String ellipsisMsg(final String key, final String defaultMsg, final Object arg1) {
    return mLocalizer.ellipsisMsg(key, defaultMsg, arg1);
  }

  /**
   * check if a given message key exists
   * @param key The key
   * @return true if the given key exists
   * @since 3.0
   */
  public boolean hasMessage(final String key) {
    return mLocalizer.hasMessage(key);
  }

  /**
   * return the given String with an ellipsis appended
   * @param someString The string.
   * @return ellipsis appended String or <code>null</code>
   * @since 3.0
   */
  public String ellipsis(final String someString) {
    return mLocalizer.ellipsis(someString);
  }
}
