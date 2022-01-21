/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.filter.dlgs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang3.StringUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.ParserException;
import tvbrowser.core.filters.UserFilter;
import util.i18n.Localizer;
import util.ui.DragAndDropMouseListener;
import util.ui.ListDragAndDropHandler;
import util.ui.ListDropAction;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

public class EditFilterDlg extends JDialog implements ActionListener, DocumentListener, CaretListener, WindowClosingIf, ListDropAction<FilterItem> {

  static final util.i18n.Localizer LOCALIZER = util.i18n.Localizer.getLocalizerFor(EditFilterDlg.class);

  static final util.i18n.Localizer FILTER_LOCALIZER = util.i18n.Localizer.getLocalizerFor(UserFilter.class);

  private Window mParent;

  private JTextField mFilterNameTF, mFilterRuleTF;

  private UserFilter mFilter = null;

  private JLabel mFilterRuleErrorLb, mColLb;

  private String mFilterName = null;

  private FilterList mFilterList;
  
  private JList<FilterItem> mFilterConstruction;
  private FilterComponentPanel mFilterComponent;
  
  private DefaultListModel<FilterItem> mFilterConstructionListModel;
  
  private JCheckBox mFilterHighlight;
  
  private boolean mFromFilterList;
  private boolean mOkWasPressed;
  
  private JButton mOkBtn, mCancelBtn;

  public EditFilterDlg(Window parent, FilterList filterList, UserFilter filter, boolean fromFilterList) {
    super(parent, ModalityType.DOCUMENT_MODAL);
    init(parent,filterList,filter, fromFilterList);
  }
  
  private void init(Window parent,FilterList filterList, UserFilter filter, boolean fromFilterList) {
    UiUtilities.registerForClosing(this);
    try {
    mFromFilterList = fromFilterList;
    mOkWasPressed = false;
    
    if (filter == null) {
      setTitle(LOCALIZER.msg("titleNew", "Create filter"));
    } else {
      setTitle(LOCALIZER.msg("titleEdit", "Edit filter {0}", fromFilterList ? filter.toString() : "").replaceAll("\\s+", " "));
      mFilterName = filter.toString();
    }
    
    mFilterList = filterList;
    mParent = parent;
    mFilter = filter;
    
    mFilterNameTF = new JTextField(new PlainDocument() {
      public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        str = str.replaceAll("[\\p{Punct}&&[^_]]", "_");
        super.insertString(offset, str, a);
      }
    }, "", 30);
    mFilterNameTF.getDocument().addDocumentListener(this);
    mFilterNameTF.setEditable(fromFilterList);
    
    mFilterRuleTF = new JTextField();
    mFilterRuleTF.getDocument().addDocumentListener(this);
    mFilterRuleTF.addCaretListener(this);  
    
    FormLayout layout = new FormLayout("5dlu,fill:min:grow,5dlu,default,5dlu","default,5dlu,default,default,5dlu,default,5dlu,fill:min:grow,5dlu,default,5dlu,default,5dlu,default,5dlu,default");
    PanelBuilder filterCreation = new PanelBuilder(layout);
    filterCreation.border(Borders.DIALOG);
    
    int y = 1;
    
    if(fromFilterList) {
      layout.insertRow(1, RowSpec.decode("10dlu"));
      layout.insertRow(1, RowSpec.decode("default"));
      layout.insertRow(1, RowSpec.decode("5dlu"));
      layout.insertRow(1, RowSpec.decode("default"));
      
      filterCreation.addSeparator(LOCALIZER.msg("filterName", "Filter name:"), CC.xyw(1,y,5));
      filterCreation.add(mFilterNameTF, CC.xyw(2,y+2,3));
      
      y = 5;
    }
    
    filterCreation.addSeparator(LOCALIZER.msg("ruleString", "Filter rule:"), CC.xyw(1,y,5));
    filterCreation.add(mFilterRuleTF, CC.xy(2,y+2));
    mColLb = filterCreation.addLabel("0", CC.xy(4,y+2));
    mFilterRuleErrorLb = filterCreation.addLabel(LOCALIZER.msg("ruleExample",
    "example: component1 or (component2 and not component3)"), CC.xy(2,y+3));
    
    mFilterHighlight = new JCheckBox(LOCALIZER.msg("highlight", "Highlight all matching programs"));
    
    if(mFilter != null) {
      mFilterHighlight.setSelected(Settings.Markings.HIGHLIGHTING_FILTERS.containsItem(mFilter.getName()));
    }
    
    ButtonBarBuilder bottomBar = Utilities.createFilterButtonBar();

    mOkBtn = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    mOkBtn.addActionListener(this);
    getRootPane().setDefaultButton(mOkBtn);

    mCancelBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCancelBtn.addActionListener(this);

    bottomBar.addButton(new JButton[] {mOkBtn, mCancelBtn});

    mFilterComponent = new FilterComponentPanel(this,mFilterRuleTF,mFilter);
    
    mFilterConstructionListModel = new DefaultListModel<>();
    mFilterConstruction = new JList<>(mFilterConstructionListModel);
    mFilterConstruction.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mFilterConstruction.setCellRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        try {
          if(label.getBorder() != null && label.getBorder().getClass().getName().contains("GTKPainter$ListTableFocusBorder")) {
            label.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
          }
          
          if(value instanceof FilterItem) {
            FilterItem item = (FilterItem)value;
            
            FormLayout layout = new FormLayout("default:grow","default");
            
            for(int i = 0; i < item.getLevel(); i++) {
              layout.insertColumn(1,ColumnSpec.decode("9dlu"));
            }
            
            JPanel panel = new JPanel(layout);
            panel.setOpaque(isSelected);
            
            if(isSelected) {
              panel.setBackground(list.getSelectionBackground());
              label.setForeground(list.getSelectionForeground());
            }
            else {
              panel.setBackground(list.getBackground());
              label.setForeground(list.getForeground());
            }
            
            label.setOpaque(false);
            
            if(item.isAndItem() || item.isOrItem() || item.isNotItem()) {
              label.setFont(label.getFont().deriveFont(Font.BOLD));
              layout.insertColumn(1,ColumnSpec.decode("3dlu"));
              panel.add(label,CC.xy(Math.max(1,item.getLevel()+2),1));
            }
            else if(item.isOpenBracketItem() || item.isCloseBracketItem()) {
              label.setFont(label.getFont().deriveFont(Font.BOLD));
              panel.add(label,CC.xy(Math.max(1, item.getLevel()+1),1));
            }
            else {
              panel.add(label,CC.xy(Math.max(1,item.getLevel()+1),1));
  
              label.setText(FilterComponentList.getLabelForComponent(item.getComponent(), label.getText()));
            }
  
            if(index > 0) {
              FilterItem test = (FilterItem)list.getModel().getElementAt(index-1);
              FilterItem test2 = null;
              
              if(index < list.getModel().getSize() - 2) {
                test2 = (FilterItem)list.getModel().getElementAt(index+1);
              }
                          
              if(test.isCloseBracketItem() && !item.isAndItem() && !item.isOrItem() && !item.isCloseBracketItem()) {
                label.setForeground(Color.red);
              }
              else if(item.isNotItem() && ((!test.isAndItem() && !test.isOrItem()) || test.isNotItem()) && (test2 == null || test2.getComponent() == null)) {
                label.setForeground(Color.red);
              }
              else if((item.isAndItem() || item.isOrItem()) && (test.isAndItem() || test.isOrItem() || test.isOpenBracketItem())) {
                label.setForeground(Color.red);
              }
              else if(item.getComponent() != null && test2 != null && test2.isNotItem()) {
                label.setForeground(Color.red);
              }
              else if(test2 != null && test2.isOpenBracketItem() && !item.isAndItem() && !item.isOrItem() && !item.isNotItem() && !item.isOpenBracketItem()) {
                label.setForeground(Color.red);
              }            
              else if((index == list.getModel().getSize()-1) && (item.isAndItem() || item.isNotItem() || item.isOrItem() || item.isOpenBracketItem())) {
                label.setForeground(Color.red);
              }
              else if(item.getComponent() != null && test.getComponent() != null) {
                label.setForeground(Color.red);
              }
            }
  
            return panel;
          }
        }catch(Throwable t) {t.printStackTrace();}
        
        return label;
      }
    });
    
    if (mFilter != null) {
      mFilterName = filter.getName();
      mFilterNameTF.setText(mFilter.toString());
      mFilterRuleTF.setText(mFilter.getRule());
      fillFilterConstruction();
    }
    
    //Register DnD on the List.
    ListDragAndDropHandler dnDHandler = new ListDragAndDropHandler(mFilterComponent.getList(), mFilterConstruction, this);
    new DragAndDropMouseListener<FilterItem>(mFilterComponent.getList(),mFilterConstruction,this,dnDHandler,false);
    new DragAndDropMouseListener<FilterItem>(mFilterConstruction,mFilterComponent.getList(),this,dnDHandler);
    
    mFilterComponent.registerMouseListener();
    
    PanelBuilder listPanel = new PanelBuilder(new FormLayout("5dlu,min:grow,5dlu,10dlu,5dlu,min:grow,5dlu","default,5dlu,fill:min:grow"));
    listPanel.addSeparator(LOCALIZER.msg("componentsTitle","Available filter components:"), CC.xyw(5,1,3));
    listPanel.add(mFilterComponent, CC.xy(6,3));
    listPanel.addSeparator(LOCALIZER.msg("filterConstruction", "Filter construction"), CC.xyw(1,1,3));
    listPanel.add(new JScrollPane(mFilterConstruction), CC.xy(2,3));
    
    filterCreation.add(listPanel.getPanel(), CC.xyw(1,y+7,4));
    filterCreation.add(UiUtilities.createHelpTextArea(LOCALIZER.msg("help","To create or edit a filter you can enter the rules in the text field or drag and drop the rules to the left side.")), CC.xyw(2,y+9,4));
    filterCreation.add(mFilterHighlight, CC.xyw(2, y+11, 3));
    filterCreation.add(new JSeparator(JSeparator.HORIZONTAL), CC.xyw(1,y+13,4));
    filterCreation.add(bottomBar.getPanel(), CC.xyw(1,y+15,4));
    
    updateBtns();

    setMinimumSize(new Dimension(600,560));
    
    setLayout(new BorderLayout());
    
    add(filterCreation.getPanel(), BorderLayout.CENTER);
    }
    catch(Throwable t) {t.printStackTrace();}
    Settings.layoutWindow("editFilterDlg",this,getMinimumSize(),mParent);
    setVisible(true);
  }
  

  void updateBtns() {
    boolean validRule = true;
    
    try {
      UserFilter.testTokenTree(mFilterRuleTF.getText(),false);
      mFilterRuleErrorLb.setForeground(UIManager.getColor("Label.foreground"));
      mFilterRuleErrorLb.setText(LOCALIZER.msg("ruleExample",
      "example: component1 or (component2 and not component3)"));
    } catch (ParserException e) {
      mFilterRuleErrorLb.setForeground(Color.red);
      mFilterRuleErrorLb.setText(e.getMessage());
      validRule = false;
    }

    if(mFilterRuleTF.hasFocus()) {
      fillFilterConstruction();
    }
    
    mOkBtn.setEnabled(StringUtils.isNotBlank(mFilterNameTF.getText()) && mFilterComponent.getList().getModel().getSize() > 0 && validRule);
    mFilterHighlight.setEnabled(mOkBtn.isEnabled());
  }

  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    
    if (o == mOkBtn) {
      mOkWasPressed = true;
      
      if(mFromFilterList) {
        String filterName = mFilterNameTF.getText();
        if (!filterName.equalsIgnoreCase(mFilterName) && mFilterList.containsFilter(filterName)) {
          JOptionPane
              .showMessageDialog(this, LOCALIZER.msg("alreadyExists", "Filter '{0}' already exists.", filterName));
          mOkWasPressed = false;
        } else {
          if (mFilter == null) {
            mFilter = new UserFilter(mFilterNameTF.getText());
          } else {
            mFilter.setName(mFilterNameTF.getText());
          }
  
          try {
            mFilter.setRule(mFilterRuleTF.getText());
            FilterComponentList.getInstance().store();
            setVisible(false);
          } catch (ParserException exc) {
            mOkWasPressed = false;
            JOptionPane.showMessageDialog(this, LOCALIZER.msg("invalidRule", "Invalid rule: ") + exc.getMessage());
          }
        }
      }
      else {
        try {
          mFilter.setRule(mFilterRuleTF.getText());
          FilterComponentList.getInstance().store();
          setVisible(false);
        } catch (ParserException e1) {
          mOkWasPressed = false;
          JOptionPane.showMessageDialog(this, LOCALIZER.msg("invalidRule", "Invalid rule: ") + e1.getMessage());
        }
      }
      
      if(mOkWasPressed) {
        if(mFilterHighlight.isSelected()) {
          Settings.Markings.HIGHLIGHTING_FILTERS.addItem(mFilter.getName());
        }
        else {
          Settings.Markings.HIGHLIGHTING_FILTERS.removeItem(mFilter.getName());
        }
      }
    } else if (o == mCancelBtn) {
      FilterComponentList.getInstance().store();
      setVisible(false);
    }

  }
  
  public boolean getOkWasPressed() {
    return mOkWasPressed;
  }

  public UserFilter getUserFilter() {
    return mFilter;
  }

  public void changedUpdate(DocumentEvent e) {
    updateBtns();
  }

  public void insertUpdate(DocumentEvent e) {
    updateBtns();
  }

  public void removeUpdate(DocumentEvent e) {
    updateBtns();
  }

  public void caretUpdate(javax.swing.event.CaretEvent e) {
    mColLb.setText("pos: " + mFilterRuleTF.getCaretPosition());
  }

  public void close() {
    setVisible(false);
  }
  /*
  private class FilterItem {    
    private String mRuleType;
    private FilterComponent mComponent;
    private int mLevel;
    
    private FilterItem(String ruleType, int level, FilterComponent comp) {
      mRuleType = ruleType;
      mLevel = level;
      mComponent = comp;
    }
    
    public FilterItem(String ruleType, int level) {
      mRuleType = ruleType;
      mComponent = null;
      mLevel = level;
    }
    public FilterItem(FilterComponent comp, int level) {
      mRuleType = null;
      mComponent = comp;
      mLevel = level;
    }
    
    public String toString() {
      if(mRuleType != null) {
        if(getLocale().getLanguage().equals("de")) {
          if(mRuleType.equals(AND_KEY)) {
            return "UND";
          }
          else if(mRuleType.equals(OR_KEY)) {
            return "ODER";
          }
          else if(mRuleType.equals(NOT_KEY)) {
            return "NICHT";
          } 
        }
        else {
          if(mRuleType.equals(AND_KEY)) {
            return "AND";
          }
          else if(mRuleType.equals(OR_KEY)) {
            return "OR";
          }
          else if(mRuleType.equals(NOT_KEY)) {
            return "NOT";
          } 
        }
        
        if(mRuleType.equals(OPEN_BRACKET_KEY)) {
          return "(";
        }
        else if(mRuleType.equals(CLOSE_BRACKET_KEY)) {
          return ")";
        }
      }
      return mComponent.getName();
    }
    
    public void setLevel(int level) {
      mLevel = level;
    }
    
    public int getLevel() {
      return mLevel;
    }
    
    public FilterComponent getComponent() {
      return mComponent;
    }
    
    public FilterItem clone(int level) {
      return new FilterItem(mRuleType,level,mComponent);
    }
    
    public boolean isOpenBracketItem() {
      return mRuleType != null && mRuleType.equals(OPEN_BRACKET_KEY);
    }
    
    public boolean isCloseBracketItem() {
      return mRuleType != null && mRuleType.equals(CLOSE_BRACKET_KEY);
    }
    
    public boolean isAndItem() {
      return mRuleType != null && mRuleType.equals(AND_KEY);
    }

    public boolean isOrItem() {
      return mRuleType != null && mRuleType.equals(OR_KEY);
    }
    
    public boolean isNotItem() {
      return mRuleType != null && mRuleType.equals(NOT_KEY);
    }
  }*/
  
  public boolean checkValueForRuleType(String value, String ruleType) {
    if(ruleType != null) {
      if(ruleType.equals(FilterItem.AND_KEY) && (value.toLowerCase().equals("and") || value.toLowerCase().equals("und"))) {
        return true;
      }
      else if(ruleType.equals(FilterItem.OR_KEY) && (value.toLowerCase().equals("or") || value.toLowerCase().equals("oder"))) {
        return true;
      }
      else if(ruleType.equals(FilterItem.NOT_KEY) && (value.toLowerCase().equals("not") || value.toLowerCase().equals("nicht"))) {
        return true;
      }
      else if(ruleType.equals(FilterItem.OPEN_BRACKET_KEY) && value.toLowerCase().equals("(")) {
        return true;
      }
      else if(ruleType.equals(FilterItem.CLOSE_BRACKET_KEY) && value.toLowerCase().equals(")")) {
        return true;
      }
    }
    
    return false;
  }

  @Override
  public void drop(JList<FilterItem> source, JList<FilterItem> target, int row, boolean move) {try {
    mFilterNameTF.requestFocus();
    
    if(target.equals(mFilterConstruction)) {
      if(source.equals(mFilterComponent.getList())) {
        FilterItem value = (FilterItem)source.getSelectedValue();
        ((DefaultListModel<FilterItem>)target.getModel()).add(row,value.clone(0));
        
        int level = 0;
        
        for(int i = 0; i < target.getModel().getSize(); i++) {
          FilterItem item = (FilterItem)target.getModel().getElementAt(i);
          
          if(item.toString().equals("(")) {
            item.setLevel(level++);
          }
          else if(item.toString().equals(")")) {
            level--;
            level = Math.max(level,0);
            item.setLevel(level);
          }
          else {
            item.setLevel(level);
          }

        }        
      }
      else {
        UiUtilities.moveSelectedItems(target,row,false);
        
        int level = 0;
        
        for(int i = 0; i < target.getModel().getSize(); i++) {
          FilterItem item = (FilterItem)target.getModel().getElementAt(i);
          
          if(item.toString().equals("(")) {
            item.setLevel(level++);
          }
          else if(item.toString().equals(")")) {
            level--;
            level = Math.max(level,0);
            item.setLevel(level);
          }
          else {
            item.setLevel(level);
          }

        }
      }
    }
    else if(source.equals(mFilterConstruction)){
      ((DefaultListModel<FilterItem>)source.getModel()).remove(source.getSelectedIndex());
      
      
      int level = 0;
      
      for(int i = 0; i < source.getModel().getSize(); i++) {
        FilterItem item = (FilterItem)source.getModel().getElementAt(i);
        
        if(item.toString().equals("(")) {
          item.setLevel(level++);
        }
        else if(item.toString().equals(")")) {
          level--;
          level = Math.max(level,0);
          item.setLevel(level);
        }
        else {
          item.setLevel(level);
        }
      }
    }
        
    StringBuilder build = new StringBuilder();
    
    for(int i = 0; i < mFilterConstruction.getModel().getSize(); i++) {
      build.append(mFilterConstruction.getModel().getElementAt(i).toString()).append(" ");
    }
    
    if(build.length() > 0) {
      build.delete(build.length()-1,build.length());
    }
    
    mFilterRuleTF.setText(build.toString());
    
    if(source != null) {
      source.repaint();
    }
    if(target != null) {
      target.repaint();
    }
  }catch(Throwable t) {t.printStackTrace();}
  }
  
  void fillFilterConstruction() {
    mFilterConstructionListModel.clear();
    
    ArrayList<String> values = new ArrayList<String>();
    
    String rule = mFilterRuleTF.getText();
    
    for(int i = rule.length()-1; i >= 0; i--) {
      
      if(rule.charAt(i) == ' ') {
        
        values.add(0,rule.substring(i).trim());
        rule = rule.substring(0,i).trim();
        i = rule.length();
      }
      else if(rule.charAt(i) == '(' || rule.charAt(i) == ')') {
        String test = rule.substring(i+1).trim();
        
        if(test.length() > 0) {
          values.add(0,test);
        }
        values.add(0,rule.substring(i,i+1));
        rule = rule.substring(0,i).trim();
        i = rule.length();
      }
    }
    
    if(!rule.isEmpty()) {
      values.add(0,rule.trim());
    }
    
    int level = 0;
    
    for(String value : values) {
      if(checkValueForRuleType(value,FilterItem.AND_KEY)) {
        mFilterConstructionListModel.addElement(new FilterItem(FilterItem.AND_KEY,level));
        
      }
      else if(checkValueForRuleType(value,FilterItem.OR_KEY)) {
        mFilterConstructionListModel.addElement(new FilterItem(FilterItem.OR_KEY,level));
        
      }
      else if(checkValueForRuleType(value,FilterItem.NOT_KEY)) {
        mFilterConstructionListModel.addElement(new FilterItem(FilterItem.NOT_KEY,level));
        
      }
      else if(checkValueForRuleType(value,FilterItem.OPEN_BRACKET_KEY)) {
        mFilterConstructionListModel.addElement(new FilterItem(FilterItem.OPEN_BRACKET_KEY,level));
        level++;
        
      }
      else if(checkValueForRuleType(value,FilterItem.CLOSE_BRACKET_KEY)) {
        level--;
        mFilterConstructionListModel.addElement(new FilterItem(FilterItem.CLOSE_BRACKET_KEY,level));
        
      }
      else {
        for (FilterComponent element : FilterComponentList.getInstance().getAvailableFilterComponents()) {
          
          if(element.getName().equals(value)) {
            mFilterConstructionListModel.addElement(new FilterItem(element,level));
          }
        }
      }
      level = Math.max(level,0);
    }
  }
  
  String getFilterName() {
    return mFilterNameTF.getText();
  }
  
  void repaintFilterConstruction() {
    mFilterConstruction.repaint();
  }
}