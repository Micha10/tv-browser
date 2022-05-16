/*
 * TV-Browser
 * Copyright (C) 2022 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.filter.dlgs;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.core.filters.filtercomponents.AcceptNoneFilterComponent;
import tvbrowser.core.filters.filtercomponents.SingleChannelFilterComponent;
import util.ui.EnhancedPanelBuilder;
import util.ui.TVBrowserIcons;

/**
 * A class with a panel that contains the list of the current 
 * filter components for creating, editing and deleting those.
 * 
 * @author René Mach
 * @since 4.2.5
 */
public class FilterComponentPanel extends JPanel implements ActionListener {
  private JList<FilterItem> mFilterComponentList;
  private DefaultListModel<FilterItem> mFilterComponentListModel;
  private JButton mNewBtn, mEditBtn, mCopyButton, mRemoveBtn;
  private JDialog mParent;
  private JTextField mFilterRuleTF;
  private UserFilter mFilter = null;
  private boolean mFilterComponentTouched;
  
  FilterComponentPanel(final JDialog parent) {
    this(parent, null, null);
  }
  
  FilterComponentPanel(final JDialog parent, final JTextField filterRuleTF, final UserFilter filter) {
    mFilterComponentTouched = false;
    mParent = parent;
    mFilterRuleTF = filterRuleTF;
    mFilter = filter;
    EnhancedPanelBuilder filterComponents = new EnhancedPanelBuilder(new FormLayout("default:grow,5dlu,default"),this);
    
    mFilterComponentListModel = new DefaultListModel<>();
    
    if(mParent instanceof EditFilterDlg) {
      mFilterComponentListModel.addElement(new FilterItem(FilterItem.AND_KEY,0));
      mFilterComponentListModel.addElement(new FilterItem(FilterItem.OR_KEY,0));
      mFilterComponentListModel.addElement(new FilterItem(FilterItem.NOT_KEY,0));
      mFilterComponentListModel.addElement(new FilterItem(FilterItem.OPEN_BRACKET_KEY,0));
      mFilterComponentListModel.addElement(new FilterItem(FilterItem.CLOSE_BRACKET_KEY,0));
    }
    
    mFilterComponentList = new JList<>(mFilterComponentListModel);
    mFilterComponentList.setCellRenderer(new DefaultListCellRenderer() {      
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        
        if(value instanceof FilterItem) {
          FilterItem item = (FilterItem)value;
          
          if(item.isAndItem() || item.isNotItem() || item.isOrItem() || item.isOpenBracketItem() || item.isCloseBracketItem()) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
          }
          else if(item.getComponent().getDescription().length() > 0) {
            label.setText(label.getText() + " [" + item.getComponent().getDescription() + "]");
          }
          
          label.setText(FilterComponentList.getLabelForComponent(item.getComponent(), label.getText()));
        }
        
        return label;
      }
    });
    mFilterComponentList.addListSelectionListener(e -> {
      updateBtns();
    });
    mFilterComponentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    FilterComponent[] fc = FilterComponentList.getInstance().getAvailableFilterComponents();
    
    Arrays.sort(fc, new FilterComponent.NameComparator());
    
    for (FilterComponent element : fc) {
      mFilterComponentListModel.addElement(new FilterItem(element,0));
    }
    
    mNewBtn = new JButton(TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    mNewBtn.setToolTipText(EditFilterDlg.LOCALIZER.msg("newButton", "Create new filter component..."));
    mEditBtn = new JButton(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
    mEditBtn.setToolTipText(EditFilterDlg.LOCALIZER.msg("editButton", "Edit selected filter component..."));
    mCopyButton = new JButton(TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));
    mCopyButton.setToolTipText(EditFilterDlg.LOCALIZER.msg("copyButton", "Copy selected filter component..."));
    mRemoveBtn = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mRemoveBtn.setToolTipText(EditFilterDlg.LOCALIZER.msg("removeButton", "Delete selected filter component"));

    mNewBtn.addActionListener(this);
    mEditBtn.addActionListener(this);
    mCopyButton.addActionListener(this);
    mRemoveBtn.addActionListener(this);
    
    filterComponents.addRow(false, mNewBtn, 3);
    filterComponents.addRow(mEditBtn, 3);
    filterComponents.addRow(mCopyButton,3);
    filterComponents.addRow(mRemoveBtn, 3);
    filterComponents.addGrowingRow(false);
    filterComponents.add(new JScrollPane(mFilterComponentList), CC.xywh(1,1,1,8));
    
    if(!(mParent instanceof EditFilterDlg)) {
      registerMouseListener();
    }
    
    updateBtns();
  }
  
  void registerMouseListener() {
    mFilterComponentList.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        mFilterComponentList.requestFocus();
      }
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
          int index = mFilterComponentList.locationToIndex(e.getPoint());
          mFilterComponentList.setSelectedIndex(index);
          
          if(mEditBtn.isEnabled()) {
            editSelectedFilterComponent();
          }
        }
      }
    });
  }
  
  private void updateBtns() {
    if (mFilterComponentList == null) {
      return;
    }
    if(mFilterComponentList.getSelectedIndex() >= 0) {
      FilterItem item = (FilterItem)mFilterComponentList.getSelectedValue();
      
      mRemoveBtn.setEnabled(!item.isAndItem() && !item.isOrItem() && !item.isNotItem() && !item.isOpenBracketItem() && !item.isCloseBracketItem() && !(item.getComponent() instanceof SingleChannelFilterComponent));
      
      mEditBtn.setEnabled(mRemoveBtn.isEnabled() && !(item.getComponent() instanceof AcceptNoneFilterComponent));
      mCopyButton.setEnabled(mEditBtn.isEnabled());
    }
    else {
      mEditBtn.setEnabled(false);
      mRemoveBtn.setEnabled(false);
      mCopyButton.setEnabled(false);
    }
    
    if(mParent instanceof EditFilterDlg) {
      ((EditFilterDlg) mParent).updateBtns();
    }
  }
  
  JList<FilterItem> getList() {
    return mFilterComponentList;
  }
  
  @Override
  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    
    if (o == mNewBtn) {
      EditFilterComponentDlg dlg = new EditFilterComponentDlg(mParent);
            
      FilterComponent rule = dlg.getFilterComponent();
      if (rule != null) {
        mFilterComponentListModel.addElement(new FilterItem(rule,0));
        
        tvbrowser.core.filters.FilterComponentList.getInstance().add(rule);
        
        if(mParent instanceof EditFilterDlg) {
          String text = mFilterRuleTF.getText();
          if (text.length() > 0) {
            text += " " + EditFilterDlg.FILTER_LOCALIZER.msg("or", "or") + " ";
          }
          text += rule.getName();
          mFilterRuleTF.setText(text);
        
          ((EditFilterDlg) mParent).fillFilterConstruction();
        }
      }
    } else if (o == mCopyButton) {
      copySelectedFilterComponent();
    } else if (o == mEditBtn) {
      editSelectedFilterComponent();
    } else if (o == mFilterComponentList) {
      updateBtns();
    } else if (o == mRemoveBtn) {
      boolean allowRemove = true;
      UserFilter[] userFilterArr = FilterList.getInstance().getUserFilterArr();
      FilterComponent fc = ((FilterItem)mFilterComponentListModel.getElementAt(mFilterComponentList.getSelectedIndex())).getComponent();

      if(mParent instanceof EditFilterDlg ) {
        String name = ((EditFilterDlg) mParent).getFilterName();
        // Create the Filter based on the new Rule and check if the FC exists
        // there
        UserFilter testFilter = new UserFilter("test");
  
        try {
          testFilter.setRule(mFilterRuleTF.getText());
  
          if (testFilter.containsRuleComponent(fc.getName())) {
            allowRemove = false;
            JOptionPane.showMessageDialog(this, EditFilterDlg.LOCALIZER.msg("usedByAnotherFilter",
                "This filter component is used by filter '{0}'\nRemove the filter first.", name));
          }
        } catch (Exception ex) {
          // Filter creation failed, assume the old one is correct
          if ((mFilter != null) && (mFilter.containsRuleComponent(fc.getName()))) {
            allowRemove = false;
            JOptionPane.showMessageDialog(this, EditFilterDlg.LOCALIZER.msg("usedByAnotherFilter",
                "This filter component is used by filter '{0}'\nRemove the filter first.", name));
          }
        }
      }

      for (int i = 0; i < userFilterArr.length && allowRemove; i++) {
        if ((userFilterArr[i] != mFilter) && userFilterArr[i].containsRuleComponent(fc.getName())) {
          allowRemove = false;
          JOptionPane.showMessageDialog(this, EditFilterDlg.LOCALIZER.msg("usedByAnotherFilter",
              "This filter component is used by filter '{0}'\nRemove the filter first.", userFilterArr[i].toString()));
        }
      }
      if (allowRemove) {
        FilterComponentList.getInstance().remove(fc.getName());
        mFilterComponentListModel.removeElementAt(mFilterComponentList.getSelectedIndex());
        updateBtns();
      }

    }
  }
  
  private void copySelectedFilterComponent() {
    int inx = mFilterComponentList.getSelectedIndex();

    if(inx == -1) {
      return;
    }

    FilterComponent rule = ((FilterItem)mFilterComponentListModel.getElementAt(inx)).getComponent();
    
    String name = rule.getName();
    int count = name.lastIndexOf("_");
    
    if(count != -1) {
      try {
      count = Integer.parseInt(name.substring(count+1))+1;
      name = name.substring(0,name.lastIndexOf("_"));
      }catch(NumberFormatException nfe) {
        count = 1;
      }
    }
    else {
      count = 1;
    }
    
    while(FilterComponentList.getInstance().exists(name+"_"+count)) {
      count++;
    }
    
    rule = FilterComponentList.getInstance().createCopy(rule, name + "_" + count);
    
    EditFilterComponentDlg dlg = null;
    
    dlg = new EditFilterComponentDlg(mParent,rule);
    
    FilterComponent newRule = dlg.getFilterComponent();
    
    dlg.dispose();
    
    if (newRule != null) {
      mFilterComponentTouched = true;
      
      FilterComponentList.getInstance().add(newRule);
    
      mFilterComponentListModel.addElement(new FilterItem(newRule,0));
      
      if(mParent instanceof EditFilterDlg) {
        ((EditFilterDlg) mParent).repaintFilterConstruction();
      }
    }
    updateBtns();
  }

  private void editSelectedFilterComponent() {
    new Thread("EDIT FILTER COMPONENT THREAD") {
      @Override
      public void run() {
        int inx = mFilterComponentList.getSelectedIndex();
    
        if(inx == -1) {
          return;
        }
    
        FilterComponent rule = ((FilterItem)mFilterComponentListModel.getElementAt(inx)).getComponent();
        final String oldName = rule.getName();
        FilterComponentList.getInstance().remove(oldName);
        mFilterComponentListModel.removeElementAt(inx);
        EditFilterComponentDlg dlg = null;
        FilterComponent newRule = rule;
        
        dlg = new EditFilterComponentDlg(mParent,rule);
        boolean result = dlg.getOkWasPressed();
        
        if(result) {
          mFilterComponentTouched = true;
        
          newRule = dlg.getFilterComponent();
          
          if (newRule == null) {
            newRule = rule;
          }
          
          dlg.dispose();
          
          FilterComponentList.getInstance().add(newRule);
          FilterTreeModel.getInstance().updateFilterComponent(oldName, newRule);
          
          if(!oldName.equalsIgnoreCase(newRule.getName())) {
            FilterComponentList.getInstance().remove(oldName);
          }
        }
        
        if(mParent instanceof EditFilterDlg && result) {
          if(!oldName.equalsIgnoreCase(newRule.getName())) {
            final String[] parts = mFilterRuleTF.getText().split("\\s+");
            final StringBuilder newRuleText = new StringBuilder();
            
            for(int i = 0; i < parts.length; i++) {
              if(parts[i].equals(oldName)) {
                parts[i] = newRule.getName();
              }
              
              if(newRuleText.length() > 0) {
                newRuleText.append(" ");
              }
              
              newRuleText.append(parts[i]);
            }
            
            mFilterRuleTF.setText(newRuleText.toString());
          }
          
          ((EditFilterDlg) mParent).repaintFilterConstruction();
        }
          
        mFilterComponentListModel.insertElementAt(new FilterItem(newRule,0),inx);
        mFilterComponentList.setSelectedIndex(inx);
        mFilterComponentList.ensureIndexIsVisible(inx);
        
        updateBtns();
      }
    }.start();
  }
  
  public boolean getFilterComponentWasTouched() {
    return mFilterComponentTouched;
  }
}
