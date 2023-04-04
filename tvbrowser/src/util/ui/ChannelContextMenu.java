package util.ui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelFilter;
import tvbrowser.core.ChannelList;
import tvbrowser.core.DummyChannel;
import tvbrowser.core.Settings;
import tvbrowser.core.filters.ChannelFilterList;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.mainframe.ChannelChooserPanel;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.actions.TVBrowserActions;
import tvbrowser.ui.settings.ChannelsSettingsTab;
import tvbrowser.ui.settings.channel.ChannelConfigDlg;
import util.browserlauncher.Launch;
import util.i18n.Localizer;
import util.ui.menu.MenuUtil;

/**
 * A class that builds a PopupMenu for a Channel.
 *
 * @author René Mach
 *
 */
public class ChannelContextMenu implements ActionListener {

  /**
   * The localizer for this class.
   */
  public static final Localizer LOCALIZER = Localizer
      .getLocalizerFor(ChannelContextMenu.class);

  private JPopupMenu mMenu;

  private JMenuItem mChAdd, mChConf, mChGoToURL;
  private JMenu mFilterChannels;

  private Object mSource;

  private Channel mChannel;

  private Component mComponent;

  private JRadioButtonMenuItem layoutBoth;

  private JRadioButtonMenuItem layoutLogo;

  private JRadioButtonMenuItem layoutName;

  /**
   * Constructs the PopupMenu.
   *
   * @param e
   *          The event that requested the PopupMenu.
   * @param ch
   *          The Channel on which the event was.
   * @param src
   *          The source Object.
   */
  public ChannelContextMenu(MouseEvent e, Channel ch, Object src) {
    mSource = src;
    mChannel = ch;
    mComponent = e.getComponent();

    mMenu = new JPopupMenu();
    mChAdd = new JMenuItem(LOCALIZER.msg("addChannels", "Add/Remove channels"));
    mChConf = new JMenuItem(LOCALIZER.msg("configChannel", "Setup channel"),
        TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
    mChGoToURL = new JMenuItem(LOCALIZER.msg("openURL", "Open internet page"),
        IconLoader.getInstance().getIconFromTheme("apps",
            "internet-web-browser"));

    // dynamically create filters from available channel filter components
    mFilterChannels = new JMenu(LOCALIZER.msg("filterChannels",
        "Channel filter"));
    MainFrame.getInstance().updateChannelGroupMenu(mFilterChannels);

    mChAdd.addActionListener(this);
    mChConf.addActionListener(this);
    mChGoToURL.addActionListener(this);

    mMenu.add(mChGoToURL);
    if (ChannelList.isSubscribedChannel(ch) && !(ch instanceof DummyChannel)) {
      mMenu.add(mChConf);
    }
    if (!(mSource instanceof ChannelsSettingsTab)) {
      mMenu.add(mFilterChannels);
      mMenu.addSeparator();
      JMenu configureLayout = new JMenu(LOCALIZER.msg("layout", "Layout"));
      layoutBoth = new JRadioButtonMenuItem(LOCALIZER.msg("layoutBoth",
          "Logo and name"));
      layoutLogo = new JRadioButtonMenuItem(LOCALIZER
          .msg("layoutLogo", "Logo"));
      layoutName = new JRadioButtonMenuItem(LOCALIZER
          .msg("layoutName", "Name"));
      configureLayout.add(layoutBoth);
      configureLayout.add(layoutLogo);
      configureLayout.add(layoutName);
      
      layoutBoth.addActionListener(this);
      layoutLogo.addActionListener(this);
      layoutName.addActionListener(this);

      // is the layout configuration for the channel chooser
      if (mSource instanceof ChannelChooserPanel) {
        JCheckBoxMenuItem dragAndDropEnabled = new JCheckBoxMenuItem(LOCALIZER.msg("dragAndDrop", "Move channels with drag and drop"));
        dragAndDropEnabled.setSelected(Settings.Window.CHANNEL_SELECTION_DRAG_AND_DROP.getBoolean());
        dragAndDropEnabled.addActionListener(e1 -> {
          Settings.Window.CHANNEL_SELECTION_DRAG_AND_DROP.toggleValue();
          ((ChannelChooserPanel)mSource).updateDragAndDropEnabled();
        });
        
        mMenu.add(dragAndDropEnabled);
        
        if (Settings.IconAndNames.SHOW_ICONS_IN_CHANNEL_LIST.getBoolean()
            && Settings.IconAndNames.SHOW_NAMES_IN_CHANNEL_LIST.getBoolean()) {
          layoutBoth.setSelected(true);
        } else if (Settings.IconAndNames.SHOW_ICONS_IN_CHANNEL_LIST.getBoolean()) {
          layoutLogo.setSelected(true);
        } else {
          layoutName.setSelected(true);
        }
      }
      // or is it for the program table?
      else if (mSource instanceof ChannelLabel) {
        if (Settings.IconAndNames.SHOW_ICONS_IN_PROGRAM_TABLE.getBoolean()
            && Settings.IconAndNames.SHOW_NAMES_IN_PROGRAM_TABLE.getBoolean()) {
          layoutBoth.setSelected(true);
        } else if (Settings.IconAndNames.SHOW_ICONS_IN_PROGRAM_TABLE.getBoolean()) {
          layoutLogo.setSelected(true);
        } else {
          layoutName.setSelected(true);
        }
      }
      
      mMenu.add(mChAdd);
      mMenu.add(configureLayout);
      // add context menu actions from plugins
      addPluginContextMenuItems(ch);
    }
    mMenu.show(e.getComponent(), e.getX(), e.getY());
  }

  private void addPluginContextMenuItems(final Channel ch) {
    boolean separator = false;
    for (PluginProxy plugin : PluginProxyManager.getInstance().getActivatedPlugins()) {
      ActionMenu context = plugin.getContextMenuActions(ch);
      if (context != null) {
        if (!separator) {
          mMenu.addSeparator();
          separator = true;
        }
        mMenu.add(MenuUtil.createMenuItem(context));
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource().equals(mChAdd)) {
      TVBrowserActions.configureChannels.actionPerformed(null);
    } else if (e.getSource().equals(mChConf)) {
      if ((mSource instanceof ChannelsSettingsTab)) {
        ((ChannelsSettingsTab) mSource).configChannels();
      } else {

        Window parent = UiUtilities.getBestDialogParent(mComponent);
        ChannelConfigDlg dialog = new ChannelConfigDlg(parent, mChannel);
        dialog.centerAndShow();

        ChannelList.checkForJointChannels();
        
        // If from a ChannelLabel update it
      /*  if (mSource instanceof ProgramTableChannelLabel) {
          ((ProgramTableChannelLabel) mSource).setChannel(mChannel);
        }*/
      }

     // if (!(mSource instanceof ProgramTableChannelLabel)) {
        MainFrame.getInstance().getProgramTableScrollPane()
            .updateChannelLabelForChannel(mChannel);
     // }
      MainFrame.getInstance().updateChannelChooser();
      ChannelList.storeAllSettings();
    } else if (e.getSource().equals(mChGoToURL)) {
      Launch.openURL(mChannel.getWebpage());
    } else {
      if (e.getSource() instanceof JRadioButtonMenuItem) {
        if (e.getSource() == layoutBoth || e.getSource() == layoutName
            || e.getSource() == layoutLogo) {
          boolean showNames = e.getSource() == layoutBoth || e.getSource() == layoutName;
          boolean showIcons = e.getSource() == layoutBoth || e.getSource() == layoutLogo;
          if (mSource instanceof ChannelChooserPanel) {
            Settings.IconAndNames.SHOW_NAMES_IN_CHANNEL_LIST.setBoolean(showNames);
            Settings.IconAndNames.SHOW_ICONS_IN_CHANNEL_LIST.setBoolean(showIcons);
            MainFrame.getInstance().updateChannelChooser();
          }
          else {
            Settings.IconAndNames.SHOW_NAMES_IN_PROGRAM_TABLE.setBoolean(showNames);
            Settings.IconAndNames.SHOW_ICONS_IN_PROGRAM_TABLE.setBoolean(showIcons);
            MainFrame.getInstance().getProgramTableScrollPane().updateChannelPanel();
          }
        } else {
          JRadioButtonMenuItem filterItem = (JRadioButtonMenuItem) e
              .getSource();
          setChanneFilter(ChannelFilterList.getInstance().getChannelFilterForName(filterItem.getText()));
        }
      }
    }
  }

  private void setChanneFilter(final ChannelFilter channelFilter) {
    MainFrame.getInstance().setChannelFilter(channelFilter);
  }
}
