/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * CVS information:
 * $RCSfile$
 * $Source$
 * $Date: 2009-04-25 09:58:28 +0200 (Sa, 25 Apr 2009) $
 * $Author: Bananeweizen $
 * $Revision: 5670 $
 */

package printplugin.dlgs;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import printplugin.PrintPlugin;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

@SuppressWarnings("nls")
public final class MainPrintDialog extends JDialog implements ActionListener, FocusListener, WindowClosingIf {

  private static final long serialVersionUID = -7546626861023253017L;

  /** The localizer for this class. */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(MainPrintDialog.class);

  protected final JButton mCloseButton;
  protected final JPanel mContent;

  protected int mResult = PRINT_CLOSE;

  public static final int PRINT_CLOSE = 0;
  public static final int PRINT_DAYPROGRAMS = 1;
  public static final int PRINT_QUEUE = 2;
  public static final int PRINT_SETTINGS = 3;

  public MainPrintDialog(final Frame parent) {
    super(parent, mLocalizer.msg("title", "Print"), true);

    mContent = (JPanel) getContentPane();

    mCloseButton = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCloseButton.addActionListener(this);
    mCloseButton.setActionCommand("close");
    mCloseButton.setDefaultCapable(true);

    UiUtilities.registerForClosing(this);

    Font font = UIManager.getDefaults().getFont("Button.font");
    font = font.deriveFont(font.getSize2D() + 2f);

    final JLabel label = new JLabel(mLocalizer.msg("whatDoYouWantToPrint", "What do you want to print?"));

    final JButton printDayProgramsButton = new JButton(mLocalizer.msg("fullDayPrograms", "Full TV listings"),
        PrintPlugin.getInstance().createImageIcon("devices", "printer", 22));
    printDayProgramsButton.addActionListener(this);
    printDayProgramsButton.setActionCommand("dayPrograms");
    printDayProgramsButton.setBackground(printDayProgramsButton.getBackground().brighter());
    printDayProgramsButton.setFont(font);
    printDayProgramsButton.setHorizontalAlignment(SwingConstants.LEADING);
    printDayProgramsButton.setIconTextGap(10);

    final JButton printQueueButton = new JButton(mLocalizer.msg("printFromQueue", "Print from printer queue"),
        PrintPlugin.getInstance().createImageIcon("devices", "printer", 22));
    printQueueButton.addActionListener(this);
    printQueueButton.setActionCommand("queue");
    printQueueButton.setBackground(printQueueButton.getBackground().brighter());
    printQueueButton.setEnabled(PrintPlugin.getInstance().canPrintQueue());
    printQueueButton.setFont(font);
    printQueueButton.setHorizontalAlignment(SwingConstants.LEADING);
    printQueueButton.setIconTextGap(10);

    final JButton settingsButton = new JButton(
        PrintPlugin.getInstance().createImageIcon("categories", "preferences-system", 16));
    settingsButton.addActionListener(this);
    settingsButton.addFocusListener(this);
    settingsButton.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseEntered(final MouseEvent e) {
        if (!settingsButton.isBorderPainted()) {
          settingsButton.setBorderPainted(true);
        }
      }

      @Override
      public void mouseExited(final MouseEvent e) {
        if (!settingsButton.hasFocus()) {
          settingsButton.setBorderPainted(false);
        }
      }
    });
    settingsButton.setActionCommand("settings");
    settingsButton.setBorderPainted(false);
    settingsButton.setIconTextGap(0);
    settingsButton.setRolloverEnabled(true);
    settingsButton.setToolTipText(Localizer.getLocalization(Localizer.I18N_SETTINGS));

    mCloseButton.setText(Localizer.getLocalization(Localizer.I18N_CLOSE));

    final JPanel bottomPanel = new ButtonBarBuilder()
        .addFixed(settingsButton)
        .addGlue()
        .addButton(mCloseButton)
        .background(getBackground())
        .border(BorderFactory.createEmptyBorder(10, 10, 10, 10))
        .build();

    final GridBagConstraints gbc = new GridBagConstraints();
    final Insets oldInsets = gbc.insets;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.insets = new Insets(10, 10, 5, 10);

    mContent.setBackground(mContent.getBackground().brighter());
    mContent.setLayout(new GridBagLayout());
    mContent.add(label, gbc);
    mContent.add(printDayProgramsButton, gbc);
    mContent.add(printQueueButton, gbc);

    oldInsets.top = 10;
    gbc.anchor = GridBagConstraints.PAGE_END;
    gbc.insets = oldInsets;
    gbc.weighty = 1.0;
    mContent.add(bottomPanel, gbc);

    pack();
    getRootPane().setDefaultButton(mCloseButton);
    mCloseButton.requestFocus();

    setResizable(false);
    setLocationRelativeTo(parent);
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void actionPerformed(final ActionEvent e) {
    switch (e.getActionCommand()) {
      case "dayPrograms":
        mResult = PRINT_DAYPROGRAMS;
        break;
      case "queue":
        mResult = PRINT_QUEUE;
        break;
      case "settings":
        mResult = PRINT_SETTINGS;
        break;
      case "close":
        mResult = PRINT_CLOSE;
        break;
    }
    close();
  }

  @Override
  public void focusGained(final FocusEvent e) {
    if (!e.isTemporary()) {
      ((JButton) e.getSource()).setBorderPainted(true);
    }
  }

  @Override
  public void focusLost(final FocusEvent e) {
    if (!e.isTemporary()) {
      ((JButton) e.getSource()).setBorderPainted(false);
    }
  }

  @Override
  public void close() {
    setVisible(false);
  }

  public int getResult() {
    return mResult;
  }
}