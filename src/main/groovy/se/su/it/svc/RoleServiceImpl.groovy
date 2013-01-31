package se.su.it.svc

import org.apache.log4j.Logger
import se.su.it.svc.ldap.SuRole
import se.su.it.svc.query.SuRoleQuery
import org.springframework.ldap.core.DistinguishedName
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.query.SuPersonQuery
import javax.jws.WebParam
import javax.jws.WebService
import se.su.it.svc.query.SuRoleQuery

/**
 * Implementing class for RoleService CXF Web Service.
 * This Class handles all Role activities in SUKAT.
 */

@WebService
public class RoleServiceImpl implements RoleService {
  private static final Logger logger = Logger.getLogger(RoleServiceImpl.class)

  /**
   * This method adds the specified uid to the roles in the list.
   *
   *
   * @param uid uid of the user.
   * @param roleDNList List<String> of DN's for the roles
   * @param audit Audit object initilized with audit data about the client and user.
   * @return void.
   * @see se.su.it.svc.commons.SvcAudit
   */
  public void addUidToRoles(@WebParam(name = "uid") String uid, @WebParam(name = "roleDNList") List<String> roleDNList, @WebParam(name = "audit") SvcAudit audit) {
    if (uid == null || roleDNList == null || audit == null)
      throw new java.lang.IllegalArgumentException("addUidToRoles - Null argument values not allowed for uid, roleDNList or audit")

    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RO, uid)
    if(person) {
      roleDNList.each {roleDN ->
        logger.debug("addUidToRoles - Trying to find role for DN<${roleDN}>")
        SuRole role = SuRoleQuery.getSuRoleFromDN(GldapoManager.LDAP_RW, roleDN)
        if (role != null) {
          logger.debug("addUidToRoles - Role <${role.cn}> found for DN<${roleDN}>")
          DistinguishedName uidDN = new DistinguishedName(person.getDn())
          def roList = role.roleOccupant.collect { ro -> new DistinguishedName(ro) }
          if (!roList.contains(uidDN)) {
            role.roleOccupant.add(uidDN.toString())
            SuRoleQuery.saveSuRole(role)
            logger.info("addUidToRoles - Uid<${person.uid}> added as occupant to role <${role.cn}> ")
          } else {
            logger.debug("addUidToRoles - Occupant <${person.uid}> already exist for role <${role.cn}>")
          }
        } else {
          logger.warn("addUidToRoles - Could not add uid <${person.uid}> to role <${roleDN}>, role not found!")
        }
      }
    } else {
      throw new IllegalArgumentException("addUidToRoles - No such uid found: "+uid)
    }
  }

  /**
   * This method removes the specified uid from the roles in the list.
   *
   *
   * @param uid uid of the user.
   * @param roleDNList List<String> of DN's for the roles
   * @param audit Audit object initilized with audit data about the client and user.
   * @return void.
   * @see se.su.it.svc.commons.SvcAudit
   */
  public void removeUidFromRoles(@WebParam(name = "uid") String uid, @WebParam(name = "roleDNList") List<String> roleDNList, @WebParam(name = "audit") SvcAudit audit) {
    if (uid == null || roleDNList == null || audit == null)
      throw new java.lang.IllegalArgumentException("removeUidFromRoles - Null argument values not allowed for uid, roleDNList or audit")

    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RO, uid)
    if(person) {
      roleDNList.each {tmpRoleDN ->
         DistinguishedName roleDN = new DistinguishedName(tmpRoleDN)
        logger.debug("removeUidFromRoles - Trying to find role for DN<${roleDN.toString()}>")
        SuRole role = SuRoleQuery.getSuRoleFromDN(GldapoManager.LDAP_RW, roleDN.toString())
        if (role != null) {
          logger.debug("removeUidFromRoles - Role <${role.cn}> found for DN<${roleDN.toString()}>")
          DistinguishedName uidDN = new DistinguishedName(person.getDn())
          def roList = role.roleOccupant.collect { ro -> new DistinguishedName(ro) }
          if (roList.contains(uidDN)) {
            roList.remove(uidDN)
            role.roleOccupant = new LinkedList<String>(roList.collect {dn -> dn.toString()} )//      roList.toArray(new String[roList.size()]))
            SuRoleQuery.saveSuRole(role)
            logger.info("removeUidFromRoles - Uid<${person.uid}> removed as occupant from role <${role.cn}> ")
          } else {
            logger.debug("removeUidFromRoles - Occupant <${person.uid}> not found for role <${role.cn}>")
          }
        } else {
          logger.warn("removeUidFromRoles - Could not remove uid <${person.uid}> from role <${roleDN}>, role not found!")
        }
      }
    } else {
      throw new IllegalArgumentException("removeUidFromRoles - No such uid found: "+uid)
    }
  }
}
