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
 *     $Date: 2009-06-28 11:01:16 +0200 (So, 28 Jun 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5777 $
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
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

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
import printplugin.settings.Scheme;
import printplugin.settings.Settings;

import util.exc.ErrorHandler;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

@SuppressWarnings("nls")
public final class SettingsDialog<S extends Settings> extends JDialog implements WindowClosingIf {

  private static final long serialVersionUID = -3982533489835626647L;

  /** The localizer for this class. */
  public static final Localizer mLocalizer = Localizer.getLocalizerFor(SettingsDialog.class);

  private DialogContent<S> mDialogContent;
  private PageFormat mPageFormat;
  private PrinterJob mPrinterJob;
  private Scheme<S>[] mSchemes;

  private DefaultComboBoxModel<Scheme<S>> mSchemeCBModel;
  private Frame mParentFrame;
  private JComboBox<Scheme<S>> mSchemeCB;

  private ProgressMonitorExtended mProgressMonitorExtended;

  public SettingsDialog(final Frame parent, final DialogContent<S> content) {

    super(parent, content.getDialogTitle(), true);
    UiUtilities.registerForClosing(this);

    mDialogContent = content;
    mParentFrame = parent;
    mPrinterJob = PrinterJob.getPrinterJob();
    mPageFormat = mPrinterJob.validatePage(mPrinterJob.defaultPage());
    mPageFormat = PageFormatType.NORMAL.getPageFormat(mPrinterJob, mPageFormat.getPaper(),
        mPageFormat.getOrientation());

    mSchemes = content.loadSchemes();

    final JPanel contentPane = (JPanel) getContentPane();
    contentPane.setBorder(Borders.DIALOG);
    contentPane.setLayout(new BorderLayout(10, 10));
    contentPane.add(createPrinterActionPanel(), BorderLayout.LINE_END);
    contentPane.add(createButtonBar(), BorderLayout.PAGE_END);
    contentPane.add(content.getContent(mParentFrame), BorderLayout.CENTER);

    content.setSettings(mSchemes[0].getSettings());

    PrintPlugin.getInstance().layoutWindow("settingsDlg", this, new Dimension(450, 400));
    getRootPane().getDefaultButton().requestFocus();
  }

  /**
   * @param parent
   * @param printerJob
   * @return
   */
  private JPanel createPrinterActionPanel() {

    final JButton printerSetupBtn = new JButton(mLocalizer.ellipsisMsg("printer", "Printer"),
        PrintPlugin.getInstance().createImageIcon("devices", "printer", 16));
    printerSetupBtn.addActionListener(event -> {
      mPrinterJob.printDialog();
    });
    printerSetupBtn.setActionCommand("printer");
    printerSetupBtn.setHorizontalAlignment(SwingConstants.LEFT);

    final JButton pageBtn = new JButton(mLocalizer.ellipsisMsg("page", "Page"),
        PrintPlugin.getInstance().createImageIcon("actions", "document-properties", 16));
    pageBtn.addActionListener(event -> {
      mPageFormat = mPrinterJob.pageDialog(mPageFormat);
      mPageFormat = mPrinterJob.validatePage(mPageFormat);
    });
    pageBtn.setActionCommand("page");
    pageBtn.setHorizontalAlignment(SwingConstants.LEFT);

    final JButton previewBtn = new JButton(mLocalizer.ellipsisMsg("preview", "Preview"),
        PrintPlugin.getInstance().createImageIcon("actions", "document-print-preview", 16));
    previewBtn.addActionListener(event -> {
      final printplugin.printer.PrintJob job = getPrintJob();
      if (job.getNumOfPages() < 1) {
        JOptionPane.showMessageDialog(mParentFrame, mLocalizer.msg("noPagesToPrint", "No pages available."));
      } else {
        final PreviewDlg dlg = new PreviewDlg(mParentFrame, job);
        PrintPlugin.getInstance().layoutWindow("previewDlg", dlg);
        dlg.setVisible(true);
      }
    });
    previewBtn.setActionCommand("preview");
    previewBtn.setHorizontalAlignment(SwingConstants.LEFT);

    return new ButtonStackBuilder().addButton(printerSetupBtn, pageBtn, previewBtn).build();
  }

  private JPanel createButtonBar() {

    final JButton printBt = new JButton(mLocalizer.msg("print", "Print"));
    printBt.addActionListener(e -> {
      print();
    });
    printBt.setActionCommand("print");
    printBt.setDefaultCapable(true);

    final JButton cancelBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancelBt.addActionListener(e -> close());
    cancelBt.setActionCommand("cancel");

    getRootPane().setDefaultButton(printBt);

    return createSchemePanel().addGlue().addButton(printBt, cancelBt).build();
  }

  private void print() {
    mProgressMonitorExtended = Plugin.getPluginManager().createProgressMonitor();
    mProgressMonitorExtended.setMessage(mLocalizer.msg("print", "Print"));
    mProgressMonitorExtended.setIndeterminate(true);
    mProgressMonitorExtended.setVisible(true);
    new Thread(() -> {
      try {
        mDialogContent.storeSchemes(getSchemes());
        final PrintJob job = getPrintJob();
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

  /**
   * @param buttonBarBuilder
   * @param parent
   * @param mSchemes
   * @return {@link ButtonBarBuilder}
   */
  @SuppressWarnings("unchecked")
  private ButtonBarBuilder createSchemePanel() {

    final JButton newSchemeBtn = new JButton(TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    newSchemeBtn.addActionListener(e -> {
      final String newSchemeName = JOptionPane.showInputDialog(mParentFrame,
          mLocalizer.msg("enterNewSchemeName", "Enter new name for scheme:"), mLocalizer.msg("newScheme", "New scheme"),
          JOptionPane.PLAIN_MESSAGE);
      if (newSchemeName != null) {
        if (newSchemeName.trim().length() > 0) {
          final Scheme<S> newScheme = mDialogContent.createNewScheme(newSchemeName);
          newScheme.setSettings(mDialogContent.getSettings());
          mSchemeCBModel.addElement(newScheme);
          mSchemeCB.setSelectedItem(newScheme);
          mDialogContent.setSettings(newScheme.getSettings());
          mDialogContent.storeSchemes(getSchemes());
        } else {
          JOptionPane.showMessageDialog(mParentFrame, mLocalizer.msg("invalidSchemeMsg", "Invalid scheme name"),
              mLocalizer.msg("invalidInput", "Invalid input"), JOptionPane.INFORMATION_MESSAGE);
        }
      }
    });
    newSchemeBtn.setActionCommand("new");
    newSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    newSchemeBtn.setToolTipText(mLocalizer.msg("newScheme", "New scheme"));

    final JButton mEditSchemeBtn = new JButton(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
    mEditSchemeBtn.addActionListener(e -> {
      final Scheme<S> scheme = (Scheme<S>) mSchemeCB.getSelectedItem();
      final Object newSchemeName = JOptionPane.showInputDialog(mParentFrame,
          mLocalizer.msg("enterNewSchemeName", "Enter new name for scheme:"),
          mLocalizer.msg("editScheme", "Edit scheme"), JOptionPane.PLAIN_MESSAGE, null, null, scheme.getName());
      if (newSchemeName != null) {
        if (newSchemeName.toString().trim().length() > 0) {
          scheme.setName(newSchemeName.toString());
          mSchemeCB.repaint();
          mDialogContent.storeSchemes(getSchemes());
        } else {
          JOptionPane.showMessageDialog(mParentFrame, mLocalizer.msg("invalidSchemeMsg", "Invalid scheme name"),
              mLocalizer.msg("invalidInput", "Invalid input"), JOptionPane.INFORMATION_MESSAGE);
        }
      }
    });
    mEditSchemeBtn.setActionCommand("edit");
    mEditSchemeBtn.setEnabled(false);
    mEditSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    mEditSchemeBtn.setToolTipText(mLocalizer.msg("editScheme", "Edit scheme"));

    final JButton mDeleteSchemeBtn = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mDeleteSchemeBtn.addActionListener(event -> {
      final Scheme<S> scheme = (Scheme<S>) mSchemeCB.getSelectedItem();
      if (scheme != null) {
        if (JOptionPane.showConfirmDialog(mParentFrame,
            mLocalizer.msg("deleteSchemeMsg", "Do you want to delete the selected scheme?"),
            mLocalizer.msg("deleteScheme", "Delete Scheme"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
          mSchemeCBModel.removeElement(scheme);
          mSchemeCB.setSelectedIndex(0);
          mDialogContent.storeSchemes(getSchemes());
        }
      }
    });
    mDeleteSchemeBtn.setActionCommand("delete");
    mDeleteSchemeBtn.setEnabled(false);
    mDeleteSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);
    mDeleteSchemeBtn.setToolTipText(mLocalizer.msg("deleteScheme", "Delete scheme"));

    final JButton mSaveSchemeBtn = new JButton(
        PrintPlugin.getInstance().createImageIcon("actions", "document-save", 16));
    mSaveSchemeBtn.addActionListener(e -> {
      final Scheme<S> scheme = (Scheme<S>) mSchemeCB.getSelectedItem();
      scheme.setSettings(mDialogContent.getSettings());
      mDialogContent.storeSchemes(getSchemes());
    });
    mSaveSchemeBtn.setActionCommand("save");
    mSaveSchemeBtn.setToolTipText(mLocalizer.msg("saveScheme", "Save scheme"));
    mSaveSchemeBtn.setMargin(UiUtilities.ZERO_INSETS);

    mSchemeCBModel = new DefaultComboBoxModel<>(mSchemes);
    mSchemeCB = new JComboBox<>(mSchemeCBModel);
    mSchemeCB.addItemListener(e -> {
      mDeleteSchemeBtn.setEnabled(mSchemeCB.getSelectedIndex() != 0);
      mEditSchemeBtn.setEnabled(mSchemeCB.getSelectedIndex() != 0);
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

  @SuppressWarnings("unchecked")
  public Scheme<S>[] getSchemes() {
    final Scheme<S>[] result = new Scheme[mSchemeCBModel.getSize()];
    for (int i = 0; i < result.length; i++) {
      result[i] = mSchemeCBModel.getElementAt(i);
    }
    return result;
  }

  public PrintJob getPrintJob() {
    return mDialogContent.createPrintJob(mPageFormat);
  }

  @Override
  public void close() {
    setVisible(false);
  }
}