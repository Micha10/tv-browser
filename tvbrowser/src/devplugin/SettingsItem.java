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
package devplugin;

/**
 * This Class holds a List of SettingItem-IDs. With this it's possible for Plugins to open
 * specific Settings.
 * 
 * E.g.:
 * <code>
 * getPluginManager().showSettings(SettingsItem.TIMEBUTTONS);
 * </code>
 * @author bodum
 * @since 2.2
 */
public class SettingsItem {

  public static final String TIMEBUTTONS = "#timebuttons";
  public static final String PLUGINS = "#plugins";
  public static final String STARTUP = "#startup";
  public static final String PROGRAMINFO = "#programinfo";
  public static final String REMINDER = "#reminder";
  public static final String SEARCH = "#search";
  public static final String FAVORITE = "#favorite";
  public static final String CHANNELS = "#channels";
  public static final String DATA_PLUGIN_POST_PROCESSING = "#dataPluginPostProcessing";
  public static final String WEBBROWSER = "#webbrowser";
  public static final String CONTEXTMENU = "#contextmenu";
  public static final String LOOKANDFEEL = "#lookandfeel";
  public static final String PLUGINPROGRAMFORMAT = "#pluginprogramformat";
  
  /** @since 4.2.3 */
  public static final String I18N = "#i18n";
  /** @since 4.2.3 */
  public static final String TECHNICAL = "#technical";
  /** @since 4.2.3 */
  public static final String LOCALE = "#local";
  /** @since 4.2.3 */
  public static final String GENERIC_PLUGIN_FILTER = "#genericPluginFilter";
  /** @since 4.2.3 */
  public static final String CHANNEL_ICON_NAME = "#channelIconName";
  /** @since 4.2.3 */
  public static final String FONTS = "#fonts";
  /** @since 4.2.3 */
  public static final String NETWORK = "#network";
  /** @since 4.2.3 */
  public static final String PROXY = "#proxy";
  /** @since 4.2.3 */
  public static final String DIRECTORIES = "#directories";

  public static final String PROGRAMPANELLOOK = "#programpanellook";
  public static final String PROGRAMPANELMARKING = "#programpanelmarking";
  public static final String PROGRAMTABLELOOK = "#programtablelook";
  public static final String CENTERPANELSETUP = "#centerpanelsetup";
  
  public static final String PICTURES = "#pictures";
  
  public static final String TRAY = "#tray";
  /** @since 4.2.3 */
  public static final String TRAY_CHANNELS = "#trayChannels";
  /** @since 4.2.3 */
  public static final String TRAY_IMPORTANT = "#trayImportant";
  /** @since 4.2.3 */
  public static final String TRAY_NOW = "#trayNow";
  /** @since 4.2.3 */
  public static final String TRAY_ONTIME = "#trayOnTime";
  /** @since 4.2.3 */
  public static final String TRAY_SOON = "#traySoon";
  
  /**@deprecated since 4.2.3 use {@link #TRAY_ONTIME} instead*/
  @Deprecated(since="4.2.3") public static final String TRAYONTIMEPROGRAMS = TRAY_ONTIME;
  
  public static final String MOUSE = "#mouse";

  private SettingsItem () {}
}