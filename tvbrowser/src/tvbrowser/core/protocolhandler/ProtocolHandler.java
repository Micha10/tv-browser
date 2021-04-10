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
package tvbrowser.core.protocolhandler;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import devplugin.PluginManager;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramReceiveTarget;
import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.programinfo.ProgramInfo;
import tvbrowser.extras.searchplugin.SearchPlugin;
import tvbrowser.ui.DontShowAgainOptionBox;
import tvbrowser.ui.mainframe.MainFrame;
import util.browserlauncher.Launch;
import util.exc.TvBrowserException;
import util.i18n.Localizer;
import util.io.ExecutionHandler;
import util.io.windows.registry.RegistryKey;
import util.io.windows.registry.RegistryValue;
import util.settings.BooleanProperty;
import util.settings.ByteProperty;
import util.settings.ChoiceProperty;
import util.settings.ColorProperty;
import util.settings.IntArrayProperty;
import util.settings.IntProperty;
import util.settings.StringArrayProperty;
import util.settings.StringProperty;
import util.ui.SearchFormSettings;
import util.ui.UiUtilities;

/**
 * Handler for protocol message with tvb:\\
 * 
 * @author René Mach
 * @since 4.2.3
 */
public class ProtocolHandler {
  private static final String ESCAPE_SLASH = "|||";
  private static final String ESCAPE_SEMICOLON = ":::";
  private static final String ESCAPE_COMMA = "%%%";
  private static final String ESCAPE_QUOTE = "§§§";
  
  private static final String MESSAGE_CONFIG = "config";
  private static final String MESSAGE_PLUGIN = "plugin";
  private static final String MESSAGE_ENABLE = "enable";
  private static final String MESSAGE_SHOW = "show";
  private static final String MESSAGE_SEARCH = "search";
  private static final String MESSAGE_PROGRAM = "program";
  private static final String MESSAGE_SETTINGS = "settings";
  private static final String MESSAGE_PLUGIN_UPDATE = "pluginUpdate";
  
  private static final String KEY_SEARCH_WHERE = "where";
  private static final String KEY_SEARCH_CASE_SENSITIVE = "casesensitive";
  private static final String KEY_SEARCH_TEXT = "text";
  
  private static final String VALUE_SEARCH_WHERE_ALL = "all";
  private static final String VALUE_SEARCH_WHERE_TITLE = "title";
  
  private static final String KEY_SHOW_PROGRAM_ID = "id";
  
  private static final String MESSAGE_SEARCH_TYPE_EXACTLY = "exact";
  private static final String MESSAGE_SEARCH_TYPE_WHOLE_TERM = "term";
  private static final String MESSAGE_SEARCH_TYPE_KEYWORD = "keyword";
  private static final String MESSAGE_SEARCH_TYPE_BOOL = "bool";
  private static final String MESSAGE_SEARCH_TYPE_REGEX = "regex";
  
  
  
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(ProtocolHandler.class);
  private static final Logger LOG = Logger.getLogger(ProtocolHandler.class.getName());
  
  private static ProtocolHandler INSTANCE;
  private boolean mIsEnabled;
  
  private ProtocolHandler() {
    INSTANCE = this;
    mIsEnabled = Settings.propCanReceiveProtocolMessages.getBoolean();
    
    if(mIsEnabled) {
      if(Launch.getOs() == Launch.OS_LINUX) {
        final File mime = new File(System.getProperty("user.home")+"/.config/mimeapps.list");
        String handler = null;
        
        if(mime.isFile()) {
          try(BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(mime), "UTF-8"))) {
            String line = null;
            
            while((line = in.readLine()) != null) {
              if(line.startsWith("x-scheme-handler/tvb")) {
                handler = line.substring(line.indexOf("=")+1);
                break;
              }
            }
          }catch(IOException ioe) {
            ioe.printStackTrace();
          }
        }
        
        final File baseDir = new File("");
        final File start = new File(baseDir.getAbsolutePath(),"/tvbrowser"+(TVBrowser.isTransportable() ? "-transportable":"")+".sh");
        
        File source = new File(System.getProperty("user.home")+"/.local/share/applications/tvbrowserWebstart.desktop");
        boolean ask = !source.isFile();
        
        if(handler.equals("tvbrowser.desktop")) {
          source = null;
          ask = !baseDir.getAbsolutePath().startsWith("/usr/share/tvbrowser");
        }
        
        if(source != null && source.isFile()) {
          try(BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(source), "UTF-8"))) {
            String line = null;
            
            while((line = in.readLine()) != null) {
              if(line.startsWith("Exec")) {
                ask = !line.contains(start.getAbsolutePath());
                break;
              }
            }
          }catch(IOException ioe) {
            ioe.printStackTrace();
          }
        }
        
        if(source == null) {
          source = new File("/usr/share/tvbrowser/tvbrowser.sh");
          
        }
        
        if(ask) {
          if(!source.isFile() || DontShowAgainOptionBox.showOptionDialog("tvbProtocolWrongTarget", UiUtilities.getParentFrameOnMouseScreen(), LOCALIZER.msg("error.linux.msg", "Receiving protocol message with tvb:\\ is activated.\nProtocol message make it easier to configure TV-Browser.\nBut the protocol leads to another TV-Browser.\n\nShould the protocol instead lead to this TV-Browser?\n(The protocol messages are deactivted for this TV-Browser if not.)"), LOCALIZER.msg("error.linux.title", "tvb:\\\\-Protokoll leads to another TV-Browser"), JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
            enable(false);
          }
          else {
            Settings.propCanReceiveProtocolMessages.setBoolean(false);
            mIsEnabled = false;
            try {
              Settings.storeSettings(true);
            } catch (TvBrowserException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
        }
      }
      else if(Launch.getOs() == Launch.OS_WINDOWS) {
        checkWindows();
      }
      else if(Launch.getOs() == Launch.OS_MAC && !TVBrowser.isTransportable()) {
        File tvbprotocol = new File("/Applications/TV-Browser Protocol.app");
        
        if(!tvbprotocol.exists()) {
          DontShowAgainOptionBox.showOptionDialog("tvbProtocolMissing", UiUtilities.getParentFrameOnMouseScreen(), LOCALIZER.msg("error.mac.msg", "Receiving protocol message with tvb:\\ is activated.\nProtocol message make it easier to configure TV-Browser.\nBut the protocol app is missing.\n\nPlease make sure to also install the TV-Browser Protocol app from the DMG with TV-Browser."), LOCALIZER.msg("error.mac.title", "tvb:\\ protocol app missing"), JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        }
      }
    }
  }
  
  public static synchronized ProtocolHandler getInstance() {
    if(INSTANCE == null) {
      new ProtocolHandler();
    }
    
    return INSTANCE;
  }
  
  /**
   * Handles tvb:// protocol messages
   * 
   * @param message The tvb:// protocol message to handle
   */
  public synchronized void handleMessage(final String message) {
    try {
      Thread.sleep(500);
    } catch (InterruptedException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    
    if(Settings.propCanReceiveProtocolMessages.getBoolean() && message != null && message.startsWith("tvb://")) {
      final String[] parts = message.substring(6).strip().split("/");
      
      if(parts.length > 1) {
        if(MESSAGE_CONFIG.equalsIgnoreCase(parts[0]) && parts[1].contains("=")) {
          configMessage(parts);
        }
        else if(MESSAGE_SHOW.equalsIgnoreCase(parts[0]) && (parts.length == 2 || parts.length == 3) && parts[parts.length-1].contains("=")) {
          showMessage(parts);
        }
        else if(MESSAGE_PLUGIN.equalsIgnoreCase(parts[0]) && parts.length >= 3) {
          pluginMessage(parts);
        }
        else if(MESSAGE_SEARCH.equalsIgnoreCase(parts[0]) && parts.length == 3) {
          searchMessage(parts);
        }
      }
    }
  }
  
  // URL for searches, values in () are optional
  // tvb://search/TYPE/text=TEXT(;where=VALUES;casesensitive=true/1)
  private void searchMessage(String[] parts) {
    SearchFormSettings settings = new SearchFormSettings("");
    
    if(MESSAGE_SEARCH_TYPE_EXACTLY.equals(parts[1])) {
      settings.setSearcherType(PluginManager.TYPE_SEARCHER_EXACTLY);
    }
    else if(MESSAGE_SEARCH_TYPE_WHOLE_TERM.equals(parts[1])) {
      settings.setSearcherType(PluginManager.TYPE_SEARCHER_WHOLE_TERM);
    }
    else if(MESSAGE_SEARCH_TYPE_REGEX.equals(parts[1])) {
      settings.setSearcherType(PluginManager.TYPE_SEARCHER_REGULAR_EXPRESSION);
    }
    else if(MESSAGE_SEARCH_TYPE_BOOL.equals(parts[1])) {
      settings.setSearcherType(PluginManager.TYPE_SEARCHER_BOOLEAN);
    }
    else if(MESSAGE_SEARCH_TYPE_KEYWORD.equals(parts[1])) {
      settings.setSearcherType(PluginManager.TYPE_SEARCHER_KEYWORD);
    }
    
    String[] values = parts[2].split(";");
    
    String searchText = null;
    
    for(String value : values) {
      int index = value.indexOf("=");
      
      if(index != -1) {
        String key = value.substring(0,index);
        String v = unescape(value.substring(index+1));
        
        if(key.equals(KEY_SEARCH_WHERE)) {
          if(v.equals(VALUE_SEARCH_WHERE_ALL)) {
            settings.setSearchIn(SearchFormSettings.SEARCH_IN_ALL);
          }
          else if(v.equals(VALUE_SEARCH_WHERE_TITLE)) {
            settings.setSearchIn(SearchFormSettings.SEARCH_IN_TITLE);
          }
          else if(v.contains(",")) {
            String[] fields = v.split(",");
            
            ArrayList<ProgramFieldType> progTypes = new ArrayList<ProgramFieldType>();
            
            for(String f : fields) {
              try {
                Field field = ProgramFieldType.class.getDeclaredField(f);
                field.setAccessible(true);
                progTypes.add((ProgramFieldType)field.get(null));
              } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // Ignore
              }
            }
            
            if(!progTypes.isEmpty()) {
              settings.setSearchIn(SearchFormSettings.SEARCH_IN_USER_DEFINED);
              settings.setUserDefinedFieldTypes(progTypes.toArray(new ProgramFieldType[0]));
            }
          }
        }
        else if(key.equals(KEY_SEARCH_TEXT)) {
          searchText = v;
        }
        else if(key.equals(KEY_SEARCH_CASE_SENSITIVE)) {
          settings.setCaseSensitive(v.equals("true") || v.equals("1"));
        }
      }
    }
    
    if(!searchText.isBlank()) {
      SearchPlugin.getInstance().openSearchDialog(searchText, settings, true);
    }
  }
  
  private void configMessage(String[] parts) {
    if(JOptionPane.YES_OPTION == UiUtilities.showConfirmDialogOnMouseScreen(LOCALIZER.msg("receive.config.msg","TV-Browser received changes of settings.\nIf you haven't triggered the change, please cancel it now!\n\nDo you want to apply the changed settings?"), LOCALIZER.msg("receive.config.title","Apply settings change?"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, true)) {
      try {
        String[] props = parts[1].split(";");
        
        for(String prop : props) {
          String name = prop.substring(0,prop.indexOf("="));
          
          if(!name.equals("CanReceiveProtocolMessages") && !name.equals("ServerRestoreEnabled")) {
            Field f = Settings.class.getDeclaredField("prop"+name);
            Object p = f.get(null);
            String value = prop.substring(prop.indexOf("=")+1);
            
            if(p instanceof BooleanProperty) {
              if(value.equals("true") || value.equals("false") || value.equals("1") || value.equals("0")) {
                ((BooleanProperty) p).setBoolean(value.equals("true") || value.equals("1"));
              }
            }
            else if(p instanceof IntProperty) {
              try {
                int v = Integer.parseInt(value);
                
                ((IntProperty) p).setInt(v);
              }catch(NumberFormatException nfe) {
                nfe.printStackTrace();
              }
            }
            else if(p instanceof IntArrayProperty) {
              try {
                String[] values = value.split(",");
                int[] arr = new int[values.length];
                
                for(int i = 0; i < arr.length; i++) {
                  arr[i] = Integer.parseInt(values[i]);
                }
                
                ((IntArrayProperty) p).setIntArray(arr);
              }catch(NumberFormatException nfe) {
                nfe.printStackTrace();
              }
            }
            else if(p instanceof ByteProperty) {
              try {
                byte v = Byte.parseByte(value);
                
                ((ByteProperty) p).setByte(v);
              }catch(NumberFormatException nfe) {
                nfe.printStackTrace();
              }
            }
            else if(p instanceof StringProperty) {
              ((StringProperty) p).setString(unescape(value));
            }
            else if(p instanceof StringArrayProperty) {
              String[] temp = value.split(",");
              
              for(int i = 0; i < temp.length; i++) {
                temp[i] = unescape(temp[i]);
              }
              
              ((StringArrayProperty) p).setStringArray(temp);
            }
            else if(p instanceof ChoiceProperty) {
              String v = unescape(value);
              
              if(((ChoiceProperty) p).isAllowed(v)) {
                ((ChoiceProperty) p).setString(v);
              }
            }
            else if(p instanceof ColorProperty) {
              try {
                String[] values = value.split(",");
                Color c = null;
                
                if(values.length == 3) {
                  c = new Color(Integer.parseInt(values[0]),Integer.parseInt(values[1]),Integer.parseInt(values[2]));
                } else if(values.length == 4) {
                  c = new Color(Integer.parseInt(values[0]),Integer.parseInt(values[1]),Integer.parseInt(values[2]),Integer.parseInt(values[3]));
                }
                
                if(c != null) {
                  ((ColorProperty) p).setColor(c);
                }
              }catch(NumberFormatException nfe) {
                nfe.printStackTrace();
              }
            }
          }
        }
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      try {
        Settings.storeSettings(true);
      } catch (TvBrowserException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  private void showMessage(String[] parts) {
    if(parts[1].equals(MESSAGE_PROGRAM)) {
      showProgramMessage(parts);
    }
    else {
      String name = parts[1].substring(0,parts[1].indexOf("="));
      String value = parts[1].substring(parts[1].indexOf("=")+1);
      
      if(name.equals(MESSAGE_SETTINGS)) {
        SwingUtilities.invokeLater(() -> PluginManagerImpl.getInstance().showSettings((value.contains(".") ? "" : "#")+value));
      }
      else if(name.equals(MESSAGE_PLUGIN_UPDATE)) {
        boolean select = false;
        
        String[] subParts = value.split(";");
        StringBuilder search = new StringBuilder();
        
        for(String subPart : subParts) {
          if(subPart.toLowerCase().equals("select")) {
            select = true;
          }
          else {
            search.append(subPart);
          }
        }
        
        MainFrame.getInstance().showUpdatePluginsDlg(false,unescape(search.toString()),select);
      }
    }
  }
  
  // tvb://show/program/id=UNIQUEID
  private void showProgramMessage(String[] parts) {
    int index = parts[2].indexOf("=");
    
    if(index != -1) {
      String key = parts[2].substring(0,index);
      String v = unescape(parts[2].substring(index+1));
      
      if(key.equals(KEY_SHOW_PROGRAM_ID)) {
        Program p = PluginManagerImpl.getInstance().getProgram(v);
        
        if(p != null) {
          ProgramInfo.getInstance().showProgramInformation(p);
        }
        
        return;
      }
    }
  }
  
  private void pluginMessage(String[] parts) {
    if(MESSAGE_ENABLE.contentEquals(parts[1]) && parts.length == 3 && parts[2].contains("=")) {
      pluginEnableMessage(parts);
    }
    else if(MESSAGE_CONFIG.equals(parts[1]) && parts.length == 4) {
      pluginConfigMessage(parts);
    }
  }
  
  private void pluginEnableMessage(String[] parts) {
  //"NewsPlugin"
    String[] values = parts[2].split("=");
    
    PluginProxy p = PluginProxyManager.getInstance().getPluginForId("java."+values[0].toLowerCase()+"."+values[0]);
    
    if(p != null) {
      if(!p.isActivated() && (values[1].equals("true") || values[1].equals("1"))) {
        if(JOptionPane.YES_OPTION == UiUtilities.showConfirmDialogOnMouseScreen(LOCALIZER.msg("receive.plugin.enable.msg","TV-Browser received the activation of the plugin '{0}'.\n\nDo you wan't to activate the plugin '{0}' now?", p.getInfo().getName()),LOCALIZER.msg("receive.plugin.enable.title","Activate plugin '{0}'?", p.getInfo().getName()), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
          try {
            PluginProxyManager.getInstance().activatePlugin(p, true);
              try {
                PluginProxyManager.getInstance().fireTvBrowserStartFinished(p);
              }catch(Throwable t) {
                /* Catch all possible not catched errors that occur in the plugin mehtod*/
                LOG.log(Level.WARNING, "A not catched error occured in 'fireTvBrowserStartFinishedThread' of Plugin '" + p +"'.", t);
              }
          } catch (TvBrowserException e) {
            e.printStackTrace();
          }
          
          MainFrame.getInstance().getToolbar().updatePluginButtons();
          MainFrame.getInstance().updatePluginsMenu();
        }
      }
      else if(p.isActivated() && (values[1].equals("false") || values[1].equals("0"))) {
        if(JOptionPane.YES_OPTION == UiUtilities.showConfirmDialogOnMouseScreen(LOCALIZER.msg("receive.plugin.disable.msg","TV-Browser received the deactivation of the plugin '{0}'.\n\nDo you wan't to deactivate the plugin '{0}' now?", p.getInfo().getName()),LOCALIZER.msg("receive.plugin.disable.title","Dectivate plugin '{0}'?", p.getInfo().getName()), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
          try {
            PluginProxyManager.getInstance().deactivatePlugin(p);
          } catch (TvBrowserException e) {
            e.printStackTrace();
          }
          
          MainFrame.getInstance().getToolbar().updatePluginButtons();
          MainFrame.getInstance().updatePluginsMenu();
        }
      }
    }
    
    // Update the settings
    String[] deactivatedPlugins = PluginProxyManager.getInstance().getDeactivatedPluginIds();
    Settings.propDeactivatedPlugins.setStringArray(deactivatedPlugins);

    try {
      Settings.storeSettings(true);
    } catch (TvBrowserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private void pluginConfigMessage(String[] parts) {
    PluginProxy a = PluginProxyManager.getInstance().getActivatedPluginForId("java."+parts[2].toLowerCase()+"."+parts[2]);
    
    if(a != null) {
      a.receiveValues(ProgramReceiveTarget.TYPE_EVENT_UNDIFINED, parts[3].split(";"), null);
    }
    else {
      TvDataServiceProxy[] ps = TvDataServiceProxyManager.getInstance().getTvDataServices(new String[] {parts[2].toLowerCase()+"."+parts[2]});
      
      if(ps.length == 1 && ps[0].getId().equals(parts[2].toLowerCase()+"."+parts[2])) {
        ps[0].receiveProtocolMessage(parts[3].split(";"));
      }
    }
  }
  
  public void handleSettingsChanged() {
    if(mIsEnabled && !Settings.propCanReceiveProtocolMessages.getBoolean()) {
      disable(true);
    }
    else if(!mIsEnabled && Settings.propCanReceiveProtocolMessages.getBoolean()) {
      enable(true);
    }
  }
  
  private void checkWindows() {
    RegistryKey rKey = new RegistryKey(RegistryKey.HKEY_CURRENT_USER, "SOFTWARE\\classes\\tvb\\shell\\open\\command");
    RegistryValue v = rKey.getValue("");
    File exe = new File(TVBrowser.isTransportable() ? "tvbrowser-transportable.exe" : "tvbrowser.exe");
    
    if((v == null || !v.getData().contains(exe.getAbsolutePath()))) {
      if(DontShowAgainOptionBox.showOptionDialog("tvbProtocolWrongTarget", UiUtilities.getParentFrameOnMouseScreen(), LOCALIZER.msg("error.win.msg", "Receiving protocol message with tvb:\\ is activated.\nProtocol message make it easier to configure TV-Browser.\nBut the protocol doesn't exists in Windows or leads to another TV-Browser.\n\nShould the protocol be created in Windows now to lead to this TV-Browser (Administrator rights are needed for this)?\n(The protocol messages are deactivted for this TV-Browser if not.)"), LOCALIZER.msg("error.win.title", "tvb:\\ protocol error"), JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
        enableWindows();
      }
      else {
        Settings.propCanReceiveProtocolMessages.setBoolean(false);
        mIsEnabled = false;
        try {
          Settings.storeSettings(true);
        } catch (TvBrowserException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }
  
  private void enable(boolean fromSettings) {
    if(Launch.getOs() == Launch.OS_LINUX) {
      File test = new File(System.getProperty("user.home")+"/.local/share/applications/tvbrowserWebstart.desktop");
      
      if(test.isFile()) {
        test.delete();
      }
      
      File target = new File("/usr/share/applications/tvbrowser.desktop");
      File baseDir = new File("");
      String name = "tvbrowser.desktop";
      
      if(!target.isFile() || !baseDir.getAbsolutePath().equals("/usr/share/tvbrowser")) {
        target = test;
        
        if(target.isFile()) {
          target.delete();
        }
        
        createDesktopFile(target, "TV-Browser Webstart", true);
        name = "tvbrowserWebstart.desktop";
      }
      
      ExecutionHandler h = ExecutionHandler.create("/usr/bin/xdg-mime","default",name,"x-scheme-handler/tvb");
      try {
        h.execute();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      h = ExecutionHandler.create("/usr/bin/xdg-desktop-menu","forceupdate");
      try {
        h.execute();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    else if(Launch.getOs() == Launch.OS_WINDOWS) {
      enableWindows();
    }
    
    mIsEnabled = true;
  }
  
  private void enableWindows() {
    File exe = new File(TVBrowser.isTransportable() ? "tvbrowser-transportable.exe" : "tvbrowser.exe");
    RegistryKey key = new RegistryKey(RegistryKey.HKEY_CURRENT_USER, "SOFTWARE\\Classes\\tvb");
    key.setValue(new RegistryValue("\"\"", RegistryValue.TYPE_REG_SZ, "URL:tvb Protocol"));
    key.setValue(new RegistryValue("URL Protocol", RegistryValue.TYPE_REG_SZ, "\"\""));
    
    key = new RegistryKey(RegistryKey.HKEY_CURRENT_USER, "SOFTWARE\\Classes\\tvb\\DefaultIcon");
    key.setValue(new RegistryValue("\"\"", RegistryValue.TYPE_REG_SZ, "\\\""+exe.getAbsolutePath()+"\\\""));
    
    key = new RegistryKey(RegistryKey.HKEY_CURRENT_USER, "SOFTWARE\\Classes\\tvb\\shell\\open\\command");
    key.setValue(new RegistryValue("\"\"", RegistryValue.TYPE_REG_SZ, "\\\""+exe.getAbsolutePath()+"\\\" \\\"%1\\\""));
  }
  
  private void disable(boolean fromSettings) {
    if(Launch.getOs() == Launch.OS_LINUX) {
      final File target = new File(System.getProperty("user.home")+"/.local/share/applications/tvbrowserWebstart.desktop");
      
      if(target.isFile()) {
        target.delete();
      }
      
      ExecutionHandler h = ExecutionHandler.create("/usr/bin/sed","-i","/x-scheme-handler\\/tvb=/d",System.getProperty("user.home")+"/.config/mimeapps.list");
      try {
        h.execute();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      h = ExecutionHandler.create("/usr/bin/xdg-desktop-menu","forceupdate");
      try {
        h.execute();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    else if(Launch.getOs() == Launch.OS_WINDOWS) {
      RegistryKey key = new RegistryKey(RegistryKey.HKEY_CURRENT_USER, "SOFTWARE\\Classes\\tvb");
      key.delete();
    }
    
    mIsEnabled = false;
  }
  
  public static void createDesktopFile(final File target, final String name, final boolean isMimeHandler) {
    final File baseDir = new File("");
    
    try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target), "UTF-8"))) {
      out.write("[Desktop Entry]\n");
      out.write("Version=1.0\n");
      out.write("Type=Application\n");
      out.write("Terminal=false\n");
      out.write("Name="+name+"\n");
      out.write("Icon="+baseDir.getAbsolutePath()+"/imgs/tvbrowser128.png\n");
      out.write("Exec="+baseDir.getAbsolutePath()+"/tvbrowser"+(TVBrowser.isTransportable() ? "-transportable":"")+".sh %u\n");
      out.write("Comment=Themeable and easy to use TV Guide - written in Java\n");
      out.write("Categories=Video;AudioVideo;TV\n");
      /*if(isMimeHandler) {
        out.write("MimeType=x-scheme-handler/tvb;\n");
      }*/
      
      out.write("Name[de]="+name+"\n");
      out.write("GenericName=Digital TV Guide\n");
      out.write("GenericName[de]=Digitale TV-Zeitschrift\n");
      out.write("Comment[de]=Anpassbare und einfach zu benutzende TV-Zeitschrift - geschrieben in Java\n");
      out.write("StartupWMClass=tvbrowser-TVBrowser\n");
    }catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }
  
  private static String unescape(String value) {
    return value.replace(ESCAPE_COMMA, ",").replace(ESCAPE_QUOTE, "\"").replace(ESCAPE_SEMICOLON, ";").replace(ESCAPE_SLASH, "/");
  }
}
