package se.su.it.svc.commons

import spock.lang.Shared
import spock.lang.Specification

class LdapAttributeValidatorSpec extends Specification
{
    @Shared
    LdapAttributeValidator lav

    def setup()
    {
        lav = new LdapAttributeValidator()
    }

    def "checkValidMailAddress: email patterns"()
    {
        expect:
        lav.checkValidMailAddress(email) == res

        where:
        email       | res
        "a@b.se"    | true
        "a@b@su.se" | false
    }
}
