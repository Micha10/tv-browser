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

package printplugin.dlgs;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import printplugin.PrintPlugin;
import printplugin.dlgs.components.PreviewComponent;
import printplugin.printer.PrintJob;
import printplugin.util.Utils;

import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

@SuppressWarnings("nls")
public final class PreviewDlg extends JDialog implements ActionListener, PropertyChangeListener, WindowClosingIf {

  private static final long serialVersionUID = 7038525175772100332L;

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PreviewDlg.class);

  private Printable mPrinter;
  private PageFormat mPageFormat;

  private JButton mNextBt;
  private JButton mPrevBt;
  private JButton mZoomIn;
  private JButton mZoomOut;
  private JLabel mSiteLb;
  private PreviewComponent mPreviewComponent;

  public PreviewDlg(Window parent, PrintJob job) {
    super(parent);
    setModal(true);

    mPrinter = job.getPrintable();
    mPageFormat = job.getPageFormat();

    UiUtilities.registerForClosing(this);
    createGui(mPrinter, mPageFormat, job.getNumOfPages());
    updateDialogState();
    updateTitle();
    pack();
    readAndApplyDialogSettings();

    mZoomIn.setEnabled(!mPreviewComponent.maxZoom());
    mZoomOut.setEnabled(!mPreviewComponent.minZoom());

    getRootPane().getDefaultButton().requestFocus();
  }

  private void createGui(Printable printer, PageFormat pageFormat, int numberOfPages) {

    mPreviewComponent = new PreviewComponent(mPrinter, mPageFormat, numberOfPages);
    mPreviewComponent.addPropertyChangeListener("preferredSize", this);

    final JScrollPane scrollPane = new JScrollPane(mPreviewComponent);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
    scrollPane.getVerticalScrollBar().setUnitIncrement(20);
    scrollPane.getViewport().setBackground(Color.DARK_GRAY);

    final JPanel content = (JPanel) getContentPane();
    content.setBorder(Borders.DIALOG);
    content.setLayout(new FormLayout("fill:default:grow", "pref, 5dlu, fill:default:grow, 3dlu, pref, 5dlu, pref"));
    content.add(createToolBar(), CC.xy(1, 1));
    content.add(scrollPane, CC.xy(1, 3));
    if (mPreviewComponent.getNumberOfPages() > 1) {
      content.add(createPageNavigationBar(), CC.xy(1, 5));
    }
    content.add(createButtonBar(), CC.xy(1, 7));
  }

  private void readAndApplyDialogSettings() {
    final Properties prop = PrintPlugin.getInstance().getPluginSettings().storeSettings();
    try {
      if (prop.getProperty("PreviewDlg.Width") != null && prop.getProperty("PreviewDlg.Height") != null) {
        int width = Integer.parseInt(prop.getProperty("PreviewDlg.Width"));
        int height = Integer.parseInt(prop.getProperty("PreviewDlg.Height"));
        setSize(width, height);
      }
      if (prop.getProperty("PreviewDlg.X") != null && prop.getProperty("PreviewDlg.Y") != null) {
        int x = Integer.parseInt(prop.getProperty("PreviewDlg.X"));
        int y = Integer.parseInt(prop.getProperty("PreviewDlg.Y"));
        setLocation(x, y);
      } else {
        setLocationRelativeTo(getParent());
      }
      if (prop.getProperty("PreviewDlg.Zoom") != null) {
        double zoom = Double.parseDouble(prop.getProperty("PreviewDlg.Zoom"));
        mPreviewComponent.setZoom(zoom);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private JPanel createToolBar() {

    mZoomIn = new JButton(TVBrowserIcons.zoomIn(TVBrowserIcons.SIZE_SMALL));
    mZoomIn.addActionListener(this);
    mZoomIn.setActionCommand("zoomIn");
    mZoomIn.setToolTipText(mLocalizer.msg("zoomIn", "Zoom in"));

    mZoomOut = new JButton(TVBrowserIcons.zoomOut(TVBrowserIcons.SIZE_SMALL));
    mZoomOut.addActionListener(this);
    mZoomOut.setActionCommand("zoomOut");
    mZoomOut.setToolTipText(mLocalizer.msg("zoomOut", "Zoom out"));

    final JPanel panel = new JPanel(new FormLayout("pref, 3dlu, pref", "pref"));
    panel.add(mZoomOut, CC.xy(1, 1));
    panel.add(mZoomIn, CC.xy(3, 1));
    return panel;
  }

  private JPanel createButtonBar() {
    final JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    close.addActionListener(this);
    close.setActionCommand("close");
    close.setDefaultCapable(true);
    getRootPane().setDefaultButton(close);
    return new ButtonBarBuilder().addGlue().addButton(close).build();
  }

  private JPanel createPageNavigationBar() {

    mPrevBt = new JButton(TVBrowserIcons.left(TVBrowserIcons.SIZE_SMALL));
    mPrevBt.addActionListener(this);
    mPrevBt.setActionCommand("previous");
    mPrevBt.setToolTipText(mLocalizer.msg("previous", "Previous page"));

    mSiteLb = new JLabel();
    mSiteLb.setHorizontalAlignment(SwingConstants.CENTER);

    mNextBt = new JButton(TVBrowserIcons.right(TVBrowserIcons.SIZE_SMALL));
    mNextBt.addActionListener(this);
    mNextBt.setActionCommand("next");
    mNextBt.setToolTipText(mLocalizer.msg("next", "Next page"));

    final JPanel southPn = new JPanel(new FormLayout("left:10dlu:grow, pref, right:10dlu:grow", "pref"));
    southPn.add(mPrevBt, CC.xy(1, 1));
    southPn.add(mSiteLb, CC.xy(2, 1));
    southPn.add(mNextBt, CC.xy(3, 1));
    return southPn;
  }

  @SuppressWarnings("boxing")
  private void updateDialogState() {
    final int numberOfPages = mPreviewComponent.getNumberOfPages();
    if (numberOfPages > 1) {
      mSiteLb.setText(mLocalizer.msg("pageInfo", "Page {0} of {1}",
          mPreviewComponent.getPageIndex() + 1, numberOfPages));
      mPrevBt.setEnabled(mPreviewComponent.getPageIndex() > 0);
      mNextBt.setEnabled(mPreviewComponent.getPageIndex() + 1 < numberOfPages);
    }
  }

  @SuppressWarnings("boxing")
  private void updateTitle() {
    try {
      setTitle(String.format(Locale.getDefault(), "%s (%d x %d mm)",
          mLocalizer.msg("preview", "Preview"),
          Math.round(Utils.mm(mPageFormat.getWidth())),
          Math.round(Utils.mm(mPageFormat.getHeight()))));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void actionPerformed(final ActionEvent event) {
    switch (event.getActionCommand()) {
      case "close":
        close();
        break;
      case "next":
        mPreviewComponent.next();
        updateDialogState();
        break;
      case "previous":
        mPreviewComponent.previous();
        updateDialogState();
        break;
      case "zoomIn":
        mPreviewComponent.zoomIn();
        mZoomIn.setEnabled(!mPreviewComponent.maxZoom());
        mZoomOut.setEnabled(true);
        break;
      case "zoomOut":
        mPreviewComponent.zoomOut();
        mZoomOut.setEnabled(!mPreviewComponent.minZoom());
        mZoomIn.setEnabled(true);
        break;
    }
  }

  @Override
  public void close() {
    storeDialogSettings();
    dispose();
  }

  private void storeDialogSettings() {
    final Properties prop = PrintPlugin.getInstance().getPluginSettings().storeSettings();
    prop.setProperty("PreviewDlg.X", Integer.toString(getLocationOnScreen().x));
    prop.setProperty("PreviewDlg.Y", Integer.toString(getLocationOnScreen().y));
    prop.setProperty("PreviewDlg.Width", Integer.toString(getWidth()));
    prop.setProperty("PreviewDlg.Height", Integer.toString(getHeight()));
    prop.setProperty("PreviewDlg.Zoom", Double.toString(mPreviewComponent.getZoom()));
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    mZoomOut.setEnabled(!mPreviewComponent.minZoom());
    mZoomIn.setEnabled(!mPreviewComponent.maxZoom());
  }
}