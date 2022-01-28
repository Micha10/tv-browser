package captureplugin.drivers.dreambox.connector.cs;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import captureplugin.drivers.dreambox.connector.DreamboxConnector;

/**
 * @author fishhead
 * 
 */
public class E2InfoHelper {
  // Logger
  private static final Logger mLog = Logger.getLogger(E2InfoHelper.class.getName());
  // Class
  private static Map<String, E2InfoHelper> singletonMap = new HashMap<String, E2InfoHelper>();

  // Member
  private final DreamboxConnector mConnector;
  private final Thread mThreadWaitFor;
  private Thread mThread;
  private List<Map<String, String>> mInfos = null;
  
  /**
   * Factory
   * 
   * @param config
   *          for dreambox
   * @param thread
   *          to wait for
   * 
   * @return infoThread
   */
  public static E2InfoHelper getInstance(DreamboxConnector connector, Thread thread) {
    String id = connector.getConfig().getId();
    E2InfoHelper singleton = null;
    synchronized (singletonMap) {
      singleton = singletonMap.get(id);
      if (singleton == null) {
        singleton = new E2InfoHelper(connector, thread);
        singletonMap.put(id, singleton);
      }
    }
    // Info immer aktuell besorgen
    singleton.run();
    return singleton;
  }

  /**
   * Konstruktor
   * 
   * @param connector
   * @param thread
   */
  private E2InfoHelper(DreamboxConnector connector, Thread thread) {
    mLog.setLevel(Level.INFO);    
    this.mConnector = connector;
    this.mThreadWaitFor = thread;
  }

  /**
   * get thread
   * 
   * @return Thread
   */
  public Thread getThread() {
    return mThread;
  }

  /**
   * get about info
   * 
   * @return about info
   */
  public synchronized List<Map<String, String>> getInfos() {
    if (!mThread.isAlive() && (mInfos == null)) {
      run();
    }

    try {
      mThread.join();
    } catch (InterruptedException e) {
      while ((mThread.getState() == Thread.State.RUNNABLE) && (mInfos == null)) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e1) {
          mLog.log(Level.WARNING, "InterruptedException", e1);
        }
      }
    }
    return mInfos;
  }

  /**
   * read info from dreambox
   */
  private void run() {
    mThread = new Thread() {

      @Override
      public void run() {

        if (mInfos == null) {

          // wait
          if (mThreadWaitFor != null) {
            try {
              mThreadWaitFor.join();
            } catch (InterruptedException e) {
              mLog.log(Level.WARNING, "InterruptedException", e);
            }
          }

          final Calendar cal = new GregorianCalendar();
          String data = "";

          try {
            if(mConnector.isAccessible()) {
              data = mConnector.getDataForLocalUrl("/web/about", "Error getting info from box "+mConnector.getConfig().getDreamboxAddress(), true);
              
              if(DreamboxConnector.testXmlData(data,"<e2abouts>")) {
                E2ListMapHandler handler = new E2ListMapHandler("e2abouts", "e2about");
                SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
                saxParser.parse(new InputSource(new StringReader(data)), handler);
    
                mInfos = handler.getList();
              }
            }
          } catch (ParserConfigurationException e) {
            mLog.log(Level.WARNING, "ParserConfigurationException", e);
          } catch (SAXException e) {
            mLog.log(Level.WARNING, "SAXException", e);
            mInfos = new ArrayList<Map<String, String>>();
            Map<String, String> map = new TreeMap<String, String>();
            map.put("Fehlertext", e.getLocalizedMessage());
            mInfos.add(map);
          } catch (MalformedURLException e) {
            mLog.log(Level.WARNING, "MalformedURLException", e);
          } catch (SocketTimeoutException e) {
            mLog.log(Level.WARNING, "SocketTimeoutException", e);
          } catch (IOException e) {
            mLog.log(Level.WARNING, "IOException", e);
          } catch (IllegalArgumentException e) {
            mLog.log(Level.WARNING, "IllegalArgumentException", e);
          }

          mLog.info("[" + mConnector.getConfig().getDreamboxAddress() + "] " + "GET about - "
              + (new GregorianCalendar().getTimeInMillis() - cal.getTimeInMillis()) + " ms");
        }
      }
    };
    mThread.start();
  }

  public boolean isNewNigma2() {
    //reset infos
    mInfos = null;
    boolean result = false;
    
    List<Map<String,String>> infos = getInfos();
    
    if(infos != null) {
      for(Map<String,String> info : infos) {
        String version = info.get("e2imageversion");
        
        if(version != null) {
          result = version.toLowerCase().startsWith("newnigma2");
          break;
        }
      }
    }
    
    return result;
  }
}
