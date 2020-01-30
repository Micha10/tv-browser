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

import devplugin.SettingsTab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;

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
public class PrintPluginSettingsTab implements SettingsTab {

  /** The localizer for this class. */
  public static final Localizer mLocalizer = Localizer.getLocalizerFor(PrintPluginSettingsTab.class);

  private final PrintPlugin mPrintPlugin;

  private DefaultMarkingPrioritySelectionPanel mMarkingsPanel;

  public PrintPluginSettingsTab(final PrintPlugin printPlugin) {
    mPrintPlugin = printPlugin;
  }

  @Override
  public JPanel createSettingsPanel() {

    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab(mLocalizer.msg("markings", "Markings"), mMarkingsPanel = createMarkingsTab());
    Utils.setOpaque(tabbedPane, false);

    final JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 0));
    panel.add(tabbedPane, BorderLayout.CENTER);
    return panel;
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
  public void saveSettings() {
    mPrintPlugin.setMarkPriority(mMarkingsPanel.getSelectedPriority());
  }

  private static DefaultMarkingPrioritySelectionPanel createMarkingsTab() {

    final Font font = UIManager.getFont("Label.font");
    final Color foreground = UIManager.getColor("Label.foreground");

    String text = Localizer.getLocalizerFor(DefaultMarkingPrioritySelectionPanel.class).msg("help",
        "The selected higlighting color is only shown if the program is higlighted by this plugin only "
            + "or if the other higlightings have a lower or the same priority. The higlighting colors of "
            + "the priorities can be changed in the <a href=\"#link\">higlighting settings</a>.");
    if (text.indexOf("<html>") >= 0) {
      text = StringUtils.substringBetween(text, "<html>", "</html>");
    }

    text = "<html><div style=\"color:" + UiUtilities.getHTMLColorCode(foreground)
        + ";font-family:" + font.getName() + "; font-size:" + font.getSize() + ";\">" + text + "</div></html>";

    final DefaultMarkingPrioritySelectionPanel defaultMarkingPrioritySelectionPanel = DefaultMarkingPrioritySelectionPanel
        .createPanel(PrintPlugin.getInstance().getMarkPriorityForProgram(null), false, true);

    final JEditorPane editorPane = Utils.findFirst(JEditorPane.class, defaultMarkingPrioritySelectionPanel);
    editorPane.setBackground(null);
    editorPane.setFont(font);
    editorPane.setForeground(foreground);
    editorPane.setOpaque(false);
    editorPane.setText(text);

    return defaultMarkingPrioritySelectionPanel;
  }
}