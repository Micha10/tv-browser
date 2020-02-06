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

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.print.PrintService;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;

import printplugin.dlgs.components.FontChooserPanel;
import printplugin.settings.PageFormatType;
import printplugin.settings.PluginSettings;
import printplugin.util.BaseAction;
import printplugin.util.Utils;

import util.ui.DefaultMarkingPrioritySelectionPanel;
import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * The settings tab for the print plugin.
 *
 * @author René Mach
 */
@SuppressWarnings("nls")
public class PrintPluginSettingsTab implements ActionListener, ChangeListener, ItemListener, SettingsTab {

  /** The localizer for this class. */
  public static final Localizer mLocalizer = Localizer.getLocalizerFor(PrintPluginSettingsTab.class);

  private final PluginSettings mSettings;
  private final PrinterJob mPrinterJob;
  private final PrintPlugin mPrintPlugin;
  private final PrintService mSystemPrintService;

  private DefaultMarkingPrioritySelectionPanel mMarkingsPanel;

  private JCheckBox mPrintProgramInfoWithoutDialog;
  private JCheckBox mAntialias;
  private JCheckBox mDisplayFont;

  private FontChooserPanel mFontChooserPanel;
  private JComboBox<PrintService> mPrintServiceComboBox;
  private JComboBox<MediaSizeName> mPaperComboBox;
  private JComboBox<PageFormatType> mPageFormatTypeComboBox;

  private PageFormat mPageFormat;
  private PageFormatType mPageFormatType;
  private PrintService mPrintService;

  @SuppressWarnings("unused")
  private String error;

  private JCheckBox mPrintPageNumbers;
  private ButtonGroup mButtonGroup;
  private JComboBox<String> mShowImageableArea;
  private JCheckBox mIntegrateExtras;

  public PrintPluginSettingsTab(final PrintPlugin printPlugin) {

    mPrintPlugin = printPlugin;
    mSettings = PrintPlugin.settings();
    mPrinterJob = PrinterJob.getPrinterJob();
    mSystemPrintService = mPrinterJob.getPrintService();
    mPrintService = mSystemPrintService;
    final String printServiceName = mSettings.getPrintServiceName(null);
    final PrintService[] printServices = PrinterJob.lookupPrintServices();
    if (printServiceName != null && printServices != null) {
      for (final PrintService printService : printServices) {
        if (printService.getName().equals(printServiceName)
            && !printService.getName().equals(mSystemPrintService.getName())) {
          try {
            mPrinterJob.setPrintService(printService);
            mPrintService = printService;
          } catch (PrinterException e) {
          }
          break;
        }
      }
    }
    mPageFormatType = mSettings.getPageFormatType();
    mPageFormat = mPageFormatType.getPageFormat(mPrinterJob, mSettings);
  }

  @Override
  public JPanel createSettingsPanel() {

    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab(mLocalizer.msg("defaults", "Defaults"), createDefaultsTab());
    tabbedPane.addTab(mLocalizer.msg("appearance", "Appearance"), createAppearanceTab());
    tabbedPane.addTab(mLocalizer.msg("markings", "Markings"), mMarkingsPanel = createMarkingsTab());
    tabbedPane.addTab(mLocalizer.msg("changes", "Changes"), createChangesTab());
    tabbedPane.addTab(mLocalizer.msg("licenses", "Licenses"), createLicensesTab());
    Utils.setOpaque(tabbedPane, false);

    final JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 0));
    panel.add(tabbedPane, BorderLayout.CENTER);
    return panel;
  }

  private Component createLicensesTab() {
    final String language = "de".equals(Locale.getDefault().getLanguage()) ? "_de" : "";
    final JScrollPane pane = Utils
        .getHtmlPane(getClass().getResource("/printplugin/license" + language + ".html"), 10);
    final JPanel panel = new JPanel(new FormLayout("fill:min:grow", "fill:min:grow"));
    panel.add(pane, CC.xy(1, 1));
    return panel;
  }

  private Component createChangesTab() {
    final String language = ""; // "de".equals(Locale.getDefault().getLanguage()) ? "_de" : "";
    final JScrollPane pane = Utils
        .getHtmlPane(getClass().getResource("/printplugin/ChangeLog" + language + ".htm"), 10);
    final JPanel panel = new JPanel(new FormLayout("fill:min:grow", "fill:min:grow"));
    panel.add(pane, CC.xy(1, 1));
    return panel;
  }

  private Component createDefaultsTab() {
    createPageFormatComboBox();
    try {
      createPrintServiceComboBox();
    } catch (Exception e) {
      error = e.getLocalizedMessage();
      e.printStackTrace();
    }
    createPaperComboBox();

    final JPanel panel = new JPanel();
    panel.setBorder(Borders.DIALOG);
    panel.setLayout(new FormLayout(new ColumnSpec[] {
        FormSpecs.DEFAULT_COLSPEC,
        FormSpecs.RELATED_GAP_COLSPEC,
        FormSpecs.DEFAULT_COLSPEC,
        FormSpecs.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"),},
        new RowSpec[] {
            FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,

            FormSpecs.UNRELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,

            FormSpecs.UNRELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,

            FormSpecs.UNRELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,
        }));

    mFontChooserPanel = new FontChooserPanel(null, mSettings.getDefaultFont(), false);
    try {
      Utils.findFirst(JSpinner.class, mFontChooserPanel).setVisible(false);
    } catch (Exception e) {
    }

    mPrintPageNumbers = new JCheckBox(mLocalizer.msg("printPageNumbers", "Print page numbers"),
        mSettings.printPageNumbers());

    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("systemFont", "System font")),
        "1, 1, 5, 1");
    panel.add(mFontChooserPanel, "3, 3, 3, 1");

    panel.add(
        DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("systemPrinter", "System printer")),
        "1, 5, 5, 1");
    panel.add(mPrintServiceComboBox, "3, 7, 3, 1");

    panel.add(
        DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("pageMargins", "Page margins")),
        "1, 11, 5, 1");

    panel.add(mPageFormatTypeComboBox, "3, 13, 3, 1");
    // panel.add(mPaperComboBox, "3, 15, 3, 1");

    panel.add(
        DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("otherSettings", "Other settings")),
        "1, 17, 5, 1");
    panel.add(mPrintPageNumbers, "3, 19, 3, 1");

    return panel;
  }

  private JPanel createAppearanceTab() {

    final JPanel panel = new JPanel();
    panel.setBorder(Borders.DIALOG);
    panel.setLayout(new FormLayout(new ColumnSpec[] {
        FormSpecs.DEFAULT_COLSPEC,
        FormSpecs.RELATED_GAP_COLSPEC,
        FormSpecs.DEFAULT_COLSPEC,
        FormSpecs.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"),},
        new RowSpec[] {
            FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,

            FormSpecs.UNRELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,

            FormSpecs.UNRELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,

            FormSpecs.UNRELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,

            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC
        }));

    mPrintProgramInfoWithoutDialog = new JCheckBox(
        mLocalizer.msg("printProgramInfoWithoutDialog", "Add \"Print program info (without dialog)\" to context menu"),
        mSettings.showProgramInfoDirectPrintingMenuItem());
    mPrintProgramInfoWithoutDialog.setEnabled(false);

    mAntialias = new JCheckBox(mLocalizer.msg("antialiasing", "Antialiasing"), mSettings.isAntialias());
    mIntegrateExtras = new JCheckBox(mLocalizer.msg("integrateExtras", "Integrate Extras with Layout settings"),
        mSettings.integrateExtras());
    mDisplayFont = new JCheckBox(mLocalizer.msg("previewFonts", "Preview fonts"), mSettings.isDisplayFont());
    mDisplayFont.addChangeListener(this);

    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("contextmenu", "Context Menu")),
        "1, 1, 5, 1");
    panel.add(mPrintProgramInfoWithoutDialog, "3, 3, 3, 1");

    panel.add(
        DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("preview", "Preview")),
        "1, 5, 5, 1");
    panel.add(mAntialias, "3, 7, 3, 1");

    panel.add(DefaultComponentFactory.getInstance()
        .createReadOnlyLabel(mLocalizer.msg("showImageableArea", "Show imageable area")), "3, 9, 1, 1");
    panel.add(createImageableAreaSettings(), "5, 9, 1, 1");

    panel.add(
        DefaultComponentFactory.getInstance()
            .createSeparator(mLocalizer.msg("toolbarPos", "Placement of printer actions")),
        "1, 11, 5, 1");
    panel.add(createToolbarSettings(), "3, 13, 3, 1");

    panel.add(
        DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("otherSettings", "Other settings")),
        "1, 15, 5, 1");
    panel.add(mDisplayFont, "3, 17, 3, 1");
    panel.add(mIntegrateExtras, "3, 19, 3, 1");

    return panel;
  }

  private Component createImageableAreaSettings() {
    mShowImageableArea = new JComboBox<>(new String[] {
        mLocalizer.msg("mouseOver", "On mouse over"),
        mLocalizer.msg("ever", "Always"),
        mLocalizer.msg("never", "Never")
    });
    mShowImageableArea.setSelectedIndex(mSettings.showImageableArea());
    return mShowImageableArea;
  }

  private Component createToolbarSettings() {
    final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
    final String toolbarPos = mSettings.toolbarPosition();
    final JRadioButton top = new JRadioButton(BaseAction
        .builder(BorderLayout.PAGE_START, this)
        .text(mLocalizer.msg("top", "Top"))
        .build());
    final JRadioButton left = new JRadioButton(BaseAction
        .builder(BorderLayout.LINE_START, this)
        .text(mLocalizer.msg("left", "Left"))
        .build());
    final JRadioButton right = new JRadioButton(BaseAction
        .builder(BorderLayout.LINE_END, this)
        .text(mLocalizer.msg("right", "Right"))
        .build());
    mButtonGroup = new ButtonGroup();
    mButtonGroup.add(top);
    mButtonGroup.add(left);
    mButtonGroup.add(right);
    for (Enumeration<AbstractButton> e = mButtonGroup.getElements(); e.hasMoreElements();) {
      final AbstractButton button = e.nextElement();
      if (button.getActionCommand().equals(toolbarPos)) {
        button.setSelected(true);
        break;
      }
    }
    panel.add(top);
    panel.add(left);
    panel.add(right);
    return panel;
  }

  private static DefaultMarkingPrioritySelectionPanel createMarkingsTab() {

    final Color foreground = UIManager.getColor("Label.foreground");
    final Font font = UIManager.getFont("Label.font");

    String text = Localizer.getLocalizerFor(DefaultMarkingPrioritySelectionPanel.class).msg("help",
        "The selected higlighting color is only shown if the program is higlighted by this plugin only "
            + "or if the other higlightings have a lower or the same priority. The higlighting colors of "
            + "the priorities can be changed in the <a href=\"#link\">higlighting settings</a>.");
    if (text.indexOf("<html>") >= 0) {
      text = StringUtils.substringBetween(text, "<html>", "</html>");
    }

    text = "<html><body><div style=\"color:" + UiUtilities.getHTMLColorCode(foreground)
        + ";font-family:" + font.getName() + "; font-size:" + font.getSize() + ";\">" + text + "</div></body></html>";

    final DefaultMarkingPrioritySelectionPanel defaultMarkingPrioritySelectionPanel = DefaultMarkingPrioritySelectionPanel
        .createPanel(PrintPlugin.instance().getMarkPriorityForProgram(null), false, true);

    final JEditorPane editorPane = Utils.findFirst(JEditorPane.class, defaultMarkingPrioritySelectionPanel);
    editorPane.setBackground(null);
    editorPane.setFont(font);
    editorPane.setForeground(foreground);
    editorPane.setOpaque(false);
    editorPane.setText(text);

    return defaultMarkingPrioritySelectionPanel;
  }

  private void createPageFormatComboBox() {
    final PageFormatType[] pageFormatTypeArray = PageFormatType.values();
    final List<PageFormatType> pageFormatTypes = new ArrayList<>(pageFormatTypeArray.length - 1);
    for (final PageFormatType pageFormatType : pageFormatTypeArray) {
      if (PageFormatType.CUSTOM.equals(pageFormatType)) {
        continue;
      }
      pageFormatTypes.add(pageFormatType);
    }
    mPageFormatTypeComboBox = new JComboBox<>(pageFormatTypes.toArray(new PageFormatType[pageFormatTypes.size()]));
    mPageFormatTypeComboBox.setRenderer(new DefaultListCellRenderer() {

      private static final long serialVersionUID = -5788817565534405037L;

      @SuppressWarnings("boxing")
      @Override
      public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
          final boolean isSelected, final boolean cellHasFocus) {
        final PageFormatType pageFormatType = (PageFormatType) value;
        final PageFormat pageFormat = pageFormatType.getPageFormat(mPrinterJob, mSettings);
        final String text = String.format(Locale.getDefault(), "%s (%1.2f mm)",
            mLocalizer.msg(pageFormatType.name(), pageFormatType.name()),
            Utils.mm(pageFormat.getImageableX()));
        final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setText(text);
        return label;
      }
    });
    mPageFormatTypeComboBox.addItemListener(this);
    mPageFormatTypeComboBox.setSelectedItem(mSettings.getPageFormatType());
  }

  private void createPaperComboBox() {

    mPaperComboBox = new JComboBox<>();
    setPrintMediaForPrintService();
    mPaperComboBox.setRenderer(new DefaultListCellRenderer() {

      private static final long serialVersionUID = -5788817565534405037L;

      @Override
      public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
          final boolean isSelected, final boolean cellHasFocus) {
        final MediaSizeName mediaSizeName = (MediaSizeName) value;
        final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setText(toDisplayName(mediaSizeName));
        return label;
      }

      private String toDisplayName(final MediaSizeName mediaSizeName) {
        String name = mediaSizeName.toString().replace("iso-", "ISO-").replace("jis-", "JIS-").replace("din-", "DIN-");
        if (name.startsWith("ISO-") && Character.isDigit(name.charAt(5))) {
          name = name.substring(4);
        } else if (name.startsWith("na-")) {
          name = name.substring(3);
        } else if ("ISO-designated-long".equals(name)) {
          name = "Designated Long-Envelope";
        }

        if ("de".equals(getLocale().getLanguage())) {
          name = name.replace("Designated Long", "DL").replace("Envelope", "Umschlag").replace("envelope", "Umschlag")
              .replace("Number", "Nummer").replace("german-", "Deutsch. ").replace("German", "Deutsch.")
              .replace("number", "Nummer").replace("italian-", "Ital. ").replace("indian-", "Ind. ")
              .replace("japanese-", "Japan. ").replace("postcard", "Postkarte");
        }

        name = Character.toTitleCase(name.charAt(0)) + name.substring(1);
        return name;
      }
    });
    mPaperComboBox.addItemListener(this);
    Paper paper = mPageFormat.getPaper();
    MediaSizeName name = MediaSize.findMedia((float) paper.getWidth(), (float) paper.getHeight(), MediaSize.MM);
    mPaperComboBox.setSelectedItem(name);
  }

  private void setPrintMediaForPrintService() {
    final Media[] media = (Media[]) mPrintService.getSupportedAttributeValues(Media.class, null,
        null);
    final List<Media> mediaList = new ArrayList<>(media.length);
    for (final Media medium : media) {
      if (medium instanceof MediaSizeName) {
        mediaList.add(medium);
      }
    }
    mPaperComboBox.setModel(new DefaultComboBoxModel<>(mediaList.toArray(new MediaSizeName[mediaList.size()])));
  }

  private void createPrintServiceComboBox() {

    final PrintService[] printServices = PrinterJob.lookupPrintServices();
    Arrays.sort(printServices, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
    mPrintServiceComboBox = new JComboBox<>(printServices);
    mPrintServiceComboBox.setRenderer(new DefaultListCellRenderer() {

      private static final long serialVersionUID = -431923003940323582L;

      @Override
      public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
          final boolean isSelected, final boolean cellHasFocus) {
        final PrintService printService = (PrintService) value;
        final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (mSystemPrintService != null && printService.getName().equalsIgnoreCase(mSystemPrintService.getName())) {
          label.setFont(label.getFont().deriveFont(Font.BOLD));
        }
        label.setText(printService.getName());
        return label;
      }
    });
    final String printServiceName = mSettings.getPrintServiceName(mPrinterJob);
    for (int i = 0; i < mPrintServiceComboBox.getModel().getSize(); i++) {
      final PrintService printService = mPrintServiceComboBox.getModel().getElementAt(i);
      if (printService.getName().equalsIgnoreCase(printServiceName)) {
        mPrintServiceComboBox.setSelectedItem(printService);
        break;
      }
    }
    mPrintServiceComboBox.addItemListener(this);
  }

  @Override
  public void stateChanged(final ChangeEvent e) {
    if (e.getSource() == mDisplayFont) {
      mFontChooserPanel.setDisplayFonts(mDisplayFont.isSelected());
    }
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      if (e.getSource().equals(mPageFormatTypeComboBox)) {
        handlePageFormatTypeChanged((PageFormatType) e.getItem());
      } else if (e.getSource() == mPrintServiceComboBox) {
        handlePrintServiceChanged((PrintService) e.getItem());
      }
    }
  }

  private void handlePrintServiceChanged(final PrintService item) {
    try {
      mPrintService = item;
      setPrintMediaForPrintService();
      mPrinterJob.setPrintService(mPrintService);
      handlePageFormatTypeChanged(mPageFormatType);
    } catch (PrinterException e) {
      e.printStackTrace();
    }
  }

  private void handlePageFormatTypeChanged(final PageFormatType item) {
    mPageFormatType = item;
    mPageFormat = mPageFormatType.getPageFormat(mPrinterJob, mPageFormat.getPaper(), mPageFormat.getOrientation());
  }

  @Override
  public void saveSettings() {
    mPrintPlugin.setMarkPriority(mMarkingsPanel.getSelectedPriority());

    mSettings.setAntialias(mAntialias.isSelected());
    mSettings.setDisplayFont(mDisplayFont.isSelected());
    mSettings.setProgramInfoDirectPrintingMenuItem(mPrintProgramInfoWithoutDialog.isSelected());

    mSettings.setDefaultFontName(Utils.encodeFont(mFontChooserPanel.getChosenFont()));
    mSettings.setPageFormatType((PageFormatType) mPageFormatTypeComboBox.getSelectedItem());
    if (mPrintServiceComboBox != null) {
      mSettings.setPrintServiceName(((PrintService) mPrintServiceComboBox.getSelectedItem()).getName());
    }
    mSettings.printPageNumbers(mPrintPageNumbers.isSelected());
    mSettings.toolbarPosition(mButtonGroup.getSelection().getActionCommand());

    mSettings.showImageableArea(mShowImageableArea.getSelectedIndex());
    mSettings.integrateExtras(mIntegrateExtras.isSelected());
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getTitle() {
    return null;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {}
}