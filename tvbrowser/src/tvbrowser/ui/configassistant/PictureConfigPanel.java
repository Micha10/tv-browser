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
 */
package tvbrowser.ui.configassistant;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvdataservice.PictureSettingsIf;
import tvdataservice.SettingsPanel;
import util.i18n.Localizer;
import util.ui.EnhancedPanelBuilder;
import util.ui.UiUtilities;

/**
 * A panel with the settings for the picture
 * download of the TvBrowserDataService.
 * 
 * @author René Mach
 * @since 2.2.2
 */
public class PictureConfigPanel extends JPanel {
  private static final long serialVersionUID = 1L;

  protected static final Localizer mLocalizer = Localizer.getLocalizerFor(PictureConfigPanel.class);
  
  private JRadioButton mDownloadAll, mDownloadNoPictures, mDownloadEvening, mDownloadMorning;
  private SettingsPanel mTvBrowserDataServiceSettingsPanel;

  /**
   * Creates this panel.
   * 
   * @param update If this panel is for an update of TV-Browser.
   */
  public PictureConfigPanel(boolean update) {
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder(new FormLayout(
        "fill:pref:grow, 10dlu, fill:300dlu:grow, fill:pref:grow","15dlu"), this);
    
    mDownloadAll = new JRadioButton(mLocalizer.msg("allPictures","Download pictures for all programs"));
    mDownloadNoPictures = new JRadioButton(mLocalizer.msg("noPictures","Don't download pictures"));
    mDownloadEvening = new JRadioButton(mLocalizer.msg("eveningPictures",
        "Download only pictures for the evening programs (4 PM to midnight)"));
    mDownloadMorning = new JRadioButton(mLocalizer.msg("morningPictures",
        "Download only pictures for the day programs (midnight to 4 PM)"));
    
    pb.addRow("fill:0dlu:grow, default", UiUtilities.createHtmlHelpTextArea((update ? mLocalizer.msg("preambelUpdate", "Preambel") : "") + mLocalizer.msg("preambel", "Preambel")), 2, 2);
    pb.addRow(mDownloadAll, 3);
    pb.addRow(false, mDownloadNoPictures, 3);
    pb.addRow(false, mDownloadEvening, 3);
    pb.addRow(false, mDownloadMorning, 3);
    
    pb.addRow(UiUtilities.createHtmlHelpTextArea(mLocalizer.msg(update ? "closingUpdate" : "closing", "Closing")), 2, 2);
    pb.addRow("fill:0dlu:grow",false);
    
    ButtonGroup bg = new ButtonGroup();
    
    bg.add(mDownloadAll);
    bg.add(mDownloadNoPictures);
    bg.add(mDownloadEvening);
    bg.add(mDownloadMorning);
    
    TvDataServiceProxy[] services = TvDataServiceProxyManager.getInstance().getTvDataServices(new String[] {"tvbrowserdataservice.TvBrowserDataService"});
    
    if(services != null && services.length == 1) {
      mTvBrowserDataServiceSettingsPanel = services[0].getSettingsPanel();
      
      if(mTvBrowserDataServiceSettingsPanel instanceof PictureSettingsIf) {
        int i = ((PictureSettingsIf)mTvBrowserDataServiceSettingsPanel).getPictureState();
        
        mDownloadNoPictures.setSelected(i == PictureSettingsIf.NO_PICTURES);
        mDownloadAll.setSelected(i == PictureSettingsIf.ALL_PICTURES);
        mDownloadMorning.setSelected(i == PictureSettingsIf.MORNING_PICTURES);
        mDownloadEvening.setSelected(i == PictureSettingsIf.EVENING_PICTURES);
      }
    }
  }
  
  /**
   * Saves the picture settings for TvBrowserDataService.
   */
  public void saveSettings() {
    if(mTvBrowserDataServiceSettingsPanel instanceof PictureSettingsIf) {
      PictureSettingsIf pictureIf = ((PictureSettingsIf)mTvBrowserDataServiceSettingsPanel);
      
      if(mDownloadNoPictures.isSelected()) {
        pictureIf.setPictureState(PictureSettingsIf.NO_PICTURES);
      } else if(mDownloadMorning.isSelected()) {
        pictureIf.setPictureState(PictureSettingsIf.MORNING_PICTURES);
      } else if(mDownloadEvening.isSelected()) {
        pictureIf.setPictureState(PictureSettingsIf.EVENING_PICTURES);
      } else if(mDownloadAll.isSelected()) {
        pictureIf.setPictureState(PictureSettingsIf.ALL_PICTURES);
      }

      mTvBrowserDataServiceSettingsPanel.ok();
    }
  }
  
  /**
   * Gets if the picture downloading is activated.
   * 
   * @return If the picture downloading is activated.
   */
  public boolean isActivated() {
    return !mDownloadNoPictures.isSelected();
  }
}
