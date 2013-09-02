package se.su.it.svc.util

import org.junit.Test
import se.su.it.svc.ldap.SuEnrollPerson
import spock.lang.Specification
import spock.lang.Unroll

class EnrollmentServiceUtilsSpec extends Specification {

  @Test
  def "setPrimaryAffiliation: Test adding new primary affiliation"() {
    given:
    def person = new SuEnrollPerson()
    def affiliation = 'kaka'
    person.eduPersonPrimaryAffiliation = affiliation
    person.eduPersonAffiliation = new TreeSet()
    person.eduPersonAffiliation.add(affiliation)
    String newPrimaryAffiliation = 'foo'

    when:
    EnrollmentServiceUtils.setPrimaryAffiliation(newPrimaryAffiliation, person)

    then:
    person.eduPersonPrimaryAffiliation == newPrimaryAffiliation
    person.eduPersonAffiliation.contains(newPrimaryAffiliation)
  }

  @Test
  def "setPrimaryAffiliation: when no affiliations exists"() {
    given:
    def person = new SuEnrollPerson()
    String newPrimaryAffiliation = 'foo'

    when:
    EnrollmentServiceUtils.setPrimaryAffiliation(newPrimaryAffiliation, person)

    then:
    person.eduPersonPrimaryAffiliation == newPrimaryAffiliation
    person.eduPersonAffiliation.contains(newPrimaryAffiliation)
  }

  @Test
  def "setMailAttributes: When adding a second mailLocalAddress"() {
    given:
    def person = new SuEnrollPerson()
    person.mailLocalAddress = new TreeSet()
    person.mailLocalAddress.add('kaka@kaka.se')
    person.uid = 'foo'

    when:
    EnrollmentServiceUtils.setMailAttributes(person, 'kaka.se')

    then:
    person.mailLocalAddress.contains('foo@kaka.se')
  }

  @Test @Unroll
  def "setNin when nin => #nin"() {
    given:
    def person = new SuEnrollPerson()
    person.objectClass = []

    when:
    EnrollmentServiceUtils.setNin(nin, person)

    then: '01 gets cut from case 2, the others are untouched.'
    person.socialSecurityNumber == expected

    where:
    nin << ['abc', '0123456789AB', '0123456789ABC']
    expected << ['abc', '23456789AB', '0123456789ABC']
  }
}
