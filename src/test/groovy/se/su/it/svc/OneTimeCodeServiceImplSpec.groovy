package se.su.it.svc

import se.su.it.svc.util.GeneralUtils

import spock.lang.Shared
import spock.lang.Specification

class OneTimeCodeServiceImplSpec extends Specification
{
    @Shared
    OneTimeCodeServiceImpl service

    def setup()
    {
        service = new OneTimeCodeServiceImpl()
    }

    def cleanup()
    {
        service = null
    }

    def "getConfirmed: happy path"()
    {
        when:
        service.getConfirmed("190012236782", 1)

        then:
        thrown(RuntimeException)
    }

    def "getUnconfirmed: happy path"()
    {
        when:
        service.getUnconfirmed(1)

        then:
        thrown(RuntimeException)
    }
}

