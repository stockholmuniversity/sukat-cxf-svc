/*
 * Copyright (c) 2013, IT Services, Stockholm University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Stockholm University nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

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

  /**
   * WEB (online order)
   */
  private final int DEFAULT_ORDER_STATUS = 3

  /** Find all card orders for <b>uid</b> */
  public static final findAllCardsQuery = "SELECT r.id, serial, owner, printer, " +
      "createTime, firstname, lastname, streetaddress1, " +
      "streetaddress2, locality, zipcode, value, description " +
      "FROM request r JOIN address a ON r.address = a.id" +
      " JOIN status s ON r.status = s.id WHERE r.owner = :uid"

  /**
   * Find all active card orders for <b>owner</b>
   */
  public static final findActiveCardOrdersQuery = "SELECT r.id, serial, owner, printer, createTime, firstname, " +
      "lastname, streetaddress1, streetaddress2, locality, zipcode, value, description " +
      "FROM request r JOIN address a ON r.address = a.id " +
      "JOIN status s ON r.status = s.id WHERE r.owner = :owner AND status in (1,2,3)"

  /**
   * Insert into <i>address</i> values <b>streetaddress1</b>, <b>streetaddress2</b>,
   *  <b>locality</b> & <b>zipcode</b>
   */
  public static final insertAddressQuery = "INSERT INTO address VALUES(null, :streetaddress1, " +
      ":streetaddress2, :locality, :zipcode)"

  /**
   * Insert into <i>request</i> values <b>id</b>, <b>owner</b>, <b>serial</b>,
   *  <b>printer</b>, <b>createTime</b>, <b>address</b>, <b>status</b>, <b>firstname</b> &
   *  <b>lastname</b>
   */
  public static final insertRequestQuery = "INSERT INTO request VALUES(:id, :owner, :serial, " +
      ":printer, :createTime, :address, :status, :firstname, :lastname)"

  /**
   * Insert into <i>status_history</i> values <b>status</b>, <b>request</b>, <b>comment</b> &
   *  <b>createTime</b>
   */
  public static final insertStatusHistoryQuery = "INSERT INTO status_history VALUES " +
      "(null, :status, :request, :comment, :createTime)"

  /**
   * Update <i>request</i> with new <b>discardedStatus</b> for <b>id</b>
   */
  public static final markCardAsDiscardedQuery = "UPDATE request SET status = :discardedStatus WHERE id = :id"

  /**
   * Find <i>id</i> from <i>request</i> for <b>uuid</b>
   */
  public static final findFreeUUIDQuery = "SELECT id FROM request WHERE id = :uuid"

  public List findAllCardOrdersForUid(String uid) {

    ArrayList cardOrders = []

    if (uid) {
      log.info "Querying card orders for uid: $uid"

      List rows = doListQuery(findAllCardsQuery, [uid:uid]) ?: []

      log.info "Found ${rows?.size()} order entries in the database for $uid."

      cardOrders = handleOrderListResult(rows)
    }

    return cardOrders
  }

  /**
   * Accepts a cardOrderVO and returns a UUID reference to the created card.
   * cardOrderVO needs to contain
   * @param cardOrderVO
   * @return
   */
  public String orderCard(SvcCardOrderVO cardOrderVO) {
    String uuid = null

    try {
      Map addressArgs = getAddressQueryArgs(cardOrderVO)
      Map requestArgs = getRequestQueryArgs(cardOrderVO)

      Closure queryClosure = { Sql sql ->
        if (!sql) { return false }

        def cardOrders = sql?.rows(findActiveCardOrdersQuery, [owner:cardOrderVO.owner])

        log.debug "Active card orders returned: ${cardOrders?.size()}"

        if (cardOrders?.size() > 0) {
          log.error "Can't order new card since an order already exists."
          for (order in cardOrders) {
            log.debug "Order: $order"
          }
          return false
        }

        uuid = findFreeUUID(sql)
        requestArgs.id = uuid

        try {
          sql.withTransaction {
            doCardOrderInsert(sql, addressArgs, requestArgs)
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
      log.error "Failed to create card order for ${cardOrderVO?.owner}", ex
      return null
    }

    log.info "Returning $uuid"

    return uuid
  }
  /**
   * Marks
   * @param uuid
   * @param uid
   * @return
   */
  public boolean markCardAsDiscarded(String uuid, String uid) {
    Closure queryClosure = { Sql sql ->
      try {
        sql.withTransaction {
          doMarkCardAsDiscarded(sql, uuid, uid)
        }
      } catch (ex) {
        log.error "Failed to mark card as discarded in sucard db.", ex
        return false
      }
      return true
    }

    return (withConnection(queryClosure)) ? true : false
  }

  private boolean doCardOrderInsert(Sql sql, Map addressArgs, Map requestArgs) {
    String addressQuery = insertAddressQuery
    String requestQuery = insertRequestQuery
    String statusQuery = insertStatusHistoryQuery

    log.debug "Sending: $addressQuery with arguments $addressArgs"
    def addressResponse = sql?.executeInsert(addressQuery, addressArgs)
    log.debug "Address response is $addressResponse"
    def addressId = addressResponse[0][0]
    log.debug "Recieved: $addressId as response."

    /** Get the address id and set it as the request address id. */
    requestArgs['address'] = addressId
    log.debug "Sending: $requestQuery with arguments $requestArgs"
    sql?.executeInsert(requestQuery, requestArgs)
    String comment = "Created by " + requestArgs?.owner + " while activating account"

    def statusResponse = sql?.executeInsert(statusQuery,
        [status:DEFAULT_ORDER_STATUS,
            request:requestArgs.id,
            comment: comment,
            createTime:new Timestamp(new Date().getTime())
        ])

    log.debug "Status response: $statusResponse"
    return true
  }

  private Map getRequestQueryArgs(SvcCardOrderVO cardOrderVO) {
    /** id and address will be set later in the process and serials should be unset. */
    return [
        id: null,
        owner: cardOrderVO.owner,
        serial: null,
        printer: cardOrderVO.printer,
        createTime: new Timestamp(new Date().getTime()),
        firstname: cardOrderVO.firstname,
        lastname: cardOrderVO.lastname,
        address: null,
        status: DEFAULT_ORDER_STATUS
    ]
  }

  private static Map getAddressQueryArgs(SvcCardOrderVO cardOrderVO) {
    return [
        streetaddress1: cardOrderVO.streetaddress1,
        streetaddress2: cardOrderVO.streetaddress2,
        locality: cardOrderVO.locality,
        zipcode: cardOrderVO.zipcode
    ]
  }

  private static String findFreeUUID(Sql sql) {
    String uuid = null
    boolean newUUID = false

    while (!newUUID) {
      uuid = UUID.randomUUID().toString()
      log.info "findFreeUUID: Querying for uuid: ${uuid}"

      def rows = sql.rows(findFreeUUIDQuery, [uuid: uuid])

      if (rows?.size() == 0) {
        newUUID = true
      } else {
        log.info "${uuid} was already taken, retrying."
      }
    }
    return uuid
  }
  /**
   *
   * @param sql
   * @param uuid
   * @param uid
   * @return
   */
  private static boolean doMarkCardAsDiscarded(Sql sql, String uuid, String uid) {
    sql?.executeUpdate(markCardAsDiscardedQuery, [id:uuid])
    sql?.executeInsert(insertStatusHistoryQuery, [
        status:5,
        request: uuid,
        comment: "Discarded by " + uid,
        createTime: new Timestamp(new Date().getTime())
    ])
    return true
  }

  private withConnection = { Closure query ->
    def response = null
    Sql sql = null
    try {
      sql = new Sql(suCardDataSource as BasicDataSource)
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

  private List doListQuery(String query, Map args) {
    Closure queryClosure = { Sql sql ->
      if (!sql) { return null }
      return sql?.rows(query, args)
    }

    return withConnection(queryClosure)
  }

  private static ArrayList handleOrderListResult(List rows) {
    def cardOrders = []

    for (row in rows) {
      try {
        SvcCardOrderVO svcCardOrderVO = new SvcCardOrderVO(row as GroovyRowResult)
        cardOrders << svcCardOrderVO
      } catch (ex) {
        log.error "Failed to add order $row to orders.", ex
      }
    }
    cardOrders
  }
}
