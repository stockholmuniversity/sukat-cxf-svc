package se.su.it.svc.ldap

import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.util.GeneralUtils
import spock.lang.Specification
import spock.lang.Unroll

class SuPersonSpec extends Specification {

  @Unroll
  def "setMail - should update mailLocalAddress for #input"() {
    given:
    SuPerson suPerson = new SuPerson(
            mail: preMail,
            mailLocalAddress: preMLA)

    when:
    suPerson.mail = input

    then:
    suPerson.mail == input
    suPerson.mailLocalAddress == expectedMLA as Set<String>

    where:
    input   | preMail | preMLA    | expectedMLA
    null    | null    | null      | null
    null    | null    | ['a@b.c'] | ['a@b.c']
    null    | 'a@b.c' | null      | null
    null    | 'a@b.c' | ['a@b.c'] | ['a@b.c']
    'a@b.c' | null    | null      | ['a@b.c']
    'a@b.c' | 'a@b.c' | null      | ['a@b.c']
    'a@b.c' | null    | ['a@b.c'] | ['a@b.c']
    'a@b.c' | ''      | []        | ['a@b.c']
    'a@b.c' | 'a@b.c' | []        | ['a@b.c']
    'a@b.c' | ''      | ['a@b.c'] | ['a@b.c']
    'a@b.c' | 'a@b.c' | ['a@b.c'] | ['a@b.c']
    'a@b.c' | 'b@b.c' | ['a@b.c'] | ['a@b.c']
    'a@b.c' | 'a@b.c' | ['b@b.c'] | ['b@b.c', 'a@b.c']
    'a@b.c' | 'b@b.c' | ['b@b.c'] | ['b@b.c', 'a@b.c']
    'a@b.c' | 'b@b.c' | ['b@b.c'] | ['b@b.c', 'a@b.c']
  }

  @Unroll
  def "setMailLocalAddress - when MLA=#expectedMLA should set OC=#expectedOC if OC=#preOC"() {
    given:
    SuPerson suPerson = new SuPerson(
            objectClass: preOC)

    when:
    suPerson.mailLocalAddress = expectedMLA

    then:
    suPerson.objectClass == expectedOC as Set<String>
    suPerson.mailLocalAddress == expectedMLA as Set<String>

    where:
    expectedMLA | preOC                      | expectedOC
    null        | null                       | null
    null        | ['foo']                    | ['foo']
    null        | ['inetLocalMailRecipient'] | ['inetLocalMailRecipient']
    ['a@b.c']   | null                       | null
    ['a@b.c']   | ['foo']                    | ['foo','inetLocalMailRecipient']
    ['a@b.c']   | ['inetLocalMailRecipient'] | ['inetLocalMailRecipient']
  }

  def "updateFromSvcSuPersonVO - should copy properties"() {
    given:
    def suPerson = new SuPerson()
    def svcSuPerson = new SvcSuPersonVO(uid: 'foo')

    GroovyMock(GeneralUtils, global: true)

    when:
    suPerson.updateFromSvcSuPersonVO(svcSuPerson)

    then:
    1 * GeneralUtils.copyProperties(svcSuPerson, suPerson)
  }

  def "createSvcSuPersonVO - should copy properties"() {
    given:
    def suPerson = new SuPerson()

    GroovyMock(GeneralUtils, global: true)

    when:
    suPerson.createSvcSuPersonVO()

    then:
    1 * GeneralUtils.copyProperties(suPerson, _)
  }

  def "createSvcSuPersonVO - should set accountIsActive if SuPerson has objectClass posixAccount"() {
    given:
    def suPerson = new SuPerson(objectClass: ['posixAccount'])

    GroovyMock(GeneralUtils, global: true)

    when:
    def ret = suPerson.createSvcSuPersonVO()

    then:
    ret.accountIsActive
  }

  def "createSvcSuPersonVO - should not set accountIsActive if SuPerson don't have objectClass posixAccount"() {
    given:
    def suPerson = new SuPerson(objectClass: ['suPerson'])

    GroovyMock(GeneralUtils, global: true)

    when:
    def ret = suPerson.createSvcSuPersonVO()

    then:
    !ret.accountIsActive
  }
}
