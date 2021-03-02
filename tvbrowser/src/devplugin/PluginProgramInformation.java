/*
 * TV-Browser
 * Copyright (C) 2021 TV-Browser-Team (dev@tvbrowser.org)
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

import java.util.HashMap;
import java.util.Set;

/**
 * A class with information about a program.
 * 
 * @author René Mach
 * @since 4.2.3
 */
public class PluginProgramInformation {
  private Plugin mPlugin;
  private String mUniqueProgramId;
  private HashMap<String, Object> mInfoMap;
  
  private PluginProgramInformation(final Plugin plugin, final String uniqueProgramId) {
    mPlugin = plugin;
    mUniqueProgramId = uniqueProgramId;
    mInfoMap = new HashMap<String, Object>();
  }
  
  /**
   * Create an instance of a PluginInformationSetter that contains a PluginProgramInformation
   * instance for the given plugin and program id.
   * <p> 
   * @param plugin The plugin to create an instance for.
   * @param uniqueProgramId The unique ID of the program the information is about.
   * @return
   */
  public static PluginInformationSetter create(final Plugin plugin, final String uniqueProgramId) {
    PluginProgramInformation info = new PluginProgramInformation(plugin, uniqueProgramId);
    return new PluginInformationSetter(info);
  }
  
  /**
   * Gets the available keys for this PluginProgramInformation
   * <p>
   * @return The keys of this PluginProgramInformation.
   */
  public Set<String> getKeys() {
    return mInfoMap.keySet();
  }
  
  /**
   * Gets the information for the given key.
   * <p>
   * @param key The key for the information to get.
   * @return The information for the given key or <code>null</code>
   *         if no information with the given key exists.
   */
  public Object getInformationForKey(final String key) {
    return mInfoMap.get(key);
  }
  
  
  
  /**
   * Gets the id of the plugin this information belongs to.
   * <p>
   * @return The plugin id of this information's plugin.
   */
  public String getPluginId() {
    return mPlugin.getId();
  }
  
  /**
   * Get the unique ID of the program this information belongs to.
   * <p>
   * @return The unique ID of this information's program.
   */
  public String getUniqueProgramId() {
    return mUniqueProgramId;
  }
  
  /**
   * A class to set the information for a PluginProgramInformation class.
   * This is to prevent manipulation of data contained in PluginProgramInformation.
   * <p>
   * @author René Mach
   * @since 4.2.3
   */
  public static final class PluginInformationSetter {
    private PluginProgramInformation mInstance;
    
    private PluginInformationSetter(PluginProgramInformation instance) {
      mInstance = instance;
    }
    
    /**
     * Gets the PluginProgramInformation instance.
     * <p>
     * @return The PluginProgramInformation instance.
     */
    public PluginProgramInformation getPluginProgramInformation() {
      return mInstance;
    }
    
    /**
     * Sets the information for the given key. If the given information
     * is <code>null</code> the information for the given key is removed.
     * <p>
     * @param key The information key to add.
     * @param information The information to add.
     */
    public void setInformation(final String key, final Object information) {
      if(information != null) {
        mInstance.mInfoMap.put(key, information);
      }
      else {
        mInstance.mInfoMap.remove(key);
      }
    }
  }
}
