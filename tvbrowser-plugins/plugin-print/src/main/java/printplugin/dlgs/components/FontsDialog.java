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

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * A {@link JDialog} that let the user select fonts for different parts of
 * the program text to print: title, description, and dates (optional).
 *
 * @author Bananeweizen
 * @since 2009-04-25 09:58:28 +0200
 */
@SuppressWarnings("nls")
public class FontsDialog extends JDialog implements WindowClosingIf {

  private static final long serialVersionUID = 1108301325894399327L;

  /** The localizer for this class. */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(FontsDialog.class);

  private static final int CANCEL = 0;
  protected static final int OK = 1;

  private FontChooserPanel mTitleFontPanel;
  private FontChooserPanel mDescriptionFontPanel;
  private FontChooserPanel mDateFontPanel;
  private int mResult = CANCEL;

  /**
   * Creates a new dialog instance with the given parameters.
   *
   * @param parent
   *                          the parent {@link Frame} of this dialog
   * @param titleFont
   *                          the initial font that is used for program titles
   * @param descriptionFont
   *                          the initial font that is used for program content
   *                          (description)
   * @param dateFont
   *                          the initial font that is used to display dates
   *                          (optional)
   */
  public FontsDialog(final Frame parent, final Font titleFont, final Font descriptionFont, final Font dateFont) {
    super(parent, true);
    setTitle(mLocalizer.msg("dialog.title", "Fonts"));

    UiUtilities.registerForClosing(this);

    final JPanel content = (JPanel) getContentPane();
    content.setBorder(Borders.DIALOG);
    content.setLayout(new BorderLayout());
    content.add(createForm(titleFont, descriptionFont, dateFont), BorderLayout.PAGE_START);
    content.add(createButtonBar(), BorderLayout.PAGE_END);
    pack();
    setMinimumSize(getSize());
    getRootPane().getDefaultButton().requestFocus();
  }

  private JPanel createForm(final Font titleFont, final Font descriptionFont, final Font dateFont) {

    mTitleFontPanel = new FontChooserPanel(mLocalizer.msg("title", "Title"), titleFont, true);
    mDescriptionFontPanel = new FontChooserPanel(mLocalizer.msg("description", "Description"), descriptionFont, true);

    final FormLayout layout = new FormLayout("pref:grow", "pref,5dlu,pref,10dlu");
    final JPanel form = new JPanel();
    final PanelBuilder pb = new PanelBuilder(layout, form);
    pb.add(mTitleFontPanel, CC.xy(1, 1));
    pb.add(mDescriptionFontPanel, CC.xy(1, 3));
    if (dateFont != null) {
      mDateFontPanel = new FontChooserPanel(mLocalizer.msg("date", "Date"), dateFont, true);
      layout.insertRow(4, RowSpec.decode("5dlu"));
      layout.insertRow(5, RowSpec.decode("pref"));
      form.add(mDateFontPanel, CC.xy(1, 5));
    }
    return form;
  }

  private JPanel createButtonBar() {

    final JButton okBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    okBt.addActionListener(e -> {
      mResult = OK;
      setVisible(false);
    });
    okBt.setActionCommand("ok");
    okBt.setDefaultCapable(true);

    final JButton cancelBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancelBt.addActionListener(e -> close());
    cancelBt.setActionCommand("cancel");

    getRootPane().setDefaultButton(okBt);

    return new ButtonBarBuilder().addGlue().addButton(okBt, cancelBt).build();
  }

  public int getResult() {
    return mResult;
  }

  public Font getTitleFont() {
    return mTitleFontPanel.getChosenFont();
  }

  public Font getDescriptionFont() {
    return mDescriptionFontPanel.getChosenFont();
  }

  public Font getDateFont() {
    if (mDateFontPanel != null) {
      return mDateFontPanel.getChosenFont();
    }
    return null;
  }

  @Override
  public void close() {
    mResult = CANCEL;
    setVisible(false);
  }
}