package se.su.it.svc

import gldapo.GldapoSchemaRegistry
import org.gcontracts.PreconditionViolation
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuCard

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

import se.su.it.svc.query.SuCardOrderQuery
import se.su.it.svc.query.SuCardQuery
import spock.lang.Specification

class CardAdminServiceImplSpec extends Specification {

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
    SuCard.metaClass.static.save = {->}
    SuCard.metaClass.static.find = { Map arg1, Closure arg2 ->}
    SuCard.metaClass.static.update = {->}
  }

  def cleanup() {
    SuCard.metaClass = null
    SuCardQuery.metaClass = null
    SuCardOrderQuery.metaClass = null
    GldapoSchemaRegistry.metaClass = null
  }

  def "Test revokeCard with null suCardUUID argument"() {
    setup:
    def cardAdminServiceImpl = new CardAdminServiceImpl()

    when:
    cardAdminServiceImpl.revokeCard(null,new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  def "Test revokeCard with null SvcAudit argument"() {
    setup:
    def cardAdminServiceImpl = new CardAdminServiceImpl()

    when:
    cardAdminServiceImpl.revokeCard("testcarduuid",null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test revokeCard sets state to revoked"() {
    setup:
    def suCard = new SuCard()

    GroovyMock(SuCardQuery, global:true)
    SuCardQuery.findCardBySuCardUUID(*_) >> { return suCard }

    def cardAdminServiceImpl = new CardAdminServiceImpl()

    cardAdminServiceImpl.suCardOrderQuery = GroovyMock(SuCardOrderQuery) {
      markCardAsDiscarded(*_) >> { return true }
    }

    when:
    cardAdminServiceImpl.revokeCard("testcarduuid",new SvcAudit())

    then:
    suCard.suCardState == "urn:x-su:su-card:state:revoked"
  }

  def "Test revokeCard when updating SuCardDb fails"() {
    setup:

    def suCard = new SuCard()
    GroovyMock(SuCardQuery, global:true)
    SuCardQuery.findCardBySuCardUUID(*_) >> { return suCard }

    def cardAdminServiceImpl = new CardAdminServiceImpl()

    cardAdminServiceImpl.suCardOrderQuery = GroovyMock(SuCardOrderQuery) {
      markCardAsDiscarded(*_) >> { throw new RuntimeException("foo") }
    }

    when:
    cardAdminServiceImpl.revokeCard("testcarduuid",new SvcAudit())

    then:
    thrown(RuntimeException)

    and:
    suCard.suCardState == "urn:x-su:su-card:state:revoked"
  }

  def "Test revokeCard throws IllegalArgumentException when no card was found"() {
    setup:
    SuCardQuery.metaClass.static.findCardBySuCardUUID = {String arg1, String arg2 -> return null}

    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.revokeCard("testcarduuid",new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

    def "Test setCardPIN with null suCardUUID argument"() {
    setup:
    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.setCardPIN(null,"1234", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  def "Test setCardPIN with null pin argument"() {
    setup:
    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.setCardPIN("testcarduuid",null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  def "Test setCardPIN with null SvcAudit argument"() {
    setup:
    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.setCardPIN("testcarduuid","1234",null)
    then:
    thrown(IllegalArgumentException)
  }

  def "Test setCardPIN sets pin"() {
    setup:
    def suCard = new SuCard()
    suCard.metaClass.save = { }
    SuCardQuery.metaClass.static.findCardBySuCardUUID = {String arg1, String arg2 -> return suCard}

    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.setCardPIN("testcarduuid","1234",new SvcAudit())
    then:
    suCard.suCardPIN == "1234"
  }

  def "Test setCardPIN returns false when no card was found"() {
    setup:
    SuCardQuery.metaClass.static.findCardBySuCardUUID = {String arg1, String arg2 -> return null}

    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.setCardPIN("testcarduuid","1234", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }
}
