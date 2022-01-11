package captureplugin.drivers.dreambox.connector.cs;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class E2LocationHelper {
  // Logger
  private static final Logger mLog = Logger.getLogger(E2LocationHelper.class.getName());
  // Class
  private static final Map<String, E2LocationHelper> singletonMap = new HashMap<String, E2LocationHelper>();
  // Member
  private List<String> mLocations = null;
  private final DreamboxConnector mConnector;
  private final Thread mThreadWaitFor;
  private Thread mThread;

  /**
   * Factory
   * 
   * @param connector
   *          for dreambox
   * @param thread
   *          to wait for
   * 
   * @return locationThread
   * 
   */
  public static E2LocationHelper getInstance(DreamboxConnector connector, Thread thread) {
    String id = connector.getConfig().getId();
    E2LocationHelper singleton = null;
    synchronized (singletonMap) {
      singleton = singletonMap.get(id);
      if (singleton == null) {
        singleton = new E2LocationHelper(connector, thread);
        singletonMap.put(id, singleton);
      }
    }
    return singleton;
  }

  /**
   * Konstruktor
   * 
   * @param connector
   * @param thread
   */
  private E2LocationHelper(DreamboxConnector connector, Thread thread) {
    mLog.setLevel(Level.INFO);    
    this.mConnector = connector;
    this.mThreadWaitFor = thread;
    this.mLocations = null;
    run(connector.getConfig().getDreamboxAddress());
  }
  
  public String getDefaultLocation() {
      return mConnector.getConfig().getDefaultLocation();
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
   * get locations
   * 
   * @return locations
   */
  public synchronized List<String> getLocations() {
    if (mThread.isAlive()) {
        try {
            mThread.join();
        } catch (InterruptedException e) {
            while ((mThread.getState() == Thread.State.RUNNABLE)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    mLog.log(Level.WARNING, "InterruptedException", e1);
                }
            }
        }
    }
    if (mLocations == null) {
      run(mConnector.getConfig().getDreamboxAddress());
    }
    try {
      mThread.join();
    } catch (InterruptedException e) {
      while ((mThread.getState() == Thread.State.RUNNABLE) && (mLocations == null)) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e1) {
          mLog.log(Level.WARNING, "InterruptedException", e1);
        }
      }
    }
    return mLocations;
  }
  

  public synchronized List<String> getLocations(String dreamboxAddress) {
    if (mThread.isAlive()) {
        try {
            mThread.join();
        } catch (InterruptedException e) {
            while ((mThread.getState() == Thread.State.RUNNABLE)) {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e1) {
                  mLog.log(Level.WARNING, "InterruptedException", e1);
                }
            }
        }
    }
    run(dreamboxAddress);
    try {
      mThread.join();
    } catch (InterruptedException e) {
      while ((mThread.getState() == Thread.State.RUNNABLE) && (mLocations == null)) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e1) {
          mLog.log(Level.WARNING, "InterruptedException", e1);
        }
      }
    }

    List<String> locations = mLocations;
    mLocations = null;
    return locations;
  }

  /**
   * read locations from dreambox
   */
  private void run(final String dreamboxAddress) {
    mThread = new Thread() {

      @Override
      public void run() {
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
            data = mConnector.getDataForLocalUrl("/web/getlocations", "Error getting location from box "+mConnector.getConfig().getDreamboxAddress());
            
            E2ListItemHandler handler;
            if (data.indexOf("e2location") != -1) {
              handler = new E2ListItemHandler("e2location");
            } else if (data.indexOf("e2simplexmlitem") != -1) {
              handler = new E2ListItemHandler("e2simplexmlitem");
            } else {
              data = "<e2locations><e2location>/hdd/movie/</e2location></e2locations>";
              handler = new E2ListItemHandler("e2location");
            }
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(new StringReader(data)), handler);

            mLocations = handler.getList();

          } catch (ParserConfigurationException e) {
            mLog.log(Level.WARNING, "ParserConfigurationException", e);
          } catch (SAXException e) {
            mLog.warning(data);
            mLog.log(Level.WARNING, "SAXException", e);
          } catch (MalformedURLException e) {
            mLog.log(Level.WARNING, "MalformedURLException", e);
          } catch (SocketTimeoutException e) {
            mLog.log(Level.WARNING, "SocketTimeoutException", e);
          } catch (IOException e) {
            mLog.log(Level.WARNING, "IOException", e);
          } catch (IllegalArgumentException e) {
            mLog.log(Level.WARNING, "IllegalArgumentException", e);
          }

          mLog.info("[" + mConnector.getConfig().getDreamboxAddress() + "] " + "GET getlocations - "
              + (new GregorianCalendar().getTimeInMillis() - cal.getTimeInMillis()) + " ms");
        }
    };
    mThread.start();
  }

}
