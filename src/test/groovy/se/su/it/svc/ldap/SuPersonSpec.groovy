package se.su.it.svc.ldap

import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.util.GeneralUtils
import org.junit.Test
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

  @Test
  def "setPrimaryAffiliation: Test adding new primary affiliation"() {
    given:
    def person = new SuPerson(objectClass: [])
    def affiliation = 'kaka'
    person.eduPersonPrimaryAffiliation = affiliation
    person.eduPersonAffiliation = new TreeSet()
    person.eduPersonAffiliation.add(affiliation)
    String[] newPrimaryAffiliation = [ SuPerson.Affilation.OTHER.value ]

    when:
    person.updateAffiliations(newPrimaryAffiliation)

    then:
    person.eduPersonPrimaryAffiliation == newPrimaryAffiliation.first()
    person.eduPersonAffiliation.contains(newPrimaryAffiliation.first())
  }

  @Test
  def "setPrimaryAffiliation: when no affiliations exists"() {
    given:
    def person = new SuPerson(objectClass: [])
    String[] newPrimaryAffiliation = ['other']

    when:
    person.updateAffiliations(newPrimaryAffiliation)

    then:
    person.eduPersonPrimaryAffiliation == newPrimaryAffiliation.first()
    person.eduPersonAffiliation.contains(newPrimaryAffiliation.first())
  }

  @Test
  def "setPrimaryAffiliation: when no valid affiliation is provided"() {
    given:
    def person = new SuPerson(objectClass: [])
    String[] newPrimaryAffiliation = ['foo']

    when:
    person.updateAffiliations(newPrimaryAffiliation)

    then:
    thrown(IllegalArgumentException)
  }


  @Test
  def "setPrimaryAffiliation: should set objectClass"() {
    given:
    def person = new SuPerson(objectClass: [])
    String[] newPrimaryAffiliation = ['other']

    when:
    person.updateAffiliations(newPrimaryAffiliation)

    then:
    person.objectClass.contains('eduPerson')
  }

  @Test
  def "setPrimaryAffiliation: sending null as affiliations"() {
    given:
    def person = new SuPerson(objectClass: [])
    String[] affiliations = null

    when:
    person.updateAffiliations(affiliations)

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  @Unroll
  def "setPrimaryAffiliation: sets #expected as primary for #affiliations"() {
    given:
    def person = new SuPerson(objectClass: [])

    when:
    person.updateAffiliations(affiliations as String[])

    then:
    person.eduPersonPrimaryAffiliation == expected

    where:
    expected   | affiliations
    'employee' | ['employee', 'student', 'alumni', 'member', 'other']
    'student'  | ['student', 'alumni', 'member', 'other']
    'alumni'   | ['alumni', 'member', 'other']
    'member'   | ['member', 'other']
    'other'    | ['other']
  }

  @Test @Unroll
  def "testAffiliations: Given affiliation #affiliation expecting value #expected"() {
    expect: 'we test the order and value of the affiliations'
    (affiliation as SuPerson.Affilation).value == expected

    where:
    affiliation << SuPerson.Affilation.enumConstants
    expected << ['employee', 'student', 'alumni', 'member', 'other']
  }
}
