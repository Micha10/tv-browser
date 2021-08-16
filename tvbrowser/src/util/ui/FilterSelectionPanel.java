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
    
    add(new JLabel(label != null ? label : LOCALIZER.msg("filterLabel", "Filter:")), CC.xy(1, 1));
    add(mFilterBox, CC.xy(3, 1));
    
    if(showEditButton) {
      final JButton edit = new JButton(Localizer.getLocalization(Localizer.I18N_EDIT),TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
      edit.setEnabled(selectedFilter instanceof UserFilter);
      edit.addActionListener(e -> {
        UserFilter filter = (UserFilter)mFilterBox.getSelectedItem();
        new EditFilterDlg(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), FilterList.getInstance(), filter, true);
        
        FilterTreeModel.getInstance().fireFilterTouched(filter);
      });
      
      mFilterBox.addItemListener(e -> {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          edit.setEnabled(mFilterBox.getSelectedItem() instanceof UserFilter);
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
}
