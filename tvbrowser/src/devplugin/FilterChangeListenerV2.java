/*
 * TV-Browser
 * Copyright (C) 2013 TV-Browser team (dev@tvbrowser.org)
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
package devplugin;

/**
 * A listener for changes of filters.
 * <p>
 * @author René Mach
 * @since 3.3.4
 */
public interface FilterChangeListenerV2 {
  /**
   * Called when a filter is added.
   * <p>
   * @param filter The filter that was added.
   */
  public void filterAdded(ProgramFilter filter);
  
  /**
   * Called when a filter was removed.
   * <p>
   * @param filter The filter that was removed.
   */
  public void filterRemoved(ProgramFilter filter);
  
  /**
   * Called when user edited the filter.
   * NOTE: If you override {@link #filterTouched(ProgramFilter[])}
   * this method will never be called.
   * <p>
   * @param filter The filter that was touched.
   * @deprecated since 4.2.7
   */
  @Deprecated(since="4.2.7") public void filterTouched(ProgramFilter filter);
  
  /**
   * Called when user edited filters.
   * <p>
   * @param filters An array with the filters that were touched.
   * @since 4.2.7
   */
  default public void filterTouched(ProgramFilter[] filters) {
    for(ProgramFilter filter : filters) {
      filterTouched(filter);
    }
  }
  
  /**
   * Called when the default filter was changed.
   * <p>
   * @param filter The new default filter, or the
   * all filter if no default filter exists.
   */
  public void filterDefaultChanged(ProgramFilter filter);
}

