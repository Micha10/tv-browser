package org.apache.log4j;

import java.util.logging.Level;

/**
 * Dummy log4j Logger using java.util.logging.Logger for logging messages.
 * 
 * @author René Mach
 */
public class Logger {
  private java.util.logging.Logger LOG; 
  
  private Logger(Class<?> clazz) {
    LOG = java.util.logging.Logger.getLogger(clazz.getName());
  }
  
  public static Logger getLogger(Class<?> clazz) {
    return new Logger(clazz);
  }
  
  public void info(Object o) {
    LOG.info(o.toString());
  }
  
  public void debug(Object o) {
    LOG.log(Level.INFO, o.toString());
  }
  
  public void error(Object o) {
    LOG.log(Level.SEVERE, o.toString());
  }
}
