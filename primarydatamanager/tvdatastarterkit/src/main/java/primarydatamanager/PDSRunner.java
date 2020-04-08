package primarydatamanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import primarydatamanager.primarydataservice.PrimaryDataService;


public class PDSRunner {
  
  private CopyOnWriteArrayList<PrimaryDataService> mPDSList;
  private AtomicInteger mActiveThreadCount = new AtomicInteger(0);
  private File mLogDir, mRawDir;
  private static Properties mProperties;
  private Thread mWaitingThread;
  
  private static final int MAX_EXECUTION_MINUTES = 90;
  private static final int CONCURRENT_DOWNLOADS=5;
  
  private static final Logger mLog = Logger.getLogger(PDSRunner.class.getName());
  
  public PDSRunner(File baseDir) {
    Logger.getLogger("sun.awt.X11.timeoutTask.XToolkit").setLevel(Level.INFO);
    mPDSList=new CopyOnWriteArrayList<PrimaryDataService>();
    mRawDir=new File(baseDir,"raw");
    mLogDir=new File(baseDir,"pdslog");
    mProperties=new Properties();
  }
  
  public void addPDS(PrimaryDataService pds) {
    mPDSList.add(pds);
  }
  
  public void setLogDir(File logDir) {
    mLogDir=logDir;
  }
  
  public void setRawDir(File rawDir) {
    mRawDir=rawDir;
  }
  
  public void runAllPrimaryDataServices() throws PreparationException {
     
    if (!mRawDir.exists()) {
      if (!mRawDir.mkdirs()) {
        throw new PreparationException("Could not create directory "+mRawDir.getAbsolutePath());
      }
    }
     
    if (!mLogDir.exists()) {
      if (!mLogDir.mkdirs()) {
        throw new PreparationException("Could not create directory "+mLogDir.getAbsolutePath());
      }
    }
     
     
    
    mActiveThreadCount.set(0);
    
    //  Set the max. connections
    if (CONCURRENT_DOWNLOADS > 5) {
      System.setProperty("http.maxConnections", Integer.toString(CONCURRENT_DOWNLOADS));
    }
    
    for (int i=0;i<CONCURRENT_DOWNLOADS;i++) {
      Thread downloadThread = new Thread("PDS runner Thread") {
        public void run() {
          PDSThreadRun();
        }
      };
      downloadThread.start();
    }
    
    //  Wait until all jobs are processed
    final AtomicBoolean isFinished = new AtomicBoolean(false);
    final AtomicBoolean isLocked = new AtomicBoolean(false);
    final long start = System.currentTimeMillis();
    final long waitTime = (mPDSList.size()+1) * MAX_EXECUTION_MINUTES * 60000l;
    
    mWaitingThread = new Thread("Waiting for PDS running to finish") {
      public void run() {
        do {
          try {
            Thread.sleep(30*60000);
          } catch (InterruptedException exc) {}
          
          synchronized (mPDSList) {
            isFinished.set((mPDSList.isEmpty() && mActiveThreadCount.get() <= 0));
          }
          
          if(System.currentTimeMillis()-start > waitTime) {
            isLocked.set(true);
          }
        } while (! isFinished.get() && !isLocked.get());
      };
    };
    mWaitingThread.start();
    try {
      mWaitingThread.join();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    if(isLocked.get()) {
      System.exit(1);
    }
  }
  
  


  private void PDSThreadRun() {
    mActiveThreadCount.incrementAndGet();

    boolean isFinished = false;
    // Get the next job
    final AtomicReference<PrimaryDataService> pds = new AtomicReference<PrimaryDataService>(null);
    
    do {
      synchronized (mPDSList) {
        if (mPDSList.isEmpty()) {
          isFinished = true;
        } else {
          pds.set(mPDSList.remove(0));
        }
      }
      
      if (!isFinished && pds.get() != null) {
        final String dir = mRawDir.getAbsolutePath();
        File logFile=new File(mLogDir,pds.get().getClass().getName()+".txt");
        try {
          FileOutputStream out=new FileOutputStream(logFile);
          final PrintStream errOut=new PrintStream(out);
          if (mProperties != null) {
            pds.get().setParameters(mProperties);
          }
          
          final AtomicBoolean isRunning = new AtomicBoolean(true);
          final AtomicBoolean thereWereErrors = new AtomicBoolean(false);
          final long start = System.currentTimeMillis();
          
          final Thread execute = new Thread("Execute PDS Thread, wait for errors") {
            @Override
            public void run() {
              boolean error = pds.get().execute(dir, errOut);
              thereWereErrors.set(error);
              isRunning.set(false);
            }
          };
          execute.start();
          
          while(execute.isAlive() && isRunning.get() && System.currentTimeMillis()-start < (MAX_EXECUTION_MINUTES * 60000l)) {
            Thread.sleep(30000);
          }
          
          if (thereWereErrors.get() || isRunning.get()) {
            mLog.warning("There were errors during the execution of primary "
                + "data service " + pds.get().getClass().getName() +" still running: "+ isRunning.get() + ". See log file: "
                + logFile.getAbsolutePath());
          }else{
            mLog.fine(pds.get().getClass().getName()+ " terminated normally");
            logFile.delete();
          }
          errOut.close();
        }catch(Exception exc) {
          mLog.log(Level.SEVERE, "Error executing primary data service "+pds.get().getClass().getName(), exc);
        }
      }
      
      pds.set(null);
    } while (! isFinished);
    
    mActiveThreadCount.decrementAndGet();
  }

  
  
  public static void main(String[] args) throws PreparationException {
    
    PDSRunner pdsRunner=new PDSRunner(new File("."));
    
    if (args.length==0) {
      System.out.println("usage: PDSRunner [-raw directory] [-log directory] [-PDSparameterX valueX] [...] pds ...");
      System.exit(1);
    }
    
    for (int i=0;i<args.length;i++) {
      
      if ("-raw".equalsIgnoreCase(args[i])) {
        if ((i + 1) >= args.length) {
           System.out.println("You have to specify the raw directory");
           System.exit(1);
        } else {
           i++;
           pdsRunner.setRawDir(new File(args[i]));
        }
      }
      else if ("-log".equalsIgnoreCase(args[i])) {
        if ((i + 1) >= args.length) {
          System.out.println("You have to specify the log directory");
          System.exit(1);
        }
        else {
          i++;
          pdsRunner.setLogDir(new File(args[i]));
        }
      }
      else if (args[i].startsWith("-")) {
        if ((i + 1) >= args.length) {
          System.out.println("You have to specify a value for the parameter "+args[i]);
          System.exit(1);
        }
        else {
          mProperties.setProperty(args[i].substring(1), args[i+1]);
          i++;
        }
      }
      else {
        String[] classes = args[i].split(",");
        for (final String clas:classes) {
            try {
                pdsRunner.addPDS(PrimaryDataManager.createPrimaryDataService(clas));
            } catch (PreparationException e) {
                mLog.log(Level.SEVERE, "A PDS Class with the name " + clas + " could not be initialised ", e);
            }
        }
      }
    }
    
    
    pdsRunner.runAllPrimaryDataServices();
    System.exit(0);
  }
  
  
}