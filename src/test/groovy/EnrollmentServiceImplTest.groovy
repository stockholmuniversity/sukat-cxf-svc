import org.junit.Test
import se.su.it.commons.ExecUtils
import se.su.it.commons.PasswordUtils
import se.su.it.svc.AccountServiceImpl
import se.su.it.svc.commons.SvcAudit
import gldapo.GldapoSchemaRegistry
import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.commons.SvcUidPwd
import se.su.it.svc.ldap.SuEnrollPerson
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
  def "Test enrollUser without null domain argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser(null,"test","testsson","other","100000000000", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser without null givenName argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se",null,"testsson","other","100000000000", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser without null sn argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test",null,"other","100000000000", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser without null eduPersonAffiliation argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson",null,"100000000000", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser without null nin argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other",null, new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser without null SvcAudit argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","100000000000", null)

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser with bad format nin argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","100000", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test enrollUser search with 10 - digit"() {
    setup:
    boolean beenThere = false
    SuEnrollPerson.metaClass.parent = "stuts"
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory, String uid -> return null}
    SuPersonQuery.metaClass.static.getSuEnrollPersonFromSsn = {String directory,String nin -> beenThere = true; return null }
    SuPersonQuery.metaClass.static.initSuEnrollPerson = {String directory, SuEnrollPerson person -> return person}
    SuPersonQuery.metaClass.static.saveSuEnrollPerson = {SuEnrollPerson person -> return null}
    EnrollmentServiceUtils.metaClass.static.enableUser = {String uid, String password, Object o -> return true}

    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","1000000000", new SvcAudit())

    then:
    beenThere == true
  }

  @Test
  def "Test enrollUser search with 12 - digit"() {
    setup:
    boolean beenThere1 = false
    boolean beenThere2 = false
    SuEnrollPerson.metaClass.parent = "stuts"
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory, String uid -> return null}
    SuPersonQuery.metaClass.static.initSuEnrollPerson = {String directory, SuEnrollPerson person -> return person}
    SuPersonQuery.metaClass.static.saveSuEnrollPerson = {SuEnrollPerson person -> return null}
    EnrollmentServiceUtils.metaClass.static.enableUser = {String uid, String password, Object o -> return true}
    SuPersonQuery.metaClass.static.getSuEnrollPersonFromNin = {String directory,String nin -> beenThere1 = true; return null }
    SuPersonQuery.metaClass.static.getSuEnrollPersonFromSsn = {String directory,String nin -> beenThere2 = true; return null }

    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","100000000000", new SvcAudit())

    then:
    beenThere1 == true
    beenThere2 == true
  }

  @Test
  def "Test enrollUser scripts fail"() {
    setup:
    SuEnrollPerson suEnrollPerson = new SuEnrollPerson(uid: "testuid")
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuEnrollPersonFromSsn = {String directory,String nin -> return suEnrollPerson }
    EnrollmentServiceUtils.metaClass.static.enableUser = {String uid, String password, Object o -> return false}
    SuEnrollPerson.metaClass.parent = "stuts"
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory, String uid -> return null}
    SuPersonQuery.metaClass.static.initSuEnrollPerson = {String directory, SuEnrollPerson person -> return person}
    SuPersonQuery.metaClass.static.saveSuEnrollPerson = {SuEnrollPerson person -> return null}


    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","1000000000", new SvcAudit())

    then:
    thrown(Exception)
  }

  @Test
  def "Test enrollUser Happy Path"() {
    setup:
    int p1=0
    int p2=0
    SuEnrollPerson suEnrollPerson = new SuEnrollPerson(uid: "testuid")
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuEnrollPerson.metaClass.parent = "stuts"
    SuPersonQuery.metaClass.static.getSuEnrollPersonFromSsn = {String directory,String nin -> return suEnrollPerson }
    SuPersonQuery.metaClass.static.saveSuEnrollPerson = {SuEnrollPerson sip -> return null}
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> new SuPerson(eduPersonPrimaryAffiliation: "kalle") }
    PasswordUtils.metaClass.static.genRandomPassword = {int a, int b -> p1 = a; p2 = b; return "hacker"}
    EnrollmentServiceUtils.metaClass.static.enableUser = {String uid, String password, Object o -> return true}

    def enrollmentServiceImpl = new EnrollmentServiceImpl()

    when:
    SvcUidPwd ret = enrollmentServiceImpl.enrollUser("student.su.se","test","testsson","other","1000000000", new SvcAudit())

    then:
    ret.uid == "testuid"
    ret.password == "hacker"
    p1 == 10
    p2 == 10
  }

}
