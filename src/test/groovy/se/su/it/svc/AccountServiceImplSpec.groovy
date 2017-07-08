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

package se.su.it.svc

import gldapo.GldapoSchemaRegistry
import org.apache.commons.lang.NotImplementedException
import org.gcontracts.PostconditionViolation
import org.gcontracts.PreconditionViolation
import se.su.it.commons.Kadmin
import se.su.it.commons.PasswordUtils
import se.su.it.svc.commons.SvcPostalAddressVO
import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.commons.SvcUidPwd

import se.su.it.svc.ldap.Account
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuPersonStub

import se.su.it.svc.query.AccountQuery
import se.su.it.svc.query.SuPersonQuery

import se.su.it.svc.util.AccountServiceUtils
import se.su.it.svc.util.GeneralUtils
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method

class AccountServiceImplSpec extends Specification {

  @Shared
  AccountServiceImpl service

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
    this.service = new AccountServiceImpl()
    SuPersonStub.metaClass.save = {->}
    SuPersonStub.metaClass.parent = "parent"
    SuPersonStub.metaClass.directory = "directory"
  }

  def cleanup() {
    this.service = null
    AccountServiceUtils.metaClass = null
    GeneralUtils.metaClass = null
    Kadmin.metaClass = null
    SuPersonStub.metaClass = null
    SuPersonQuery.metaClass = null
    GldapoSchemaRegistry.metaClass = null
  }

    def "setHomePostalAddress: happy path"()
    {
        setup:
        SuPersonQuery.metaClass.static.getSuPersonFromUID = { String directory, String uid -> new SuPerson(uid: "foo") }
        SuPersonQuery.metaClass.static.updateSuPerson = { SuPerson person -> return true }

        when:
        service.setHomePostalAddress("shpauid", new SvcPostalAddressVO(country: 'SE'))

        then:
        notThrown(Exception)
    }

    def "setHomePostalAddress: street2 is set"()
    {
        setup:
        SuPersonQuery.metaClass.static.getSuPersonFromUID = { String directory, String uid -> new SuPerson(uid: "foo") }
        SuPersonQuery.metaClass.static.updateSuPerson = { SuPerson person -> return true }

        when:
        service.setHomePostalAddress("shpauid", new SvcPostalAddressVO(street2: 'a', country: 'SE'))

        then:
        notThrown(Exception)
    }

    def "setHomePostalAddress: invalid country code"()
    {
        setup:
        SuPersonQuery.metaClass.static.getSuPersonFromUID = { String directory, String uid -> new SuPerson(uid: "foo") }
        SuPersonQuery.metaClass.static.updateSuPerson = { SuPerson person -> return true }

        when:
        service.setHomePostalAddress("shpauid", new SvcPostalAddressVO(country: '11'))

        then:
        thrown(IllegalArgumentException)
    }

  def "createSubAccount: happy path"()
  {
    setup:
    AccountServiceUtils.metaClass.static.createSubAccount = { String a, String b -> }

    when:
    service.createSubAccount("csauid", "csaType")

    then:
    notThrown(Exception)
  }

  def "deleteSubAccount: happy path"()
  {
    setup:
    AccountServiceUtils.metaClass.static.deleteSubAccount = { String a, String b -> }

    when:
    service.deleteSubAccount("csauid", "csaType")

    then:
    notThrown(Exception)
  }

  def "getSubAccount: happy path"()
  {
    setup:
    AccountServiceUtils.metaClass.static.getSubAccount = { String a, String b -> [uid: 'gsaTest'] }

    when:
    def ret = service.getSubAccount("gsauid", "gsaType")

    then:
    ret.uid == "gsaTest"
  }

  def "Test updatePrimaryAffiliation with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.updatePrimaryAffiliation(null, "employee")

    then:
    thrown(PreconditionViolation)
  }

  def "Test updatePrimaryAffiliation with null affiliation argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.updatePrimaryAffiliation("testuid", null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test updatePrimaryAffiliation without person exist"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updatePrimaryAffiliation("testuid", "employee")
    then:
    thrown(IllegalArgumentException)
  }

  def "Test updatePrimaryAffiliation when person exist"() {
    setup:
    String myaffiliation = null
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson(eduPersonPrimaryAffiliation: "kalle") }
    SuPersonQuery.metaClass.static.updateSuPerson = {SuPerson person -> myaffiliation = person.eduPersonPrimaryAffiliation}
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updatePrimaryAffiliation("testuid", "employee")
    then:
    myaffiliation == "employee"
  }

  def "getPassword: happy path"()
  {
    setup:
    GeneralUtils.metaClass.static.execHelper = { String a, String b -> [password: 'gppasswd'] }

    when:
    def ret = service.getPassword("gptest/ppp")

    then:
    ret == "gppasswd"
  }

  def "Test resetPassword with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.resetPassword(null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test resetPassword uid dont exist"() {
    setup:
    Kadmin.metaClass.principalExists = {String uid -> return false}
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.resetPassword("testuid")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test resetPassword password 10 chars"() {
    setup:
    GroovyMock(PasswordUtils, global: true)
    def kadmin = Mock(Kadmin)
    Kadmin.metaClass.static.newInstance = { kadmin }
    kadmin.principalExists(_) >> true
    def accountServiceImpl = new AccountServiceImpl()

    when:
    def ret = accountServiceImpl.resetPassword("testuid")

    then:
    1* PasswordUtils.genRandomPassword(10, 10) >> "*" *10
    assert ret == "*" *10
  }

  def "Test resetPassword correct conversion of uid"() {
    setup:
    def kadmin = Mock(Kadmin)
    Kadmin.metaClass.static.newInstance = { kadmin }

    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.resetPassword("testuid.jabber")

    then:
    1 * kadmin.principalExists("testuid/jabber") >> true
    1 * kadmin.setPassword("testuid/jabber", _)
  }

  def "scramblePassword - is a void method"() {
    when:
    Method m = AccountServiceImpl.getMethod('scramblePassword', String)

    then:
    m.returnType == void
  }

  def "scramblePassword - happy path"() {
    setup:
    String uid = "foo"
    def spy = Spy(AccountServiceImpl) {
      1 * resetPassword(uid) >> 'foobar'
    }

    when:
    spy.scramblePassword(uid)

    then:
    notThrown(IllegalStateException)
  }

  def "scramblePassword - passes exception forward"() {
    setup:
    String uid = "foo"
    def spy = Spy(AccountServiceImpl) {
      1 * resetPassword(uid) >> { throw new IllegalStateException("foo") }
    }

    when:
    spy.scramblePassword(uid)

    then:
    thrown(IllegalStateException)
  }

  def "scramblePassword - fails precondition on null uid"() {
    when:
    new AccountServiceImpl().scramblePassword(null)

    then:
    thrown(PreconditionViolation)
  }

  def "scramblePassword - fails precondition on empty uid"() {
    when:
    new AccountServiceImpl().scramblePassword('')

    then:
    thrown(PreconditionViolation)
  }

  def "Test updateSuPerson with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.updateSuPerson(null,new SvcSuPersonVO())

    then:
    thrown(PreconditionViolation)
  }

  def "Test updateSuPerson with null personVO argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.updateSuPerson("testuid",null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test updateSuPerson without person exist"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String directory, String uid -> throw new IllegalArgumentException("foo") }
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.updateSuPerson("testuid",new SvcSuPersonVO())

    then:
    thrown(IllegalArgumentException)
  }

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
    accountServiceImpl.updateSuPerson("testuid",suPerson)
    then:
    title.iterator().next() == "knallhatt"
    listEntry0 == "other"
  }

  def "Test createSuPerson with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson(null,"196601010357","Test","Testsson")

    then:
    thrown(PreconditionViolation)
  }

    def "createSuPerson: account already exist"()
    {
        setup:
        AccountQuery.metaClass.static.findAccountByUid = { String directory, String uid -> new Account() }
        def accountServiceImpl = new AccountServiceImpl()

        when:
        accountServiceImpl.createSuPerson("testtest", "6601010357", "Test", "Testsson")

        then:
        thrown(IllegalArgumentException)
    }

  def "Test createSuPerson with already exist uid argument"() {
    setup:
    AccountQuery.metaClass.static.findAccountByUid = { String directory, String uid -> null }
    SuPersonQuery.metaClass.static.findSuPersonByUID = {String directory,String uid -> new SuPerson() }
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest","6601010357","Test","Testsson")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test createSuPerson with null ssn argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest",null,"Test","Testsson")

    then:
    thrown(PreconditionViolation)
  }

  def "Test createSuPerson with wrong ssn argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest","20001128-5764","Test","Testsson")

    then:
    thrown(PreconditionViolation)
  }

  def "createSuPerson: socialSecurityNumber already exists"() {
    setup:
    AccountQuery.metaClass.static.findAccountByUid = { String directory, String uid -> null }
    SuPersonQuery.metaClass.static.findSuPersonByUID = {String directory,String uid -> null }
    SuPersonQuery.metaClass.static.getSuPersonFromSsn = {String directory,String uid -> new SuPerson() }
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest","6601010357","Test","Testsson")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test createSuPerson with null givenName argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest","196601010357",null,"Testsson")

    then:
    thrown(PreconditionViolation)
  }

  def "Test createSuPerson with null sn argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest","196601010357","Test",null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test createSuPerson with null person argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.createSuPerson("testtest","196601010357","Test","Testsson")

    then:
    thrown(PreconditionViolation)
  }

  def "Test createSuPerson true flow"() {
    setup:
    Properties properties = new Properties()
    properties.put("parent", "foo")
    service.configManager = [config:[ldap:[accounts:[default:properties]]]]

    def uid = 'uid'
    def ssn = '0000000000'
    def givenName = 'Test'
    def sn = 'Testsson'

    SuPersonQuery.metaClass.static.findSuPersonByUID = { String a, String b -> null }
    SuPersonQuery.metaClass.static.getSuPersonFromSsn = { String a, String b -> null }

    def spy = GroovySpy(SuPersonStub)

    SuPersonStub.metaClass.static.newInstance = { String arg1, String arg2, String arg3, String arg4, String arg5, String arg6 ->
      return spy
    }

    SuPersonStub.metaClass.save = {-> delegate.uid = uid.reverse() }

    when:
    service.createSuPerson(
            uid,
            ssn,
            givenName,
            sn)

    then: 'we test that the object returned has been saved.'
    spy.uid == uid.reverse()
  }

  def "Test terminateSuPerson"() {
    when:
    new AccountServiceImpl().terminateSuPerson("testuid")

    then:
    thrown(NotImplementedException)
  }

  def "Test getMailRoutingAddress with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.getMailRoutingAddress(null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test getMailRoutingAddress with person not found"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String directory, String uid -> throw new IllegalArgumentException("foo") }
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.getMailRoutingAddress("testuid")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test getMailRoutingAddress Happy Path"() {
    setup:
    SuPerson suPerson = new SuPerson(mailRoutingAddress: "kalle")
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return suPerson }
    def accountServiceImpl = new AccountServiceImpl()
    when:
    def ret = accountServiceImpl.getMailRoutingAddress("testuid")
    then:
    ret == "kalle"
  }

  def "Test setMailRoutingAddress with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.setMailRoutingAddress(null, "mail@test.su.se")

    then:
    thrown(PreconditionViolation)
  }

  def "Test setMailRoutingAddress with null mail argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.setMailRoutingAddress("testuid", null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test setMailRoutingAddress with wrong format mail argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.setMailRoutingAddress("testuid", "testuser.mail.se")

    then:
    thrown(PreconditionViolation)
  }

  def "Test setMailRoutingAddress with person not found"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String directory, String uid -> throw new IllegalArgumentException("foo") }
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.setMailRoutingAddress("testuid", "mail@test.su.se")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test setMailRoutingAddress Happy Path"() {
    setup:
    SuPerson suPerson = new SuPerson(mailRoutingAddress: "kalle", objectClass: [])
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return suPerson }
    SuPersonQuery.metaClass.static.updateSuPerson = {SuPerson tmpp -> suPerson.mailRoutingAddress = tmpp.mailRoutingAddress}
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.setMailRoutingAddress("testuid", "mail@test.su.se")

    then:
    suPerson.mailRoutingAddress == "mail@test.su.se"

    and:
    suPerson.objectClass.contains("inetLocalMailRecipient")
  }

  def "Test findSuPersonBySocialSecurityNumber: with invalid ssn"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.findAllSuPersonsBySocialSecurityNumber(null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test findSuPersonBySocialSecurityNumber: When a user ain't found"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromSsn = { String a, String b -> new SvcSuPersonVO[0] }

    when:
    def ret = accountServiceImpl.findAllSuPersonsBySocialSecurityNumber('1001010000')

    then:
    notThrown(PostconditionViolation)
    ret.size() == 0
  }

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
    def resp = accountServiceImpl.findAllSuPersonsBySocialSecurityNumber('1001010000')

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

  def "Test findSuPersonByUid: with invalid ssn"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()

    when:
    accountServiceImpl.findSuPersonByUid(null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test findSuPersonByUid: When a user ain't found"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    SuPersonQuery.metaClass.static.findSuPersonByUID = { String directory, String uid -> return null }

    when:
    def resp = accountServiceImpl.findSuPersonByUid('foo')

    then:
    resp == null
  }

  def "Test findSuPersonByUid: When a user is found"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    SuPersonQuery.metaClass.static.findSuPersonByUID = {String directory,String uid ->
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
    def resp = accountServiceImpl.findSuPersonByUid('foo')

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

  def "activateSuPersonWithMailRoutingAddress: test when attributes are invalid, should throw Exception"() {
    when:
    service.activateSuPerson("uid", "domain", ['affiliation'] as String[])

    then:
    thrown(PreconditionViolation)
  }

  def "activateSuPerson: test when user exists in LDAP, should handle user and return new password"() {
    given:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> return GroovyMock(SuPerson) }

    when:
    def svcUidPwd = service.activateSuPerson(
            "uid",
            "student.su.se",
            ['other'] as String[])

    then:
    svcUidPwd.uid == 'uid'
    svcUidPwd.password.size() == 10
  }

  def "activateSuPerson: test when user doesn't exist in LDAP, should throw exception"() {
    given:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String directory, String uid -> throw new IllegalArgumentException("foo") }

    when:
    service.activateSuPerson('uid', "student.su.se", ["other"] as String[])

    then:
    thrown(IllegalArgumentException)
  }

  def "Test activateSuPerson without null domain argument"() {
    when:
    service.activateSuPerson('uid', null, ["other"] as String[])

    then:
    thrown(PreconditionViolation)
  }

  def "Test activateSuPerson without null eduPersonAffiliation argument"() {
    when:
    service.activateSuPerson('uid', "student.su.se", null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test activateSuPerson Happy Path"() {
    setup:
    def uid = "testuid"
    def password = "*" * 10

    SuPerson suPerson = Mock(SuPerson)
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> suPerson }

    GroovyMock(PasswordUtils, global: true)

    when:
    SvcUidPwd ret = service.activateSuPerson(uid, "student.su.se", ["other"] as String[])

    then:
    ret.uid == uid
    ret.password == password
    1 * PasswordUtils.genRandomPassword(10, 10) >> password
  }

  def "addMailLocalAddresses: given no valid uid"() {
    when:
    service.addMailLocalAddresses('', [] as String[])

    then:
    thrown(PreconditionViolation)
  }

  def "addMailLocalAddresses: given no mailLocalAddresses"() {
    when:
    service.addMailLocalAddresses('foo', [] as String[])

    then:
    thrown(PreconditionViolation)
  }

  def "addMailLocalAddresses: given an invalid email in mailLocalAddresses"() {
    when:
    service.addMailLocalAddresses('foo', ['foo', 'kaka@su.se'] as String[])

    then:
    thrown(PreconditionViolation)
  }

  def "addMailLocalAddresses: When SuPerson can't be found by uid"() {
    given:
    SuPerson.metaClass.static.find = { Map arg1, Closure arg2 ->}

    when:
    service.addMailLocalAddresses('foo', ['kaka@su.se'] as String[])

    then:
    thrown(IllegalArgumentException)
  }

  def "addMailLocalAddresses: When SuPerson doesn't already have the attribute mailLocalAddress"() {
    given:
    SuPerson.metaClass.update = {-> }
    def mailLocalAddresses = ['kaka@su.se', "bar@su.se"]
    String uid = 'foo'
    SuPerson suPerson = GroovyMock(SuPerson)
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> suPerson }

    when:
    def resp = service.addMailLocalAddresses(uid, mailLocalAddresses as String[])

    then:
    resp == mailLocalAddresses

    and:
    1 * suPerson.addMailLocalAddress(_) >> mailLocalAddresses
  }

  def "addMailLocalAddresses: When SuPerson"() {
    given:
    SuPerson.metaClass.update = {-> }
    def mailLocalAddresses = ['kaka@su.se', "bar@su.se"]
    String uid = 'foo'
    SuPerson suPerson = GroovyMock(SuPerson) {
      1 * addMailLocalAddress(*_) >> mailLocalAddresses
    }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> suPerson }

    when:
    def resp = service.addMailLocalAddresses(uid, mailLocalAddresses as String[])

    then:
    resp instanceof String[]
  }

}
