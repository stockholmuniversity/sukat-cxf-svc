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
    when:
    def ret = cardInfoServiceImpl.getAllCards("testuid",true,new SvcAudit())
    then:
    ret.size() == 1
    ret[0] instanceof SuCard
  }

  @Test
  def "Test getAllCards throws IllegalArgumentException when person don't exists"() {
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
  def "Test getCardByUUID with null suCardUUID argument, should throw IllegalArgumentException"() {
    setup:
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    cardInfoServiceImpl.getCardByUUID(null,new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getCardByUUID with null SvcAudit argument, should throw IllegalArgumentException"() {
    setup:
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    cardInfoServiceImpl.getCardByUUID("testcarduuid",null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getCardByUUID when card doesn't exist, should throw IllegalArgumentException"() {
    given:
    SuCardQuery.metaClass.static.findCardBySuCardUUID = {String directory,String uid -> return null }

    def cardInfoServiceImpl = new CardInfoServiceImpl()

    when:
    cardInfoServiceImpl.getCardByUUID("testCardUUID", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getCardByUUID default flow"() {
    given:
    SuCardQuery.metaClass.static.findCardBySuCardUUID = {String directory,String uid -> return new SuCard() }

    def cardInfoServiceImpl = new CardInfoServiceImpl()

    when:
    def res = cardInfoServiceImpl.getCardByUUID("testCardUUID", new SvcAudit())

    then:
    assert res
  }
}