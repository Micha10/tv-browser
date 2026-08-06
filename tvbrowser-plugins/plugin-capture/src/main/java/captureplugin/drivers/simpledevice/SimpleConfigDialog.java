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
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mär 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */
package captureplugin.drivers.simpledevice;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import util.ui.ChannelTableCellRenderer;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import captureplugin.utils.ConfigTableModel;
import captureplugin.utils.ExternalChannelTableCellEditor;
import captureplugin.utils.ExternalChannelTableCellRenderer;

/**
 * Config Dialog
 * 
 * @author bodum
 */
public class SimpleConfigDialog extends JDialog implements WindowClosingIf {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(SimpleConfigDialog.class);

  /** Device */
  private SimpleDevice mDevice;
  /** Connection */
  private SimpleConnectionIf mConnection;
  /** Configuration */
  private SimpleConfig mConfig;
  /** Which Button was pressed */
  private int mReturn = JOptionPane.CANCEL_OPTION;
  /** Table with mapping */
  private JTable mTable;

  private JTextField mName;

  private void initialize(SimpleDevice dev, SimpleConnectionIf connection,
      SimpleConfig config) {
    mConnection = connection;
    mConfig = (SimpleConfig) config.clone();
    mDevice = dev;
    createGui();
  }

  /**
   * Create Dialog
   * 
   * @param parent
   *          Parent
   * @param dev
   *          Device
   * @param connection
   *          Connection
   * @param config
   *          Configuration
   */
  public SimpleConfigDialog(Window parent, SimpleDevice dev,
      SimpleConnectionIf connection, SimpleConfig config) {
    super(parent);
    setModal(true);
    initialize(dev, connection, config);
  }

  /**
   * Create the Gui
   */
  private void createGui() {
    JPanel panel = (JPanel) getContentPane();
    
    setTitle(mLocalizer.msg("title","Device Settings"));

    panel.setLayout(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(4, 4, 4, 4);
    gc.anchor = GridBagConstraints.WEST;
    gc.fill = GridBagConstraints.HORIZONTAL;

    gc.gridx = 0;
    gc.gridy = 0;
    gc.gridwidth = 3;
    gc.weightx = 1;
    JLabel nameSection = new JLabel(mLocalizer.msg("deviceName","Device name"));
    nameSection.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), ""));
    panel.add(nameSection, gc);

    gc.gridy = 1;
    gc.gridx = 0;
    gc.gridwidth = 1;
    gc.weightx = 0;
    panel.add(new JLabel(mLocalizer.msg("deviceNameInput", "Name")+ ":"), gc);
    mName = new JTextField(mDevice.getName());
    gc.gridx = 1;
    gc.gridwidth = 2;
    gc.weightx = 1;
    panel.add(mName, gc);

    gc.gridy = 2;
    gc.gridx = 0;
    gc.gridwidth = 3;
    gc.weightx = 1;
    JLabel channelSection = new JLabel(mLocalizer.msg("channelAssignment","Channel assignment"));
    channelSection.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), ""));
    panel.add(channelSection, gc);

    mTable = new JTable(new ConfigTableModel(mConfig, mLocalizer.msg("external", "external")));
    mTable.getTableHeader().setReorderingAllowed(false);
    mTable.getColumnModel().getColumn(0).setCellRenderer(new ChannelTableCellRenderer());
    mTable.getColumnModel().getColumn(1).setCellRenderer(new ExternalChannelTableCellRenderer());
    mTable.getColumnModel().getColumn(1).setCellEditor(new ExternalChannelTableCellEditor(mConfig));
    gc.gridy = 3;
    gc.gridx = 0;
    gc.gridwidth = 3;
    gc.weightx = 1;
    gc.weighty = 1;
    gc.fill = GridBagConstraints.BOTH;
    panel.add(new JScrollPane(mTable), gc);
    
    JButton fetch = new JButton(mLocalizer.msg("fetchChannels","Fetch Channellist"));
    
    fetch.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            SimpleChannel[] lists = mConnection.getAvailableChannels();

            if (lists == null) {
                JOptionPane.showMessageDialog(SimpleConfigDialog.this,
                        mLocalizer.msg("errorChannels","Could not load external channels"),
                        mLocalizer.msg("errorTitle","Error"), JOptionPane.ERROR_MESSAGE);
            } else {
                mConfig.setExternalChannels(lists);
            }

            mTable.repaint();
          }
        });
      }
    });

    gc.gridy = 4;
    gc.gridx = 2;
    gc.gridwidth = 1;
    gc.weightx = 0;
    gc.weighty = 0;
    gc.fill = GridBagConstraints.NONE;
    gc.anchor = GridBagConstraints.EAST;
    panel.add(fetch, gc);
    
    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        if (mTable.isEditing()) {
            TableCellEditor editor = mTable.getCellEditor();
            if (editor != null) {
              editor.stopCellEditing();
            }
        }
        mReturn = JOptionPane.OK_OPTION;
        setVisible(false);
      }
    });

    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        setVisible(false);
      }
    });
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(ok);
    buttonPanel.add(cancel);

    gc.gridy = 5;
    gc.gridx = 0;
    gc.gridwidth = 3;
    gc.anchor = GridBagConstraints.EAST;
    panel.add(buttonPanel, gc);
    
    getRootPane().setDefaultButton(ok);
    UiUtilities.registerForClosing(this);
    
    setSize(500, 400);
  }

  /**
   * Was the OK-Button pressed ?
   * @return true if OK was pressed
   */
  public boolean wasOkPressed() {
    return mReturn == JOptionPane.OK_OPTION;
  }

  /**
   * @return Modified configuration
   */
  public SimpleConfig getConfig() {
    return mConfig;
  }

  /**
   * @return Modified Name
   */
  public String getName() {
    return mName.getText();
  }
  
  public void close() {
    setVisible(false);
  }
  
}