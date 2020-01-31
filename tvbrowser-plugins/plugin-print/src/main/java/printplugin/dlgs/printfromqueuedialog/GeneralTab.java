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
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.PluginTreeNode;
import devplugin.Program;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import printplugin.PrintPlugin;
import printplugin.util.BaseAction;

import util.exc.ErrorHandler;
import util.misc.OperatingSystem;
import util.program.ProgramUtilities;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;

@SuppressWarnings("nls")
public class GeneralTab extends JPanel implements ActionListener {

  private static final long serialVersionUID = -2247976798179949758L;

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GeneralTab.class);

  private final JButton mEmptyQueueBt;
  private final JCheckBox mEmptyQueueCb;
  private final JList<Program> mList;
  private final JButton mRemoveSelected;

  private final DefaultListModel<Program> mListModel;
  private final Frame mParentFrame;
  private final PluginTreeNode mRootNode;
  private final String mTitle;

  private ProgramListCellRenderer mProgramListCellRenderer;

  public GeneralTab(final Frame parent, final String title, final PluginTreeNode rootNode) {

    try {
      mParentFrame = parent;
      mTitle = title;
      mRootNode = rootNode;

      final Program[] programs = mRootNode.getPrograms();
      Arrays.sort(programs, ProgramUtilities.getProgramComparator());

      mList = initList(programs);
      mListModel = new DefaultListModel<>();
      for (final Program program : programs) {
        mListModel.addElement(program);
      }
      mList.setModel(mListModel);

      mRemoveSelected = new JButton(BaseAction
          .builder("removeFromQueue", this)
          .enabled(!mListModel.isEmpty() && mList.getSelectedIndices().length > 0)
          .text(mLocalizer.msg("removeFromQueue", "Remove selected"))
          .build());
      mEmptyQueueBt = new JButton(BaseAction
          .builder("clearQueue", this)
          .enabled(!mListModel.isEmpty())
          .icon(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL))
          .text(mLocalizer.msg("clearQueue", "Clear printer queue"))
          .build());
      mEmptyQueueCb = new JCheckBox(mLocalizer.msg("emptyQueue", "Empty queue after printing"));

      final PanelBuilder pb = new PanelBuilder(new FormLayout("pref:grow,5dlu,pref:grow",
          "fill:default:grow,5dlu,pref,10dlu,pref"), this);
      pb.add(new JScrollPane(mList), CC.xyw(1, 1, 3));
      pb.add(mRemoveSelected, CC.xy(1, 3));
      pb.add(mEmptyQueueBt, CC.xy(3, 3));
      pb.add(mEmptyQueueCb, CC.xyw(1, 5, 3));
      pb.border(Borders.DIALOG);
    } catch (Exception e) {
      ErrorHandler.handle("GeneralTab", e);
      throw e;
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private JList<Program> initList(final Program[] programs) {
    final JList<Program> list = new JList() {

      private static final long serialVersionUID = 3607552994305420657L;

      @Override
      public boolean getScrollableTracksViewportWidth() {
        return OperatingSystem.isMacOs() ? super.getScrollableTracksViewportWidth() : true;
      }
    };
    Font font = UIManager.getFont("List.font");
    if (font == null) {
      font = UIManager.getFont("Label.font");
    }
    list.setFixedCellHeight(font.getSize() * 3);
    list.addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(final ComponentEvent e) {
        // cache invalidation by temporarily setting fixed height
        if (!OperatingSystem.isMacOs()) {
          list.setFixedCellHeight(mProgramListCellRenderer.getMaxHeight());
          list.setFixedCellHeight(-1);
        }
      }
    });
    list.addKeyListener(new KeyAdapter() {

      @Override
      public void keyTyped(final KeyEvent e) {
        switch (e.getKeyChar()) {
          case KeyEvent.VK_BACK_SPACE:
          case KeyEvent.VK_DELETE:
            if (!mList.getSelectedValuesList().isEmpty()) {
              removeSelected();
            }
            break;
          default:
            break;
        }
      }
    });

    list.addListSelectionListener(e -> update());
    mProgramListCellRenderer = new ProgramListCellRenderer();
    list.setCellRenderer(mProgramListCellRenderer);

    return list;
  }

  private void update() {
    mEmptyQueueBt.setEnabled(!mListModel.isEmpty());
    mRemoveSelected.setEnabled(!mListModel.isEmpty() && mList.getSelectedIndices().length > 0);
  }

  private void clearQueue() {
    if (JOptionPane.showOptionDialog(
        mParentFrame,
        mLocalizer.msg("clearQueue", "Clear printer queue").concat("?"),
        mTitle,
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        new String[] {Localizer.getLocalization(Localizer.I18N_OK), Localizer.getLocalization(Localizer.I18N_CANCEL)},
        null) == JOptionPane.YES_OPTION) {
      PluginTreeNode root = PrintPlugin.getInstance().getRootNode();
      root.removeAllChildren();
      root.update();
      mListModel.clear();
      update();
    }
  }

  private void removeSelected() {
    final PrintPlugin printPlugin = PrintPlugin.getInstance();
    final List<Program> selectedValuesList = mList.getSelectedValuesList();
    for (final Program program : selectedValuesList) {
      mListModel.removeElement(program);
      program.unmark(printPlugin);
      mRootNode.removeProgram(program);
    }
    mRootNode.update();
    update();
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    switch (e.getActionCommand()) {
      case "clearQueue":
        clearQueue();
        break;
      case "removeFromQueue":
        removeSelected();
        break;
      default:
        break;
    }
  }

  public void setEmptyQueueAfterPrinting(final boolean selected) {
    mEmptyQueueCb.setSelected(selected);
  }

  public boolean emptyQueueAfterPrinting() {
    return mEmptyQueueCb.isSelected();
  }
}