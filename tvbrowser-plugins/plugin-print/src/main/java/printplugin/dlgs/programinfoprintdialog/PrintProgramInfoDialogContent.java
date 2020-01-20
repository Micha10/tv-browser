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

package printplugin.dlgs.programinfoprintdialog;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.Program;
import devplugin.ProgramFieldType;

import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JTabbedPane;

import printplugin.PrintPlugin;
import printplugin.dlgs.DialogContent;
import printplugin.dlgs.components.FontChooserPanel;
import printplugin.printer.PrintJob;
import printplugin.printer.singleprogramprinter.DocumentRenderer;
import printplugin.settings.ProgramInfoPrintSettings;
import printplugin.settings.ProgramInfoScheme;
import printplugin.settings.Scheme;
import printplugin.util.Utils;

import util.program.ProgramTextCreator;
import util.program.ProgramTextCreator.Configuration;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.OrderChooser;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.ExtendedHTMLEditorKit;

@SuppressWarnings("nls")
public class PrintProgramInfoDialogContent implements DialogContent<ProgramInfoPrintSettings> {

  /** The localizer for this class. */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PrintProgramInfoDialogContent.class);

  private Program mProgram;

  private AbstractButton mPrintIcons;
  private AbstractButton mPrintImage;
  private FontChooserPanel mFontChooser;
  private OrderChooser<Object> mFieldChooser;

  public PrintProgramInfoDialogContent(Program program) {
    mProgram = program;
  }

  @Override
  public void printingDone() {}

  @Override
  public Component getContent(final Frame parentFrame) {

    final boolean hasImage = mProgram.hasFieldValue(ProgramFieldType.PICTURE_TYPE);
    boolean hasIcons = mProgram.getMarkerArr().length > 0;

    if (!hasIcons) {
      PluginAccess[] plugins = Plugin.getPluginManager().getActivatedPlugins();

      for (PluginAccess pluginAccess : plugins) {
        Icon[] ico = pluginAccess.getProgramTableIcons(mProgram);

        if (ico != null && ico.length > 0) {
          hasIcons = true;
          break;
        }
      }
    }

    final Font defaultFont = PrintPlugin.getInstance().getPluginSettings().getDefaultFont();

    mFieldChooser = new OrderChooser<>(
        ProgramTextCreator.getDefaultOrder(),
        ProgramTextCreator.getDefaultOrder(),
        true);
    mFontChooser = new FontChooserPanel("", defaultFont, false);
    mPrintImage = new JCheckBox(mLocalizer.msg("printImage", "Print image"), true);
    mPrintIcons = new JCheckBox(mLocalizer.msg("printPluginIcons", "Print plugin icons"), true);

    PanelBuilder b1 = new PanelBuilder(
        new FormLayout("5dlu,pref:grow", "pref,5dlu,pref,10dlu,pref,5dlu,fill:default:grow"
            + (hasIcons || hasImage ? ",3dlu" : "") + (hasIcons ? ",pref" : "") + (hasImage ? ",pref" : "")));
    b1.border(Borders.DIALOG);
    b1.addSeparator(mLocalizer.msg("font", "Font"), CC.xyw(1, 1, 2));
    b1.add(mFontChooser, CC.xyw(1, 3, 2));
    b1.addSeparator(mLocalizer.msg("order", "Info fields and order"), CC.xyw(1, 5, 2));
    b1.add(mFieldChooser, CC.xyw(1, 7, 2));

    if (hasIcons) {
      b1.add(mPrintIcons, CC.xy(2, 9));
    }

    if (hasImage) {
      if (hasIcons) {
        b1.add(mPrintImage, CC.xy(2, 10));
      } else {
        b1.add(mPrintImage, CC.xy(2, 9));
      }
    }

    JTabbedPane tab = new JTabbedPane();
    tab.add(mLocalizer.msg("layoutTab", "Layout"), b1.build());
    Utils.setOpaque(tab, false);
    return tab;
  }

  @Override
  public String getDialogTitle() {
    return mLocalizer.msg("title", "Print program info");
  }

  @Override
  public ProgramInfoPrintSettings getSettings() {
    final ProgramInfoPrintSettings programInfoPrintSettings = new ProgramInfoPrintSettings();
    programInfoPrintSettings.setFieldTypes(mFieldChooser.getOrderList().toArray());
    programInfoPrintSettings.setFont(mFontChooser.getChosenFont());
    programInfoPrintSettings.setPrintImage(mPrintImage.isSelected());
    programInfoPrintSettings.setPrintPluginIcons(mPrintIcons.isSelected());
    return programInfoPrintSettings;
  }

  @Override
  public void setSettings(ProgramInfoPrintSettings settings) {
    mFontChooser.selectFont(settings.getFont());
    mFieldChooser.setOrder(settings.getFieldTypes(), settings.getAllFieldTypes());
    mPrintImage.setSelected(settings.isPrintImage());
    mPrintIcons.setSelected(settings.isPrintPluginIcons());
  }

  @Override
  public PrintJob createPrintJob(final PageFormat format) {

    final JEditorPane pane = new JEditorPane();
    pane.setEditorKit(new ExtendedHTMLEditorKit());
    pane.setText(createProgramInfoText((ExtendedHTMLDocument) pane.getDocument()));

    final DocumentRenderer documentRenderer = new DocumentRenderer(format);
    documentRenderer.setEditorPane(pane);

    return new PrintJob() {

      @Override
      public Printable getPrintable() {
        return documentRenderer;
      }

      @Override
      public PageFormat getPageFormat() {
        return format;
      }

      @Override
      public int getNumOfPages() {
        return documentRenderer.getPageCount();
      }
    };
  }

  /**
   * @param doc
   *              {@link ExtendedHTMLDocument}
   * @return
   */
  private String createProgramInfoText(final ExtendedHTMLDocument doc) {

    final ProgramPanelSettings programPanelSettings = new ProgramPanelSettings(
        mPrintImage.isSelected() ? ProgramPanelSettings.SHOW_PICTURES_EVER
            : ProgramPanelSettings.SHOW_PICTURES_NEVER,
        -1, -1, false, true, 10);

    final Configuration configuration = new Configuration(mFieldChooser.getOrderList().toArray(), null,
        mFontChooser.getChosenFont(),
        programPanelSettings,
        false);
    configuration.setZoom(100);
    configuration.setShowPluginIcons(mPrintIcons.isSelected());
    configuration.setShowShortDescriptionOnlyIfNoDescription(true);
    configuration.setShowPersonLinks(true);

    return ProgramTextCreator.createInfoText(mProgram, doc, configuration);
  }

  @Override
  public Scheme<ProgramInfoPrintSettings> createNewScheme(String schemeName) {
    return new ProgramInfoScheme(schemeName);
  }

  @Override
  public void storeSchemes(final Scheme<ProgramInfoPrintSettings>[] schemes) {
    ProgramInfoScheme.storeSchemes(schemes);
  }

  @Override
  public Scheme<ProgramInfoPrintSettings>[] loadSchemes() {
    return ProgramInfoScheme.loadSchemes();
  }
}