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
*     $Date: 2009-04-17 09:05:19 +0200 (Fr, 17 Apr 2009) $
*   $Author: bananeweizen $
* $Revision: 5652 $
*/

package printplugin.dlgs.printfromqueuedialog;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.Date;
import devplugin.PluginTreeNode;
import devplugin.Program;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import printplugin.EmptyQueueAction;
import printplugin.PrintPlugin;

import util.program.ProgramUtilities;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

@SuppressWarnings("nls")
public class GeneralTab extends JPanel {

  private static final long serialVersionUID = -2247976798179949758L;

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GeneralTab.class);

  private JCheckBox mEmptyQueueCb;
  private PluginTreeNode mRootNode;
  private JButton mEmptyQueueBt;
  private JPanel mProgramListPanel;

  public GeneralTab(PluginTreeNode rootNode) {
    mRootNode = rootNode;

    final PanelBuilder pb = new PanelBuilder(new FormLayout("pref:grow",
        "fill:default:grow,5dlu,pref,10dlu,pref"), this);
    pb.border(Borders.DIALOG);

    JScrollPane scrollPane = new JScrollPane(mProgramListPanel = createProgramListPanel());
    scrollPane.getVerticalScrollBar().setUnitIncrement(30);
    scrollPane.getVerticalScrollBar().setBlockIncrement(80);

    pb.add(scrollPane, CC.xy(1, 1));
    pb.add(mEmptyQueueBt = new JButton(new EmptyQueueAction()), CC.xy(1, 3));
    pb.add(mEmptyQueueCb = new JCheckBox(mLocalizer.msg("emptyQueue", "Empty queue after pringing")), CC.xy(1, 5));

    mEmptyQueueBt.addActionListener(e -> {
      mProgramListPanel.removeAll();
      mProgramListPanel.invalidate();
      mProgramListPanel.repaint();
    });
  }

  private JPanel createProgramListPanel() {

    final JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    Program[] progs = mRootNode.getPrograms();

    Arrays.sort(progs, ProgramUtilities.getProgramComparator());
    Date curDate = null;

    final Font defaultFont = getFont();
    final Font dateFont = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize2D() + 1f);
    final Font channelFont = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize2D() - 1f);

    for (Program prog : progs) {
      if (!prog.getDate().equals(curDate)) {
        curDate = prog.getDate();
        JPanel datePanel = new JPanel(new BorderLayout());
        JLabel dateLb = new JLabel(curDate.getLongDateString());
        dateLb.setFont(dateFont);
        datePanel.add(dateLb, BorderLayout.LINE_START);
        content.add(datePanel);
      }
      addProgramPanel(channelFont, content, prog);
    }

    return content;
  }

  private void addProgramPanel(final Font channelFont, final JPanel content, final Program program) {
    final JPanel progPn = new JPanel(new BorderLayout());

    Icon icon = TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL);
    JButton removeBtn = UiUtilities.createToolBarButton(mLocalizer.msg("removeFromQueue", "Remove from queue"), icon);
    removeBtn.addActionListener(event -> {
      program.unmark(PrintPlugin.getInstance());
      mRootNode.removeProgram(program);
      mRootNode.update();
      content.remove(progPn);
      content.repaint();
    });

    progPn.add(removeBtn, BorderLayout.LINE_START);

    JPanel pn1 = new JPanel(new BorderLayout());
    progPn.add(pn1, BorderLayout.CENTER);
    Channel ch = program.getChannel();
    JLabel channelLb;
    channelLb = new JLabel(ch.getName());
    channelLb.setFont(channelFont);
    channelLb.setHorizontalAlignment(SwingConstants.RIGHT);
    channelLb.setPreferredSize(new Dimension(60, 10));
    pn1.add(channelLb, BorderLayout.LINE_END);

    JLabel progLb = new JLabel("<html><b>" + program.getTimeString() + ":</b> " + program.getTitle());
    pn1.add(progLb, BorderLayout.CENTER);
    content.add(progPn);
  }

  public void setEmptyQueueAfterPrinting(boolean b) {
    mEmptyQueueCb.setSelected(b);
  }

  public boolean emptyQueueAfterPrinting() {
    return mEmptyQueueCb.isSelected();
  }
}