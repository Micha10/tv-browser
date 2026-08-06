package captureplugin.drivers.dreambox.connector.cs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * @author fishhead
 * 
 */
public class FtpHelper implements ProtocolCommandListener {

  // Logger
  private static final Logger LOG = Logger
      .getLogger(FtpHelper.class.getName());

  private static final String ENCODING = "UTF-8";

  private static final String NAME = "name";
  private static final String SIZE = "size";
  
  private FTPClient mClient;
  private StringBuilder mReceived;
  private StringBuilder mSent;
  
  public FtpHelper() {
    mClient = new FTPClient();
    mClient.setControlEncoding(ENCODING);
    mClient.addProtocolCommandListener(this);
    mReceived = new StringBuilder();
    mSent = new StringBuilder();
  }
  
  String cmd(String... args) {
    return cmd(true, args);
  }
  
  /**
   * ftp Commands ausfuehren
   * 
   * @param args
   * 
   * @return
   */
  String cmd(boolean log, String... args) {
    mReceived.setLength(0);
    mSent.setLength(0);
    
    String s = null;
    String cmd = args[0];
    try {
      if (cmd.equalsIgnoreCase("OPEN")) {
        // OPEN
        try {
          String host = args[1];
          int port = 21;
          int index = args[1].lastIndexOf(':');

          if(index > 0 && index == args[1].indexOf(':')) {
            host = args[1].substring(0,index);
            port = Integer.parseInt(args[1].substring(index + 1));
          }

          mClient.connect(host, port);
          s = getString(false);
        }catch(Exception e) {
          if(log) {
            LOG.log(Level.SEVERE, "Could not connect to server: " + args[1], e);
          }
          s = "Could not connect to server: " + args[1];
        }
      } else if (cmd.equalsIgnoreCase("CLOSE")) {
        try {
          if(mClient.isConnected()) {
            mClient.disconnect();
          }
          
          s = getString(false);
        }catch(Exception e) {
          if(log) {
            LOG.log(Level.SEVERE, "Error at disconnection from server", e);
          }
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
          if (!mClient.login(user, password)) {
            throw new IOException("Could not login to server");
          }
          s = getString(false);
        }catch(Exception e) {
          if(log) {
            LOG.log(Level.SEVERE, "Could not login to server", e);
          }
          s = "Could not login to server";
        }
      } else if (cmd.equalsIgnoreCase("CD")) {
        // CD
        try {
          if (!mClient.changeWorkingDirectory(args[1])) {
            throw new IOException("Could not change directory");
          }
          s = getString(false);
        }catch(Exception e) {
          if(log) {
            LOG.log(Level.SEVERE, "Could not change directory", e);
          }
          s = "Could not change directory";
        }
      } else if (cmd.equalsIgnoreCase("PWD")) {
        // PWD
        try {
          s = mClient.printWorkingDirectory().trim();
        }catch(Exception e) {
          if(log) {
            LOG.log(Level.SEVERE, "Could not get current directory from server", e);
          }
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
    	ByteArrayOutputStream out = null;
        
    	try {
          mClient.setFileType(FTP.BINARY_FILE_TYPE);
          String filename = new String(args[1].getBytes(ENCODING));
          out = new ByteArrayOutputStream();

	        if(mClient.retrieveFile(filename, out)) {
	          s = new String(out.toByteArray(),ENCODING);
	        }
        }catch(Exception e) {
          if(log) {
            LOG.log(Level.SEVERE, "Could not download file from server: " + args[1], e);
          }
        }finally {
			if(out != null){
				try {
					out.close();
				}catch(IOException ioe) {}
			}
		}
      } else if (cmd.equalsIgnoreCase("PUT")) {
        // PUT file
        mClient.setFileType(FTP.BINARY_FILE_TYPE);
        ByteArrayInputStream in = null;
        
        try {
          String filename = new String(args[1].getBytes(ENCODING));
          in = new ByteArrayInputStream(args[2].getBytes(ENCODING));
            mClient.storeFile(filename, in);
        }catch(Exception e) {
          if(log) {
            LOG.log(Level.SEVERE, "Could not upload file: " + args[1], e);
          }
        }finally {
        	if(in != null) {
        		try {
        			in.close();
        		}catch(IOException ioe) {}
        	}
        }
      } else {
        if(log) {
          LOG.warning("unkown command : " + cmd);
          for (int i = 1; i < args.length; i++) {
            LOG.warning(String.format("parameter %4d : %s", i, args[i]));
          }
        }
      }
    } catch (FileNotFoundException e) {
      s = null;
    } catch (IOException e) {
      if(log) {
        LOG.log(Level.WARNING, "IOException", e);
      }
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
      if (!mClient.changeWorkingDirectory(dir)) {
        throw new IOException("Could not change directory");
      }
      FTPFile[] names = mClient.listFiles();
  
      // Parse
      for (FTPFile row : names) {
        Map<String, String> map = new TreeMap<String, String>();
        list.add(map);
        
        map.put(NAME, dir + row.getName());
        Calendar modifiedDate = row.getTimestamp();
        if (modifiedDate != null) {
          map.put("date", String.valueOf(modifiedDate.getTimeInMillis()));
        } else {
          map.put("date", "0");
        }
        map.put(SIZE, String.valueOf(row.getSize()));
        
        if (row.isDirectory()) {
          map.put("type", "DIRECTORY");
        } else if (row.isFile()) {
          map.put("type", "FILE");
        } else if (row.isSymbolicLink()) {
          map.put("type", "LINK");
        } else {
          map.put("type", "UNKNOWN");
        }
      }
    }catch(Exception e) {
      LOG.log(Level.SEVERE, "Could not retrieve files for directory: " + dir, e);
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
  public void protocolReplyReceived(ProtocolCommandEvent event) {
    if (event.getMessage() != null) {
      mReceived.append(event.getMessage());
    }
  }

  @Override
  public void protocolCommandSent(ProtocolCommandEvent event) {
    if (event.getMessage() != null) {
      mSent.append(event.getMessage());
    }
  }
  
  private String getString(boolean sent) {
    String result = sent ? mSent.toString() : mReceived.toString();
    
    if(result.trim().isEmpty()) {
      result = null;
    }
    
    return result;
  }
}
