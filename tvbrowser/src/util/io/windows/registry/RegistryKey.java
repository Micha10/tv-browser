/*
 * TV-Browser
 * Copyright (C) 2019 TV-Browser team (dev@tvbrowser.org)
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
package util.io.windows.registry;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.browserlauncher.Launch;
import util.io.ExecutionHandler;

/**
 * Class to access windows registry.
 * 
 * @author René Mach
 * @since 4.1
 */
public class RegistryKey {
	public static final String HKEY_LOCAL_MACHINE = "HKLM";
	public static final String HKEY_CURRENT_USER = "HKCU";
	public static final String HKEY_CLASS_ROOT = "HKCR";
	
	private String mKey;
	private String mPath;
	
	public static final File REG_TOOL = new File(System.getenv("windir")+File.separator+(System.getProperty("os.arch").contains("64") ? "SysWOW64" : "System32")+File.separator+"reg.exe");
	
	private static final Pattern PATTERN_QUERY = Pattern.compile("\\s{2,}(.*?)\\s+(REG_.*?)\\s+(.*?)$",Pattern.DOTALL);
	
	/**
	 * @return <code>true</code> if the registry is accessible, <code>false</code> otherwise.
	 */
	public static boolean isUsable() {
		return REG_TOOL.isFile();
	}
	
	/**
	 * Creates a registry key to access.
	 * 
	 * @param hkey The H key to access either {@value #HKEY_CURRENT_USER} or {@value #HKEY_LOCAL_MACHINE}
	 * @param path The path to access.
	 * @throws RuntimeException Thrown if the registry is not accessible or if operating system is not Windows.
	 */
	public RegistryKey(final String hkey, final String path) throws RuntimeException {
		if(!isUsable() || Launch.getOs() != Launch.OS_WINDOWS) {
			throw new RuntimeException("Reg tool '" + REG_TOOL.getAbsolutePath() + "' not available. No access to Windows Registry");
		}
		
		mKey = hkey;
		mPath = path;
	}
	
	
	/**
	 * Get the value of the given key.
	 * 
	 * @param key The key to get the value for.
	 * @return The result of the registry query.
	 */
	public RegistryValue getValue(String key) {
		final ArrayList<String> cmdList = new ArrayList<>();
		cmdList.add(REG_TOOL.getAbsolutePath());
		cmdList.add("query");
		cmdList.add(mKey + "\\" + mPath);
		
		if(key.equals(RegistryValue.DEFAULT)) {
			cmdList.add("/ve");
			key = "";
		}
		else if(!key.isBlank()) {
			cmdList.add("/v");
			cmdList.add(key);
		}
		
		final ExecutionHandler handler = new ExecutionHandler(cmdList.toArray(new String[cmdList.size()]));
		
		RegistryValue result = new RegistryValue(key, RegistryValue.TYPE_REG_UNKNOWN, "");
		
		try {
			handler.execute(true);
			handler.getProcess().waitFor();
			
			final Matcher m = PATTERN_QUERY.matcher(handler.getOutput());
			int pos = 0;
			
			while(m.find(pos)) {
				if(key.equals(m.group(1)) || key.isBlank()) {
					if("REG_DWORD".equals(m.group(2))) {
						result = new RegistryValue(key, RegistryValue.TYPE_REG_DWORD, String.valueOf(Long.parseLong(m.group(3).trim().replace("0x", ""), 16)));
					}
					else if("REG_SZ".equals(m.group(2))) {
						result = new RegistryValue(key, RegistryValue.TYPE_REG_SZ, m.group(3).trim());
					}
					else if("REG_QWORD".equals(m.group(2))) {
						result = new RegistryValue(key, RegistryValue.TYPE_REG_QWORD, String.valueOf(Long.parseLong(m.group(3).trim().replace("0x", ""))));
					}
					else if("REG_BINARY".equals(m.group(2))) {
						result = new RegistryValue(key, RegistryValue.TYPE_REG_BINARY, m.group(3).trim());
					}
					
					break;
				}
				
				pos = m.end();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Sets a value in this RegistryKey.
	 * 
	 * @param value The registry value to set.
	 * @return <code>true</code> if the value could be set, <code>false</code> otherwise.
	 * @since 4.2.1
	 */
	public boolean setValue(final RegistryValue value) {
	  boolean result = false;
	  
	  final ArrayList<String> cmdList = new ArrayList<>();
    cmdList.add(REG_TOOL.getAbsolutePath());
    cmdList.add("add");
    cmdList.add(mKey + "\\" + mPath);
    cmdList.add("/t");
    
    switch(value.getType()) {
      case RegistryValue.TYPE_REG_SZ:cmdList.add("REG_SZ");break;
      case RegistryValue.TYPE_REG_BINARY:cmdList.add("REG_BINARY");break;
      case RegistryValue.TYPE_REG_DWORD:cmdList.add("REG_DWORD");break;
      case RegistryValue.TYPE_REG_QWORD:cmdList.add("REG_QWORD");break;
      
      default: return result;
    }
    
    cmdList.add("/v");
    cmdList.add(value.getName());
    cmdList.add("/d");
    cmdList.add(value.getData());
    cmdList.add("/f");
    
    final ExecutionHandler handler = new ExecutionHandler(cmdList.toArray(new String[0]));
    try {
      handler.execute(true);
      handler.getProcess().waitFor();
      result = handler.getProcess().exitValue() != 0;
    }catch(Throwable t) {
      t.printStackTrace();
    }
	  
    return result;
	}
	
	/**
	 * Deletes the key and all values below it
	 * <p>
	 * @return <code>true</code> if the key was deleted, <code>false</code> otherwise.
	 * @since 4.2.3
	 */
	public boolean delete() {
	  boolean result = false;
	  
	  final ArrayList<String> cmdList = new ArrayList<>();
    cmdList.add(REG_TOOL.getAbsolutePath());
    cmdList.add("delete");
    cmdList.add(mKey + "\\" + mPath);
    cmdList.add("/f");
	  
    final ExecutionHandler handler = new ExecutionHandler(cmdList.toArray(new String[0]));
    try {
      handler.execute(true);
      handler.getProcess().waitFor();
      result = handler.getProcess().exitValue() != 0;
    }catch(Throwable t) {
      t.printStackTrace();
    }
    
	  return result;
	}
	
	public String getFullPath() {
    return mKey+"//"+mPath;
  }
}
