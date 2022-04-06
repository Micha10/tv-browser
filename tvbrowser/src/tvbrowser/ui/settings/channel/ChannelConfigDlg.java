/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.settings.channel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Calendar;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import util.i18n.Localizer;
import util.io.IOUtilities;
import util.ui.CaretPositionCorrector;
import util.ui.ColorButton;
import util.ui.EnhancedPanelBuilder;
import util.ui.ImageIconEnhanced;
import util.ui.ImageUtilities;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * This dialog enables the user to change every setting in the channel appearance
 * 
 * @author bodum
 * @since 2.1
 */
public class ChannelConfigDlg extends JDialog implements ActionListener, WindowClosingIf {
  /** Localizer */
  static final util.i18n.Localizer LOCALIZER = util.i18n.Localizer.getLocalizerFor(ChannelConfigDlg.class);
  /** Current Channel */
  private Channel mChannel;
  /** Close/OK Buttons */
  private JButton mCloseBt, mOKBt;
  /** The Correction-Time*/
  private JComboBox<String> mCorrectionCB;
  
  /** File for Icon */
  private File mIconFile;

  /** Button to Change FileName */
  private JButton mChangeIcon;

  /** Channel-Name */
  private JTextField mChannelName;
  /** Channel-WebPage*/
  private JTextField mWebPage;
  
  /** Start time limit selection */
  private JSpinner mStartTimeLimit;
  /** End time limit selection */
  private JSpinner mEndTimeLimit;
  private JLabel mIconLabel;

  /** The sort number of the channel*/
  private JTextField mSortNumber;
  
  /** Enabe user background color */
  private JCheckBox mUseUserBackground;
  
  /** User background color selection */
  private ColorButton mSelectBackgroundColor;
  
  /**
   * Create the Dialog
   * 
   * @param parent
   *          Parent
   * @param channel
   *          Channel to show
   * @since 3.0
   */
  public ChannelConfigDlg(Window parent, Channel channel) {
    super(parent, LOCALIZER.msg("configChannel", "Configure Channel"));
    setModalityType(ModalityType.DOCUMENT_MODAL);
    mChannel = channel;
    createDialog();
  }

  /**
   * Create the GUI
   */
  private void createDialog() {
    EnhancedPanelBuilder panel = new EnhancedPanelBuilder(new FormLayout("default, 3dlu, fill:default:grow"),"3dlu", (JPanel) getContentPane());

    UiUtilities.registerForClosing(this);
    
    panel.border(Borders.DLU4);
    
    // sort number
    panel.addLabelRow(false, LOCALIZER.msg("channelNumber", "Sort number:"), 1);
    panel.add(mSortNumber = new JTextField(mChannel.getSortNumber()), 3);
    
    // name
    panel.addLabelRow(LOCALIZER.msg("channelName", "Channel Name:"), 1);
    panel.add(mChannelName = new JTextField(mChannel.getName()), 3);
    
    // provider
    panel.addLabelRow(LOCALIZER.msg("provider", "Provided by:"), 1);
    panel.labelAdd(ChannelUtil.getProviderName(mChannel), 3);
    
    // logo
    panel.addLabelRow(LOCALIZER.msg("channelLogo", "Channel Logo:"), 1);
    if (mChannel.getUserIconFileName() != null) {
      mIconFile = new File(IOUtilities.translateRelativePath(mChannel.getUserIconFileName()));
    }
    
    mIconLabel = new JLabel(createUserIcon());
    mChangeIcon = new JButton(LOCALIZER.msg("useIcon", "Select channel icon"));
    mChangeIcon.addActionListener(e -> {
      changeIcon();
    });

    panel.add(mIconLabel, 3);
    panel.addRow(mChangeIcon, 3);
    
    // background color
    
    mUseUserBackground = new JCheckBox(LOCALIZER.msg("backgroundColorUse", "User defined background color"), mChannel.isUsingUserBackgroundColor());
    mSelectBackgroundColor = new ColorButton(mChannel.isUsingUserBackgroundColor() ? mChannel.getUserBackgroundColor() : Color.white);
    mSelectBackgroundColor.setEnabled(mUseUserBackground.isSelected());
    
    mUseUserBackground.addItemListener(e -> {
      mSelectBackgroundColor.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
    });
    
    panel.addRow(mUseUserBackground, 3);
    panel.addRow(mSelectBackgroundColor, 3);
    
    // URL
    panel.addLabelRow(LOCALIZER.msg("webAddress", "Web Address:"), 1);
    mWebPage =new JTextField(mChannel.getWebpage());
    panel.add(mWebPage, 3);
    
    // time correction
    panel.addLabelRow(LOCALIZER.msg("time", "Time zone correction:"), 1);
    mCorrectionCB = new JComboBox<>(new String[] { "-1:00", "-0:45", "-0:30", "-0:15", "0:00", "+0:15", "+0:30", "+0:45", "+1:00" });
    mCorrectionCB.setSelectedIndex(mChannel.getTimeZoneCorrectionMinutes() / 15 + 4);
    panel.add(mCorrectionCB, 3);

    JTextArea txt = UiUtilities.createHelpTextArea(LOCALIZER.msg("DLSTNote", ""));
    // Hack because of growing JTextArea in FormLayout
    txt.setMinimumSize(new Dimension(150, 20));
    panel.addRow(txt, 3);

    // time limitation
    panel.addLabelRow(LOCALIZER.msg("timeLimits","Time limits:"), 1);
    
    String timePattern = LOCALIZER.msg("timePattern", "hh:mm a");
        
    mStartTimeLimit = new JSpinner(new SpinnerDateModel());
    mStartTimeLimit.setEditor(new JSpinner.DateEditor(mStartTimeLimit, timePattern));
    setTimeDate(mStartTimeLimit, mChannel.getStartTimeLimit());

    mEndTimeLimit = new JSpinner(new SpinnerDateModel());
    mEndTimeLimit.setEditor(new JSpinner.DateEditor(mEndTimeLimit, timePattern));
    setTimeDate(mEndTimeLimit, mChannel.getEndTimeLimit());
        
    ((JSpinner.DateEditor)mStartTimeLimit.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.LEFT);
    ((JSpinner.DateEditor)mEndTimeLimit.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.LEFT);
    
    CaretPositionCorrector.createCorrector(((JSpinner.DateEditor)mStartTimeLimit.getEditor()).getTextField(), new char[] {':'}, -1);
    CaretPositionCorrector.createCorrector(((JSpinner.DateEditor)mEndTimeLimit.getEditor()).getTextField(), new char[] {':'}, -1);
    
    EnhancedPanelBuilder timeLimitPanel = new EnhancedPanelBuilder(new FormLayout("default:grow,10dlu,default:grow"),"2dlu");
    timeLimitPanel.addLabelRow(false, LOCALIZER.msg("startTime","Start time:"), 1);
    timeLimitPanel.labelAdd(LOCALIZER.msg("endTime","End time:"), 3);
    timeLimitPanel.addRow(mStartTimeLimit, 1);
    timeLimitPanel.add(mEndTimeLimit, 3);
    
    panel.add(timeLimitPanel.getPanel(), 3);
    
    JTextArea txt2 = UiUtilities.createHelpTextArea(LOCALIZER.msg("DLSTNote", ""));
    // Hack because of growing JTextArea in FormLayout
    txt2.setMinimumSize(new Dimension(150, 20));
    panel.addRow(txt2, 3);

    // buttons
    ButtonBarBuilder builder = new ButtonBarBuilder();
    JButton defaultButton = new JButton(Localizer.getLocalization(Localizer.I18N_DEFAULT));
    
    defaultButton.addActionListener(e -> {
      resetToDefaults();
    });
    
    builder.addButton(defaultButton);
    builder.addRelatedGap();
    builder.addGlue();

    mOKBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    mOKBt.addActionListener(this);
    
    getRootPane().setDefaultButton(mOKBt);
    
    mCloseBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCloseBt.addActionListener(this);

    builder.addButton(new JButton[] { mOKBt, mCloseBt });
    
    panel.addRow("fill:0dlu:grow", false);
    panel.addRowFull("5dlu,default", new JSeparator());
    panel.addRowFull(builder.getPanel());
    
    pack();
    Settings.layoutWindow("channelConfig", this, new Dimension(420,420));
  }
  
  private void setTimeDate(JSpinner toSet, int time) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, time / 60);
    cal.set(Calendar.MINUTE, time % 60);
    
    toSet.setValue(cal.getTime());
  }
  
  private int getTimeInMinutes(JSpinner toGet) {
    Calendar cal = Calendar.getInstance();
    cal.setTime((java.util.Date)toGet.getValue());
    
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }

  /**
   * Reset the Channel to the Default-Values
   */
  private void resetToDefaults() {
    mSortNumber.setText("");
    mWebPage.setText(mChannel.getDefaultWebPage());
    mChannelName.setText(mChannel.getDefaultName());
    mIconFile = null;
    mIconLabel.setIcon(createUserIcon());
    mCorrectionCB.setSelectedIndex(mCorrectionCB.getItemCount() / 2);
    setTimeDate(mStartTimeLimit, 0);
    setTimeDate(mEndTimeLimit, 0);
  }

  /**
   * Create a User-Icon
   * @return Icon
   */
  private Icon createUserIcon() {
    Icon icon;
    if ((mIconFile != null) && (mIconFile.exists())) {
      Image img = ImageUtilities.createImageAsynchronous(mIconFile.getAbsolutePath());
      if (img != null) {
        icon = UiUtilities.createChannelIcon(new ImageIconEnhanced(img));
      } else {
        icon = UiUtilities.createChannelIcon(mChannel.getIcon());
      }
    } else {
      icon = UiUtilities.createChannelIcon(mChannel.getDefaultIcon());
    }

    return icon;
  }

  /**
   * Change the Icon
   */
  private void changeIcon() {
    JFileChooser fileChooser = new JFileChooser(mIconFile);
    String[] extArr = { ".jpg", ".jpeg", ".gif", ".png" };
    fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(extArr, ".jpg, .gif, .png"));
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      File selection = fileChooser.getSelectedFile();
      if (selection != null) {
        mIconFile = selection;
        mIconLabel.setIcon(createUserIcon());
      }
    }
  }

  /**
   * Center and Show the Dialog
   */
  public void centerAndShow() {
    UiUtilities.centerAndShow(this);
  }

  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    if (o == mOKBt) {
      int minutes = (mCorrectionCB.getSelectedIndex() - 4) * 15;
      mChannel.setTimeZoneCorrectionMinutes(minutes);
      mChannel.useUserIcon(mIconFile != null);
      if (mIconFile != null) {
        mChannel.setUserIconFileName(IOUtilities.checkForRelativePath(mIconFile.getAbsolutePath()));
      } else {
        mChannel.setUserIconFileName(null);
      }
      mChannel.setUserChannelName(mChannelName.getText());
      mChannel.setUserWebPage(mWebPage.getText());
      mChannel.setStartTimeLimit(getTimeInMinutes(mStartTimeLimit));
      mChannel.setEndTimeLimit(getTimeInMinutes(mEndTimeLimit));
      mChannel.setSortNumber(mSortNumber.getText().trim());
      mChannel.setUserBackgroundColor(mUseUserBackground.isSelected() ? mSelectBackgroundColor.getColor() : null);
      
      Settings.updateChannelFilters(ChannelList.getSubscribedChannels());
      
      setVisible(false);
    } else if (o == mCloseBt) {
      setVisible(false);
    }

  }

  public void close() {
    setVisible(false);
  }

}