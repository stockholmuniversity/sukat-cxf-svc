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
    def "validateNin: nin #nin is ok"()
    {
        when:
        lav.validateNin(nin)

        then:
        noExceptionThrown()

        where:
        nin            | _
        "199010101013" | _  // Personnummer
    }

    @Unroll
    def "validateNin: nin #nin is bad"()
    {
        when:
        lav.validateNin(nin)

        then:
        thrown(IllegalArgumentException)

        where:
        nin            | _
        "8601011234"   | _   // Too short
        "218601011234" | _   // Invalid century
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
        "9010101013" | _  // Personnummer
        "871010A232" | _  // SUKAT-personnummer
        "871010P232" | _  // Interimspersonnummer
        "8301822980" | _  // Samordningsnummer
    }

    @Unroll
    def "validateSsn: ssn #ssn is bad"()
    {
        when:
        lav.validateSsn(ssn)

        then:
        thrown(IllegalArgumentException)

        where:
        ssn            | _
        "860101"       | _   // Too short
        "198601011234" | _   // Too long
        "AA01011013"   | _   // Invalid month
        "8613011234"   | _   // Invalid month
        "8601321234"   | _   // Invalid day
        "8301822A80"   | _   // Birthnumber
        "9010101016"   | _   // Invalid checksum
        "009000A000"   | _   // Testaccount
        "871010B232"   | _   // Invalid letter
    }

    @Unroll
    def "validateUid: uid is ok"()
    {
        when:
        lav.validateUid(uid)

        then:
        noExceptionThrown()

        where:
        uid            | _
        "validuid"     | _
        "042larkar"    | _
        "0123456789"   | _
        "01234567890"  | _
        "asabrattlund" | _
    }

    @Unroll
    def "validateUid: uid is bad"()
    {
        when:
        lav.validateUid(uid)

        then:
        thrown(IllegalArgumentException)

        where:
        uid             | _
        null            | _
        "a"             | _
        "a-b"           | _
        "0123456789012" | _
        [1: "foo"]      | _
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
