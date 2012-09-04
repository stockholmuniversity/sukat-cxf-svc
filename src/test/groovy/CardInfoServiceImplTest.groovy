import spock.lang.*
import org.junit.Test
import se.su.it.svc.CardInfoServiceImpl
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuCard
import gldapo.GldapoSchemaRegistry
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.query.SuCardQuery
import se.su.it.svc.manager.EhCacheManager
import org.springframework.context.ApplicationContext
import net.sf.ehcache.hibernate.EhCache
import se.su.it.svc.manager.ApplicationContextProvider

@Mock([EhCacheManager, EhCache, ApplicationContext, ApplicationContextProvider])
class CardInfoServiceImplTest extends spock.lang.Specification {
  @Test
  def "Test getAllCards with null uid argument"() {
    setup:
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    cardInfoServiceImpl.getAllCards(null,false,new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getAllCards with null SvcAudit argument"() {
    setup:
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    cardInfoServiceImpl.getAllCards("testuid",false,null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getAllCards returns list of SuCard when person exists"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    def person = new SuPerson()
    def suCards = [new SuCard()]
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    person.metaClass.getDn = {""}
    SuCardQuery.metaClass.static.findAllCardsBySuPersonDnAndOnlyActiveOrNot = {String directory,String dn, boolean onlyActiveCards -> return suCards}
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    SuCardQuery suCardQuery = new SuCardQuery()

    ApplicationContext applicationContext = Mock(ApplicationContext)
    EhCacheManager manager = Mock(EhCacheManager)
    suCardQuery.metaClass.static.applicationContext = (ApplicationContext)applicationContext
    suCardQuery.metaClass.static.cacheManager = (EhCacheManager)manager
//    suCardQuery.metaClass.static.applicationContext.getBean = {String bean -> return manager}
    when:
    def ret = cardInfoServiceImpl.getAllCards("testuid",true,new SvcAudit())
    then:
    ret.size() == 1
    ret[0] instanceof SuCard
  }

  @Test
  def "Test getAllCards returns empty list when person dont exists"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    def ret = cardInfoServiceImpl.getAllCards("testuid",true,new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getCardByUUID with null suCardUUID argument"() {
    setup:
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    cardInfoServiceImpl.getCardByUUID(null,new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getCardByUUID with null SvcAudit argument"() {
    setup:
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    cardInfoServiceImpl.getCardByUUID("testcarduuid",null)
    then:
    thrown(IllegalArgumentException)
  }
}