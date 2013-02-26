package se.su.it.svc.manager

import org.apache.log4j.Logger
import org.apache.log4j.LogManager
import org.apache.log4j.Level

public class SukatLogManager {


  public SukatLogManager() {
    String logfile = System.getProperty("log.file")
    if(logfile != null) {
      ((org.apache.log4j.DailyRollingFileAppender)LogManager.getRootLogger().getAppender("A")).setFile(logfile)
      ((org.apache.log4j.DailyRollingFileAppender)LogManager.getRootLogger().getAppender("A")).activateOptions()
    }

    if(System.getProperty("DEBUG") != null) {
      LogManager.getRootLogger().setLevel(Level.DEBUG)
    }
  }
}
