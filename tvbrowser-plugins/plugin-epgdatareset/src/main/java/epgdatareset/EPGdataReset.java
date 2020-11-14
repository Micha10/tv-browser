/*
 * EPGdataReset plugin for TV-Browser
 * Copyright (C) 2019 René Mach (rene@tvbrowser.org)
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
 */
package epgdatareset;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.TvBrowserSettings;
import devplugin.Version;
import util.settings.StringProperty;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

/**
 * A plugin to delete the data cache of the data plugins
 * EPGfree and EPGdonate.
 * 
 * @author Ren\u00E9 Mach
 */
public class EPGdataReset extends Plugin {
  private static final String KEY_AUTO_RESET = "autoReset";
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(EPGdataReset.class);
  private static final Version VERSION = new Version(0, 30, true);
  private PluginInfo mInfo = new PluginInfo(EPGdataReset.class, LOCALIZER.msg("name","EPGdataReset"), LOCALIZER.msg("desc","Resets the data cache of the EPGfree and EPGdonate data plugins."), "Ren\u00E9 Mach", "GPL v3.0");
  private Properties mProperties;
  
  public static Version getVersion() {
    return VERSION;
  }
  
  @Override
  public PluginInfo getInfo() {
    return mInfo;
  }
  
  @Override
  public void loadSettings(Properties settings) {
    mProperties = settings;
  }
  
  @Override
  public Properties storeSettings() {
    return mProperties;
  }
  
  @Override
  public SettingsTab getSettingsTab() {
    SettingsTab result = null;
    
    if(getPluginManager().getTVBrowserVersion().compareTo(new Version(3,20,true)) >= 0) {
      result = new SettingsTab() {
        private JCheckBox mAutoReset;
        
        @Override
        public void saveSettings() {
          mProperties.setProperty(KEY_AUTO_RESET, String.valueOf(mAutoReset.isSelected()));
        }
        
        @Override
        public String getTitle() {
          return getInfo().getName();
        }
        
        @Override
        public Icon getIcon() {
          return null;
        }
        
        @Override
        public JPanel createSettingsPanel() {
          final PanelBuilder pb = new PanelBuilder(new FormLayout("10dlu,default:grow","5dlu,default"));
          
          mAutoReset = new JCheckBox(LOCALIZER.ellipsisMsg("autoReset","Always reset data cache before data update"), mProperties.getProperty(KEY_AUTO_RESET, "false").equals("true"));
          pb.add(mAutoReset, CC.xy(2, 2));
          
          return pb.getPanel();
        }
      };
    }
    
    return result;
  }
  
  public void handleTvDataUpdateStarted() {
    clean();
  }
  
  public void handleTvDataUpdateStarted(Date until) {
    clean();
  }
  
  private void clean() {
    if(mProperties.getProperty(KEY_AUTO_RESET, "false").equals("true")) {
      clean(true);
    }
  }
  
  @Override
  public ActionMenu getButtonAction() {
    final ContextMenuAction action = new ContextMenuAction(LOCALIZER.msg("action","Reset data cache now"), TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL)) {
      @Override
      public void actionPerformed(ActionEvent event) {
        clean(false);
      }
    };
    action.putValue(Plugin.BIG_ICON, TVBrowserIcons.delete(TVBrowserIcons.SIZE_LARGE));
    
    return new ActionMenu(getInfo().getName(),new ContextMenuAction[] {action});
  }
  
  private void clean(boolean quiet) {
    TvBrowserSettings settings = getPluginManager().getTvBrowserSettings();
    
    Class<? extends TvBrowserSettings> clazz = settings.getClass();
    
    String dataDirectory = null;
    
    try {
      Method getDataDirectory = clazz.getDeclaredMethod("getDataDirectory");
      getDataDirectory.setAccessible(true);
      dataDirectory = (String)getDataDirectory.invoke(settings);
    } catch (Exception e) {
      try {
        Class<?> clazz2 = Class.forName("tvbrowser.core.Settings");
        Field propTVDataDirectory = clazz2.getDeclaredField("propTVDataDirectory");
        dataDirectory = ((StringProperty)propTVDataDirectory.get(null)).getString();
      } catch (Exception e1) {}
    }
    
    if(dataDirectory != null) {
      final File epgdonateCache = new File(dataDirectory,"epgdonatedata.EPGdonateData" + File.separator + "summary.gz");
      
      boolean success = true;
      
      if(epgdonateCache.isFile()) {
        success = epgdonateCache.delete();
      }
      
      final File epgfreeDir = new File(dataDirectory,"tvbrowserdataservice.TvBrowserDataService");
      
      if(epgfreeDir.isDirectory()) {
        final File[] epgfreeCache = epgfreeDir.listFiles(new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            return pathname.getName().endsWith(".prog.gz");
          }
        });
        
        if(epgdonateCache != null) {
          for(File epgFree : epgfreeCache) {
            success = epgFree.delete();
          }
        }
      }
      
      if(!quiet) {
        if(success) {
          JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(getParentFrame()), LOCALIZER.msg("success", "Data cache cleared successfully"), LOCALIZER.msg("result","Result"), JOptionPane.INFORMATION_MESSAGE);
        }
        else {
          JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(getParentFrame()), LOCALIZER.msg("error", "Data cache could not be cleared completely"), LOCALIZER.msg("result","Result"), JOptionPane.WARNING_MESSAGE);
        }
      }
    }
    else if(!quiet) {
      JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(getParentFrame()), LOCALIZER.msg("nodatadirectory", "Data directory could not be found.\n\nData cache was not deleted."), LOCALIZER.msg("result","Result"), JOptionPane.ERROR_MESSAGE);
    }
  }
}
