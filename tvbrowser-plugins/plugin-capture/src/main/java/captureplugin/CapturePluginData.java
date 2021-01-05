/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *     $Date: 2009-09-27 22:30:36 +0200 (So, 27 Sep 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5980 $
 */
package captureplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Vector;

import util.exc.ErrorHandler;
import util.ui.Localizer;
import captureplugin.drivers.DeviceIf;
import devplugin.Program;


/**
 * This Class contains all needed Data
 */
public final class CapturePluginData implements Cloneable {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CapturePluginData.class);

    private int mMarkPriority = Program.MIN_MARK_PRIORITY;
    private int mPriorityMarkingMulti = Program.MAX_MARK_PRIORITY;
    
    private boolean mShowAdditionalCommandsOnTop = false;
    
    private int mWidthProgramTableColum1 = 200;
    private int mWidthProgramTableColum2 = 400;
    
    /**
     * All Devices
     */
    private Vector<DeviceIf> mDevices = new Vector<DeviceIf>();
    
    /**
     * Default Constructor
     */
    public CapturePluginData() {
        
    }

    /**
     * Copies Settings from another Data-Object
     * @param data Data object to copy
     */
    public CapturePluginData(CapturePluginData data) {
      
      mMarkPriority = data.getMarkPriority();
      
      mDevices = new Vector<DeviceIf>();
      
      Vector<DeviceIf> old = new Vector<DeviceIf>(data.getDevices());

        for (DeviceIf deviceIf : old) {
            mDevices.add((DeviceIf) deviceIf.clone());
        }
    }
    
    
    /**
     * Writes the Data to an OuputStream
     * 
     * @param out write here
     * @throws IOException problems while writing
     */
    public void writeData(ObjectOutputStream out) throws IOException {
        out.writeInt(6);
        
        out.writeInt(mMarkPriority);
        out.writeInt(mPriorityMarkingMulti);
        out.writeBoolean(mShowAdditionalCommandsOnTop);
        out.writeInt(mDevices.size());
        
        DeviceFileHandling writer = new DeviceFileHandling();
        
        writer.clearDirectory();

        for (DeviceIf dev : mDevices) {
            out.writeObject(dev.getDriver().getClass().getName());
            out.writeObject(dev.getName());
            out.writeObject(writer.writeDevice(dev));
        }
        
        out.writeInt(mWidthProgramTableColum1);
        out.writeInt(mWidthProgramTableColum2);
    }

    /**
     * Loads the Data from an InputStream
     * 
     * @param in InputStream
     * @param p Plugin
     * @throws IOException problems while reading
     * @throws ClassNotFoundException class creation problems
     */
    public void readData(ObjectInputStream in, CapturePlugin p) throws IOException, ClassNotFoundException {
        int version = in.readInt();
        
        if (version < 2) {
            return;
        }
        
        if(version >= 3) {
          mMarkPriority = in.readInt();
        }
        
        if(version >= 5) {
          mPriorityMarkingMulti = in.readInt();
        }
        
        if(version >= 4) {
          mShowAdditionalCommandsOnTop = in.readBoolean();
        }
        
        int num = in.readInt();
        
        mDevices = new Vector<DeviceIf>();
        
        DeviceFileHandling reader = new DeviceFileHandling();

        for (int i = 0; i < num; i++) {
            String classname = (String) in.readObject();
            String devname = (String)in.readObject();
            String filename = (String)in.readObject();
            try {
                DeviceIf dev = reader.readDevice(classname, filename, devname);
                
                if (dev != null) {
                    mDevices.add(dev);
                }
            } catch (Throwable e) {
                ErrorHandler.handle(mLocalizer.msg("ProblemDevice", "Problems while loading Device {0}.", devname),e);
            }
            
        }
        
        if(version > 5) {
          mWidthProgramTableColum1 = in.readInt();
          mWidthProgramTableColum2 = in.readInt();
        }
    }

    /**
     * Returns all Devices
     * @return Devices
     */
    public Collection<DeviceIf> getDevices() {
        return mDevices;
    }
    
    /**
     * Returns all Devices as Array
     * @return Devices
     */
    public DeviceIf[] getDeviceArray() {
        DeviceIf[] dev = new DeviceIf[mDevices.size()];
        
        for (int i = 0; i < mDevices.size(); i++) {
            dev[i] = mDevices.get(i);
        }
        
        return dev;
    }

    /**
     * Clones this Object
     */
    public Object clone() {
        return new CapturePluginData(this);
    }
    
    /**
     * Gets the mark priority used by capture plugin.
     * 
     * @return The current mark priority
     * @since 2.12
     */
    public int getMarkPriority() {
      return mMarkPriority;
    }
    
    public int getPriorityMarkingMulti() {
      return mPriorityMarkingMulti;
    }
    
    /**
     * Sets the mark priority used by capture plugin.
     * 
     * @param priorities The new mark priorities.
     * @since 3.14.18
     */
    public void setMarkPriority(int[] priorities) {
      mMarkPriority = priorities[0];
      
      if(priorities.length > 0) {
        mPriorityMarkingMulti = priorities[1];
      }
      
      for(DeviceIf device : mDevices) {
        Program[] programs = device.getProgramList();
        
        if (programs != null) {
          for(Program program : programs) {
            program.validateMarking();
          }
        }
      }
    }
    
    /**
     * Gets if the additional commands should be on top of context menu
     * <p>
     * @return If the additional commands should be on top of context menu.
     * @since 3.14
     */
    public boolean showAdditionalCommandsOnTop() {
      return mShowAdditionalCommandsOnTop;
    }
    
    /**
     * Sets the value for additional commands on top of context menu
     * <p>
     * @param value If the additional commands should be on top of context menu.
     * @since 3.14
     */
    public void setShowAdditionalCommandsOnTop(boolean value) {
      mShowAdditionalCommandsOnTop = value;
    }
    
    public int getWidthProgramTableColum1() {
      return mWidthProgramTableColum1;
    }
    
    public void setWidthProgramTableColum1(int width) {
      mWidthProgramTableColum1 = width;
    }
    
    public int getWidthProgramTableColum2() {
      return mWidthProgramTableColum2;
    }
    
    public void setWidthProgramTableColum2(int width) {
      mWidthProgramTableColum2 = width;
    }
}