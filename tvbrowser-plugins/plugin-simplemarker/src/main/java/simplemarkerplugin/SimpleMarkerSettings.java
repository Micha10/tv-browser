/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package simplemarkerplugin;

import java.util.Properties;

import util.settings.PropertyBasedSettings;

/**
 * @author bananeweizen
 *
 */
public final class SimpleMarkerSettings extends PropertyBasedSettings{
  private static final String KEY_SPLIT_POSITION = "splitPosition";
  private static final String KEY_SHOW_DATE_SEPARATORS = "showDateSeparator";
  private static final String KEY_SHOW_ALL_IN_CONTEXT_MENU = "showInContextMenu";
  
  private static final String KEY_REACT_SCROLL_TIME = "reactScrollTime";
  private static final String KEY_REACT_SCROLL_DATE = "reactScrollDate";
  private static final String KEY_REACT_FILTER_CHANGE = "reactFilterChange";
  private static final String KEY_SCROLL_TO_NEXT_TIME = "scrollToNextTime";
  private static final String KEY_SCROLL_TIME = "scrollToTime";
  
  /**
   * show plugin nodes by title and date
   */
  private static final int GROUPING_BY_TITLE_AND_DATE = 0;
  /**
   * show plugin nodes by titles only
   */
  private static final int GROUPING_BY_TITLE = 1;
  /**
   * show plugin nodes by date only
   */
  private static final int GROUPING_BY_DATE = 2;
  private static final String KEY_GROUPING = "grouping";

  SimpleMarkerSettings(final Properties properties) {
    super(properties);
  }

  /**
   * Gets the position of the split pane divider.
   * <p>
   * @return The position of the split pane divider.
   */
  public int getSplitPosition() {
    return get(KEY_SPLIT_POSITION, -1);
  }

  /**
   * Sets the position of the split pane divider.
   * <p>
   * @param dividerLocation The new location of the split pane divider.
   */
  public void setSplitPosition(int dividerLocation) {
    set(KEY_SPLIT_POSITION, dividerLocation);
  }

  void setNodeGroupingByBoth() {
    setNodeGrouping(GROUPING_BY_TITLE_AND_DATE);
  }

  private void setNodeGrouping(int grouping) {
    set(KEY_GROUPING, grouping);
  }

  /**
   * Gets if the programs in tree should be grouped by title and date.
   * <p>
   * @return If the programs should be grouped by title and date.
   */
  public boolean isGroupingByBoth() {
    return isGrouping(GROUPING_BY_TITLE_AND_DATE);
  }

  private boolean isGrouping(int grouping) {
    return get(KEY_GROUPING, 0) == grouping;
  }

  void setNodeGroupingByTitle() {
    setNodeGrouping(GROUPING_BY_TITLE);
  }

  /**
   * Gets if the programs in tree should be grouped by title.
   * <p>
   * @return If the program should be grouped by title.
   */
  public boolean isGroupingByTitle() {
    return isGrouping(GROUPING_BY_TITLE);
  }

  void setNodeGroupingByDate() {
    setNodeGrouping(GROUPING_BY_DATE);
  }

  /**
   * Gets if the programs in tree should be grouped by date.
   * <p>
   * @return If the program should be grouped by date.
   */
  public boolean isGroupingByDate() {
    return isGrouping(GROUPING_BY_DATE);
  }
  
  void setShowingDateSeperators(boolean show) {
    set(KEY_SHOW_DATE_SEPARATORS, show);
  }
  
  /**
   * Gets if the date separators should be shown in the program list.
   * <p>
   * @return If the date separators should be shown in the program list.
   */
  public boolean isShowingDateSeperators() {
    return get(KEY_SHOW_DATE_SEPARATORS, true);
  }
  
  void setShowingInContextMenu(boolean show) {
    set(KEY_SHOW_ALL_IN_CONTEXT_MENU, show);
  }
  
  public boolean isShowingInContextMenu() {
    return get(KEY_SHOW_ALL_IN_CONTEXT_MENU, false);
  }
  
  void setReactScrollTime(boolean scroll) {
    set(KEY_REACT_SCROLL_TIME, scroll);
  }
  
  public boolean isReactScrollTime() {
    return get(KEY_REACT_SCROLL_TIME, true);
  }
  
  void setReactScrollDate(boolean scroll) {
    set(KEY_REACT_SCROLL_DATE, scroll);
  }
  
  public boolean isReactScrollDate() {
    return get(KEY_REACT_SCROLL_DATE, true);
  }
  
  void setReactFilterChange(boolean react) {
    set(KEY_REACT_FILTER_CHANGE, react);
  }
  
  public boolean isReactFilterChange() {
    return get(KEY_REACT_FILTER_CHANGE, true);
  }

  void setScrollToTime(boolean scrollToTimeNext, boolean scrollToTime) {
    set(KEY_SCROLL_TO_NEXT_TIME, scrollToTimeNext && !scrollToTime);
    set(KEY_SCROLL_TIME, scrollToTime && !scrollToTimeNext);
  }
  
  public boolean isScrollToTimeNext() {
    return get(KEY_SCROLL_TO_NEXT_TIME, true);
  }
  
  public boolean isScrollToTime() {
    return get(KEY_SCROLL_TIME, false);
  }
}
