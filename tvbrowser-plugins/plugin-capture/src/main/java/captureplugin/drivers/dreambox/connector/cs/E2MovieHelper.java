package captureplugin.drivers.dreambox.connector.cs;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
public class E2MovieHelper {

  /** Sendername */
  public static final String SERVICENAME = "e2servicename";
  /** Sender-Reference */
  public static final String SERVICEREFERENCE = "e2servicereference";
  /** Filmtitel */
  public static final String TITLE = "e2title";
  /** Filmbeschreibung */
  public static final String DESCRIPTION = "e2description";
  /** Filmbeschreibung erweitert */
  public static final String DESCRIPTIONEXTENDED = "e2descriptionextended";
  /** Filmbeginn */
  public static final String TIME = "e2time";
  /** Filmlaenge */
  public static final String LENGTH = "e2length";
  /** Filmkategorie */
  public static final String TAGS = "e2tags";
  /** Dateiname */
  public static final String FILENAME = "e2filename";
  /** Dateigroesse */
  public static final String FILESIZE = "e2filesize";

  // Logger
  private static final Logger mLog = Logger.getLogger(E2MovieHelper.class.getName());
  // Class
  private static final Map<String, E2MovieHelper> singletonMap = new HashMap<String, E2MovieHelper>();
  // Member
  private List<Map<String, String>> mMovies = null;
  private Set<String> mTags = null;
  private final DreamboxConnector mConnector;
  private Thread mThread;
  private final Thread mThreadWaitFor;
  private String mDirname;

  /**
   * Factory
   * 
   * @param connector
   *          for dreambox
   * @param thread
   *          to wait for
   * 
   * @return movieThread
   */
  public static E2MovieHelper getInstance(DreamboxConnector connector, Thread thread) {
    String id = connector.getConfig().getId();
    E2MovieHelper singleton = null;
    synchronized (singletonMap) {
      singleton = singletonMap.get(id);
      if (singleton == null) {
        singleton = new E2MovieHelper(connector, thread);
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
  private E2MovieHelper(DreamboxConnector connector, Thread thread) {
    mLog.setLevel(Level.INFO);    
    this.mConnector = connector;
    this.mThreadWaitFor = thread;
    this.mDirname = null;
    this.mTags = null;
    this.mMovies = null;
    run();
  }

  /**
   * get movies
   * 
   * @return movies
   */
  public synchronized List<Map<String, String>> getMovies() {
    if (mThread == null || (!mThread.isAlive() && mMovies == null)) {
      run();
    }
    try {
      mThread.join();
    } catch (InterruptedException e) {
      while ((mThread.getState() == Thread.State.RUNNABLE) && (mMovies == null)) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e1) {
          mLog.log(Level.WARNING, "InterruptedException", e1);
        }
      }
    }
    return mMovies;
  }

  /**
   * get tags distinct for comboBox
   * 
   * @return tags
   */
  public Set<String> getTags() {
    if (mTags == null) {
      mTags = new TreeSet<String>();
      mTags.add("");      
      // Movie-Tags
      List<Map<String, String>> movies = getMovies();
      if (movies != null) {
        for (Map<String, String> movie : movies) {
          if ((movie != null) && (movie.containsKey(TAGS))) {
            try {
              mTags.add(movie.get(TAGS));
            } catch (NullPointerException e) {
              // Fehlerhaftes Tag, also ignorieren
            }
          }
        }
      }
      // Timer-Tags
      List<Map<String, String>> timers = E2TimerHelper.getInstance(mConnector).getTimers();
      if (timers != null) {
        for (Map<String, String> timer : timers) {
          if ((timer != null) && (timer.containsKey(E2TimerHelper.TAGS))) {
            try {
              mTags.add(timer.get(E2TimerHelper.TAGS));
            } catch (NullPointerException e) {
              // Fehlerhaftes Tag, also ignorieren
            }
          }
        }
      }
      // Property-Tags
      String genres = System.getProperty("captureplugin.E2MovieHelper.genres");
      if (genres != null) {
        for (String genre : genres.split("[,]")) {
          mTags.add(genre);
        }
      }
    }
    return mTags;
  }
  
  private String getCurrentDirectory() {
    return mDirname != null ? mDirname : mConnector.getConfig().getDefaultLocation();
  }

  /**
   * read movies from dreambox
   */
  private synchronized void run() {
    mThread = new Thread() {
      @Override
      public void run() {

        if (mMovies == null) {
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
          String url = "";
          String directory = getCurrentDirectory();
          
          try {
            url = "/web/movielist?dirname=" + URLEncoder.encode(directory, "UTF-8") + "&tag=";
            data = mConnector.getDataForLocalUrl(url, "Error reading movies from box " + mConnector.getConfig().getDreamboxAddress(), false);
                        
            if(DreamboxConnector.testXmlData(data,"<e2movielist>")) {
              E2ListMapHandler handler = new E2ListMapHandler("e2movielist", "e2movie");
              SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
              saxParser.parse(new InputSource(new StringReader(data)), handler);
  
              mMovies = handler.getList();
              Map<String, String> mapFileSize = null;
              
              // correct filesize
              try {
                FtpHelper ftpHelper = new FtpHelper();
                ftpHelper.cmd("OPEN", mConnector.getConfig().getDreamboxAddressWithoutProtocol());
                ftpHelper.cmd("LOGIN", mConnector.getConfig().getUserName(), mConnector.getConfig().getPassword());
                mapFileSize = ftpHelper.getFileSize(directory);
                ftpHelper.cmd("CLOSE");
              }catch(Exception ftpe) {
                mapFileSize = new HashMap<String, String>(0);
              }
              
              for (int i = mMovies.size()-1; i >= 0; i-- ) {
                Map<String,String> movie = mMovies.get(i);
                String e2filename = movie.get(FILENAME);
                
                if (mapFileSize.containsKey(e2filename)) {
                  movie.put(FILESIZE, mapFileSize.get(e2filename));
                }
              }
            }
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

          mLog.info("[" + mConnector.getConfig().getDreamboxAddress() + "] " + "GET movielist " + url + " - "
              + (new GregorianCalendar().getTimeInMillis() - cal.getTimeInMillis()) + " ms - " + getCurrentDirectory());
        }
      }
    };
    mThread.start();
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
   * set location and run
   * 
   * @param location
   */
  public void setLocation(String location) {
    mMovies = null;
    mTags = null;
    mDirname = location;
    run();
  }
  
  public void reset() {
    mMovies = null;
    mTags = null;
    run();
  }
}
