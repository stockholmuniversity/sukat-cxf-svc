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

import org.apache.commons.lang.NotImplementedException
import org.junit.After
import org.junit.Test
import se.su.it.svc.AccountServiceImpl
import se.su.it.svc.commons.SvcAudit
import gldapo.GldapoSchemaRegistry
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.ldap.SuPerson
import se.su.it.commons.Kadmin
import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.ldap.SuInitPerson
import se.su.it.commons.ExecUtils
import se.su.it.commons.PasswordUtils
import spock.lang.Ignore

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-11
 * Time: 13:48
 * To change this template use File | Settings | File Templates.
 */
class AccountServiceImplTest extends spock.lang.Specification {

  @After
  def tearDown() {
    Kadmin.metaClass = null
    GldapoSchemaRegistry.metaClass = null
    SuInitPerson.metaClass = null
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
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updatePrimaryAffiliation with null affiliation argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updatePrimaryAffiliation("testuid", null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updatePrimaryAffiliation with null SvcAudit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updatePrimaryAffiliation("testuid", "employee", null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updatePrimaryAffiliation without person exist"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
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
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson(eduPersonPrimaryAffiliation: "kalle") }
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson person -> myaffiliation = person.eduPersonPrimaryAffiliation}
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
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test resetPassword with null audit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.resetPassword("testuid", null)
    then:
    thrown(IllegalArgumentException)
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
    1* PasswordUtils.genRandomPassword(10, 10) >> "**********"
    assert ret == "**********"
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
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updateSuPerson with null personVO argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updateSuPerson("testuid",null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updateSuPerson with null SvcAudit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updateSuPerson("testuid",new SvcSuPersonVO(), null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updateSuPerson without person exist"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
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
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson(title: ["systemdeveloper"], eduPersonAffiliation: ["employee"]) }
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson person -> title = person.title;listEntry0=person.eduPersonAffiliation.iterator().next()}
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
    accountServiceImpl.createSuPerson(null,"it.su.se","196601010357","Test","Testsson",new SvcSuPersonVO(), false, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with already exist uid argument"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUIDNoCache = {String directory,String uid -> new SuPerson() }
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest","it.su.se","196601010357","Test","Testsson",new SvcSuPersonVO(), false, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with null domain argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest",null,"196601010357","Test","Testsson",new SvcSuPersonVO(), false, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with null nin argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest","it.su.se",null,"Test","Testsson",new SvcSuPersonVO(), false, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with wrong nin argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest","it.su.se","20001128-5764","Test","Testsson",new SvcSuPersonVO(), false, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with null givenName argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest","it.su.se","196601010357",null,"Testsson",new SvcSuPersonVO(), false, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with null sn argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest","it.su.se","196601010357","Test",null,new SvcSuPersonVO(), false, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with null person argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest","it.su.se","196601010357","Test","Testsson",null, false, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with null audit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest","it.su.se","196601010357","Test","Testsson",new SvcSuPersonVO(), false, null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  @Ignore //TODO: Rebuild this test into smaller parts.
  def "Test createSuPerson true flow"() {
    setup:
    SuInitPerson person1
    SuInitPerson person2
    String script
    String[] argArray
    boolean updatePersArgsOk = false
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuInitPerson.metaClass.parent = "stuts"
    SuPersonQuery.metaClass.static.getSuPersonFromUIDNoCache = {String directory,String uid -> return null }
    SuPersonQuery.metaClass.static.initSuPerson = {String directory, SuInitPerson tmpPerson -> person1 = tmpPerson}
    PasswordUtils.metaClass.static.genRandomPassword = {int a, int b -> return "secretpwd"}
    ExecUtils.metaClass.static.exec = {String tmpScript, String[] tmpArgArray -> script = tmpScript; argArray = tmpArgArray; return "OK (uidnumber:245234)"}
    SuPersonQuery.metaClass.static.saveSuInitPerson = {SuInitPerson tmpPerson2 -> person2 = tmpPerson2}
    def accountServiceImpl = Spy(AccountServiceImpl)
    accountServiceImpl.updateSuPerson(*_) >> {String uid, SvcSuPersonVO person,SvcAudit audit -> if(uid == "testtest") updatePersArgsOk = true}

    when:
    def pwd = accountServiceImpl.createSuPerson("testtest","it.su.se","196601010357","Test","Testsson",new SvcSuPersonVO(), true, new SvcAudit())

    then:
    pwd == "secretpwd"
    person1.uid == "testtest"
    person1.cn == "Test Testsson"
    person1.sn == "Testsson"
    person1.givenName == "Test"
    person1.norEduPersonNIN == "196601010357"
    person1.eduPersonPrincipalName == "testtest@su.se"
    person1.objectClass.containsAll(["suPerson","sSNObject","norEduPerson","eduPerson","inetOrgPerson","organizationalPerson","person","top"])
    person1.parent == "dc=it,dc=su,dc=se"

    person2.objectClass.contains("posixAccount")
    person2.loginShell == "/usr/local/bin/bash"
    person2.homeDirectory == "/afs/su.se/home/t/e/testtest"
    person2.uidNumber == "245234"
    person2.gidNumber == "1200"

    script == "/local/scriptbox/bin/run-token-script.sh"
    argArray.toList().containsAll(["--user", "uadminw", "/local/sukat/libexec/enable-user.pl", "--uid", "testtest", "--password", "secretpwd", "--gidnumber", "1200"])
    updatePersArgsOk == true
  }

  @Test
  def "Test terminateSuPerson with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.terminateSuPerson(null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test terminateSuPerson with null SvcAudit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.terminateSuPerson("testuid", null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test terminateSuPerson without person exist"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.terminateSuPerson("testuid", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test terminateSuPerson when person exist"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson() }
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.terminateSuPerson("testuid", new SvcAudit())
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
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getMailRoutingAddress with null audit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.getMailRoutingAddress("testuid", null)
    then:
    thrown(IllegalArgumentException)
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
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test setMailRoutingAddress with null mail argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.setMailRoutingAddress("testuid", null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test setMailRoutingAddress with wrong format mail argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.setMailRoutingAddress("testuid", "testuser.mail.se", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test setMailRoutingAddress with null audit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.setMailRoutingAddress("testuid", "mail@test.su.se", null)
    then:
    thrown(IllegalArgumentException)
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
    SuPerson suPerson = new SuPerson(mailRoutingAddress: "kalle")
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return suPerson }
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson tmpp -> suPerson.mailRoutingAddress = tmpp.mailRoutingAddress}
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.setMailRoutingAddress("testuid", "mail@test.su.se", new SvcAudit())
    then:
    suPerson.mailRoutingAddress == "mail@test.su.se"
  }

  @Test
  def "Test findSuInitPersonByNorEduPersonNIN: with invalid nin"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.findSuPersonByNorEduPersonNIN(null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test findSuInitPersonByNorEduPersonNIN: When a user ain't found"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromNin = {String directory,String uid -> return null }
    when:
    accountServiceImpl.findSuPersonByNorEduPersonNIN('180001010000', new SvcAudit())
    then:
    thrown(Exception)
  }

  @Test
  def "Test findSuPersonByNorEduPersonNIN: When a user is found"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromNin = {String directory,String uid -> new SuPerson(uid:'foo') }

    when:
    def resp = accountServiceImpl.findSuPersonByNorEduPersonNIN('180001010000', new SvcAudit())

    then:
    resp instanceof SvcSuPersonVO

    and:
    resp.uid == 'foo'
  }

  @Test
  def "Test findSuPersonBySocialSecurityNumber: with invalid ssn"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.findSuPersonBySocialSecurityNumber(null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test findSuPersonBySocialSecurityNumber: When a user ain't found"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromSsn = {String directory,String uid -> return null }
    when:
    def resp = accountServiceImpl.findSuPersonBySocialSecurityNumber('1001010000', new SvcAudit())
    then:
    resp instanceof SvcSuPersonVO

    and:
    resp.uid == null
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
          mail:['email1@su.se', 'email2@su.se'],
          objectClass: []
      )
    }

    when:
    def resp = accountServiceImpl.findSuPersonBySocialSecurityNumber('1001010000', new SvcAudit())

    then:
    resp instanceof SvcSuPersonVO

    and:
    resp.uid == 'foo'
    resp.givenName == 'givenName'
    resp.sn == 'sn'
    resp.displayName == 'displayName'
    resp.registeredAddress == 'registeredAddress'
    (resp.mail as Set).contains('email1@su.se')
    (resp.mail as Set).contains('email2@su.se')
    !resp.accountIsActive
  }

  @Test
  def "Test findSuPersonByUid: with invalid ssn"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.findSuPersonByUid(null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test findSuPersonByUid: When a user ain't found"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    when:
    def resp = accountServiceImpl.findSuPersonByUid('foo', new SvcAudit())
    then:
    resp instanceof SvcSuPersonVO

    and:
    resp.uid == null
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
          mail:['email1@su.se', 'email2@su.se'],
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
    (resp.mail as Set).contains('email1@su.se')
    (resp.mail as Set).contains('email2@su.se')
    resp.accountIsActive
  }

}
