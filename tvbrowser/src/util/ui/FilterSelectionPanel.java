package util.ui;

import java.awt.event.ItemEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.PluginsProgramFilter;
import devplugin.ProgramFilter;
import tvbrowser.core.filters.Excludable;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.ui.filter.dlgs.EditFilterDlg;
import tvbrowser.ui.filter.dlgs.FilterTreeModel;
import tvbrowser.ui.mainframe.MainFrame;
import util.i18n.Localizer;

/**
 * A class with a filter selection drop down and an edit button.
 * 
 * @author René Mach
 * @since 4.2.4
 */
public final class FilterSelectionPanel extends JPanel {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(FilterSelectionPanel.class);
  private JLabel mLabel;
  private WideComboBox<WrapperFilter> mFilterBox;
  private JButton mEdit;
  private WrapperFilter mNewFilter;
  private WrapperFilter mLastSelectedFilter;
  
  public static String getNewFilterName() {
    return LOCALIZER.ellipsisMsg("createFilter","Create new filter");
  }
  
  private static String getLineSpec(final boolean hasLabel, final boolean grow) {
    StringBuilder b = new StringBuilder();
    
    if(hasLabel) {
      b.append("default,2dlu,");
    }
    else {
      b.append("0dlu,0dlu,");
    }
    
    b.append("default");
    
    if(grow) {
      b.append(":grow");
    }
    
    b.append(",2dlu,default");
    
    return b.toString();
  }
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param selectedFilter The filter to be selected at first.
   */
  public FilterSelectionPanel(final ProgramFilter selectedFilter) {
    this(null, selectedFilter, true);
  }
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param label The text for the label of the filter.
   * @param selectedFilter The filter to be selected at first.
   * @param showEditButton <code>true</code> if the edit button should be shown.
   */
  public FilterSelectionPanel(final String label, ProgramFilter selectedFilter, final boolean showEditButton) {
    this(label, selectedFilter, showEditButton, false);
  }

  /**
   * Creates an instance of this class.
   * <p>
   * @param label The text for the label of the filter.
   * @param selectedFilter The filter to be selected at first.
   * @param showEditButton <code>true</code> if the edit button should be shown.
   * @param grow If the selection JComboBox should grow with the width of the component.
   * @param exclusions The classes implementing ProgramFilter or FilterComponent that should be excluded
   * from the list or <code>null</code> if there are no exclusions.
   * @since 4.2.5
   */
  @SafeVarargs
  public FilterSelectionPanel(final String label, ProgramFilter selectedFilter, final boolean showEditButton, final boolean grow, final Class<? extends Excludable>... exclusions) {
    this(label, selectedFilter, showEditButton, grow, false, false, exclusions);
  }
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param label The text for the label of the filter.
   * @param selectedFilter The filter to be selected at first.
   * @param showEditButton <code>true</code> if the edit button should be shown.
   * @param grow If the selection JComboBox should grow with the width of the component.
   * @param showHighlighting If the highlighting panel should be visible
   * @param showDeleteButton If the delete button shoudl be visible
   * @param exclusions The classes implementing ProgramFilter or FilterComponent that should be excluded
   * from the list or <code>null</code> if there are no exclusions.
   * @since 4.2.8
   */
  @SafeVarargs
  public FilterSelectionPanel(final String label, ProgramFilter selectedFilter, final boolean showEditButton, final boolean grow, final boolean showHighlighting, final boolean showDeleteButton, final Class<? extends Excludable>... exclusions) {
    setLayout(new FormLayout(getLineSpec(label == null || !label.isBlank(),grow),"default"));
    
    mFilterBox = new WideComboBox<>();
    mNewFilter = new WrapperFilter(new UserFilter(getNewFilterName()));
    
    if(selectedFilter == null) {
      selectedFilter = FilterManagerImpl.getInstance().getDefaultFilter();
    }
    
    if(selectedFilter == null) {
      selectedFilter = FilterManagerImpl.getInstance().getAllFilter();
    }
    
    final ProgramFilter[] filters = FilterManagerImpl.getInstance().getAvailableFilters();
    
    for(ProgramFilter filter : filters) {
      if(!isExcluded(filter, exclusions)) {
        final WrapperFilter wrapper = new WrapperFilter(filter);
        
        mFilterBox.addItem(wrapper);
        
        if(filter.getName().equals(selectedFilter.getName())) {
          mFilterBox.setSelectedItem(wrapper);
        }
      }
    }
    
    mFilterBox.addItem(mNewFilter);
    
    add(mLabel = new JLabel(label != null ? label : LOCALIZER.msg("filterLabel", "Filter:")), CC.xy(1, 1));
    add(mFilterBox, CC.xy(3, 1));
    
    if(showEditButton) {
      mEdit = new JButton(Localizer.getLocalization(Localizer.I18N_EDIT),TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
      mEdit.setEnabled(selectedFilter instanceof UserFilter);
      mEdit.addActionListener(e -> {try {
        UserFilter filter = (UserFilter)((WrapperFilter)mFilterBox.getSelectedItem()).getFilter();
        boolean filterNew = mFilterBox.getSelectedItem().equals(mNewFilter);
        
        if(filterNew) {
          filter = new UserFilter("");
        }
        
        EditFilterDlg dlg = new EditFilterDlg(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), FilterList.getInstance(), filter, showHighlighting, showDeleteButton);
        
        if(dlg.getOkWasPressed()) {
          if(filterNew) {
            FilterTreeModel.getInstance().addFilter(filter);
            FilterList.getInstance().store();
            
            if(!isExcluded(filter, exclusions)) {
              final WrapperFilter wrapper = new WrapperFilter(filter);
              mFilterBox.insertItemAt(wrapper, mFilterBox.getItemCount()-1);
              mFilterBox.setSelectedItem(wrapper);
            }
          }
          else {
            FilterTreeModel.getInstance().fireFilterTouched(filter);
          }
          
          dlg.dispose();
        } else if(filterNew) {
          mFilterBox.setSelectedItem(mLastSelectedFilter);
        }}catch(Throwable t) {
          t.printStackTrace();
        }
      });
      
      mFilterBox.addItemListener(e -> {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          mEdit.setEnabled(((WrapperFilter)mFilterBox.getSelectedItem()).getFilter() instanceof UserFilter);
          
          if(mFilterBox.getSelectedItem().equals(mNewFilter)) {
            mFilterBox.setPopupVisible(false);
            mEdit.doClick();
          }
        }
        else {
          mLastSelectedFilter = (WrapperFilter)e.getItem();
        }
      });
      
      add(mEdit, CC.xy(5, 1));
    }
  }
  
  /**
   * Gets the currently selected filter.
   * <p>
   * @return The selected filter.
   */
  public ProgramFilter getSelectedFilter() {
    return ((WrapperFilter)mFilterBox.getSelectedItem()).getFilter();
  }
  
  public JComboBox<WrapperFilter> getFilterBox() {
    return mFilterBox;
  }
  
  public JButton getEditButton() {
    return mEdit;
  }
  
  /**
   * Sets the filter to the given filter if it is in the list.
   * <p> 
   * @param filter
   * @since 4.2.5
   */
  public void setSelectedFilter(final ProgramFilter filter) {
    mFilterBox.setSelectedItem(new WrapperFilter(filter));
  }
  
  @Override
  public void setEnabled(boolean enabled) {
    mLabel.setEnabled(enabled);
    mFilterBox.setEnabled(enabled);
    
    if(mEdit != null) {
      mEdit.setEnabled(enabled && (((WrapperFilter)mFilterBox.getSelectedItem()).getFilter() instanceof UserFilter));
    }
  }
  
  private boolean isExcluded(ProgramFilter filter, Class<? extends Excludable>[] exclusions) {
    boolean result = false;
    
    if(exclusions != null) {
      for(Class<? extends Excludable> exclusion : exclusions) {
        if(exclusion.isInstance(filter) || 
            (filter instanceof PluginsProgramFilter && Plugin.class.isAssignableFrom(exclusion) &&
                ((PluginsProgramFilter)filter).containsPluinClass(exclusion)) ||
            (filter instanceof UserFilter && FilterComponent.class.isAssignableFrom(exclusion) &&
                ((UserFilter)filter).containsRuleComponent(exclusion))) {
          result = true;
          break;
        }
      }
    }
    
    return result;
  }
}
