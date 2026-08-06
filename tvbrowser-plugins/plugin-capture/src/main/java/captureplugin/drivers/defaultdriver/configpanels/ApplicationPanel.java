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
 *     $Date: 2009-09-04 11:15:55 +0200 (Fr, 04 Sep 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5953 $
 */
package captureplugin.drivers.defaultdriver.configpanels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import util.ui.Localizer;
import captureplugin.drivers.defaultdriver.DeviceConfig;


/**
 * This Panel lets the User choose the Application / URL
 */
public class ApplicationPanel extends JPanel {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ApplicationPanel.class);

    /** Data for Panel */
    private DeviceConfig mData;
    
    /** Path */
    private JTextField mPathTextField = new JTextField();

    private JTextField mUrl = new JTextField();
    
    private JButton mFileButton = new JButton(Localizer.getLocalization(Localizer.I18N_FILE));
    
    /**
     * Creates the Panel
     * @param data Configuration
     */
    public ApplicationPanel(DeviceConfig data) {
        mData = data;
        createPanel();
    }
    
    /**
     * creates a JPanel for getting the programpath
     */
    private void createPanel() {
      setLayout(new GridBagLayout());
      setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

      GridBagConstraints gc = new GridBagConstraints();
      gc.insets = new Insets(4, 4, 4, 4);
      gc.anchor = GridBagConstraints.WEST;
      gc.fill = GridBagConstraints.HORIZONTAL;

      gc.gridx = 0;
      gc.gridy = 0;
      gc.gridwidth = 3;
      gc.weightx = 1.0;
      add(new JLabel(mLocalizer.msg("What", "What to start")), gc);
      
      JRadioButton application = new JRadioButton(mLocalizer.msg("Application", "Application"));

      gc.gridy = 1;
      gc.gridx = 0;
      gc.gridwidth = 1;
      gc.weightx = 0;
      add(application, gc);
        
      mPathTextField.setText(mData.getProgramPath());
      mPathTextField.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          mData.setProgramPath(mPathTextField.getText());
        }
      });

      gc.gridx = 1;
      gc.weightx = 1.0;
      add(mPathTextField, gc);

      mFileButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          pathButtonPressed(e);
        }
      });

      gc.gridx = 2;
      gc.weightx = 0;
      add(mFileButton, gc);
        
      JRadioButton url = new JRadioButton(mLocalizer.msg("URL", "URL"));

      gc.gridy = 2;
      gc.gridx = 0;
      add(url, gc);
        
      mUrl.setText(mData.getWebUrl());
        
      mUrl.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          mData.setWebUrl(mUrl.getText());
        }
      });

      gc.gridx = 1;
      gc.gridwidth = 2;
      gc.weightx = 1.0;
      add(mUrl, gc);
        
      ButtonGroup group = new ButtonGroup();
        
      group.add(application);
      group.add(url);
        
      url.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setUrlMode(true);
        }
      });
        
      application.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setUrlMode(false);
        }
      });
        
      if (mData.getUseWebUrl()) {
        url.setSelected(true);
      } else {
        application.setSelected(true);
      }
      
      setUrlMode(mData.getUseWebUrl());
    }

    /**
     * Sets the Mode of the Application
     * @param urlmode
     */
    private void setUrlMode(boolean urlmode) {
        mData.setUseWebUrl(urlmode);
        mUrl.setEnabled(urlmode);
        mPathTextField.setEnabled(!urlmode);
        mFileButton.setEnabled(!urlmode);
    }
    
    /**
     * invoked when the user clicks the Button to open an FileChooser - Dialog
     */
    private void pathButtonPressed(ActionEvent e) {
        JFileChooser f = new JFileChooser();
        if (f.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            mData.setProgramPath(f.getSelectedFile().toString());
            mPathTextField.setText(mData.getProgramPath());
        }
    }


}