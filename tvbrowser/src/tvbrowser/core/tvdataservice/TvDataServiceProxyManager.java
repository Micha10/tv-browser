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

package tvbrowser.core.tvdataservice;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import devplugin.AbstractTvDataService;
import devplugin.Version;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.core.settings.PluginSettings;


/**
 * Manages the TvDataServices
 */
public class TvDataServiceProxyManager {
  public static final String PLUGIN_DIRECTORY = "tvdataservice";

  private static TvDataServiceProxyManager mInstance;

  /**
   * The list of all installed TvDataServices
   */
  private ArrayList<TvDataServiceProxy> mProxyList;

  private TvDataServiceProxyManager() {
    mProxyList = new ArrayList<TvDataServiceProxy>();
    AbstractTvDataService.setPluginManager(PluginManagerImpl.getInstance());
  }

  public static TvDataServiceProxyManager getInstance() {
    if (mInstance == null) {
      mInstance = new TvDataServiceProxyManager();
    }
    return mInstance;
  }


  public void registerTvDataService(TvDataServiceProxy service) {
     mProxyList.add(service);
  }

  /**
   * Changes the TvDataService working directory to the specified folder.
   * @param dir The tv data directory to use.
   */
  public void setTvDataDir(File dir) {
    for (TvDataServiceProxy proxy : getDataServices()) {
      File dataServiceDir=new File(dir,proxy.getId());
      if (!dataServiceDir.exists()) {
        dataServiceDir.mkdirs();
      }
      proxy.setWorkingDirectory(dataServiceDir);
    }
  }

  /**
   * Loads and initializes all available TvDataServices
   */
  public void init() {
    try {
      String tvdataRoot = Settings.Directories.TV_DATA.getString();
      File rootDir = new File(tvdataRoot);
      if (!rootDir.exists()) {
        rootDir.mkdirs();
      }
      setTvDataDir(rootDir);

      // load only the settings of services with current subscription
      TvDataServiceProxy[] proxies = getDataServices();
      String[] subscribedServices = Settings.Channels.DATA_SERVICE_IDS_USED_CURRENTLY
          .getStringArray();
      java.util.List<String> list = Arrays.asList(subscribedServices);

      for (TvDataServiceProxy proxy : proxies) {
        if (list.size() == 0 || list.contains(proxy.getId())) {
          PluginSettings.loadSettings(proxy);
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public boolean licensesAccepted(final TvDataServiceProxy services[]) {
    return true;
  }


  public void shutDown() {
    for (TvDataServiceProxy proxy : getDataServices()) {
      if(proxy.hasToSaveSettings()) {
        PluginSettings.storeSettings(proxy);
      }
    }
  }

  public TvDataServiceProxy findDataServiceById(final String id) {
    if (mProxyList != null) {
      for (TvDataServiceProxy proxy : mProxyList) {
        if (id.equals(proxy.getId())) {
          return proxy;
        }
      }
    }
    return null;
  }


  public TvDataServiceProxy[] getTvDataServices(final String[] idArr) {
    ArrayList<TvDataServiceProxy> list = new ArrayList<TvDataServiceProxy>();
    for (String id : idArr) {
      TvDataServiceProxy proxy = findDataServiceById(id);
      if (proxy != null) {
        list.add(proxy);
      }
    }
    if(list.size() > 0) {
      return list.toArray(new TvDataServiceProxy[list.size()]);
    } else {
      return getDataServices();
    }
  }


  public TvDataServiceProxy[] getDataServices() {
    if (mProxyList == null) {
      return new TvDataServiceProxy[]{};
    }
    return mProxyList.toArray(new TvDataServiceProxy[mProxyList.size()]);
  }

  /**
   * Set the Parent-Frame for all Dataservices
   * @param frame Parentframe
   */
  public void setParamFrame(final JFrame frame) {
      for (TvDataServiceProxy proxy : getDataServices()) {
        proxy.setParent(frame);
      }
  }

  public void fireTvBrowserStartFinished() {
    for (TvDataServiceProxy proxy : getDataServices()) {
        proxy.handleTvBrowserStartFinished();
    }
  }
  
  public void fireTvBrowserVersionUpdate(final Version previousVersion) {
    for (TvDataServiceProxy proxy : getDataServices()) {
        proxy.handleTvBrowserVersionUpdate(previousVersion);
    }
  }

  public void loadNotSubscribed() {
    try {
      // load only the settings of services WITHOUT subscription
      String[] subscribedServices = Settings.Channels.DATA_SERVICE_IDS_USED_CURRENTLY
          .getStringArray();
      if (subscribedServices.length == 0) {
        return;
      }
      List<String> list = Arrays.asList(subscribedServices);
      
      for (TvDataServiceProxy proxy : getDataServices()) {
        if (!list.contains(proxy.getId())) {
          PluginSettings.loadSettings(proxy);
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
