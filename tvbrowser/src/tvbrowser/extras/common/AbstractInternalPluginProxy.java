/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package tvbrowser.extras.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import devplugin.Program;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;

public abstract class AbstractInternalPluginProxy implements InternalPluginProxyIf, ProgramReceiveIf {

  @Override
  public int compareTo(ProgramReceiveIf other) {
    if (other instanceof InternalPluginProxyIf) {
      return getName().compareTo(((InternalPluginProxyIf) other).getName());
    }
    return getName().compareTo(other.toString());
  }
  
  @Override
  public boolean canReceiveProgramsWithTarget() {
    return false;
  }
  
  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return null;
  }
  
  @Override
  @Deprecated(since="4.2.2") public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
    return receivePrograms(ProgramReceiveTarget.TYPE_EVENT_UNDIFINED, programArr, receiveTarget);
  }
  
  @Override
  @Deprecated(since="4.2.2") public boolean receiveValues(String[] values, ProgramReceiveTarget receiveTarget) {
    return receiveValues(ProgramReceiveTarget.TYPE_EVENT_UNDIFINED, values, receiveTarget);
  }
  
  @Override
  public boolean receivePrograms(int type, Program[] programArr, ProgramReceiveTarget receiveTarget) {
    return receivePrograms(programArr, receiveTarget);
  }
  
  @Override
  public boolean receiveValues(int type, String[] values, ProgramReceiveTarget receiveTarget) {
    return receiveValues(values, receiveTarget);
  }
  
  @Override
  public String toString() {
    return getName();
  }
  
  @Override
  public String getBaseFileName() {
    return "java." + getId();
  }
  
  @Override
  public boolean hasToSaveSettings() {
    return true;
  }
  
  @Override
  public void loadSettings(Properties prop) {
    
  }
  
  public Properties storeSettings() {
    return null;
  };
  
  @Override
  public void writeData(ObjectOutputStream out) throws IOException {
    
  }
  
  @Override
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    
  }
  
}
