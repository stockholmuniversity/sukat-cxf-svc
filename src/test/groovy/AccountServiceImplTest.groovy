import org.junit.Test
import se.su.it.svc.AccountServiceImpl
import se.su.it.svc.commons.SvcAudit
import gldapo.GldapoSchemaRegistry
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.ldap.SuPerson
/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-11
 * Time: 13:48
 * To change this template use File | Settings | File Templates.
 */
class AccountServiceImplTest extends spock.lang.Specification{
  @Test
  def "Test updateAffiliation with null uid argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updateAffiliation(null, "employee", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updateAffiliation with null affiliation argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updateAffiliation("testuid", null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updateAffiliation with null SvcAudit argument"() {
    setup:
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updateAffiliation("testuid", "employee", null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updateAffiliation without person exist"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updateAffiliation("testuid", "employee", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test updateAffiliation when person exist"() {
    setup:
    String myaffiliation = null
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson(eduPersonPrimaryAffiliation: "kalle") }
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson person -> myaffiliation = person.eduPersonPrimaryAffiliation}
    def accountServiceImpl = new AccountServiceImpl()
    when:
    accountServiceImpl.updateAffiliation("testuid", "employee", new SvcAudit())
    then:
    myaffiliation == "employee"
  }
}
