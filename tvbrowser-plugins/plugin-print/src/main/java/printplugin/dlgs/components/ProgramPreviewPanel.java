/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 */

package printplugin.dlgs.components;

import com.jgoodies.forms.builder.ButtonStackBuilder;

import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFieldType;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import printplugin.printer.ProgramIcon;
import printplugin.settings.MutableProgramIconSettings;
import printplugin.settings.ProgramIconSettings;
import printplugin.util.BaseAction;

import util.ui.Localizer;
import util.ui.TimeFormatter;
import util.ui.UiUtilities;

/**
 * {@link JPanel} that displays a (dummy) program entry to show
 * the selected fonts.
 *
 * Lets the user choose {@link ProgramFieldType} entries and fonts.
 *
 * @author troggan
 * @see FontsDialog
 * @see ProgramItemFieldsConfigDlg
 * @since 2006-03-06 17:29:38 +0100
 */
@SuppressWarnings("nls")
public class ProgramPreviewPanel extends JPanel implements ActionListener {

  private static final long serialVersionUID = 8953071252035184547L;

  /** The localizer for this class. */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramPreviewPanel.class);

  private final Frame mParent;
  private final JLabel mProgramIconLabel;
  private final JLabel mDateLabel;
  private final JPanel mIconPanel;

  private Font mDateFont;
  private MutableProgramIconSettings mProgramIconSettings;

  /**
   * Creates a new instance for the given parent frame.
   * Uses default values.
   *
   * @param dlgParent
   *                    the parent frame of the dialog
   */
  public ProgramPreviewPanel(final Frame dlgParent) {
    this(dlgParent, null, null);
  }

  /**
   * Creates a new instance with the given parameters.
   *
   * @param dlgParent
   *                              the parent frame of the dialog
   * @param programIconSettings
   *                              settings containing the selected program field
   *                              type entries and fonts (can be null)
   * @param dateFont
   *                              an optional font that is used to display dates
   *                              (can be null)
   */
  public ProgramPreviewPanel(final Frame dlgParent, final ProgramIconSettings programIconSettings,
      final Font dateFont) {

    mDateFont = dateFont;
    mParent = dlgParent;

    setLayout(new BorderLayout(3, 3));

    if (programIconSettings != null) {
      setProgramIconSettings(programIconSettings);
    }

    mDateLabel = new JLabel(new Date().getLongDateString());
    mDateLabel.setForeground(Color.BLACK);
    mProgramIconLabel = new JLabel(createDemoProgramPanel(mProgramIconSettings));

    mIconPanel = new JPanel(new BorderLayout()) {

      private static final long serialVersionUID = -2983369556171749885L;

      @Override
      protected void paintChildren(final Graphics g) {
        final Graphics2D g2d = (Graphics2D) g;
        final Color c = g2d.getColor();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(1, 1, getWidth() - 1, getHeight() - 1);
        g2d.setColor(c);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintChildren(g);
      }
    };

    if (mDateFont != null) {
      mIconPanel.add(mDateLabel, BorderLayout.PAGE_START);
    }
    mIconPanel.add(mProgramIconLabel, BorderLayout.CENTER);

    final JButton fontsButton = new JButton(
        BaseAction.builder("fonts", this).text(mLocalizer.msg("fonts", "Fonts\u2026")).build());
    final JButton fieldsButton = new JButton(
        BaseAction.builder("fields", this).text(mLocalizer.msg("fields", "Fields\u2026")).build());

    final JScrollPane scrollPane = new JScrollPane(mIconPanel);
    scrollPane.setPreferredSize(new Dimension(0, 90));
    add(scrollPane, BorderLayout.CENTER);
    add(new ButtonStackBuilder().addButton(fontsButton, fieldsButton).build(), BorderLayout.LINE_END);

    updatePreviewPanel();
  }

  /**
   * Updates the panel after changes where made by the user.
   */
  public void updatePreviewPanel() {
    mProgramIconLabel.setIcon(createDemoProgramPanel(mProgramIconSettings));
    mDateLabel.setFont(mDateFont);
    if (mDateFont != null) {
      mIconPanel.add(mDateLabel, BorderLayout.PAGE_START);
    }
  }

  public void setProgramIconSettings(final ProgramIconSettings settings) {
    mProgramIconSettings = new MutableProgramIconSettings(settings);
    updatePreviewPanel();
  }

  public void setDateFont(final Font f) {
    mDateFont = f;
    updatePreviewPanel();
  }

  public void setShowPluginMarking(final boolean show) {
    mProgramIconSettings.setPaintPluginMarks(show);
  }

  public boolean getShowPluginMarking() {
    return mProgramIconSettings.getPaintPluginMarks();
  }

  public ProgramIconSettings getProgramIconSettings() {
    return mProgramIconSettings;
  }

  public Font getDateFont() {
    return mDateFont;
  }

  private static Icon createDemoProgramPanel(final MutableProgramIconSettings programIconSettings) {
    final Program prog = Plugin.getPluginManager().getExampleProgram();
    if (programIconSettings != null) {
      programIconSettings.setTimeFieldWidth(
          UiUtilities.getStringWidth(programIconSettings.getTimeFont(), new TimeFormatter().formatTime(23, 59)) + 4);
    }
    final ProgramIcon ico = new ProgramIcon(prog, programIconSettings, 200, false);
    ico.setMaximumHeight(Integer.MAX_VALUE);
    return ico;
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void actionPerformed(final ActionEvent e) {
    switch (e.getActionCommand()) {
      case "fields":
        if (mProgramIconSettings == null) {
          return;
        }
        final ProgramItemFieldsConfigDlg programItemFieldsConfigDialog = new ProgramItemFieldsConfigDlg(mParent,
            mProgramIconSettings.getProgramInfoFields());
        UiUtilities.centerAndShow(programItemFieldsConfigDialog);
        if (programItemFieldsConfigDialog.getResult() == ProgramItemFieldsConfigDlg.OK) {
          mProgramIconSettings.setProgramInfoFields(programItemFieldsConfigDialog.getProgramItemFieldTypes());
          updatePreviewPanel();
        }
        break;
      case "fonts":
        if (mProgramIconSettings == null) {
          return;
        }
        final FontsDialog fontsDialog = new FontsDialog(mParent, mProgramIconSettings.getTitleFont(),
            mProgramIconSettings.getTextFont(), mDateFont);
        UiUtilities.centerAndShow(fontsDialog);
        if (fontsDialog.getResult() == FontsDialog.OK) {
          final Font titleFont = fontsDialog.getTitleFont();
          final Font descFont = fontsDialog.getDescriptionFont();
          mDateFont = fontsDialog.getDateFont();
          mProgramIconSettings.setTextFont(descFont);
          mProgramIconSettings.setTimeFont(titleFont);
          mProgramIconSettings.setTitleFont(titleFont);
          updatePreviewPanel();
        }
        break;
    }
  }
}