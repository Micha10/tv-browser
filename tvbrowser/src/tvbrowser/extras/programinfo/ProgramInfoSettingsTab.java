package tvbrowser.extras.programinfo;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.swing.plaf.LookAndFeelAddons;

import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.i18n.Localizer;
import util.program.ProgramTextCreator;
import util.ui.EnhancedPanelBuilder;
import util.ui.FontChooserPanel;
import util.ui.OrderChooser;
import util.ui.PluginsPictureSettingsPanel;
import util.ui.ScrollableJPanel;

/**
 * The order settings for the ProgramInfo.
 *
 * @author René Mach
 *
 */
public class ProgramInfoSettingsTab implements SettingsTab {

  private OrderChooser<Object> mList;
  private Object[] mOldOrder;
  private JCheckBox mShowShortDescriptionOnlyWhenNoDescription;
  
  private boolean mOldSetupState;
  private PluginsPictureSettingsPanel mPictureSettings;

  private JCheckBox mZoomEnabled;
  private JSpinner mZoomValue;

  private JCheckBox mUserFont, mAntiAliasing;
  private FontChooserPanel mTitleFont, mBodyFont;

  private String mOldTitleFont, mOldBodyFont;
  private int mOldTitleFontSize, mOldBodyFontSize;
  private boolean mOldUserFontSelected, mOldAntiAliasingSelected;

  private boolean mOldShowFunctions;

  private String mOldLook;

  private JComboBox<String> mLook;
  
  private static int mCurrentTab = 0;
  private JTabbedPane mTabbedPane;

  private String[] mLf = {
      "com.l2fprod.common.swing.plaf.aqua.AquaLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.metal.MetalLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.motif.MotifLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.windows.WindowsLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.windows.WindowsClassicLookAndFeelAddons"
  };

  private JCheckBox mShowFunctions, mShowTextSearchButton;

  private ButtonGroup mAvailableTargetGroup;
  private JCheckBox mPersonSearchCB;
  private JCheckBox mHighlight;
  private ColorLabel mHighlightColorLb;
  private ColorButton mHighlightButton;
  private int mOldTitleStyle;
  private int mOldBodyStyle;

  public JPanel createSettingsPanel() {
    final ProgramInfoSettings settings = ProgramInfo.getInstance()
        .getSettings();
    mOldAntiAliasingSelected = settings.getAntialiasing();
    mOldUserFontSelected = settings.getUserFont();
    mOldTitleFontSize = settings.getTitleFontSize();
    mOldBodyFontSize = settings.getBodyFontSize();
    mOldTitleFont = settings.getTitleFontName();
    mOldBodyFont = settings.getBodyFontName();
    mOldTitleStyle = settings.getTitleFontStyle();
    mOldBodyStyle = settings.getBodyFontStyle();

    mAntiAliasing = new JCheckBox(ProgramInfo.LOCALIZER
        .msg("antialiasing", "Antialiasing"));
    mAntiAliasing.setSelected(mOldAntiAliasingSelected);

    mUserFont = new JCheckBox(ProgramInfo.LOCALIZER.msg("userfont", "Use user fonts"));
    mUserFont.setSelected(mOldUserFontSelected);

    mTitleFont = new FontChooserPanel(null, new Font(mOldTitleFont, mOldTitleStyle, mOldTitleFontSize), true);
    mTitleFont.setBorder(BorderFactory.createEmptyBorder());
//    mTitleFont.setMaximumSize(mTitleFont.getPreferredSize());
    mTitleFont.setAlignmentX(Component.LEFT_ALIGNMENT);

    mBodyFont = new FontChooserPanel(null, new Font(mOldBodyFont, mOldBodyStyle, mOldBodyFontSize), true);
    mBodyFont.setBorder(BorderFactory.createEmptyBorder());
//    mBodyFont.setMaximumSize(mBodyFont.getPreferredSize());
    mBodyFont.setAlignmentX(Component.LEFT_ALIGNMENT);

    mTitleFont.setEnabled(mUserFont.isSelected());
    mBodyFont.setEnabled(mUserFont.isSelected());

    mOldLook = settings.getLook();

    final String[] lf = { "Aqua", "Metal", "Motif", "Windows XP", "Windows Classic" };

    mLook = new JComboBox<>(lf);

    String look = mOldLook.length() > 0 ? mOldLook : LookAndFeelAddons.getBestMatchAddonClassName();

    for(int i = 0; i < mLf.length; i++) {
      if(look.toLowerCase().indexOf(mLf[i].toLowerCase()) != -1) {
        mLook.setSelectedIndex(i);
        break;
      }
    }

    mOldShowFunctions = settings.getShowFunctions();

    mShowFunctions = new JCheckBox(ProgramInfo.LOCALIZER.msg("showFunctions",
        "Show Functions"), settings.getShowFunctions());
    mShowTextSearchButton = new JCheckBox(ProgramInfo.LOCALIZER.msg(
        "showTextSearchButton", "Show \"Search in program\""), ProgramInfo
        .getInstance().getSettings().getShowSearchButton());

    mShowTextSearchButton.setEnabled(mShowFunctions.isSelected());

    mShowFunctions.addActionListener(e -> {
      mShowTextSearchButton.setEnabled(mShowFunctions.isSelected());
    });

    mHighlight = new JCheckBox(ProgramInfoDialog.mLocalizer.msg("highlight", "Highlight favorite matches"), settings.getHighlightFavorite());
    mHighlight.addActionListener(e -> {
      mHighlightColorLb.setEnabled(mHighlight.isSelected());
      mHighlightButton.setEnabled(mHighlight.isSelected());
    });

    EnhancedPanelBuilder formatPanel = new EnhancedPanelBuilder(new FormLayout("5dlu,10dlu,pref,pref,5dlu,default:grow,pref,5dlu"));
    formatPanel.border(Borders.DIALOG);
    formatPanel.addParagraph(ProgramInfo.LOCALIZER.msg("font","Font settings"));
    formatPanel.addRowFull(mAntiAliasing, 2);
    formatPanel.addRowFull(mUserFont, 2);
    
    final JLabel titleLabel = formatPanel.addLabelRow(ProgramInfo.LOCALIZER.msg("title", "Title font"), 3);
    formatPanel.add(mTitleFont, 6, 2);
    
    final JLabel bodyLabel = formatPanel.addLabelRow(ProgramInfo.LOCALIZER.msg("body", "Description font"), 3);
    formatPanel.add(mBodyFont, 6, 2);

    mUserFont.addChangeListener(e -> {
      mTitleFont.setEnabled(mUserFont.isSelected());
      mBodyFont.setEnabled(mUserFont.isSelected());
      titleLabel.setEnabled(mUserFont.isSelected());
      bodyLabel.setEnabled(mUserFont.isSelected());
    });

    mTitleFont.setEnabled(mUserFont.isSelected());
    mBodyFont.setEnabled(mUserFont.isSelected());
    titleLabel.setEnabled(mUserFont.isSelected());
    bodyLabel.setEnabled(mUserFont.isSelected());

    formatPanel.addParagraph(ProgramInfo.LOCALIZER.msg("design","Design"));
    formatPanel.addRow(mLook, 2, 2);

    formatPanel.addParagraph(ProgramInfoDialog.mLocalizer.msg("functions","Functions"));
    formatPanel.addRow(mShowFunctions, 2, formatPanel.getColumnCount() - 2);
    formatPanel.addRow(mShowTextSearchButton, 3, formatPanel.getColumnCount() - 3);

    formatPanel.addParagraph(ProgramInfo.LOCALIZER.msg("favorites","Favorites"));
    formatPanel.addRow(mHighlight, 2, 5);
    JPanel panel = new JPanel(new FlowLayout());
    mHighlightColorLb = new ColorLabel(settings.getHighlightColor());
    panel.add(mHighlightColorLb);
    mHighlightColorLb.setStandardColor(settings.getHighlightColor());
    mHighlightButton = new ColorButton(mHighlightColorLb);
    panel.add(mHighlightButton);
    mHighlight.getActionListeners()[0].actionPerformed(null);
    formatPanel.add(panel, 7);

    mOldOrder = settings.getFieldOrder();
    mOldSetupState = ProgramInfo.getInstance().getSettings().getSetupwasdone();

    mList = new OrderChooser<>(mOldOrder, ProgramTextCreator.getDefaultOrderWithActivatedPluginInfo(),true);
    mShowShortDescriptionOnlyWhenNoDescription = new JCheckBox(ProgramInfo.LOCALIZER.msg("showShortDescriptionOnlyWhenNoDescription", "Show short description only, if no long description exists"), settings.getShowShortDescriptionOnlyWithoutDescription());
    
    JButton previewBtn = new JButton(ProgramInfo.LOCALIZER.msg("preview", "Preview"));
    previewBtn.addActionListener(e -> {
      saveSettings();
      ProgramInfo.getInstance().showProgramInformation(
          Plugin.getPluginManager().getExampleProgram(), false);
      restoreSettings();
    });

    JButton defaultBtn = new JButton(ProgramInfo.LOCALIZER.msg("default", "Default"));
    defaultBtn.addActionListener(e -> {
      resetSettings();
    });

    EnhancedPanelBuilder orderPanel = new EnhancedPanelBuilder("default:grow");
    orderPanel.border(Borders.DIALOG);
    orderPanel.addRowFull(mShowShortDescriptionOnlyWhenNoDescription);
    orderPanel.addGrowingRowFull(mList);

    EnhancedPanelBuilder picturePanel = new EnhancedPanelBuilder("default:grow");
    picturePanel.border(Borders.DIALOG);
    picturePanel.addRowFull(mPictureSettings = new PluginsPictureSettingsPanel(ProgramInfo.getInstance().getPictureSettings(),false));

    EnhancedPanelBuilder pb = new EnhancedPanelBuilder(new FormLayout("default,2dlu,default,5dlu,default","default"));

    pb.add(mZoomEnabled = new JCheckBox(ProgramInfo.LOCALIZER.msg(
        "scaleImage", "Scale picture:"), ProgramInfo.getInstance()
        .getSettings().getZoomEnabled()), 1);
    pb.add(mZoomValue = new JSpinner(new SpinnerNumberModel(ProgramInfo
        .getInstance().getSettings().getZoomValue(), 50, 300, 1)), 3);
    final JLabel label = pb.labelAdd("%", 5);

    mZoomEnabled.addItemListener(e -> {
      mZoomValue.setEnabled(mZoomEnabled.isSelected());
      label.setEnabled(mZoomEnabled.isSelected());
    });

    mZoomValue.setEnabled(mZoomEnabled.isSelected());
    label.setEnabled(mZoomEnabled.isSelected());

    picturePanel.addRowFull(pb.getPanel());

    PluginAccess webPlugin = PluginManagerImpl.getInstance().getActivatedPluginForId("java.webplugin.WebPlugin");

    mAvailableTargetGroup = new ButtonGroup();

    final ArrayList<InternalRadioButton<?>> availableDefaultTargets = new ArrayList<InternalRadioButton<?>>();

    availableDefaultTargets.add(new InternalRadioButton<String>(ProgramInfoDialog.mLocalizer.msg("searchTvBrowser","Search in TV-Browser")));
    mAvailableTargetGroup.add(availableDefaultTargets.get(0));
    availableDefaultTargets.add(new InternalRadioButton<String>(ProgramInfoDialog.mLocalizer.msg("searchWikipedia","Search in Wikipedia")));
    mAvailableTargetGroup.add(availableDefaultTargets.get(1));

    final String currentValue = settings.getActorSearch();

    int selectedIndex = -1;

    if(webPlugin != null && webPlugin.canReceiveProgramsWithTarget()) {
      ProgramReceiveTarget[] targets = webPlugin.getProgramReceiveTargets();

      if(targets != null) {
        for(ProgramReceiveTarget target : targets) {
          availableDefaultTargets.add(new InternalRadioButton<ProgramReceiveTarget>(target));
          mAvailableTargetGroup.add(availableDefaultTargets.get(availableDefaultTargets.size()-1));

          if(currentValue.equals(target.getReceiveIfId() + "#_#_#" + target.getTargetId())) {
            selectedIndex = availableDefaultTargets.size()-1;
          }
        }
      }
    }

    if(selectedIndex == -1) {
      if(currentValue.equals("internalSearch")) {
        selectedIndex = 0;
      }
      else {
        selectedIndex = 1;
      }
    }

    availableDefaultTargets.get(selectedIndex).setSelected(true);

    ScrollableJPanel buttonPanel = new ScrollableJPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
    buttonPanel.setOpaque(false);

    for(InternalRadioButton<?> button : availableDefaultTargets) {
      buttonPanel.add(button);
    }

    final JScrollPane scrollPane = new JScrollPane(buttonPanel);
    scrollPane.setBackground(UIManager.getDefaults().getColor("List.background"));
    scrollPane.getViewport().setBackground(UIManager.getDefaults().getColor("List.background"));
    EnhancedPanelBuilder actorPanel = new EnhancedPanelBuilder(new FormLayout("default:grow"));
    actorPanel.border(Borders.DIALOG);

    mPersonSearchCB = new JCheckBox(ProgramInfo.LOCALIZER.msg("enableSearch", "Show person names as links to person search"));
    actorPanel.addRowFull(mPersonSearchCB);
    
    final JLabel searchLabel = actorPanel.addLabelRow("3dlu,default",ProgramInfo.LOCALIZER.msg("defaultActorSearchMethod", "Default search method:"));
    actorPanel.addRowFull("1dlu,fill:default:grow",scrollPane);

    mPersonSearchCB.addActionListener(e -> {
      scrollPane.setEnabled(mPersonSearchCB.isSelected());
      searchLabel.setEnabled(mPersonSearchCB.isSelected());
      for (InternalRadioButton<?> button : availableDefaultTargets) {
        button.setEnabled(mPersonSearchCB.isSelected());
      }
    });
    mPersonSearchCB.setSelected(settings.getEnableSearch());
    mPersonSearchCB.getActionListeners()[0].actionPerformed(null);
    
    mTabbedPane = new JTabbedPane();
    mTabbedPane.add(ProgramInfo.LOCALIZER.msg("look","Look"), formatPanel.getPanel());
    mTabbedPane.add(ProgramInfo.LOCALIZER.msg("fields","Fields"), orderPanel.getPanel());
    mTabbedPane.add(Localizer.getLocalization(Localizer.I18N_PICTURES), picturePanel.getPanel());
    mTabbedPane.add(ProgramInfo.LOCALIZER.msg("actorSearch","Actor search"), actorPanel.getPanel());
    mTabbedPane.setSelectedIndex(mCurrentTab);

    formatPanel.getPanel().setOpaque(true);
    orderPanel.getPanel().setOpaque(true);
    picturePanel.getPanel().setOpaque(true);
    actorPanel.getPanel().setOpaque(true);
    
    FormLayout layout = new FormLayout("default,default:grow,default","pref");
    layout.setColumnGroups(new int[][] {{1,3}});
    JPanel buttonPn = new JPanel(layout);
    buttonPn.add(previewBtn, CC.xy(3,1));
    buttonPn.add(defaultBtn, CC.xy(1,1));

    JPanel base = new JPanel(new FormLayout("default:grow","fill:default:grow,10dlu,default"));
    base.setBorder(Borders.DIALOG);
    base.add(mTabbedPane, CC.xy(1,1));
    base.add(buttonPn, CC.xy(1,3));

    return base;
  }
  
  

  private void resetSettings() {
    mList.setOrder(ProgramTextCreator.getDefaultOrderWithActivatedPluginInfo(),ProgramTextCreator.getDefaultOrderWithActivatedPluginInfo());
    mAntiAliasing.setSelected(false);
    mUserFont.setSelected(false);

    mZoomEnabled.setSelected(false);
    mZoomValue.setValue(100);

    String look = LookAndFeelAddons.getBestMatchAddonClassName();

    for(int i = 0; i < mLf.length; i++) {
      if(look.toLowerCase().indexOf(mLf[i].toLowerCase()) != -1) {
        mLook.setSelectedIndex(i);
        break;
      }
    }
  }

  public void saveSettings() {
    mCurrentTab = mTabbedPane.getSelectedIndex();
    try {
      final ProgramInfoSettings settings = ProgramInfo.getInstance().getSettings();
      settings.setZoomEnabled(mZoomEnabled.isSelected());
      settings.setZoomValue((Integer) mZoomValue.getValue());
      
      settings.setShowShortDescriptionOnlyWithoutDescription(mShowShortDescriptionOnlyWhenNoDescription.isSelected());
      final List<Object> order = mList.getOrderList();
      
      settings.setFieldOrder(order.toArray(new Object[order.size()]));
      settings.setSetupwasdone(true);
      settings.setPictureSettings(mPictureSettings.getSettings().getType());

      ProgramInfo.getInstance().setOrder();

      settings.setAntialiasing(mAntiAliasing.isSelected());
      settings.setUserFont(mUserFont.isSelected());

      Font f = mTitleFont.getChosenFont();
      settings.setTitleFontName(f.getFamily());
      settings.setTitleFontSize(f.getSize());
      settings.setTitleFontStyle(f.getStyle());

      f = mBodyFont.getChosenFont();
      settings.setBodyFontName(f.getFamily());
      settings.setBodyFontSize(f.getSize());
      settings.setBodyFontStyle(f.getStyle());

      settings.setLook(mLf[mLook.getSelectedIndex()]);
      ProgramInfo.getInstance().setLook();

      if (mShowFunctions != null) {
        settings.setShowFunctions(mShowFunctions.isSelected());
        if (mShowFunctions.isSelected() != mOldShowFunctions) {
          ProgramInfoDialog.recreateInstance();
        }
      }
      if (mShowTextSearchButton != null) {
        settings.setShowSearchButton(mShowTextSearchButton.isSelected());
      }
      settings.setHighlightFavorite(mHighlight.isSelected());
      settings.setHighlightColor(mHighlightColorLb.getColor());

      Enumeration<AbstractButton> actorSearchDefault = mAvailableTargetGroup.getElements();

      while (actorSearchDefault.hasMoreElements()) {
        AbstractButton button = actorSearchDefault.nextElement();

        if (button.isSelected()) {
          settings.setActorSearch(((InternalRadioButton<?>) button).getValue());
          break;
        }
      }
      settings.setEnableSearch(mPersonSearchCB.isSelected());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void restoreSettings() {
    final ProgramInfoSettings settings = ProgramInfo.getInstance()
        .getSettings();
    settings.setSetupwasdone(mOldSetupState);
    settings.setFieldOrder(mOldOrder);
    ProgramInfo.getInstance().setOrder();

    settings.setAntialiasing(mOldAntiAliasingSelected);
    settings.setUserFont(mOldUserFontSelected);
    settings.setTitleFontName(mOldTitleFont);
    settings.setTitleFontSize(mOldTitleFontSize);
    settings.setBodyFontName(mOldBodyFont);
    settings.setBodyFontSize(mOldBodyFontSize);
    settings.setTitleFontStyle(mOldTitleStyle);
    settings.setBodyFontStyle(mOldBodyStyle);

    settings.setLook(mOldLook);
    ProgramInfo.getInstance().setLook();

    settings.setShowFunctions(mOldShowFunctions);

    ProgramInfoDialog.recreateInstance();
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("actions", "edit-find", 16);
  }

  public String getTitle() {
    return ProgramInfo.getInstance().toString();
  }

  private static class InternalRadioButton<T> extends JRadioButton {
    private T mValue;

    protected InternalRadioButton(T value) {
      super(value.toString());

      mValue = value;
      setOpaque(false);
    }

    protected String getValue() {
      if(mValue instanceof String) {
        if(mValue.equals(ProgramInfoDialog.mLocalizer.msg("searchTvBrowser","Search in TV-Browser"))) {
          return "internalSearch";
        }
        else {
          return "internalWikipedia";
        }
      }
      else if (mValue instanceof ProgramReceiveTarget) {
        ProgramReceiveTarget target = (ProgramReceiveTarget)mValue;
        return target.getReceiveIfId() + "#_#_#" + target.getTargetId();
      }

      return "internalWikipedia";
    }
  }
}
