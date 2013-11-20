import groovy.util.logging.Slf4j
import se.su.it.svc.server.Start

@Slf4j
class Run extends Start {

  private static ConfigObject config

  public static final String CONFIG_FILE_PROPERTY = 'config'
  public static final String DEFAULT_CONFIG_FILE_PATH = "WEB-INF/classes/defaultApplicationConfig.groovy"

  public static void main(String[] args) {
    // Prevent ehcache from accessing the internet on startup.
    System.setProperty("net.sf.ehcache.skipUpdateCheck", "true")

    try {
      log.info "Initializing Jetty server."
      start(configuration.toProperties())
    } catch (ex) {
      log.error "Failed to start Jetty server ${ex.message}", ex
      System.exit(1)
    }
  }

  public static synchronized ConfigObject getConfiguration() {
    if (!config) {
      loadConfiguration()
    }
    return config
  }

  private static void loadConfiguration() {
    /** Handle default config */
    URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
    URL defaultConfigUrl = cl.getResource(DEFAULT_CONFIG_FILE_PATH);
    ConfigObject config = new ConfigSlurper().parse(defaultConfigUrl)

    if (!config) {
      throw new IllegalStateException("Failed to load default config file $DEFAULT_CONFIG_FILE_PATH")
    }

    this.config = config

    /** Merge custom config. */
    String configProperty = System.getProperty(CONFIG_FILE_PROPERTY)

    if (!configProperty) {
      log.warn "Using default configuration: No config property '$CONFIG_FILE_PROPERTY' defined."
      return
    }

    File configFile = new File(configProperty)

    if (!configFile?.exists()) {
      log.warn "Using default configuration: Config file $configFile.absolutePath does not exist."
      return
    }

    URL configUrl = configFile.toURI().toURL()
    ConfigObject customConfig = new ConfigSlurper().parse(configUrl)
    this.config.merge(customConfig)
  }
}
