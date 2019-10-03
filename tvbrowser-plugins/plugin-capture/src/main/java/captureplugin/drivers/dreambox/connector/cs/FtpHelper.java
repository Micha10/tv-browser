package captureplugin.drivers.dreambox.connector.cs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPCommunicationListener;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPFile;

/**
 * @author fishhead
 * 
 */
public class FtpHelper implements FTPCommunicationListener {

  // Logger
  private static final Logger mLog = Logger
      .getLogger(FtpHelper.class.getName());

  private static final String ENCODING = "UTF-8";

  private static final String NAME = "name";
  private static final String SIZE = "size";
  /*private static final String[] FTP_COLS = new String[] { "rights", "type",
      "user", "group", SIZE, "date", "date", "date", NAME };*/
  
  private FTPClient mClient;
  private StringBuilder mReceived;
  private StringBuilder mSent;
  
  public FtpHelper() {
    mClient = new FTPClient();
    mReceived = new StringBuilder();
    mSent = new StringBuilder();
  }

/*  public static void main(String[] args) {
    FtpHelper ftpHelper = new FtpHelper();
    System.out.println(ftpHelper.cmd("OPEN", server));
    System.out.println(ftpHelper.cmd("LOGIN", user, password));
    System.out.println(ftpHelper.cmd("CD", "/bodostv/"));
    
    System.out.println(ftpHelper.cmd("LIST", "/bodostv/"));
    /*try {
      for (Entry<String, String> entry : ftpHelper.getFileSize("/bodostv/")
          .entrySet()) {
        System.out.println(entry.toString());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    /*String s = ftpHelper.cmd("GET", 
        "/hdd/movie/20101225 1439 - Das Erste HD - Rapunzel.ts.meta");
    String t = s;*/
    //mLog.info(t);
/*   System.out.println(ftpHelper.cmd("CLOSE"));
  }*/

  /**
   * ftp Commands ausfuehren
   * 
   * @param args
   * 
   * @return
   */
  String cmd(String... args) {
    mReceived.setLength(0);
    mSent.setLength(0);
    
    String s = null;
    String cmd = args[0];
    try {
      if (cmd.equalsIgnoreCase("OPEN")) {
        // OPEN
        try {
          String[] connect = mClient.connect(args[1]);
          
          for(String c : connect) {
            mReceived.append(c).append("\n");
          }
          
          s = getString(false);
        }catch(Exception e) {
          mLog.log(Level.SEVERE, "Could not connect to server: " + args[1], e);
          s = "Could not connect to server: " + args[1];
        }
      } else if (cmd.equalsIgnoreCase("CLOSE")) {
        try {
          mClient.disconnect(true);
          s = getString(false);
        }catch(Exception e) {
          mLog.log(Level.SEVERE, "Error at disconnection from server", e);
          s = "Error at disconnecting from server";
        }
      } else if (cmd.equalsIgnoreCase("SYSTEM")) {
        // SYSTEM
        s = "NOT SUPPORTED";
      } else if (cmd.equalsIgnoreCase("LOGIN")) {
        // LOGIN
        String user = args[1];
        String password = args[2];
        if (user.length() == 0) {
          user = "root";
        }
        if (password.length() == 0) {
          password = " ";
        }
        
        try {
          mClient.login(user, password);
          s = getString(false);
        }catch(Exception e) {
          mLog.log(Level.SEVERE, "Could not login to server", e);
          s = "Could not login to server";
        }
      } else if (cmd.equalsIgnoreCase("CD")) {
        // CD
        try {
          mClient.changeDirectory(args[1]);
          s = getString(false);
        }catch(Exception e) {
          mLog.log(Level.SEVERE, "Could not change directory", e);
          s = "Could not change directory";
        }
      } else if (cmd.equalsIgnoreCase("PWD")) {
        // PWD
        try {
          s = mClient.currentDirectory().trim();
        }catch(Exception e) {
          mLog.log(Level.SEVERE, "Could not get current directory from server", e);
        }
      } else if (cmd.equalsIgnoreCase("LIST")) {
        // LIST
        s = "";
        for (Map<String, String> map : getListOfFiles(args[1])) {
          for (Entry<String, String> entry : map.entrySet()) {
            s += entry.getKey() + "=" + entry.getValue() + "\t";
          }
          s += "\n";
        }
      } else if (cmd.equalsIgnoreCase("GET")) {
        // GET file
        try {
          mClient.setType(FTPClient.TYPE_BINARY);
          String filename = new String(args[1].getBytes(ENCODING));
          
          try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final AtomicBoolean completed = new AtomicBoolean(false);
            
            mClient.download(filename, out, 0, new FTPDataTransferListener() {
              @Override
              public void transferred(int arg0) {}
              
              @Override
              public void started() {}
              
              @Override
              public void failed() {}
              
              @Override
              public void completed() {
                completed.set(true);
              }
              
              @Override
              public void aborted() {}
            });
            
            if(completed.get()) {
              s = new String(out.toByteArray(),ENCODING);
            }
          }
        }catch(Exception e) {
          mLog.log(Level.SEVERE, "Could not download file from server: " + args[1], e);
        }
      } else if (cmd.equalsIgnoreCase("PUT")) {
        // PUT file
        mClient.setType(FTPClient.TYPE_BINARY);
        try {
          String filename = new String(args[1].getBytes(ENCODING));
          try(ByteArrayInputStream in = new ByteArrayInputStream(args[2].getBytes(ENCODING))) {
            final AtomicBoolean complete = new AtomicBoolean(false);
            
            mClient.upload(filename, in, 0, 0, new FTPDataTransferListener() {
              @Override
              public void transferred(int arg0) {}
              
              @Override
              public void started() {}
              
              @Override
              public void failed() {}
              
              @Override
              public void completed() {
                complete.set(true);
              }
              
              @Override
              public void aborted() {}
            });
          }
        }catch(Exception e) {
          mLog.log(Level.SEVERE, "Could not upload file: " + args[1], e);
        }
      } else {
        mLog.warning("unkown command : " + cmd);
        for (int i = 1; i < args.length; i++) {
          mLog.warning(String.format("parameter %4d : %s", i, args[i]));
        }
      }
    } catch (sun.net.ftp.FtpLoginException e) {
      JOptionPane.showMessageDialog(null, e.getLocalizedMessage(),
          "FTP-Error: " + cmd, JOptionPane.ERROR_MESSAGE);
    } catch (FileNotFoundException e) {
      s = null;
    } catch (IOException e) {
      mLog.log(Level.WARNING, "IOException", e);
    }

    return s;
  }

  /**
   * Rekursiv alle Dateien auflisten
   * 
   * @param dir
   * 
   * @return list files
   * 
   * @throws IOException
   */
  public List<Map<String, String>> getListOfFiles(String dir)
      throws IOException {

    if (!dir.endsWith("/")) {
      dir += "/";
    }
    
    List<Map<String, String>> list = new ArrayList<Map<String, String>>();
    
    // CD
    try {
      mClient.changeDirectory(dir);
      FTPFile[] names = mClient.list();
  
      // Parse
      for (FTPFile row : names) {
        Map<String, String> map = new TreeMap<String, String>();
        list.add(map);
        
        map.put(NAME, dir + row.getName());
        map.put("date", String.valueOf(row.getModifiedDate().getTime()));
        map.put(SIZE, String.valueOf(row.getSize()));
        
        switch(row.getType()) {
          case FTPFile.TYPE_DIRECTORY:map.put("type", "DIRECTORY"); break;
          case FTPFile.TYPE_FILE:map.put("type", "FILE"); break;
          case FTPFile.TYPE_LINK:map.put("type", "LINK"); break;
        }
      }
    }catch(Exception e) {
      mLog.log(Level.SEVERE, "Could not retrieve files for directory: " + dir, e);
    }
    return list;
  }

  /**
   * get filename and size
   * 
   * @param dir
   * @return map filename/size
   * @throws IOException
   */
  public Map<String, String> getFileSize(String dir) throws IOException {
    Map<String, String> mapFileSize = new TreeMap<String, String>();
    for (Map<String, String> map : getListOfFiles(dir)) {
      mapFileSize.put(map.get(NAME), map.get(SIZE));
    }
    return mapFileSize;
  }

  @Override
  public void received(String s) {
    mReceived.append(s).append("\n");
  }

  @Override
  public void sent(String s) {
    mSent.append(s).append("\n");
  }
  
  private String getString(boolean sent) {
    String result = sent ? mSent.toString() : mReceived.toString();
    
    if(result.trim().isEmpty()) {
      result = null;
    }
    
    return result;
  }
}
