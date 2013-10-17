import groovy.util.logging.Slf4j
import se.su.it.svc.Start

@Slf4j
class Run extends Start {

  private static ConfigObject config

  public static void main(String[] args) {
    try {
      log.info "Initializing Jetty server."
      start(configuration.toProperties())
    } catch (ex) {
      System.err.println ex.message
    }
  }

  public static synchronized ConfigObject getConfiguration() {

    if (!config) {
      println "Initializing Configuration"
      println "Loading Default Configuration"
      URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
      URL defaultConfigUrl = cl.getResource("WEB-INF/classes/defaultApplicationConfig.groovy");
      ConfigObject config = new ConfigSlurper().parse(defaultConfigUrl)

      this.config = config

      // merge custom config.
      println "Loading Custom Configuration"
      File configFile = new File(System.getProperty('config'))
      if (configFile) {
        URL configUrl = configFile.toURI().toURL()
        ConfigObject customConfig = new ConfigSlurper().parse(configUrl)
        this.config.merge(customConfig)
      }
      println "Configuration Loaded."
    }
    return config
  }
}
