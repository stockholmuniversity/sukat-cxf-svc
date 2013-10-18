import groovy.util.logging.Slf4j
import se.su.it.svc.server.Start

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
      URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
      URL defaultConfigUrl = cl.getResource("WEB-INF/classes/defaultApplicationConfig.groovy");
      ConfigObject config = new ConfigSlurper().parse(defaultConfigUrl)

      this.config = config

      // merge custom config.
      File configFile = new File(System.getProperty('config'))
      if (configFile) {
        URL configUrl = configFile.toURI().toURL()
        ConfigObject customConfig = new ConfigSlurper().parse(configUrl)
        this.config.merge(customConfig)
      }
    }
    return config
  }
}
