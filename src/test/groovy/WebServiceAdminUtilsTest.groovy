import org.apache.log4j.Level
import se.su.it.svc.util.WebServiceAdminUtils

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2013-04-08
 * Time: 12:03
 * To change this template use File | Settings | File Templates.
 */
class WebServiceAdminUtilsTest extends spock.lang.Specification {

  def "getLogLevelFromString: Happy Path log levels supplied level is #level results in #expected" () {
    when:
    Level result = WebServiceAdminUtils.getLogLevelFromString(level)

    then:
    result.toString() == expected

    where:
    level   | expected
    'all'   | 'ALL'
    'trace' | 'TRACE'
    'debug' | 'DEBUG'
    'info'  | 'INFO'
    'warn'  | 'WARN'
    'fatal' | 'FATAL'
    'error' | 'ERROR'
    'off'   | 'OFF'
    'gris'  | 'INFO'
    null    | 'INFO'
    ''      | 'INFO'
  }
  def "getFunctionByteFromLoggerAndString: Happy Path log levels supplied level is #loggerString in #level results in #expected" () {
    when:
    byte result = WebServiceAdminUtils.getFunctionByteFromLoggerAndString(loggerString,level)

    then:
    result == expected

    where:
    loggerString  | level   | expected
    null          | ''      | 0
    ''            | ''      | 0
    'gris'        | 'gris'  | 0

    'root'        | 'all'   | 1
    'root'        | 'trace' | 2
    'root'        | 'debug' | 3
    'root'        | 'info'  | 4
    'root'        | 'warn'  | 5
    'root'        | 'fatal' | 6
    'root'        | 'error' | 7
    'root'        | 'off'   | 8
    'root'        | 'gris'  | 4
    'root'        | null    | 0
    'root'        | ''      | 0

    'app'        | 'all'   | 9
    'app'        | 'trace' | 10
    'app'        | 'debug' | 11
    'app'        | 'info'  | 12
    'app'        | 'warn'  | 13
    'app'        | 'fatal' | 14
    'app'        | 'error' | 15
    'app'        | 'off'   | 16
    'app'        | 'gris'  | 12
    'app'        | null    | 0
    'app'        | ''      | 0

    'jetty'      | 'all'   | 17
    'jetty'      | 'trace' | 18
    'jetty'      | 'debug' | 19
    'jetty'      | 'info'  | 20
    'jetty'      | 'warn'  | 21
    'jetty'      | 'fatal' | 22
    'jetty'      | 'error' | 23
    'jetty'      | 'off'   | 24
    'jetty'      | 'gris'  | 20
    'jetty'      | null    | 0
    'jetty'      | ''      | 0

    'spring'     | 'all'   | 25
    'spring'     | 'trace' | 26
    'spring'     | 'debug' | 27
    'spring'     | 'info'  | 28
    'spring'     | 'warn'  | 29
    'spring'     | 'fatal' | 30
    'spring'     | 'error' | 31
    'spring'     | 'off'   | 32
    'spring'     | 'gris'  | 28
    'spring'     | null    | 0
    'spring'     | ''      | 0

    'cxf'       | 'all'   | 33
    'cxf'       | 'trace' | 34
    'cxf'       | 'debug' | 35
    'cxf'       | 'info'  | 36
    'cxf'       | 'warn'  | 37
    'cxf'       | 'fatal' | 38
    'cxf'       | 'error' | 39
    'cxf'       | 'off'   | 40
    'cxf'       | 'gris'  | 36
    'cxf'       | null    | 0
    'cxf'       | ''      | 0
  }
}
