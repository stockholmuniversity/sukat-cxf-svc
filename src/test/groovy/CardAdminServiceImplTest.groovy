import spock.lang.*
import org.junit.Test
import se.su.it.svc.CardInfoServiceImpl
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuCard
import gldapo.GldapoSchemaRegistry
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.query.SuCardQuery
import se.su.it.svc.CardAdminServiceImpl

class CardAdminServiceImplTest extends spock.lang.Specification{
  @Test
  def "Test revokeCard with null suCardUUID argument"() {
    setup:
    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.revokeCard(null,new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test revokeCard with null SvcAudit argument"() {
    setup:
    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.revokeCard("testcarduuid",null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test revokeCard returns true and sets state to revoked"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    def suCard = new SuCard()
    SuCardQuery.metaClass.static.findCardBySuCardUUID = {String arg1, String arg2 -> return suCard}
    SuCardQuery.metaClass.static.saveSuCard = {SuCard arg1 -> return void}

    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.revokeCard("testcarduuid",new SvcAudit())
    then:
    suCard.suCardState == "urn:x-su:su-card:state:revoked"
  }

  @Test
  def "Test revokeCard returns false when no card was found"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuCardQuery.metaClass.static.findCardBySuCardUUID = {String arg1, String arg2 -> return null}

    def cardAdminServiceImpl = new CardAdminServiceImpl()
    when:
    cardAdminServiceImpl.revokeCard("testcarduuid",new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }
}
