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

package printplugin;

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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import printplugin.dlgs.DialogContent;
import printplugin.dlgs.MainPrintDialog;
import printplugin.dlgs.SettingsDialog;
import printplugin.dlgs.printdayprogramsdialog.PrintDayProgramsDialogContent;
import printplugin.dlgs.printfromqueuedialog.PrintFromQueueDialogContent;
import printplugin.dlgs.programinfoprintdialog.PrintProgramInfoDialogContent;
import printplugin.settings.PluginSettings;
import printplugin.settings.ProgramInfoPrintSettings;
import printplugin.settings.Settings;
import printplugin.util.BaseAction;
import printplugin.util.Utils;
import printplugin.util.Utils.FontInfo;

import util.exc.ErrorHandler;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

/**
 * Print Plugin for the desktop version of the TV-Browser project.
 * This is the main class of the extension.
 *
 * @author Martin Oberhauser
 * @author Thorsten Giesecke
 */
@SuppressWarnings("nls")
public final class PrintPlugin extends Plugin {

  private static final Version mVersion = new Version(3, 02, 7, false);

  /** The localizer for this class. */
  @SuppressWarnings("hiding")
  public static final Localizer mLocalizer = Localizer.getLocalizerFor(PrintPlugin.class);

  private static PrintPlugin mInstance;

  private final List<FontInfo> mFontInfo;

  private PluginInfo mPluginInfo;
  private PluginSettings mPluginSettings;
  private ProgramInfoPrintSettings mOldProgramInfoPrintSettings;
  private Properties mSettings;

  private int mMarkPriority = -2;

  /**
   * Creates a new instance of this plugin.
   *
   * @see #getInstance()
   */
  public PrintPlugin() {
    mInstance = this;
    mFontInfo = new ArrayList<>(7);
    Utils.registerFontsAsync(mFontInfo);
  }

  /**
   * Returns an unique instance (singleton) of this plugin.
   *
   * @return unique instance of this plugin
   */
  public static PrintPlugin getInstance() {
    return mInstance;
  }

  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("devices", "printer", TVBrowserIcons.SIZE_SMALL);
  }

  /**
   * Returns the version of this plugin.
   *
   * @return {@link Version} of this plugin.
   */
  public static Version getVersion() {
    return mVersion;
  }

  @Override
  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("printProgram", "Print program");
      final String desc = mLocalizer.msg("printdescription", "Allows printing programs.");
      final String author = "Martin Oberhauser (martin@tvbrowser.org), Thorsten Giesecke (tvbrowser@giesecke.org)";
      mPluginInfo = new PluginInfo(PrintPlugin.class, name, desc, author);
    }

    return mPluginInfo;
  }

  @Override
  public void onActivation() {
    final PluginTreeNode rootNode = getRootNode();
    for (final Program program : rootNode.getPrograms()) {
      program.mark(this);
    }
    rootNode.update();
    rootNode.addAction(BaseAction
        .builder("emptyQueue", e -> emptyQueue(rootNode))
        .icon(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL))
        .text(mLocalizer.msg("emptyQueue", "Clear printer queue"))
        .build());
  }

  @Override
  public void handleTvBrowserStartFinished() {
    for (final Program program : getRootNode().getPrograms()) {
      program.validateMarking();
    }
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {

    final ImageIcon icon = createImageIcon("devices", "printer", TVBrowserIcons.SIZE_SMALL);
    final List<AbstractAction> actions = new ArrayList<>(4);
    final PluginTreeNode rootNode = getRootNode();

    if (rootNode.contains(program)) {
      actions.add(BaseAction
          .builder("removeFromPrinterQueue", e -> removeFromPrinterQueue(program, rootNode))
          .icon(icon)
          .text(mLocalizer.msg("removeFromPrinterQueue", "Remove from printer queue"))
          .build());
    } else {
      actions.add(BaseAction
          .builder("addToPrinterQueue", e -> addToPrinterQueue(program, rootNode))
          .icon(icon)
          .text(mLocalizer.msg("addToPrinterQueue", "Add to printer queue"))
          .build());
    }

    actions.add(BaseAction
        .builder("printProgramInfo", e -> showPrintDialog(new PrintProgramInfoDialogContent(program)))
        .icon(icon)
        .text(mLocalizer.msg("printProgramInfo", "Print program info"))
        .build());

    if (canPrintQueue()) {
      actions.add(ContextMenuSeparatorAction.getInstance());
      actions.add(BaseAction
          .builder("printQueue", e -> showPrintDialog(new PrintFromQueueDialogContent(rootNode)))
          .icon(icon)
          .text(mLocalizer.msg("printQueue", "Print queue"))
          .build());
    }
    return new ActionMenu(mLocalizer.msg("printProgram", "Print"), icon,
        actions.toArray(new AbstractAction[actions.size()]));
  }

  /**
   * Returns <code>true</code> if the plugin's root node has children
   * (marked or selected programs).
   *
   * @return <code>true</code> if the plugin's root node has children
   * @see Plugin#getRootNode()
   * @see PluginTreeNode
   */
  public boolean canPrintQueue() {
    return !getRootNode().isEmpty();
  }

  @Override
  public ActionMenu getButtonAction() {
    return new ActionMenu(BaseAction
        .builder("print", e -> print())
        .icon(createImageIcon("devices", "printer", TVBrowserIcons.SIZE_SMALL))
        .largeIcon(createImageIcon("devices", "printer", TVBrowserIcons.SIZE_LARGE))
        .text(mLocalizer.msg("print", "Print"))
        .tooltip(getInfo().getDescription())
        .build());
  }

  @Override
  public boolean receivePrograms(final Program[] programs, final ProgramReceiveTarget receiveTarget) {
    final PluginTreeNode rootNode = getRootNode();
    for (Program program : programs) {
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

  /**
   * Returns the settings of this plugin.
   *
   * @return the plugin's settings
   * @see #getPluginSettings()
   * @see Properties
   */
  public Properties getSettings() {
    return mSettings;
  }

  /**
   * Returns the {@link PluginSettings}.
   *
   * @return the plugin's settings
   * @see #getSettings()
   */
  public PluginSettings getPluginSettings() {
    if (mPluginSettings == null) {
      mPluginSettings = new PluginSettings(mSettings);
    }
    return mPluginSettings;
  }

  /**
   * Returns a list of loaded {@link FontInfo} instances.
   * The font info instances are containing information about
   * fonts loaded during startup.
   *
   * @return a list of loaded {@link FontInfo} instances
   * @see Utils#registerFontsAsync(List)
   */
  public List<FontInfo> getFontInfo() {
    return mFontInfo;
  }

  /**
   * Returns a {@link ProgramInfoPrintSettings} instance, or <code>null</code>
   * if the data are greater or equals to version 3 of the settings (theme
   * support).
   *
   * @return a loaded {@link ProgramInfoPrintSettings} object, or
   *           <code>null</code>
   * @see #readData(ObjectInputStream)
   */
  public ProgramInfoPrintSettings getOldProgramInfoPrintSettings() {
    return mOldProgramInfoPrintSettings;
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
    return new PrintPluginSettingsTab(this);
  }

  @Override
  public int getMarkPriorityForProgram(final Program p) {
    if (mMarkPriority == -2 && mSettings != null) {
      // mSettings.getProperty("markPriority",
      // String.valueOf(ProgramCompat.PRIORITY_MARK_MIN))
      mMarkPriority = Integer
          .parseInt(mSettings.getProperty("markPriority", String.valueOf(0)));
    }
    return mMarkPriority;
  }

  /**
   * Sets the mark priority of the plugin.
   *
   * @param priority
   *                   the mark priority to set
   * @see #handleTvBrowserStartFinished()
   */
  protected void setMarkPriority(final int priority) {
    mMarkPriority = priority;
    mSettings.setProperty("markPriority", String.valueOf(priority));
    handleTvBrowserStartFinished();
  }

  @Override
  public String getPluginCategory() {
    return Plugin.CATEGORY_OTHER;
  }

  private void addToPrinterQueue(final Program program, final PluginTreeNode rootNode) {
    rootNode.addProgram(program);
    rootNode.update();
    program.mark(this);
  }

  private static void emptyQueue(final PluginTreeNode rootNode) {
    rootNode.removeAllChildren();
    rootNode.update();
  }

  private void print() {
    if (getPluginManager().getFilterManager() != null) {
      final MainPrintDialog mainDialog = new MainPrintDialog(getParentFrame());
      // layoutWindow("mainDlg", mainDialog, mainDialog.getSize());
      mainDialog.setVisible(true);

      try {
        switch (mainDialog.getResult()) {
          case MainPrintDialog.PRINT_DAYPROGRAMS:
            showPrintDialog(new PrintDayProgramsDialogContent());
            break;
          case MainPrintDialog.PRINT_QUEUE:
            showPrintDialog(new PrintFromQueueDialogContent(getRootNode()));
            break;
          case MainPrintDialog.PRINT_SETTINGS:
            Plugin.getPluginManager().showSettings(this);
            break;
          default:
            break;
        }
      } catch (Exception e) {
        ErrorHandler.handle("MainDialog#getResult " + mainDialog.getResult(), e);
        throw e;
      }
    }
  }

  private void removeFromPrinterQueue(final Program program, final PluginTreeNode rootNode) {
    rootNode.removeProgram(program);
    rootNode.update();
    program.unmark(this);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private <S extends Settings> void showPrintDialog(final DialogContent<S> content) {
    UiUtilities.centerAndShow(new SettingsDialog(getParentFrame(), content));
  }
}