package se.su.it.svc

import gldapo.GldapoSchemaRegistry

import org.gcontracts.PreconditionViolation

import se.su.it.svc.ldap.SuCard
import se.su.it.svc.query.SuCardOrderQuery
import se.su.it.svc.query.SuCardQuery

import spock.lang.Shared
import spock.lang.Specification

class CardAdminServiceImplSpec extends Specification
{
    @Shared
    CardAdminServiceImpl service

    def setup()
    {
        GldapoSchemaRegistry.metaClass.add = { Object registration -> }
        SuCard.metaClass.static.save = {-> }
        SuCard.metaClass.static.find = { Map arg1, Closure arg2 -> }
        SuCard.metaClass.static.update = {-> }

        service = new CardAdminServiceImpl()
        service.suCardOrderQuery = Mock(SuCardOrderQuery)
    }

    def cleanup()
    {
        service = null
        SuCard.metaClass = null
        SuCardQuery.metaClass = null
        SuCardOrderQuery.metaClass = null
        GldapoSchemaRegistry.metaClass = null
    }

    def "revokeCard with null suCardUUID argument"()
    {
        when:
        service.revokeCard(null, 'uid')

        then:
        thrown(PreconditionViolation)
    }

    def "revokeCard with null revokerUid argument"()
    {
        when:
        service.revokeCard('uuid', null)

        then:
        thrown(PreconditionViolation)
    }

    def "revokeCard sets state to revoked"()
    {
        setup:
        def suCard = new SuCard()
        SuCardQuery.metaClass.static.findCardBySuCardUUID = { String a, String b -> suCard }

        service.suCardOrderQuery.markCardAsDiscarded(*_) >> { return true }

        when:
        service.revokeCard("testcarduuid", 'uid')

        then:
        suCard.suCardState == "urn:x-su:su-card:state:revoked"
    }

    def "revokeCard when updating SuCardDb fails"()
    {
        setup:
        def suCard = new SuCard()
        SuCardQuery.metaClass.static.findCardBySuCardUUID = { String a, String b -> suCard }
        service.suCardOrderQuery.markCardAsDiscarded(*_) >> { throw new IllegalStateException("foo") }

        when:
        service.revokeCard("testcarduuid", 'uid')

        then:
        thrown(IllegalStateException)

        and:
        suCard.suCardState == "urn:x-su:su-card:state:revoked"
    }

    def "revokeCard throws IllegalArgumentException when no card was found"()
    {
        setup:
        SuCardQuery.metaClass.static.findCardBySuCardUUID = { String arg1, String arg2 -> return null }

        when:
        service.revokeCard("testcarduuid", 'uid')

        then:
        thrown(IllegalArgumentException)
    }

    def "setCardPIN is unsupported"()
    {
        when:
        service.setCardPIN(null, "1234")

        then:
        thrown(UnsupportedOperationException)
    }
}
