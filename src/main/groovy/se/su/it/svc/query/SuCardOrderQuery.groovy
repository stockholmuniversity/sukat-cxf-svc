package se.su.it.svc.query

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.apache.commons.dbcp.BasicDataSource
import se.su.it.svc.commons.SvcCardOrderVO

import java.sql.Timestamp

@Slf4j
class SuCardOrderQuery {

  def suCardDataSource

  private final int DEFAULT_ORDER_STATUS = 3 // WEB (online order)

  public List findAllCardOrdersForUid(String uid) {

    log.info "Querying card orders for uid: $uid"

    def query = "SELECT r.id,serial,owner,printer,createTime,firstname,lastname,streetaddress1,streetaddress2,locality,zipcode,value,description FROM request r JOIN address a ON r.address = a.id JOIN status s ON r.status = s.id WHERE r.owner = :uid"
    def args = [uid:uid]

    Closure queryClosure = { Sql sql ->
      if (!sql) { return null }
      return sql?.rows(query, args)
    }

    def rows = withConnection(queryClosure)

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

  public String orderCard(SvcCardOrderVO cardOrderVO) {
    String uuid = null

    try {
      def addressQuery = "INSERT INTO address VALUES(null, :streetaddress1, :streetaddress2, :locality, :zipcode)"
      def addressArgs = [
          streetaddress1:cardOrderVO.streetaddress1,
          streetaddress2:cardOrderVO.streetaddress2,
          locality:cardOrderVO.locality,
          zipcode:cardOrderVO.zipcode
      ]

      def requestQuery = "INSERT INTO request VALUES(:id, :owner, :serial, :printer, :createTime, :address, :status, :firstname, :lastname)"
      def requestArgs = [
        id: uuid,
        owner: cardOrderVO.owner,
        serial: cardOrderVO.serial,
        printer: cardOrderVO.printer,
        createTime: new Timestamp(new Date().getTime()),
        firstname: cardOrderVO.firstname,
        lastname: cardOrderVO.lastname,
        address: null,
        status: DEFAULT_ORDER_STATUS
      ]

      Closure queryClosure = { Sql sql ->
        if (!sql) { return false }

        def findActiveCardOrdersQuery = "SELECT r.id,serial,owner,printer,createTime,firstname,lastname,streetaddress1,streetaddress2,locality,zipcode,value,description FROM request r JOIN address a ON r.address = a.id JOIN status s ON r.status = s.id WHERE r.owner = :owner AND status in (1,2,3)"
        def findActiveCardOrdersArgs = [owner:cardOrderVO.owner]

        def cardOrders = sql?.rows(findActiveCardOrdersQuery, findActiveCardOrdersArgs)

        log.debug "Active card orders returned: ${cardOrders?.size()}"

        if (cardOrders?.size() > 0) {
          log.error "Can't order new card since an order already exists."
          for (order in cardOrders) {
            log.debug "Order: $order"
          }
          return false
        }

        boolean newUUID = false
        def findFreeUUIDQuery = "SELECT id FROM request WHERE id = :uuid"

        while (!newUUID) {
          uuid = UUID.randomUUID().toString()
          log.info "findFreeUUID: Querying for uuid: ${uuid}"

          def rows = sql.rows(findFreeUUIDQuery, [uuid:uuid])

          if (rows?.size() == 0) {
            newUUID = true
            requestArgs.id = uuid
          } else {
            log.info "${uuid} was already taken, retrying."
          }
        }

        try {
          sql.withTransaction {
            log.debug "Sending: $addressQuery with arguments $addressArgs"
            def addressResponse = sql?.executeInsert(addressQuery, addressArgs)
            log.debug "Address response is $addressResponse"
            def addressId = addressResponse[0][0]
            log.debug "Recieved: $addressId as response."

            /** Get the address id and set it as the request address id. */
            requestArgs['address'] = addressId
            log.debug "Sending: $requestQuery with arguments $requestArgs"
            sql?.executeInsert(requestQuery, requestArgs)
            String comment = "Created by " + cardOrderVO.owner + " while activating account"

            def statusResponse = sql?.executeInsert("INSERT INTO status_history VALUES (null, :status, :request, :comment, :createTime)",
                [status:DEFAULT_ORDER_STATUS,
                    request:uuid,
                    comment: comment,
                    createTime:new Timestamp(new Date().getTime())
                ])

            log.debug "Status response: $statusResponse"
          }
        } catch (ex) {
          log.error "Error in SQL card order transaction.", ex
          return false
        }
        return true
      }

      if (withConnection(queryClosure)) {
        log.info "Card order successfully added to database!"
      }


    } catch (ex) {
      log.error "Failed to create card order for ${cardOrderVO.owner}", ex
      return null
    }

    log.info "Returning $uuid"

    return uuid
  }

  private withConnection = { Closure query ->
    def response = null
    Sql sql = null
    try {
      sql = new Sql(suCardDataSource)
      response = query(sql)
    } catch (ex) {
      log.error "Connection to SuCardDB failed", ex
      throw(ex)
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
