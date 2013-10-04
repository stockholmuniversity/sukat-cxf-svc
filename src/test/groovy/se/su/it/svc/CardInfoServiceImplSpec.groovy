package se.su.it.svc

import gldapo.GldapoSchemaRegistry
import org.gcontracts.PreconditionViolation
import org.springframework.ldap.core.DistinguishedName

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

import se.su.it.svc.ldap.SuCard
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.query.SuCardQuery
import se.su.it.svc.query.SuPersonQuery
import spock.lang.Specification

class CardInfoServiceImplSpec extends Specification {

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
  }

  def cleanup() {
    SuCardQuery.metaClass = null
    SuPersonQuery.metaClass = null
    GldapoSchemaRegistry.metaClass = null
  }

  def "Test getAllCards with null uid argument"() {
    setup:
    def cardInfoServiceImpl = new CardInfoServiceImpl()

    when:
    cardInfoServiceImpl.getAllCards(null,false)

    then:
    thrown(PreconditionViolation)
  }

  def "Test getAllCards returns list of SuCard when person exists"() {
    setup:
    def person = new SuPerson()
    person.metaClass.getDn = { new DistinguishedName("") }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> person }

    GroovyMock(SuCardQuery, global: true)
    1 * SuCardQuery.findAllCardsBySuPersonDnAndOnlyActiveOrNot(*_) >> [new SuCard()]

    when:
    def ret = new CardInfoServiceImpl().getAllCards("testuid",true)

    then:
    ret.size() == 1
    ret[0] instanceof SuCard
  }

  def "Test getAllCards throws exception if person doesn't exist"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> throw new IllegalArgumentException("foo") }

    when:
    new CardInfoServiceImpl().getAllCards("testuid",true)

    then:
    thrown(IllegalArgumentException)
  }

  def "Test that getAllCards ensures SuCard[]"() {
    setup:
    def person = new SuPerson()
    person.metaClass.getDn = { new DistinguishedName("") }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> person }

    GroovyMock(SuCardQuery, global: true)
    1 * SuCardQuery.findAllCardsBySuPersonDnAndOnlyActiveOrNot(*_) >> null

    when:
    def resp = new CardInfoServiceImpl().getAllCards("testuid",true)

    then:
    resp instanceof SuCard[]
  }

  def "Test getCardByUUID with null suCardUUID argument, should throw IllegalArgumentException"() {
    setup:
    def cardInfoServiceImpl = new CardInfoServiceImpl()

    when:
    cardInfoServiceImpl.getCardByUUID(null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test getCardByUUID when card doesn't exist"() {
    given:
    SuCardQuery.metaClass.static.findCardBySuCardUUID = {String directory,String uid -> return null }
    def cardInfoServiceImpl = new CardInfoServiceImpl()

    when:
    def resp = cardInfoServiceImpl.getCardByUUID("testCardUUID")

    then:
    resp == null
  }

  def "Test getCardByUUID default flow"() {
    given:
    SuCardQuery.metaClass.static.findCardBySuCardUUID = {String directory,String uid -> return new SuCard() }

    def cardInfoServiceImpl = new CardInfoServiceImpl()

    when:
    def res = cardInfoServiceImpl.getCardByUUID("testCardUUID")

    then:
    assert res
  }
}
