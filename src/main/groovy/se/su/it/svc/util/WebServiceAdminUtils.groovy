package se.su.it.svc.util

import org.apache.log4j.LogManager
import org.apache.log4j.Level
import org.apache.log4j.Logger

public class WebServiceAdminUtils {
  private static final Logger logger = Logger.getLogger(WebServiceAdminUtils.class)

  public static Level getLogLevelFromString(String level) {
    if(level == null || level.length() <= 0) {
      logger.info("No such log level <null>! Using default log level INFO!")
      return Level.INFO
    }

    switch (level.toLowerCase()) {
      case  "all"   : return Level.ALL
      case  "trace" : return Level.TRACE
      case  "debug" : return Level.DEBUG
      case  "info"  : return Level.INFO
      case  "warn"  : return Level.WARN
      case  "fatal" : return Level.FATAL
      case  "error" : return Level.ERROR
      case  "off"   : return Level.OFF
      default       : logger.info("No such log level <" + level + ">! Using default log level INFO!")
        return Level.INFO
    }
  }
}

