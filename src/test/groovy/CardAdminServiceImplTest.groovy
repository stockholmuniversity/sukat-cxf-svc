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
}
