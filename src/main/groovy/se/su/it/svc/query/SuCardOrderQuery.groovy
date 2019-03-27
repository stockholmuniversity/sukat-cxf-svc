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

import se.su.it.svc.commons.SvcCardOrderVO
import se.su.it.svc.commons.SvcCardOrderHistoryVO

import java.sql.Timestamp

@Slf4j
class SuCardOrderQuery {

  def suCardSql

  /**
   * Constant WEB (online order)
   */
  public static final String STATUS_DEFAULT_ORDER = 'WEB'

  /**
   * Constant DISCARDED (discarded card)
   */
  public static final String STATUS_DISCARDED = 'DISCARDED'

    /**
     * Find card order by <b>uuid</b>
     */
    public static final findCardOrderByUuidQuery = "SELECT r.id, serial, owner, createTime, " +
        "firstname, lastname, streetaddress1, streetaddress2, locality, zipcode, value, description " +
        "FROM request r LEFT JOIN address a ON r.address = a.id " +
        "JOIN status s ON r.status = s.id WHERE r.id = :uuid"

  /**
   * Find all card orders for <b>uid</b>
   */
  public static final findAllCardsQuery = "SELECT r.id, r.serial, r.owner, r.printer, " +
      "r.createTime, r.firstname, r.lastname, a.streetaddress1, " +
      "a.streetaddress2, a.locality, a.zipcode, s.value, s.description " +
      "FROM request r LEFT JOIN address a ON r.address = a.id" +
      " JOIN status s ON r.status = s.id WHERE r.owner = :uid"

  /**
   * Find all active card orders for <b>owner</b>
   */
  public static final findActiveCardOrdersQuery = "SELECT r.id, serial, owner, printer, createTime, firstname, " +
      "lastname, streetaddress1, streetaddress2, locality, zipcode, value, description " +
      "FROM request r LEFT JOIN address a ON r.address = a.id " +
      "JOIN status s ON r.status = s.id WHERE r.owner = :owner AND status in (1,2,3)"

    /**
     * Get status history for a card order.
     */
    public static final getStatusHistoryQuery = "SELECT timestamp, s.value, comment " +
    "FROM status_history h " +
    "JOIN status s ON h.status = s.id WHERE request = :uuid"

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
      ":printer, :createTime, :address, (select id from status where value=:status), :firstname, :lastname)"

  /**
   * Insert into <i>status_history</i> values <b>status</b>, <b>request</b>, <b>comment</b> &
   *  <b>createTime</b>
   */
  public static final insertStatusHistoryQuery = "INSERT INTO status_history (status, request, comment, timestamp) " +
                                                 "VALUES ((select id from status where value=:status), :request, :comment, :createTime)"

  /**
   * Update <i>request</i> with new <b>discardedStatus</b> for <b>id</b>
   */
  public static final markCardAsDiscardedQuery = "UPDATE request SET status = (select id from status where value=:status) WHERE id = :id"

  /**
   * Find <i>id</i> from <i>request</i> for <b>uuid</b>
   */
  public static final findFreeUUIDQuery = "SELECT id FROM request WHERE id = :uuid"

    /**
     * Find card order for supplied uuid
     *
     * @param uuid the uuid to find card orders for
     *
     * @return A single card order
     */
    SvcCardOrderVO findCardOrderByUuid(String uuid)
    {
        return suCardSql.firstRow(findCardOrderByUuidQuery, [uuid: uuid])
    }

    /**
     * Get card order history for supplied uuid
     *
     * @param uuid the uuid to find card order history for
     *
     * @return An array of card order history entries
     */
    SvcCardOrderHistoryVO[] getCardOrderHistory(String uuid)
    {
        return suCardSql.rows(getStatusHistoryQuery, [uuid: uuid])
    }

  /**
   * Find all card orders for supplied uid
   *
   * @param uid the uid to find card orders for
   * @return a list of card orders found for the uid
   */
  public List findAllCardOrdersForUid(String uid) {

    ArrayList cardOrders = []

    if (uid) {
      log.info "Querying card orders for uid: $uid"

      List rows = suCardSql?.rows(findAllCardsQuery, [uid:uid]) ?: []

      log.info "Found ${rows?.size()} order entries in the database for $uid."

      cardOrders = handleOrderListResult(rows)
    }

    return cardOrders
  }

  /**
   * Accepts a cardOrderVO and returns a UUID reference to the created card.
   * cardOrderVO needs to contain: <b>owner</b>, <b>streetaddress1, <b>streetaddress2</b>,
   * <b>locality</b>, <b>zipcode</b>, <b>printer</b>, <b>firstname</b> & <b>lastname</b>
   *
   * @param cardOrderVO the card order to create a new card for
   * @return the UUID for the new card. Returns false if no card could be created.
   */
  public String orderCard(SvcCardOrderVO cardOrderVO) throws Exception {
    String uuid = null

    try {
      Map addressArgs = getAddressQueryArgs(cardOrderVO)
      Map requestArgs = getRequestQueryArgs(cardOrderVO)

      if (suCardSql) {
        def cardOrders = suCardSql?.rows(findActiveCardOrdersQuery, [owner:cardOrderVO.owner])

        log.debug "Active card orders returned: ${cardOrders?.size()}"

        if (cardOrders?.size() > 0) {
          String errorMsg = "Can't order new card since an order already exists."
          log.error errorMsg
          for (order in cardOrders) {
            log.debug "Order: $order"
          }
          throw new IllegalStateException(errorMsg)
        }

        uuid = findFreeUUID(suCardSql)
        requestArgs.id = uuid

        try {
          doCardOrderInsert(suCardSql, addressArgs, requestArgs)
          log.info "Card order successfully added to database!"
        } catch (ex) {
          log.error "Error in SQL card order transaction.", ex
          throw ex
        }
      }
    } catch (ex) {
      log.error "Failed to create card order for ${cardOrderVO?.owner}", ex
      throw ex
    }

    log.info "Returning $uuid"
    return uuid
  }

  /**
   * Marks a card as discarded
   *
   * @param uuid the UUID of the card to be marked discarded
   * @param uid the uid of the user whom discards the card
   * @return true if the card has been marked as discarded, false if the operation fails.
   */
  public boolean markCardAsDiscarded(String uuid, String uid) {
    try {
      return doMarkCardAsDiscarded(suCardSql, uuid, uid)
    } catch (ex) {
      log.error "Failed to mark card as discarded in sucard db.", ex
      throw ex
    }
  }
  /**
   * Handles the persisting of the card order request.
   *
   * @param sql
   * @param addressArgs
   * @param requestArgs
   * @return true
   */
  private static boolean doCardOrderInsert(Sql sql, Map addressArgs, Map requestArgs) {
    sql.withTransaction {
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
      String comment = "Card order created by " + requestArgs?.owner

      def statusResponse = sql?.executeInsert(statusQuery,
          [status:STATUS_DEFAULT_ORDER,
              request:requestArgs.id,
              comment: comment,
              createTime:new Timestamp(new Date().getTime())
          ])

      log.debug "Status response: $statusResponse"
    }
    return true
  }

  /**
   * Creates a map composed of values partially from the supplied SvcCardOrderVO.
   * createTime and status are supplied from this class and attributes
   * id, address and serial should all be null as they are not to be handled by this method.
   *
   * @param cardOrderVO
   * @return a map with values.
   */
  private static Map getRequestQueryArgs(SvcCardOrderVO cardOrderVO) {
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
        status: STATUS_DEFAULT_ORDER
    ]
  }

  /**
   * Extracts address bound attributes from the supplied SvcCardOrderVO
   *
   * @param cardOrderVO
   * @return a map of address attributes.
   */
  private static Map getAddressQueryArgs(SvcCardOrderVO cardOrderVO) {
    return [
        streetaddress1: cardOrderVO.streetaddress1,
        streetaddress2: cardOrderVO.streetaddress2,
        locality: cardOrderVO.locality,
        zipcode: cardOrderVO.zipcode
    ]
  }

  /**
   * Finds a free UUID for a card order, makes sure the UUID does not already exists in the database.
   *
   * @param sql
   * @return a free UUID
   */
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
   * Marks a card entry as discarded in the database,
   * also handles setting proper status history.
   *
   * @param sql
   * @param uuid
   * @param uid
   * @return true
   */
  private static boolean doMarkCardAsDiscarded(Sql sql, String uuid, String uid) {
    sql.withTransaction {
      sql?.executeUpdate(markCardAsDiscardedQuery, [id:uuid, status: STATUS_DISCARDED])
      sql?.executeInsert(insertStatusHistoryQuery, [
          status: STATUS_DISCARDED,
          request: uuid,
          comment: "Discarded by " + uid,
          createTime: new Timestamp(new Date().getTime())
      ])
    }
    return true
  }

  /**
   * Creates a list of SvcCardOrderVOs from the sql retrieved from the database.
   *
   * @param rows
   * @return a list of SvcCardOrderVO objects.
   */
  private static ArrayList handleOrderListResult(List rows) throws Exception {
    def cardOrders = []

    for (row in rows) {
      try {
        SvcCardOrderVO svcCardOrderVO = new SvcCardOrderVO(row as GroovyRowResult)
        cardOrders << svcCardOrderVO
      } catch (ex) {
        log.error "Failed to add order $row to orders.", ex
        throw ex
      }
    }
    cardOrders
  }
}
