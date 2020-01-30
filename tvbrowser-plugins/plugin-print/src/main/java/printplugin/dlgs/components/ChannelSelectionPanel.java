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

package printplugin.dlgs.components;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import printplugin.util.BaseAction;

import util.ui.ChannelChooserDlg;
import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * {@link JPanel} that let the user choose between all and
 * selected channels using the {@link ChannelChooserDlg}.
 *
 * @author Martin Oberhauser (martin@tvbrowser.org)
 * @author troggan (2010-01-17 19:17:30 +0100)
 * @since 06.02.2005 21:11:24
 */
@SuppressWarnings("nls")
public class ChannelSelectionPanel extends JPanel implements ActionListener, ChangeListener {

  private static final long serialVersionUID = 8211494254284007875L;

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ChannelSelectionPanel.class);

  private final Frame mParent;
  private final JRadioButton mAllChannelsRb;
  private final JRadioButton mSelectedChannelsRb;
  private final JButton mChangeSelectedChannelsBt;

  private Channel[] mChannels;

  public ChannelSelectionPanel(final Frame dlgParent, final Channel[] channels) {

    mChannels = channels;
    mParent = dlgParent;

    mChangeSelectedChannelsBt = new JButton(BaseAction.select(this).build());

    final PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref:grow,10dlu,pref",
        "pref,5dlu,pref,2dlu,pref,10dlu"), this);
    pb.addSeparator(Localizer.getLocalization(Localizer.I18N_CHANNELS), CC.xyw(1, 1, 4));
    pb.add(mAllChannelsRb = new JRadioButton(mLocalizer.msg("all", "All")), CC.xy(2, 3));
    pb.add(mSelectedChannelsRb = new JRadioButton(), CC.xy(2, 5));
    pb.add(mChangeSelectedChannelsBt, CC.xy(4, 5));

    final ButtonGroup group = new ButtonGroup();
    group.add(mAllChannelsRb);
    group.add(mSelectedChannelsRb);

    updateSelectedChannelsPanel();

    mAllChannelsRb.addChangeListener(this);
    mSelectedChannelsRb.addChangeListener(this);
    mAllChannelsRb.setSelected(true);
  }

  private void updateSelectedChannelsPanel() {
    String radioBtnText = mLocalizer.msg("selectedChannels", "Selected");
    if (mChannels != null) {
      radioBtnText += " ("
          + mLocalizer.msg("selectedChannelsCnt", "{0} channels selected",
              String.valueOf(mChannels.length))
          + ")";

      if (mChannels.length > 0) {
        final String s = Arrays.stream(mChannels)
            .map(c -> c.getName())
            .collect(Collectors.joining(", "));
        mSelectedChannelsRb.setToolTipText(
            String.format(Locale.getDefault(), "<html><body><p style=\"width:360px;\">%s</p></body></html>", s));
      } else {
        mSelectedChannelsRb.setToolTipText(null);
      }
    }

    mSelectedChannelsRb.setText(radioBtnText);
    mChangeSelectedChannelsBt.setEnabled(mSelectedChannelsRb.isSelected());
  }

  public Channel[] getChannels() {
    if (mAllChannelsRb.isSelected()) {
      return null;
    }
    return mChannels;
  }

  public void setChannels(final Channel[] channels) {
    mChannels = channels;
    if (mChannels == null) {
      mAllChannelsRb.setSelected(true);
    } else {
      mSelectedChannelsRb.setSelected(true);
    }
    updateSelectedChannelsPanel();
  }

  @Override
  public void stateChanged(final ChangeEvent e) {
    if (mSelectedChannelsRb.isSelected()) {
      mChannels = mChannels == null ? new Channel[0] : mChannels;
      updateSelectedChannelsPanel();
    }
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void actionPerformed(final ActionEvent e) {
    switch (e.getActionCommand()) {
      case BaseAction.SELECT:
        final ChannelChooserDlg dlg = new ChannelChooserDlg(UiUtilities.getLastModalChildOf(mParent), mChannels,
            null);
        dlg.setMinimumSize(dlg.getSize());
        UiUtilities.centerAndShow(dlg);
        mChannels = dlg.getChannels();
        updateSelectedChannelsPanel();
        break;
    }
  }
}