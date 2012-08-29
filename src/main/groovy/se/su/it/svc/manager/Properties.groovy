package se.su.it.svc.manager

/**
 * Created by: Jack Enqvist (jaen4109)
 * Date: 2012-08-28 ~ 09:13
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
