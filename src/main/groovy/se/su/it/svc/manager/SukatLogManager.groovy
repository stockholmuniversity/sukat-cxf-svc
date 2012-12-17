package se.su.it.svc.manager

import org.apache.log4j.Logger
import org.apache.log4j.LogManager
import org.apache.log4j.Level

/**
 * This class specifies the ldap integration<br />
 * We have 2 directories that use different sukat servers.<br />
 * LDAP_RO are the slave servers and we use them to do searches and queries.<br />
 * LDAP_RW is the master server, we use this one for modifications of data on sukat.<br />
 */
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
