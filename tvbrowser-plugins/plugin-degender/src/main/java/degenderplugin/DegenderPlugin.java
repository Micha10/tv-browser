package degenderplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.ProgramFieldType;
import devplugin.SettingsTab;
import devplugin.Version;
import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import util.ui.EnhancedPanelBuilder;
import util.ui.customizableitems.SelectableItemList;

public class DegenderPlugin extends Plugin {
  private static final ProgramFieldType[] FIELD_TYPES = {
      ProgramFieldType.TITLE_TYPE,
      ProgramFieldType.SHORT_DESCRIPTION_TYPE,
      ProgramFieldType.DESCRIPTION_TYPE,
      ProgramFieldType.ADDITIONAL_INFORMATION_TYPE,
      ProgramFieldType.PICTURE_DESCRIPTION_TYPE,
      ProgramFieldType.EPISODE_TYPE,
      ProgramFieldType.SERIES_TYPE
  };
  
  private LinkedHashMap<String, String> mSingularReplacement;
  private LinkedHashMap<String, String> mPluralReplacement;
  private HashSet<Channel> mChannelSet;
  
  private static final Pattern GENDERED = Pattern.compile("(\\b(?i)(die\\s){0,1}(?-i)\\b(?!Mc)(\\w+?)(?:\\s*[\\*\\:_](?i:i)|I)n(nen){0,1})", Pattern.DOTALL | Pattern.UNICODE_CHARACTER_CLASS); 
  private static final Pattern GENDERED_LONG = Pattern.compile("(\\b([\\w\\-]+?)innen\\b\\s+(?:und|oder)\\s+\\-{0,1}\\b(\\w+?)\\b)|(\\b([\\w\\-]+?)\\b\\s+(?:und|oder)\\s+\\-{0,1}\\b(\\w+?)innen\\b)", Pattern.DOTALL | Pattern.UNICODE_CHARACTER_CLASS);
  private static final Pattern GENDERED_PARTIZIP = Pattern.compile("(\\b(?i)(?:(\\w*eine|der|die|bei|mit|\\w+en)\\s){0,1}(?-i)(?:\\b\\w+\\b\\s){0,1}\\b((\\p{Upper}\\w+)ende(n|r){0,1}(\\w*)\\b))", Pattern.DOTALL | Pattern.UNICODE_CHARACTER_CLASS);
  private static final Version VERSION = new Version(0,13,3,true);
  
  private boolean mRemoveLongForm = false;
  private boolean mReplacePartizip = false;
  
  private int mCountShort;
  private int mCountLong;
  private int mCountPartizip;
  
  public DegenderPlugin() {
    mSingularReplacement = new LinkedHashMap<String, String>();
    
    mSingularReplacement.put("\u00c4rzt", "Arzt");
    mSingularReplacement.put("Anw\u00e4lt", "Anwalt");
    mSingularReplacement.put("B\u00e4uer", "Bauer");
    mSingularReplacement.put("Beamt", "Beamter");
    mSingularReplacement.put("G\u00c4st", "Gast");
    mSingularReplacement.put("log", "loge");
    mSingularReplacement.put("r\u00e4t", "rat");
    
    mPluralReplacement = new LinkedHashMap<String, String>();
    
    mPluralReplacement.put("\u00c4rzt","\u00c4rzte");
    mPluralReplacement.put("Anw\u00e4lt", "Anw\u00e4lte");
    mPluralReplacement.put("B\u00e4uer", "Bauern");
    mPluralReplacement.put("Beamt","Beamte");
    mPluralReplacement.put("G\u00c4st","G\u00c4ste");
    mPluralReplacement.put("Freund", "Freunde");
    
    mPluralReplacement.put("ling","linge");
    mPluralReplacement.put("eur","eure");
    mPluralReplacement.put("ich","iche");
    mPluralReplacement.put("ier", "iere"); 
    mPluralReplacement.put("ig", "ige");
    mPluralReplacement.put("\u00f6r", "\u00f6re");
    mPluralReplacement.put("r\u00e4t", "r\u00e4te");
  }
  
  public static Version getVersion() {
    return VERSION;
  }
  
  @Override
  public PluginInfo getInfo() {
    return new PluginInfo(DegenderPlugin.class, "DeGender", "Entfernt Binnen-I, Gender-*, Gender-: und Gender-_ aus Titel, Episodentitel, Serientitel, Kurzbeschreibung, Beschreibung, Bildbeschreibung und weitere Informationen.", "René Mach");
  }
  
  @Override
  public void handleTvBrowserStartFinished() {
    if(mChannelSet == null) {
      mChannelSet = new HashSet<Channel>();
      
      final Channel[] channels = getPluginManager().getSubscribedChannels();
      
      for(Channel ch : channels) {
          mChannelSet.add(ch);
      }
      
      saveMe();
    }
  }
  
  @Override
  public void handleTvDataUpdateStarted(Date until) {
    mCountShort = 0;
    mCountLong = 0;
    mCountPartizip = 0;
  }
  
  @Override
  public void handleTvDataAdded(MutableChannelDayProgram newProg) {
    if(mChannelSet != null) {
      if(mChannelSet.contains(newProg.getChannel())) {
        for(int i = 0; i < newProg.getProgramCount(); i++) {
          MutableProgram p = (MutableProgram)newProg.getProgramAt(i);
          
          for(ProgramFieldType type : FIELD_TYPES) {
            String text = deGender(p.getTextField(type));
            
            if(text != null) {
              p.setTextField(type, text);
            }
          }
        }
      }
    }
  }
  
  private String deGenderShort(String text) {
    Matcher m = GENDERED.matcher(text);
    StringBuilder result = new StringBuilder();
    
    do {
      int pos = 0;
      
      while(m.find(pos)) {
       /*for(int i = 1; i <= m.groupCount(); i++) {
          System.out.println(i+": " + m.group(i));
        }
        */
        boolean wrongArticle = (m.group(2) != null && !m.group(2).trim().isEmpty());
        String replace = m.group(3);
        String test = replace.toLowerCase();
        boolean singular = (m.group(4) == null || m.group(4).trim().isEmpty());
        
        if(!singular) {
          Set<String> keys = mPluralReplacement.keySet();
          boolean found = false;
          for(String key : keys) {
            if(test.endsWith(key.toLowerCase())) {
              found = true;
              String replacement = mPluralReplacement.get(key);
              
              if(replace.charAt(replace.length()-key.length()) != key.charAt(0)) {
                replacement = replacement.toLowerCase();
              }
              
              replace = replace.substring(0,replace.length()-key.length()) + replacement;
              
              break;
            }
          }
          
          if(!found && !test.endsWith("er") && !test.endsWith("el")) {
            replace += "en";
          }
        }
        else {
          Set<String> keys = mSingularReplacement.keySet();
          for(String key : keys) {
            if(test.endsWith(key.toLowerCase())) {
              String replacement = mSingularReplacement.get(key);
              
              if(replace.charAt(replace.length()-key.length()) != key.charAt(0)) {
                replacement = replacement.toLowerCase();
              }
              
              replace = replace.substring(0,replace.length()-key.length()) + replacement;
              
              break;
            }
          }
          
          if(wrongArticle) {
            if(m.group(2).startsWith("D")) {
              replace = "Der "+replace;
            }
            else {
              replace = "der "+replace;
            }
          }
        }
     //   System.out.println("    "+replace+"\n");
        mCountShort++;
        result.append(text.substring(pos,m.start(1))).append(replace);
        pos = m.end();
      }
      
      if(pos < text.length()) {
        result.append(text.substring(pos,text.length()));
      }
      
      text = result.toString();
      
      m = GENDERED.matcher(text);
    }while(m.find());
    
    return text;
  }
  
  private String deGenderLong(String text) {
    if(mRemoveLongForm && text != null) {
      int pos = 0;
      StringBuilder result = new StringBuilder();
      Matcher m = GENDERED_LONG.matcher(text);
      
      while(m.find(pos)) {
        /*for(int i = 1; i <= m.groupCount(); i++) {
        System.out.println(i+": " + m.group(i));
      }System.out.println();*/
        String needle = null;
        String replace = null;
        String male = null;
        String female = null;
        int index = -1;
        
        if(m.group(1) != null && m.group(2) != null && m.group(3) != null) {
          needle = m.group(1);
          female = m.group(2).toLowerCase().replace("ä", "a").replace("ö", "o").replace("ü", "u");
          replace = m.group(3);
          male = replace.toLowerCase().replace("ä", "a").replace("ö", "o").replace("ü", "u");
          
          if(female.contains("-")) {
            female = female.substring(female.lastIndexOf("-")+1);
            replace = m.group(2).substring(0,m.group(2).lastIndexOf("-")+1)+replace;
          }
          
          index = m.start(1);
        }
        else if(m.group(4) != null && m.group(5) != null && m.group(6) != null) {
          needle = m.group(4);
          female = m.group(6).toLowerCase().replace("ä", "a").replace("ö", "o").replace("ü", "u");
          replace = m.group(5);
          male = replace.toLowerCase().replace("ä", "a").replace("ö", "o").replace("ü", "u");
          
          if(male.contains("-")) {
            male = male.substring(male.lastIndexOf("-")+1);
          }
          
          index = m.start(4);
        }
        
        if(needle != null && male != null && female != null && male.startsWith(female)) {
          result.append(text.substring(pos,index)).append(replace);
          mCountLong++;
        }
        else {
          result.append(text.substring(pos,index)).append(needle);
        }
        
        pos = m.end();
      }
      
      if(pos < text.length()) {
        result.append(text.substring(pos,text.length()));
      }
      
      text = result.toString();
    }
    
    return text;
  }
  
  private String deGenderPartizip(String text) {
    if(mReplacePartizip && text != null) {
      StringBuilder result = new StringBuilder();
      Matcher m = GENDERED_PARTIZIP.matcher(text);
      int pos = 0;
      
      while(m.find(pos)) {
        if(!VERSION.isStable()) {
          for(int i = 1; i <= m.groupCount(); i++) {
            System.out.println(i+": " + m.group(i));
          }
        }
        
        String replace = m.group(1);
        
      /*  if(m.group(5) != null || m.group(2) == null) {
          if(m.group(4).toLowerCase().endsWith("studier") || m.group(4).toLowerCase().endsWith("dozier")) {
            replace = m.group(4).substring(0,m.group(4).length()-3)+"enten";
            
            if(m.group(5) != null && m.group(5).equals("r")) {
              replace = replace.substring(0,replace.length()-2);
            }
          }
          else if(m.group(4).toLowerCase().endsWith("kunstschaff")) {
            replace = m.group(4).substring(0,m.group(4).length()-10)+"ünstler";
          }
          else if(m.group(4).toLowerCase().endsWith("forsch") || m.group(4).toLowerCase().endsWith("lehr") 
              || m.group(4).toLowerCase().endsWith("bewohn") || m.group(4).toLowerCase().endsWith("besuch")
              || m.group(4).toLowerCase().endsWith("eit") || m.group(4).toLowerCase().endsWith("ütz")
              || m.group(4).toLowerCase().endsWith("fahr")) {
            replace = m.group(4)+"er";
          }
          else if(m.group(4).toLowerCase().endsWith("zufußgeh")) {
            replace = m.group(4).substring(0,m.group(4).length()-8);
            
            if(Character.isUpperCase(m.group(4).charAt(m.group(4).length()-8))) {
              replace += "Fußgänger";
            }
            else {
              replace += "fußgänger";
            }
          }
          
          
          if(!replace.equals(m.group(1))) {
            if(m.group(2) != null) {
              replace = m.group(2)+" "+replace;
              
              if((m.group(2).toLowerCase().equals("den") || m.group(2).toLowerCase().equals("bei")) && replace.toLowerCase().endsWith("er")) {
                replace += "n";
              }
            }
            if(m.group(6) != null && !m.group(6).trim().isEmpty()) {
              replace += m.group(6);
            }
            
           // System.out.println(m.group(1) + " " + m.group(6) + " " + replace);
            mCountPartizip++;
          }
        }*/
        
        if(m.group(4).toLowerCase().endsWith("studier") || m.group(4).toLowerCase().endsWith("dozier")) {
          replace = m.group(1).substring(0,m.group(1).length()-m.group(3).length()+1) + m.group(4).substring(1,m.group(4).length()-3)+"ent";
          
          if((m.group(2) == null && (m.group(5) == null || (m.group(5) != null && m.group(5).equals("n")))) || 
              (m.group(2) != null && (m.group(5) != null && m.group(5).equals("n")))) {
            replace += "en";
          }
        }
        else if(m.group(4).toLowerCase().endsWith("kunstschaff")) {
          replace = m.group(1).substring(0,m.group(1).length()-m.group(3).length()+1) + m.group(4).substring(1,m.group(4).length()-10)+"ünstler";
        }
        else if(m.group(4).toLowerCase().endsWith("forsch") || m.group(4).toLowerCase().endsWith("lehr") 
            || m.group(4).toLowerCase().endsWith("bewohn") || m.group(4).toLowerCase().endsWith("besuch")
            || m.group(4).toLowerCase().endsWith("eit") || m.group(4).toLowerCase().endsWith("ütz")
            || m.group(4).toLowerCase().endsWith("fahr") || m.group(4).toLowerCase().endsWith("arbeitnehm")) {
          replace = m.group(1).substring(0,m.group(1).length()-m.group(3).length()) + m.group(4)+"er";
        }
        else if(m.group(4).toLowerCase().endsWith("zufußgeh")) {
          replace = m.group(4).substring(0,m.group(4).length()-8);
          
          if(Character.isUpperCase(m.group(4).charAt(m.group(4).length()-8))) {
            replace += "Fußgänger";
          }
          else {
            replace += "fußgänger";
          }
        }
        
        if(!replace.equals(m.group(1))) {
          if(m.group(6) != null && !m.group(6).trim().isEmpty()) {
            replace += m.group(6);
          }
     
          if(m.group(5) == null && m.group(2) != null && (m.group(2).toLowerCase().equals("die") || m.group(2).toLowerCase().endsWith("eine"))) {
            replace += "in";
          }
          else if(m.group(5) != null && m.group(2) != null && (m.group(2).toLowerCase().equals("mit") || m.group(2).toLowerCase().equals("bei") 
              || m.group(2).endsWith("en")) && replace.toLowerCase().endsWith("er")) {
            replace += "n";
          }
          
         // System.out.println(m.group(1) + " " + m.group(6) + " " + replace);
          mCountPartizip++;
        }
        
        if(!VERSION.isStable()) {
          System.out.println(m.group(4) + "   " + replace+"\n");
        }
        
        result.append(text.substring(pos,m.start(1))).append(replace);
        
        pos = m.end();
      }
      
      
      
      if(pos < text.length()) {
        result.append(text.substring(pos,text.length()));
      }
      
      text = result.toString();
    }
    
    return text;
  }
  
  private String deGender(String text) {
    if(text != null) {
      text = deGenderShort(text);
      text = deGenderLong(text);
      text = deGenderPartizip(text);
    }
    
    return text;
  }
  
  @Override
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt(); //read version
    mRemoveLongForm = in.readBoolean();
    
    if(version >= 3) {
      mReplacePartizip = in.readBoolean();
      mCountPartizip = in.readInt();
    }
    
    if(version >= 2) {
      mCountShort = in.readInt();
      mCountLong = in.readInt();
    }
    
    int n = in.readInt();
    
    mChannelSet = new HashSet<Channel>();
    
    for(int i = 0; i < n; i++) {
      Channel ch = Channel.readData(in, true);
      
      if(ch != null) {
        mChannelSet.add(ch);
      }
    }
  }
  
  @Override
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(3); //version
    out.writeBoolean(mRemoveLongForm);
    out.writeBoolean(mReplacePartizip);
    out.writeInt(mCountPartizip);
    out.writeInt(mCountShort);
    out.writeInt(mCountLong);
    
    out.writeInt(mChannelSet.size());
    
    for(Channel ch : mChannelSet) {
      ch.writeData(out);
    }
  }
  
  @Override
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private JCheckBox mRemoveLongGendered;
      private JCheckBox mReplacePartizipCb;
      private SelectableItemList<Channel> mChannelSelection;
      
      @Override
      public void saveSettings() {
        mRemoveLongForm = mRemoveLongGendered.isSelected();
        mReplacePartizip = mReplacePartizipCb.isSelected();
        mChannelSet.clear();
        mChannelSet.addAll(mChannelSelection.getSelectionList());
      }
      
      @Override
      public String getTitle() {
        return getInfo().getName();
      }
      
      @Override
      public Icon getIcon() {
        return null;
      }
      
      @Override
      public JPanel createSettingsPanel() {
        mRemoveLongGendered = new JCheckBox("Gender-Langform (z.B. Nutzerinnen und Nutzer) entfernen",mRemoveLongForm);
        mReplacePartizipCb = new JCheckBox("Häufig verwendete Partizipformen (z.B. Studierende) ersetzen",mReplacePartizip);
        mChannelSelection = new SelectableItemList<Channel>(mChannelSet.toArray(new Channel[0]), getPluginManager().getSubscribedChannels(),true);
        
        EnhancedPanelBuilder pb = new EnhancedPanelBuilder("5dlu,default:grow");
        pb.addRow("5dlu", false);
        pb.addRow(false);
        pb.add(mRemoveLongGendered, CC.xy(2, pb.getRowCount()));
        pb.addRow(false);
        pb.add(mReplacePartizipCb, CC.xy(2, pb.getRowCount()));
        pb.addRow("5dlu", false);
        pb.addRow(false);
        pb.addSeparator("Sender", CC.xyw(1, pb.getRowCount(), 2));
        pb.addRow("5dlu", false);
        pb.addRow("fill:default:grow", false);
        pb.add(mChannelSelection, CC.xy(2, pb.getRowCount()));
        pb.addRow("10dlu", false);
        pb.addRow(false);
        pb.addSeparator("Statistik des letzten Datenupdates", CC.xyw(1, pb.getRowCount(), 2));
        pb.addRow("5dlu", false);
        pb.addRow(false);
        pb.add(new JLabel("Ersetzte Kurzformen (*,:,I,_): "+mCountShort), CC.xy(2, pb.getRowCount()));
        pb.addRow(false);
        pb.add(new JLabel("Ersetzte Langformen: "+mCountLong), CC.xy(2, pb.getRowCount()));
        pb.addRow(false);
        pb.add(new JLabel("Ersetzte Partizipien: "+mCountPartizip), CC.xy(2, pb.getRowCount()));
        
        return pb.getPanel();
      }
    };
  }
}
