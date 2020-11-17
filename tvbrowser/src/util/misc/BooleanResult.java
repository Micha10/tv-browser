/*
 * TV-Browser
 * Copyright (C) 2020 TV-Browser team (dev@tvbrowser.org)
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
package util.misc;

import java.util.Hashtable;

/**
 * A class with multiple boolean values.
 * 
 * @author René Mach
 * @since 4.2.2
 */
public class BooleanResult {
  private boolean[] mResultArr;
  private Hashtable<String, Integer> mResultNamesTable;
  private Hashtable<Integer, String> mNamesResultTable;
  
  /**
   * @param values The boolean values for this result
   */
  public BooleanResult(final boolean... values) {
    mResultArr = values;
    mResultNamesTable = new Hashtable<String, Integer>();
    mNamesResultTable = new Hashtable<Integer, String>();
  }
  
  /**
   * Sets the names for the result values, so it later can
   * be used to acquire the result value for the name.
   * @param names The names to set.
   */
  public void setResultNames(final String... names) {
    mResultNamesTable.clear();
    mNamesResultTable.clear();
    
    for(int i = 0; i < names.length; i++) {
      mResultNamesTable.put(names[i], i);
      mNamesResultTable.put(i, names[i]);
    }
  }
  
  /**
   * Gets the number of the results contained by this BooleanResult.
   * 
   * @return The number of the available results.
   */
  public int getResultCount() {
    return mResultArr.length;
  }
  
  /**
   * Gets an array with the result values.
   * 
   * @return The array with the result values.
   */
  public boolean[] getResults() {
    return mResultArr;
  }

  /**
   * Gets the boolean value for the result with the given index.
   * 
   * @param index The index to get the boolean value for.
   * @return <code>true</code> if the index exists and the result value is <code>true</code>, <code>false</code> otherwise.
   */
  public boolean getResultForIndex(final Integer index) {
    if(mResultArr != null && index != null && index < mResultArr.length) {
      return mResultArr[index];
    }
    
    return false;
  }

  /**
   * Gets the boolean value for the result with the given name.
   * 
   * @param name The name of the boolean value to get.
   * @return <code>true</code> if the name exists and the result value is <code>true</code>, <code>false</code> otherwise.
   */
  public boolean getResultForName(final String name) {
    return getResultForIndex(mResultNamesTable.get(name));
  }
  
  /**
   * Gets if all boolean results contained are <code>true</code>
   * 
   * @return <code>true</code> if all values contained are <code>true</code>, <code>false</code> otherwise.
   */
  public boolean isAllTrue() {
    boolean result = true;
    
    for(boolean value : mResultArr) {
      result &= value;
    }
    
    return result;
  }
  
  /**
   * Gets if all boolean results contained are <code>false</code>
   * 
   * @return <code>true</code> if all values contained are <code>false</code>, <code>false</code> otherwise.
   */
  public boolean isAllFalse() {
    boolean result = false;
    
    for(boolean value : mResultArr) {
      result |= value;
    }
    
    return !result;
  }
  
  /**
   * Gets if at least one boolean results contained is <code>true</code>
   * 
   * @return <code>true</code> if at least one value contained is <code>true</code>, <code>false</code> otherwise.
   */
  public boolean isPartlyTrue() {
    boolean result = false;
    
    for(boolean value : mResultArr) {
      if(value) {
        result = true;
        break;
      }
    }
    
    return result;
  }
  
  @Override
  public String toString() {
    String result = "";
    for(int i = 0; i < mResultArr.length; i++) {
      String name = mNamesResultTable.get(i);
      result+=i+(name != null ? "["+name+"]" : "")+":"+mResultArr[i]+",";
    }
    
    return result;
  }
}
