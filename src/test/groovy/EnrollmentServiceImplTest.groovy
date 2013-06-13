import org.junit.Test
import se.su.it.commons.PasswordUtils
import se.su.it.svc.commons.SvcAudit
import gldapo.GldapoSchemaRegistry
import se.su.it.svc.commons.SvcUidPwd
import se.su.it.svc.ldap.SuInitPerson
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.EnrollmentServiceImpl
import se.su.it.commons.Kadmin
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.util.EnrollmentServiceUtils

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

  @Test
  def "Test enrollUserByNIN without null nin argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUserByNIN(null, new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUserByNIN without null SvcAudit argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUserByNIN("100000000000", null)

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUserByNIN with bad format nin argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUserByNIN("100000", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUserByNIN search with 10 - digit"() {
    setup:
    boolean beenThere = false
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuInitPersonFromSsn = {String directory,String nin -> beenThere = true; return null }
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUserByNIN("1000000000", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
    beenThere == true
  }

  @Test
  def "Test enrollUserByNIN search with 12 - digit"() {
    setup:
    boolean beenThere1 = false
    boolean beenThere2 = false
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuInitPersonFromNin = {String directory,String nin -> beenThere1 = true; return null }
    SuPersonQuery.metaClass.static.getSuInitPersonFromSsn = {String directory,String nin -> beenThere2 = true; return null }
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUserByNIN("100000000000", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
    beenThere1 == true
    beenThere2 == true
  }

  @Test
  def "Test enrollUserByNIN scripts fail"() {
    setup:
    int p1=0
    int p2=0
    SuInitPerson suInitPerson = new SuInitPerson(uid: "testuid")
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuInitPersonFromSsn = {String directory,String nin -> return suInitPerson }
    SuPersonQuery.metaClass.static.saveSuInitPerson = {SuInitPerson sip -> return null}
    PasswordUtils.metaClass.static.genRandomPassword = {int a, int b -> p1 = a; p2 = b; return "hacker"}
    EnrollmentServiceUtils.metaClass.static.enableUser = {String uid, String password, SuInitPerson sip -> return false}
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUserByNIN("1000000000", new SvcAudit())

    then:
    thrown(Exception)
  }

  @Test
  def "Test enrollUserByNIN Happy Path"() {
    setup:
    int p1=0
    int p2=0
    SuInitPerson suInitPerson = new SuInitPerson(uid: "testuid")
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuInitPersonFromSsn = {String directory,String nin -> return suInitPerson }
    SuPersonQuery.metaClass.static.saveSuInitPerson = {SuInitPerson sip -> return null}
    PasswordUtils.metaClass.static.genRandomPassword = {int a, int b -> p1 = a; p2 = b; return "hacker"}
    EnrollmentServiceUtils.metaClass.static.enableUser = {String uid, String password, SuInitPerson sip -> return true}
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    SvcUidPwd ret = enrollmentServiceImpl.enrollUserByNIN("1000000000", new SvcAudit())

    then:
    ret.uid == "testuid"
    ret.password == "hacker"
    p1 == 10
    p2 == 10
  }

}
