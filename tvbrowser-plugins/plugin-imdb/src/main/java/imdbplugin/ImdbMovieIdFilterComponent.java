package imdbplugin;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.PluginsFilterComponent;
import devplugin.Program;
import util.ui.LineNumberHeader;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

public class ImdbMovieIdFilterComponent extends PluginsFilterComponent {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(ImdbMovieIdFilterComponent.class);
  private static final Color NOT_FOUND = new Color(255,100,100);

  private ArrayList<String> mAcceptedList = new ArrayList<String>();
  private ArrayList<String> mCommentList = new ArrayList<String>();
  
  @Override
  public boolean accept(Program p) {
    ImdbRating rating = ImdbPlugin.getInstance().getRatingFor(p);
    
    if(rating != null && mAcceptedList.contains(rating.getMovieId())) {
      return true;
    }
    
    return false;
  }
  
  private JTextArea mContent;
  
  @Override
  public JPanel getSettingsPanel() {
    if(mContent == null) {
      mContent = new JTextArea();
    }
    else {
      mContent.setText("");
    }
    
    LineNumberHeader header = new LineNumberHeader(mContent);
    
    JScrollPane scroll = new JScrollPane(mContent);
    scroll.setRowHeaderView(header);
    
    StringBuilder b = new StringBuilder();
    
    for(int i = 0; i < mAcceptedList.size(); i++) {
      if(b.length() > 0) {
        b.append("\n");
      }
      
      b.append(mAcceptedList.get(i));
      
      String comment = mCommentList.get(i);
      
      if(comment != null && !comment.isBlank()) {
        b.append(" ").append(comment);
      }
    }
    
    mContent.setText(b.toString());
    
    if(b.length() > 0) {
      mContent.setCaretPosition(0);
    }
    final JButton search = new JButton(TVBrowserIcons.search(TVBrowserIcons.SIZE_LARGE));
    JLabel searchLabel = new JLabel(LOCALIZER.msg("search", "Search in list:"));
    JTextField searchField = new JTextField() {
      @Override
      public void paste() {
        super.paste();
        search.setEnabled(!getText().isBlank());
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
    search.addActionListener(e -> {
      mContent.select(-1, -1);
      int pos = mContent.getText().indexOf(searchField.getText().trim());
      
      if(pos != -1) {
        mContent.getCaret().setSelectionVisible(true);
        java.awt.geom.Rectangle2D view;
        try {
          view = mContent.modelToView2D(pos);
          mContent.scrollRectToVisible(view.getBounds());
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
    
    searchField.addCaretListener(e -> {
      searchField.setBackground(backgroundDefault);
      searchField.setForeground(foregroundDefault);
      search.setEnabled(searchField.getText().trim().length() > 0 && mContent.getText().length() > 0);
    });
    
    JPanel main = new JPanel(new FormLayout("default,2dlu,default:grow,2dlu,default","fill:40dlu:grow,2dlu,default"));
    
    main.add(searchLabel, CC.xy(1, 3));
    main.add(searchField, CC.xy(3, 3));
    main.add(search, CC.xy(5, 3));
    main.add(scroll, CC.xyw(1, 1, 5));
    
    return main;
  }

  @Override
  public void saveSettings() {
    mAcceptedList.clear();
    mCommentList.clear();
    
    final String[] parts = mContent.getText().replace("\r\n", "\n").split("\n");
    
    for(String p : parts) {
      int pos = p.indexOf(" ");
      
      String value = null;
      String comment = null;
      
      if(pos == -1) {
        value = p;
        comment = "";
      }
      else {
        value = p.substring(0, pos).trim();
        comment = p.substring(pos+1).trim();
      }
      
      if(!mAcceptedList.contains(value)) {
        mAcceptedList.add(value);
        mCommentList.add(comment);
      }
    }
  }
  
  @Override
  public int getVersion() {
    return 1;
  }

  @Override
  public void read(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    int n = in.readInt();
    
    for(int i = 0; i < n; i++) {
      mAcceptedList.add(in.readUTF());
      
      if(version >= 1) {
        mCommentList.add(in.readUTF());
      }
      else {
        mCommentList.add("");
      }
    }
  }

  @Override
  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mAcceptedList.size());
    
    for(int i = 0; i < mAcceptedList.size(); i++) {
      out.writeUTF(mAcceptedList.get(i));
      out.writeUTF(mCommentList.get(i));
    }
  }

  @Override
  public String getUserPresentableClassName() {
    return LOCALIZER.msg("name", "IMDb movie ID");
  }

  @Override
  public String getTypeDescription() {
    return LOCALIZER.msg("help", "List with IMDB movie IDs to match (one ID per line, comments can be added after the ID starting with a blank):");
  }
}
