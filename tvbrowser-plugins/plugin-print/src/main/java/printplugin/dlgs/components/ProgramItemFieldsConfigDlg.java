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
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */

package printplugin.dlgs.components;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;

import devplugin.ProgramFieldType;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import util.ui.Localizer;
import util.ui.OrderChooser;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

@SuppressWarnings("nls")
public class ProgramItemFieldsConfigDlg extends JDialog implements WindowClosingIf {

  private static final long serialVersionUID = 5596520895252347786L;

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramItemFieldsConfigDlg.class);

  private OrderChooser<ProgramFieldType> mOrderChooser;
  protected static final int OK = 0;
  private static final int CANCEL = 1;
  private int mResult = CANCEL;

  public ProgramItemFieldsConfigDlg(Frame parent, ProgramFieldType[] fieldTypes) {

    super(parent, mLocalizer.msg("configureProgram", "Configure program data"), true);

    UiUtilities.registerForClosing(this);

    final JButton okBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    okBt.addActionListener(event -> {
      mResult = OK;
      setVisible(false);
    });
    okBt.setDefaultCapable(true);

    final JButton cancelBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancelBt.addActionListener(event -> close());

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

    // pack();
    final Dimension dimension = getSize();
    dimension.height = Math.min(330, getGraphicsConfiguration().getBounds().height / 3);
    dimension.width = Math.max(dimension.width, 437);
    setMinimumSize(dimension);
    setSize(dimension);
    getRootPane().setDefaultButton(okBt);
    okBt.requestFocus();
  }

  public int getResult() {
    return mResult;
  }

  public ProgramFieldType[] getProgramItemFieldTypes() {
    return mOrderChooser.getOrderList().toArray(new ProgramFieldType[0]);
  }

  private static ProgramFieldType[] getAvailableTypes() {
    List<ProgramFieldType> typeList = new ArrayList<>();

    Iterator<ProgramFieldType> typeIter = ProgramFieldType.getTypeIterator();
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

    ProgramFieldType[] typeArr = new ProgramFieldType[typeList.size()];
    typeList.toArray(typeArr);
    return typeArr;
  }

  @Override
  public void close() {
    mResult = CANCEL;
    setVisible(false);
  }
}