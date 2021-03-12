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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.core;

import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import devplugin.Date;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.update.PluginAutoUpdater;
import util.browserlauncher.Launch;
import util.exc.TvBrowserException;
import util.io.ExecutionHandler;
import util.io.IOUtilities;
import util.io.Mirror;
import util.io.PropertiesSorted;
import util.i18n.Localizer;
import util.ui.UiUtilities;

/**
 * A class to search for updates, the download and installation of the TV-Browser JRE.
 * <p>
 * @author René Mach
 * @since 4.1
 */
public class JREUpdater {
  /** The name of the updates file. */
  public static final String FILENAME = "tvbjre";
  /** The default download url */
  public static final String DEFAULT_DOWNLOAD_URL = "http://download.tvbrowser.org/jre/";
 
	private static final Localizer LOCALIZER = Localizer.getLocalizerFor(JREUpdater.class);
	public static final int INTERVAL = 13;
	
	public static boolean checkForUpdate(final JLabel infoLabel) {
	  boolean result = false;
	  
		if(hasTvBrowserJRE() && Settings.propJreUpdateEnabled.getBoolean()) {
			infoLabel.setText(LOCALIZER.msg("info.info","Searching for TV-Browser JRE updates..."));
			final File temp = new File(System.getProperty("java.io.tmpdir"),"tvbjre");
			
			if(temp.isFile()) {
				temp.delete();
			}
			
			try {
			  String url = DEFAULT_DOWNLOAD_URL + FILENAME;
			  
			  if(PluginAutoUpdater.downloadMirrorList()) {
			    final Mirror mirror = PluginAutoUpdater.getPluginUpdatesMirror();
			    
			    if(mirror != null) {
			      url = mirror.getUrl();
			    }
			    
			    if(!url.endsWith("/")) {
			      url += "/";
			    }
			    
			    url += FILENAME;
			  }
			  
			  String[] parts = null;
			  
			  try {
  				if(IOUtilities.download(new java.net.URL(url), temp, 10000)) {
  				  parts = new String(IOUtilities.getBytesFromFile(temp)).split("\n");
  				  
  				  if(parts.length < 4) {
  				    throw new IOException();
  				  }
  				}
			  }catch(IOException ioe1) {
		        if(IOUtilities.download(new java.net.URL(DEFAULT_DOWNLOAD_URL+FILENAME), temp, 10000)) {
		          parts = new String(IOUtilities.getBytesFromFile(temp)).split("\n");
		        }
			  }
			  
			  String md5_32 = null;
			  String md5_64 = null;
			  
			  if(parts != null && parts.length == 4) {
				  md5_32 = parts[2];
				  md5_64 = parts[3];
			  }
			  			  
				if(parts != null && parts.length == 2) {
					Settings.propJreUpdateDateLast.setDate(Date.getCurrentDate());
					
					final String currentVersion = System.getProperty("java.version");
					final String[] sParts = parts[0].trim().split("\\.");
					String[] cParts = currentVersion.split("\\.");
					
					if(cParts.length < sParts.length) {
						String[] newArr = new String[sParts.length];
						System.arraycopy(cParts, 0, newArr, 0, cParts.length);
						
						for(int i = cParts.length; i < sParts.length; i++) {
							newArr[i] = "0";
						}
						
						cParts = newArr;
					}
					
					boolean update = false;
					
					for(int i = 0; i < Math.min(cParts.length, sParts.length); i++) {
						if(Integer.parseInt(cParts[i]) < Integer.parseInt(sParts[i])) {
							update = true;
							break;
						} else if(Integer.parseInt(cParts[i]) > Integer.parseInt(sParts[i])) {
							break;
						}
					}
					
					if(update) {
						String bits = System.getProperty("sun.arch.data.model");
						
						final String downloadUrl = parts[1].replace("%version%", parts[0]).replace("%arch%", "win"+bits);
						final File target = new File(Settings.getUserSettingsDirName(),"tvbrowser-jre_"+parts[0]+"_win"+bits+".exe");
						
						if(!target.isFile()) {
							IOUtilities.download(new java.net.URL(downloadUrl), target, 30000);
						}
						
						if(target.isFile()) {
						  String md5 = IOUtilities.getMD5Hash(target);
						  
						  if(bits.equals("64")) {
							result = md5_64 == null || md5.equals(md5_64);
						  }
						  else {
							result = md5_32 == null || md5.equals(md5_32);  
						  }
						  
						  if(result) {
							Settings.propJreUpdate.setString(target.getAbsolutePath());
							handlePossibleUpdate();
						  }
						  else {
							target.delete();
						  }
						}
					}
					
					temp.delete();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			infoLabel.setText("");
		}
		
		return result;
	}
	
	public static boolean hasTvBrowserJRE() {
	  boolean result = false;
	  
	  if(Launch.getOs() == Launch.OS_WINDOWS) {
      final File release = new File("java"+File.separator+"release");
      
      if(release.isFile()) {
        PropertiesSorted prop = PropertiesSorted.load(release);
        
        result = prop.getProperty("tvbjre", "false").equals("true");
      }
	  }
	  
	  return result;
	}
	
	public static void handlePossibleUpdate() {
		if(hasTvBrowserJRE() && !Settings.propJreUpdate.getString().equals(Settings.propJreUpdate.getDefault())) {
			if(JOptionPane.showOptionDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), LOCALIZER.msg("info.message", "A New TV-Browser JRE is available for installation.\n\nIt's important to install JRE updates as soon as possible\nto prevent possible security risks.\n\nDo you want to install it now?"), LOCALIZER.msg("info.header", "New TV-Browser JRE available"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[] {LOCALIZER.msg("info.install","Install JRE now"),Localizer.getLocalization(Localizer.I18N_CANCEL)}, null) == JOptionPane.YES_OPTION) {
				Settings.propJreUpdate.setString(Settings.propJreUpdate.getString()+";install");
				MainFrame.getInstance().quit();
			}
		}
		else {
		  File[] installer = new File(Settings.getUserSettingsDirName()).listFiles((f) -> {return f.getName().toLowerCase().endsWith(".exe") && f.getName().contains("tvbrowser-jre");});
		  
		  if(installer != null) {
		    for(File file : installer) {
		      if(!file.delete()) {
		        file.deleteOnExit();
		      }
		    }
		  }
		}
	}
	
	public static void doUpdateIfAvailable() {
		if(hasTvBrowserJRE() && Settings.propJreUpdate.getString().endsWith(";install")) {
			String file = Settings.propJreUpdate.getString().substring(0, Settings.propJreUpdate.getString().lastIndexOf(";"));
			Settings.propJreUpdate.resetToDefault();
			try {
				Settings.storeSettings(false);
			} catch (TvBrowserException e1) {}
			
			ExecutionHandler ex = new ExecutionHandler(new String[] {"cmd","/c",file});
			try {
				ex.execute();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
