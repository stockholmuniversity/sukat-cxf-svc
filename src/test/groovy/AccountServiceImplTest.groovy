import org.junit.Test
import se.su.it.svc.AccountServiceImpl
import se.su.it.svc.commons.SvcAudit
import gldapo.GldapoSchemaRegistry
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.ldap.SuPerson
import se.su.it.commons.Kadmin
import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.ldap.SuRole
import se.su.it.svc.query.SuRoleQuery
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
    accountServiceImpl.updateSuPerson(null,null,new SvcSuPersonVO(), new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updateSuPerson with null personVO argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updateSuPerson("testuid",null,null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updateSuPerson with null SvcAudit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updateSuPerson("testuid",null,new SvcSuPersonVO(), null)
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
    accountServiceImpl.updateSuPerson("testuid", null,new SvcSuPersonVO(), new SvcAudit())
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
    accountServiceImpl.updateSuPerson("testuid",null,suPerson, new SvcAudit())
    then:
    title == "knallhatt"
    listEntry0 == "other"
  }

  @Test
  def "Test updateSuPerson when person and role exist"() {
    setup:
    SvcSuPersonVO suPerson = new SvcSuPersonVO()
    suPerson.title = "knallhatt"
    suPerson.eduPersonAffiliation = ["other"]
    String title = null
    String listEntry0 = null
    int roleList = 0
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPerson.metaClass.getDn = {return "uid=nisse,dc=it,dc=su,dc=se"}
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson(title: "systemdeveloper", eduPersonAffiliation: ["employee"]) }
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson person -> title = person.title;listEntry0=person.eduPersonAffiliation.iterator().next()}
    SuRoleQuery.metaClass.static.getSuRoleFromDN = {String directory, String roleDN -> new SuRole(cn: "fakerole", roleOccupant: [])}
    SuRoleQuery.metaClass.static.saveSuRole = {SuRole role -> roleList = role.roleOccupant.size()}
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updateSuPerson("testuid","cn=Teamledare,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE",suPerson, new SvcAudit())
    then:
    title == "knallhatt"
    listEntry0 == "other"
    roleList == 1
  }
}
