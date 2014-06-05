package se.su.it.svc.commons

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class LdapAttributeValidatorSpec extends Specification
{
    @Shared
    LdapAttributeValidator lav

    def setup()
    {
        lav = new LdapAttributeValidator()
    }

    @Unroll
    def "validateUid: uid is ok"()
    {
        when:
        lav.validateUid(uid)

        then:
        noExceptionThrown()

        where:
        uid          | _
        "validuid"   | _
        "042larkar"  | _
        "0123456789" | _
    }

    @Unroll
    def "validateUid: uid is bad"()
    {
        when:
        lav.validateUid(uid)

        then:
        thrown(IllegalArgumentException)

        where:
        uid           | _
        null          | _
        "a"           | _
        "01234567890" | _
        "a-b"         | _
        [1: "foo"]    | _
    }

    def "checkValidMailAddress: email patterns"()
    {
        expect:
        lav.checkValidMailAddress(email) == res

        where:
        email       | res
        "a@b.se"    | true
        "a@b-c.se"  | true
        "a@b@su.se" | false
    }
}
