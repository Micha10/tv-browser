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

import javax.swing.JOptionPane;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
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
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(EPGdataReset.class);
  private static final Version VERSION = new Version(0, 20, true);
  private PluginInfo mInfo = new PluginInfo(EPGdataReset.class, LOCALIZER.msg("name","EPGdataReset"), LOCALIZER.msg("desc","Resets the data cache of the EPGfree and EPGdonate data plugins."), "Ren\u00E9 Mach", "GPL v3.0");
  
  public static Version getVersion() {
    return VERSION;
  }
  
  @Override
  public PluginInfo getInfo() {
    return mInfo;
  }
  
  @Override
  public ActionMenu getButtonAction() {
    final ContextMenuAction action = new ContextMenuAction(LOCALIZER.msg("action","Reset data cache now"), TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL)) {
      @Override
      public void actionPerformed(ActionEvent event) {
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
          
          if(success) {
            JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(getParentFrame()), LOCALIZER.msg("success", "Data cache cleared successfully"), LOCALIZER.msg("result","Result"), JOptionPane.INFORMATION_MESSAGE);
          }
          else {
            JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(getParentFrame()), LOCALIZER.msg("error", "Data cache could not be cleared completely"), LOCALIZER.msg("result","Result"), JOptionPane.WARNING_MESSAGE);
          }
        }
        else {
          JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(getParentFrame()), LOCALIZER.msg("nodatadirectory", "Data directory could not be found.\n\nData cache was not deleted."), LOCALIZER.msg("result","Result"), JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    action.putValue(Plugin.BIG_ICON, TVBrowserIcons.delete(TVBrowserIcons.SIZE_LARGE));
    
    return new ActionMenu(getInfo().getName(),new ContextMenuAction[] {action});
  }
}
