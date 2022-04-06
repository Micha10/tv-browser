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

package tvbrowser.extras.favoritesplugin.wizards;

import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.Program;
import tvbrowser.extras.common.LimitationConfiguration;
import tvbrowser.extras.common.LimitationConfiguration.DayLimitValue;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import util.ui.ChannelChooserDlg;
import util.ui.EnhancedPanelBuilder;
import util.ui.TimePeriodChooser;
import util.ui.UiUtilities;

public class LimitationsWizardStep extends AbstractWizardStep {

  private static final util.i18n.Localizer LOCALIZER = util.i18n.Localizer
      .getLocalizerFor(LimitationsWizardStep.class);

  private JCheckBox mChannelCb;

  private JCheckBox mDayOfWeekCb;

  private JCheckBox mTimeCb;

  private JButton mChooseChannelsBtn;

  private JComboBox<Object> mDayOfWeekCombo;

  private TimePeriodChooser mTimePeriodChooser;

  private Program mProgram;

  private Channel[] mChannelArr;

  private WizardStep mCaller;

  private JPanel mContent;

  public LimitationsWizardStep(WizardStep caller, Program program) {
    mProgram = program;
    mCaller = caller;
  }

  public String getTitle() {
    return LOCALIZER.msg("title", "Limitations");
  }

  public JPanel createContent(WizardHandler handler) {

    if (mProgram != null) {
      mChannelArr = new Channel[]{ mProgram.getChannel()};
    }
    else {
      mChannelArr = new Channel[]{};
    }

    mDayOfWeekCombo = new JComboBox<>(LimitationConfiguration.DAYLIMIT_VALUE_ARRAY);
    
    int lowBnd, upBnd;
    if (mProgram != null) {
      lowBnd = (mProgram.getHours() - 1) * 60;
      if (lowBnd < 0) {
        lowBnd = 0;
      }
      upBnd = lowBnd + 120;
      if (upBnd >= 24 * 60) {
        upBnd = 24 * 60 - 1;
      }
    } else {
      lowBnd = 0;
      upBnd = 24 * 60 - 1;
    }
    mTimePeriodChooser = new TimePeriodChooser(lowBnd, upBnd, TimePeriodChooser.ALIGN_RIGHT);

    EnhancedPanelBuilder panelBuilder = new EnhancedPanelBuilder(new FormLayout("pref, default:grow, pref"));
    panelBuilder.border(Borders.DLU4);

    panelBuilder.addLabelRow(false, LOCALIZER.msg("mainQuestion", "Are there any limitations?"), 1);
    panelBuilder.addRow(mChannelCb = new JCheckBox(LOCALIZER.msg("limitByChannel", "Certain channels only:")), 1);
    panelBuilder.add(mChooseChannelsBtn = new JButton(LOCALIZER.msg("selectChannels","Select channels")), 3);
    panelBuilder.addRow(mDayOfWeekCb = new JCheckBox(LOCALIZER.msg("limitByDayOfWeek","Certain day of week only:")), 1);
    panelBuilder.add(mDayOfWeekCombo, 3);
    panelBuilder.addRow(mTimeCb = new JCheckBox(LOCALIZER.msg("limitByTime", "Certain start times only:")), 1);
    panelBuilder.add(mTimePeriodChooser, 3);

    updateControls();

    mChannelCb.addActionListener(e -> {
      updateControls();
    });

    mDayOfWeekCb.addActionListener(e -> {
      updateControls();
    });

    mTimeCb.addActionListener(e -> {
      updateControls();
    });

    mChooseChannelsBtn.addActionListener(e -> {
      Window parent = UiUtilities.getBestDialogParent(mContent);
      ChannelChooserDlg dlg = new ChannelChooserDlg(parent, mChannelArr,
          null,
          ChannelChooserDlg.SELECTABLE_ITEM_LIST);
      UiUtilities.centerAndShow(dlg);
      Channel[] chArr = dlg.getChannels();
      if (chArr != null) {
        mChannelArr = dlg.getChannels();
        if (mChannelArr.length == 0) {
          mChannelCb.setSelected(false);
          updateControls();
        }
      }
    });

    mContent = panelBuilder.getPanel();
    mContent.addFocusListener(new FocusAdapter() {

        public void focusGained(FocusEvent e) {
          mChannelCb.requestFocusInWindow();
        }
      });
    return mContent;

  }


  private void updateControls() {
    mChooseChannelsBtn.setEnabled(mChannelCb.isSelected());
    mDayOfWeekCombo.setEnabled(mDayOfWeekCb.isSelected());
    mTimePeriodChooser.setEnabled(mTimeCb.isSelected());

  }

  public Object createDataObject(Object obj) {
    Favorite fav = (Favorite)obj;
    if (mChannelCb.isSelected()) {
      fav.getLimitationConfiguration().setChannels(mChannelArr);
    }
    if (mTimeCb.isSelected()) {
      fav.getLimitationConfiguration().setTime(mTimePeriodChooser.getFromTime(), mTimePeriodChooser.getToTime());
    }
    if (mDayOfWeekCb.isSelected()) {
      int dayOfWeek = ((DayLimitValue)mDayOfWeekCombo.getSelectedItem()).getDay();
      fav.getLimitationConfiguration().setDayLimit(dayOfWeek);
      if (!mTimeCb.isSelected()) {
        fav.getLimitationConfiguration().setTime(0, 24*60-1);
      }
    }
    return obj;
  }

  public WizardStep next() {
    return new RenameWizardStep(this);
  }

  public WizardStep back() {
    return mCaller;
  }

  public boolean isValid() {
    return true;
  }

  public int[] getButtons() {
    return new int[] { WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL, WizardStep.BUTTON_BACK, WizardStep.BUTTON_NEXT };
  }

}
