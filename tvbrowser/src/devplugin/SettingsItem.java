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

import tvbrowser.core.Settings;

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

  public static final String TIMEBUTTONS = Settings.Buttons.ID;
  public static final String GENRAL = Settings.General.ID;
  public static final String PLUGINS = Settings.Plugins.ID;
  /**@deprecated since 4.2.5 use {@link #GENRAL} instead */
  @Deprecated(since = "4.2.5") public static final String STARTUP = GENRAL;
  public static final String PROGRAMINFO = "#programinfo";
  public static final String REMINDER = "#reminder";
  public static final String SEARCH = "#search";
  public static final String FAVORITE = "#favorite";
  public static final String CHANNELS = Settings.Channels.ID;
  public static final String DATA_PLUGIN_POST_PROCESSING = Settings.DataPostProcessing.ID;
  public static final String WEBBROWSER = Settings.WebBrowser.ID;
  public static final String CONTEXTMENU = Settings.ContextMenu.ID;
  public static final String LOOKANDFEEL = Settings.LookAndFeel.ID;
  public static final String PLUGINPROGRAMFORMAT = "#pluginprogramformat";
  
  /** @since 4.2.5 */
  public static final String TOOLBAR = Settings.ToolBar.ID;
  
  /** @since 4.2.3 */
  public static final String I18N = "#i18n";
  /** @since 4.2.3 */
  public static final String TECHNICAL = "#technical";
  /** @since 4.2.3 */
  public static final String LOCALE = Settings.Locales.ID;
  /** @since 4.2.3 */
  public static final String GENERIC_PLUGIN_FILTER = "#genericPluginFilter";
  /** @since 4.2.3 */
  public static final String CHANNEL_ICON_NAME = Settings.IconAndNames.ID;
  /** @since 4.2.3 */
  public static final String FONTS = Settings.Fonts.ID;
  /** @since 4.2.3 */
  public static final String NETWORK = Settings.Network.ID;
  /** @since 4.2.3 */
  public static final String PROXY = Settings.Proxy.ID;
  /** @since 4.2.3 */
  public static final String DIRECTORIES = Settings.Directories.ID;

  public static final String PROGRAMPANELLOOK = Settings.ProgramPanel.ID;
  public static final String PROGRAMPANELMARKING = Settings.MarkingsProgramPanel.ID;
  public static final String PROGRAMTABLELOOK = Settings.ProgramTable.ID;
  public static final String CENTERPANELSETUP = Settings.CenterPanels.ID;
  
  public static final String PICTURES = Settings.Pictures.ID;
  
  public static final String TRAY = Settings.Tray.ID;
  /** @since 4.2.3 */
  public static final String TRAY_CHANNELS = Settings.Tray.Channels.ID;
  /** @since 4.2.3 */
  public static final String TRAY_IMPORTANT = Settings.Tray.Important.ID;
  /** @since 4.2.3 */
  public static final String TRAY_NOW = Settings.Tray.Now.ID;
  /** @since 4.2.3 */
  public static final String TRAY_ONTIME = Settings.Tray.OnTime.ID;
  /** @since 4.2.3 */
  public static final String TRAY_SOON = Settings.Tray.Soon.ID;
  
  /**@deprecated since 4.2.3 use {@link #TRAY_ONTIME} instead*/
  @Deprecated(since="4.2.3") public static final String TRAYONTIMEPROGRAMS = TRAY_ONTIME;
  
  public static final String MOUSE = Settings.Mouse.ID;

  private SettingsItem () {}
}