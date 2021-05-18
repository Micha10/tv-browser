/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *     $Date: 2009-09-04 11:15:55 +0200 (Fr, 04 Sep 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5953 $
 */
package captureplugin.drivers.defaultdriver.configpanels;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import captureplugin.CapturePlugin;
import captureplugin.drivers.defaultdriver.DeviceConfig;
import devplugin.Channel;
import devplugin.Plugin;
import devplugin.ThemeIcon;
import util.browserlauncher.Launch;
import util.ui.ChannelTableCellRenderer;
import util.ui.ExtensionFileFilter;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

/**
 * This Panel makes it possible to assign external names to channels
 */
public class ChannelPanel extends JPanel {

    /** Translator */
    private static final Localizer LOCALIZER = Localizer.getLocalizerFor(ChannelPanel.class);

    private JTable mChannelTable = new JTable();
    private ChannelTableModel mTableModel;

    /** Settings */
    private DeviceConfig mData;

    /**
     * Creates the Panel
     * @param data Data to use
     */
    public ChannelPanel(DeviceConfig data) {
        mData = data;
        createPanel();
    }
    
    /**
     * creates a JPanel for managing the channels
     */
    private void createPanel() {
      PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default,pref:grow,5dlu","pref,5dlu,fill:default:grow,5dlu,default"),this);
      pb.border(Borders.DIALOG);

      mTableModel = new ChannelTableModel(mData);
      mChannelTable.setModel(mTableModel);
      mChannelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      mChannelTable.getTableHeader().setReorderingAllowed(false);
      mChannelTable.getColumnModel().getColumn(0).setCellRenderer(new ChannelTableCellRenderer());
      mChannelTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
          JPanel background = new JPanel(new FormLayout("default:grow","fill:default:grow"));
          JLabel label = new JLabel(value.toString());
          label.setOpaque(false);
          
          if(isSelected) {
            background.setBackground(table.getSelectionBackground());
            label.setForeground(table.getSelectionForeground());
          }
          else {
            background.setBackground(table.getBackground());
          }
          
          background.add(label, new CellConstraints().xy(1,1));
          
          return background;
        }
      });
      
      JScrollPane sp = new JScrollPane(mChannelTable);

      addAncestorListener(new AncestorListener() {
        public void ancestorAdded(AncestorEvent event) {}

        public void ancestorMoved(AncestorEvent event) {}

        public void ancestorRemoved(AncestorEvent event) {
          if (mChannelTable.isEditing()) {
            TableCellEditor editor = mChannelTable.getCellEditor();
            if (editor != null) {
              editor.stopCellEditing();
            }
          }
        }
      });
      
      JButton tryMapping = new JButton(LOCALIZER.msg("tryMapping", "Try mapping of channels from CSV file with external names"));
      tryMapping.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          tryMapping();
        }
      });
      
      JButton help = new JButton(Localizer.getLocalization(Localizer.I18N_HELP), Plugin.getPluginManager().getIconFromTheme(null, new ThemeIcon("apps", "help-browser", TVBrowserIcons.SIZE_SMALL)));
      help.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(CapturePlugin.getInstance().getSuperFrame()), LOCALIZER.msg("help.message", "The CSV file must contain two colums, the order of the internal and external\nchannels can be controlled with the first line of the file.\n\nINT;EXT as first line signals that the first column contains the internal names (of TV-Browser)\nand the external names (of the recording program) are contained in the second column.\nEXT;INT or if the line to control the order is omitted signals that the first column contains\nthe external names and the second column contains the internale names.\n\nTo separate the columns a semicolon (;) is used. If the name of a channel contains a semicolon\nit can be marked as being text by using a backslash (\\) in front of it. Example: abc\\;xy\n\nDo you want to create a CSV file that contains the names of the TV-Browser channel now?\n(It will be created in the encoding of the system your currently using.)"), Localizer.getLocalization(Localizer.I18N_HELP), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE)) {
            export();
          }
        }
      });
      
      JPanel mapping = new JPanel(new FormLayout("default,5dlu:grow,default","default"));
      mapping.add(tryMapping, CC.xy(1, 1));
      mapping.add(help, CC.xy(3, 1));
        
      pb.addSeparator(LOCALIZER.msg("ChannelNames", "Channel Names"), CC.xyw(1,1,4));
      pb.add(sp, CC.xyw(2,3,2));
      pb.add(mapping, CC.xyw(2,5,2));
    }
    
    private void export() {
      JFileChooser chooser = new JFileChooser();
      chooser.setMultiSelectionEnabled(false);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      
      chooser.setDialogTitle(LOCALIZER.msg("help.save", "Save channel names to CSV file"));
      
      ExtensionFileFilter filter = new ExtensionFileFilter("csv", LOCALIZER.msg("fileType", "Text files (*.csv)"));

      chooser.setFileFilter(filter);
      
      if(JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(ChannelPanel.this)) {
        boolean success = true;
        
        try(FileOutputStream fOut = new FileOutputStream(chooser.getSelectedFile()); BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fOut))) {
          if(chooser.getSelectedFile().isFile()) {
            fOut.getChannel().truncate(0);
          }
          
          out.write("INT;EXT");
          
          for(int i = 0; i < mTableModel.getRowCount(); i++) {
            Channel ch = (Channel)mTableModel.getValueAt(i,0);
            String value = (String)mTableModel.getValueAt(i,1);
            
            out.write("\n");
            out.write(ch.getName().replace(";", "\\;"));
            out.write(";");
            
            if(value != null && !value.trim().isEmpty()) {
              out.write(value.replace(";", "\\;"));
            }
          }
        } catch (IOException e) {
          success = false;
        }
        
        JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(CapturePlugin.getInstance().getSuperFrame()), LOCALIZER.msg(success ? "export.success" : "export.error", success ? "CSV file created successfully" : "Error creating CSV file"), Localizer.getLocalization(success ? Localizer.I18N_INFO : Localizer.I18N_ERROR), success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
      }
    }
    
    /**
     * @since 3.1.1
     */
    private void tryMapping() {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setDialogTitle(LOCALIZER.msg("tryMappingTitle", "Load external channel names from text file"));
      
      ExtensionFileFilter filter = new ExtensionFileFilter("csv", LOCALIZER.msg("fileType", "Text files (*.csv)"));

      chooser.setFileFilter(filter);
      
      if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        String encoding = "UTF-8";
        JRadioButton win = new JRadioButton("Windows (ISO-8859-1)");
        JRadioButton other = new JRadioButton(LOCALIZER.msg("encoding.other", "Other (UTF-8)"));
        JRadioButton dontKnow = new JRadioButton(LOCALIZER.msg("encoding.dontKnow", "Don't know"));
        
        Object[] message = {
            LOCALIZER.msg("encoding.message", "Please select encoding of file:"),
            win,
            other,
            dontKnow
         };
        
        
        if(Launch.getOs() == Launch.OS_WINDOWS) {
          win.setSelected(true);
        }
        else {
          other.setSelected(true);
        }
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(win);
        bg.add(other);
        bg.add(dontKnow);
        
        JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(CapturePlugin.getInstance().getSuperFrame()), message, LOCALIZER.msg("encoding.title", "Encoding of file?"), JOptionPane.QUESTION_MESSAGE);
        
        if(win.isSelected() || (dontKnow.isSelected() && Launch.getOs() == Launch.OS_WINDOWS)) {
          encoding = "ISO-8859-1";
        }
        
        ArrayList<ChannelImport> externalChannelList = new ArrayList<ChannelImport>(0);

        try(BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(chooser.getSelectedFile()),encoding))) {
          String line = in.readLine();
          
          int indexInt = 1;
          
          if(line.equals("INT;EXT")) {
            indexInt = 0;
          }
          else if(!line.equals("EXT;INT")) {
            ChannelImport ch = parseChannel(indexInt, line);
            
            if(ch != null) {
              externalChannelList.add(ch);
            }
          }
          
          while((line = in.readLine()) != null) {
            ChannelImport ch = parseChannel(indexInt, line);
            
            if(ch != null) {
              externalChannelList.add(ch);
            }
          }
        } catch (IOException e) {e.printStackTrace();}
        
        if(!externalChannelList.isEmpty()) {
          guessChannels(externalChannelList);
        }
      }
    }
    
    private ChannelImport parseChannel(int indexInt, String line) {
      ChannelImport result = null;
      String[] parts = {"",""};
      
      if(line.contains(";")) {
        boolean escape = false;
        int index = 0;
        
        for(int i = 0; i < line.length(); i++) {
          if(line.charAt(i) == '\\') {
            escape = true;
          }
          else if(!escape && line.charAt(i) == ';') {
            if(++index > 1) {
              break;
            }
          }
          else {
            parts[index] += line.charAt(i);
            escape = false;
          }
        }
        
        if(!parts[0].isEmpty() && !parts[1].isEmpty()) {
          if(indexInt == 0) {
            result = new ChannelImport(parts[0], parts[1]);
          }
          else if(indexInt == 1) {
            result = new ChannelImport(parts[1], parts[0]);
          }
        }
      }
      
      return result;
    }
    
    /**
     * Taken from WtvcgScheduler2 an changed for CapturePlugin
     * <p>
     * @param externalChannels The names of the external channels.
     * @since 3.1.1
     */
    private void guessChannels(ArrayList<ChannelImport> externalChannels) {
      for(int i = 0; i < mTableModel.getRowCount(); i++) {
        Channel ch = (Channel)mTableModel.getValueAt(i,0);
        
        int foundLength = -1;
        String foundValue = null;
        
        for(int j = externalChannels.size()-1; j >= 0; j--) {
          ChannelImport external = externalChannels.get(j);
          
          if(external.equals(ch.getName())) {
            externalChannels.remove(j);
            foundValue = external.mExternalName;
            break;
          }
          else {
            int length = external.getMachtingLength(ch.getName());
            
            if(length >= 2 && length > foundLength) {
              foundLength = length;
              foundValue = external.mExternalName;
            }
          }
        }
        
        if(foundValue != null) {
          mTableModel.setValueAt(foundValue,i,1);
        }
      }
      
      mChannelTable.repaint();
    }
    
    private static final class ChannelImport {
      private String mInternalName;
      private String mExternalName;
      
      public ChannelImport(String internalName, String externalName) {
        mInternalName = internalName.replaceAll("\\p{Punct}|\\s+","").toLowerCase();
        mExternalName = externalName;
      }
      
      public boolean equals(String internalName) {
        return mInternalName.equals(internalName.replaceAll("\\p{Punct}|\\s+","").toLowerCase());
      }
      
      public int getMachtingLength(String internalName) {
        internalName = internalName.replaceAll("\\p{Punct}|\\s+","").toLowerCase();
        
        int n = Math.min(mInternalName.length(), internalName.length());
        int i = 0;
        
        for(; i < n; i++) {
          if(internalName.charAt(i) != mInternalName.charAt(i)) {
            break;
          }
        }
        
        return i;
      }
    }
}