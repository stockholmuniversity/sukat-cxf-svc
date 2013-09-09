package se.su.it.svc.util

import org.junit.After
import org.junit.Before
import org.junit.Test
import se.su.it.commons.ExecUtils
import se.su.it.svc.commons.SvcUidPwd
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.manager.Config
import se.su.it.svc.query.SuPersonQuery
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
    def posixAccoount = new SuPerson(objectClass: [])
    posixAccoount.metaClass.update = {}
    Config.instance.props.enrollment.skipCreate = "true"

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
    def posixAccoount = new SuPerson(objectClass: [])
    posixAccoount.metaClass.update = {}
    Config.instance.props.enrollment.skipCreate = "false"

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
  def "setMailAttributes: When adding a second mailLocalAddress"() {
    given:
    def person = new SuPerson()
    person.mailLocalAddress = new TreeSet()
    person.mailLocalAddress.add('kaka@kaka.se')
    person.uid = 'foo'

    when:
    EnrollmentServiceUtils.setMailAttributes(person, 'kaka.se')

    then:
    person.mailLocalAddress.contains('foo@kaka.se')
  }

  def "activateUser should move the suPerson to the supplied domain"() {
    given:
    GroovyMock(EnrollmentServiceUtils, global: true)
    EnrollmentServiceUtils.enableUser(*_) >> true
    GroovyMock(SuPersonQuery, global: true)

    SuPerson suPerson = new SuPerson()

    when:
    util.activateUser(suPerson, new SvcUidPwd(), [""] as String[], "it.su.se")

    then:
    1 * SuPersonQuery.moveSuPerson(suPerson, 'dc=it,dc=su,dc=se')
  }

  def "activateUser should save the suPerson"() {
    given:
    GroovyMock(EnrollmentServiceUtils, global: true)
    EnrollmentServiceUtils.enableUser(*_) >> true
    GroovyMock(SuPersonQuery, global: true)

    SuPerson suPerson = new SuPerson()

    when:
    util.activateUser(suPerson, new SvcUidPwd(), [""] as String[], "it.su.se")

    then:
    1 * SuPersonQuery.updateSuPerson(suPerson)
  }

  def "activateUser should set affiliation"() {
    given:
    GroovyMock(EnrollmentServiceUtils, global: true)
    EnrollmentServiceUtils.enableUser(*_) >> true

    GroovyMock(SuPersonQuery, global: true)

    SuPerson suPerson = new SuPerson()

    when:
    util.activateUser(suPerson, new SvcUidPwd(), ["affiliation"] as String[], "it.su.se")

    then:
    1 * suPerson.setAffiliations(['affiliation'] as String[])
  }

  def "activateUser should set mail attributes"() {
    given:
    GroovyMock(EnrollmentServiceUtils, global: true)
    EnrollmentServiceUtils.enableUser(*_) >> true

    GroovyMock(SuPersonQuery, global: true)

    SuPerson suPerson = new SuPerson()

    when:
    util.activateUser(suPerson, new SvcUidPwd(), [""] as String[], "it.su.se")

    then:
    1 * EnrollmentServiceUtils.setMailAttributes(suPerson, 'it.su.se')
  }

  def "activateUser throw exception if enable fails"() {
    given:
    GroovyMock(EnrollmentServiceUtils, global: true)
    EnrollmentServiceUtils.enableUser(*_) >> false

    when:
    util.activateUser(new SuPerson(), new SvcUidPwd(), [""] as String[], "it.su.se")

    then:
    thrown(RuntimeException)
  }
}
