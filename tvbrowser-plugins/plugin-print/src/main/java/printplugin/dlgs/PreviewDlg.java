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
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import printplugin.PrintPlugin;
import printplugin.dlgs.components.PreviewComponent;
import printplugin.printer.PrintJob;
import printplugin.util.BaseAction;
import printplugin.util.Utils;

import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

@SuppressWarnings("nls")
public final class PreviewDlg extends JDialog implements ActionListener, PropertyChangeListener, WindowClosingIf {

  private static final long serialVersionUID = 7038525175772100332L;

  private static final String PREVIEW_DLG_ZOOM = "PreviewDlg.Zoom";
  private static final String PREVIEW_DLG_Y = "PreviewDlg.Y";
  private static final String PREVIEW_DLG_X = "PreviewDlg.X";
  private static final String PREVIEW_DLG_HEIGHT = "PreviewDlg.Height";
  private static final String PREVIEW_DLG_WIDTH = "PreviewDlg.Width";

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PreviewDlg.class);

  private Printable mPrinter;
  private PageFormat mPageFormat;

  private JLabel mSiteLb;
  private PreviewComponent mPreviewComponent;

  private Action mCloseAction;
  private Action mNextAction;
  private Action mPreviousAction;
  private Action mZoomInAction;
  private Action mZoomOutAction;

  private boolean mAntialias;

  public PreviewDlg(final Window parent, final PrintJob job) {
    super(parent);
    setModal(true);

    mAntialias = PrintPlugin.settings().isAntialias();
    mPrinter = job.getPrintable();
    mPageFormat = job.getPageFormat();

    UiUtilities.registerForClosing(this);
    createActions();
    createGui(job.getNumOfPages());
    updateDialogState();
    updateTitle();
    pack();
    readAndApplyDialogSettings();

    mZoomInAction.setEnabled(!mPreviewComponent.maxZoom());
    mZoomOutAction.setEnabled(!mPreviewComponent.minZoom());

    getRootPane().getDefaultButton().requestFocus();
  }

  private void createGui(final int numberOfPages) {

    mPreviewComponent = new PreviewComponent(mPrinter, mPageFormat, numberOfPages);
    mPreviewComponent.addPropertyChangeListener("preferredSize", this);
    mPreviewComponent.setAntialias(mAntialias);

    final JPanel content = (JPanel) getContentPane();
    content.setBorder(Borders.DIALOG);
    content.setLayout(new FormLayout("fill:default:grow", "pref, 5dlu, fill:default:grow, 3dlu, pref, 5dlu, pref"));
    content.add(createToolBar(), CC.xy(1, 1));
    content.add(createScrollPane(), CC.xy(1, 3));
    if (numberOfPages > 1) {
      content.add(createPageNavigationBar(), CC.xy(1, 5));
    }
    content.add(createButtonBar(), CC.xy(1, 7));
  }

  private JScrollPane createScrollPane() {
    final JScrollPane scrollPane = new JScrollPane(mPreviewComponent);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
    scrollPane.getVerticalScrollBar().setUnitIncrement(20);
    scrollPane.getViewport().setBackground(Color.DARK_GRAY);

    JScrollBar horizontal = scrollPane.getHorizontalScrollBar();
    InputMap horizontalMap = horizontal.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    horizontalMap.put(KeyStroke.getKeyStroke("RIGHT"), "positiveUnitIncrement");
    horizontalMap.put(KeyStroke.getKeyStroke("LEFT"), "negativeUnitIncrement");

    JScrollBar vertical = scrollPane.getVerticalScrollBar();
    InputMap verticalMap = vertical.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    verticalMap.put(KeyStroke.getKeyStroke("DOWN"), "positiveUnitIncrement");
    verticalMap.put(KeyStroke.getKeyStroke("UP"), "negativeUnitIncrement");
    return scrollPane;
  }

  private void readAndApplyDialogSettings() {
    final Properties prop = PrintPlugin.settings().storeSettings();
    try {
      if (prop.getProperty(PREVIEW_DLG_WIDTH) != null && prop.getProperty(PREVIEW_DLG_HEIGHT) != null) {
        final int width = Integer.parseInt(prop.getProperty(PREVIEW_DLG_WIDTH));
        final int height = Integer.parseInt(prop.getProperty(PREVIEW_DLG_HEIGHT));
        setSize(width, height);
      }
      if (prop.getProperty(PREVIEW_DLG_X) != null && prop.getProperty(PREVIEW_DLG_Y) != null) {
        final int x = Integer.parseInt(prop.getProperty(PREVIEW_DLG_X));
        final int y = Integer.parseInt(prop.getProperty(PREVIEW_DLG_Y));
        setLocation(x, y);
      } else {
        setLocationRelativeTo(getParent());
      }
      if (prop.getProperty(PREVIEW_DLG_ZOOM) != null) {
        final double zoom = Double.parseDouble(prop.getProperty(PREVIEW_DLG_ZOOM));
        mPreviewComponent.setZoom(zoom);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private JPanel createToolBar() {
    return new ButtonBarBuilder()
        .addButton(new JButton(mZoomOutAction))
        .addRelatedGap()
        .addButton(new JButton(mZoomInAction))
        .addUnrelatedGap()
        .addGlue()
        // .addButton(new JButton(mFullscreenAction))
        .build();
  }

  private JPanel createButtonBar() {
    final JButton close = new JButton(mCloseAction);
    close.setDefaultCapable(true);
    getRootPane().setDefaultButton(close);
    return new ButtonBarBuilder().addGlue().addButton(close).build();
  }

  private JPanel createPageNavigationBar() {

    mSiteLb = new JLabel();
    mSiteLb.setHorizontalAlignment(SwingConstants.CENTER);

    return new ButtonBarBuilder()
        .addButton(new JButton(mPreviousAction))
        .addUnrelatedGap()
        .addGlue()
        .addFixed(mSiteLb)
        .addGlue()
        .addUnrelatedGap()
        .addButton(new JButton(mNextAction))
        .build();
  }

  @SuppressWarnings("boxing")
  private void updateDialogState() {
    final int numberOfPages = mPreviewComponent.getNumberOfPages();
    if (numberOfPages > 1) {
      mSiteLb.setText(mLocalizer.msg("pageInfo", "Page {0} of {1}",
          mPreviewComponent.getPageIndex() + 1, numberOfPages));
      mPreviousAction.setEnabled(mPreviewComponent.getPageIndex() > 0);
      mNextAction.setEnabled(mPreviewComponent.getPageIndex() + 1 < numberOfPages);
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

  @Override
  public void actionPerformed(final ActionEvent event) {
    switch (event.getActionCommand()) {
      case BaseAction.CLOSE:
        close();
        break;
      case "end":
        mPreviewComponent.setPageIndex(mPreviewComponent.getNumberOfPages() - 1);
        updateDialogState();
        break;
      case "home":
        mPreviewComponent.setPageIndex(0);
        updateDialogState();
        break;
      case "previous":
        mPreviewComponent.previous();
        updateDialogState();
        break;
      case "next":
        mPreviewComponent.next();
        updateDialogState();
        break;
      case "zoom100":
        mPreviewComponent.setZoom(1D);
        propertyChange(null);
        break;
      case "zoomIn":
        mPreviewComponent.zoomIn();
        propertyChange(null);
        break;
      case "zoomMax":
        mPreviewComponent.setZoom(PreviewComponent.MAX_ZOOM);
        propertyChange(null);
        break;
      case "zoomMin":
        mPreviewComponent.setZoom(PreviewComponent.MIN_ZOOM);
        propertyChange(null);
        break;
      case "zoomOut":
        mPreviewComponent.zoomOut();
        propertyChange(null);
        break;
      default:
        break;
    }
  }

  @Override
  public void close() {
    storeDialogSettings();
    dispose();
  }

  private void storeDialogSettings() {
    final Properties prop = PrintPlugin.settings().storeSettings();
    prop.setProperty(PREVIEW_DLG_X, Integer.toString(getLocationOnScreen().x));
    prop.setProperty(PREVIEW_DLG_Y, Integer.toString(getLocationOnScreen().y));
    prop.setProperty(PREVIEW_DLG_WIDTH, Integer.toString(getWidth()));
    prop.setProperty(PREVIEW_DLG_HEIGHT, Integer.toString(getHeight()));
    prop.setProperty(PREVIEW_DLG_ZOOM, Double.toString(mPreviewComponent.getZoom()));
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    mZoomInAction.setEnabled(!mPreviewComponent.maxZoom());
    mZoomOutAction.setEnabled(!mPreviewComponent.minZoom());
  }

  private void createActions() {

    final ActionMap actionMap = getRootPane().getActionMap();
    final InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

    mZoomOutAction = BaseAction.builder("zoomOut", this)
        .actionMap(actionMap)
        .bindKeys(inputMap, 0, KeyEvent.VK_MINUS, KeyEvent.VK_SUBTRACT)
        .icon(TVBrowserIcons.zoomOut(TVBrowserIcons.SIZE_SMALL))
        .tooltip(mLocalizer.msg("zoomOut", "Zoom out"))
        .build();

    mZoomInAction = BaseAction.builder("zoomIn", this)
        .actionMap(actionMap)
        .bindKeys(inputMap, 0, KeyEvent.VK_PLUS, KeyEvent.VK_ADD)
        .icon(TVBrowserIcons.zoomIn(TVBrowserIcons.SIZE_SMALL))
        .tooltip(mLocalizer.msg("zoomIn", "Zoom in"))
        .build();

    mPreviousAction = BaseAction.builder("previous", this)
        .actionMap(actionMap)
        .bindKeys(inputMap, 0, KeyEvent.VK_PAGE_UP)
        .icon(TVBrowserIcons.left(TVBrowserIcons.SIZE_SMALL))
        .tooltip(mLocalizer.msg("previous", "Previous page"))
        .build();

    mNextAction = BaseAction.builder("next", this)
        .actionMap(actionMap)
        .bindKeys(inputMap, 0, KeyEvent.VK_PAGE_DOWN)
        .icon(TVBrowserIcons.right(TVBrowserIcons.SIZE_SMALL))
        .tooltip(mLocalizer.msg("next", "Next page"))
        .build();

    mCloseAction = BaseAction.close(this).build();

    BaseAction.builder("end", this)
        .actionMap(actionMap)
        .bindKeys(inputMap, 0, KeyEvent.VK_END)
        .build();

    BaseAction.builder("home", this)
        .actionMap(actionMap)
        .bindKeys(inputMap, 0, KeyEvent.VK_HOME)
        .build();

    BaseAction.builder("zoom100", this)
        .actionMap(actionMap)
        .bindKeys(inputMap, 0, KeyEvent.VK_1, KeyEvent.VK_NUMPAD1)
        .build();

    BaseAction.builder("zoomMax", this)
        .actionMap(actionMap)
        .bindKeys(inputMap, KeyEvent.SHIFT_DOWN_MASK, KeyEvent.VK_PLUS, KeyEvent.VK_ADD)
        .build();

    BaseAction.builder("zoomMin", this)
        .actionMap(actionMap)
        .bindKeys(inputMap, KeyEvent.SHIFT_DOWN_MASK, KeyEvent.VK_MINUS, KeyEvent.VK_SUBTRACT)
        .build();
  }
}