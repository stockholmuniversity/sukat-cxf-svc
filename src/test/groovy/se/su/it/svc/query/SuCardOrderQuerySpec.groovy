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

import gldapo.GldapoSchemaRegistry
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import se.su.it.svc.commons.SvcCardOrderVO
import spock.lang.Shared
import spock.lang.Specification

public class SuCardOrderQuerySpec extends Specification {

  @Shared
  SuCardOrderQuery service

  void setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
    service = new SuCardOrderQuery()
    service.suCardSql = Mock(Sql)
  }

  void cleanup() {
    service = null
    GldapoSchemaRegistry.metaClass = null
  }

  private SvcCardOrderVO getCardOrder(){
    return new SvcCardOrderVO(
        id:1,
        owner:'foo',
        serial:'012345',
        printer:'printer',
        firstname:'foo',
        lastname:'kaka',
        streetaddress1: 's1',
        streetaddress2: 's2',
        locality: 'se',
        zipcode: '12345')
  }

    def "findCardOrderByUuid: happy path"()
    {
        setup:
        service.suCardSql.firstRow(*_) >> [id: 1]

        when:
        def resp = service.findCardOrderByUuid("1")

        then:
        resp.id == "1"
    }

    def "getCardOrderHistory: happy path"()
    {
        setup:
        service.suCardSql.rows(*_) >> [[comment: "Ordered by foo"]]

        when:
        def resp = service.getCardOrderHistory("1")

        then:
        resp[0].comment == "Ordered by foo"
    }

  void "getFindAllCardsQuery"() {
    expect: 'should return'
    service.findAllCardsQuery == "SELECT r.id, r.serial, r.owner, r.printer, r.createTime, r.firstname, r.lastname, a.streetaddress1," +
        " a.streetaddress2, a.locality, a.zipcode, s.value, s.description FROM request r LEFT JOIN address a " +
        "ON r.address = a.id JOIN status s ON r.status = s.id WHERE r.owner = :uid"
  }

  void "handleOrderListResult: When creation of objects work"() {
    given:
    def list = []
    list << new GroovyRowResult([id:1, owner:'foo'])
    list << new GroovyRowResult([id:2, owner:'bar'])

    when:
    def resp = service.handleOrderListResult(list)

    then:
    resp.every { it instanceof SvcCardOrderVO }

    and:
    resp.size() == 2
  }

  void "handleOrderListResult: Broken entry containing a property that does not exist in VO results in"() {
    given:
    def list = []
    list << new GroovyRowResult([id:1, owner:'foo'])
    list << new GroovyRowResult([id:2, name:'kaka', owner:'bar'])
    list << new GroovyRowResult([id:3, owner:'bar'])

    when:
    service.handleOrderListResult(list)

    then:
    thrown(MissingPropertyException)
  }

  void "findAllCardOrdersForUid: given uid => \'#uid\'"() {
    expect:
    [] == service.findAllCardOrdersForUid(uid)

    where:
    uid << ['', null]
  }

  void "findAllCardOrdersForUid"() {
    given:
    def list = [[id:1, owner:'foo'], [id:1, owner:'foo'], [id:1, owner:'foo']]

    service.suCardSql.rows(*_) >> {
      return list
    }

    when:
    def resp = service.findAllCardOrdersForUid('someUid')

    then:
    resp?.size() == 3
    resp.every { it instanceof SvcCardOrderVO }
  }

  void "getAddressQuery"() {
    expect:
    service.insertAddressQuery == "INSERT INTO address VALUES(null, :streetaddress1, :streetaddress2, :locality, :zipcode)"
  }

  void "getRequestQueryArgs"() {
    given:
    SvcCardOrderVO svcCardOrderVO = new SvcCardOrderVO(
        id:1,
        owner:'foo',
        serial:'012345',
        printer:'printer',
        firstname:'foo',
        lastname:'kaka'
    )

    when:
    def resp = service.getRequestQueryArgs(svcCardOrderVO)
    then:
    resp.id == null
    resp.owner == svcCardOrderVO.owner
    resp.serial == null
    resp.printer == svcCardOrderVO.printer
    resp.createTime != null
    resp.firstname == svcCardOrderVO.firstname
    resp.lastname == svcCardOrderVO.lastname
    resp.address == null
    resp.status == service.STATUS_DEFAULT_ORDER

  }

  void "getAddressQueryArgs"() {
    given:
    SvcCardOrderVO svcCardOrderVO = new SvcCardOrderVO(streetaddress1: 's1',
        streetaddress2: 's2',
        locality: 'se',
        zipcode: '12345')

    when:
    def resp = service.getAddressQueryArgs(svcCardOrderVO)

    then:
    resp.streetaddress1 == svcCardOrderVO.streetaddress1
    resp.streetaddress2 == svcCardOrderVO.streetaddress2
    resp.locality == svcCardOrderVO.locality
    resp.zipcode == svcCardOrderVO.zipcode
  }

  void "findFreeUUID"() {
    given:
    service.suCardSql.rows(_, _) >> []

    when:
    def resp = service.findFreeUUID(service.suCardSql)

    then:'Expect a UUID back (should be 36 chars long)'
    resp instanceof String
    resp?.size() == 36
  }

  void "getInsertRequestQuery"() {
    expect:
    service.insertRequestQuery == "INSERT INTO request VALUES(:id, :owner, :serial, :printer, :createTime, :address, (select id from status where value=:status), :firstname, :lastname)"
  }

  void "getFindActiveCardOrdersQuery"() {
    expect:
    service.findActiveCardOrdersQuery == "SELECT r.id, serial, owner, printer, createTime, firstname, lastname, streetaddress1, streetaddress2, locality, zipcode, value, description FROM request r LEFT JOIN address a ON r.address = a.id JOIN status s ON r.status = s.id WHERE r.owner = :owner AND status in (1,2,3)"
  }

  void "getFindFreeUUIDQuery"() {
    expect:
    service.findFreeUUIDQuery == "SELECT id FROM request WHERE id = :uuid"
  }

  void "getInsertStatusHistoryQuery"() {
    expect:
    service.insertStatusHistoryQuery == "INSERT INTO status_history (status, request, comment, timestamp) VALUES ((select id from status where value=:status), :request, :comment, :createTime)"
  }

  void "orderCard: a failed request"() {
    when:
    service.orderCard(null)

    then:
    thrown(NullPointerException)
  }

  void "orderCard: When there are active orders"() {
    given:
    service.suCardSql.rows(*_) >> { String arg1, Object[] arg2 ->
      if (arg1 == service.findActiveCardOrdersQuery) {
        return [1]
      }
    }

    when:
    service.orderCard(cardOrder)

    then:
    thrown(IllegalStateException)
  }

  void "orderCard"() {
    given:

    service.suCardSql.withTransaction(*_) >> { Closure closure ->
      closure()
    }

    service.suCardSql.rows(*_) >> {
      return []
    }

    service.suCardSql.executeInsert(_, _) >> { String arg1, Object[] arg2 ->
      switch(arg1){
        case service.insertAddressQuery:
          return [[1]]
        case service.insertRequestQuery:
          return []
        case service.insertStatusHistoryQuery:
          return []
        default:
          return []
      }
    }

    when:
    def resp = service.orderCard(cardOrder)

    then:
    resp instanceof String
    resp?.size() == 36
  }

  def "doCardOrderInsert (is tested through orderCard but closure removes coverage)."(){
    given:
    service.suCardSql.withTransaction(*_) >> { Closure closure -> closure() }
    service.suCardSql.executeInsert(*_) >> { String arg1, Object[] arg2 ->
      switch(arg1){
        case service.insertAddressQuery:
          return [[1]]
        case service.insertRequestQuery:
          return []
        case service.insertStatusHistoryQuery:
          return []
        default:
          return []
      }
    }

    expect:
    service.doCardOrderInsert(
        service.suCardSql,
        service.getAddressQueryArgs(cardOrder),
        service.getRequestQueryArgs(cardOrder)
    )
  }

  def "getMarkCardAsDiscardedQuery"() {
    expect:
    service.markCardAsDiscardedQuery == "UPDATE request SET status = (select id from status where value=:status) WHERE id = :id"
  }

  def "doMarkCardAsDiscarded"(){
    given:
    service.suCardSql.withTransaction(*_) >> { Closure closure -> closure() }
    service.suCardSql.executeUpdate(*_) >> { String arg1, Object[] arg2 ->
      switch(arg1){
        case service.markCardAsDiscardedQuery:
          return 1
        default:
          return 0
      }
    }
    service.suCardSql.executeInsert(*_) >> { String arg1, Object[] arg2 ->
      switch(arg1){
        case service.insertStatusHistoryQuery:
          return [true]
        default:
          return [false]
      }
    }

    when:
    def resp = service.doMarkCardAsDiscarded(
        service.suCardSql,
        'uuid',
        'uid'
    )

    then:
    resp
  }

  def "markCardAsDiscarded"(){
    given:
    GroovySpy(SuCardOrderQuery, global:true)

    SuCardOrderQuery.doMarkCardAsDiscarded(*_) >> {
      return true
    }

    when:
    def resp = new SuCardOrderQuery().markCardAsDiscarded('uuid', 'uid')

    then:
    resp
  }

  def "markCardAsDiscarded fails"(){
    given:
    GroovySpy(SuCardOrderQuery, global:true)

    SuCardOrderQuery.getLog() >> { new Expando(
        error: { String arg1, Throwable arg2 -> }
    ) }

    SuCardOrderQuery.doMarkCardAsDiscarded(*_) >> {
      throw new IllegalStateException("foo")
    }

    when:
    new SuCardOrderQuery().markCardAsDiscarded('uuid', 'uid')

    then:
    thrown(IllegalStateException)
  }

}
