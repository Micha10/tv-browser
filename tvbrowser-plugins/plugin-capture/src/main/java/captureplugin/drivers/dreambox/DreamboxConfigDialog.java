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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;

import captureplugin.CapturePlugin;
import captureplugin.drivers.dreambox.connector.DreamboxChannel;
import captureplugin.drivers.dreambox.connector.DreamboxConnector;
import captureplugin.drivers.dreambox.connector.cs.E2LocationHelper;
import captureplugin.ui.EnhancedPanelBuilder;
import captureplugin.utils.ConfigTableModel;
import captureplugin.utils.ExternalChannelIf;
import captureplugin.utils.ExternalChannelTableCellEditor;
import captureplugin.utils.ExternalChannelTableCellRenderer;
import devplugin.Channel;
import devplugin.Plugin;
import util.ui.Localizer;
import util.ui.ProgramReceiveTargetSelectionPanel;
import util.ui.ScrollableJPanel;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * The configuration dialog for the dreambox
 */
public class DreamboxConfigDialog extends JDialog implements WindowClosingIf {
    /**
     * Translator
     */
    private static final Localizer LOCALIZER = Localizer.getLocalizerFor(DreamboxConfigDialog.class);

    /** Configuration */
    private DreamboxConfig mConfig;
    /** Device */
    private DreamboxDevice mDevice;
    /** Was ok pressed ? */
    private boolean mOkPressed;
    /** The software version on the box */
    private JComboBox mSoftwareSelection;
    /** The default recording directory */
    private JComboBox mDefaultLocation;
    /** The default event after recording */
    private JComboBox mDefaultAfterEvent;
    /** IP-Address of the dreambox */
    private JTextField mDreamboxAddress;
    /** Device Name of the dreambox */
    private JTextField mDeviceName;
    /** Table with channel mappings */
    private JTable mTable;

    private SpinnerNumberModel mBeforeModel;
    private SpinnerNumberModel mAfterModel;
    private SpinnerNumberModel mTimeoutModel;

    private JComboBox mTimezone;
    private JTextField mUserName;
    private JPasswordField mPasswordField;

    private JTextField mMediaplayer;

    private JButton mRefreshButton;

    private ProgramReceiveTargetSelectionPanel mProgramReceiveTargetSelection;
    
    private DreamboxConnector mConnector;
    
    private Timer mTimeOutTimer;

  /**
   * Create the Dialog
   *
   * @param parent
   *          Parent-Frame
   * @param device
   *          Device to configure
   */
    public DreamboxConfigDialog(Window parent, DreamboxDevice device,
      DreamboxConnector connector) {
      super(parent);
      setModal(true);
      mConnector = connector;
      mConfig = connector.getConfig();
      mDevice = device;
      createGui();
    }

    /**
     * Create the GUI
     */
    private void createGui() {
        setTitle(LOCALIZER.msg("title", "Configure Dreambox"));

        UiUtilities.registerForClosing(this);

        JPanel basicPanelView = new ScrollableJPanel();
        EnhancedPanelBuilder basicPanel = new EnhancedPanelBuilder("2dlu, default, 3dlu, fill:min:grow, 3dlu, default, 3dlu, default", basicPanelView);
        basicPanelView.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        basicPanel.addParagraph(LOCALIZER.msg("misc", "Miscellaneous"));

        basicPanel.addRow("default");
        basicPanel.add(new JLabel(LOCALIZER.msg("name","Name:")), 2);
        mDeviceName = new JTextField(mDevice.getName());
        basicPanel.add(mDeviceName, 4);

//        basicPanel.addRow("default");
//        basicPanel.add(new JLabel(mLocalizer.msg("webIf","Webif:")), cc.xy(2,basicPanel.getRow()));

//        String[] values = {"ipkg",
//                           "opkg"};
//
//        mSoftwareSelection = new JComboBox(values);
//        mSoftwareSelection.setSelectedIndex(mConfig.isOpkg() ? 1 : 0);

//        basicPanel.add(mSoftwareSelection, cc.xy(4, basicPanel.getRow()));

        basicPanel.addRow("default");
        basicPanel.add(new JLabel(LOCALIZER.msg("ipaddress", "IP address")), 2);
        mDreamboxAddress = new JTextField(mConfig.getDreamboxAddress());
        basicPanel.add(mDreamboxAddress, 4);

        JButton help = new JButton(CapturePlugin.getInstance().createImageIcon("apps", "help-browser", 16));
        help.setToolTipText(Localizer.getLocalization(Localizer.I18N_HELP));
        help.setOpaque(false);
        help.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        basicPanel.add(help, 8);
        help.setVisible(false);

////////////////////////
        final JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        refreshPanel.setOpaque(false);

        mRefreshButton = new JButton(LOCALIZER.msg("refresh", "Refresh channel list"));
        mRefreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshChannelList();
            }
        });
        mRefreshButton.setIcon(TVBrowserIcons.refresh(TVBrowserIcons.SIZE_SMALL));
        mRefreshButton.setEnabled(mConfig.hasValidAddress());
        
        mDreamboxAddress.getDocument().addDocumentListener(new DocumentListener() {

          @Override
          public void removeUpdate(DocumentEvent e) {
            check(e);
          }

          @Override
          public void insertUpdate(DocumentEvent e) {
            check(e);
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
            check(e);
          }

          private void check(DocumentEvent e) {
            mRefreshButton.setEnabled(!mDreamboxAddress.getText().trim().isEmpty());
            
            if(mConnector.isTimedOut() && !mTimeOutTimer.isRunning()) {
              mTimeOutTimer.start();
            }
          }
        });

        refreshPanel.add(mRefreshButton);

        JButton attach = new JButton(LOCALIZER.msg("attach", "Attach"));
        attach.setToolTipText(LOCALIZER.msg("attachHelp", "Attach channels"));
        attach.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                attachChannels();
            }
        });

        refreshPanel.add(attach);
///////////////////////
        
        basicPanel.addRow("default");
        
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        
        final JPanel timedOutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        timedOutPanel.setOpaque(false);
        final JLabel timeOut = new JLabel(String.format("%02d", mConnector.getTimeoutResetSeconds(mDreamboxAddress.getText().trim())/60)+":"+String.format("%02d", mConnector.getTimeoutResetSeconds(mDreamboxAddress.getText().trim())%60));
        timeOut.setOpaque(true);
        timeOut.setBackground(new Color(255,96,96));
        timeOut.setForeground(Color.WHITE);
        
        statusPanel.add(refreshPanel, BorderLayout.NORTH);
        statusPanel.add(timedOutPanel, BorderLayout.SOUTH);
        
        mTimeOutTimer = new Timer(500, e -> {
          refreshPanel.setVisible(mConnector.getTimeoutResetSeconds(mConfig.getDreamboxAddress()) <= 0);
          timedOutPanel.setVisible(!refreshPanel.isVisible());
          
          int seconds = mConnector.getTimeoutResetSeconds(mDreamboxAddress.getText().trim());
          
          timeOut.setText(String.format("%02d", seconds/60)+":"+String.format("%02d", seconds%60));
          
          if(seconds <= 0) {
            mTimeOutTimer.stop();
            refreshPanel.setVisible(true);
            timedOutPanel.setVisible(false);
          }
        });
        mTimeOutTimer.setInitialDelay(0);
        
        basicPanel.add(statusPanel, 4);
        
        refreshPanel.setVisible(mConnector.getTimeoutResetSeconds(mConfig.getDreamboxAddress()) < 5);
        timedOutPanel.setVisible(!refreshPanel.isVisible());
        
        if(timedOutPanel.isVisible()) {
          mTimeOutTimer.start();
        }
        
        JLabel timeOutInfo = new JLabel(LOCALIZER.msg("timeOut.info", "Box access locked for:"));
        JButton timeOutReset = new JButton(LOCALIZER.msg("timeOut.reset", "Reset lock"));
        timeOutReset.addActionListener(e -> {
          mConnector.resetTimeout();
        });
        
        timedOutPanel.add(timeOutInfo);
        timedOutPanel.add(timeOut);
        timedOutPanel.add(timeOutReset);
        
        basicPanel.addRow("default");
        basicPanel.add(new JLabel(LOCALIZER.msg("preTime", "Time before in minutes:")), 2);

        mBeforeModel = new SpinnerNumberModel(mConfig.getPreTime(), 0, 60, 1);
        JSpinner beforeSpinner = new JSpinner(mBeforeModel);
        basicPanel.add(beforeSpinner, 4);

        basicPanel.addRow("default");
        basicPanel.add(new JLabel(LOCALIZER.msg("afterTime", "Time after in minutes:")), 2);

        mAfterModel = new SpinnerNumberModel(mConfig.getAfterTime(), 0, 60, 1);
        JSpinner afterSpinner = new JSpinner(mAfterModel);
        basicPanel.add(afterSpinner, 4);

        basicPanel.addParagraph(LOCALIZER.msg("channel", "Channel assignment"));

        mTable = new JTable(new ConfigTableModel(mConfig, LOCALIZER.msg("dreambox", "Dreambox channel")));
        mTable.getTableHeader().setReorderingAllowed(false);
        mTable.getColumnModel().getColumn(0).setCellRenderer(new util.ui.ChannelTableCellRenderer());
        mTable.getColumnModel().getColumn(1).setCellRenderer(new ExternalChannelTableCellRenderer());
        mTable.getColumnModel().getColumn(1).setCellEditor(new ExternalChannelTableCellEditor(mConfig));

        basicPanel.addRow("fill:75dlu:grow");
        basicPanel.add(new JScrollPane(mTable), 2, 7);

        JPanel extendedPanelView = new ScrollableJPanel();
        EnhancedPanelBuilder extendedPanel = new EnhancedPanelBuilder("2dlu, default, 3dlu, fill:default:grow, 3dlu, default, 5dlu", extendedPanelView);
        extendedPanelView.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        extendedPanel.addParagraph(LOCALIZER.msg("misc", "Miscellaneous"));

        extendedPanel.addRow("default");
        extendedPanel.add(new JLabel(LOCALIZER.msg("Timeout", "Timeout for connections in ms:")), 2);

        mTimeoutModel = new SpinnerNumberModel(mConfig.getTimeout(), 0, 100000, 10);
        JSpinner timeoutSpinner = new JSpinner(mTimeoutModel);
        extendedPanel.add(timeoutSpinner, 4, 3);

        extendedPanel.addParagraph(LOCALIZER.msg("timeZoneSeparator","Time zone"));

        extendedPanel.addRow("default");
        extendedPanel.add(new JLabel(LOCALIZER.msg("timeZone", "Time zone:")), 2);

        String[] zoneIds = new String[0];
        try {
          zoneIds = TimeZone.getAvailableIDs();
        } catch (Exception e) {
          e.printStackTrace();
        }
        Arrays.sort(zoneIds);
        mTimezone = new JComboBox(zoneIds);

        String zone = mConfig.getTimeZoneAsString();
        for (int i = 0; i < zoneIds.length; i++) {
          if (zoneIds[i].equals(zone)) {
            mTimezone.setSelectedIndex(i);
            break;
          }
        }

        extendedPanel.add(mTimezone, 4, 3);

        extendedPanel.addParagraph(LOCALIZER.msg("security", "Security"));

        extendedPanel.addRow("default");
        extendedPanel.add(new JLabel(LOCALIZER.msg("userName", "User name :")), 2);
        mUserName = new JTextField(mConfig.getUserName());
        extendedPanel.add(mUserName, 4, 3);

        extendedPanel.addRow("default");
        extendedPanel.add(new JLabel(LOCALIZER.msg("password", "Password :")), 2);
        mPasswordField = new JPasswordField(mConfig.getPassword());
        extendedPanel.add(mPasswordField, 4, 3);

        extendedPanel.addParagraph(LOCALIZER.msg("streaming", "Streaming"));

        extendedPanel.addRow("default");
        extendedPanel.add(new JLabel(LOCALIZER.msg("mediaplayer", "Mediaplayer :")), 2);
        mMediaplayer = new JTextField(mConfig.getMediaplayer());
        extendedPanel.add(mMediaplayer, 4);

        JButton select = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
        select.addActionListener(new ActionListener() {
            JPanel extendedJPanel = null;
            
            private ActionListener init(JPanel extendedJPanel) {
                this.extendedJPanel = extendedJPanel;
                return this;
            }
            
            public void actionPerformed(ActionEvent e) {
                JFileChooser mediaplayerChooser = new JFileChooser();
                int returnVal = mediaplayerChooser.showOpenDialog(extendedJPanel);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    mMediaplayer.setText(mediaplayerChooser.getSelectedFile().getAbsolutePath());
               }
            }
        }.init(extendedPanelView));
        extendedPanel.add(select, 6);

        mProgramReceiveTargetSelection = new ProgramReceiveTargetSelectionPanel(UiUtilities.getLastModalChildOf(CapturePlugin.getInstance().getSuperFrame()),
            mConfig.getProgramReceiveTargets(),null,CapturePlugin.getInstance(),true,LOCALIZER.msg("sendToTitle","Send scheduled programs to:"));

        extendedPanel.addRow("default");
        extendedPanel.addRow("default");
        extendedPanel.add(mProgramReceiveTargetSelection, 1, 7);

        mDefaultLocation = new JComboBox();
        mDefaultAfterEvent = new JComboBox();

        for(int i = 0; i < 4; i++) {
          AfterEventEntry entry = new AfterEventEntry(i);
          mDefaultAfterEvent.addItem(entry);
          
          if(entry.matchesEvent(mConfig.getAfterEvent())) {
            mDefaultAfterEvent.setSelectedItem(entry);
          }
        }
        
        extendedPanel.addParagraph(LOCALIZER.msg("recording", "Recording"));
        extendedPanel.addRow("default");
        extendedPanel.add(new JLabel(LOCALIZER.msg("defaultlocation", "DefaultLocation :")), 2);
        extendedPanel.add(mDefaultLocation, 4);
        extendedPanel.addRow("default");
        extendedPanel.add(new JLabel(LOCALIZER.msg("afterEvent", "Action after recording:")), 2);
        extendedPanel.add(mDefaultAfterEvent, 4);
        
        JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	
                okPressed();
            }
        });

        JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        basicPanelView.setOpaque(false);

        getRootPane().setDefaultButton(ok);
        
        final JScrollPane scroll = new JScrollPane(extendedPanelView);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        
        final JTabbedPane tabs = new JTabbedPane();
        tabs.add(LOCALIZER.msg("basicTitle", "Basic settings"), basicPanelView);
        tabs.add(LOCALIZER.msg("extendedTitle", "Extended settings"), scroll);
        tabs.addAncestorListener(new AncestorListener() {
          
          @Override
          public void ancestorRemoved(AncestorEvent arg0) {
            // TODO Auto-generated method stub
            if(mTimeOutTimer.isRunning()) {
              mTimeOutTimer.stop();
            }
          }
          
          @Override
          public void ancestorMoved(AncestorEvent arg0) {
            // TODO Auto-generated method stub
            
          }
          
          @Override
          public void ancestorAdded(AncestorEvent arg0) {
            // TODO Auto-generated method stub
            
          }
        });
        
        tabs.addChangeListener(new ChangeListener() {
            String currentDefaultLocation = mConfig.getDefaultLocation();
           
            public void stateChanged(ChangeEvent e) {
                if (tabs.getSelectedIndex() == 0) {
                    currentDefaultLocation = (String) mDefaultLocation.getSelectedItem();
                } else if (tabs.getSelectedIndex() == 1) {
                    mConfig.setDreamboxAddress(mDreamboxAddress.getText());
                    mDefaultLocation.removeAllItems();
                    List<String> locations = E2LocationHelper.getInstance(mConnector, null).getLocations(mDreamboxAddress.getText());
                    
                    if(mConnector.isTimedOut() && !mTimeOutTimer.isRunning()) {
                      mTimeOutTimer.start();
                    }
                    
                    Iterator<String> it = locations.iterator();
                    while(it.hasNext()) {
                        mDefaultLocation.addItem(it.next());
                    }
                    if (!currentDefaultLocation.equals("") && locations.contains(currentDefaultLocation)) {
                        int defaultIndex = locations.indexOf(currentDefaultLocation);
                        mDefaultLocation.setSelectedIndex(defaultIndex);
                    }
                }
            }
        });

        JPanel content = (JPanel) getContentPane();
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.setLayout(new BorderLayout(0, 3));
        content.add(tabs, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        CapturePlugin.getInstance().layoutWindow("captureDreamboxSettingsDialog", this);
    }

    /**
     * Try to attach internal channels with dreambox channels
     */
    private void attachChannels() {
        Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();
        ExternalChannelIf[] dchannels = mConfig.getExternalChannels();

        for (Channel channel:channels) {
            if (mConfig.getExternalChannel(channel) == null) {

                String name = normalizeName(channel.getName());

                for (ExternalChannelIf dch:dchannels) {
                    if (normalizeName(dch.getName()).equals(name)) {
                        mConfig.setExternalChannel(channel,  dch);
                    }
                }

            }

        }

        mTable.repaint();
    }

    /**
     * Normalizes a channel name. Lower case and no spaces.
     * @param name channel name
     * @return normalized channel name
     */
    private String normalizeName(String name) {
        return name.toLowerCase().replaceAll("\\W", "");
    }

    /**
     * Refresh all Channels and update the table
     */
    private void refreshChannelList() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Thread(new Runnable() {
                    public void run() {
                        mConfig.setDreamboxAddress(mDreamboxAddress.getText());

                        try {
                          if (mConnector.testDreamboxVersion()) {
                            Collection<DreamboxChannel> channels = null;

                            try {
                                channels = mConnector.getChannels();
                                
                                if(mConnector.isTimedOut() && !mTimeOutTimer.isRunning()) {
                                  mTimeOutTimer.start();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (channels == null) {
                                JOptionPane.showMessageDialog(DreamboxConfigDialog.this, LOCALIZER.msg("errorText", "Sorry, could not load channel list from Dreambox."),
                                        LOCALIZER.msg("errorTitle", "Error"), JOptionPane.ERROR_MESSAGE);
                            } else {
                                mConfig.setDreamboxChannels(channels.toArray(new DreamboxChannel[channels.size()]));
                                JOptionPane.showMessageDialog(DreamboxConfigDialog.this, LOCALIZER.msg("okText", "Channel list updated."),
                                        LOCALIZER.msg("okTitle", "Updated"), JOptionPane.INFORMATION_MESSAGE);
                            }
                            mTable.repaint();
                          } else {
                            JOptionPane.showMessageDialog(DreamboxConfigDialog.this, LOCALIZER.msg("wrongVersion", "Wrong Version of Dreambox-WebInterface. Please update!"),
                                        LOCALIZER.msg("errorTitle", "Error"), JOptionPane.INFORMATION_MESSAGE);
                          }
                        } catch (IOException e) {
                          JOptionPane.showMessageDialog(DreamboxConfigDialog.this, LOCALIZER.msg("errorText", "Sorry, could not load channel list from Dreambox."),
                                  LOCALIZER.msg("errorTitle", "Error"), JOptionPane.ERROR_MESSAGE);
                          e.printStackTrace();
                        }

                    }
                }).start();

            }
        });
    }

    /**
     * OK was pressed, config gets saved
     */
    private void okPressed() {
        mOkPressed = true;

        if (mTable.isEditing()) {
            TableCellEditor editor = mTable.getCellEditor();
            if (editor != null) {
              editor.stopCellEditing();
            }
        }

        mConfig.setAfterTime(mAfterModel.getNumber().intValue());
        mConfig.setBeforeTime(mBeforeModel.getNumber().intValue());
        mConfig.setTimeout(mTimeoutModel.getNumber().intValue());

        mConfig.setDreamboxAddress(mDreamboxAddress.getText());
        
        mConfig.setTimeZone(((String) mTimezone.getSelectedItem()));

        mConfig.setUserName(mUserName.getText());
        mConfig.setPassword(mPasswordField.getPassword());

        mConfig.setMediaplayer(mMediaplayer.getText());
        mConfig.setProgramReceiveTargets(mProgramReceiveTargetSelection.getCurrentSelection());
        
        String defaultLocation = (String) mDefaultLocation.getSelectedItem();
        mConfig.setDefaultLocation(defaultLocation != null ? defaultLocation : "");
        mConfig.setAfterEvent(((AfterEventEntry)mDefaultAfterEvent.getSelectedItem()).mCode);
        
        setVisible(false);
    }

    /**
     * @return true, if ok was pressed
     */
    public boolean wasOkPressed() {
        return mOkPressed;
    }

    /**
     * @return current configuration
     */
    public DreamboxConfig getConfig() {
        return mConfig;
    }

    /**
     * Close the Dialog
     */
    public void close() {
      setVisible(false);
    }

    /**
     * @return Name of the Device
     */
    public String getDeviceName() {
        return mDeviceName.getText();
    }
    
    private static final class AfterEventEntry {
      private int mCode = 0;
      
      private AfterEventEntry(int code) {
        mCode = code;
      }
      
      @Override
      public String toString() {
        return LOCALIZER.msg("afterEvent"+mCode,String.valueOf(mCode));
      }
      
      private boolean matchesEvent(int code) {
        return mCode == code;
      }
    }
}
