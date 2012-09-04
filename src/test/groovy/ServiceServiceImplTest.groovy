import org.junit.Test
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ServiceServiceImpl
import gldapo.GldapoSchemaRegistry
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.ldap.SuService
import se.su.it.svc.query.SuServiceQuery

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-04
 * Time: 11:26
 * To change this template use File | Settings | File Templates.
 */
class ServiceServiceImplTest extends spock.lang.Specification{
  @Test
  def "Test getServices with null uid argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.getServices(null,new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getServices with null SvcAudit argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.getServices("testuid",null)

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getServices returns list of SuCard when person exists"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    def person = new SuPerson()
    def suServices = [new SuService()]
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    person.metaClass.getDn = {""}
    SuServiceQuery.metaClass.static.getSuServices = {String directory,String dn -> return suServices}
    def serviceServiceImpl = new ServiceServiceImpl()
    when:
    def ret = serviceServiceImpl.getServices("testuid",new SvcAudit())
    then:
    ret.size() == 1
    ret[0] instanceof SuService
  }

  @Test
  def "Test getServices returns empty list of SuCard when person exists"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    def person = new SuPerson()
    def suServices = []
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    person.metaClass.getDn = {""}
    SuServiceQuery.metaClass.static.getSuServices = {String directory,String dn -> return suServices}
    def serviceServiceImpl = new ServiceServiceImpl()
    when:
    def ret = serviceServiceImpl.getServices("testuid",new SvcAudit())
    then:
    ret.size() == 0
  }

  @Test
  def "Test getServices returns exception when person dont exists"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def serviceServiceImpl = new ServiceServiceImpl()
    when:
    def ret = serviceServiceImpl.getServices("testuid",new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getServiceTemplates with null SvcAudit argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.getServiceTemplates(null)

    then:
    thrown(IllegalArgumentException)
  }
}
