/*
 * Copyright (c) 2013, IT Services, Stockholm University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Stockholm University nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

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
    if(roleDNList.size() <= 0)
      throw new java.lang.IllegalArgumentException("addUidToRoles - roleDNList cant be empty")

    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RO, uid)
    if(person) {
      roleDNList.each {roleDN ->
        logger.debug("addUidToRoles - Trying to find role for DN<${roleDN}>")
        SuRole role = SuRoleQuery.getSuRoleFromDN(GldapoManager.LDAP_RW, roleDN)
        if (role != null) {
          logger.debug("addUidToRoles - Role <${role.cn}> found for DN<${roleDN}>")
          DistinguishedName uidDN = new DistinguishedName(person.getDn())
          def roList = role.roleOccupant.collect { ro -> new DistinguishedName(ro) }
          if (!roList.find {roListItem -> if(roListItem.compareTo(uidDN) == 0) return true; return false}) {
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
    if(roleDNList.size() <= 0)
      throw new java.lang.IllegalArgumentException("addUidToRoles - roleDNList cant be empty")

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
          if (roList.find {roListItem -> if(roListItem.compareTo(uidDN) == 0) return true; return false}) {
            roList.remove(roList.find {roListItem -> if(roListItem.compareTo(uidDN) == 0) return true; return false})
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
