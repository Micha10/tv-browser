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

package printplugin.dlgs;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.factories.Borders;

import devplugin.Plugin;
import devplugin.ProgressMonitorExtended;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.PrintService;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import printplugin.PrintPlugin;
import printplugin.printer.PrintJob;
import printplugin.settings.PageFormatType;
import printplugin.settings.PluginSettings;
import printplugin.settings.Scheme;
import printplugin.settings.Settings;
import printplugin.util.BaseAction;
import printplugin.util.Utils;

import util.exc.ErrorHandler;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

@SuppressWarnings("nls")
public final class SettingsDialog<S extends Settings> extends JDialog implements ActionListener, WindowClosingIf {

  private static final long serialVersionUID = -3982533489835626647L;

  /** The localizer for this class. */
  public static final Localizer mLocalizer = Localizer.getLocalizerFor(SettingsDialog.class);

  private final DialogContent<S> mDialogContent;
  private final Frame mParentFrame;
  private final PrinterJob mPrinterJob;
  private final Scheme<S>[] mSchemes;

  private DefaultComboBoxModel<Scheme<S>> mSchemeCBModel;
  private JComboBox<Scheme<S>> mSchemeCB;
  private PageFormat mPageFormat;
  private ProgressMonitorExtended mProgressMonitorExtended;

  private PageFormatType mPageFormatType;
  private PluginSettings mSettings;
  private PrintService mSystemPrintService;
  // private PrintService mPrintService;

  private String mToolbarPosition;

  public SettingsDialog(final Frame parent, final DialogContent<S> content) {

    super(parent, content.getDialogTitle(), true);

    UiUtilities.registerForClosing(this);

    mSettings = PrintPlugin.settings();
    mDialogContent = content;
    mParentFrame = parent;
    mPrinterJob = PrinterJob.getPrinterJob();
    mSystemPrintService = mPrinterJob.getPrintService();
    mToolbarPosition = mSettings.toolbarPosition();

    final String printServiceName = mSettings.getPrintServiceName(null);
    final PrintService[] printServices = PrinterJob.lookupPrintServices();
    if (printServiceName != null && printServices != null) {
      for (final PrintService printService : printServices) {
        if (printService.getName().equals(printServiceName)
            && !printService.getName().equals(mSystemPrintService.getName())) {
          try {
            mPrinterJob.setPrintService(printService);
            // mPrintService = printService;
          } catch (PrinterException e) {
          }
          break;
        }
      }
    }

    mPageFormatType = mSettings.getPageFormatType();
    mPageFormat = mPageFormatType.getPageFormat(mPrinterJob, mSettings);

    mSchemes = content.loadSchemes();

    final JPanel contentPane = (JPanel) getContentPane();
    contentPane.setBorder(Borders.DIALOG);
    contentPane.setLayout(new BorderLayout(10, 10));
    contentPane.add(createPrinterActionPanel(), mToolbarPosition);
    contentPane.add(createButtonBar(), BorderLayout.PAGE_END);
    contentPane.add(content.getContent(mParentFrame), BorderLayout.CENTER);

    content.setSettings(mSchemes[0].getSettings());

    PrintPlugin.instance().layoutWindow("settingsDlg", this, new Dimension(450, 400));
    getRootPane().getDefaultButton().requestFocus();
  }

  private JPanel createButtonBar() {
    final JButton printBt = new JButton(BaseAction
        .builder("print", this)
        .text(mLocalizer.msg("print", "Print"))
        .build());
    final JButton cancelBt = new JButton(BaseAction
        .cancel(this)
        .build());
    getRootPane().setDefaultButton(printBt);
    return createSchemePanel().addGlue().addButton(printBt, cancelBt).build();
  }

  private JPanel createPrinterActionPanel() {

    final JButton printerSetupBtn = new JButton(BaseAction
        .builder("printer", this)
        .icon(PrintPlugin.instance().createImageIcon("devices", "printer", TVBrowserIcons.SIZE_SMALL))
        .text(mLocalizer.ellipsisMsg("printer", "Printer"))
        .build());

    final JButton pageBtn = new JButton(BaseAction
        .builder("page", this)
        .icon(PrintPlugin.instance().createImageIcon("actions", "document-properties", TVBrowserIcons.SIZE_SMALL))
        .text(mLocalizer.ellipsisMsg("page", "Page"))
        .build());

    final JButton previewBtn = new JButton(BaseAction
        .builder("preview", this)
        .icon(PrintPlugin.instance().createImageIcon("actions", "document-print-preview", TVBrowserIcons.SIZE_SMALL))
        .text(mLocalizer.ellipsisMsg("preview", "Preview"))
        .build());

    final JPanel panel;
    if (BorderLayout.PAGE_START.equals(mToolbarPosition)) {
      panel = new ButtonBarBuilder().addButton(printerSetupBtn, pageBtn, previewBtn).build();
    } else {
      printerSetupBtn.setHorizontalAlignment(SwingConstants.LEADING);
      pageBtn.setHorizontalAlignment(SwingConstants.LEADING);
      previewBtn.setHorizontalAlignment(SwingConstants.LEADING);
      panel = new ButtonStackBuilder().addButton(printerSetupBtn, pageBtn, previewBtn).build();
    }
    return panel;
  }

  @SuppressWarnings("unchecked")
  private ButtonBarBuilder createSchemePanel() {

    final JButton newSchemeBtn = new JButton(BaseAction
        .builder("newScheme", this)
        .icon(TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL))
        .tooltip(mLocalizer.msg("newScheme", "New scheme"))
        .build());
    newSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    Utils.adjustButtonMargin(newSchemeBtn, 1);

    final JButton mEditSchemeBtn = new JButton(BaseAction
        .builder("editScheme", this)
        .enabled(false)
        .icon(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL))
        .tooltip(mLocalizer.msg("editScheme", "Edit scheme"))
        .build());
    mEditSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    Utils.adjustButtonMargin(mEditSchemeBtn, 1);

    final JButton mDeleteSchemeBtn = new JButton(BaseAction
        .builder("deleteScheme", this)
        .enabled(false)
        .icon(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL))
        .tooltip(mLocalizer.msg("deleteScheme", "Delete scheme"))
        .build());
    mDeleteSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    Utils.adjustButtonMargin(mDeleteSchemeBtn, 1);

    final JButton mSaveSchemeBtn = new JButton(BaseAction
        .builder("saveScheme", this)
        .icon(PrintPlugin.instance().createImageIcon("actions", "document-save", TVBrowserIcons.SIZE_SMALL))
        .tooltip(mLocalizer.msg("saveScheme", "Save scheme"))
        .build());
    mSaveSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    Utils.adjustButtonMargin(mSaveSchemeBtn, 1);

    mSchemeCBModel = new DefaultComboBoxModel<>(mSchemes);
    mSchemeCB = new JComboBox<>(mSchemeCBModel);
    mSchemeCB.addItemListener(e -> {
      mDeleteSchemeBtn.setEnabled(mSchemeCB.getSelectedIndex() > 0);
      mEditSchemeBtn.setEnabled(mSchemeCB.getSelectedIndex() > 0);
      final Scheme<S> scheme = (Scheme<S>) mSchemeCB.getSelectedItem();
      mDialogContent.setSettings(scheme.getSettings());
    });

    return new ButtonBarBuilder()
        .addFixed(mSchemeCB)
        .addRelatedGap()
        .addFixed(newSchemeBtn)
        .addRelatedGap()
        .addFixed(mEditSchemeBtn)
        .addRelatedGap()
        .addFixed(mSaveSchemeBtn)
        .addRelatedGap()
        .addFixed(mDeleteSchemeBtn)
        .addUnrelatedGap();
  }

  public PrintJob getPrintJob() {
    return mDialogContent.createPrintJob(mPageFormat);
  }

  @SuppressWarnings("unchecked")
  public Scheme<S>[] getSchemes() {
    final Scheme<S>[] result = new Scheme[mSchemeCBModel.getSize()];
    for (int i = 0; i < result.length; i++) {
      result[i] = mSchemeCBModel.getElementAt(i);
    }
    return result;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    switch (e.getActionCommand()) {
      case BaseAction.CANCEL:
        close();
        break;
      case "deleteScheme":
        deleteScheme();
        break;
      case "editScheme":
        editScheme();
        break;
      case "newScheme":
        newScheme();
        break;
      case "page":
        page();
        break;
      case "preview":
        preview();
        break;
      case "print":
        print();
        break;
      case "printer":
        printer();
        break;
      case "saveScheme":
        saveScheme();
        break;
      default:
        break;
    }
  }

  @Override
  public void close() {
    setVisible(false);
  }

  @SuppressWarnings("unchecked")
  private void deleteScheme() {
    final Scheme<S> scheme = (Scheme<S>) mSchemeCB.getSelectedItem();
    if (scheme != null) {
      if (JOptionPane.showOptionDialog(
          mParentFrame,
          mLocalizer.msg("deleteSchemeMsg", "Do you want to delete the selected scheme?"),
          mLocalizer.msg("deleteScheme", "Delete Scheme"),
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE,
          null,
          new String[] {Localizer.getLocalization(Localizer.I18N_OK), Localizer.getLocalization(Localizer.I18N_CANCEL)},
          null) == JOptionPane.YES_OPTION) {
        mSchemeCBModel.removeElement(scheme);
        mSchemeCB.setSelectedIndex(0);
        mDialogContent.storeSchemes(getSchemes());
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void editScheme() {
    final Scheme<S> scheme = (Scheme<S>) mSchemeCB.getSelectedItem();
    final Object newSchemeName = Utils.showInputDialog(
        mParentFrame,
        mLocalizer.msg("editScheme", "Edit scheme"),
        mLocalizer.msg("enterNewSchemeName", "Enter new name for scheme:"),
        scheme.getName());
    if (newSchemeName instanceof String) {
      final String name = newSchemeName.toString().trim();
      if (name.length() > 0) {
        scheme.setName(name);
        mSchemeCB.repaint();
        mDialogContent.storeSchemes(getSchemes());
        return;
      }
    }
    showInvalidSchemeNameDialog();
  }

  private void newScheme() {

    final Object newSchemeName = Utils.showInputDialog(
        mParentFrame,
        mLocalizer.msg("newScheme", "New scheme"),
        mLocalizer.msg("enterNewSchemeName", "Enter new name for scheme:"),
        "");
    if (newSchemeName instanceof String) {
      final String name = newSchemeName.toString().trim();
      if (name.length() > 0) {
        final Scheme<S> newScheme = mDialogContent.createNewScheme(name);
        newScheme.setSettings(mDialogContent.getSettings());
        mSchemeCBModel.addElement(newScheme);
        mSchemeCB.setSelectedItem(newScheme);
        mDialogContent.setSettings(newScheme.getSettings());
        mDialogContent.storeSchemes(getSchemes());
        return;
      }
    }
    showInvalidSchemeNameDialog();
  }

  private void page() {
    mPageFormat = mPrinterJob.pageDialog(mPageFormat);
    mPageFormat = mPrinterJob.validatePage(mPageFormat);
  }

  private void preview() {
    final PrintJob job = getPrintJob();
    if (job.getNumOfPages() < 1) {
      JOptionPane.showMessageDialog(
          mParentFrame,
          mLocalizer.msg("noPagesToPrint", "No pages available."),
          mLocalizer.msg("print", "Print"),
          JOptionPane.WARNING_MESSAGE);
      return;
    }
    final PreviewDlg dlg = new PreviewDlg(mParentFrame, job);
    PrintPlugin.instance().layoutWindow("previewDlg", dlg);
    dlg.setVisible(true);
  }

  private void print() {
    final PrintJob job = getPrintJob();
    if (job.getNumOfPages() < 1) {
      JOptionPane.showMessageDialog(
          mParentFrame,
          mLocalizer.msg("noPagesToPrint", "No pages available."),
          mLocalizer.msg("print", "Print"),
          JOptionPane.WARNING_MESSAGE);
      return;
    }
    mProgressMonitorExtended = Plugin.getPluginManager().createProgressMonitor();
    mProgressMonitorExtended.setMessage(mLocalizer.msg("print", "Print"));
    mProgressMonitorExtended.setIndeterminate(true);
    mProgressMonitorExtended.setVisible(true);
    new Thread(() -> {
      try {
        mDialogContent.storeSchemes(getSchemes());
        mPrinterJob.setPrintable(job.getPrintable(), job.getPageFormat());
        mPrinterJob.print();
        mDialogContent.printingDone();
      } catch (Exception ex) {
        ErrorHandler.handle("Could not print pages: " + ex.getLocalizedMessage(), ex);
      } finally {
        SwingUtilities.invokeLater(() -> {
          if (mProgressMonitorExtended != null) {
            mProgressMonitorExtended.setVisible(false);
          }
        });
      }
    }).start();
    close();
  }

  private void printer() {
    mPrinterJob.printDialog();
  }

  @SuppressWarnings("unchecked")
  private void saveScheme() {
    final Scheme<S> scheme = (Scheme<S>) mSchemeCB.getSelectedItem();
    scheme.setSettings(mDialogContent.getSettings());
    mDialogContent.storeSchemes(getSchemes());
  }

  private void showInvalidSchemeNameDialog() {
    JOptionPane.showOptionDialog(mParentFrame,
        mLocalizer.msg("invalidSchemeMsg", "Invalid scheme name"),
        mLocalizer.msg("invalidInput", "Invalid input"),
        JOptionPane.OK_OPTION,
        JOptionPane.WARNING_MESSAGE,
        null,
        new String[] {Localizer.getLocalization(Localizer.I18N_OK)},
        null);
  }
}