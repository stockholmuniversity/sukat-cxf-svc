import spock.lang.*
import org.junit.Test
import se.su.it.svc.CardInfoServiceImpl
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuCard
import gldapo.GldapoSchemaRegistry

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
    SuPerson.metaClass.static.getPersonFromUID = {String uid -> return person }
    person.metaClass.getDn = {""}
    SuCard.metaClass.static.findAll = {Object arg1,Closure arg2 -> return suCards}
    def cardInfoServiceImpl = new CardInfoServiceImpl()
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
    SuPerson.metaClass.static.getPersonFromUID = {String uid -> return null }
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    def ret = cardInfoServiceImpl.getAllCards("testuid",true,new SvcAudit())
    then:
    ret.size() == 0
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