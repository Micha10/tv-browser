/*
 * TV-Browser
 * Copyright (C) 2003-2021 TV-Browser-Team (dev@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
 *     $Date: 2018-09-24 11:11:01 +0200 (Mo, 24 Sep 2018) $
 *     $Id: PluginBaseInfo.java 8894 2018-09-24 09:11:01Z ds10 $
 *   $Author: ds10 $
 * $Revision: 8894 $
 */
package devplugin;

import java.awt.Color;

import javax.swing.UIManager;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.ui.settings.GeneralSettingsTab;

/**
 * Working implementation of devplugin.TvBrowserSettings
 * 
 * @author René Mach
 * @since 4.2.3
 */
public final class TvBrowserSettingsImpl implements TvBrowserSettings {
  private static TvBrowserSettingsImpl INSTANCE;
  
  private TvBrowserSettingsImpl() {}
  
  public TvBrowserSettingsImpl(PluginManagerImpl pManager) throws IllegalAccessException {
    if(pManager != null && INSTANCE == null) {
      INSTANCE = new TvBrowserSettingsImpl();
    }
    else {
      throw new IllegalAccessException("Only PluginManagerImpl can create an instance of this class");
    }
  }
  
  public String getTvBrowserUserHome() {
    return Settings.getUserSettingsDirName();
  }

  public int[] getTimeButtonTimes() {
    return Settings.Buttons.TIME_BUTTONS.getIntArray();
  }

  public Date getLastDownloadDate() {
    return Settings.Data.DOWNLOAD_DATE_LAST.getDate();
  }
  
  public int getDefaultNetworkConnectionTimeout(){
    return Settings.Network.DEFAULT_CONNECTION_TIMEOUT.getInt();
  }
  
  public Color getColorForMarkingPriority(int priority) {
    Color result = null;
    
    if(priority > Settings.getHighlightingPriorityMaximum()) {
      priority = Settings.getHighlightingPriorityMaximum();
    }
    
    if(priority == Program.PRIORITY_MARK_NONE) {
      result = new Color(255,255,255,0);
    }
    else if(priority > Program.PRIORITY_MARK_NONE) {
      result = Settings.getHighlightingColorForPriority(priority);
    }
    
    return result;
  }
  
  public int getProgramTableEndOfDay() {
    return Settings.ProgramTable.END_OF_DAY.getInt();
  }

  public int getProgramTableStartOfDay() {
    return Settings.ProgramTable.START_OF_DAY.getInt();
  }
  
  public Color getProgramPanelOnAirLightColor() {
    return Settings.ProgramPanel.COLOR_ON_AIR_LIGHT.getColor();
  }
  
  public Color getProgramPanelOnAirDarkColor() {
    return Settings.ProgramPanel.COLOR_ON_AIR_DARK.getColor();
  }

  public boolean isMarkingBorderPainted() {
    return Settings.Markings.WITH_MARKINGS_SHOWING_BORDER.getBoolean();
  }

  public boolean isUsingExtraSpaceForMarkIcons() {
    return Settings.Markings.USES_EXTRA_SPACE_FOR_MARK_ICONS.getBoolean();
  }

  public short getAutoDownloadWaitingTime() {
    return Settings.General.AUTO_DOWNLOAD_WAITING_TIME.getShort();
  }

  @Override
  public Color getProgramTableMouseOverColor() {
    return Settings.ProgramTable.MOUSE_OVER.getBoolean() ? Settings.ProgramTable.COLOR_MOUSE_OVER.getColor() : null;
  }
  
  @Override
  public Color getProgramTableForegroundColor() {
    return Settings.ProgramTable.STYLE_BACKGROUND.getString().contains("ui") ? UIManager.getColor("List.foreground") : Settings.ProgramPanel.COLOR_FOREGROUND.getColor();
  }
  
  @Override
  public Color getProgramPanelSelectionColor() {
    return Settings.ProgramPanel.COLOR_KEYBOARD_SELECTED.getColor();
  }

  @Override
  public String getTimePattern() {
    return Settings.getTimePattern();
  }

  @Override
  public boolean isChannelUpdateActivated() {
    return Settings.General.AUTO_CHANNEL_UPDATE_PERIOD.getInt() > GeneralSettingsTab.VALUE_AUTO_CHANNEL_UPDATE_DISABLED;
  }

  @Override
  public Color getScrollColorTimeLight() {
    return Settings.ProgramTable.COLOR_SCROLL_TO_TIME_PROGRAMS_BACKGROUND_LIGHT.getColor();
  }

  @Override
  public Color getScrollColorTimeDark() {
    return Settings.ProgramTable.COLOR_SCROLL_TO_TIME_PROGRAMS_BACKGROUND_DARK.getColor();
  }

  @Override
  public Color getScrollColorChannel() {
    return Settings.ProgramTable.COLOR_HIGHLIGHT_CHANNEL_PROGRAMS_BACKGROUND.getColor();
  }

  @Override
  public boolean isScrollToTimeHighlightActivated() {
    return Settings.ProgramTable.SCROLL_TO_TIME_MARKING.getBoolean();
  }

  @Override
  public boolean isScrollToChannelHighlightActivated() {
    return Settings.ProgramTable.HIGHLIGHT_CHANNEL_COLUMN_BY_SCROLLING.getBoolean();
  }

  @Override
  public String getDataDirectory() {
    return Settings.Directories.TV_DATA.getString();
  }

  @Override
  public boolean getCanReceiveProtocolMessages() {
    return Settings.General.CAN_RECEIVE_PROTOCOL_MESSAGE.getBoolean();
  }
}
