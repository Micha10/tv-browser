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
package tvbrowser.ui.settings;

import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.HyperlinkEvent;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.CancelableSettingsTab;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.SettingsItem;
import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.GenericFilterMap;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import tvbrowser.ui.filter.dlgs.EditFilterDlg;
import tvbrowser.ui.mainframe.MainFrame;
import util.i18n.Localizer;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.CaretPositionCorrector;
import util.ui.EnhancedPanelBuilder;
import util.ui.MarkerChooserDlg;
import util.ui.PluginsPictureSettingsPanel;
import util.ui.UiUtilities;

/**
 * The settings tab for the program panel picture settings.
 *
 * @author René Mach
 * @since 2.2.2
 */
public class PictureSettingsTab extends AbstractSettingsTab implements CancelableSettingsTab {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(PictureSettingsTab.class);

  private JRadioButton mShowPicturesEver, mShowPicturesNever, mShowPicturesForSelection, mShowPicturesForFilter;
  private JCheckBox mShowPicturesInTimeRange, mShowPicturesForDuration, mShowPicturesForPlugins;
  private JSpinner mPictureStartTime, mPictureEndTime, mDuration;
  private JLabel mStartLabel, mEndLabel;
  private JCheckBox mShowDescription;
  private JCheckBox mShowPictureBorderProgramTable;

  private JLabel mPluginLabel;
  private Marker[] mClientPlugins;

  private JButton choose;

  private PluginsPictureSettingsPanel mPluginsPictureSettings;

  private JSpinner mDescriptionLines;

  private JLabel mDescriptionLabel;
  
  private static int PLUGIN_PICTURE_SELECTION_ORIGINAL = -1;
  private long mLastPluginSelectionHandling = 0;
  
  public PictureSettingsTab() {
  }

  public JPanel createSettingsPanel() {
    try {
      mShowPicturesNever = new JRadioButton(LOCALIZER.msg("showNever", "Show never"), Settings.Pictures.TYPE.getInt() == ProgramPanelSettings.SHOW_PICTURES_NEVER);
      mShowPicturesEver = new JRadioButton(LOCALIZER.msg("showEver", "Show always"), Settings.Pictures.TYPE.getInt() == ProgramPanelSettings.SHOW_PICTURES_EVER);
      mShowPicturesForSelection = new JRadioButton(LOCALIZER.msg("showForSelection", "Selection..."), Settings.Pictures.TYPE.getInt() > 1 && Settings.Pictures.TYPE.getInt() < 10);
      mShowPicturesForFilter = new JRadioButton(LOCALIZER.msg("showForFilter", "For filter..."), Settings.Pictures.TYPE.getInt() == ProgramPanelSettings.SHOW_PICTURES_FOR_FILTER);

      mShowPicturesInTimeRange = new JCheckBox(LOCALIZER.msg("showInTimeRange", "Show in time range:"), ProgramPanelSettings.typeContainsType(Settings.Pictures.TYPE.getInt(), ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE));
      mShowPicturesForDuration = new JCheckBox(LOCALIZER.msg("showForDuration", "Show for duration more than or equals to:"), ProgramPanelSettings.typeContainsType(Settings.Pictures.TYPE.getInt(), ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION));

      mShowPictureBorderProgramTable = new JCheckBox(LOCALIZER.msg("showPictureBorder","Show border around picture"), Settings.Pictures.BORDER_SHOW.getBoolean());
      
      ButtonGroup bg = new ButtonGroup();

      bg.add(mShowPicturesEver);
      bg.add(mShowPicturesNever);
      bg.add(mShowPicturesForSelection);
      bg.add(mShowPicturesForFilter);

      String timePattern = LOCALIZER.msg("timePattern", "hh:mm a");

      mPictureStartTime = new JSpinner(new SpinnerDateModel());
      mPictureStartTime.setEditor(new JSpinner.DateEditor(mPictureStartTime, timePattern));
      CaretPositionCorrector.createCorrector(((JSpinner.DateEditor) mPictureStartTime.getEditor()).getTextField(), new char[]{':'}, -1);

      mPictureEndTime = new JSpinner(new SpinnerDateModel());
      mPictureEndTime.setEditor(new JSpinner.DateEditor(mPictureEndTime, timePattern));
      CaretPositionCorrector.createCorrector(((JSpinner.DateEditor) mPictureEndTime.getEditor()).getTextField(), new char[]{':'}, -1);

      mDuration = new JSpinner(new SpinnerNumberModel(Settings.Pictures.DURATION.getInt(), 10, 240, 1));

      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY, Settings.Pictures.TIME_START.getHourOfDay());
      cal.set(Calendar.MINUTE, Settings.Pictures.TIME_START.getMinutesOfHour());
      mPictureStartTime.setValue(cal.getTime());

      cal.set(Calendar.HOUR_OF_DAY, Settings.Pictures.TIME_END.getHourOfDay());
      cal.set(Calendar.MINUTE, Settings.Pictures.TIME_END.getMinutesOfHour());
      mPictureEndTime.setValue(cal.getTime());

      mShowDescription = new JCheckBox(LOCALIZER.msg("showDescription", "Show description for pictures"), Settings.Pictures.DESCRIPTION_SHOW.getBoolean());
      mShowDescription.addItemListener(e -> {
        mShowPictureBorderProgramTable.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
      });
      
      JEditorPane helpLabel = UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("help", "These settings affect only the showing of the pictures. The pictures can only be shown if the download of pictures in enabled. To enable the picture download look at the <a href=\"#link\">settings of the TV dataservices</a>."), e -> {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.PLUGINS);
        }
      });
      
      EnhancedPanelBuilder pb = new EnhancedPanelBuilder(new FormLayout("5dlu,12dlu,15dlu,default,5dlu,default,5dlu,default:grow,default,5dlu"));

      pb.border(Borders.DIALOG);
      
      pb.addSeparatorRowFull(false, LOCALIZER.msg("basics", "Picture settings for the program table"));

      pb.addRowFull(mShowPicturesNever, 2);
      pb.addRowFull(false, mShowPicturesEver, 2);
      pb.addRowFull(false, mShowPicturesForSelection, 2);

      pb.addRowFull("2dlu,default", mShowPicturesInTimeRange, 3);
      mStartLabel = pb.addLabelRow(LOCALIZER.msg("startTime", "From:"), 4);
      pb.add(mPictureStartTime, 6);
      mEndLabel = pb.addLabelRow("2dlu,default", LOCALIZER.msg("endTime", "To:"), 4);
      pb.add(mPictureEndTime, 6);
      
      pb.addRowFull("2dlu,default", mShowPicturesForDuration, 3);
      pb.addRow(false, mDuration, 6);
      final JLabel minutesLabel = pb.labelAdd(LOCALIZER.msg("minutes", "Minutes"), 8);
      
      if (Settings.Pictures.PLUGIN_IDS.getStringArray() != null) {
        JPanel mSubPanel = new JPanel(new FormLayout("15dlu,150dlu:grow,5dlu,pref", "pref,2dlu,pref"));

        mShowPicturesForPlugins = new JCheckBox(LOCALIZER.msg("showPicturesForPlugins", "Show for programs that are marked by plugins:"), ProgramPanelSettings.typeContainsType(Settings.Pictures.TYPE.getInt(), ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS));
        mPluginLabel = new JLabel();
        mPluginLabel.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            handlePluginSelection();
          }
        });
        
        mPluginLabel.setEnabled(mShowPicturesForPlugins.isSelected());

        choose = new JButton(LOCALIZER.msg("selectPlugins", "Choose Plugins"));
        choose.addActionListener(e -> {
          Window parent = UiUtilities.getLastModalChildOf(MainFrame
              .getInstance());
          MarkerChooserDlg chooser = new MarkerChooserDlg(parent,
              mClientPlugins, null);

          chooser.setLocationRelativeTo(parent);
          chooser.setVisible(true);

          mClientPlugins = chooser.getMarker();
          
          handlePluginSelection();
        });
        choose.setEnabled(ProgramPanelSettings.typeContainsType(Settings.Pictures.TYPE.getInt(), ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS));

        mShowPicturesForPlugins.addItemListener(e -> {
          mPluginLabel.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
          choose.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        });

        String[] clientPluginIdArr = Settings.Pictures.PLUGIN_IDS.getStringArray();

        ArrayList<Marker> clientPlugins = new ArrayList<Marker>();

        for (String arr : clientPluginIdArr) {
          PluginAccess plugin = Plugin.getPluginManager().getActivatedPluginForId(arr);
          if (plugin != null) {
            clientPlugins.add(plugin);
          } else if (ReminderPluginProxy.getInstance().getId().compareTo(arr) == 0) {
            clientPlugins.add(ReminderPluginProxy.getInstance());
          } else if (FavoritesPluginProxy.getInstance().getId().compareTo(arr) == 0) {
            clientPlugins.add(FavoritesPluginProxy.getInstance());
          }
        }

        mClientPlugins = clientPlugins.toArray(new Marker[clientPlugins.size()]);
        
        mSubPanel.add(mShowPicturesForPlugins, CC.xyw(1, 1, 4));
        mSubPanel.add(mPluginLabel, CC.xy(2, 3));
        mSubPanel.add(choose, CC.xy(4, 3));

        pb.addRow("2dlu,default", mSubPanel, 3, 7);
        pb.addRow("2dlu",false);
      }
      else {
        mPluginLabel.setEnabled(false);
      }
      
      final JButton editFilter = new JButton(LOCALIZER.msg("editFilter", "Edit filter"));
      editFilter.setEnabled(mShowPicturesForFilter.isSelected());
      editFilter.addActionListener(e -> {
        final UserFilter filter = GenericFilterMap.getInstance().getGenericInternalFilter(GenericFilterMap.GENERIC_PICTURE_FILTER_NAME);
        
        final EditFilterDlg editFilter1 = new EditFilterDlg(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), FilterList.getInstance(), filter, false);
        
        if(editFilter1.getOkWasPressed()) {
          GenericFilterMap.getInstance().updateGenericInternalFilter(GenericFilterMap.GENERIC_PICTURE_FILTER_NAME, filter);
        }
      });
      
      mShowPicturesForFilter.addItemListener(e -> {
        editFilter.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      });
      
      pb.addRowFull(false, mShowPicturesForFilter);
      pb.addRow("2dlu,default", editFilter, 3, 4);
      pb.addRowFull(mShowDescription, 2);

      mDescriptionLines = new JSpinner(new SpinnerNumberModel(Settings.Pictures.DESCRIPTION_LINES.getInt(), 1, 20, 1));
      pb.addRow(false, mDescriptionLines, 3, 4);
      mDescriptionLabel = new JLabel(LOCALIZER.msg("lines", "lines"));
  	  pb.add(mDescriptionLabel, 8);
      pb.addRowFull(false, mShowPictureBorderProgramTable, 3);
  	  mDescriptionLabel.setEnabled(mShowDescription.isSelected());
  	  mDescriptionLines.setEnabled(mShowDescription.isSelected());
  	  mShowPictureBorderProgramTable.setEnabled(!mShowDescription.isSelected());
  	  mShowDescription.addActionListener(e -> {
  		  mDescriptionLines.setEnabled(mShowDescription.isSelected());
  		  mDescriptionLabel.setEnabled(mShowDescription.isSelected());
  		});

      pb.addParagraph(LOCALIZER.msg("pluginPictureTitle", "Default picture settings for the program lists of the Plugins"), 1, 9);
      pb.addRow(mPluginsPictureSettings = new PluginsPictureSettingsPanel(new PluginPictureSettings(Settings.Pictures.PLUGINS_SETTING.getInt()), true), 2, 8);
      pb.addRowFull("10dlu,default", helpLabel);
      
      if(PLUGIN_PICTURE_SELECTION_ORIGINAL == -1) {
        PLUGIN_PICTURE_SELECTION_ORIGINAL = mPluginsPictureSettings.getSettings().getType();
      }
      
      mPluginsPictureSettings.addChangeListener(e -> {
        Settings.setRestartInfo(PictureSettingsTab.class.getCanonicalName(), PLUGIN_PICTURE_SELECTION_ORIGINAL != mPluginsPictureSettings.getSettings().getType());
      });
      
      mShowPicturesInTimeRange.addItemListener(e -> {
        mPictureStartTime.setEnabled(mShowPicturesInTimeRange.isSelected());
        mPictureEndTime.setEnabled(mShowPicturesInTimeRange.isSelected());
        mStartLabel.setEnabled(mShowPicturesInTimeRange.isSelected());
        mEndLabel.setEnabled(mShowPicturesInTimeRange.isSelected());
      });

      mShowPicturesForDuration.addItemListener(e -> {
        mDuration.setEnabled(mShowPicturesForDuration.isSelected());
        minutesLabel.setEnabled(mShowPicturesForDuration.isSelected());
      });

      mShowPicturesNever.addItemListener(e -> {
        mShowDescription.setEnabled(!mShowPicturesNever.isSelected());
        mDescriptionLines.setEnabled(!mShowPicturesNever.isSelected() && mShowDescription.isSelected());
        mShowPictureBorderProgramTable.setEnabled(!mShowPicturesNever.isSelected() && !mShowDescription.isSelected());
      });

      mShowPicturesForSelection.addItemListener(e -> {
        mShowPicturesForDuration.setEnabled(mShowPicturesForSelection.isSelected());
        mShowPicturesInTimeRange.setEnabled(mShowPicturesForSelection.isSelected());
        mStartLabel.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesInTimeRange.isSelected());
        mEndLabel.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesInTimeRange.isSelected());
        minutesLabel.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesForDuration.isSelected());
        mPictureStartTime.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesInTimeRange.isSelected());
        mPictureEndTime.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesInTimeRange.isSelected());
        mDuration.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesForDuration.isSelected());
        
        if (mShowPicturesForPlugins != null) {
          mShowPicturesForPlugins.setEnabled(mShowPicturesForSelection.isSelected());
        }
        if (mPluginLabel != null) {
          mPluginLabel.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesForPlugins.isSelected());
        }
        if (choose != null) {
          choose.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesForPlugins.isSelected());
        }
      });

      mShowPicturesInTimeRange.getItemListeners()[0].itemStateChanged(null);
      mShowPicturesForDuration.getItemListeners()[0].itemStateChanged(null);
      mShowPicturesForSelection.getItemListeners()[0].itemStateChanged(null);
      mShowPicturesNever.getItemListeners()[0].itemStateChanged(null);

      return pb.getPanel();

    } catch (Exception e) {
      e.printStackTrace();
    }


    return null;
  }

  @Override
  public Icon getIcon() {
    return getPictureIcon();
  }

  public String getTitle() {
    return Localizer.getLocalization(Localizer.I18N_PICTURES);
  }

  public void saveSettings() {
    Settings.Pictures.TYPE.setInt(getPictureShowingType());
    Settings.Pictures.TIME_START.setInt(getPictureTimeRangeStart());
    Settings.Pictures.TIME_END.setInt(getPictureTimeRangeEnd());
    Settings.Pictures.DURATION.setInt((Integer) mDuration.getValue());
    Settings.Pictures.BORDER_SHOW.setBoolean(mShowDescription.isSelected());
    
    if(!mShowDescription.isSelected()) {
      Settings.Pictures.BORDER_SHOW.setBoolean(mShowPictureBorderProgramTable.isSelected());
    }
    else {
      Settings.Pictures.BORDER_SHOW.setBoolean(true);
    }

    if (ProgramPanelSettings.typeContainsType(getPictureShowingType(), ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS)) {
      Settings.Pictures.PLUGIN_IDS.setStringArray(getClientPluginIds());
    }

    Settings.Pictures.PLUGINS_SETTING.setInt(mPluginsPictureSettings.getSettings().getType());
    Settings.Pictures.DESCRIPTION_LINES.setInt((Integer) mDescriptionLines.getValue());
  }
  
  @Override
  public void cancel() {
    Settings.setRestartInfo(PictureSettingsTab.class.getCanonicalName(), PLUGIN_PICTURE_SELECTION_ORIGINAL != Settings.Pictures.PLUGINS_SETTING.getInt());
  }
  
  /**
   * @since 2.6
   */
  private synchronized void handlePluginSelection() {
    if(System.currentTimeMillis() - mLastPluginSelectionHandling > 10) {
      if (mClientPlugins.length > 0) {
        mPluginLabel.setText(mClientPlugins[0].toString());
        mPluginLabel.setEnabled(mShowPicturesForPlugins.isSelected());
        
        int i = 1;
        final String others = LOCALIZER.ellipsisMsg("otherPlugins", "others");
        int otherLength = mPluginLabel.getFontMetrics(mPluginLabel.getFont()).stringWidth(others)+30;
        
        do {
          String text = mPluginLabel.getText() + ", " + mClientPlugins[i];
          int addLength = otherLength;
          
          if(i == mClientPlugins.length-1) {
            addLength = 0;
          }
          
          if(mPluginLabel.getFontMetrics(mPluginLabel.getFont()).stringWidth(text)+addLength < mPluginLabel.getWidth()) {
          
            mPluginLabel.setText(text);
          }
          else {
            break;
          }
        }while(i++ < mClientPlugins.length-1);
    
        if (i < mClientPlugins.length) {
          mPluginLabel.setText(mPluginLabel.getText() + " (" + (mClientPlugins.length - i) + " " + others + ")");
        }
      } else {
        mPluginLabel.setText(LOCALIZER.msg("noPlugins", "No Plugins choosen"));
        mPluginLabel.setEnabled(false);
      }
      
      mLastPluginSelectionHandling = System.currentTimeMillis();
    }
  }

  /**
   * @return The picture showing type of this settings
   * @since 2.6
   */
  private int getPictureShowingType() {
    int value = ProgramPanelSettings.SHOW_PICTURES_NEVER;

    if (mShowPicturesEver.isSelected()) {
      value = ProgramPanelSettings.SHOW_PICTURES_EVER;
    } else if (mShowPicturesForSelection.isSelected()) {
      if (mShowPicturesForDuration.isSelected()) {
        value += ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION;
      }
      if (mShowPicturesForPlugins != null && mShowPicturesForPlugins.isSelected() && mClientPlugins != null && mClientPlugins.length > 0) {
        value += ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS;
      }
      if (mShowPicturesInTimeRange.isSelected()) {
        value += ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE;
      }
    } else if(mShowPicturesForFilter.isSelected()) {
      value = ProgramPanelSettings.SHOW_PICTURES_FOR_FILTER;
    }

    return value;
  }

  /**
   * @return The time range start time.
   * @since 2.6
   */
  private int getPictureTimeRangeStart() {
    Calendar cal = Calendar.getInstance();
    Date startTime = (Date) mPictureStartTime.getValue();
    cal.setTime(startTime);
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }

  /**
   * @return The time range end time.
   * @since 2.6
   */
  private int getPictureTimeRangeEnd() {
    Calendar cal = Calendar.getInstance();
    Date startTime = (Date) mPictureEndTime.getValue();
    cal.setTime(startTime);
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }

  /**
   * @return The selected client plugins.
   * @since 2.6
   */
  private String[] getClientPluginIds() {
    if (mShowPicturesForPlugins != null) {
      String[] clientPluginIdArr = new String[mClientPlugins.length];

      for (int i = 0; i < mClientPlugins.length; i++) {
        clientPluginIdArr[i] = mClientPlugins[i].getId();
      }

      return clientPluginIdArr;
    }

    return null;
  }

}
