/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package util.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Plugin;
import devplugin.SettingsItem;
import tvbrowser.core.Settings;
import tvbrowser.ui.settings.MarkingsSettingsTab;
import tvbrowser.ui.settings.SettingsDialog;
import util.i18n.Localizer;


/**
 * A class that is a panel that allows selection of the mark priority for programs.
 *
 * @author René Mach
 * @since 2.5.3
 */
public final class DefaultMarkingPrioritySelectionPanel extends JPanel {
  public static final String TYPE_LABEL = "labeled";
  public static final String TYPE_SELECTABLE = "selectable";
  
  /**
   * default serial version uid.
   */
  private static final long serialVersionUID = 1L;

  /**
   * the localizer for this class.
   */
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(DefaultMarkingPrioritySelectionPanel.class);

  /**
   * the dropdowns for the mark priority selection.
   */
  private ArrayList<Object> mPrioritySelection;

  /**
   * a comment/help text shown below the drop down for the
   * mark prio selection.
   */
  private JEditorPane mHelpLabel;

  /**
   * the seperator for the panel.
   */
  private JComponent mSeparator;

  /**
   * the labels for the dropdowns.
   */
  private ArrayList<JComponent> mLabel;

  /**
   * @param priority which priority is selected in the drop down
   * @param showTitle if true, show the title
   * @param withDefaultDialogBorder if true, use the default border
   */
  private DefaultMarkingPrioritySelectionPanel(final int priority, final boolean showTitle, final boolean withDefaultDialogBorder) {
    this(priority, LOCALIZER.msg("color", "Highlighting color"), showTitle, true, withDefaultDialogBorder);
  }


  /**
   * @param priority which priority is selected in the drop down
   * @param label the label for the drop down
   * @param showTitle if true, show the title
   * @param showHelpLabel if true, show the help text
   * @param withDefaultDialogBorder if true, use the default border
   * @since 3.0
   */
  private DefaultMarkingPrioritySelectionPanel(final int priority, final String label, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder) {
    this(new State(TYPE_LABEL,true), priority, label, showTitle, showHelpLabel, withDefaultDialogBorder, false, true);
  }
  
  /**
   * @param state which {@link State} the selection will have.
   * @param priority which priority is selected in the drop down
   * @param label the label for the drop down
   * @param showTitle if true, show the title
   * @param showHelpLabel if true, show the help text
   * @param withDefaultDialogBorder if true, use the default border
   * @param growingGap if true, gap between label and selection grows
   * @param showNoMarkingPriority if true the selection contains an entry to select no marking
   * @since 4.2.5
   */
  private DefaultMarkingPrioritySelectionPanel(final State state, final int priority, final String label, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder, final boolean growingGap, final boolean showNoMarkingPriority) {
    this(new State[] {state}, new int[] {priority}, new String[] {label}, showTitle, showHelpLabel, withDefaultDialogBorder, growingGap, showNoMarkingPriority);
  }

  /**
   * the arrays for label and priority must have the same length. the index of
   * both arrays must be in the range of an integer. both indexes must be > 0.
   *
   * @param priority which priority is selected in the dropdowns. must not be null.
   * @param label the labels for the dropdowns. must not be null.
   * @param showTitle if true, show the title
   * @param showHelpLabel if true, show the help text
   * @param withDefaultDialogBorder if true, use the default border
   * @since 3.0
   */
  private DefaultMarkingPrioritySelectionPanel(final int[] priority, final String[] label, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder) {
    this(getStatesDefault(label.length), priority, label, showTitle, showHelpLabel, withDefaultDialogBorder, false, true);
  }
  
  private static State[] getStatesDefault(int length) {
    State[] states = new State[length];
    
    Arrays.fill(states, new State(TYPE_LABEL,true));

    return states;
  }

  /**
   * the arrays for label and priority must have the same length. the index of
   * both arrays must be in the range of an integer. both indexes must be > 0.
   *
   * @param states which {@link State} the selection will have.
   * @param priority which priority is selected in the dropdowns. must not be null.
   * @param label the labels for the dropdowns. must not be null.
   * @param showTitle if true, show the title
   * @param showHelpLabel if true, show the help text
   * @param withDefaultDialogBorder if true, use the default border
   * @param growingGap if true, gap between label and selection grows
   * @param showNoMarkingPriority if true the selection contains an entry to select no marking
   * @since 4.2.5
   */
  private DefaultMarkingPrioritySelectionPanel(final State[] states, final int[] priority, final String[] label, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder, final boolean growingGap, final boolean showNoMarkingPriority) {
    try {
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder(new FormLayout("5dlu,default,default,0dlu:grow"),this);
    
    if(growingGap) {
      pb.getLayout().insertColumn(3, ColumnSpec.decode("5dlu:grow"));
    }
    else {
      pb.getLayout().insertColumn(3, ColumnSpec.decode("5dlu"));
    }
    
    //how many selectors do we have to draw?
    int choosersToDraw = Math.min(priority.length, label.length);

    if (withDefaultDialogBorder) {
      pb.border(Borders.DIALOG);
    }

    //init the components
    mLabel = new ArrayList<JComponent>();
    mPrioritySelection = new ArrayList<>();// JComboBox[choosersToDraw];

    //add all the sub components to this panel
    if (showTitle) {
      mSeparator = pb.addSeparatorRowFull(getTitle());
    }

    for (int i = 0; i < choosersToDraw; i++) {
      final JComboBox<Object> box = new JComboBox<>();
      
      final String[] names = getMarkingColorNames(showNoMarkingPriority);
      
      for(int j = 0; j < names.length; j++) {
        box.addItem(new PriortiyLabel(j + (showNoMarkingPriority ? -1 : 0), names[j]));
      }
      
      final State state = states[i];
      
      if(state.mType.equals(TYPE_SELECTABLE)) {
        mLabel.add(new JCheckBox(label[i], state.mActivated));
        box.setEnabled(state.mActivated);
        ((JCheckBox)mLabel.get(i)).addItemListener(e -> {
          box.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
          state.mActivated = box.isEnabled();
        });
        
        pb.addRow(mLabel.get(i), 2);
      }
      else {
        mLabel.add(pb.addLabelRow(label[i], 2));
      }
      
      mPrioritySelection.add(box);
      box.setSelectedIndex(Math.min(priority[i],Settings.getHighlightingPriorityMaximum()) + (showNoMarkingPriority ? 1 : 0));
      box.setRenderer(new MarkPriorityComboBoxRenderer(box.getRenderer()));
      
      pb.add(box, 4);
    }

    if (showHelpLabel) {
      mHelpLabel = UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("help", "The selected higlighting color is only shown if the program is higlighted by this plugin only or if the other higlightings have a lower or the same priority. The higlighting colors of the priorities can be changed in the <a href=\"#link\">higlighting settings</a>."), e -> {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.PROGRAMPANELMARKING);
        }
      });
      mHelpLabel.setMaximumSize(new Dimension(Sizes.dialogUnitXAsPixel(200, mHelpLabel), Sizes.dialogUnitXAsPixel(600, mHelpLabel)));
      
      pb.addRowFull(mHelpLabel, 2);      
    }
    }catch(Throwable t) {t.printStackTrace();}
  }

  /**
   * Creates an instance of this class.
   *
   * @param priority The current selected priority.
   * @param showTitle If the title should be shown.
   * @param withDefaultDialogBorder If the panel should show the default dialog border of FormLayouts PanelBuilder.
   * @return The created instance of this class.
   */
  public static DefaultMarkingPrioritySelectionPanel createPanel(final int priority, final boolean showTitle, final boolean withDefaultDialogBorder) {
    return new DefaultMarkingPrioritySelectionPanel(priority, showTitle, withDefaultDialogBorder);
  }

  /**
   * Creates an instance of this class.
   *
   * @param priority which priority is selected in the drop down
   * @param label the label for the drop down
   * @param showTitle if true, show the title
   * @param showHelpLabel if true, show the help text
   * @param withDefaultDialogBorder if true, use the default border
   * @return The created instance of this class.
   * @since 3.0
   */
  public static DefaultMarkingPrioritySelectionPanel createPanel(final int priority, final String label, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder) {
    return new DefaultMarkingPrioritySelectionPanel(priority, label, showTitle, showHelpLabel, withDefaultDialogBorder);
  }
  
  /**
   * Creates an instance of this class.
   *
   * @param state which {@link State} the selection will have.
   * @param priority which priority is selected in the drop down
   * @param label the label for the drop down
   * @param showTitle if true, show the title
   * @param showHelpLabel if true, show the help text
   * @param withDefaultDialogBorder if true, use the default border
   * @return The created instance of this class.
   * @param growingGap if true, gap between label and selection grows
   * @param showNoMarkingPriority if true the selection contains an entry to select no marking
   * @since 4.2.5
   */
  public static DefaultMarkingPrioritySelectionPanel createPanel(final State state, final int priority, final String label, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder, final boolean growingGap, final boolean showNoMarkingPriority) {
    return new DefaultMarkingPrioritySelectionPanel(state, priority, label, showTitle, showHelpLabel, withDefaultDialogBorder, growingGap, showNoMarkingPriority);
  }

  /**
   * the arrays for label and priority must have the same length. the index of
   * both arrays must be in the range of an integer. both indexes must be &gt; 0.
   *
   * @param states which {@link State} the selections will have.
   * @param priorities which priority is selected in the dropdowns. must not be null.
   * @param labels the labels for the dropdowns. must not be null.
   * @param showTitle if true, show the title
   * @param showHelpLabel if true, show the help text
   * @param withDefaultDialogBorder if true, use the default border
   * @return The created instance of this class.
   * @param growingGap if true, gap between label and selection grows
   * @param showNoMarkingPriority if true the selection contains an entry to select no marking
   * @since 4.2.5
   */
  public static DefaultMarkingPrioritySelectionPanel createPanel(final State[] states, final int[] priorities, final String[] labels, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder, final boolean growingGap, final boolean showNoMarkingPriority) {
    return new DefaultMarkingPrioritySelectionPanel(states, priorities, labels, showTitle, showHelpLabel, withDefaultDialogBorder, growingGap, showNoMarkingPriority);
  }
  
  /**
   * the arrays for label and priority must have the same length. the index of
   * both arrays must be in the range of an integer. both indexes must be &gt; 0.
   *
   * @param priority which priority is selected in the dropdowns. must not be null.
   * @param label the labels for the dropdowns. must not be null.
   * @param showTitle if true, show the title
   * @param showHelpLabel if true, show the help text
   * @param withDefaultDialogBorder if true, use the default border
   * @return The created instance of this class.
   * @since 3.0
   */
  public static DefaultMarkingPrioritySelectionPanel createPanel(final int[] priority, final String[] label, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder) {
    return new DefaultMarkingPrioritySelectionPanel(priority, label, showTitle, showHelpLabel, withDefaultDialogBorder);
  }

  /**
   * @return The selected marking priority of the first dropdown
   */
  public int getSelectedPriority() {
    return getSelectedPriority(0);
  }

  /**
   * @param index the index of the dropdown
   * @return The selected marking priority of the dropdown with the given index
   */
  @SuppressWarnings("unchecked")
  public int getSelectedPriority(final int index) {
    return ((PriortiyLabel)((JComboBox<Object>)mPrioritySelection.get(index)).getSelectedItem()).getPriority();
  }

  /**
   * @return The selected marking priorities of all dropdowns
   */
  public int[] getSelectedPriorities() {
    int[] prios = new int[mPrioritySelection.size()];
    for (int i = 0; i < mPrioritySelection.size(); i++)
    {
      prios[i] = getSelectedPriority(i);
    }
    return prios;
  }

  /**
   * Gets the title of this settings panel.
   *
   * @return The title of this settings panel.
   */
  public static String getTitle() {
    return LOCALIZER.msg("title", "Highlighting");
  }

  /**
   * Gets the name of the marking colors in an array sorted from the lowest to the highest priority.
   * <p>
   * @param withNoMarkPriority If the array should contain the no mark priority name.
   * @return The names of the marking colors in an array sorted from the lowest to the highest priority.
   * @since 2.7
   */
  public static String[] getMarkingColorNames(final boolean withNoMarkPriority) {
    final String[] colors = new String[Settings.getHighlightingPriorityMaximum() + (withNoMarkPriority ? 2 : 1)];
    int offset = withNoMarkPriority ? 0 : 1;
    int i = 0;
    
    if (withNoMarkPriority) {
      colors[i++] = MarkingsSettingsTab.LOCALIZER.msg("color.noPriority", "Don't highlight");
    }
    
    for(;i < colors.length; i++) {
      colors[i] = (i+offset) +". "+ MarkingsSettingsTab.LOCALIZER.msg("color.colorPriority","Color/priority");
    }
    
    if(withNoMarkPriority) {
      i = 1;
    }
    else {
      i = 0;
    }
    
    if(colors.length > 2 || (!withNoMarkPriority && colors.length > 1)) {
      colors[i] += MarkingsSettingsTab.LOCALIZER.msg("color.colorPriority.min"," (minimum)");
      colors[colors.length-1] += MarkingsSettingsTab.LOCALIZER.msg("color.colorPriority.max"," (maximum)");
    }
    
    return colors;
  }

  /**
   * this enables the panel and all its subcomponents.
   * @param enabled true to enable this panel, false otherwise
   * @see javax.swing.JComponent#setEnabled(boolean)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void setEnabled(final boolean enabled) {
    if (mSeparator != null) {
      mSeparator.setEnabled(enabled);
    }
    if (mHelpLabel != null)
    {
      mHelpLabel.setEnabled(enabled);
    }
    for (int i = 0; i < mLabel.size(); i++)
    {
      mLabel.get(i).setEnabled(enabled);
      ((JComboBox<Object>)mPrioritySelection.get(i)).setEnabled(enabled && (!(mLabel.get(i) instanceof JCheckBox) || ((JCheckBox)mLabel.get(i)).isSelected()));
    }
  }
  
  /**
   * @author René Mach
   * @since 4.2.5
   */
  public static final class State {
    private String mType;
    private boolean mActivated;
    
    /**
     * State of an selection entry.
     * 
     * @param type The type for this State.
     * @param activated <code>true</code> if the associated selection entry is activated,
     * <code>false</code> if not.
     * NOTE: Activation state for {@link DefaultMarkingPrioritySelectionPanel#TYPE_LABEL} will always be <code>true</code>.
     */
    public State(final String type, final boolean activated) {
      mType = type;
      mActivated = activated;
    }
    
    /**
     * @return If the associated selection entry is activated.
     * NOTE: Activation state for {@link DefaultMarkingPrioritySelectionPanel#TYPE_LABEL} will always be <code>true</code>.
     */
    public boolean isActivated() {
      return mType.equals(TYPE_SELECTABLE) ? mActivated : true;
    }
  }
  
  public static final class PriortiyLabel {
    private final int mPriority;
    private final String mLabel;
    
    public PriortiyLabel(final int priority, final String label) {
      mPriority = priority;
      mLabel = label;
    }
    
    public int getPriority() {
      return mPriority;
    }
    
    public Color getColor() {
      return Plugin.getPluginManager().getTvBrowserSettings().getColorForMarkingPriority(mPriority);
    }
    
    @Override
    public String toString() {
      return mLabel;
    }
  }
}
