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
import com.jgoodies.forms.factories.Borders;

import devplugin.ProgramFieldType;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import printplugin.util.BaseAction;

import util.ui.Localizer;
import util.ui.OrderChooser;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * {@link JDialog} that lets the user choose from a list of
 * {@link ProgramFieldType} items to display while printing.
 *
 * @author bananeweizen
 * @since 2010-06-28 19:33:48 +0200
 */
@SuppressWarnings("nls")
public class ProgramItemFieldsConfigDlg extends JDialog implements ActionListener, WindowClosingIf {

  private static final long serialVersionUID = 5596520895252347786L;

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramItemFieldsConfigDlg.class);

  protected static final int OK = 0;
  private static final int CANCEL = 1;

  private final OrderChooser<ProgramFieldType> mOrderChooser;

  private int mResult = CANCEL;

  /**
   * Creates a new instance of the dialog using the given parent frame and field
   * types.
   *
   * @param parent
   *                     the parent frame
   * @param fieldTypes
   *                     the {@link ProgramFieldType} array for initialization
   */
  public ProgramItemFieldsConfigDlg(Frame parent, ProgramFieldType[] fieldTypes) {

    super(parent, mLocalizer.msg("configureProgram", "Configure program data"), true);

    UiUtilities.registerForClosing(this);

    final JButton okBt = new JButton(BaseAction.ok(this).build());
    final JButton cancelBt = new JButton(BaseAction.cancel(this).build());

    final JPanel contentPane = (JPanel) getContentPane();
    contentPane.setLayout(new BorderLayout(0, 10));
    contentPane.setBorder(Borders.DIALOG);
    contentPane.add(new LineWrapLabel(mLocalizer.msg("configureProgramDesc",
        "Bestimmen Sie welche Informationen und in welcher " +
            "Reihenfolge diese Informationen dargestellt werden. " +
            "Beachten Sie, dass je nach verfügbarem Platz auf dem Papier " +
            "nicht alles dargestellt werden kann.")),
        BorderLayout.PAGE_START);
    contentPane.add(mOrderChooser = new OrderChooser<>(fieldTypes, getAvailableTypes(), true), BorderLayout.CENTER);
    contentPane.add(new ButtonBarBuilder().addGlue().addButton(okBt, cancelBt).build(), BorderLayout.PAGE_END);

    final Dimension dimension = getSize();
    dimension.height = Math.min(330, getGraphicsConfiguration().getBounds().height / 3);
    dimension.width = Math.max(dimension.width, 437);
    setMinimumSize(dimension);
    setSize(dimension);
    getRootPane().setDefaultButton(okBt);
    okBt.requestFocus();
  }

  /**
   * Returns the result (OK or Cancel).
   *
   * @return the result (OK or Cancel)
   */
  public int getResult() {
    return mResult;
  }

  /**
   * Returns the selected {@link ProgramFieldType} entries as an array.
   *
   * @return the selected {@link ProgramFieldType} entries as an array
   */
  public ProgramFieldType[] getProgramItemFieldTypes() {
    return mOrderChooser.getOrderList().toArray(new ProgramFieldType[0]);
  }

  private static ProgramFieldType[] getAvailableTypes() {
    final List<ProgramFieldType> typeList = new ArrayList<>();

    final Iterator<ProgramFieldType> typeIter = ProgramFieldType.getTypeIterator();
    while (typeIter.hasNext()) {
      ProgramFieldType type = typeIter.next();

      if (type.getFormat() != ProgramFieldType.FORMAT_BINARY
          && type != ProgramFieldType.INFO_TYPE
          && type != ProgramFieldType.START_TIME_TYPE
          && type != ProgramFieldType.END_TIME_TYPE
          && type != ProgramFieldType.TITLE_TYPE) {
        typeList.add(type);
      }
    }

    final ProgramFieldType[] typeArr = new ProgramFieldType[typeList.size()];
    typeList.toArray(typeArr);
    return typeArr;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    mResult = CANCEL;
    setVisible(false);
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void actionPerformed(final ActionEvent e) {
    switch (e.getActionCommand()) {
      case BaseAction.CANCEL:
        close();
        break;
      case BaseAction.OK:
        mResult = OK;
        setVisible(false);
        break;
    }
  }
}