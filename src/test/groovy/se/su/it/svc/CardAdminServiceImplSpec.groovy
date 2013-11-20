package se.su.it.svc

import gldapo.GldapoSchemaRegistry
import org.gcontracts.PreconditionViolation
import se.su.it.svc.ldap.SuCard
import se.su.it.svc.query.SuCardOrderQuery
import se.su.it.svc.query.SuCardQuery
import spock.lang.Specification

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
class CardAdminServiceImplSpec extends Specification {

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
    SuCard.metaClass.static.save = {-> }
    SuCard.metaClass.static.find = { Map arg1, Closure arg2 -> }
    SuCard.metaClass.static.update = {-> }
  }

  def cleanup() {
    SuCard.metaClass = null
    SuCardQuery.metaClass = null
    SuCardOrderQuery.metaClass = null
    GldapoSchemaRegistry.metaClass = null
  }

  def "revokeCard with null suCardUUID argument"() {
    setup:
    def cardAdminServiceImpl = new CardAdminServiceImpl()

    when:
    cardAdminServiceImpl.revokeCard(null, 'uid')

    then:
    thrown(PreconditionViolation)
  }

  def "revokeCard with null revokerUid argument"() {
    setup:
    def cardAdminServiceImpl = new CardAdminServiceImpl()

    when:
    cardAdminServiceImpl.revokeCard('uuid', null)

    then:
    thrown(PreconditionViolation)
  }

  def "revokeCard sets state to revoked"() {
    setup:
    def suCard = new SuCard()

    GroovyMock(SuCardQuery, global: true)
    SuCardQuery.findCardBySuCardUUID(* _) >> { return suCard }

    def cardAdminServiceImpl = new CardAdminServiceImpl()

    cardAdminServiceImpl.suCardOrderQuery = GroovyMock(SuCardOrderQuery) {
      markCardAsDiscarded(* _) >> { return true }
    }

    when:
    cardAdminServiceImpl.revokeCard("testcarduuid", 'uid')

    then:
    suCard.suCardState == "urn:x-su:su-card:state:revoked"
  }

  def "revokeCard when updating SuCardDb fails"() {
    setup:

    def suCard = new SuCard()
    GroovyMock(SuCardQuery, global: true)
    SuCardQuery.findCardBySuCardUUID(* _) >> { return suCard }

    def cardAdminServiceImpl = new CardAdminServiceImpl()

    cardAdminServiceImpl.suCardOrderQuery = GroovyMock(SuCardOrderQuery) {
      markCardAsDiscarded(* _) >> { throw new IllegalStateException("foo") }
    }

    when:
    cardAdminServiceImpl.revokeCard("testcarduuid", 'uid')

    then:
    thrown(IllegalStateException)

    and:
    suCard.suCardState == "urn:x-su:su-card:state:revoked"
  }

  def "revokeCard throws IllegalArgumentException when no card was found"() {
    setup:
    SuCardQuery.metaClass.static.findCardBySuCardUUID = { String arg1, String arg2 -> return null }

    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.revokeCard("testcarduuid", 'uid')
    then:
    thrown(IllegalArgumentException)
  }

  def "setCardPIN with null suCardUUID argument"() {
    setup:
    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.setCardPIN(null, "1234")
    then:
    thrown(IllegalArgumentException)
  }

  def "setCardPIN with null pin argument"() {
    setup:
    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.setCardPIN("testcarduuid", null)
    then:
    thrown(IllegalArgumentException)
  }

  def "setCardPIN sets pin"() {
    setup:
    def suCard = new SuCard()
    suCard.metaClass.save = {}
    SuCardQuery.metaClass.static.findCardBySuCardUUID = { String arg1, String arg2 -> return suCard }

    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.setCardPIN("testcarduuid", "1234")
    then:
    suCard.suCardPIN == "1234"
  }

  def "setCardPIN returns false when no card was found"() {
    setup:
    SuCardQuery.metaClass.static.findCardBySuCardUUID = { String arg1, String arg2 -> return null }

    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.setCardPIN("testcarduuid", "1234")
    then:
    thrown(IllegalArgumentException)
  }
}
