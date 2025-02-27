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
 *     $Date: 2007-01-03 09:06:40 +0100 (Mi, 03 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2979 $
 */
package captureplugin.drivers.dreambox;

import java.awt.Window;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

import javax.swing.JOptionPane;

import captureplugin.drivers.Command;
import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;
import captureplugin.drivers.dreambox.connector.DreamboxChannel;
import captureplugin.drivers.dreambox.connector.DreamboxConnector;
import captureplugin.drivers.dreambox.connector.cs.DreamboxOptionPane;
import captureplugin.drivers.dreambox.connector.cs.E2LocationHelper;
import captureplugin.drivers.dreambox.connector.cs.E2MovieHelper;
import captureplugin.drivers.dreambox.connector.cs.E2ServiceHelper;
import captureplugin.drivers.dreambox.connector.cs.E2TimerHelper;
import captureplugin.drivers.dreambox.connector.cs.ProgramOptionPanel;
import captureplugin.drivers.utils.ProgramTime;
import captureplugin.drivers.utils.ProgramTimeDialog;
import captureplugin.utils.ExternalChannelIf;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.Version;
import util.paramhandler.ParamParser;
import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * The Dreambox-Device
 * 
 * adopted by fishhead
 */
public final class DreamboxDevice implements DeviceIf {
    /**
     * Translator
     */
    private static final Localizer LOCALIZER = Localizer.getLocalizerFor(DreamboxDevice.class);
    
    /**
     * Driver
     */
    private DreamboxDriver mDriver;
    /**
     * Name for this Device
     */
    private String mName;
    /**
     * Configuration for this Device
     */
    private DreamboxConfig mConfig;
    /**
     * List of Recordings
     */
    private ArrayList<ProgramTime> mProgramTimeList = new ArrayList<ProgramTime>();
    /**
     * List of Recordings
     */
    private ArrayList<Program> mProgramList = new ArrayList<Program>();
    private DreamboxConnector mConnector;
    private int mActionIdLast;
    
    /**
     * Creates this Device
     *
     * @param dreamboxDriver Driver for the Dreambox
     * @param name           Name for this Device
     */
    public DreamboxDevice(DreamboxDriver dreamboxDriver, String name, int actionIdLast) {
        mDriver = dreamboxDriver;
        mName = name;
        mActionIdLast = actionIdLast;
        mConfig = new DreamboxConfig();
        mConnector = new DreamboxConnector(mConfig,mName);
    }

    /**
     * Clones another dreambox device
     *
     * @param dreamboxDevice Device to clone
     */
    public DreamboxDevice(DreamboxDevice dreamboxDevice) {
        mDriver = (DreamboxDriver) dreamboxDevice.getDriver();
        mName = dreamboxDevice.getName();
        mConfig = dreamboxDevice.getConfig().clone();
        mConnector = new DreamboxConnector(mConfig,mName);
    }

    /**
     * @return Configuration for this device
     */
    private DreamboxConfig getConfig() {
        return mConfig;
    }

    /**
     * @return ID for this Device
     */
    public String getId() {
        return mConfig.getId();
    }

    /**
     * @return Name for this Device
     */
    public String getName() {
        return mName;
    }

    /**
     * Set the new Name for this Device
     *
     * @param name new Name
     * @return new Name
     */
    public String setName(String name) {
        mName = name;
        return mName;
    }

    /**
     * @return Driver for this Device
     */
    public DriverIf getDriver() {
        return mDriver;
    }

    /**
     * Opens a configure dialog for this device
     *
     * @param parent Parent for the dialog
     */
    public void configDevice(Window parent) {
        DreamboxConfigDialog dialog = new DreamboxConfigDialog(parent, this, mConnector);

        UiUtilities.centerAndShow(dialog);

        if (dialog.wasOkPressed()) {
            mName = dialog.getDeviceName();
            mConfig = dialog.getConfig();
            mConnector.setConfig(mConfig);
        }
    }

    /**
     * @see captureplugin.drivers.DeviceIf#isInList(devplugin.Program)
     */
    public boolean isInList(Program program) {
        return mProgramList.contains(program);
    }

    /**
     * @see captureplugin.drivers.DeviceIf#isAbleToAddAndRemovePrograms()
     */
    public boolean isAbleToAddAndRemovePrograms() {
        return true;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#add(java.awt.Window,devplugin.Program)
     */
    @Override
    public boolean add(Window parent, Program program, boolean noGui) {
        if(!mConnector.isAccessible()) {
          JOptionPane.showMessageDialog(parent,
              LOCALIZER.msg("boxNotAccessible.msg","The box '{0}' with the address '{1}' is not accessible.\nAction '{2}' not possible.",mName,mConfig.getDreamboxAddress(),LOCALIZER.msg("boxNotAccessible.addTimer","Add Timer")),
              LOCALIZER.msg("boxNotAccessible.title","Box not accessible"),
              JOptionPane.ERROR_MESSAGE);
          return false;
        }
        
        if (program.isExpired()) {
            JOptionPane.showMessageDialog(parent,
                    LOCALIZER.msg("expiredText","This program has expired. It's not possible to record it.\nWell, unless you have a time-machine."),
                    LOCALIZER.msg("expiredTitle","Expired"),
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        final DreamboxChannel channel = (DreamboxChannel) mConfig.getExternalChannel(program.getChannel());

        if (channel == null) {
            int ret = JOptionPane.showConfirmDialog(parent,
                    LOCALIZER.msg("notConfiguredText", "Channel not configured, do\nyou want to do this now?"),
                    LOCALIZER.msg("notConfiguredTitle", "Configure"), JOptionPane.YES_NO_OPTION);

            if (ret == JOptionPane.YES_OPTION) {
                configDevice(parent);
            }
        } else {
            ProgramTimeDialog dialog;

            // fishhead ------------------------
            // read actual timers
            E2TimerHelper timerHelper = E2TimerHelper.getInstance(mConnector);
            E2LocationHelper locationHelper = E2LocationHelper.getInstance(mConnector, timerHelper.getThread());
            E2MovieHelper movieThread = E2MovieHelper.getInstance(mConnector, locationHelper.getThread());
            // fishhead ------------------------

            ProgramTime time = new ProgramTime(program);

            Calendar start = time.getStartAsCalendar();
            start.add(Calendar.MINUTE, mConfig.getPreTime()*-1);
            time.setStart(start.getTime());

            Calendar end = time.getEndAsCalendar();
            end.add(Calendar.MINUTE, mConfig.getAfterTime());
            time.setEnd(end.getTime());

            // fishhead ------------------------
            ProgramOptionPanel pgmOptPanel = new ProgramOptionPanel(locationHelper, movieThread, mConfig);
            int info = time.getProgram().getInfo();
            boolean useHdService = (info & Program.INFO_VISION_HD) == Program.INFO_VISION_HD;
            pgmOptPanel.setUseHdService(useHdService);
            pgmOptPanel.setUseDescription(true);
            pgmOptPanel.setZapBeforeEvent(Boolean.getBoolean("captureplugin.ProgramOptionPanel.beforeEvent"));
            if (!E2ServiceHelper.hasHdService(channel.getReference())) {
                pgmOptPanel.hideUseHdService();
            }

            dialog = new ProgramTimeDialog(parent, time, false, LOCALIZER.msg("afterEventTitle", "After recording"),
                    pgmOptPanel);

            if(!noGui) {
              UiUtilities.centerAndShow(dialog);
            }

            ProgramTime prgTime = dialog.getPrgTime();
            if (prgTime != null) {
                boolean added = false;

                // REC
                Map<String, String> timerRec = timerHelper.createRecTimer(channel, prgTime, pgmOptPanel
                        .getSelectedAfterEvent(), pgmOptPanel.getRepeated(), mConfig.getTimeZone(), pgmOptPanel
                        .getSelectedLocation(), pgmOptPanel.getSelectedTag(), pgmOptPanel.isUseHdService(), pgmOptPanel
                        .isUsingDescription());

                if (!pgmOptPanel.isOnlyCreateZapTimer()) {
                    // Timer programmieren
                    added = mConnector.addRecording(timerRec, timerHelper);
                }

                // ZAP before
                if (pgmOptPanel.isOnlyCreateZapTimer() || (added && pgmOptPanel.isZapBeforeEvent())) {
                    // vorher auf Sender schalten
                    Map<String, String> zapBeforeTimer = timerHelper.createZapBeforeTimer(timerRec);
                    timerHelper.timerAdd(zapBeforeTimer);
                }

                // ZAP after
                if (Boolean.getBoolean("captureplugin.ProgramOptionPanel.switchToSd") && pgmOptPanel.isUseHdService()) {
                    // nachher auf SD-Sender schalten
                    Map<String, String> zapAfterTimer = timerHelper.createZapAfterTimer(timerRec);
                    timerHelper.timerAdd(zapAfterTimer);
                }

                // Timer neu einlesen
                timerHelper.refresh();

                return added;
                // fishhead ------------------------
            }
        }
        
        return false;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#remove(java.awt.Window,devplugin.Program)
     */
    @Override
    public boolean remove(Window parent, Program program, boolean noGui) {
      if(!mConnector.isAccessible()) {
        JOptionPane.showMessageDialog(parent,
            LOCALIZER.msg("boxNotAccessible.msg","The box '{0}' with the address '{1}' is not accessible.\nAction '{2}' not possible.",mName,mConfig.getDreamboxAddress(),LOCALIZER.msg("boxNotAccessible.removeTimer","Remove Timer")),
            LOCALIZER.msg("boxNotAccessible.title","Box not accessible"),
            JOptionPane.ERROR_MESSAGE);
      }
      else {
        for (ProgramTime time : mProgramTimeList) {
            if (time.getProgram().equals(program)) {
                ExternalChannelIf channel = mConfig.getExternalChannel(program.getChannel());
                if (channel != null) {
                    return mConnector.removeRecording((DreamboxChannel) channel, time, mConfig.getTimeZone());
                }
            }
        }
      }
      
      return false;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#getProgramList()
     */
    public Program[] getProgramList() {
        if (mConnector.isAccessible()) {
            ProgramTime[] times = mConnector.getRecordings(mConfig);
            mProgramTimeList = new ArrayList<ProgramTime>(Arrays.asList(times));

            mProgramList = new ArrayList<Program>();

            for (ProgramTime time : times) {
              Program[] progs = time.getAllPrograms();
              
              for(Program p : progs) {
                if(!mProgramList.contains(p)) {
                  mProgramList.add(time.getProgram());
                }
              }
            }

            return mProgramList.toArray(new Program[0]);
        }

        return null;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#getAdditionalCommands()
     */
    public Command[] getAdditionalCommands() {
        return new Command[] { 
                new Command(mActionIdLast+5, LOCALIZER.msg("switch", "Switch channel")),
                new Command(mActionIdLast+6, LOCALIZER.msg("sendMessage", "Send as Message")),
                new Command(mActionIdLast+7, LOCALIZER.msg("streamChannel", "Open channel with mediaplayer")),
                // fishhead ------------------------
                new Command(mActionIdLast+8, LOCALIZER.msg("timerlist", "Show Timerlist")) };
                // fishhead ------------------------
    }

    /**
     * @see captureplugin.drivers.DeviceIf#executeAdditionalCommand(java.awt.Window,int,devplugin.Program)
     */
    public boolean executeAdditionalCommand(Window parent, int num, Program program) {
        if(!mConnector.isAccessible()) {
          JOptionPane.showMessageDialog(parent,
              LOCALIZER.msg("boxNotAccessible.msg","The box '{0}' with the address '{1}' is not accessible.\nAction '{2}' not possible.",mName,mConfig.getDreamboxAddress(),getAdditionalCommands()[num]),
              LOCALIZER.msg("boxNotAccessible.title","Box not accessible"),
              JOptionPane.ERROR_MESSAGE);
        }
        else {
          if (num == 0) {
              final DreamboxChannel channel = (DreamboxChannel) mConfig.getExternalChannel(program.getChannel());
  
              if (channel != null) {
                  new Thread(new Runnable() {
                      public void run() {
                          mConnector.switchToChannel(channel);
                      }
                  }).start();
              } else {
                  int ret = JOptionPane.showConfirmDialog(parent,
                          LOCALIZER.msg("notConfiguredText", "Channel not configured, do\nyou want to do this now?"),
                          LOCALIZER.msg("notConfiguredTitle", "Configure"), JOptionPane.YES_NO_OPTION);
  
                  if (ret == JOptionPane.YES_OPTION) {
                      configDevice(parent);
                  }
              }
              return true;
          } else if (num == 1) {
              ParamParser parser = new ParamParser();
  
              mConnector.sendMessage(parser.analyse("{channel_name} - {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n{title}", program));
          } else if (num == 2) {
              final DreamboxChannel channel = (DreamboxChannel) mConfig.getExternalChannel(program.getChannel());
            
              if (channel != null) {
                  if (!mConnector.streamChannel(channel)) {
                      int ret = JOptionPane.showConfirmDialog(parent,
                          LOCALIZER.msg("mediaplayerNotConfiguredText", "Unfortunately, a problem occurred during executing the mediaplayer,\ndo you want to correct the configuration now?"),
                          LOCALIZER.msg("mediaplayerNotConfiguredTitle", "Configure"), JOptionPane.YES_NO_OPTION);
                      if (ret == JOptionPane.YES_OPTION) {
                          configDevice(parent);
                      }
                  }
              }
              // fishhead ------------------------
          } else if (num == 3) {
              DreamboxOptionPane.showTimer(mConnector);
              return true;
              // fishhead ------------------------
          }
        }
        return false;
    }

    public Object clone() {
        return new DreamboxDevice(this);
    }

    /**
     * @see captureplugin.drivers.DeviceIf#writeData(java.io.ObjectOutputStream)
     */
    public void writeData(ObjectOutputStream stream) throws IOException {
        mConfig.writeData(stream);
    }

    /**
     * @see captureplugin.drivers.DeviceIf#readData(java.io.ObjectInputStream, boolean)
     */
    public void readData(ObjectInputStream stream, boolean importDevice) throws IOException, ClassNotFoundException {
        mConfig = new DreamboxConfig(stream);
        mConnector.setConfig(mConfig);
    }

    /**
     * @see captureplugin.drivers.DeviceIf#checkProgramsAfterDataUpdateAndGetDeleted()
     */
    public Program[] checkProgramsAfterDataUpdateAndGetDeleted() {
        return new Program[0];
    }

    /**
     * @see captureplugin.drivers.DeviceIf#getDeleteRemovedProgramsAutomatically()
     */
    public boolean getDeleteRemovedProgramsAutomatically() {
        return true;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#removeProgramWithoutExecution(devplugin.Program)
     */
    public void removeProgramWithoutExecution(Program p) {
        for (ProgramTime time : mProgramTimeList) {
            if (time.getProgram().equals(p)) {
                DreamboxChannel channel = (DreamboxChannel) mConfig.getExternalChannel(p.getChannel());
                if (channel != null) {
                    mConnector.removeRecording(channel, time, mConfig.getTimeZone());
                }
            }
        }
    }
    
    @Override
    public Program getProgramForProgramInList(Program p) {
      for(ProgramTime time : mProgramTimeList) {
        for(Program prog : time.getAllPrograms()) {
          if(prog.equals(p)) {
            return time.getProgram();
          }
        }
      }
      
      return null;
    }

    @Override
    public void sendProgramsToReceiveTargets(Program[] progs) {
      ProgramReceiveTarget[] targets = mConfig.getProgramReceiveTargets();
      
      for(ProgramReceiveTarget target : targets) {
        target.receivePrograms(progs);
      }
    }

    @Override
    public void handleTvBrowserVersionUpdate(Version previousVersion) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public int getActionIdLast() {
      return mActionIdLast;
    }
}