package se.su.it.svc.aspect
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
import org.apache.commons.lang.NotImplementedException
import org.gcontracts.PostconditionViolation
import org.gcontracts.PreconditionViolation
import org.junit.Test
import se.su.it.commons.ExecUtils
import se.su.it.commons.Kadmin
import se.su.it.commons.PasswordUtils
import se.su.it.svc.AccountServiceImpl

import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.commons.SvcUidPwd
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuPersonStub
import se.su.it.svc.manager.Config
import se.su.it.svc.query.SuPersonQuery
import spock.lang.Shared
import spock.lang.Specification

class AccountServiceImplTest extends Specification {

  @Shared
  AccountServiceImpl service

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
    this.service = new AccountServiceImpl()
  }

  def cleanup() {
    this.service = null
    Kadmin.metaClass = null
    SuPersonStub.metaClass = null
    SuPersonQuery.metaClass = null
    PasswordUtils.metaClass = null
    ExecUtils.metaClass = null
  }

  @Test
  def "Test updatePrimaryAffiliation with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.updatePrimaryAffiliation(null, "employee", new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test updatePrimaryAffiliation with null affiliation argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.updatePrimaryAffiliation("testuid", null, new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test updatePrimaryAffiliation with null SvcAudit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.updatePrimaryAffiliation("testuid", "employee", null)

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test updatePrimaryAffiliation without person exist"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updatePrimaryAffiliation("testuid", "employee", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updatePrimaryAffiliation when person exist"() {
    setup:
    String myaffiliation = null
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson(eduPersonPrimaryAffiliation: "kalle") }
    SuPersonQuery.metaClass.static.updateSuPerson = {SuPerson person -> myaffiliation = person.eduPersonPrimaryAffiliation}
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updatePrimaryAffiliation("testuid", "employee", new SvcAudit())
    then:
    myaffiliation == "employee"
  }

  @Test
  def "Test resetPassword with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.resetPassword(null, new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test resetPassword with null audit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.resetPassword("testuid", null)

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test resetPassword uid dont exist"() {
    setup:
    Kadmin.metaClass.principalExists = {String uid -> return false}
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.resetPassword("testuid", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test resetPassword password 10 chars"() {
    setup:
    def passwordUtils = GroovyMock(PasswordUtils, global: true)
    def kadmin = Mock(Kadmin)
    Kadmin.metaClass.static.newInstance = { kadmin }
    kadmin.principalExists(_) >> true
    def accountServiceImpl = new AccountServiceImpl()

    when:
    def ret = accountServiceImpl.resetPassword("testuid", new SvcAudit())

    then:
    1* PasswordUtils.genRandomPassword(10, 10) >> "*" *10
    assert ret == "*" *10
  }

  @Test
  def "Test resetPassword correct conversion of uid"() {
    setup:
    def kadmin = Mock(Kadmin)
    Kadmin.metaClass.static.newInstance = { kadmin }

    def accountServiceImpl = new AccountServiceImpl()

    when:
    String pwd = accountServiceImpl.resetPassword("testuid.jabber", new SvcAudit())

    then:
    1 * kadmin.principalExists("testuid/jabber") >> true
    1 * kadmin.setPassword("testuid/jabber", _)
  }

  @Test
  def "Test updateSuPerson with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.updateSuPerson(null,new SvcSuPersonVO(), new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test updateSuPerson with null personVO argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.updateSuPerson("testuid",null, new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test updateSuPerson with null SvcAudit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.updateSuPerson("testuid",new SvcSuPersonVO(), null)

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test updateSuPerson without person exist"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.updateSuPerson("testuid",new SvcSuPersonVO(), new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updateSuPerson when person exist"() {
    setup:
    SvcSuPersonVO suPerson = new SvcSuPersonVO()
    suPerson.title = ["knallhatt"]
    suPerson.eduPersonAffiliation = ["other"]
    def title = []
    String listEntry0 = null
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson(title: ["systemdeveloper"], eduPersonAffiliation: ["employee"]) }
    SuPersonQuery.metaClass.static.updateSuPerson = {SuPerson person -> title = person.title;listEntry0=person.eduPersonAffiliation.iterator().next()}
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updateSuPerson("testuid",suPerson, new SvcAudit())
    then:
    title.iterator().next() == "knallhatt"
    listEntry0 == "other"
  }

  @Test
  def "Test createSuPerson with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson(null,"196601010357","Test","Testsson", new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test createSuPerson with already exist uid argument"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson() }
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest","6601010357","Test","Testsson", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with null ssn argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest",null,"Test","Testsson", new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test createSuPerson with wrong ssn argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest","20001128-5764","Test","Testsson", new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test createSuPerson with null givenName argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest","196601010357",null,"Testsson", new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test createSuPerson with null sn argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest","196601010357","Test",null, new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test createSuPerson with null person argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest","196601010357","Test","Testsson", new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test createSuPerson with null audit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest","196601010357","Test","Testsson", null)

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test createSuPerson true flow"() {
    setup:
    def uid = 'uid'
    def ssn = '0000000000'
    def givenName = 'Test'
    def sn = 'Testsson'
    SuPersonStub suPersson = null

    SuPersonStub.metaClass.parent = "_"

    GroovyMock(SuPersonQuery, global: true)
    SuPersonQuery.initSuPerson(*_) >> { a, b -> suPersson = b }
    SuPersonQuery.getSuPersonFromUID(*_) >> null

    when:
    new AccountServiceImpl().createSuPerson(
            uid,
            ssn,
            givenName,
            sn,
            new SvcAudit())

    then:
    suPersson.uid == uid
    suPersson.cn == givenName + ' ' + sn
    suPersson.sn == sn
    suPersson.givenName == givenName
    suPersson.socialSecurityNumber == ssn
    suPersson.objectClass.containsAll(["suPerson","sSNObject","inetOrgPerson"])
    suPersson.parent == Config.instance.props.ldap.accounts.default.parent
  }

  @Test
  def "Test terminateSuPerson"() {
    when:
    new AccountServiceImpl().terminateSuPerson("testuid", new SvcAudit())

    then:
    thrown(NotImplementedException)
  }

  @Test
  def "Test getMailRoutingAddress with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.getMailRoutingAddress(null, new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test getMailRoutingAddress with null audit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.getMailRoutingAddress("testuid", null)

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test getMailRoutingAddress with person not found"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.getMailRoutingAddress("testuid", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getMailRoutingAddress Happy Path"() {
    setup:
    SuPerson suPerson = new SuPerson(mailRoutingAddress: "kalle")
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return suPerson }
    def accountServiceImpl = new AccountServiceImpl()
    when:
    def ret = accountServiceImpl.getMailRoutingAddress("testuid", new SvcAudit())
    then:
    ret == "kalle"
  }

  @Test
  def "Test setMailRoutingAddress with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.setMailRoutingAddress(null, "mail@test.su.se", new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test setMailRoutingAddress with null mail argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.setMailRoutingAddress("testuid", null, new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test setMailRoutingAddress with wrong format mail argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.setMailRoutingAddress("testuid", "testuser.mail.se", new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test setMailRoutingAddress with null audit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.setMailRoutingAddress("testuid", "mail@test.su.se", null)

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test setMailRoutingAddress with person not found"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.setMailRoutingAddress("testuid", "mail@test.su.se", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test setMailRoutingAddress Happy Path"() {
    setup:
    SuPerson suPerson = new SuPerson(mailRoutingAddress: "kalle", objectClass: [])
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return suPerson }
    SuPersonQuery.metaClass.static.updateSuPerson = {SuPerson tmpp -> suPerson.mailRoutingAddress = tmpp.mailRoutingAddress}
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.setMailRoutingAddress("testuid", "mail@test.su.se", new SvcAudit())

    then:
    suPerson.mailRoutingAddress == "mail@test.su.se"

    and:
    suPerson.objectClass.contains("inetLocalMailRecipient")
  }

  @Test
  def "Test findSuPersonBySocialSecurityNumber: with invalid ssn"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.findAllSuPersonsBySocialSecurityNumber(null, new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test findSuPersonBySocialSecurityNumber: When a user ain't found"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    GroovyMock(SuPersonQuery, global: true)
    SuPersonQuery.getSuPersonFromSsn(_,_) >> { new SvcSuPersonVO[0] }

    when:
    def ret = accountServiceImpl.findAllSuPersonsBySocialSecurityNumber('1001010000', new SvcAudit())

    then:
    notThrown(PostconditionViolation)
    ret.size() == 0
  }

  @Test
  def "Test findSuPersonBySocialSecurityNumber: When a user is found"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromSsn = {String directory,String uid ->
      new SuPerson(
          uid:'foo',
          givenName:'givenName',
          sn:'sn',
          displayName:'displayName',
          registeredAddress: 'registeredAddress',
          mail: 'email1@su.se',
          objectClass: []
      )
    }

    when:
    def resp = accountServiceImpl.findAllSuPersonsBySocialSecurityNumber('1001010000', new SvcAudit())

    then:
    resp instanceof SvcSuPersonVO[]

    and:
    resp.first().uid == 'foo'
    resp.first().givenName == 'givenName'
    resp.first().sn == 'sn'
    resp.first().displayName == 'displayName'
    resp.first().registeredAddress == 'registeredAddress'
    resp.first().mail == 'email1@su.se'
    !resp.first().accountIsActive
  }

  @Test
  def "Test findSuPersonByUid: with invalid ssn"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.findSuPersonByUid(null, new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test findSuPersonByUid: When a user ain't found"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }

    when:
    def resp = accountServiceImpl.findSuPersonByUid('foo', new SvcAudit())

    then:
    resp == null
  }

  @Test
  def "Test findSuPersonByUid: When a user is found"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid ->
      new SuPerson(
          uid:'foo',
          givenName:'givenName',
          sn:'sn',
          displayName:'displayName',
          registeredAddress: 'registeredAddress',
          mail: 'email1@su.se',
          objectClass: ['posixAccount']
      )
    }

    when:
    def resp = accountServiceImpl.findSuPersonByUid('foo', new SvcAudit())

    then:
    resp instanceof SvcSuPersonVO

    and:
    resp.uid == 'foo'
    resp.givenName == 'givenName'
    resp.sn == 'sn'
    resp.displayName == 'displayName'
    resp.registeredAddress == 'registeredAddress'
    resp.mail == 'email1@su.se'
    resp.accountIsActive
  }

  @Test
  def "activateSuPersonWithMailRoutingAddress: test when attributes are invalid, should throw Exception"() {
    when:
    service.activateSuPerson("uid", "domain", ['affiliation'] as String[], new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "activateSuPerson: test when user exists in LDAP, should handle user and return new password"() {
    given:
    GroovyMock(SuPersonQuery, global: true)
    SuPersonQuery.getSuPersonFromUID(_,_) >> {
      return GroovyMock(SuPerson)
    }

    when:
    def svcUidPwd = service.activateSuPerson(
            "uid",
            "student.su.se",
            ['other'] as String[],
            new SvcAudit())

    then:
    svcUidPwd.uid == 'uid'
    svcUidPwd.password.size() == 10
  }

  @Test
  def "activateSuPerson: test when user doesn't exist in LDAP, should throw exception"() {
    given:
    GroovyMock(SuPersonQuery, global: true)
    SuPersonQuery.getSuPersonFromUID(_,_) >> { null }

    when:
    service.activateSuPerson('uid', "student.su.se", ["other"] as String[], new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test activateSuPerson without null domain argument"() {
    when:
    service.activateSuPerson('uid', null, ["other"] as String[], new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test activateSuPerson without null eduPersonAffiliation argument"() {
    when:
    service.activateSuPerson('uid', "student.su.se", null, new SvcAudit())

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test activateSuPerson without null SvcAudit argument"() {
    when:
    service.activateSuPerson('uid', "student.su.se", ["other"] as String[], null)

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test activateSuPerson Happy Path"() {
    setup:
    def uid = "testuid"
    def password = "*" * 10

    SuPerson suPerson = Mock(SuPerson)
    GroovyMock(SuPersonQuery, global: true)
    SuPersonQuery.getSuPersonFromUID(_,_) >> { suPerson }

    GroovyMock(PasswordUtils, global: true)

    when:
    SvcUidPwd ret = service.activateSuPerson(uid, "student.su.se", ["other"] as String[], new SvcAudit())

    then:
    ret.uid == uid
    ret.password == password
    1 * PasswordUtils.genRandomPassword(10, 10) >> password
  }
}
