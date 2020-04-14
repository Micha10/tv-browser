/*
 * ColorMe plugin for TV-Browser
 * Copyright (C) 2014 René Mach (rene@tvbrowser.org)
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
package colorme;

import java.awt.Cursor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.SwingUtilities;

import compat.PluginCompat;
import compat.ProgramCompat;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.ThemeIcon;
import devplugin.Version;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;

/**
 * A plugin that receives programs to color them with the received priority.
 * 
 * @author René Mach
 */
public class ColorMe extends Plugin {
  private static final Version VERSION = new Version(0,13,0,true);
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(ColorMe.class);
  
  private Hashtable<Integer, HashSet<Program>> mPrograms;
  private Hashtable<String, HashSet<Integer>> mPriorites;
  
  private ProgramReceiveTarget[] mReceiveTargets;
  private PluginsProgramFilter[] mAvailableFilter;
  
  private ThemeIcon mIcon;
  
  public ColorMe() {
    mPrograms = new Hashtable<Integer, HashSet<Program>>(5);
    mPriorites = new Hashtable<String, HashSet<Integer>>(0);
    
    mReceiveTargets = new ProgramReceiveTarget[0];
    mAvailableFilter = new PluginsProgramFilter[0];
    
    mIcon = new ThemeIcon("apps", "colorme", TVBrowserIcons.SIZE_SMALL);
  }
  
  @Override
  public void onActivation() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        updateReceiveTargets(getHighlightingPriorityMaximum(),true);
      }
    });
  }
  
  private int getHighlightingPriorityMaximum() {
    int priorityMax = Program.MAX_MARK_PRIORITY;
    
    try {
      priorityMax = (Integer)Program.class.getMethod("getHighlightingPriorityMaximum").invoke(null);
    }catch(Exception e) {
      // ignore
    }
    
    return priorityMax;
  }
  
  public static Version getVersion() {
    return VERSION;
  }
  
  @Override
  public PluginInfo getInfo() {
    return new PluginInfo(ColorMe.class, LOCALIZER.msg("name", "ColorMe"), LOCALIZER.msg("description", "Colors programs with priorities"), "René Mach", "GPL");
  }
  
  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return mIcon;
  }
  
  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }
  
  private void updateReceiveTargets(int priorityMax, boolean initial) {
    priorityMax++;
    final ProgramReceiveTarget[] newTargets = new ProgramReceiveTarget[priorityMax*2];
    final PluginsProgramFilter[] newFilters = new PluginsProgramFilter[priorityMax];
    
    for(int i = 0; i < priorityMax; i++) {
      if(i < mReceiveTargets.length/2) {
        newTargets[i] = mReceiveTargets[i];
        newFilters[i] = mAvailableFilter[i];
      }
      else {
        newTargets[i] = new ProgramReceiveTarget(this, LOCALIZER.msg("add", "Add: Priority") + " - " + (i+1), String.valueOf(i+1));
        
        final int priority = i;
        final String subName = LOCALIZER.msg("filter", "Priority") + " - " + (i+1);
        
        newFilters[i] = new PluginsProgramFilter(this) {
          @Override
          public boolean accept(Program program) {
            final HashSet<Program> programs = mPrograms.get(priority);
            
            return programs != null && programs.contains(program);
          }
          
          @Override
          public String getSubName() {
            return subName;
          }
        };
        
        if(!initial) {
          getPluginManager().getFilterManager().addFilter(newFilters[i]);
        }
      }
    }
    
    for(int i = priorityMax; i < mAvailableFilter.length; i++) {
      mToDelete = mAvailableFilter[i];
      getPluginManager().getFilterManager().deleteFilter(mToDelete);
      mToDelete = null;
    }
    
    int count = mReceiveTargets.length/2;
    
    for(int i = priorityMax; i < newTargets.length; i++) {
      if(count < mReceiveTargets.length) {
        newTargets[i] = mReceiveTargets[count++];
      }
      else {
        newTargets[i] = new ProgramReceiveTarget(this, LOCALIZER.msg("remove", "Remove: Priority:") + " - " + (i-priorityMax+1), String.valueOf(-((i-priorityMax)+1)));
      }
    }
    
    mReceiveTargets = newTargets;
    mAvailableFilter = newFilters;
    
  }
  
  private PluginsProgramFilter mToDelete = null;
  
  @Override
  public boolean isAllowedToDeleteProgramFilter(PluginsProgramFilter programFilter) {
    return mToDelete != null && programFilter.equals(mToDelete);
  }
  
  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return mReceiveTargets;
  }
  
  @Override
  public int getMarkPriorityForProgram(Program p) {
    final HashSet<Integer> priorities = mPriorites.get(p.getUniqueID());
    
    int found = Program.NO_MARK_PRIORITY;
    
    if(priorities != null) {
      for(Integer priority : priorities) {
        if(priority > found) {
          found = priority;
        }
      }
    }
    
    return found;
  }
  
  private void addProgramToSet(final Program p, final int priority) {
    HashSet<Program> set = mPrograms.get(priority);
    
    if(set == null) {
      set = new HashSet<Program>();
      mPrograms.put(priority, set);
    }
    
    set.add(p);
    
    HashSet<Integer> priorities = mPriorites.get(p.getUniqueID());
    
    if(priorities == null) {
      priorities = new HashSet<Integer>();
      mPriorites.put(p.getUniqueID(), priorities);
    }
    
    priorities.add(priority);
    
    if(priorities.size() < 2) {
      p.mark(ColorMe.this);
    }
    else {
      p.validateMarking();
    }
  }
  
  private void removeProgramFromSet(final Program p, final int priority) {
    final HashSet<Program> set = mPrograms.get(priority);
    
    if(set != null) {
      set.remove(p);
    }
    
    final HashSet<Integer> priorities = mPriorites.get(p.getUniqueID());
    
    if(priorities != null) {
      if(priorities.remove(priority)) {
        if(priorities.size() < 1) {
          p.unmark(this);
        }
        else {
          p.validateMarking();
        }
      }
    }
  }
  
  @Override
  public boolean receivePrograms(final Program[] programArr, ProgramReceiveTarget receiveTarget) {
    boolean returnValue = false;
    
   // HashSet<Program> toUse = null;
    boolean add = false;
    int priority = Program.NO_MARK_PRIORITY;
    
    if(receiveTarget.getReceiveIfId().equals(getId())) {
      int id = Integer.parseInt(receiveTarget.getTargetId());
      
      add = id>=0;
      priority = Math.abs(id)-1;
    }
    
    if(priority != Program.NO_MARK_PRIORITY) {
      Cursor old = getParentFrame().getCursor();
      
      getParentFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));
      
      for(Program p : programArr) {
        if(p != null) {
          if(add) {
            addProgramToSet(p, priority);
        /*    if(toUse.add(p)) {
              if(!programIsDoubleMarked(p)) {
                p.mark(ColorMe.this);
              }
              else {
                p.validateMarking();
              }
            }*/
          }
          else {
            removeProgramFromSet(p, priority);
           /* if(toUse.remove(p)) {
              if(!programIsDoubleMarked(p)) {
                p.unmark(ColorMe.this);
              }
              else {
                p.validateMarking();
              }
            }*/
          }
        }
      }
      
      getParentFrame().setCursor(old);
      
      returnValue = true;
      
      saveMe();
    }
    
    return returnValue;
  }
  
  @Override
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt(); // read version
    
    if(version == 1) {
      for(int priority = 0; priority < 5; priority++) {
        readSet(in, priority);
      }
    }
    else if(version >= 2) {
      final int n = in.readInt();
      
      for(int i = 0; i < n; i++) {
        int priority = in.readInt();
        readSet(in, priority);
      }
    }
  }
  
  private void readSet(ObjectInputStream in, final int priority) throws IOException {
    final HashSet<Program> set = new HashSet<Program>();
    
    int size = in.readInt();
    //TODO defer loading of programs to after
    for(int i = 0; i < size; i++) {
      String progID = in.readUTF();
      
      Program[] progs = ProgramCompat.getPrograms(progID);
      
      if(progs != null) {
        for(Program p : progs) {
          set.add(p);
          p.mark(ColorMe.this);
          
          HashSet<Integer> prio = mPriorites.get(p.getUniqueID());
          
          if(prio == null) {
            prio = new HashSet<Integer>();
            mPriorites.put(p.getUniqueID(), prio);
          }
          
          if(!prio.contains(priority)) {
            prio.add(priority);
          }
        }
      }
    }
    
    mPrograms.put(priority, set);
  }
  
  @Override
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version;
    
    out.writeInt(mPrograms.size());
    
    final Set<Integer> keys = mPrograms.keySet();
    
    for(Integer key : keys) {
      final HashSet<Program> programs = mPrograms.get(key);
      out.writeInt(key);
      
      out.writeInt(programs.size());
      
      for(Program p : programs) {
        out.writeUTF(p.getUniqueID());
      }
    }
  }
    
  @Override
  public PluginsProgramFilter[] getAvailableFilter() {
    return mAvailableFilter;
  }
    
  public boolean isAllowingArtificialPluginTree() {
    return false;
  }
  
  public String getPluginCategory() {
    return PluginCompat.CATEGORY_OTHER;
  }
  
  public void handleTvBrowserSettingsChanged() {
    int priorityMax = getHighlightingPriorityMaximum();
    
    if(mReceiveTargets.length != (priorityMax+1)*2) {
      updateReceiveTargets(priorityMax,false);
    }
  }
}
