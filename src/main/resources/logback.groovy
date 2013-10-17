import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.FileAppender

def config = Run.configuration.log

appender("FILE", FileAppender) {
  file = config.file
  encoder(PatternLayoutEncoder) {
    pattern = "%msg%n"
  }
}

root(INFO, ["FILE"])