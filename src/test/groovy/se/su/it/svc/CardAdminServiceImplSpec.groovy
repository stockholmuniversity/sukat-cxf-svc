package se.su.it.svc

import gldapo.GldapoSchemaRegistry

import org.gcontracts.PreconditionViolation

import se.su.it.svc.ldap.SuCard
import se.su.it.svc.query.SuCardOrderQuery
import se.su.it.svc.query.SuCardQuery

import spock.lang.Specification

class CardAdminServiceImplSpec extends Specification
{

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

  def "setCardPIN is unsupported"() {
    setup:
    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.setCardPIN(null, "1234")
    then:
    thrown(UnsupportedOperationException)
  }
}
