package se.su.it.svc.util

import org.junit.Test
import se.su.it.svc.ldap.SuEnrollPerson
import org.junit.After
import org.junit.Before
import se.su.it.commons.ExecUtils
import se.su.it.svc.ldap.SuEnrollPerson
import se.su.it.svc.manager.Properties
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class EnrollmentServiceUtilsSpec extends Specification {

  @Shared
  EnrollmentServiceUtils util

  @Before
  def setup() {
    this.util = new EnrollmentServiceUtils()
  }

  @After
  def cleanup() {
    this.util = null
  }

  @Unroll
  def "getHomeDirectoryPath should generate homedir='#dir' for uid='#uid'"() {
    expect: util.getHomeDirectoryPath(uid) == dir

    where:
    uid   | dir
    null  | null
    ''    | null
    'a'   | null // To short
    'aa'  | EnrollmentServiceUtils.AFS_HOME_DIR_BASE + 'a/a/aa'
    'aaa' | EnrollmentServiceUtils.AFS_HOME_DIR_BASE + 'a/a/aaa'
    'abc' | EnrollmentServiceUtils.AFS_HOME_DIR_BASE + 'a/b/abc'
  }

  def "runEnableScript should return null on exception"() {
    given:
    GroovyMock(ExecUtils, global: true)
    ExecUtils.exec(*_) >> { throw new Exception() }

    when:
    def ret = util.runEnableScript("", "")

    then:
    ret == null
  }

  @Unroll
  def "runEnableScript should return '#result' on script output '#execReturn'"() {
    given:
    GroovyMock(ExecUtils, global: true)
    ExecUtils.exec(*_) >> { "OK (uidnumber:1234)" }

    when:
    def ret = util.runEnableScript("", "")

    then:
    ret == "1234"

    where:
    execReturn             | result
    null                   | null
    ""                     | null
    "OK"                   | null
    "OK (uidnumber: 1234)" | null
    "OK (uidnumber:1)"     | "1"
    "OK (uidnumber:12)"    | "12"
    "OK (uidnumber:123)"   | "123"
    "OK (uidnumber:1234)"  | "1234"
  }

  def "enableUser should set uidNumber=-1 if skipCreate"() {
    given:
    def posixAccoount = new SuEnrollPerson(objectClass: [])
    posixAccoount.metaClass.save = {}
    Properties.instance.props.enrollment.skipCreate = "true"

    when:
    util.enableUser("", "", posixAccoount)

    then:
    posixAccoount.uidNumber == "-1"
  }

  def "enableUser should set attributes on person & return true"() {
    given:
    GroovyMock(EnrollmentServiceUtils, global: true)
    EnrollmentServiceUtils.runEnableScript(*_) >> "1234"
    EnrollmentServiceUtils.getHomeDirectoryPath(_) >> "uid"
    def posixAccoount = new SuEnrollPerson(objectClass: [])
    posixAccoount.metaClass.save = {}
    Properties.instance.props.enrollment.skipCreate = "false"

    when:
    def ret = util.enableUser("", "", posixAccoount)

    then:
    ret

    and:
    posixAccoount.objectClass.contains 'posixAccount'
    posixAccoount.loginShell == util.SHELL_PATH
    posixAccoount.homeDirectory == 'uid'
    posixAccoount.uidNumber == "1234"
    posixAccoount.gidNumber == util.DEFAULT_USER_GID
  }

  def "enableUser should return false on error"() {
    given:
    GroovyMock(EnrollmentServiceUtils, global: true)
    EnrollmentServiceUtils.runEnableScript(*_) >> null

    when:
    def ret = util.enableUser("", "", null)

    then:
    !ret
  }

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
