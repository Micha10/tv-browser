package androidsync;

import java.util.ArrayList;

import devplugin.Plugin;
import devplugin.PluginCommunication;

public class AndroidSyncCommunication extends PluginCommunication {
  private AndroidSync mPlugin;
  
  AndroidSyncCommunication(AndroidSync plugin) {
    mPlugin = plugin;
  }
  
  @Override
  public int getVersion() {
    return 2;
  }

  public String[] getStoredChannels() {
    String[] result = null;
    
    if(Plugin.getPluginManager().getActivatedPluginForId(mPlugin.getId()) != null) {
      result = mPlugin.getStoredChannels();
    }
    
    return result;
  }
  
  public String[] getFavorites() {
    String[] result = null;
    
    if(Plugin.getPluginManager().getActivatedPluginForId(mPlugin.getId()) != null) {
      result = mPlugin.download(AndroidSync.PREF_DOWN_SYNC_ADDRESS,false,false);
      
      if(result != null) {
        ArrayList<String> values = new ArrayList<String>();
        
        for(String r : result) {
          if(r.startsWith("favorite:")) {
            values.add(r.substring(r.indexOf("=")+1));
          }
        }
        
        result = values.toArray(new String[0]);
      }
    }
    
    return result;
  }
}
