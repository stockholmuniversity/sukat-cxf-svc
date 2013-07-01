package se.su.it.svc.query

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.apache.commons.dbcp.BasicDataSource
import se.su.it.svc.commons.SvcCardOrderVO

@Slf4j
class SuCardOrderQuery {

  def suCardDataSource

  public static List findAllCardOrdersForUid(String uid) {
    def rows = runQuery("SELECT " +
        "r.id, r.serial, r.createTime, r.firstname, r.lastname, r.printer, " +
        "a.streetaddress1, a.streetaddress2, a.locality, a.zipcode, " +
        "" +
        "FROM request r JOIN " +
        "address a ON r.address = a.id JOIN " +
        "status s ON r.status = s.id WHERE " +
        "request.owner = :uid", [uid:uid])

    if (!rows) { return [] }

    List cardOrders = []

    for (row in rows) {
      try {
        cardOrders << new SvcCardOrderVO( row as GroovyRowResult )
      } catch (ex) {
        log.error "Failed to add order $row to orders.", ex
      }

    }

    return cardOrders
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
