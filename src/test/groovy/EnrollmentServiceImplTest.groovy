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
  def "Test enrollUserByUid with null uid argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.enrollUserByUid(null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUserByUid with null SvcAudit argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.enrollUserByUid("testuid", null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUserByUid without person exist"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.enrollUserByUid("testuid", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUserByUid have posix"() {
    setup:
    boolean haveKeeprouting = false
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return new SuPerson(objectClass: ["posixAccount"]) }
    Kadmin.metaClass.resetOrCreatePrincipal = { String uid -> return "pwd"}
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson person -> haveKeeprouting = person.eduPersonEntitlement?.contains("urn:x-su:autoenable-keeprouting")}
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.enrollUserByUid("testuid", new SvcAudit())
    then:
    haveKeeprouting == false
  }

  @Test
  def "Test enrollUserByUid have keeprouting"() {
    setup:
    boolean doesSave = false
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return new SuPerson(eduPersonEntitlement: ["urn:x-su:autoenable-keeprouting"]) }
    Kadmin.metaClass.resetOrCreatePrincipal = { String uid -> return "pwd"}
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson person -> doesSave = true}
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.enrollUserByUid("testuid", new SvcAudit())
    then:
    doesSave == false
  }

  @Test
  def "Test enrollUserByUid"() {
    setup:
    boolean haveKeeproutingAndPosix = false
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return new SuPerson() }
    Kadmin.metaClass.resetOrCreatePrincipal = { String uid -> return "pwd"}
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson person -> haveKeeproutingAndPosix = person.eduPersonEntitlement?.contains("urn:x-su:autoenable-keeprouting")}
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.enrollUserByUid("testuid", new SvcAudit())
    then:
    haveKeeproutingAndPosix == true
  }
}
