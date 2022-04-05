/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.extras.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import devplugin.Channel;
import tvbrowser.core.Settings;


public class LimitationConfiguration {
  private static final util.i18n.Localizer LOCALIZER = util.i18n.Localizer.getLocalizerFor(LimitationConfiguration.class);
  
  public static final int DAYLIMIT_DAILY = -1;
  public static final int DAYLIMIT_WEEKEND = -2;
  public static final int DAYLIMIT_WEEKDAY = -3;
  public static final int DAYLIMIT_SUNDAY = Calendar.SUNDAY;  // 1
  public static final int DAYLIMIT_MONDAY = Calendar.MONDAY;   // 2
  public static final int DAYLIMIT_TUESDAY = Calendar.TUESDAY;
  public static final int DAYLIMIT_WEDNESDAY = Calendar.WEDNESDAY;
  public static final int DAYLIMIT_THURSDAY = Calendar.THURSDAY;
  public static final int DAYLIMIT_FRIDAY = Calendar.FRIDAY;
  public static final int DAYLIMIT_SATURDAY = Calendar.SATURDAY;

  public static final DayLimitValue[] DAYLIMIT_VALUE_ARRAY = new DayLimitValue[10]; 
  
  static{
    DAYLIMIT_VALUE_ARRAY[0] = new DayLimitValue(DAYLIMIT_DAILY);
    DAYLIMIT_VALUE_ARRAY[1] = new DayLimitValue(DAYLIMIT_WEEKEND);
    DAYLIMIT_VALUE_ARRAY[2] = new DayLimitValue(DAYLIMIT_WEEKDAY);
    
    ArrayList<DayLimitValue> dayList = new ArrayList<LimitationConfiguration.DayLimitValue>(7);
    dayList.add(new DayLimitValue(DAYLIMIT_SUNDAY));
    dayList.add(new DayLimitValue(DAYLIMIT_MONDAY));
    dayList.add(new DayLimitValue(DAYLIMIT_TUESDAY));
    dayList.add(new DayLimitValue(DAYLIMIT_WEDNESDAY));
    dayList.add(new DayLimitValue(DAYLIMIT_THURSDAY));
    dayList.add(new DayLimitValue(DAYLIMIT_FRIDAY));
    dayList.add(new DayLimitValue(DAYLIMIT_SATURDAY));
    
    int firstDayOfWeek = Settings.Locales.FIRST_DAY_OF_WEEK.getInt() - 1;
    int index = 3;
    
    while(dayList.size() > firstDayOfWeek) {
      DAYLIMIT_VALUE_ARRAY[index++] = dayList.remove(firstDayOfWeek);
    }
    
    for(DayLimitValue day : dayList) {
      DAYLIMIT_VALUE_ARRAY[index++] = day;
    }
  }
  
  private int mFrom, mTo;
  private Channel[] mChannelArr;
  private ArrayList<ChannelItem> mChannelItemList = new ArrayList<ChannelItem>();
  private boolean mIsLimitedByChannel;
  private boolean mIsLimitedByTime;
  private int mDayLimit;



  public LimitationConfiguration(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt();  // version

    mIsLimitedByTime = in.readBoolean();
    if (mIsLimitedByTime) {
      mFrom = in.readInt();
      mTo = in.readInt();
    }

    mIsLimitedByChannel = in.readBoolean();
    if (mIsLimitedByChannel) {
      int cnt = in.readInt();
      ArrayList<Channel> list = new ArrayList<Channel>();
      for (int i=0; i<cnt; i++) {
        ChannelItem item = new ChannelItem(in, version);
        
        mChannelItemList.add(item);

        if (item.getChannel() != null) {
          list.add(item.getChannel());
        }
      }
      
      mChannelArr = list.toArray(new Channel[0]);
    }

    mDayLimit = in.readInt();
  }

  public LimitationConfiguration() {
    mDayLimit = DAYLIMIT_DAILY;
  }

  public void store(ObjectOutputStream out) throws IOException {
    out.writeInt(3); // version

    out.writeBoolean(mIsLimitedByTime);
    if (mIsLimitedByTime) {
      out.writeInt(mFrom);
      out.writeInt(mTo);
    }

    out.writeBoolean(mIsLimitedByChannel);
    if (mIsLimitedByChannel) {
      out.writeInt(mChannelItemList.size());
      for (int i=0; i<mChannelItemList.size(); i++) {
        (mChannelItemList.get(i)).saveItem(out);
      }
    }

    out.writeInt(mDayLimit);
  }

  public void setTime(int from, int to) {
    mFrom = from;
    mTo = to;
    mIsLimitedByTime = true;
  }

  public int getTimeFrom() {
    return mFrom;
  }

  public int getTimeTo() {
    return mTo;
  }

  public void setChannels(Channel[] ch) {
    mChannelItemList.clear();
    
    for (Channel element : ch) {
      mChannelItemList.add(new ChannelItem(element));
    }
    
    mChannelArr = ch;
    mIsLimitedByChannel = true;
  }

  /**
   * Gets the channels that are acceptable.
   * 
   * @return The channels results are limited to or <code>null</code>
   * if there is no channel limitation.
   */
  public Channel[] getChannels() {
    return mChannelArr;
  }

  /**
   * Gets if only programs on certain channels are to be accepted.
   * 
   * @return <code>true</code> if results are limited by channels.
   */
  public boolean isLimitedByChannel() {
    return mIsLimitedByChannel;
  }

  public boolean isLimitedByTime() {
    return mIsLimitedByTime;
  }

  public void setIsLimitedByChannel(boolean b) {
    mIsLimitedByChannel = b;
  }

  public void setIsLimitedByTime(boolean b) {
    mIsLimitedByTime = b;
  }

  public void setDayLimit(int daylimit) {
    mDayLimit = daylimit;
  }

  public int getDayLimit() {
    return mDayLimit;
  }
  
  public DayLimitValue getDayLimitValue() {
    return new DayLimitValue(mDayLimit);
  }
  
  /**
   * Tries to load the channels again.
   */
  public void reValidateChannels() {
    ArrayList<Channel> channelList = new ArrayList<Channel>();
    
    for(ChannelItem item : mChannelItemList) {
      item.reValidate();
      
      if(!item.isNullChannel()) {
        channelList.add(item.getChannel());
      }
    }
    
    mChannelArr = channelList.toArray(new Channel[channelList.size()]);
  }
  
  public static final class DayLimitValue {
    private final int mDay;
    private final String mText;
    
    public DayLimitValue(final int day) {
      mDay = day;
      mText = getDayString(day);
    }
    
    public int getDay() {
      return mDay;
    }
    
    @Override
    public String toString() {
      return mText;
    }
    
    @Override
    public boolean equals(Object obj) {
      if(obj instanceof DayLimitValue) {
        return ((DayLimitValue) obj).mDay == mDay;
      }
      
      return super.equals(obj);
    }
  }
  
  public static String getDayString(int dayOfWeek) {
    String str;
      switch (dayOfWeek) {
        case LimitationConfiguration.DAYLIMIT_DAILY:
          str = LOCALIZER.msg("day.daily", "Daily");
          break;
        case LimitationConfiguration.DAYLIMIT_WEEKDAY:
          str = LOCALIZER.msg("day.weekday", "weekday");
          break;
        case LimitationConfiguration.DAYLIMIT_WEEKEND:
          str = LOCALIZER.msg("day.weekend", "weekend");
          break;
        case LimitationConfiguration.DAYLIMIT_MONDAY:
          str = LOCALIZER.msg("day.monday", "monday");
          break;
        case LimitationConfiguration.DAYLIMIT_TUESDAY:
          str = LOCALIZER.msg("day.tuesday", "tuesday");
          break;
        case LimitationConfiguration.DAYLIMIT_WEDNESDAY:
          str = LOCALIZER.msg("day.wednesday", "wednesday");
          break;
        case LimitationConfiguration.DAYLIMIT_THURSDAY:
          str = LOCALIZER.msg("day.thursday", "thursday");
          break;
        case LimitationConfiguration.DAYLIMIT_FRIDAY:
          str = LOCALIZER.msg("day.friday", "friday");
          break;
        case LimitationConfiguration.DAYLIMIT_SATURDAY:
          str = LOCALIZER.msg("day.saturday", "saturday");
          break;
        case LimitationConfiguration.DAYLIMIT_SUNDAY:
          str = LOCALIZER.msg("day.sunday", "sunday");
          break;
        default:
          str = "<unknown>";
      }
    return str;
  }
}
