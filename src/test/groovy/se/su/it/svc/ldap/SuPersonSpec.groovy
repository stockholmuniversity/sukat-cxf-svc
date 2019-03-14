package se.su.it.svc.ldap

import gldapo.GldapoSchemaRegistry
import se.su.it.svc.commons.LdapAttributeValidator
import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.commons.SvcUidPwd
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.util.GeneralUtils
import spock.lang.Specification
import spock.lang.Unroll

class SuPersonSpec extends Specification {

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPerson.metaClass.static.update = {-> true }
    SuPerson.metaClass.static.save = {-> true }
    SuPerson.metaClass.static.move = { String s -> true }
  }

  def cleanup() {
    GeneralUtils.metaClass = null
    SuPerson.metaClass = null
    SuPersonQuery.metaClass = null
    GldapoSchemaRegistry.metaClass = null
  }

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

  @Unroll
  def "testAffiliations: Given affiliation #affiliation expecting value #expected"() {
    expect: 'we test the order and value of the affiliations'
    (affiliation as SuPerson.Affilation).value == expected

    where:
    affiliation << SuPerson.Affilation.enumConstants
    expected << ['employee', 'student', 'alumni', 'member', 'other']
  }

  @Unroll
  def "setMailRoutingAddress: sets inetLocalMailRecipient if supplied mailRoutingAddress is \'#mailRoutingAddress\' => #expected"() {
    given:
    SuPerson suPerson = new SuPerson(objectClass: [])

    when:
    suPerson.setMailRoutingAddress(mailRoutingAddress)

    then:
    expected == suPerson.objectClass.contains('inetLocalMailRecipient')

    where:
    mailRoutingAddress << ["", "mail@su.se"]
    expected << [false, true]

  }
}
