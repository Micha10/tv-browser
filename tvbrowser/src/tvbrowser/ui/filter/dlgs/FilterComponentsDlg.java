/*
 * TV-Browser
 * Copyright (C) 2022 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.filter.dlgs;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.Settings;
import util.i18n.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * A dialog to edit the filter components.
 * 
 * @author René Mach
 */
public class FilterComponentsDlg extends JDialog implements WindowClosingIf {
  public static final util.i18n.Localizer LOCALIZER = Localizer.getLocalizerFor(FilterComponentsDlg.class);
  private FilterComponentPanel mPanel;
  private JButton mClose;
  
  public FilterComponentsDlg(Window parent) {
    super(parent, ModalityType.DOCUMENT_MODAL);
    setTitle(LOCALIZER.msg("title", "Edit filter components"));
    
    mPanel = new FilterComponentPanel(this);
    mClose = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    mClose.addActionListener(e -> {
      close();
    });
    
    JPanel content = new JPanel(new FormLayout("default:grow,default","fill:100dlu:grow,5dlu,default,5dlu,default"));
    content.setBorder(Borders.DIALOG);
    
    content.add(mPanel, CC.xyw(1, 1, 2));
    content.add(new JSeparator(JSeparator.HORIZONTAL), CC.xyw(1, 3, 2));
    content.add(mClose, CC.xy(2, 5));
    
    setContentPane(content);
    getRootPane().setDefaultButton(mClose);
    
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
    UiUtilities.registerForClosing(this);
    Settings.layoutWindow(FilterComponentsDlg.class.getCanonicalName(), this, new Dimension(500, 500), parent);
    setVisible(true);
  }

  @Override
  public void close() {
    dispose();
  }
}
