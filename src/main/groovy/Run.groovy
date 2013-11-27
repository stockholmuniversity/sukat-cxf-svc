import groovy.util.logging.Slf4j
import se.su.it.svc.server.Server

@Slf4j
class Run extends Server {
  public static void main(String[] args) {
    try {
      log.info "Initializing Jetty server."
      new Run().start()
    } catch (ex) {
      log.error "Failed to start Jetty server: ${ex.message}", ex
      System.exit(1)
    }
  }
}
