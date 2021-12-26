/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ProgramFieldType;
import devplugin.ProgramInfoHelper;
import devplugin.SettingsTab;
import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.GenericFilterMap;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.icontheme.InfoIconTheme;
import tvbrowser.core.icontheme.InfoThemeLoader;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.filter.dlgs.EditFilterDlg;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.settings.ProgramFieldTypeArrayProperty;
import util.settings.StringArrayProperty;
import util.ui.EnhancedPanelBuilder;
import util.i18n.Localizer;
import util.ui.OrderChooser;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.customizableitems.SelectableItem;
import util.ui.customizableitems.SelectableItemRendererCenterComponentIf;

/**
 * A settings tab for the program panel.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ProgramPanelSettingsTab implements SettingsTab {

  public static final Localizer LOCALIZER = Localizer.getLocalizerFor(ProgramPanelSettingsTab.class);

  private static final String PICTURE_ICON_NAME = LOCALIZER.msg("hasPicure", "Has picture");

  private OrderChooser<IconPlugin> mIconPluginOCh;
  private OrderChooser<Object> mInfoTextOCh;

  private OrderChooser<IconPlugin> mIconPluginOChAlt;
  private OrderChooser<Object> mInfoTextOChAlt;

  private ColorLabel mProgramItemOnAirColorLb, mProgramItemProgressColorLb, mProgramItemKeyboardSelectedLb;

  private JCheckBox mAllowProgramImportance;
  private JCheckBox mBorderForOnAirPrograms;
  private JCheckBox mGradientHighlighting;
  
  private ArrayList<IconPlugin> mFormatIcons;

  private JCheckBox mHyphenator;
  private JCheckBox mShowOriginalTitles;
  
  private JCheckBox mSmootherScrolling;

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    EnhancedPanelBuilder panel = new EnhancedPanelBuilder("5dlu, fill:50dlu:grow, 3dlu, fill:50dlu:grow, 3dlu");
    panel.border(Borders.DIALOG);
    
    panel.addParagraph("");
    // icons
    panel.add(DefaultComponentFactory.getInstance()
        .createSeparator(LOCALIZER.msg("pluginIcons", "Plugin icons")), CC.xyw(1, panel.getRowCount(), 2));

    IconPlugin[] allPluginArr = getAvailableIconPlugins();
    IconPlugin[] pluginOrderArr = getSelectedIconPlugins(allPluginArr, Settings.ProgramPanel.ICON_PLUGINS );
    mIconPluginOCh = createIconPluginChooser(allPluginArr, pluginOrderArr);

    pluginOrderArr = getSelectedIconPlugins(allPluginArr, Settings.ProgramPanel.ICON_PLUGINS_ALTERNATIVE);
    mIconPluginOChAlt = createIconPluginChooser(allPluginArr, pluginOrderArr);
    
    // info text
    panel.add(DefaultComponentFactory.getInstance().createSeparator(LOCALIZER.msg("infoText", "Info text")), CC
        .xyw(4, panel.getRowCount(), 2));
    
    ProgramFieldType[] allTypeArr = getAvailableTypes();
    ProgramFieldType[] typeOrderArr = Settings.ProgramPanel.INFO_FIELDS.getProgramFieldTypeArray();
    String[] separators = Settings.ProgramPanel.INFO_FIELDS_SEPARATORS.getStringArray();
    
    mInfoTextOCh = new OrderChooser<>(typeOrderArr, allTypeArr);

    for(int i = separators.length-1; i >= 0; i--) {
      if(separators[i].replace(" ", "").length() > 0) {
        String[] splitSeparators = separators[i].split(";#;");
        
        for(String sep : splitSeparators) {
          if(!sep.equals(" - ")) {
            mInfoTextOCh.addElement(sep, i+1, true);
          }
        }
      }
    }
    
    typeOrderArr = Settings.ProgramPanel.INFO_FIELDS_ALTERNATIVE.getProgramFieldTypeArray();
    separators = Settings.ProgramPanel.INFO_FIELDS_SEPARATORS_ALTERNATIVE.getStringArray();
    
    mInfoTextOChAlt = new OrderChooser<>(typeOrderArr, allTypeArr);

    JButton addLineBreak = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "add-line-break", TVBrowserIcons.SIZE_LARGE));
    addLineBreak.setToolTipText(LOCALIZER.msg("addLineBreakTooltip", "Adds line break"));
    addLineBreak.addActionListener(e -> {
      mInfoTextOCh.addElement("\n",mInfoTextOCh.getSelectedIndex()+1,true);
    });
    
    mInfoTextOCh.addButton(addLineBreak);
    
    for(int i = separators.length-1; i >= 0; i--) {
      if(separators[i].replace(" ", "").length() > 0) {
        String[] splitSeparators = separators[i].split(";#;");
        
        for(String sep : splitSeparators) {
          if(!sep.equals(" - ")) {
            mInfoTextOChAlt.addElement(sep, i+1, true);
          }
        }
      }
    }
    
    mShowOriginalTitles = new JCheckBox(LOCALIZER.msg("showOriginalTitles", "Show original title, if available, instead of title"), Settings.ProgramPanel.ORIGINIAL_TITLES_SHOW.getBoolean());
    
    JPanel filterPanel = new JPanel(new FormLayout("default","default"));
    JButton editFilter = new JButton("Filter editieren...");
    editFilter.addActionListener(e -> {
      final UserFilter filter = GenericFilterMap.getInstance().getGenericInternalFilter(GenericFilterMap.GENERIC_PROGRAM_PANEL_FILTER_NAME);
      final EditFilterDlg editFilter1 = new EditFilterDlg(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), FilterList.getInstance(), filter, false);
      
      if(editFilter1.getOkWasPressed()) {
        GenericFilterMap.getInstance().updateGenericInternalFilter(GenericFilterMap.GENERIC_PROGRAM_PANEL_FILTER_NAME, filter);
      }
    });
    
    filterPanel.add(editFilter, CC.xy(1, 1));
    
    panel.addRow("top:default");
    panel.addRow("default");
    panel.add(UiUtilities.createHelpTextArea(LOCALIZER.msg("pluginIcons.description", "")), CC.xywh(2, panel.getRowCount()-2, 1, 3));
    panel.add(UiUtilities.createHelpTextArea(LOCALIZER.msg("infoText.description", "")), CC.xy(4, panel.getRowCount()-2));
    panel.add(mShowOriginalTitles, CC.xy(4, panel.getRowCount()));
    
    panel.addRow("default");
    panel.addSeparator("Standard", CC.xyw(2, panel.getRowCount(), 3));
    
    panel.addGrowingRow();
    panel.add(mIconPluginOCh, CC.xy(2, panel.getRowCount()));
    panel.add(mInfoTextOCh, CC.xy(4, panel.getRowCount()));
    
    panel.addRow("default");
    panel.addSeparator("Alternative Darstellung nach Filter", CC.xyw(2, panel.getRowCount(), 3));
    
    panel.addRow("default");
    panel.add(filterPanel, CC.xyw(2, panel.getRowCount(), 3));
    
    panel.addGrowingRow();
    panel.add(mIconPluginOChAlt, CC.xy(2, panel.getRowCount()));
    panel.add(mInfoTextOChAlt, CC.xy(4, panel.getRowCount()));
    
    panel.addParagraph(LOCALIZER.msg("Colors", "Colors"));

    panel.addRow();
    panel.add(mGradientHighlighting = new JCheckBox(LOCALIZER.msg("color.programGradientHighlighting",
        "Highlight programs with gradient colors"),Settings.ProgramPanel.HIGHLIGHTING_COLOR_GRADIENT.getBoolean()), CC.xyw(2, panel.getRowCount(), 3));

    panel.addRow();
    panel.add(mAllowProgramImportance = new JCheckBox(LOCALIZER.msg("color.allowTransparency","Allow plugins to set the transparency of a program"),
        Settings.ProgramPanel.TRANSPARENCY_ALLOW.getBoolean()), CC.xyw(2, panel.getRowCount() ,3));
    
    panel.addRow();
    panel.add(mBorderForOnAirPrograms = new JCheckBox(LOCALIZER.msg("color.programOnAirWithBorder",
        "Border for programs on air"), Settings.ProgramPanel.BORDER_ON_AIR_PROGRAMS_SHOW.getBoolean()), CC.xyw(2, panel.getRowCount(),
        3));
    
    JPanel colors = new JPanel();
    Color programItemProgressColor = Settings.ProgramPanel.COLOR_ON_AIR_DARK.getColor();
    Color programItemOnAirColor = Settings.ProgramPanel.COLOR_ON_AIR_LIGHT.getColor();
    Color programItemKeyboardSelectedColor = Settings.ProgramPanel.COLOR_KEYBOARD_SELECTED.getColor();

    Color programItemDefaultProgressColor = Settings.ProgramPanel.COLOR_ON_AIR_DARK.getDefaultColor();
    Color programItemDefaultOnAirColor = Settings.ProgramPanel.COLOR_ON_AIR_LIGHT.getDefaultColor();
    Color programItemDefaultKeyboardSelectedColor = Settings.ProgramPanel.COLOR_KEYBOARD_SELECTED.getDefaultColor();

    FormLayout formLayout = new FormLayout("default, 5dlu, default, 5dlu, default, 5dlu, default",
        "5dlu, default, 3dlu, default, 3dlu, default");
    colors.setLayout(formLayout);

    colors.add(new JLabel(LOCALIZER.msg("color.programOnAir", "Background color for programs on air")), CC.xy(1, 2));
    colors.add(mProgramItemOnAirColorLb = new ColorLabel(programItemOnAirColor), CC.xy(3, 2));
    mProgramItemOnAirColorLb.setStandardColor(programItemDefaultOnAirColor);
    colors.add(new ColorButton(mProgramItemOnAirColorLb), CC.xy(5, 2));

    colors.add(new JLabel(LOCALIZER.msg("color.programProgress", "Progress bar for programs on air")), CC.xy(
        1, 4));
    colors.add(mProgramItemProgressColorLb = new ColorLabel(programItemProgressColor), CC.xy(3, 4));
    mProgramItemProgressColorLb.setStandardColor(programItemDefaultProgressColor);
    colors.add(new ColorButton(mProgramItemProgressColorLb), CC.xy(5, 4));

    colors.add(new JLabel(LOCALIZER.msg("color.keyboardSelected", "Color for programs selected by keyboard")), CC.xy(1, 6));
    colors.add(mProgramItemKeyboardSelectedLb = new ColorLabel(programItemKeyboardSelectedColor), CC.xy(3, 6));
    mProgramItemKeyboardSelectedLb.setStandardColor(programItemDefaultKeyboardSelectedColor);
    colors.add(new ColorButton(mProgramItemKeyboardSelectedLb), CC.xy(5, 6));

    panel.addRow();
    panel.add(colors, CC.xyw(2, panel.getRowCount(), panel.getColumnCount() - 1));
    
    panel.addParagraph(LOCALIZER.msg("text", "Text"));
    panel.addRow();
    panel.add(mHyphenator = new JCheckBox(LOCALIZER.msg("hyphenation", "Use hyphenation"), Settings.ProgramPanel.HYPHENATION.getBoolean()), CC.xyw(2, panel.getRowCount(), panel.getColumnCount() - 1));
    panel.addParagraph(LOCALIZER.msg("scrolling", "Scrolling"));
    panel.addRow();
    panel.add(mSmootherScrolling = new JCheckBox(LOCALIZER.msg("scrolling.smoother", "Smoother scrolling in lists with programs"), Settings.ProgramPanel.SMOOTHER_SCROLLING.getBoolean()), CC.xyw(2, panel.getRowCount(), panel.getColumnCount() - 1));
    
    return panel.getPanel();
  }
  
  
  private OrderChooser<IconPlugin> createIconPluginChooser(final IconPlugin[] allPluginArr, final IconPlugin[] pluginOrderArr) {
    return new OrderChooser<>(pluginOrderArr, allPluginArr, IconPlugin.class,
        new SelectableItemRendererCenterComponentIf<IconPlugin>() {
          private DefaultListCellRenderer mRenderer = new DefaultListCellRenderer();
          
          @Override
          public JPanel createCenterPanel(JList<? extends SelectableItem<IconPlugin>> list, IconPlugin value, int index, boolean isSelected,
              boolean isEnabled, JScrollPane parentScrollPane, int leftColumnWidth) {
            DefaultListCellRenderer label = (DefaultListCellRenderer) mRenderer.getListCellRendererComponent(list,
                value, index, isSelected, false);
            IconPlugin iconPlugin = (IconPlugin) value;
            label.setIcon(iconPlugin.getIcon());
            label.setHorizontalAlignment(SwingConstants.LEADING);
            label.setVerticalAlignment(SwingConstants.CENTER);
            label.setOpaque(false);

            JPanel panel = new JPanel(new BorderLayout());
            if (isSelected && isEnabled) {
              panel.setOpaque(true);
              panel.setForeground(list.getSelectionForeground());
              panel.setBackground(list.getSelectionBackground());
            } else {
              panel.setOpaque(false);
              panel.setForeground(list.getForeground());
              panel.setBackground(list.getBackground());
            }
            panel.add(label, BorderLayout.WEST);
            return panel;
          }

          @Override
          public void calculateSize(JList<? extends SelectableItem<IconPlugin>> list, int index, JPanel contentPane) {}
        });
  }

  private IconPlugin[] getAvailableIconPlugins() {
    final ArrayList<IconPlugin> list = new ArrayList<IconPlugin>();

    list.addAll(getFormatIconNames());
    list.add(new IconPlugin(PICTURE_ICON_NAME, InfoThemeLoader.getInstance().getIconThemeForIDOrDefault(Settings.LookAndFeel.INFO_ICON_THEME_ID.getString()).getInfoIcon(InfoIconTheme.INFO_HAS_PICTURE)));

    for (PluginProxy plugin : PluginProxyManager.getInstance().getActivatedPlugins()) {
      final String iconText = plugin.getProgramTableIconText();
      if (iconText != null) {
        Icon[] icons = plugin.getProgramTableIcons(PluginManagerImpl.getInstance().getExampleProgram());
        Icon icon;
        if (icons != null && icons.length > 0) {
          icon = icons[0];
        }
        else {
          icon = null;
        }
        list.add(new IconPlugin(plugin, icon));
      }
    }

    return list.toArray(new IconPlugin[list.size()]);
  }

  private IconPlugin[] getSelectedIconPlugins(final IconPlugin[] allArr, final StringArrayProperty prop) {
    final String[] selPluginArr = prop.getStringArray();
    final ArrayList<IconPlugin> list = new ArrayList<IconPlugin>();

    for (String selectedPluginId : selPluginArr) {
      for (IconPlugin iconPlugin : allArr) {
        final String pluginId = iconPlugin.getId();
        if (selectedPluginId.equals(pluginId)) {
          list.add(iconPlugin);
          break;
        }
      }
      if (selectedPluginId.equals(Settings.INFO_ID)) {
        list.addAll(getFormatIconNames());
      }
    }

    return list.toArray(new IconPlugin[list.size()]);
  }

  private List<IconPlugin> getFormatIconNames() {
    if (mFormatIcons == null) {
      mFormatIcons = new ArrayList<IconPlugin>();
      //String[] iconFilenames = ProgramInfoHelper.getInfoIconFilenames();
      Icon[] infoIcons = ProgramInfoHelper.getInfoIcons();
      String[] infoMessages = ProgramInfoHelper.getInfoIconMessages();
      for (int i = 0; i < infoIcons.length; i++) {
        if (infoIcons[i] != null) {
          mFormatIcons.add(new IconPlugin(LOCALIZER.msg("formatIcon", "Format: {0}", infoMessages[i]), infoIcons[i]));
        }
      }
    }
    return mFormatIcons;
  }

  private ProgramFieldType[] getAvailableTypes() {
    ArrayList<ProgramFieldType> typeList = new ArrayList<ProgramFieldType>();

    Iterator<ProgramFieldType> typeIter = ProgramFieldType.getTypeIterator();
    while (typeIter.hasNext()) {
      ProgramFieldType type = typeIter.next();

      if ((type.getFormat() != ProgramFieldType.FORMAT_BINARY) && (type != ProgramFieldType.INFO_TYPE)
          && (type != ProgramFieldType.PICTURE_DESCRIPTION_TYPE) && (type != ProgramFieldType.PICTURE_COPYRIGHT_TYPE)) {
        typeList.add(type);
      }
    }

    return typeList.toArray(new ProgramFieldType[typeList.size()]);
  }

  private void savePluginIcons(final OrderChooser<IconPlugin> chooser, final StringArrayProperty prop) {
    List<IconPlugin> iconPluginArr = chooser.getOrderList();
    String[] pluginIdArr = new String[iconPluginArr.size()];
    for (int i = 0; i < iconPluginArr.size(); i++) {
      pluginIdArr[i] = iconPluginArr.get(i).getId();
    }
    
    prop.setStringArray(pluginIdArr);
  }
  
  private void saveInfoText(final OrderChooser<Object> chooser, final ProgramFieldTypeArrayProperty prop, final StringArrayProperty separators) {
    List<Object> infoFieldArr = chooser.getOrderList();
    ArrayList<ProgramFieldType> fieldTypeList = new ArrayList<ProgramFieldType>();
    ArrayList<String> separatorList = new ArrayList<String>();
    
    for (int i = 0; i < infoFieldArr.size(); i++) {
      if(infoFieldArr.get(i) instanceof ProgramFieldType) {
        fieldTypeList.add((ProgramFieldType)infoFieldArr.get(i));
        
        if(i < infoFieldArr.size()-1) {
          if(infoFieldArr.get(i+1) instanceof String) {
            i++;
            
            StringBuilder separator = new StringBuilder();
            separator.append(infoFieldArr.get(i));
            
            int j = i+1;
            
            while(j < infoFieldArr.size() && infoFieldArr.get(j) instanceof String) {
              separator.append(";#;").append(infoFieldArr.get(j));
              j++;
              i++;
            }
            
            separatorList.add(separator.toString());
          }
          else {
            separatorList.add(" - ");
          }
        }
      }
    }
    
    prop.setProgramFieldTypeArray(fieldTypeList.toArray(new ProgramFieldType[fieldTypeList.size()]));
    separators.setStringArray(separatorList.toArray(new String[separatorList.size()]));
    
  }
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    // icons
    savePluginIcons(mIconPluginOCh,Settings.ProgramPanel.ICON_PLUGINS );
    savePluginIcons(mIconPluginOChAlt,Settings.ProgramPanel.ICON_PLUGINS_ALTERNATIVE);
    
    // info text
    saveInfoText(mInfoTextOCh, Settings.ProgramPanel.INFO_FIELDS, Settings.ProgramPanel.INFO_FIELDS_SEPARATORS);
    saveInfoText(mInfoTextOChAlt, Settings.ProgramPanel.INFO_FIELDS_ALTERNATIVE, Settings.ProgramPanel.INFO_FIELDS_SEPARATORS_ALTERNATIVE);
    
    Settings.ProgramPanel.ORIGINIAL_TITLES_SHOW.setBoolean(mShowOriginalTitles.isSelected());
    
    Settings.ProgramPanel.BORDER_ON_AIR_PROGRAMS_SHOW.setBoolean(mBorderForOnAirPrograms.isSelected());
    Settings.ProgramPanel.HIGHLIGHTING_COLOR_GRADIENT.setBoolean(mGradientHighlighting.isSelected());
    
    Settings.ProgramPanel.COLOR_ON_AIR_DARK.setColor(mProgramItemProgressColorLb.getColor());
    Settings.ProgramPanel.COLOR_ON_AIR_LIGHT.setColor(mProgramItemOnAirColorLb.getColor());
    Settings.ProgramPanel.COLOR_KEYBOARD_SELECTED.setColor(mProgramItemKeyboardSelectedLb.getColor());
    Settings.ProgramPanel.TRANSPARENCY_ALLOW.setBoolean(mAllowProgramImportance.isSelected());
    Settings.ProgramPanel.HYPHENATION.setBoolean(mHyphenator.isSelected());
    Settings.ProgramPanel.SMOOTHER_SCROLLING.setBoolean(mSmootherScrolling.isSelected());
  }

  /**
   * Returns the icon of the tab-sheet.
   */
  public Icon getIcon() {
    return null;
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return LOCALIZER.msg("title", "Program display");
  }

  private static class IconPlugin {

    private PluginProxy mPlugin;
    private String mName;
    private Icon mIcon;

    public IconPlugin(final PluginProxy plugin, Icon icon) {
      mPlugin = plugin;
      mIcon = icon;
    }

    public Icon getIcon() {
      return mIcon;
    }

    public IconPlugin(final String name, final Icon icon) {
      mName = name;
      mPlugin = null;
      mIcon = icon;
    }

    public String getId() {
      if (mPlugin != null) {
        return mPlugin.getId();
      } else if (mName != null && mName.compareTo(PICTURE_ICON_NAME) == 0) {
        return Settings.PICTURE_ID;
      } else {
     //   String[] infoFilenames = ProgramInfoHelper.getInfoIconFilenames();
        Icon[] infoIcons = ProgramInfoHelper.getInfoIcons();
        String[] infoMessages = ProgramInfoHelper.getInfoIconMessages();
        for (int i = 0; i < infoIcons.length; i++) {
          if (infoIcons[i] != null) {
            if (LOCALIZER.msg("formatIcon", "Format: {0}", infoMessages[i]).equals(mName)) {
              return "FORMAT_" + i;
            }
          }
        }
      }
      return null;
    }

    public String toString() {
      if (mPlugin != null) {
        return mPlugin.getProgramTableIconText();
      } else {
        return mName;
      }
    }

  }

}
