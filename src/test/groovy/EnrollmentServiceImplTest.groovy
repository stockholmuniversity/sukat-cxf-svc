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
import org.gcontracts.PreconditionViolation
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
import se.su.it.svc.manager.Properties
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.util.EnrollmentServiceUtils
import spock.lang.Shared
import spock.lang.Specification

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
    EnrollmentServiceImpl.metaClass = null
    EnrollmentServiceUtils.metaClass = null
  }

  @Test
  def "Test resetAndExpirePwd with null uid argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.resetAndExpirePwd(null, new SvcAudit())
    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test resetAndExpirePwd with null SvcAudit argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.resetAndExpirePwd("testuid", null)
    then:
    thrown(PreconditionViolation)
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
    thrown(PreconditionViolation)
  }

  @Test
  def "resetAndExpirePwd: test when audit param is null, should throw IllegalArgumentException"() {
    given:
    def uid = "testuid"
    def audit = null

    when:
    service.resetAndExpirePwd(uid, audit)

    then:
    thrown(PreconditionViolation)
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
    thrown(PreconditionViolation)
  }

  @Test
  def "enrollUserWithMailRoutingAddress: test when user exists in LDAP, should handle user and return new password"() {
    given:
    def nin_ = '0' *12
    boolean existingCalled = false
    EnrollmentServiceUtils.metaClass.static.findEnrollPerson = { String nin -> new SuEnrollPerson() }

    EnrollmentServiceUtils.metaClass.static.handleExistingUser = { String nin,
                                                                   SuEnrollPerson suEnrollPerson,
                                                                   SvcUidPwd svcUidPwd,
                                                                   String eduPersonPrimaryAffiliation,
                                                                   String domain,
                                                                   String mailRoutingAddress ->
      svcUidPwd.uid = "foo"
      existingCalled = true
    }

    when:
    def password = service.enrollUserWithMailRoutingAddress(
            "student.su.se",
            "test",
            "testsson",
            "other",
            nin_,
            "a@b.com",
            new SvcAudit())

    then:
    existingCalled
    password
  }

  @Test
  def "enrollUserWithMailRoutingAddress: test when user doesn't exist in LDAP, should handle new user and return new password"() {
    given:
    GroovyMock(EnrollmentServiceUtils, global: true)
    EnrollmentServiceUtils.handleExistingUser(*_) >> { a, b, c, d, e, f -> c.uid = b.uid }
    EnrollmentServiceUtils.findEnrollPerson(_) >> new SuEnrollPerson(uid: 'apa')

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
    thrown(PreconditionViolation)
  }

  @Test
  def "Test enrollUser without null givenName argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se",null,"testsson","other","100000000000", new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test enrollUser without null sn argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test",null,"other","100000000000", new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test enrollUser without null eduPersonAffiliation argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson",null,"100000000000", new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test enrollUser without null nin argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other",null, new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test enrollUser without null SvcAudit argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","100000000000", null)

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test enrollUser with bad format nin argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","10000", new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test enrollUser search with 10 - digit"() {
    setup:
    GroovyMock(EnrollmentServiceUtils, global: true)
    EnrollmentServiceUtils.handleExistingUser(*_) >> { a, b, c, d, e, f -> c.uid = b.uid }
    def nin = '1' * 10

    when:
    service.enrollUser("student.su.se","test","testsson","other",nin, new SvcAudit())

    then:
    1 * EnrollmentServiceUtils.findEnrollPerson(nin) >> { new SuEnrollPerson(uid: 'foo') }
  }

  @Test
  def "Test enrollUser search with 12 - digit"() {
    setup:
    GroovyMock(EnrollmentServiceUtils, global: true)
    EnrollmentServiceUtils.handleExistingUser(*_) >> { a, b, c, d, e, f -> c.uid = b.uid }
    def nin = '1' * 12

    when:
    service.enrollUser("student.su.se","test","testsson","other",nin, new SvcAudit())

    then:
    1 * EnrollmentServiceUtils.findEnrollPerson(nin) >> { new SuEnrollPerson(uid: 'foo') }
  }

  @Test
  def "Test enrollUser scripts fail"() {
    setup:
    Properties.instance.props.enrollment.skipCreate = "false"

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
    def nin = "1000000000"
    def uid = "testuid"
    def password = "*" * 10
    SuEnrollPerson suEnrollPerson = new SuEnrollPerson(uid: uid)

    GroovyMock(EnrollmentServiceUtils, global: true)
    1 * EnrollmentServiceUtils.findEnrollPerson(nin) >> suEnrollPerson
    1 * EnrollmentServiceUtils.handleExistingUser(*_) >> { a, b, c, d, e, f -> c.uid = uid }

    GroovyMock(PasswordUtils, global: true)

    when:
    SvcUidPwd ret = service.enrollUser("student.su.se","test","testsson","other", nin, new SvcAudit())

    then:
    ret.uid == uid
    ret.password == password
    1 * PasswordUtils.genRandomPassword(10, 10) >> password
  }
}
