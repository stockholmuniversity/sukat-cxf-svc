import org.junit.Test
import se.su.it.svc.commons.SvcAudit
import gldapo.GldapoSchemaRegistry
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.EnrollmentServiceImpl
import se.su.it.commons.Kadmin
import se.su.it.svc.ldap.SuPerson
/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-13
 * Time: 08:36
 * To change this template use File | Settings | File Templates.
 */
class EnrollmentServiceImplTest extends spock.lang.Specification{
  @Test
  def "Test resetAndExpirePwd with null uid argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.resetAndExpirePwd(null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test resetAndExpirePwd with null SvcAudit argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.resetAndExpirePwd("testuid", null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test resetAndExpirePwd without person exist"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.resetAndExpirePwd("testuid", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

}
