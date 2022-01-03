package imdbplugin;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.jgoodies.forms.factories.CC;

import devplugin.PluginsFilterComponent;
import devplugin.Program;
import util.ui.EnhancedPanelBuilder;
import util.ui.LineNumberHeader;
import util.ui.Localizer;

public class ImdbMovieIdFilterComponent extends PluginsFilterComponent {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(ImdbMovieIdFilterComponent.class);

  private ArrayList<String> mAcceptedList = new ArrayList<String>();
  
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
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder("default:grow");
    pb.addRow(false);
    pb.addLabel("<html>"+LOCALIZER.msg("help", "List with IMDB movie IDs to match<br>(one ID per line):")+"</html>", CC.xy(1, pb.getRowCount()));
    pb.addRow("fill:default:grow",true);
    
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
    
    for(String value : mAcceptedList) {
      if(b.length() > 0) {
        b.append("\n");
      }
      
      b.append(value);
    }
    
    mContent.setText(b.toString());
    
    pb.getPanel().setBackground(Color.red);
    pb.add(scroll, CC.xy(1, pb.getRowCount()));
    
    return pb.getPanel();
  }

  @Override
  public void saveSettings() {
    mAcceptedList.clear();
    
    final String[] parts = mContent.getText().replace("\r\n", "\n").split("\n");
    
    for(String p : parts) {
      mAcceptedList.add(p);
    }
  }
  
  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public void read(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    int n = in.readInt();
    
    for(int i = 0; i < n; i++) {
      mAcceptedList.add(in.readUTF());
    }
  }

  @Override
  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mAcceptedList.size());
    
    for(String value : mAcceptedList) {
      out.writeUTF(value);
    }
  }

  @Override
  public String getUserPresentableClassName() {
    return LOCALIZER.msg("name", "IMDB movie ID filter component");
  }

}
