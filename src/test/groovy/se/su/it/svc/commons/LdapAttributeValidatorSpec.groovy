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
    def "validateSsn: ssn #ssn is ok"()
    {
        when:
        lav.validateSsn(ssn)

        then:
        noExceptionThrown()

        where:
        ssn          | _
        "8710101234" | _
        "871010A234" | _
        "871010P234" | _
    }

    @Unroll
    def "validateSsn: ssn #ssn is bad"()
    {
        when:
        lav.validateSsn(ssn)

        then:
        thrown(IllegalArgumentException)

        where:
        ssn      | _
        "860101" | _
    }

    @Unroll
    def "validateUid: uid is ok"()
    {
        when:
        lav.validateUid(uid)

        then:
        noExceptionThrown()

        where:
        uid           | _
        "validuid"    | _
        "042larkar"   | _
        "0123456789"  | _
        "01234567890" | _
    }

    @Unroll
    def "validateUid: uid is bad"()
    {
        when:
        lav.validateUid(uid)

        then:
        thrown(IllegalArgumentException)

        where:
        uid            | _
        null           | _
        "a"            | _
        "012345678901" | _
        "a-b"          | _
        [1: "foo"]     | _
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
