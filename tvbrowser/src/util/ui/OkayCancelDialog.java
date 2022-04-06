/*
 * TV-Browser
 * Copyright (C) 2003-2021 TV-Browser-Team (dev@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
 *     $Id$
 *   $Author$
 * $Revision$
 */
package util.ui;

import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JSeparator;

import com.jgoodies.forms.factories.Borders;

import util.i18n.Localizer;

/**
 * A dialog that provides buttons and function for OK and Cancel action to
 * which a JComponent can be added.
 * 
 * @author René Mach
 * @since 4.2.5
 */
public class OkayCancelDialog extends JDialog implements WindowClosingIf {
  private JButton mOk;
  private boolean mOkWasPressed;
  
  public OkayCancelDialog(final Window parent, final String title, final JComponent message, final String help, final boolean okayEnabled) {
    super(parent, ModalityType.DOCUMENT_MODAL);
    setTitle(title);
    
    mOkWasPressed = false;
    
    mOk = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    mOk.setEnabled(okayEnabled);
    mOk.addActionListener(e -> {
      mOkWasPressed = true;
      close();
    });
    
    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(e -> {
      close();
    });
    
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder("100dlu:grow,default,5dlu,default");
    pb.border(Borders.DIALOG);
    
    if(help != null && !help.isBlank()) {
      pb.addLabelRowFull(false,"<html>"+help+"</html>");
    }
    
    pb.addRowFull("fill:10dlu:grow", message);
    pb.addRowFull(new JSeparator(JSeparator.HORIZONTAL));
    
    pb.addRow(cancel, 2);
    pb.add(mOk, 4);
    
    setContentPane(pb.getPanel());
    getRootPane().setDefaultButton(mOk);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
  }
  
  public void setOkayEnabled(final boolean enabled) {
    mOk.setEnabled(enabled);
  }
  
  public boolean getOkWasPressed() {
    return mOkWasPressed;
  }

  @Override
  public void close() {
    dispose();
  }
}
