package tvbrowser.ui.settings;

import java.awt.event.ItemEvent;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.factories.Borders;

import devplugin.SettingsTab;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import util.io.NetworkUtilities;
import util.ui.EnhancedPanelBuilder;

/**
 * Settings for network stuff.
 * 
 * @author René Mach
 * @since 2.5.1
 */
public class NetworkSettingsTab implements SettingsTab {
  private static final util.i18n.Localizer LOCALIZER = util.i18n.Localizer
  .getLocalizerFor(NetworkSettingsTab.class);
  
  private JSpinner mConnectionTimeout, mNetworkCheckTimeout;
  
  private JCheckBox mConnectionTest;
  
  public JPanel createSettingsPanel() {
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder("5dlu, pref, 3dlu, 0dlu:grow");
    pb.border(Borders.DIALOG);
        
    pb.addParagraph(LOCALIZER.msg("connectionTestTitle","Internet connection test"));
    pb.addRowFull(mConnectionTest = new JCheckBox(LOCALIZER.msg("connectionTestText","Internet connection test activated"), Settings.Network.INTERNET_CONNECTION_CHECK.getBoolean()), 2);
    pb.addRow(mNetworkCheckTimeout = new JSpinner(new SpinnerNumberModel(Settings.Network.CHECK_TIMEOUT.getInt()/1000,10,90,5)), 2);
    final JLabel label = pb.labelAdd(LOCALIZER.msg("waitTime","Seconds maximum waiting time for connection test"), 4);
    
    pb.addLabelRowFull(LOCALIZER.msg("sites", "Websites used for checking"), 2);
    
    final JList<String> urlList = new JList<>(NetworkUtilities.getConnectionCheckUrls());
    urlList.setEnabled(false);
    pb.addRowFull(new JScrollPane(urlList), 2);
    
    mConnectionTest.addItemListener(e -> {
      boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
      mNetworkCheckTimeout.setEnabled(enabled);
      label.setEnabled(enabled);
    });
    
    mNetworkCheckTimeout.setEnabled(mConnectionTest.isSelected());
    label.setEnabled(mConnectionTest.isSelected());
    
    pb.addParagraph(LOCALIZER.msg("cancelTime","Timeout for not responding connections"));
    pb.addRow(mConnectionTimeout = new JSpinner(new SpinnerNumberModel(Settings.Network.DEFAULT_CONNECTION_TIMEOUT.getInt()/1000,5,60,5)), 2);
    pb.labelAdd(LOCALIZER.msg("seconds","Seconds"), 4);
    
    return pb.getPanel();
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("status", "network-transmit-receive", 16);
  }

  public String getTitle() {
    return LOCALIZER.msg("title","Network");
  }

  public void saveSettings() {
    Settings.Network.INTERNET_CONNECTION_CHECK.setBoolean(mConnectionTest.isSelected());
    Settings.Network.DEFAULT_CONNECTION_TIMEOUT.setInt(((Integer)mConnectionTimeout.getValue()).intValue() * 1000);
    Settings.Network.CHECK_TIMEOUT.setInt(((Integer)mNetworkCheckTimeout.getValue()).intValue() * 1000);
  }
}
