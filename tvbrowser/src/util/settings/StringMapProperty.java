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

import java.util.HashMap;
import java.util.Set;

/**
 * @author René Mach
 * @since 4.2.5
 */
public class StringMapProperty extends Property {
  private static final String SEPARATOR_ENTRIES = "§#-";
  private static final String SEPARATOR_VALUES = "#§#";
  
  private HashMap<String, String> mMap;
  private boolean mIsCacheFilled;
  
  public StringMapProperty(PropertyManager manager, String key) {
    super(manager, key);
    
    mMap = new HashMap<String, String>();
    mIsCacheFilled = false;
  }
  
  public void putEntry(final String key, final int value) {
    putEntry(key, String.valueOf(value));
  }
  
  public String getEntry(final String key) {
    if(!mIsCacheFilled) {
      fillCache();
    }
    
    return mMap.get(key);
  }
  
  public void putEntry(final String key, final String value) {
    if(!mIsCacheFilled) {
      fillCache();
    }
    
    mMap.put(key, value);
    
    updateProperty();
  }
  
  public boolean containsKey(final String key) {
    if(!mIsCacheFilled) {
      fillCache();
    }
    
    return mMap.containsKey(key);
  }
  
  public String removeEntry(final String key) {
    if(!mIsCacheFilled) {
      fillCache();
    }
    
    String result = mMap.remove(key);
    
    if(result != null) {
      updateProperty();
    }
    
    return result;
  }
  
  public Set<String> getKeySet() {
    if(!mIsCacheFilled) {
      fillCache();
    }
    
    return mMap.keySet();
  }
  
  private void updateProperty() {
    if(!mIsCacheFilled) {
      fillCache();
    }
    
    Set<String> keys = mMap.keySet();
    StringBuilder property = new StringBuilder();
    
    for(String key : keys) {
      if(property.length() > 0) {
        property.append(SEPARATOR_ENTRIES);
      }
      property.append(key).append(SEPARATOR_VALUES).append(mMap.get(key));
    }
    
    setProperty(property.toString());
  }

  private void fillCache() {
    final String prop = getProperty();
    
    if(prop != null && !prop.isBlank()) {
      final String[] entries = prop.split(SEPARATOR_ENTRIES);
      
      for(String entry : entries) {
        final String[] parts = entry.split(SEPARATOR_VALUES);
        mMap.put(parts[0], parts[1]);
      }
    }
    
    mIsCacheFilled = prop != null;
  }
  
  @Override
  protected void clearCache() {
    mMap.clear();
    mIsCacheFilled = false;
  }
}
