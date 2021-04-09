package degenderplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;

import devplugin.Channel;
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
  private static final HashSet<String> CHANNELS_DEFAULT = new HashSet<String>();
  
  static {
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_main_de_ard");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_DASDING");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_main_de_zdf");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_swr");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_others_at_3sat");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_rbbberlin");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_others_de_phoenix");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_digital_de_zdfinfo");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_digital_de_zdfneo");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_others_de_kika");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_rbb.kultur");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_wdr");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_N.JOY");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_others_de_arte");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_br");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_de_wdr.2");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_de_wdr.3");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_hr");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_hr-info");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_hr1");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_SR.1");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_SR.2");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_SR.3");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_hr2");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_hr3");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_hr4");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_de_wdr.4");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_de_wdr.radio5");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_mdr.jump");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_WDR.Event");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_SWRinfo");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_swrrp");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_ndr1nds");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_ndr1mv");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_ndr1wn");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_ndr2");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_ndr903");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_ndrblue");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_ndr-hh");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_ndrinfo");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_ndrinfospezial");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_ndrkultur");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_ndr-mv");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_ndr");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_ndr-sh");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_swrsr");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_swr1.bw");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_swr1.rp");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_swr2.bw");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_SWR3");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_Antenne.Brandenburg");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_BR.Verkehr");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_mdr1.sachsen");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_mdr1.sachsen-anhalt");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_mdr1.thueringen");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_mdr.info");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_mdr.klassik");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_mdr.figaro");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_mdr-sn");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_mdr");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_mdr-th");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_bfs-nord");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_bfs");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_bayern4");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_Bremen.Eins");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_Bremen.Vier");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_Nordwestradio");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_radiobremen");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_local_de_rbbbrandenburg");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_SWR4.BW");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_ARD-Radio_de_SWR4.RP");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_digital_de_einsextra");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_digital_de_einstfestival");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_austria_at_orf2");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_austria_at_orf2europe");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_austria_at_orf1");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_austria_at_orf3");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_at_ORFRB");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_at_ORFRK");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_ch_drs1");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_ch_drs2");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_ch_drs3");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_ch_drs4");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_ch_drs.musikwelle");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_others_ch_sfdrs1");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_others_ch_sfinfo");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_others_ch_sfdrs2");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_at_ORFRN");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_at_ORFROE");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_at_ORFRS");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_at_ORFRST");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_at_ORFRT");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_at_ORFRV");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_radio_at_ORFRW");
      CHANNELS_DEFAULT.add("tvbrowserdataservice.TvBrowserDataService_austria_at_orfsportplus");
  };
  
  private HashMap<String, String> mSingularReplacement;
  private HashMap<String, String> mPluralReplacement;
  private HashSet<Channel> mChannelSet;
  
  private static final Pattern GENDERED = Pattern.compile("(\\b(?i)(die\\s){0,1}(?-i)\\b(?!Mc)(\\w+?)(?:\\s*[\\*\\:_](?i:i)|I)n(nen){0,1})", Pattern.DOTALL | Pattern.UNICODE_CHARACTER_CLASS); 
  private static final Pattern GENDERED_LONG = Pattern.compile("(\\b([\\w\\-]+?)innen\\b\\s+(?:und|oder)\\s+\\-{0,1}\\b(\\w+?)\\b)|(\\b([\\w\\-]+?)\\b\\s+(?:und|oder)\\s+\\-{0,1}\\b(\\w+?)innen\\b)", Pattern.DOTALL | Pattern.UNICODE_CHARACTER_CLASS);
  private static final Version VERSION = new Version(0,8,false);
  
  private boolean mRemoveLongForm = false;
  
  public DegenderPlugin() {
    mSingularReplacement = new HashMap<String, String>();
    
    mSingularReplacement.put("\u00c4rzt", "Arzt");
    mSingularReplacement.put("B\u00e4uer", "Bauer");
    mSingularReplacement.put("Beamt", "Beamter");
    mSingularReplacement.put("log", "loge");
    
    mPluralReplacement = new HashMap<String, String>();
    
    mPluralReplacement.put("\u00c4rzt","\u00c4rzte");
    mPluralReplacement.put("B\u00e4uer", "Bauern");
    mPluralReplacement.put("Beamt","Beamte");
    mPluralReplacement.put("Architekt", "Architekten");
    mPluralReplacement.put("Pr\u00e4fekt", "Pr\u00e4fekten");
    
    mPluralReplacement.put("ling","linge");
    mPluralReplacement.put("g","gen");
    mPluralReplacement.put("ist", "isten");
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
        if(CHANNELS_DEFAULT.contains(ch.getUniqueId())) {
          mChannelSet.add(ch);
        }
      }
      
      saveMe();
    }
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
  
  private String deGender(String text) {
    if(text != null) {
      Matcher m = GENDERED.matcher(text);
      StringBuilder result = new StringBuilder();
      
      do {
        int pos = 0;
        
        while(m.find(pos)) {
          /*for(int i = 1; i <= m.groupCount(); i++) {
            System.out.println(i+": " + m.group(i));
          }
          System.out.println();*/
          
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
            
            if(!found && !test.endsWith("er")) {
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
            /*if(test.endsWith("ärzt")) {
              replace = replace.replace("Ärzt","Arzt").replace("ärzt","arzt");
            }
            else if(test.endsWith("beamt")) {
              replace = replace.replace("Beamt","Beamter").replace("beamt","beamter");
            }
            else if(test.endsWith("zeug")) {
              replace += "e";
            }
            else if(test.endsWith("bäuer")) {
              replace = replace.replace("Bäuer","Bauer").replace("bäuer","bäuer");
            }*/
            
            if(wrongArticle) {
              if(m.group(2).startsWith("D")) {
                replace = "Der "+replace;
              }
              else {
                replace = "der "+replace;
              }
            }
          }
          
          result.append(text.substring(pos,m.start(1))).append(replace);
          pos = m.end();
        }
        
        if(pos < text.length()) {
          result.append(text.substring(pos,text.length()));
        }
        
        text = result.toString();
        result.setLength(0);
        
        m = GENDERED.matcher(text);
      }while(m.find());
      
      if(mRemoveLongForm) {
        int pos = 0;
        
        m = GENDERED_LONG.matcher(text);
        
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
          }
          else {
            result.append(text.substring(pos,index)).append(needle);
          }
          
          pos = m.end();
        }
        
        if(pos < text.length()) {
          result.append(text.substring(pos,text.length()));
        }
      }
      
      text = result.toString();
    }
    
    return text;
  }
  
  @Override
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); //read version
    mRemoveLongForm = in.readBoolean();
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
    out.writeInt(1); //version
    out.writeBoolean(mRemoveLongForm);
    
    out.writeInt(mChannelSet.size());
    
    for(Channel ch : mChannelSet) {
      ch.writeData(out);
    }
  }
  
  @Override
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private JCheckBox mRemoveLongGendered;
      private SelectableItemList<Channel> mChannelSelection;
      
      @Override
      public void saveSettings() {
        mRemoveLongForm = mRemoveLongGendered.isSelected();
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
        mRemoveLongGendered = new JCheckBox("Gender-Langform (z.B. Nutzerinnen und Nutzer) ebenfalls entfernen",mRemoveLongForm);
        mChannelSelection = new SelectableItemList<Channel>(mChannelSet.toArray(new Channel[0]), getPluginManager().getSubscribedChannels(),true);
        
        EnhancedPanelBuilder pb = new EnhancedPanelBuilder("5dlu,default:grow");
        pb.addRow("5dlu", false);
        pb.addRow(false);
        pb.add(mRemoveLongGendered, CC.xy(2, pb.getRowCount()));
        pb.addRow("5dlu", false);
        pb.addRow(false);
        pb.addSeparator("Sender", CC.xyw(1, pb.getRowCount(), 2));
        pb.addRow("5dlu", false);
        pb.addRow("fill:default:grow", false);
        pb.add(mChannelSelection, CC.xy(2, pb.getRowCount()));
        
        return pb.getPanel();
      }
    };
  }
}
