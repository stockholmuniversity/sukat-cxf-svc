package se.su.it.svc.query

import gldapo.GldapoSchemaRegistry

import se.su.it.svc.ldap.Account

import spock.lang.Specification

class AccountQuerySpec extends Specification
{
    def setup()
    {
        GldapoSchemaRegistry.metaClass.add = { Object registration -> }
    }

    def cleanup()
    {
        Account.metaClass = null
        GldapoSchemaRegistry.metaClass = null
    }

    def "findAccountByUid: no account is found"()
    {
        given:
        GroovyMock(Account, global:true)
        Account.find(*_) >> { return null }

        when:
        def resp = AccountQuery.findAccountByUid(null, null)

        then:
        resp == null
    }
}
