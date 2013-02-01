import gldapo.GldapoSchemaRegistry
import org.junit.Test
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.RoleServiceImpl
import se.su.it.svc.query.SuRoleQuery
import org.springframework.ldap.core.DistinguishedName
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuRole

class RoleServiceImplTest extends spock.lang.Specification{
  @Test
  def "Test addUidToRoles with null uid argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()
    when:
    roleServiceImpl.addUidToRoles(null,["dummyDN"], new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test addUidToRoles with null roleDNList argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()
    when:
    roleServiceImpl.addUidToRoles("testuid",null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test addUidToRoles with empty roleDNList argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()
    when:
    roleServiceImpl.addUidToRoles("testuid",[], new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test addUidToRoles with null SvcAudit argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()
    when:
    roleServiceImpl.addUidToRoles("testuid",["dummyDN"], null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test addUidToRoles without person exist"() {
    setup:
    def myRoles = ["cn=Test1,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE",
      "cn=Test2,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE"]
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def roleServiceImpl = new RoleServiceImpl()
    when:
    roleServiceImpl.addUidToRoles("testuid", myRoles, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test addUidToRoles with person exist"() {
    setup:
    String saved = ""
    SuPerson person = new SuPerson(uid: "testuid")
    SuRole suRole = new SuRole()
    suRole.cn = "Test1"
    suRole.roleOccupant = ["uid=dummy,dc=it,dc=su,dc=se"]
    SuRole suRole2 = new SuRole()
    suRole2.cn = "Test2"
    suRole2.roleOccupant = ["uid=dummy,dc=it,dc=su,dc=se","uid=testuid, dc=it, dc=su, dc=se"]
    def myRoles = ["cn=Test1,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE",
      "cn=Test2,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE"]
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    person.metaClass.getDn = {new DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    SuRoleQuery.metaClass.static.getSuRoleFromDN = {String directory, String roleDN -> if(roleDN.startsWith("cn=Test1")) return suRole; if(roleDN.startsWith("cn=Test2")) return suRole2;}
    SuRoleQuery.metaClass.static.saveSuRole = {SuRole role -> saved = role.cn}
    def roleServiceImpl = new RoleServiceImpl()
    when:
    roleServiceImpl.addUidToRoles("testuid", myRoles, new SvcAudit())
    then:
    saved == "Test1"
    suRole.roleOccupant.size() == 2
    suRole2.roleOccupant.size() == 2
  }

  @Test
  def "Test removeUidFromRoles with null uid argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()
    when:
    roleServiceImpl.removeUidFromRoles(null,["dummyDN"], new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test removeUidFromRoles with null roleDNList argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()
    when:
    roleServiceImpl.removeUidFromRoles("testuid",null, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test removeUidFromRoles with empty roleDNList argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()
    when:
    roleServiceImpl.removeUidFromRoles("testuid",[], new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test removeUidFromRoles with null SvcAudit argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()
    when:
    roleServiceImpl.removeUidFromRoles("testuid",["dummyDN"], null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test removeUidFromRoles without person exist"() {
    setup:
    def myRoles = ["cn=Test1,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE",
      "cn=Test2,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE"]
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def roleServiceImpl = new RoleServiceImpl()
    when:
    roleServiceImpl.removeUidFromRoles("testuid", myRoles, new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test removeUidFromRoles with person exist"() {
    setup:
    String saved = ""
    SuPerson person = new SuPerson(uid: "testuid")
    SuRole suRole = new SuRole()
    suRole.cn = "Test1"
    suRole.roleOccupant = ["uid=dummy,dc=it,dc=su,dc=se"]
    SuRole suRole2 = new SuRole()
    suRole2.cn = "Test2"
    suRole2.roleOccupant = ["uid=dummy,dc=it,dc=su,dc=se","uid=testuid, dc=it, dc=su, dc=se"]
    def myRoles = ["cn=Test1,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE",
      "cn=Test2,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE"]
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    person.metaClass.getDn = {new DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    SuRoleQuery.metaClass.static.getSuRoleFromDN = {String directory, String roleDN -> if(roleDN.startsWith("cn=Test1")) return suRole; if(roleDN.startsWith("cn=Test2")) return suRole2;}
    SuRoleQuery.metaClass.static.saveSuRole = {SuRole role -> saved = role.cn}
    def roleServiceImpl = new RoleServiceImpl()
    when:
    roleServiceImpl.removeUidFromRoles("testuid", myRoles, new SvcAudit())
    then:
    saved == "Test2"
    suRole.roleOccupant.size() == 1
    suRole2.roleOccupant.size() == 1
  }
}
