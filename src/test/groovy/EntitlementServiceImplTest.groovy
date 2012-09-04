import org.junit.Test
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.EntitlementServiceImpl
import gldapo.GldapoSchemaRegistry
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.ldap.SuPerson

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-03
 * Time: 08:34
 * To change this template use File | Settings | File Templates.
 */
class EntitlementServiceImplTest extends spock.lang.Specification{
  @Test
  def "Test addEntitlement with null uid argument"() {
    setup:
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    entitlementServiceImpl.addEntitlement(null,"urn:mace:swami.se:gmai:test:test",new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test addEntitlement with null entitlement argument"() {
    setup:
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    entitlementServiceImpl.addEntitlement("testuid",null,new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test addEntitlement with null SvcAudit argument"() {
    setup:
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    entitlementServiceImpl.addEntitlement("testuid","urn:mace:swami.se:gmai:test:test",null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test addEntitlement when person dont exists"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    def ret = entitlementServiceImpl.addEntitlement("testuid","urn:mace:swami.se:gmai:test:test",new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test addEntitlement whith duplicate entitlement"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPerson person = new SuPerson()
    def tmpSet = new java.util.LinkedHashSet<String>()
    tmpSet.add("urn:mace:swami.se:gmai:test:test")
    person.eduPersonEntitlement = tmpSet
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson arg1 -> return void}
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    def ret = entitlementServiceImpl.addEntitlement("testuid","urn:mace:swami.se:gmai:test:test",new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test addEntitlement"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPerson person = new SuPerson()
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson arg1 -> return void}
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    def ret = entitlementServiceImpl.addEntitlement("testuid","urn:mace:swami.se:gmai:test:test",new SvcAudit())
    then:
    person.eduPersonEntitlement.contains("urn:mace:swami.se:gmai:test:test") == true
  }

  @Test
  def "Test removeEntitlement with null uid argument"() {
    setup:
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    entitlementServiceImpl.removeEntitlement(null,"urn:mace:swami.se:gmai:test:test",new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test removeEntitlement with null entitlement argument"() {
    setup:
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    entitlementServiceImpl.removeEntitlement("testuid",null,new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test removeEntitlement with null SvcAudit argument"() {
    setup:
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    entitlementServiceImpl.removeEntitlement("testuid","urn:mace:swami.se:gmai:test:test",null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test removeEntitlement when person dont exists"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    def ret = entitlementServiceImpl.removeEntitlement("testuid","urn:mace:swami.se:gmai:test:test",new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test removeEntitlement with no eduPersonEntitlement list in person object"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPerson person = new SuPerson()
    person.eduPersonEntitlement = null
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson arg1 -> return void}
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    def ret = entitlementServiceImpl.removeEntitlement("testuid","urn:mace:swami.se:gmai:test:test",new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test removeEntitlement with no same entitlement in list"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPerson person = new SuPerson()
    def tmpSet = new java.util.LinkedHashSet<String>()
    tmpSet.add("urn:mace:swami.se:gmai:test:test")
    person.eduPersonEntitlement = tmpSet
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson arg1 -> return void}
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    def ret = entitlementServiceImpl.removeEntitlement("testuid","urn:mace:swami.se:gmai:test:imnotthere",new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test removeEntitlement"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPerson person = new SuPerson()
    def tmpSet = new java.util.LinkedHashSet<String>()
    tmpSet.add("urn:mace:swami.se:gmai:test:test")
    person.eduPersonEntitlement = tmpSet
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson arg1 -> return void}
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    def ret = entitlementServiceImpl.removeEntitlement("testuid","urn:mace:swami.se:gmai:test:test",new SvcAudit())
    then:
    person.eduPersonEntitlement.contains("urn:mace:swami.se:gmai:test:test") == false
  }
}
