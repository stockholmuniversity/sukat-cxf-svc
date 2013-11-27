import ch.qos.logback.classic.net.SyslogAppender

import static ch.qos.logback.classic.Level.INFO

/** To see logback status messages on console uncomment the line below */
//statusListener OnConsoleStatusListener

scan('5 minutes')  // Scan for changes every 5 minutes.

appender('SYSLOG', SyslogAppender) {
  syslogHost = "127.0.0.1"
  facility = "USER"
  suffixPattern = "sukat-svc: %-5level [%thread] %logger{0} - %msg"
}

root INFO, ['SYSLOG']
