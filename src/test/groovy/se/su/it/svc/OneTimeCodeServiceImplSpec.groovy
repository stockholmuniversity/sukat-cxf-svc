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
        setup:
        GeneralUtils.metaClass.static.execHelper = { String a, String b -> [] }

        when:
        service.getConfirmed("191123456789")

        then:
        notThrown(Exception)
  }
}

