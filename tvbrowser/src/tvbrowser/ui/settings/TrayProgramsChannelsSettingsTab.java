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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Channel;
import devplugin.SettingsItem;
import devplugin.SettingsTab;
import tvbrowser.core.Settings;
import util.i18n.Localizer;
import util.ui.EnhancedPanelBuilder;
import util.ui.OrderChooser;
import util.ui.UiUtilities;

/**
 * Channel settings for the program showing in tray.
 * 
 * @author René Mach
 *
 */
public class TrayProgramsChannelsSettingsTab implements SettingsTab {

  private static final util.i18n.Localizer LOCALIZER = TrayBaseSettingsTab.LOCALIZER;
  
  private JCheckBox mUseUserChannels;
  private OrderChooser<Channel> mChannelOCh;
  private static boolean mTrayIsEnabled = Settings.Tray.ENABLED.getBoolean();
  private JLabel mSeparator1;
  private JSlider mChannelWidth;
  
  private String mHelpLinkText;
  
  private JEditorPane mHelpLabel;
  
  private static TrayProgramsChannelsSettingsTab mInstance;
  private static boolean mNow = Settings.Tray.Now.ENABLED.getBoolean(),
                         mSoon = Settings.Tray.Soon.ENABLED.getBoolean(),
                         mOnTime = Settings.Tray.OnTime.ENABLED.getBoolean();
  
  public JPanel createSettingsPanel() {
    mInstance = this;
    
    EnhancedPanelBuilder builder = new EnhancedPanelBuilder(new FormLayout("5dlu,pref,2dlu,default,5dlu,pref,fill:default:grow,5dlu"));
    builder.border(Borders.DIALOG);
    
    try {
      mChannelWidth = new JSlider(SwingConstants.HORIZONTAL, 40, 150, Settings.Tray.Channels.WIDTH.getInt());
    }catch(Exception e){e.printStackTrace();}
    
    mUseUserChannels = new JCheckBox(LOCALIZER.msg("userChannels","Use user defined channels"),Settings.Tray.Channels.SPECIAL_USE.getBoolean());
    mUseUserChannels.setToolTipText(LOCALIZER.msg("userChannelsToolTip","<html>If you select this you can choose the channels that will be used for<br><b>Programs at...</b> and <b>Now/Soon running programs</b>.<br>If this isn't selected the first 10 channels in default order will be used.</html>"));
    
    mChannelOCh = new OrderChooser<>(
        Settings.Tray.Channels.SPECIAL.getChannelArray(),
        Settings.Channels.SUBSCRIBED.getChannelArray(), true);
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("help","The Tray is deactivated. To activate these settings activate the option <b>Tray activated</b> in the <a href=\"#link\">Tray Base settings</a>."),e -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        SettingsDialog.getInstance().showSettingsTab(SettingsItem.TRAY);
      }
    });
    
    mHelpLinkText = mHelpLabel.getText();
    mHelpLabel.setFont(mUseUserChannels.getFont());
    
    builder.addSeparatorRowFull(false, LOCALIZER.msg("channelColumnWidth","Column with for channel name"));
    builder.addRow(mChannelWidth, 2);
    final JLabel valueLabel = builder.labelAdd(String.valueOf(mChannelWidth.getValue()), 4);
    valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    Dimension dim = valueLabel.getPreferredSize();
    valueLabel.setPreferredSize(new Dimension(Sizes.dialogUnitXAsPixel(20, builder.getPanel()), dim.height));

    mChannelWidth.addChangeListener(e -> {
      valueLabel.setText(String.valueOf(mChannelWidth.getValue()));
    });
    
    JButton reset = new JButton(Localizer.getLocalization(Localizer.I18N_DEFAULT));
    reset.addActionListener(e -> {
      mChannelWidth.setValue(Settings.Tray.Channels.WIDTH.getDefault());
    });
    
    builder.add(reset, 6);
    
    JPanel c = (JPanel) builder.addParagraph(LOCALIZER.msg("channelsSeparator","Which channels should be used for these displays?"));
    builder.addRowFull(mUseUserChannels, 2);
    builder.addRowFull("10dlu,fill:default:grow", mChannelOCh, 2);
    builder.addRowFull(mHelpLabel);
        
    mSeparator1 = (JLabel)c.getComponent(0);
    
    setEnabled(true);
    
    mUseUserChannels.addActionListener(e -> {
      setEnabled(false);
    });
    
    return builder.getPanel();
  }
  
  private String createHtml(Font font,String text) {
    return "<html><div style=\"color:#000000;font-family:"+ font.getName() +"; font-size:"+font.getSize()+";\">"+text+"</div></html>";
  }

  private void setEnabled(boolean trayStateChange) {
    if(!mTrayIsEnabled) {
      mHelpLabel.setVisible(true);
      mHelpLabel.setText(createHtml(mHelpLabel.getFont(),mHelpLinkText));
    }
    else if(!mNow && !mSoon && !mOnTime) {
      mHelpLabel.setVisible(true);
      mHelpLabel.setText(createHtml(mHelpLabel.getFont(),LOCALIZER.msg("helpPrograms","<html>These settings are used only by the Now, Soon and At... programs. Enable at least one of that to enable these settings.</html>")));
    } else {
      mHelpLabel.setVisible(false);
    }
    
    if(trayStateChange) {
      mSeparator1.setEnabled(mTrayIsEnabled);
    }
    
    mUseUserChannels.setEnabled(mTrayIsEnabled && (mNow || mSoon || mOnTime));
    mChannelOCh.setEnabled(mTrayIsEnabled && mUseUserChannels.isSelected() && (mNow || mSoon || mOnTime));
  }
  
  public void saveSettings() {
    Settings.Tray.Channels.SPECIAL_USE.setBoolean(mUseUserChannels
        .isSelected());
    
    List<Channel> order = mChannelOCh.getOrderList();
    Channel[] ch = new Channel[order.size()];

    if(!mUseUserChannels.isSelected()) {
      order.clear();
      Collections.addAll(order, Settings.Channels.SUBSCRIBED.getChannelArray());
      
      ch = new Channel[order.size() > 10 ? 10 : order.size()];
    }

    for (int i = 0; i < ch.length; i++) {
      ch[i] = order.get(i);
    }
    
    if (order != null) {
      Settings.Tray.Channels.SPECIAL.setChannelArray(ch);
    }
    
    if (mChannelWidth != null) {
      Settings.Tray.Channels.WIDTH.setInt(mChannelWidth.getValue());
    }
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return Localizer.getLocalization(Localizer.I18N_CHANNELS);
  }

  protected static void setTrayIsEnabled(boolean value) {
    mTrayIsEnabled = value;
    if(mInstance != null) {
      mInstance.setEnabled(true);
    }
  }
  
  protected static void setNowIsEnabled(boolean value) {
    mNow = value;
    if(mInstance != null) {
      mInstance.setEnabled(false);
    }
  }
  
  protected static void setSoonIsEnabled(boolean value) {
    mSoon = value;
    if(mInstance != null) {
      mInstance.setEnabled(false);
    }
  }

  protected static void setOnTimeIsEnabled(boolean value) {
    mOnTime = value;
    if(mInstance != null) {
      mInstance.setEnabled(false);
    }
  }
}
