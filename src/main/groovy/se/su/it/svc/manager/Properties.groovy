package se.su.it.svc.manager

import org.apache.log4j.Logger

/**
 * Created by: Jack Enqvist (jaen4109)
 * Date: 2012-08-28 ~ 09:13
 *
 * this Singleton fetches a properties-fle from the filesystem and provides it as a groovy.util.ConfigObject.
 * the properties can then be accessed from the singleton objects field props.
 *
 * example:
 * def props = Properties.getInstance().props
 *
 * def databaseUser     = props.database.user
 * def databasePassword = props.database.password
 * def databaseServer   = props.database.serverURL
 *
 */
class Properties {
  private static final Logger logger = Logger.getLogger(se.su.it.svc.manager.Properties.class)
  private static final Properties INSTANCE = new Properties()
  private static final String CONFFILE = "/local/cxf-server/conf/config.properties"

  public ConfigObject props

  private Properties(){
    loadProperties()
  }

  /**
   *
   * @return the singleton instance containing a groovy.util.ConfigObject called props.
   */
  static getInstance(){
    return INSTANCE
  }

  /**
   * loading the properties file from the filesystem at a predefined path.
   * populates the public member field props with a groovy.util.ConfigObject containing the properties
   */
  private loadProperties(){
    def props = new java.util.Properties()
    //Begin Default Values
    //Database
    props.put("database.url", "jdbc:mysql://localhost/gormtest")
    props.put("database.driver", "com.mysql.jdbc.Driver")
    props.put("database.user", "gormtest")
    props.put("database.password", "gormtest")
    //Ldap
    props.put("ldap.serverro", "ldap://ldap-test.su.se")
    props.put("ldap.serverrw", "ldap://sukat-test-ldaprw02.it.su.se")
    //Ssl
    props.put("ssl.port", 443)
    props.put("ssl.keystore", "cxf-svc-server.keystore")
    props.put("ssl.password", "changeit")
    //Spnego
    props.put("spnego.properties", "spnego.properties")
    props.put("spnego.realm", "SU.SE")
    //Ehcache
    props.put("ehcache.maxElementsInMemory", 10000)
    props.put("ehcache.eternal", false)
    props.put("ehcache.timeToIdleSeconds", 120)
    props.put("ehcache.timeToLiveSeconds", 600)
    props.put("ehcache.overflowToDisk", false)
    props.put("ehcache.diskPersistent", false)
    props.put("ehcache.diskExpiryThreadIntervalSeconds", 120)
    props.put("ehcache.memoryStoreEvictionPolicy", "LRU")
    //End Default Values
    try {
      new File(CONFFILE).withInputStream { stream ->
        props.load(stream)
      }
    } catch (Exception e) {
      logger.warn("Exception when trying to read configuration file " + CONFFILE + ", exception message was: " + e.message + ".")
      logger.warn("Using default properties for application!")
    }
    this.props = new ConfigSlurper().parse(props)
  }




}
