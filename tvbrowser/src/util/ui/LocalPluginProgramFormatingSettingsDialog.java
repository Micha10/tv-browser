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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.ui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import util.i18n.Localizer;
import util.paramhandler.ParamHelpDialog;
import util.paramhandler.ParamLibrary;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;

/**
 * A settings dialog for the program configuration.
 *
 * @author René Mach
 * @since 2.5.1
 */
public class LocalPluginProgramFormatingSettingsDialog extends JDialog implements WindowClosingIf, ActionListener {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(LocalPluginProgramFormatingSettingsDialog.class);

  private AbstractPluginProgramFormating mConfig, mDefaultConfig;
  private JButton mSetName, mPreview, mSetBack, mHelp, mOk, mCancel;
  private JLabel mName;
  private JTextField mTitle;
  private JTextArea mContentArea;
  private JComboBox<String> mEncoding;

  /**
   * Creates an instance of this settings dialog.
   *
   * @param parent The parent window.
   * @param config The program configuration to edit.
   * @param defaultConfig The default program configurations.
   * @param showTitleSetting If the settings dialog should contain the title setting.
   * @param showEncodingSetting If the settings dialog should contain the encoding setting.
   */
  public static void createInstance(Window parent, AbstractPluginProgramFormating config, AbstractPluginProgramFormating defaultConfig, boolean showTitleSetting, boolean showEncodingSetting) {
    new LocalPluginProgramFormatingSettingsDialog(parent, config,
        defaultConfig, showTitleSetting, showEncodingSetting);
  }

  private LocalPluginProgramFormatingSettingsDialog(Window parent,
      AbstractPluginProgramFormating config,
      AbstractPluginProgramFormating defaultConfig, boolean showTitleSetting,
      boolean showEncodingSetting) {
    super(parent);
    setModalityType(ModalityType.DOCUMENT_MODAL);
    createGui(parent, config, defaultConfig, showTitleSetting, showEncodingSetting);
  }

  private void createGui(Window w, AbstractPluginProgramFormating config, AbstractPluginProgramFormating defaultConfig, boolean showTitleSetting, boolean showEncodingSetting) {
    mConfig = config;
    mDefaultConfig = defaultConfig;

    setTitle(LOCALIZER.msg("settingsFor","Settings for ") + config.getName());
    UiUtilities.registerForClosing(this);

    EnhancedPanelBuilder pb = new EnhancedPanelBuilder(new FormLayout("default,5dlu,default:grow"),(JPanel)getContentPane());
    pb.border(Borders.DIALOG);

    mName = new JLabel(config.getName());
    mSetName = new JButton(LOCALIZER.msg("changeName","Change name"));
    mSetName.addActionListener(this);

    EnhancedPanelBuilder panel = new EnhancedPanelBuilder(new FormLayout("default:grow,5dlu,default","default"));
    panel.add(mName, 1);
    panel.add(mSetName, 3);

    mTitle = new JTextField(config.getTitleValue());
    mContentArea = new JTextArea(config.getContentValue());
    
    Vector<String> encodings = new Vector<String>();
    Map<String, Charset> availcs = Charset.availableCharsets();
    Set<String> keys = availcs.keySet();
    for (String string : keys) {
       encodings.add(string);
    }

    mEncoding = new JComboBox<>(encodings);
    mEncoding.setSelectedItem(config.getEncodingValue());
    mEncoding.addActionListener(this);

    mPreview = new JButton(LOCALIZER.msg("preview","Preview"));
    mPreview.addActionListener(this);

    mSetBack = new JButton(Localizer.getLocalization(Localizer.I18N_DEFAULT));
    mSetBack.addActionListener(this);

    mHelp = new JButton(Localizer.getLocalization(Localizer.I18N_HELP));
    mHelp.addActionListener(this);

    mOk = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    mOk.addActionListener(this);

    mCancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCancel.addActionListener(this);

    EnhancedPanelBuilder buttonPanel = new EnhancedPanelBuilder(new FormLayout("default,3dlu,default,3dlu,default,0dlu:grow,default,3dlu,default","default"));
    buttonPanel.getLayout().setColumnGroups(new int[][] {{1,3,5,7,9}});
    buttonPanel.add(mPreview, 1);
    buttonPanel.add(mSetBack, 3);
    buttonPanel.add(mHelp, 5);
    buttonPanel.add(mOk, 7);
    buttonPanel.add(mCancel, 9);
    
    pb.addLabelRow(false, LOCALIZER.msg("name","Name") + ":", 1);
    pb.add(panel.getPanel(), 3);

    if(showTitleSetting) {
      pb.addLabelRow("2dlu,default",LOCALIZER.msg("title","Titel") + ":", 1);
      pb.add(mTitle, 3);
    }
    
    pb.addLabelRowFull(LOCALIZER.msg("content","Content") + ":");
    pb.addRowFull("fill:default:grow", false, new JScrollPane(mContentArea));

    if(showEncodingSetting) {
      pb.addLabelRow(LOCALIZER.msg("encoding","Encoding") + ":", 1);
      pb.add(mEncoding, 3);
    }
    
    pb.addRowFull(buttonPanel.getPanel());

    UiUtilities.setSize(this, 500, 400);
    setLocationRelativeTo(w);
    setVisible(true);
  }

  public void close() {
    setVisible(false);
    dispose();
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getSource() == mCancel) {
      close();
    } else if(e.getSource() == mPreview) {
      showPreview();
    } else if(e.getSource() == mSetBack) {
      defaultPressed();
    } else if(e.getSource() == mHelp) {
      ParamHelpDialog dialog = new ParamHelpDialog((Window)this, new ParamLibrary());
      dialog.setVisible(true);
    }
    else if(e.getSource() == mSetName) {
      String value = JOptionPane.showInputDialog(this,LOCALIZER.msg("changeName","Change name") + ":",mName.getText());

      if(value != null) {
        mName.setText(value);
      }
    }
    else if(e.getSource() == mOk) {
      mConfig.setName(mName.getText());
      mConfig.setTitleValue(mTitle.getText());
      mConfig.setContentValue(mContentArea.getText());
      mConfig.setEncodingValue(mEncoding.getSelectedItem().toString());

      close();
    }
  }

  /**
   * Show a Preview of the HTML that will be generated
   */
  protected void showPreview() {
    ParamParser parser = new ParamParser();
    String content = parser.analyse(mContentArea.getText(), Plugin.getPluginManager().getExampleProgram());

    if (parser.hasErrors()) {
      content = parser.getErrorString();
    }

    if (content == null) {
      content = "";
    }

    final JDialog dialog = new JDialog(this, LOCALIZER.msg("preview", "Preview"), true);
    JPanel contentPanel = (JPanel) dialog.getContentPane();

    UiUtilities.registerForClosing(new WindowClosingIf() {
      public void close() {
        dialog.setVisible(false);
      }
      public JRootPane getRootPane() {
        return dialog.getRootPane();
      }
    });

    contentPanel.setLayout(new FormLayout("fill:default:grow, pref", "fill:default:grow, 3dlu, pref"));
    contentPanel.setBorder(Borders.DLU4);

    JEditorPane example = new JEditorPane("text", content);
    example.setEditable(false);
    example.setCaretPosition(0);

    CellConstraints cc = new CellConstraints();

    contentPanel.add(new JScrollPane(example), cc.xyw(1, 1, 2));

    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(e -> {
      dialog.setVisible(false);
    });
    dialog.getRootPane().setDefaultButton(ok);

    contentPanel.add(ok, cc.xy(2, 3));

    dialog.setSize(500, 400);
    UiUtilities.centerAndShow(dialog);
  }

  /**
   * Default was pressed.
   * The Settings will be set to default-values after a confirm dialog
   */
  protected void defaultPressed() {
    int ret = JOptionPane.showConfirmDialog(this,
        LOCALIZER.msg("reset", "Reset to default Settings?"),
        Localizer.getLocalization(Localizer.I18N_DEFAULT)+"?", JOptionPane.YES_NO_OPTION);
    if (ret == JOptionPane.YES_OPTION) {
      mTitle.setText(mDefaultConfig.getTitleValue());
      mContentArea.setText(mDefaultConfig.getContentValue());
      mEncoding.setSelectedItem(mDefaultConfig.getEncodingValue());
    }
  }
}
