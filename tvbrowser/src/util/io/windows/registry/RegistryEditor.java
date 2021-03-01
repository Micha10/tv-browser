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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import util.io.ExecutionHandler;

/**
 * Class to change windows registry with admin rights.
 * 
 * @author René Mach
 * @since 4.2.3
 */
public class RegistryEditor {
  private static final File WSCRIPT = new File(System.getenv("windir")+File.separator+(System.getProperty("os.arch").contains("64") ? "SysWOW64" : "System32")+File.separator+"wscript.exe");  
  private Hashtable<String, ArrayList<RegistryValue>> mValueTable;
  
  private RegistryEditor() {
    mValueTable = new Hashtable<String, ArrayList<RegistryValue>>();
  }
  
  public static RegistryEditor create() {
    return new RegistryEditor();
  }
  
  public void setValue(final String path, RegistryValue value) {
    ArrayList<RegistryValue> list = mValueTable.get(path);
    
    if(list == null) {
      list = new ArrayList<RegistryValue>();
      mValueTable.put(path, list);
    }
    else {
      for(int i = list.size()-1; i >= 0; i--) {
        RegistryValue v = list.get(i);
        
        if(value.getName().equals(v.getName())) {
          list.remove(i);
          break;
        }
      }
    }
    
    if(value != null) {
      list.add(value);
    }
  }
  
  public void commit() {
    commit("regChange");
  }
  
  
  public void commit(String prefix) {
    try {
      File reg = File.createTempFile(prefix, ".reg");
      File vbs = File.createTempFile(prefix, ".vbs");
      
      try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reg), "ISO-8859-1"))) {
        out.write("REGEDIT4\r\n\r\n");
        
        final Set<String> keys = mValueTable.keySet();
        boolean first = true;
        
        for(String key : keys) {
          if(!first) {
            out.write("\r\n\r\n");
          }
          else {
            first = false;
          }
          
          out.write("[");
          out.write(key);
          out.write("]");
          
          final ArrayList<RegistryValue> list = mValueTable.get(key);
          final StringBuilder content = new StringBuilder();
          
          for(final RegistryValue value : list) {
            out.write("\r\n");
            content.setLength(0);
            
            if(value.getName().isBlank()) {
              content.append("@");
            }
            else {
              content.append("\"").append(value.getName().replace("\"", "\\\"")).append("\"");
            }
            
            content.append("=");
            
            if(value.isRegBinary()) {
              content.append("hex:");
              content.append(value.getData().replace(" ", ","));
            }
            else if(value.isRegDword()) {
              content.append("dword:");
              
              for(int i = value.getData().length(); i < 8; i++) {
                content.append("0");
              }
              
              content.append(value.getData());
            }
            else if(value.isRegQword()) {
              content.append("qword:");
              
              for(int i = value.getData().length(); i < 16; i++) {
                content.append("0");
              }
              
              content.append(value.getData());
            }
            else if(value.isRegSz()) {
              content.append("\"").append(value.getData().replace("\\", "\\\\").replace("\"", "\\\"")).append("\"");
            }
            
            String test = content.toString();
            
            if(test.length() > 79 && value.isRegBinary()) {
              out.write(test.substring(0,79));
              out.write("\\\r\n");
              
              int i = 79+77;
              
              for(; i < test.length(); i += 77) {
                out.write("  ");
                out.write(test.substring(i-77,i));
                if(i < test.length()-1) {
                  out.write("\\\r\n");
                }
              }
              
              if(i-77 < test.length()-1) {
                out.write("  ");
                out.write(test.substring(i-77,test.length()));
              }
            }
            else {
              out.write(test);
            }
          }
        }
      }
      
      if(reg.length() > 0) {
        try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(vbs), "ISO-8859-1"))) {
          out.write("Set UAC = CreateObject(\"Shell.Application\")\r\n");
          out.write("UAC.ShellExecute \"regedit.exe\", \""+reg.getAbsolutePath()+"\", \"\", \"runas\", 1");
        }catch(IOException ioe) {
          ioe.printStackTrace();
        }
        
        ExecutionHandler h = ExecutionHandler.create(WSCRIPT.getAbsolutePath(),vbs.getAbsolutePath());
        h.execute(true,true);
        try {
          h.getProcess().waitFor();
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        System.out.println(h.getOutput()+" ");
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
