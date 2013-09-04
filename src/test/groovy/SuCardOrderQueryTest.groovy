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
import org.apache.commons.dbcp.BasicDataSource
import org.junit.After
import org.junit.Before
import org.junit.Test
import se.su.it.svc.commons.SvcCardOrderVO
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

public class SuCardOrderQueryTest extends Specification {

  @Shared
  SuCardOrderQuery service

  @Before
  void setup() {
    service = new SuCardOrderQuery()
    service.suCardDataSource = Mock(BasicDataSource)
  }

  @After
  void cleanup() {
    service = null
    Sql.metaClass = null
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

  @Test
  void "getFindAllCardsQuery"() {
    expect: 'should return'
    service.findAllCardsQuery == "SELECT r.id, serial, owner, printer, createTime, firstname, lastname, streetaddress1," +
        " streetaddress2, locality, zipcode, value, description FROM request r JOIN address a " +
        "ON r.address = a.id JOIN status s ON r.status = s.id WHERE r.owner = :uid"
  }

  @Test
  void "doListQuery"() {
    given:
    def list = ['list', 'of', 'objects']

    Sql.metaClass.rows = { String arg1, Map arg2 ->
      return list
    }

    when:
    def resp = service.doListQuery("String", [map:'map'])

    then:
    resp == list
  }

  @Test
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

  @Test
  void "handleOrderListResult: Handling single failures"() {
    given:
    def list = []
    list << new GroovyRowResult([id:1, owner:'foo'])
    list << new GroovyRowResult([id:2, name:'kaka', owner:'bar'])
    list << new GroovyRowResult([id:3, owner:'bar'])

    when:
    def resp = service.handleOrderListResult(list)

    then:
    resp.every { it instanceof SvcCardOrderVO }

    and: 'VO has no attribute kaka so creating that object will fail.'
    resp.size() == 2
  }

  @Test
  void "findAllCardOrdersForUid: given uid => \'#uid\'"() {
    expect:
    [] == service.findAllCardOrdersForUid(uid)

    where:
    uid << ['', null]
  }

  @Test
  void "findAllCardOrdersForUid"() {
    given:
    def list = [[id:1, owner:'foo'], [id:1, owner:'foo'], [id:1, owner:'foo']]

    Sql.metaClass.rows = { String arg1, Map arg2 ->
      return list
    }

    when:
    def resp = service.findAllCardOrdersForUid('someUid')

    then:
    resp?.size() == 3
    resp.every { it instanceof SvcCardOrderVO }
  }

  @Test
  void "getAddressQuery"() {
    expect:
    service.insertAddressQuery == "INSERT INTO address VALUES(null, :streetaddress1, :streetaddress2, :locality, :zipcode)"
  }

  @Test
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
    resp.status == service.DEFAULT_ORDER_STATUS

  }

  @Test
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

  @Test
  void "findFreeUUID"() {
    given:
    Sql.metaClass.rows = { String arg1, Map arg2 ->
      return []
    }

    when:
    def resp = service.findFreeUUID(new Sql(service.suCardDataSource))

    then:'Expect a UUID back (should be 36 chars long)'
    resp instanceof String
    resp?.size() == 36
  }

  @Test
  void "getInsertRequestQuery"() {
    expect:
    service.insertRequestQuery == "INSERT INTO request VALUES(:id, :owner, :serial, :printer, :createTime, :address, :status, :firstname, :lastname)"
  }

  @Test
  void "getFindActiveCardOrdersQuery"() {
    expect:
    service.findActiveCardOrdersQuery == "SELECT r.id, serial, owner, printer, createTime, firstname, lastname, streetaddress1, streetaddress2, locality, zipcode, value, description FROM request r JOIN address a ON r.address = a.id JOIN status s ON r.status = s.id WHERE r.owner = :owner AND status in (1,2,3)"
  }

  @Test
  void "getFindFreeUUIDQuery"() {
    expect:
    service.findFreeUUIDQuery == "SELECT id FROM request WHERE id = :uuid"
  }

  @Test
  void "getInsertStatusHistoryQuery"() {
    expect:
    service.insertStatusHistoryQuery == "INSERT INTO status_history VALUES (null, :status, :request, :comment, :createTime)"
  }

  @Test
  void "orderCard: a failed request"() {
    expect:
    service.orderCard(null) == null
  }
  @Test
  void "orderCard: When there are active orders"() {
    given:
    Sql.metaClass.rows = { String arg1, Map arg2 ->
      if (arg1 == service.findActiveCardOrdersQuery) {
        return 1
      }
    }

    expect:
    null == service.orderCard(cardOrder)
  }

  @Test
  void "orderCard"() {
    given:
    Sql.metaClass.rows = { String arg1, Map arg2 ->
      switch(arg1) {
        case service.findActiveCardOrdersQuery:
          return []
        case service.findFreeUUIDQuery:
          return []
        default:
          return []
      }
    }

    Sql.metaClass.executeInsert = { String arg1, Map arg2 ->
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

  @Test
  def "doCardOrderInsert (is tested through orderCard but closure removes coverage)."(){
    given:
    Sql.metaClass.withTransaction = { Closure closure -> closure() }
    Sql.metaClass.executeInsert = { String arg1, Map arg2 ->
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
        new Sql(service.suCardDataSource),
        service.getAddressQueryArgs(cardOrder),
        service.getRequestQueryArgs(cardOrder)
    )
  }

  @Test
  def "getMarkCardAsDiscardedQuery"() {
    expect:
    service.markCardAsDiscardedQuery == "UPDATE request SET status = :discardedStatus WHERE id = :id"
  }

  @Test
  def "doMarkCardAsDiscarded"(){
    given:
    Sql.metaClass.withTransaction = { Closure closure -> closure() }
    Sql.metaClass.executeUpdate = { String arg1, Map arg2 ->
      switch(arg1){
        case service.markCardAsDiscardedQuery:
          return [true]
        default:
          return [false]
      }
    }
    Sql.metaClass.executeInsert = { String arg1, Map arg2 ->
      switch(arg1){
        case service.insertStatusHistoryQuery:
          return [true]
        default:
          return [false]
      }
    }

    when:
    def resp = service.doMarkCardAsDiscarded(
        new Sql(service.suCardDataSource),
        'uuid',
        'uid'
    )

    then:
    resp
  }

  @Test @Ignore //TODO: Fix this test, works in IDE but no when running mvn test.
  def "markCardAsDiscarded"(){
    given:
    Sql.metaClass.withTransaction = { Closure closure ->
      return true
    }
    when:
    def resp = service.markCardAsDiscarded('uuid', 'uid')

    then:
    resp == true
  }

  @Test @Ignore //TODO: Fix this test, works in IDE but no when running mvn test.
  def "markCardAsDiscarded fails"(){
    given:
    Sql.metaClass.withTransaction = { Closure closure ->
      throw new RuntimeException('foo')
    }
    when:
    def resp = service.markCardAsDiscarded('uuid', 'uid')

    then:
    resp == false
  }

}
