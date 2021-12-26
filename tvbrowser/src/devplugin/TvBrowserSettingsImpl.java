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
    return Settings.propTimeButtons.getIntArray();
  }

  public Date getLastDownloadDate() {
    return Settings.propLastDownloadDate.getDate();
  }
  
  public int getDefaultNetworkConnectionTimeout(){
    return Settings.propDefaultNetworkConnectionTimeout.getInt();
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
    return Settings.propProgramTableEndOfDay.getInt();
  }

  public int getProgramTableStartOfDay() {
    return Settings.propProgramTableStartOfDay.getInt();
  }
  
  public Color getProgramPanelOnAirLightColor() {
    return Settings.propProgramPanelColorOnAirLight.getColor();
  }
  
  public Color getProgramPanelOnAirDarkColor() {
    return Settings.propProgramPanelColorOnAirDark.getColor();
  }

  public boolean isMarkingBorderPainted() {
    return Settings.propProgramPanelWithMarkingsShowingBoder.getBoolean();
  }

  public boolean isUsingExtraSpaceForMarkIcons() {
    return Settings.propProgramPanelUsesExtraSpaceForMarkIcons.getBoolean();
  }

  public short getAutoDownloadWaitingTime() {
    return Settings.propAutoDownloadWaitingTime.getShort();
  }

  @Override
  public Color getProgramTableMouseOverColor() {
    return Settings.propProgramTableMouseOver.getBoolean() ? Settings.propProgramTableMouseOverColor.getColor() : null;
  }
  
  @Override
  public Color getProgramTableForegroundColor() {
    return Settings.propTableBackgroundStyle.getString().contains("ui") ? UIManager.getColor("List.foreground") : Settings.propProgramPanelForegroundColor.getColor();
  }
  
  @Override
  public Color getProgramPanelSelectionColor() {
    return Settings.propKeyboardSelectedColor.getColor();
  }

  @Override
  public String getTimePattern() {
    return Settings.getTimePattern();
  }

  @Override
  public boolean isChannelUpdateActivated() {
    return Settings.propAutoChannelUpdatePeriod.getInt() > GeneralSettingsTab.VALUE_AUTO_CHANNEL_UPDATE_DISABLED;
  }

  @Override
  public Color getScrollColorTimeLight() {
    return Settings.propScrollToTimeProgramsLightBackground.getColor();
  }

  @Override
  public Color getScrollColorTimeDark() {
    return Settings.propScrollToTimeProgramsDarkBackground.getColor();
  }

  @Override
  public Color getScrollColorChannel() {
    return Settings.propHighlightChannelProgramsBackground.getColor();
  }

  @Override
  public boolean isScrollToTimeHighlightActivated() {
    return Settings.propScrollToTimeMarkingActivated.getBoolean();
  }

  @Override
  public boolean isScrollToChannelHighlightActivated() {
    return Settings.propHighlightChannelColumnByScrolling.getBoolean();
  }

  @Override
  public String getDataDirectory() {
    return Settings.propTVDataDirectory.getString();
  }

  @Override
  public boolean getCanReceiveProtocolMessages() {
    return Settings.propCanReceiveProtocolMessages.getBoolean();
  }
}
