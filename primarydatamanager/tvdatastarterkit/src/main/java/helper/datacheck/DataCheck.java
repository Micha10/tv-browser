package helper.datacheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

public class DataCheck {
  public static void main(String[] args) {
    final Properties p = new Properties();
    
    final File prop = new File("datacheck.prop");
    
    if(prop.isFile()) {
      try (FileInputStream in = new FileInputStream(prop)) {
        p.load(in);
      }catch(IOException ioe) {
        ioe.printStackTrace();
      }
    }
    
    final File[] channels = new File("config").listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().toLowerCase().endsWith("_channellist.txt");
      }
    });
    
    final ArrayList<Channel> list = new ArrayList<>();
    
    if(channels != null) {
      for(final File c : channels) {
        String group = c.getName().substring(0,c.getName().indexOf("_"));
        String line = null;
        
        try(final BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(c), "UTF-8"))) {
          while((line = read.readLine()) != null) {
            final Channel test = new Channel(group,line);
            
            if(!p.containsKey(test.getUniqueId())) {
              list.add(test);
            }
          }
        }catch(IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }

    final StringBuilder b = new StringBuilder();
    final Calendar now = Calendar.getInstance(TimeZone.getTimeZone("CET"));
    int i = 0;
        
    for(Channel ch : list) {
      int count = 0;
      
      for(i = 0; i < 14; i++) {
        if(!ch.fileExists(now)) {
          count++;
        }
        
        if(count == 2) {
          b.append(ch.mGroup).append(" ").append(ch.mName).append("\n");
          break;
        }
        
        now.add(Calendar.DAY_OF_YEAR, 1);
      }
      
      now.add(Calendar.DAY_OF_YEAR, -i);
    }
    
    System.out.println(b);
  }

  private static class Channel {
    private String mGroup;
    private String mCountry;
    private String mId;
    private String mName;
    
    public Channel(final String group, final String line) {
      String[] parts = line.split(";");
      
      mGroup = group;
      mCountry = parts[0];
      mId = parts[2];
      mName = parts[3];
    }
    
    public String getUniqueId() {
      return mCountry+";"+mId;
    }
    
    public boolean fileExists(Calendar cal) {
      StringBuilder b = new StringBuilder();
      
      b.append(cal.get(Calendar.YEAR)).append("-").append(String.format("%02d", cal.get(Calendar.MONTH)+1)).append("-").append(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
      b.append("_").append(mCountry).append("_").append(mId).append("_base_full.prog.gz");
      
      File f = new File("prepared/"+b.toString());
      
      return f.isFile();
    }
  }
}
