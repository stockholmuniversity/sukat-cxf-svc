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
import se.su.it.svc.util.AccountServiceUtils
import se.su.it.svc.AccountService
/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-11
 * Time: 13:48
 * To change this template use File | Settings | File Templates.
 */
class AccountServiceImplTest extends spock.lang.Specification{
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
    Kadmin.metaClass.principalExists = {String uid -> return true}
    Kadmin.metaClass.setPassword = {String uid, String pwd -> return void}
    def accountServiceImpl = new AccountServiceImpl()
    when:
    String pwd = accountServiceImpl.resetPassword("testuid", new SvcAudit())
    then:
    pwd != null
    pwd.length() == 10
  }

  @Test
  def "Test resetPassword correct conversion of uid"() {
    setup:
    String changedUid = null
    String changedUid2 = null
    Kadmin.metaClass.principalExists = {String uid -> changedUid = uid
      return true}
    Kadmin.metaClass.setPassword = {String uid, String pwd -> changedUid2 = uid
      return void}
    def accountServiceImpl = new AccountServiceImpl()
    when:
    String pwd = accountServiceImpl.resetPassword("testuid.jabber", new SvcAudit())
    then:
    changedUid == "testuid/jabber"
    changedUid2 == "testuid/jabber"
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
    suPerson.title = "knallhatt"
    suPerson.eduPersonAffiliation = ["other"]
    String title = null
    String listEntry0 = null
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson(title: "systemdeveloper", eduPersonAffiliation: ["employee"]) }
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson person -> title = person.title;listEntry0=person.eduPersonAffiliation.iterator().next()}
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updateSuPerson("testuid",suPerson, new SvcAudit())
    then:
    title == "knallhatt"
    listEntry0 == "other"
  }

  @Test
  def "Test createSuPerson with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson(null,"it.su.se","196601010357","Test","Testsson",new SvcSuPersonVO(), new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with already exist uid argument"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson() }
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest","it.su.se","196601010357","Test","Testsson",new SvcSuPersonVO(), new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with null domain argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest",null,"196601010357","Test","Testsson",new SvcSuPersonVO(), new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with null nin argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest","it.su.se",null,"Test","Testsson",new SvcSuPersonVO(), new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with null givenName argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest","it.su.se","196601010357",null,"Testsson",new SvcSuPersonVO(), new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with null sn argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest","it.su.se","196601010357","Test",null,new SvcSuPersonVO(), new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with null person argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest","it.su.se","196601010357","Test","Testsson",null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson with null audit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.createSuPerson("testtest","it.su.se","196601010357","Test","Testsson",new SvcSuPersonVO(), null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test createSuPerson true flow"() {
    setup:
    SuInitPerson person1
    SuInitPerson person2
    String script
    String[] argArray
    boolean updatePersArgsOk = false
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuInitPerson.metaClass.parent = "stuts"
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    SuPersonQuery.metaClass.static.initSuPerson = {String directory, SuInitPerson tmpPerson -> person1 = tmpPerson}
    PasswordUtils.metaClass.static.genRandomPassword = {int a, int b -> return "secretpwd"}
    ExecUtils.metaClass.static.exec = {String tmpScript, String[] tmpArgArray -> script = tmpScript; argArray = tmpArgArray; return "OK (uidnumber:245234)"}
    SuPersonQuery.metaClass.static.saveSuInitPerson = {SuInitPerson tmpPerson2 -> person2 = tmpPerson2}
    def accountServiceImpl = Spy(AccountServiceImpl)
    accountServiceImpl.updateSuPerson(*_) >> {String uid, SvcSuPersonVO person,SvcAudit audit -> if(uid == "testtest") updatePersArgsOk = true}
    when:
    def pwd = accountServiceImpl.createSuPerson("testtest","it.su.se","196601010357","Test","Testsson",new SvcSuPersonVO(), new SvcAudit())
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
}
