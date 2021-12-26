/*
 * TV-Browser
 * Copyright (C) 2021 TV-Browser team (dev@tvbrowser.org)
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
package util.settings;

/**
 * Convenient property to easily get full days, hour and minute for minutes value.
 * 
 * @author René Mach
 * @since 4.2.5
 */
public class MinutesProperty extends IntProperty {

  public MinutesProperty(PropertyManager manager, String key, int defaultValue) {
    super(manager, key, defaultValue);
  }
  
  /**
   * Gets the number of full days the minutes encode.
   * Example: A value of 720 minutes will result in 0 number of day because
   *          720 minutes are only 12 hours and therefore not a full day.
   * @return The number of full days.
   */
  public int getNumberOfFullDays() {
    return (getInt() / 60) / 24;
  }
  
  public int getHourOfDay() {
    return (getInt() / 60) % 24;
  }
  
  public int getMinutesOfHour() {
    return getInt() % 60;
  }
  
  public int getDefaultNumberOfFullDays() {
    return (getDefault() / 60) / 24;
  }
  
  public int getDefaultHourOfDay() {
    return (getDefault() / 60) % 24;
  }
  
  public int getDefaultMinutesOfHour() {
    return getDefault() % 60;
  }
}
