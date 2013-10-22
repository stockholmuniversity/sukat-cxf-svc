package se.su.it.svc.ldap

import gldapo.GldapoSchemaRegistry
import se.su.it.commons.ExecUtils
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
  }

  def cleanup() {
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

  def "setPrimaryAffiliation: when no valid affiliation is provided"() {
    given:
    def person = new SuPerson(objectClass: [])
    String[] newPrimaryAffiliation = ['foo']

    when:
    person.updateAffiliations(newPrimaryAffiliation)

    then:
    thrown(IllegalArgumentException)
  }

  def "setPrimaryAffiliation: should set objectClass"() {
    given:
    def person = new SuPerson(objectClass: [])
    String[] newPrimaryAffiliation = ['other']

    when:
    person.updateAffiliations(newPrimaryAffiliation)

    then:
    person.objectClass.contains('eduPerson')
  }

  def "setPrimaryAffiliation: sending null as affiliations"() {
    given:
    def person = new SuPerson(objectClass: [])
    String[] affiliations = null

    when:
    person.updateAffiliations(affiliations)

    then:
    thrown(IllegalArgumentException)
  }

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

  @Unroll
  def "testAffiliations: Given affiliation #affiliation expecting value #expected"() {
    expect: 'we test the order and value of the affiliations'
    (affiliation as SuPerson.Affilation).value == expected

    where:
    affiliation << SuPerson.Affilation.enumConstants
    expected << ['employee', 'student', 'alumni', 'member', 'other']
  }

  @Unroll
  def "getHomeDirectoryPath should generate homedir='#dir' for uid='#uid'"() {
    given:
    SuPerson suPerson = new SuPerson(uid:uid)

    expect:
    suPerson.fetchHomeDirectoryPath() == dir

    where:
    uid   | dir
    null  | null
    ''    | null
    'a'   | null // To short
    'aa'  | SuPerson.AFS_HOME_DIR_BASE + 'a/a/aa'
    'aaa' | SuPerson.AFS_HOME_DIR_BASE + 'a/a/aaa'
    'abc' | SuPerson.AFS_HOME_DIR_BASE + 'a/b/abc'
  }

  def "runEnableScript should return false on exception"() {
    given:
    GroovyMock(ExecUtils, global: true)
    ExecUtils.exec(*_) >> { throw new Exception() }

    when:
    def ret = new SuPerson().runEnableScript("", "")

    then:
    !ret
  }

  @Unroll
  def "runEnableScript should return '#result' on script output '#execReturn'"() {
    given:
    GroovyMock(ExecUtils, global: true)
    ExecUtils.exec(*_) >> { "OK (uidnumber:1234)" }

    when:
    SuPerson suPerson = new SuPerson()
    def ret = suPerson.runEnableScript("", "")

    then:
    suPerson.uidNumber == "1234"
    assert ret

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
    def posixAccount = new SuPerson(objectClass: [])

    SuPerson.metaClass.isSkipCreateEnabled = {->
      return true
    }

    when:
    posixAccount.enable("", "")

    then:
    posixAccount.uidNumber == "-1"
  }

  def "enable should set attributes on person & return true"() {
    given:

    GroovyMock(LdapAttributeValidator, global:true)

    SuPerson.metaClass.runEnableScript = { String arg1, String arg2 ->
      return true
    }

    SuPerson.metaClass.fetchHomeDirectoryPath = { ->
      return "uid"
    }

    SuPerson suPerson = new SuPerson(objectClass: [])

    SuPerson.metaClass.isSkipCreateEnabled = {->
      return false
    }

    when:
    def ret = suPerson.enable("", "")

    then:
    ret

    and:
    suPerson.objectClass.contains 'posixAccount'
    suPerson.loginShell == SuPerson.SHELL_PATH
    suPerson.homeDirectory == 'uid'
    suPerson.gidNumber == SuPerson.DEFAULT_USER_GID
  }

  def "activate should"() {
    given:
    SuPerson suPerson = new SuPerson(objectClass: [])

    SuPerson.metaClass.runEnableScript = { String arg1, String arg2 ->
      return true
    }
    boolean correctDomain = false, updated = false
    SuPersonQuery.metaClass.static.moveSuPerson = { SuPerson a, String b ->
      correctDomain = (a == suPerson && b == 'dc=it,dc=su,dc=se')
    }
    SuPersonQuery.metaClass.static.updateSuPerson = { SuPerson a ->
      updated = (a == suPerson)
    }

    SuPerson.metaClass.isSkipCreateEnabled = {->
      return true
    }

    when:
    suPerson.activate(new SvcUidPwd(), [SuPerson.Affilation.EMPLOYEE.value] as String[], "it.su.se")

    then: 'move the person to the correct domain'
    correctDomain

    and: 'update the SuPerson'
    updated
  }

  def "activate should set affiliation"() {
    given:
    SuPerson.metaClass.runEnableScript = { String arg1, String arg2 ->
      return true
    }

    SuPerson.metaClass.isSkipCreateEnabled = {->
      return false
    }

    SuPerson suPerson = new SuPerson(objectClass: [])
    suPerson.objectClass = new TreeSet<String>()

    when:
    suPerson.activate(new SvcUidPwd(), [SuPerson.Affilation.EMPLOYEE.value] as String[], "it.su.se")

    then:
    suPerson.eduPersonPrimaryAffiliation == SuPerson.Affilation.EMPLOYEE.value
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

  def "addMailLocalAddresses: When SuPerson doesn't have any mailLocalAddress entries"() {
    given:
    def mailLocalAddresses = ['kaka@su.se', "bar@su.se"] as Set
    SuPerson suPerson = new SuPerson(objectClass:[])

    when:
    def resp = suPerson.addMailLocalAddress(mailLocalAddresses)

    then:
    resp == mailLocalAddresses as String[]

    and:
    suPerson.objectClass.contains('inetLocalMailRecipient')
  }

  def "addMailLocalAddresses: When SuPerson already has mailLocalAddress entries."() {
    given:
    def mailLocalAddresses = ['kaka@su.se', "bar@su.se", "foo@bar.se"] as LinkedHashSet
    SuPerson suPerson = new SuPerson(objectClass:['inetLocalMailRecipient'])
    suPerson.@mailLocalAddress = ["barbar@su.se", "kaka@su.se"] as LinkedHashSet

    when:
    def resp = suPerson.addMailLocalAddress(mailLocalAddresses)

    then:
    resp == ["barbar@su.se", "kaka@su.se", "foo@bar.se", "bar@su.se"]

    and:
    suPerson.objectClass.contains('inetLocalMailRecipient')
  }

  def "addMailLocalAddresses: When no new entries are added objectClass is not set either."() {
    given:
    def mailLocalAddresses = ["kaka@su.se"] as Set
    SuPerson suPerson = new SuPerson()
    suPerson.@mailLocalAddress = ["kaka@su.se"]

    when:
    def resp = suPerson.addMailLocalAddress(mailLocalAddresses)

    then:
    resp == ["kaka@su.se"]

    and:
    suPerson.objectClass == null
  }
}
