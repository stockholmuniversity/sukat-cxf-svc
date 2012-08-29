package se.su.it.svc.manager

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
  private static final Properties INSTANCE = new Properties()

  public ConfigObject props

  private Properties(){
    loadProperties()
  }

  static getInstance(){
    return INSTANCE
  }

  private loadProperties(){
    def props = new java.util.Properties()
    new File("/local/cxf-server/conf/config.properties").withInputStream { stream ->
      props.load(stream)
    }
    this.props = new ConfigSlurper().parse(props)
  }




}
