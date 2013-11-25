import groovy.util.logging.Slf4j
import se.su.it.svc.manager.ConfigManager
import se.su.it.svc.server.Start

@Slf4j
class Run extends Start {
  public static void main(String[] args) {
    try {
      log.info "Initializing Jetty server."
      start(ConfigManager.getInstance())
    } catch (ex) {
      log.error "Failed to start Jetty server ${ex.message}", ex
      System.exit(1)
    }
  }
}
