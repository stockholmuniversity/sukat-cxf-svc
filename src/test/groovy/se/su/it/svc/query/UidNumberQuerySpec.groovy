package se.su.it.svc.query

import groovy.sql.Sql

import spock.lang.Shared
import spock.lang.Specification

public class UidNumberQuerySpec extends Specification
{
    @Shared
    UidNumberQuery service

    def setup()
    {
        service = new UidNumberQuery()
        service.sukatSql = Mock(Sql)
    }

    def "getUidNumber: new entry"()
    {
        setup:
        service.sukatSql.firstRow(*_) >>> [null, [id: 400002]]

        when:
        def res = service.getUidNumber("uiqs1234")

        then:
        res == 400002
    }

    def "getUidNumber: new entry"()
    {
        setup:
        service.sukatSql.firstRow(*_) >> [id: 400003]

        when:
        def res = service.getUidNumber("uiqs1234")

        then:
        res == 400003
    }
}

