package util.io.windows.uac;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import util.browserlauncher.Launch;
import util.exc.TvBrowserException;
import util.io.ExecutionHandler;

/**
 * Starts program with Windows UAC.
 * 
 * @author René Mach
 * @since 4.3
 */
public class UACStarter {
  private static final UACStarter INSTANCE = new UACStarter();
  private static final File WSCRIPT = new File(System.getenv("windir")+File.separator+(System.getProperty("os.arch").contains("64") ? "SysWOW64" : "System32")+File.separator+"wscript.exe");
  
  private UACStarter() {}
  
  public static boolean isUsable() {
    return Launch.isWindows() && WSCRIPT.isFile();
  }
  
  public static final UACStarter getInstance() throws TvBrowserException {
    if(!isUsable()) {
      throw new TvBrowserException(UACStarter.class, "noUac", "No UAC useable");
    }
    
    return INSTANCE;
  }
  
  public void startApplication(final String appPath, final String... args) {
    try {
      File vbs = File.createTempFile("uacstarter",".vbs");
      
      try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(vbs), "ISO-8859-1"))) {
        out.write("Set UAC = CreateObject(\"Shell.Application\")\r\n");
        out.write("UAC.ShellExecute ");
        out.write("\"");
        out.write(appPath);
        out.write("\"");
        
        if(args != null) {
          out.write(", '");
          for(int i = 0; i < args.length; i++) {
            if(i > 0) {
              out.write(" ");
            }
            
            out.write("\"");
            out.write(args[i]);
            out.write("\"");            
          }
          out.write("'");
        }
        else {
          out.write(", \"\"");
        }
        
        out.write(", \"\", \"runas\", 0");
      }catch(IOException ioe) {
        ioe.printStackTrace();
      }
      
      ExecutionHandler h = ExecutionHandler.create(WSCRIPT.getAbsolutePath(),vbs.getAbsolutePath());
      h.execute(true,true);
      try {
        h.getProcess().waitFor();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
