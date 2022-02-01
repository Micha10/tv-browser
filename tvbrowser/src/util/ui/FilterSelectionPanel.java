package util.ui;

import java.awt.event.ItemEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ProgramFilter;
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
public class FilterSelectionPanel extends JPanel {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(FilterSelectionPanel.class);
  private JComboBox<ProgramFilter> mFilterBox;
  private UserFilter mNewFilter;
  private ProgramFilter mLastSelectedFilter;
  
  public static String getNewFilterName() {
    return LOCALIZER.ellipsisMsg("createFilter","Create new filter");
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
    setLayout(new FormLayout("default,2dlu,default,2dlu,default","default"));
    
    mFilterBox = new JComboBox<ProgramFilter>();
    mNewFilter = new UserFilter(getNewFilterName());
    
    if(selectedFilter == null) {
      selectedFilter = FilterManagerImpl.getInstance().getDefaultFilter();
    }
    
    if(selectedFilter == null) {
      selectedFilter = FilterManagerImpl.getInstance().getAllFilter();
    }
    
    final ProgramFilter[] filters = FilterManagerImpl.getInstance().getAvailableFilters();
    
    for(ProgramFilter filter : filters) {
      mFilterBox.addItem(filter);
      
      if(filter.getName().equals(selectedFilter.getName())) {
        mFilterBox.setSelectedItem(filter);
      }
    }
    
    mFilterBox.addItem(mNewFilter);
    
    add(new JLabel(label != null ? label : LOCALIZER.msg("filterLabel", "Filter:")), CC.xy(1, 1));
    add(mFilterBox, CC.xy(3, 1));
    
    if(showEditButton) {
      final JButton edit = new JButton(Localizer.getLocalization(Localizer.I18N_EDIT),TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
      edit.setEnabled(selectedFilter instanceof UserFilter);
      edit.addActionListener(e -> {
        UserFilter filter = (UserFilter)mFilterBox.getSelectedItem();
        boolean filterNew = filter.equals(mNewFilter);
        
        if(filterNew) {
          filter = new UserFilter("");
        }
        
        EditFilterDlg dlg = new EditFilterDlg(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), FilterList.getInstance(), filter, true);
        
        if(dlg.getOkWasPressed()) {
          if(filterNew) {
            FilterTreeModel.getInstance().addFilter(filter);
            FilterList.getInstance().store();
            mFilterBox.insertItemAt(filter, mFilterBox.getItemCount()-1);
            mFilterBox.setSelectedItem(filter);
          }
          else {
            FilterTreeModel.getInstance().fireFilterTouched(filter);
          }
        } else if(filterNew) {
          mFilterBox.setSelectedItem(mLastSelectedFilter);
        }
      });
      
      mFilterBox.addItemListener(e -> {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          edit.setEnabled(mFilterBox.getSelectedItem() instanceof UserFilter);
          
          if(mFilterBox.getSelectedItem().equals(mNewFilter)) {
            mFilterBox.setPopupVisible(false);
            edit.doClick();
          }
        }
        else {
          mLastSelectedFilter = (ProgramFilter)e.getItem();
        }
      });
      
      add(edit, CC.xy(5, 1));
    }
  }
  
  /**
   * Gets the currently selected filter.
   * <p>
   * @return The selected filter.
   */
  public ProgramFilter getSelectedFilter() {
    return (ProgramFilter)mFilterBox.getSelectedItem();
  }
  
  @Override
  public void setEnabled(boolean enabled) {
    for(int i = 0; i < getComponentCount(); i++) {
      if(enabled && getComponent(i) instanceof JButton) {
        getComponent(i).setEnabled(mFilterBox.getSelectedItem() instanceof UserFilter);
      }
      else {
        getComponent(i).setEnabled(enabled);
      }
    }
  }
}
