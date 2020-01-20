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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mrz 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
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

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import printplugin.printer.ProgramIcon;
import printplugin.settings.MutableProgramIconSettings;
import printplugin.settings.ProgramIconSettings;

import util.ui.Localizer;
import util.ui.TimeFormatter;
import util.ui.UiUtilities;

@SuppressWarnings("nls")
public class ProgramPreviewPanel extends JPanel {

  private static final long serialVersionUID = 8953071252035184547L;

  /** The localizer for this class. */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramPreviewPanel.class);

  private MutableProgramIconSettings mProgramIconSettings;
  private JLabel mProgramIconLabel;
  private Font mDateFont;
  private JLabel mDateLabel;
  private JPanel mIconPanel;

  public ProgramPreviewPanel(final Frame dlgParent, ProgramIconSettings programIconSettings, Font dateFont) {

    setLayout(new BorderLayout(3, 3));

    mDateFont = dateFont;
    if (programIconSettings != null) {
      setProgramIconSettings(programIconSettings);
    }

    JButton fontsButton = new JButton(mLocalizer.msg("fonts", "Fonts\u2026"));
    JButton fieldsButton = new JButton(mLocalizer.msg("fields", "Fields\u2026"));

    mDateLabel = new JLabel(new Date().getLongDateString());
    mDateLabel.setForeground(Color.BLACK);
    mProgramIconLabel = new JLabel(createDemoProgramPanel(mProgramIconSettings));

    mIconPanel = new JPanel(new BorderLayout()) {

      private static final long serialVersionUID = -2983369556171749885L;

      @Override
      protected void paintChildren(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Color c = g2d.getColor();
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

    JScrollPane scrollPane = new JScrollPane(mIconPanel);
    scrollPane.setPreferredSize(new Dimension(0, 90));
    add(scrollPane, BorderLayout.CENTER);
    add(new ButtonStackBuilder().addButton(fontsButton, fieldsButton).build(), BorderLayout.LINE_END);

    fontsButton.addActionListener(e -> {
      if (mProgramIconSettings == null) {
        return;
      }
      FontsDialog dlg = new FontsDialog(dlgParent, mProgramIconSettings.getTitleFont(),
          mProgramIconSettings.getTextFont(), mDateFont);
      UiUtilities.centerAndShow(dlg);
      if (dlg.getResult() == FontsDialog.OK) {
        Font titleFont = dlg.getTitleFont();
        Font descFont = dlg.getDescriptionFont();
        mDateFont = dlg.getDateFont();
        mProgramIconSettings.setTextFont(descFont);
        mProgramIconSettings.setTimeFont(titleFont);
        mProgramIconSettings.setTitleFont(titleFont);
        updatePreviewPanel();
      }
    });

    fieldsButton.addActionListener(event -> {
      if (mProgramIconSettings == null) {
        return;
      }
      ProgramItemFieldsConfigDlg dlg = new ProgramItemFieldsConfigDlg(dlgParent,
          mProgramIconSettings.getProgramInfoFields());
      UiUtilities.centerAndShow(dlg);

      if (dlg.getResult() == ProgramItemFieldsConfigDlg.OK) {
        ProgramFieldType[] fieldTypes = dlg.getProgramItemFieldTypes();
        mProgramIconSettings.setProgramInfoFields(fieldTypes);
        updatePreviewPanel();
      }
    });

    updatePreviewPanel();
  }

  public ProgramPreviewPanel(Frame dlgParent) {
    this(dlgParent, null, null);
  }

  public void updatePreviewPanel() {
    mProgramIconLabel.setIcon(createDemoProgramPanel(mProgramIconSettings));
    mDateLabel.setFont(mDateFont);
    if (mDateFont != null) {
      mIconPanel.add(mDateLabel, BorderLayout.PAGE_START);
    }
  }

  public void setProgramIconSettings(ProgramIconSettings settings) {
    mProgramIconSettings = new MutableProgramIconSettings(settings);
    updatePreviewPanel();
  }

  public void setDateFont(Font f) {
    mDateFont = f;
    updatePreviewPanel();
  }

  public void setShowPluginMarking(boolean show) {
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
}