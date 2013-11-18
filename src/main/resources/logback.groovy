import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.net.SyslogAppender
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.status.OnConsoleStatusListener

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO

final String env = getEnvironment()
final ConfigObject logConfig = Run.configuration?.log?."$env"

if(logConfig?.logbackStatusListener)
  displayStatusOnConsole()

scan('5 minutes')  // Scan for changes every 5 minutes.

// TODO: Make this proof of concept somewhat more dynamic.

def displayStatusOnConsole() {
  statusListener OnConsoleStatusListener
}

def setupAppenders = {

  // hostname is a binding variable injected by Logback.
  final String defaultPattern = "%d{HH:mm:ss.SSS} %-5level [${hostname}] %logger - %msg%n"

  // TODO: Make more dynamic, ex init appenders configured from config file. and init with name and class type.

  (getAppenders(logConfig) as List).unique().each { String entry ->
    switch(entry) {
      case 'FILE':
        appender('FILE', FileAppender) {
          file = logConfig?.appenders?.file?.logFile
          encoder(PatternLayoutEncoder) {
            pattern = logConfig?.appenders?.file?.pattern ?: defaultPattern
          }
        }
        break
      case 'CONSOLE':
        appender('CONSOLE', ConsoleAppender) {
          encoder(PatternLayoutEncoder) {
            pattern = logConfig?.appenders?.console?.pattern ?: defaultPattern
          }
        }
        break
      case 'SYSLOG':
        appender('SYSLOG', SyslogAppender) {
          syslogHost = logConfig?.appenders?.syslog?.syslogHost ?: "127.0.0.1"
          facility = logConfig?.appenders?.syslog?.facility ?: "USER"
          suffixPattern = logConfig?.appenders?.syslog?.pattern ?: defaultPattern
          stackTracePattern = "    "
        }
        break
      default:
        System.err.println "Unknown appender: $entry"
    }
  }
}

def setupLoggers = {
  root getLogLevel(logConfig), getAppenders(logConfig)
}

def getAppenders(logConfig) {
  return logConfig?.appenders?.appenders ?: ['CONSOLE']
}

def getLogLevel(logConfig) {
  // TODO: More dynamic setting of Levels
  (logConfig?.debug == "true" ? DEBUG : INFO)
}

def String getEnvironment() {
  System.properties['env'] == 'dev'? 'dev' : 'prod'
}

setupAppenders()
setupLoggers()
