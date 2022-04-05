package util.ui;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import util.i18n.Localizer;

public class SearchableTextAreaPanel extends JPanel {
  private static final Color NOT_FOUND = new Color(255,100,100);
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(SearchableTextAreaPanel.class);
  private JTextArea mContent;
  
  public SearchableTextAreaPanel(final String text) {
    this(text, true);
  }
  
  public SearchableTextAreaPanel(final String text, final boolean caseSensitiveSearch) {
    mContent = new JTextArea(text);
    
    JScrollPane scroll = new JScrollPane(mContent);
    scroll.setRowHeaderView(new LineNumberHeader(mContent));
    
    final JButton search = new JButton(TVBrowserIcons.search(TVBrowserIcons.SIZE_LARGE));
    JLabel searchLabel = new JLabel(LOCALIZER.msg("search", "Search in list:"));
    JTextField searchField = new JTextField() {
      @Override
      public void paste() {
        super.paste();
        search.setEnabled(!getText().trim().isEmpty());
        search.doClick();
      }
      
      @Override
      public void cut() {
        super.cut();
        search.setEnabled(false);
      }
    };
    final Color backgroundDefault = searchField.getBackground();
    final Color foregroundDefault = searchField.getForeground();
    search.setEnabled(false);
    search.addActionListener(ee -> {
      mContent.select(-1, -1);
      int pos = caseSensitiveSearch ? mContent.getText().indexOf(searchField.getText().trim()) : mContent.getText().toLowerCase().indexOf(searchField.getText().toLowerCase().trim());
      
      if(pos != -1) {
        mContent.getCaret().setSelectionVisible(true);
        
        try {
          Rectangle bounds = mContent.modelToView2D(pos).getBounds();
          
          mContent.scrollRectToVisible(bounds);
          mContent.moveCaretPosition(pos+ searchField.getText().trim().length());
          mContent.select(pos, pos + searchField.getText().trim().length());
        } catch (BadLocationException e1) {
          e1.printStackTrace();
        }
      }
      else {
        if(UiUtilities.isGTKLookAndFeel()) {
          searchField.setForeground(NOT_FOUND);
        }
        else {
          searchField.setBackground(NOT_FOUND);
        }
      }
    });
    
    searchField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
          search.doClick();
          e.consume();
        }
      }
    });
    
    searchField.addCaretListener(ee -> {
      searchField.setBackground(backgroundDefault);
      searchField.setForeground(foregroundDefault);
      search.setEnabled(searchField.getText().trim().length() > 0 && mContent.getText().length() > 0);
    });
    
    setLayout(new FormLayout("default,2dlu,default:grow,2dlu,default","fill:40dlu:grow,2dlu,default"));
    
    add(searchLabel, CC.xy(1, 3));
    add(searchField, CC.xy(3, 3));
    add(search, CC.xy(5, 3));
    add(scroll, CC.xyw(1, 1, 5));
  }
  
  public JTextArea getTextArea() {
    return mContent;
  }
  
  public String getText() {
    return mContent.getText();
  }
}
