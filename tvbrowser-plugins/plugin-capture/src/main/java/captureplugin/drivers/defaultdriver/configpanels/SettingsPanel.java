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
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */
package captureplugin.drivers.defaultdriver.configpanels;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import captureplugin.CapturePlugin;
import captureplugin.drivers.defaultdriver.DeviceConfig;
import devplugin.ProgramReceiveTarget;
import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.ProgramReceiveTargetSelectionPanel;
import util.ui.ScrollableJPanel;
import util.ui.UiUtilities;


/**
 * Creates the Settings-Panel
 * 
 * @author bodum
 */
public class SettingsPanel extends ScrollableJPanel implements ActionListener, ChangeListener {

    /** Translator */
    private static final Localizer LOCALIZER = Localizer.getLocalizerFor(SettingsPanel.class);

    /** GUI */
    private JSpinner mPreTimeSpinner;

    private JSpinner mPostTimeTextField;
    
    private JTextField mUserName = new JTextField();
    private JPasswordField mUserPwd = new JPasswordField();
    
    
    private JSpinner mMaxTimeout;
    
    private JSpinner mMaxSimult;
    
    
    /** Settings */
    private DeviceConfig mData;
    
    private JCheckBox mCheckReturn, mShowOnError, mShowTitleAndTimeDialog, mOldPrograms,
                      mUseTime, mDeleteRemovedPrograms, mUseTimeOffsetForAllCommands;
    
    private JComboBox mTimeZones;

    private JLabel mTimeZoneLabel;
    
    private ProgramReceiveTargetSelectionPanel mProgramReceiveTargetSelection;

    /**
     * Creates the SettingsPanel
     * @param data Settings
     */
    public SettingsPanel(DeviceConfig data) {
        mData = data;
        createPanel();
    }
    
    /**
     * creates a JPanel for getting the time offsets
     */
    private void createPanel() {
      EnhancedPanelBuilder pb = new EnhancedPanelBuilder("5dlu,12dlu,pref:grow,5dlu,pref:grow,5dlu", this);
      setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
      
      mPreTimeSpinner = new JSpinner(new SpinnerNumberModel(mData.getPreTime(), 0, null, 1));
      mPostTimeTextField = new JSpinner(new SpinnerNumberModel(mData.getPostTime(), 0, null, 1));
      mUseTimeOffsetForAllCommands = new JCheckBox(LOCALIZER.msg("timeOffsetForAll", "Also use time offset for additional commands"),mData.getUseTimeOffsetForAllCommands());
      
      mUserName.setText(mData.getUsername());
      mUserPwd.setText(mData.getPassword());
      
      mMaxSimult = new JSpinner(new SpinnerNumberModel(mData.getMaxSimultanious(), 1, null, 1));
      mMaxTimeout = new JSpinner(new SpinnerNumberModel(mData.getTimeout(), -1, 999, 1));

      mCheckReturn = new JCheckBox(LOCALIZER.msg("CheckError", "Check if returns Error"), mData.useReturnValue());
      mShowOnError = new JCheckBox(LOCALIZER.msg("ShowResultOnError","Show Result-Dialog only on Error"), mData.getDialogOnlyOnError());
      mShowTitleAndTimeDialog = new JCheckBox(LOCALIZER.msg("showTitleAndTime", "Show title and time settings dialog"), mData.getShowTitleAndTimeDialog());
      mDeleteRemovedPrograms = new JCheckBox(LOCALIZER.msg("autoDeletePrograms", "Automatically delete programs that were removed during a data update"), mData.getDeleteRemovedPrograms());
      mOldPrograms = new JCheckBox(LOCALIZER.msg("OnlyFuture", "Only allow Programs that are in the future"), mData.getOnlyFuturePrograms());
      
      mUseTime = new JCheckBox(LOCALIZER.msg("useSystemTimezone","Use timezone provided by OS"), !mData.useTimeZone());
      
      String[] zoneIds = new String[0];
      try {
        zoneIds = TimeZone.getAvailableIDs();
      } catch (Exception e) {
        e.printStackTrace();
      }
      mTimeZones = new JComboBox(zoneIds);
      mTimeZones.setEnabled(mData.useTimeZone() && mTimeZones.getItemCount() > 0);
      
      for (int i=0; i<zoneIds.length; i++) {
        if (zoneIds[i].equals(mData.getTimeZone().getID())) {
          mTimeZones.setSelectedIndex(i); break;
        }
      }
      
      pb.addParagraph(LOCALIZER.msg("TimeSettings", "Timesettings"));
      pb.addRow();
      pb.add(new JLabel(LOCALIZER.msg("Earlier", "Number of minutes to start erlier")), 2, 2);
      pb.add(mPreTimeSpinner, 5);
      pb.addRow();
      pb.add(new JLabel(LOCALIZER.msg("Later", "Number of minutes to stop later")), 2, 2);
      pb.add(mPostTimeTextField, 5);
      pb.addRow();
      pb.add(mUseTimeOffsetForAllCommands, 2, 4);

      pb.addParagraph(LOCALIZER.msg("User", "User"));
      pb.addRow();
      pb.add(new JLabel(LOCALIZER.msg("Username", "Username") + ":"), 2, 2);
      pb.add(mUserName, 5);
      pb.addRow();
      pb.add(new JLabel(LOCALIZER.msg("Password", "Password") + ":"), 2, 2);
      pb.add(mUserPwd, 5);

      pb.addParagraph(LOCALIZER.msg("Additional", "Additional"));
      pb.addRow();
      pb.add(new JLabel(LOCALIZER.msg("MaxSimult","Maximum simultaneous recordings")+ ":"), 2, 2);
      pb.add(mMaxSimult, 5);
      pb.addRow();
      pb.add(new JLabel(LOCALIZER.msg("Timeout","Wait sec. until Timeout (-1 = disabled)")+ ":"), 2, 2);
      pb.add(mMaxTimeout, 5);
      pb.addRow();
      pb.add(mCheckReturn, 2, 4);
      pb.addRow(false);
      pb.add(mShowOnError, 2, 4);
      pb.addRow(false);
      pb.add(mShowTitleAndTimeDialog, 2, 4);
      pb.addRow(false);
      pb.add(mDeleteRemovedPrograms, 2, 4);
      pb.addRow(false);
      pb.add(mOldPrograms, 2, 4);
      pb.addRow();
      pb.add(mUseTime, 2, 4);
      
      JPanel timeZonePanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
      mTimeZoneLabel = new JLabel(LOCALIZER.msg("Timezone","Timezone")+": ");
      mTimeZoneLabel.setEnabled(mTimeZones.isEnabled());
      timeZonePanel.add(mTimeZoneLabel);
      timeZonePanel.add(mTimeZones);
      
      pb.addRow(false);
      pb.add(timeZonePanel, 3, 3);
      
      ProgramReceiveTarget[] targets = mData.getProgramReceiveTargets();
      
      ArrayList<ProgramReceiveTarget> existing = new ArrayList<ProgramReceiveTarget>();
      
      for(ProgramReceiveTarget target : targets) {
        if(target.getReceifeIfForIdOfTarget() != null) {
          existing.add(target);
        }
      }
        
      mProgramReceiveTargetSelection = new ProgramReceiveTargetSelectionPanel(UiUtilities.getLastModalChildOf(CapturePlugin.getInstance().getSuperFrame()),
          existing.toArray(new ProgramReceiveTarget[existing.size()]),null,CapturePlugin.getInstance(),true,LOCALIZER.msg("sendToTitle","Send scheduled programs to:"));
      mProgramReceiveTargetSelection.addChangeListener(this);
      pb.addRow();
      pb.add(mProgramReceiveTargetSelection, 1, 5);
      
      // add ChangeListener to the spinners
      mPreTimeSpinner.addChangeListener(this);
      mPostTimeTextField.addChangeListener(this);
      mMaxSimult.addChangeListener(this);
      mMaxTimeout.addChangeListener(this);
      
      // add ActionListener to the check boxes
      mCheckReturn.addActionListener(this);
      mShowOnError.addActionListener(this);
      mShowTitleAndTimeDialog.addActionListener(this);
      mDeleteRemovedPrograms.addActionListener(this);
      mOldPrograms.addActionListener(this);
      mUseTime.addActionListener(this);
      mUseTimeOffsetForAllCommands.addActionListener(this);
      
      mUserName.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          mData.setUserName(mUserName.getText());
        }
      });
      
      mUserPwd.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          mData.setPassword(new String(mUserPwd.getPassword()));
        }
      });
    
      mTimeZones.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          mData.setTimeZone(TimeZone.getTimeZone((String)mTimeZones.getSelectedItem()));
        }
      });
      
      setOpaque(true);
    }

    public void actionPerformed(ActionEvent e) {
      if(e.getSource().equals(mCheckReturn)) {
        mData.setUseReturnValue(mCheckReturn.isSelected());
      } else if(e.getSource().equals(mShowOnError)) {
        mData.setDialogOnlyOnError(mShowOnError.isSelected());
      } else if(e.getSource().equals(mShowTitleAndTimeDialog)) {
        mData.setShowTitleAndTimeDialog(mShowTitleAndTimeDialog.isSelected());
      } else if(e.getSource().equals(mDeleteRemovedPrograms)) {
        mData.setDeleteRemovedPrograms(mDeleteRemovedPrograms.isSelected());
      } else if(e.getSource().equals(mOldPrograms)) {
        mData.setOnlyFuturePrograms(mOldPrograms.isSelected());
      } else if(e.getSource().equals(mUseTime)) {
        mData.setUseTimeZone(!mUseTime.isSelected());
        mTimeZones.setEnabled(!mUseTime.isSelected());
        mTimeZoneLabel.setEnabled(mTimeZones.isEnabled());
      } else if(e.getSource().equals(mUseTimeOffsetForAllCommands)) {
        mData.setUseTimeOffsetForAllCommands(mUseTimeOffsetForAllCommands.isSelected());
      }
      
    }

    public void stateChanged(ChangeEvent e) {
      if(e.getSource().equals(mMaxSimult)) {
        mData.setMaxSimultanious((Integer) mMaxSimult.getValue());
      } else if(e.getSource().equals(mMaxTimeout)) {
        mData.setTimeout((Integer) mMaxTimeout.getValue());
      } else if(e.getSource().equals(mPreTimeSpinner)) {
        mData.setPreTime((Integer) mPreTimeSpinner.getValue());
      } else if(e.getSource().equals(mPostTimeTextField)) {
        mData.setPostTime((Integer) mPostTimeTextField.getValue());
      } else if(e.getSource().equals(mProgramReceiveTargetSelection)) {
        mData.setProgramReceiveTargets(mProgramReceiveTargetSelection.getCurrentSelection());
      }
    }
    


}