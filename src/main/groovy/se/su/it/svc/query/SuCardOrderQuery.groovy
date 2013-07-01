package se.su.it.svc.query

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.apache.commons.dbcp.BasicDataSource

@Slf4j
class SuCardOrderQuery {

  def suCardDataSource

  public static findAllCardOrders() {
    def resp = runQuery("SELECT count(id) FROM request", [:])
    log.error "resp $resp"
  }

  private static runQuery(String query , Map args) {

    Closure queryClosure = { Sql sql ->
      if (!sql) { return null }
      return sql?.rows(query, args)
    }

    return withConnection(queryClosure)
  }

  private static withConnection = { Closure query ->
    def response = null
    Sql sql = null
    try {
      /** getDataSource added for mock and testing purposes */
      sql = new Sql(suCardDataSource as BasicDataSource)
      response = query(sql)
    } catch (ex) {
      log.error "Connection to LADOK failed", ex
    } finally {
      try {
        sql.close()
      } catch (ex) {
        log.error "Failed to close connection", ex
      }
    }
    return response
  }
}
