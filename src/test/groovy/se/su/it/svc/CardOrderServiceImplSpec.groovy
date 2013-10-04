package se.su.it.svc

import gldapo.GldapoSchemaRegistry
import org.gcontracts.PostconditionViolation

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

import org.gcontracts.PreconditionViolation
import se.su.it.svc.commons.SvcCardOrderVO
import se.su.it.svc.query.SuCardOrderQuery
import se.su.it.svc.util.CardOrderServiceUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class CardOrderServiceImplSpec extends Specification {
  @Shared
  CardOrderServiceImpl service

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
    service = new CardOrderServiceImpl()
  }

  void cleanup() {
    service = null
    GldapoSchemaRegistry.metaClass = null
  }

  private static SvcCardOrderVO getCardOrder() {
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

  @Unroll
  void "findAllCardOrdersForUid: given uid: \'#uid\'"(){
    when:
    service.findAllCardOrdersForUid(uid)

    then:
    thrown(PreconditionViolation)

    where:
    uid << [null, '']
  }

  void "findAllCardOrdersForUid: with no card orders."() {
    given:
    service.suCardOrderQuery = Mock(SuCardOrderQuery) {
      1 * findAllCardOrdersForUid(*_) >> []
    }

    expect:
    [] == service.findAllCardOrdersForUid('uid')
  }

  void "findAllCardOrdersForUid: with card orders."() {
    given:
    service.suCardOrderQuery = Mock(SuCardOrderQuery) {
      1 * findAllCardOrdersForUid(*_) >> [new SvcCardOrderVO(id:1), new SvcCardOrderVO(id:2)]
    }

    when:
    def resp = service.findAllCardOrdersForUid('uid')

    then:
    resp.size() == 2
    resp.every { it instanceof SvcCardOrderVO }
  }

  void "orderCard: when given no cardOrder"() {
    when:
    service.orderCard(null)

    then:
    thrown(PreconditionViolation)
  }

  void "orderCard: when vo has errors (id is set)"() {
    given:

    when:
    service.orderCard(cardOrder)

    then:
    thrown(IllegalArgumentException)
  }

    void "orderCard"() {
    given:
    def cardOrder = cardOrder
    cardOrder.id = null
    cardOrder.serial = null

    service.suCardOrderQuery = Mock(SuCardOrderQuery) {
      1 * orderCard(*_) >> UUID.randomUUID()
    }

    when:
    def resp = service.orderCard(cardOrder)

    then:
    resp.size() == 36
  }

  @Unroll
  void "orderCard ensures result"() {
    given:
    GroovyMock(CardOrderServiceUtils, global: true)
    CardOrderServiceUtils.validateCardOrderVO(cardOrder) >> [ hasErrors: false, errors: [] ]

    service.suCardOrderQuery = Mock(SuCardOrderQuery) {
      1 * orderCard(*_) >> uuid
    }

    when:
    service.orderCard(cardOrder)

    then:
    thrown(PostconditionViolation)

    where:
    uuid << [
            null,
            '',
            '*' * 35,
            '*' * 37
    ]
  }
}
