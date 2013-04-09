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

  public static byte getFunctionByteFromLoggerAndString(String loggerString, String level) {
    if(loggerString == null || loggerString.length() <= 0 || level == null || level.length() <= 0) {
      logger.info("logger/level <null>! Using default log level INFO for root logger!")
      return 0
    }
    switch (loggerString) {

      case "root" : switch (level.toLowerCase()) {
        case  "all"   : return 1
        case  "trace" : return 2
        case  "debug" : return 3
        case  "info"  : return 4
        case  "warn"  : return 5
        case  "fatal" : return 6
        case  "error" : return 7
        case  "off"   : return 8
        default       : logger.info("No such log level <" + level + ">! Using default log level INFO!")
          return 4
      }
      case "app" : switch (level.toLowerCase()) {
        case  "all"   : return 9
        case  "trace" : return 10
        case  "debug" : return 11
        case  "info"  : return 12
        case  "warn"  : return 13
        case  "fatal" : return 14
        case  "error" : return 15
        case  "off"   : return 16
        default       : logger.info("No such log level <" + level + ">! Using default log level INFO!")
          return 12
      }
      case "jetty" : switch (level.toLowerCase()) {
        case  "all"   : return 17
        case  "trace" : return 18
        case  "debug" : return 19
        case  "info"  : return 20
        case  "warn"  : return 21
        case  "fatal" : return 22
        case  "error" : return 23
        case  "off"   : return 24
        default       : logger.info("No such log level <" + level + ">! Using default log level INFO!")
          return 20
      }
      case "spring" : switch (level.toLowerCase()) {
        case  "all"   : return 25
        case  "trace" : return 26
        case  "debug" : return 27
        case  "info"  : return 28
        case  "warn"  : return 29
        case  "fatal" : return 30
        case  "error" : return 31
        case  "off"   : return 32
        default       : logger.info("No such log level <" + level + ">! Using default log level INFO!")
          return 28
      }
      case "cxf" : switch (level.toLowerCase()) {
        case  "all"   : return 33
        case  "trace" : return 34
        case  "debug" : return 35
        case  "info"  : return 36
        case  "warn"  : return 37
        case  "fatal" : return 38
        case  "error" : return 39
        case  "off"   : return 40
        default       : logger.info("No such log level <" + level + ">! Using default log level INFO!")
          return 36
      }
      default : logger.info("No such logger <" + loggerString + ">! Using default root logger setting level to INFO!")
        return 0
    }
  }
}

