package se.su.it.svc.query

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.apache.commons.dbcp.BasicDataSource
import se.su.it.svc.commons.SvcCardOrderVO

@Slf4j
class SuCardOrderQuery {

  def suCardDataSource

  public List findAllCardOrdersForUid(String uid) {

    log.info "Querying card orders for uid: $uid"

    def rows = runQuery("SELECT " +
        "r.id, r.serial, r.createTime, r.firstname, r.lastname, r.printer, " +
        "a.streetaddress1, a.streetaddress2, a.locality, a.zipcode, " +
        "" +
        "FROM request r JOIN " +
        "address a ON r.address = a.id JOIN " +
        "status s ON r.status = s.id WHERE " +
        "request.owner = :uid", [uid:uid])

    if (!rows) { return [] }

    log.info "Found ${rows?.size()} order entries in the database."

    def cardOrders = []

    for (row in rows) {
      try {
        SvcCardOrderVO svcCardOrderVO = new SvcCardOrderVO( row as GroovyRowResult )
        log.debug "Adding card order ${svcCardOrderVO?.id} to $uid's orders."
        cardOrders << svcCardOrderVO
      } catch (ex) {
        log.error "Failed to add order $row to orders.", ex
      }

    }

    return cardOrders
  }

  private runQuery(String query , Map args) {

    Closure queryClosure = { Sql sql ->
      if (!sql) { return null }
      return sql?.rows(query, args)
    }

    return withConnection(queryClosure)
  }

  private withConnection = { Closure query ->
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
