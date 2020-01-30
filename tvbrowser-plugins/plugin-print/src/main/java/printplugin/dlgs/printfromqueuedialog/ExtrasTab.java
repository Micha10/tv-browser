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

package printplugin.dlgs.printfromqueuedialog;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Font;
import java.awt.Frame;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import printplugin.dlgs.components.ProgramPreviewPanel;
import printplugin.settings.ProgramIconSettings;

import util.ui.Localizer;

@SuppressWarnings("nls")
public class ExtrasTab extends JPanel {

  private static final long serialVersionUID = 7731417762679599947L;

  /** The localizer for this class. */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ExtrasTab.class);

  private ProgramPreviewPanel mProgramPreviewPanel;

  private JCheckBox mShowPluginMarkingCb;

  public ExtrasTab(final Frame dlgParent) {
    CellConstraints cc = new CellConstraints();

    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref:grow",
        "pref,5dlu,pref,3dlu,pref,10dlu"), this);
    pb.border(Borders.DIALOG);
    pb.addSeparator(mLocalizer.msg("programItem", "Program item"), cc.xyw(1, 1, 2));
    pb.add(mProgramPreviewPanel = new ProgramPreviewPanel(dlgParent), cc.xy(2, 3));
    pb.add(mShowPluginMarkingCb = new JCheckBox(mLocalizer.msg("showPluginMarkings", "Show plugin markings")),
        cc.xy(2, 5));

    mShowPluginMarkingCb
        .addActionListener(e -> mProgramPreviewPanel.setShowPluginMarking(mShowPluginMarkingCb.isSelected()));
  }

  public void setProgramIconSettings(ProgramIconSettings programIconSettings) {
    mProgramPreviewPanel.setProgramIconSettings(programIconSettings);
    mShowPluginMarkingCb.setSelected(programIconSettings.getPaintPluginMarks());
  }

  public ProgramIconSettings getProgramIconSettings() {
    return mProgramPreviewPanel.getProgramIconSettings();
  }

  public void setDateFont(Font f) {
    mProgramPreviewPanel.setDateFont(f);
  }

  public Font getDateFont() {
    return mProgramPreviewPanel.getDateFont();
  }
}