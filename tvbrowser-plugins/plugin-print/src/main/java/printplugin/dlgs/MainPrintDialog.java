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
import printplugin.util.BaseAction;
import printplugin.util.Utils;

import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

@SuppressWarnings("nls")
public final class MainPrintDialog extends JDialog implements ActionListener, FocusListener, WindowClosingIf {

  private static final long serialVersionUID = -7546626861023253017L;

  private static final String FULL_DAY_PROGRAMS = "fullDayPrograms";
  private static final String PRINT_FROM_QUEUE = "printFromQueue";

  public static final int PRINT_CLOSE = 0;
  public static final int PRINT_DAYPROGRAMS = 1;
  public static final int PRINT_QUEUE = 2;
  public static final int PRINT_SETTINGS = 3;

  /** The localizer for this class. */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(MainPrintDialog.class);

  protected final JButton mCloseButton;
  protected final JPanel mContent;

  protected int mResult = PRINT_CLOSE;

  public MainPrintDialog(final Frame parent) {
    super(parent, mLocalizer.msg("title", "Print"), true);

    mContent = (JPanel) getContentPane();

    UiUtilities.registerForClosing(this);

    Font font = UIManager.getDefaults().getFont("Button.font");
    font = font.deriveFont(font.getSize2D() + 2f);

    final JLabel label = new JLabel(mLocalizer.msg("whatDoYouWantToPrint", "What do you want to print?"));

    final JButton printDayProgramsButton = new JButton(BaseAction
        .builder(FULL_DAY_PROGRAMS, this)
        .icon(PrintPlugin.getInstance().createImageIcon("devices", "printer", TVBrowserIcons.SIZE_SMALL))
        .largeIcon(PrintPlugin.getInstance().createImageIcon("devices", "printer", TVBrowserIcons.SIZE_LARGE))
        .text(mLocalizer.msg(FULL_DAY_PROGRAMS, "Full TV listings"))
        .build());
    printDayProgramsButton.setBackground(printDayProgramsButton.getBackground().brighter());
    printDayProgramsButton.setFont(font);
    printDayProgramsButton.setHorizontalAlignment(SwingConstants.LEADING);
    printDayProgramsButton.setIconTextGap(10);
    Utils.adjustButtonMargin(printDayProgramsButton, 10);

    final JButton printQueueButton = new JButton(BaseAction
        .builder(PRINT_FROM_QUEUE, this)
        .icon(PrintPlugin.getInstance().createImageIcon("devices", "printer", TVBrowserIcons.SIZE_SMALL))
        .largeIcon(PrintPlugin.getInstance().createImageIcon("devices", "printer", TVBrowserIcons.SIZE_LARGE))
        .text(mLocalizer.msg(PRINT_FROM_QUEUE, "Print from printer queue"))
        .build());
    printQueueButton.setBackground(printQueueButton.getBackground().brighter());
    printQueueButton.setFont(font);
    printQueueButton.setHorizontalAlignment(SwingConstants.LEADING);
    printQueueButton.setIconTextGap(10);
    Utils.adjustButtonMargin(printQueueButton, 10);

    final JButton settingsButton = new JButton(BaseAction.settings(this).largeIcon(null).text(null).build());
    settingsButton.setBorderPainted(false);
    settingsButton.setIconTextGap(0);
    settingsButton.setMargin(UiUtilities.ZERO_INSETS);
    settingsButton.setRolloverEnabled(true);
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
    Utils.adjustButtonMargin(settingsButton, 1);

    mCloseButton = new JButton(BaseAction.close(this).build());

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
    gbc.insets = new Insets(10, 10, 0, 10);

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
    if (PrintPlugin.getInstance().canPrintQueue()) {
      getRootPane().setDefaultButton(mCloseButton);
      mCloseButton.requestFocus();
    } else {
      printQueueButton.setEnabled(false);
      getRootPane().setDefaultButton(printDayProgramsButton);
      printDayProgramsButton.requestFocus();
    }

    setResizable(false);
    setLocationRelativeTo(parent);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    switch (e.getActionCommand()) {
      case FULL_DAY_PROGRAMS:
        mResult = PRINT_DAYPROGRAMS;
        break;
      case PRINT_FROM_QUEUE:
        mResult = PRINT_QUEUE;
        break;
      case BaseAction.SETTINGS:
        mResult = PRINT_SETTINGS;
        break;
      case BaseAction.CLOSE:
        mResult = PRINT_CLOSE;
        break;
      default:
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