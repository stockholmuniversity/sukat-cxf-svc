import groovy.util.logging.Slf4j
import se.su.it.svc.Start

@Slf4j
class Run extends Start {
  public static void main(String[] args) {

    println "Before"

    try {
      // load default config.

      log.info "Loading default configuration."
      URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
      URL defaultConfigUrl = cl.getResource("WEB-INF/classes/defaultApplicationConfig.groovy");
      ConfigObject config = new ConfigSlurper().parse(defaultConfigUrl)



      // merge custom config.
      log.info "Loading custom configuration."
      File configFile = new File(System.getProperty('config'))
      if (configFile) {
        log.info "Custom config file found => ${configFile.absolutePath}"
        URL configUrl = configFile.toURI().toURL()
        ConfigObject customConfig = new ConfigSlurper().parse(configUrl)
        config.merge(customConfig)
      }

      Properties properties = config.toProperties()

      log.info "Initializing Jetty server."
      start(properties)
    } catch (ex) {
      System.err.println ex.message
    }

    println "After"
  }
}
