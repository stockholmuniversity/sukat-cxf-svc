import org.junit.Test
import se.su.it.svc.AccountServiceImpl
import se.su.it.svc.commons.SvcAudit
import gldapo.GldapoSchemaRegistry
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.ldap.SuPerson
import se.su.it.commons.Kadmin
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
}
