/*
 * Copyright (c) 2013, IT Services, Stockholm University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Stockholm University nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */


import gldapo.GldapoSchemaRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import se.su.it.commons.Kadmin
import se.su.it.commons.PasswordUtils
import se.su.it.svc.EnrollmentServiceImpl
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.commons.SvcUidPwd
import se.su.it.svc.ldap.SuEnrollPerson
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.util.EnrollmentServiceUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-13
 * Time: 08:36
 * To change this template use File | Settings | File Templates.
 */
class EnrollmentServiceImplTest extends Specification {
  @Shared
  EnrollmentServiceImpl service

  @Before
  def setup() {
    this.service = new EnrollmentServiceImpl()
  }

  @After
  def cleanup() {
    this.service = null
  }

  @Test
  def "Test resetAndExpirePwd with null uid argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.resetAndExpirePwd(null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test resetAndExpirePwd with null SvcAudit argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.resetAndExpirePwd("testuid", null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test resetAndExpirePwd without person exist"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.resetAndExpirePwd("testuid", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "resetAndExpirePwd: test basic flow"() {
    given:
    def uid = "testuid"
    def audit = new SvcAudit()

    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String anUid -> return new SuPerson() }

    def kadmin = Mock(Kadmin)
    Kadmin.metaClass.static.newInstance = { -> return kadmin}

    when:
    def res = service.resetAndExpirePwd(uid, audit)

    then:
    assert res

    and:
    1 * kadmin.resetOrCreatePrincipal(*_) >> "xyz"

    and:
    1 * kadmin.setPasswordExpiry(*_)
  }

  @Test
  def "resetAndExpirePwd: test when uid param is null, should throw IllegalArgumentException"() {
    given:
    def uid = null
    def audit = new SvcAudit()

    when:
    service.resetAndExpirePwd(uid, audit)

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "resetAndExpirePwd: test when audit param is null, should throw IllegalArgumentException"() {
    given:
    def uid = "testuid"
    def audit = null

    when:
    service.resetAndExpirePwd(uid, audit)

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "resetAndExpirePwd: test when search for SuPerson returns null, should throw IllegalArgumentException"() {
    given:
    def uid = "testuid"
    def audit = new SvcAudit()

    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String anUid -> return null }

    when:
    service.resetAndExpirePwd(uid, audit)

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "enrollUserWithMailRoutingAddress: test when attributes are invalid, should throw IllegalArgumentException"() {
    when:
    service.enrollUserWithMailRoutingAddress("domain", "givenName", "sn", "affiliation", "nin", "mailRoutingAddress", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "enrollUserWithMailRoutingAddress: test when user exists in LDAP, should handle user and return new password"() {
    given:
    SuPersonQuery.metaClass.static.getSuEnrollPersonFromSsn = {
      String directory,String nin ->
        def enrollPerson = new SuEnrollPerson()
        enrollPerson.objectClass = []
        return enrollPerson
    }
    SuPersonQuery.metaClass.static.saveSuEnrollPerson = {SuEnrollPerson person -> return null}
    EnrollmentServiceUtils.metaClass.static.enableUser = {String uid, String password, Object o -> return true}

    when:
    def password = service.enrollUserWithMailRoutingAddress("student.su.se", "test", "testsson", "other", "1000000000", "a@b.com", new SvcAudit())

    then:
    assert password
  }

  @Test
  def "enrollUserWithMailRoutingAddress: test when user doesn't exist in LDAP, should handle new user and return new password"() {
    given:
    SuPersonQuery.metaClass.static.getSuEnrollPersonFromSsn = {
      String directory,String nin ->
        return null
    }
    SuEnrollPerson.metaClass.parent = "stuts"
    SuPersonQuery.metaClass.static.initSuEnrollPerson = {String directory, SuEnrollPerson person -> return person}
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory, String uid -> return null}
    EnrollmentServiceUtils.metaClass.static.enableUser = {String uid, String password, Object o -> return true}

    when:
    def password = service.enrollUserWithMailRoutingAddress("student.su.se", "test", "testsson", "other", "1000000000", "a@b.com", new SvcAudit())

    then:
    assert password
  }

  @Test
  def "Test enrollUser without null domain argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser(null,"test","testsson","other","100000000000", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser without null givenName argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se",null,"testsson","other","100000000000", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser without null sn argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test",null,"other","100000000000", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser without null eduPersonAffiliation argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson",null,"100000000000", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser without null nin argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other",null, new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser without null SvcAudit argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","100000000000", null)

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser with bad format nin argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","10000", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser search with 10 - digit"() {
    setup:
    boolean beenThere = false
    SuEnrollPerson.metaClass.parent = "stuts"
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory, String uid -> return null}
    SuPersonQuery.metaClass.static.getSuEnrollPersonFromSsn = {String directory,String nin -> beenThere = true; return null }
    SuPersonQuery.metaClass.static.initSuEnrollPerson = {String directory, SuEnrollPerson person -> return person}
    SuPersonQuery.metaClass.static.saveSuEnrollPerson = {SuEnrollPerson person -> return null}
    EnrollmentServiceUtils.metaClass.static.enableUser = {String uid, String password, Object o -> return true}

    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","1000000000", new SvcAudit())

    then:
    beenThere == true
  }

  @Test
  def "Test enrollUser search with 12 - digit"() {
    setup:
    boolean beenThere1 = false
    boolean beenThere2 = false
    SuEnrollPerson.metaClass.parent = "stuts"
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory, String uid -> return null}
    SuPersonQuery.metaClass.static.initSuEnrollPerson = {String directory, SuEnrollPerson person -> return person}
    SuPersonQuery.metaClass.static.saveSuEnrollPerson = {SuEnrollPerson person -> return null}
    EnrollmentServiceUtils.metaClass.static.enableUser = {String uid, String password, Object o -> return true}
    SuPersonQuery.metaClass.static.getSuEnrollPersonFromNin = {String directory,String nin -> beenThere1 = true; return null }
    SuPersonQuery.metaClass.static.getSuEnrollPersonFromSsn = {String directory,String nin -> beenThere2 = true; return null }

    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","100000000000", new SvcAudit())

    then:
    beenThere1 == true
    beenThere2 == true
  }

  @Test
  def "Test enrollUser scripts fail"() {
    setup:

    se.su.it.svc.manager.Properties.metaClass.static.getInstance = {
      def properties1 = new se.su.it.svc.manager.Properties()
      ConfigObject co = new ConfigObject()
      co.put('enrollUser.skipCreate', false)
      properties1.props = co
      return properties1
    }


    SuEnrollPerson suEnrollPerson = new SuEnrollPerson(uid: "testuid")
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuEnrollPersonFromSsn = {String directory,String nin -> return suEnrollPerson }
    EnrollmentServiceUtils.metaClass.static.enableUser = {String uid, String password, Object o -> return false}
    SuEnrollPerson.metaClass.parent = "stuts"
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory, String uid -> return null}
    SuPersonQuery.metaClass.static.initSuEnrollPerson = {String directory, SuEnrollPerson person -> return person}
    SuPersonQuery.metaClass.static.saveSuEnrollPerson = {SuEnrollPerson person -> return null}


    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","1000000000", new SvcAudit())

    then:
    thrown(Exception)
  }

  @Test
  def "Test enrollUser Happy Path"() {
    setup:
    int p1=0
    int p2=0
    SuEnrollPerson suEnrollPerson = new SuEnrollPerson(uid: "testuid")
    suEnrollPerson.objectClass = []
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuEnrollPerson.metaClass.parent = "stuts"
    SuPersonQuery.metaClass.static.getSuEnrollPersonFromSsn = {String directory,String nin -> return suEnrollPerson }
    SuPersonQuery.metaClass.static.saveSuEnrollPerson = {SuEnrollPerson sip -> return null}
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson(eduPersonPrimaryAffiliation: "kalle") }
    PasswordUtils.metaClass.static.genRandomPassword = {int a, int b -> p1 = a; p2 = b; return "hacker"}
    EnrollmentServiceUtils.metaClass.static.enableUser = {String uid, String password, Object o -> return true}

    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    SvcUidPwd ret = enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","1000000000", new SvcAudit())

    then:
    ret.uid == "testuid"
    ret.password == "hacker"
    p1 == 10
    p2 == 10
  }

  @Test
  def "Test enrollUser with skipCreate"() {
    setup:

    /** needs to be fully qualified or java Properties will be chosen by default. */


    se.su.it.svc.manager.Properties.metaClass.static.getInstance = {
      def properties1 = new se.su.it.svc.manager.Properties()
      ConfigObject co = new ConfigObject()
      co.put('enrollUser.skipCreate', true)
      properties1.props = co
      return properties1
    }

    int p1=0
    int p2=0
    SuEnrollPerson suEnrollPerson = new SuEnrollPerson(uid: "testuid")
    suEnrollPerson.objectClass = []
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuEnrollPerson.metaClass.parent = "stuts"
    SuPersonQuery.metaClass.static.getSuEnrollPersonFromSsn = {String directory,String nin -> return suEnrollPerson }
    SuPersonQuery.metaClass.static.saveSuEnrollPerson = {SuEnrollPerson sip -> return null}
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson(eduPersonPrimaryAffiliation: "kalle") }
    PasswordUtils.metaClass.static.genRandomPassword = {int a, int b -> p1 = a; p2 = b; return "hacker"}
    EnrollmentServiceUtils.metaClass.static.enableUser = {String uid, String password, Object o -> return true}

    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    SvcUidPwd ret = enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","1000000000", new SvcAudit())

    then:
    ret.uid == "testuid"
    ret.password == "hacker"
    p1 == 10
    p2 == 10
  }

  @Test @Unroll
  def "setNin when nin => #nin"() {
    given:
    def person = new SuEnrollPerson()
    person.objectClass = []

    when:
    service.setNin(nin, person)

    then: '01 gets cut from case 2, the others are untouched.'
    person.socialSecurityNumber == expected

    where:
    nin << ['abc', '0123456789AB', '0123456789ABC']
    expected << ['abc', '23456789AB', '0123456789ABC']
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
    service.setPrimaryAffiliation(newPrimaryAffiliation, person)

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
    service.setPrimaryAffiliation(newPrimaryAffiliation, person)

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
    service.setMailAttributes(person, 'kaka.se')

    then:
    person.mailLocalAddress.contains('foo@kaka.se')
  }

}
