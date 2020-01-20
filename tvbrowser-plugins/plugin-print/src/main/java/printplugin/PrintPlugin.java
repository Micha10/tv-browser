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
 *     $Date: 2011-08-04 02:44:48 +0200 (Do, 04 Aug 2011) $
 *   $Author: ds10 $
 * $Revision: 7079 $
 */

package printplugin;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import devplugin.ActionMenu;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;
import printplugin.dlgs.DialogContent;
import printplugin.dlgs.MainPrintDialog;
import printplugin.dlgs.SettingsDialog;
import printplugin.dlgs.printdayprogramsdialog.PrintDayProgramsDialogContent;
import printplugin.dlgs.printfromqueuedialog.PrintFromQueueDialogContent;
import printplugin.dlgs.programinfoprintdialog.PrintProgramInfoDialogContent;
import printplugin.settings.PluginSettings;
import printplugin.settings.ProgramInfoPrintSettings;
import printplugin.settings.Settings;
import printplugin.util.Utils;
import printplugin.util.Utils.FontInfo;
import util.ui.Localizer;
import util.ui.UiUtilities;

@SuppressWarnings("nls")
public final class PrintPlugin extends Plugin {

  private static final Version mVersion = new Version(3, 02, 5, false);

  /** The localizer for this class. */
  @SuppressWarnings("hiding")
  public static final Localizer mLocalizer = Localizer.getLocalizerFor(PrintPlugin.class);

  private static PrintPlugin mInstance;

  private final List<FontInfo> mFontInfo;

  private PluginInfo mPluginInfo;
  private PluginSettings mPluginSettings;
  private ProgramInfoPrintSettings mOldProgramInfoPrintSettings;

  /** Global Settings for the PrintPlugin */
  private Properties mSettings;

  private int mMarkPriority = -2;

  public ProgramInfoPrintSettings getOldProgramInfoPrintSettings() {
    return mOldProgramInfoPrintSettings;
  }

  public PrintPlugin() {
    mInstance = this;
    mFontInfo = new ArrayList<>(7);
    Utils.registerFontsAsync(mFontInfo);
  }

  public static PrintPlugin getInstance() {
    return mInstance;
  }

  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("devices", "printer", 16);
  }

  public static Version getVersion() {
    return mVersion;
  }

  @Override
  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      String name = mLocalizer.msg("printProgram", "Print program");
      String desc = mLocalizer.msg("printdescription", "Allows printing programs.");
      String author = "Martin Oberhauser (martin@tvbrowser.org), Thorsten Giesecke (tvbrowser@giesecke.org)";

      mPluginInfo = new PluginInfo(PrintPlugin.class, name, desc, author);
    }

    return mPluginInfo;
  }

  @Override
  public void onActivation() {
    PluginTreeNode root = getRootNode();
    Program[] progs = root.getPrograms();
    for (Program program : progs) {
      program.mark(this);
    }
    root.update();
    root.addAction(new EmptyQueueAction());
  }

  @Override
  public void handleTvBrowserStartFinished() {
    Program[] programs = getRootNode().getPrograms();

    for (Program program : programs) {
      program.validateMarking();
    }
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    final Plugin thisPlugin = this;
    ImageIcon icon = createImageIcon("devices", "printer", 16);

    List<AbstractAction> actions = new ArrayList<>();

    if (getRootNode().contains(program)) {
      AbstractAction action = new AbstractAction() {

        private static final long serialVersionUID = -8540985878541540847L;

        @Override
        public void actionPerformed(ActionEvent e) {
          getRootNode().removeProgram(program);
          getRootNode().update();
          program.unmark(thisPlugin);
        }
      };
      action.putValue(Action.NAME, mLocalizer.msg("removeFromPrinterQueue", "Remove from printer queue"));
      action.putValue(Action.SMALL_ICON, icon);
      actions.add(action);
    } else {
      AbstractAction action = new AbstractAction() {

        private static final long serialVersionUID = -2514534299381130838L;

        @Override
        public void actionPerformed(ActionEvent event) {
          getRootNode().addProgram(program);
          getRootNode().update();
          program.mark(thisPlugin);
        }
      };
      action.putValue(Action.NAME, mLocalizer.msg("addToPrinterQueue", "Add to printer queue"));
      action.putValue(Action.SMALL_ICON, icon);
      actions.add(action);
    }
    AbstractAction action = new AbstractAction() {

      private static final long serialVersionUID = 413218914578365116L;

      @Override
      public void actionPerformed(ActionEvent e) {
        /*
         * Window parent = UiUtilities.getLastModalChildOf(getParentFrame());
         * new ProgramInfoPrintDialog(parent, program);
         */
        showPrintDialog(new PrintProgramInfoDialogContent(program));
      }
    };

    action.putValue(Action.NAME, mLocalizer.msg("printProgramInfo", "Print program info"));
    action.putValue(Action.SMALL_ICON, icon);
    actions.add(action);

    if (canPrintQueue()) {
      actions.add(ContextMenuSeparatorAction.getInstance());
      actions.add(new AbstractAction(mLocalizer
          .msg("printQueue", "Print queue"), icon) {

        private static final long serialVersionUID = -5838274417319524387L;

        @Override
        public void actionPerformed(ActionEvent e) {
          showPrintDialog(new PrintFromQueueDialogContent(getRootNode()));
        }
      });
    }

    AbstractAction[] actionArray = new AbstractAction[actions.size()];
    actions.toArray(actionArray);
    return new ActionMenu(mLocalizer.msg("printProgram", "Print"), icon, actionArray);
  }

  public boolean canPrintQueue() {
    return !getRootNode().isEmpty();
  }

  @Override
  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {

      private static final long serialVersionUID = 1832004024084886003L;

      @Override
      public void actionPerformed(ActionEvent evt) {
        if (getPluginManager().getFilterManager() != null) {
          MainPrintDialog mainDialog = new MainPrintDialog(getParentFrame());
          // layoutWindow("mainDlg", mainDialog, mainDialog.getSize());
          mainDialog.setVisible(true);
          int result = mainDialog.getResult();
          if (result == MainPrintDialog.PRINT_DAYPROGRAMS) {
            showPrintDialog(
                new PrintDayProgramsDialogContent());
          } else if (result == MainPrintDialog.PRINT_QUEUE) {
            showPrintDialog(
                new PrintFromQueueDialogContent(getRootNode()));
          } else if (result == MainPrintDialog.PRINT_SETTINGS) {
            Plugin.getPluginManager().showSettings(PrintPlugin.this);
          }
        }
      }
    };

    action.putValue(Action.NAME, mLocalizer.msg("print", "Print"));
    action.putValue(Action.SMALL_ICON, createImageIcon("devices", "printer", 16));
    action.putValue(BIG_ICON, createImageIcon("devices", "printer", 22));
    action.putValue(Action.SHORT_DESCRIPTION, getInfo().getDescription());

    return new ActionMenu(action);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private <S extends Settings> void showPrintDialog(final DialogContent<S> content) {
    UiUtilities.centerAndShow(new SettingsDialog(getParentFrame(), content));
  }

  @Override
  public boolean receivePrograms(final Program[] programArr, final ProgramReceiveTarget receiveTarget) {
    final PluginTreeNode rootNode = getRootNode();
    for (Program program : programArr) {
      if (!rootNode.contains(program)) {
        rootNode.addProgram(program);
        program.mark(this);
      }
    }
    rootNode.update();
    return true;
  }

  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public void loadSettings(final Properties settings) {
    mSettings = settings;
    mPluginSettings = new PluginSettings(mSettings);
  }

  @Override
  public Properties storeSettings() {
    return mSettings;
  }

  public Properties getSettings() {
    return mSettings;
  }

  public PluginSettings getPluginSettings() {
    if (mPluginSettings == null) {
      mPluginSettings = new PluginSettings(mSettings);
    }
    return mPluginSettings;
  }

  @Override
  public void readData(final ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    try {
      final int version = in.readInt();
      if (version < 3) {
        mOldProgramInfoPrintSettings = new ProgramInfoPrintSettings();
        mOldProgramInfoPrintSettings.readData(in, version);
      }
    } catch (Throwable t) {
      mOldProgramInfoPrintSettings = null;
      t.printStackTrace();
      throw t;
    }
  }

  @Override
  public void writeData(final ObjectOutputStream out) throws IOException {
    // Starting with version 3, ProgramInfoPrintSettings are written as part of
    // ProgramInfoScheme
    storeRootNode();
    out.writeInt(3);
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new PrintPluginSettingsTab();
  }

  @Override
  public int getMarkPriorityForProgram(final Program p) {
    if (mMarkPriority == -2 && mSettings != null) {
      //mSettings.getProperty("markPriority", String.valueOf(ProgramCompat.PRIORITY_MARK_MIN))
      mMarkPriority = Integer
          .parseInt(mSettings.getProperty("markPriority", String.valueOf(0)));
      return mMarkPriority;
    } else {
      return mMarkPriority;
    }
  }

  protected void setMarkPriority(final int priority) {
    mMarkPriority = priority;
    mSettings.setProperty("markPriority", String.valueOf(priority));
    handleTvBrowserStartFinished();
  }

  @Override
  public String getPluginCategory() {
    return Plugin.CATEGORY_OTHER;
  }
}