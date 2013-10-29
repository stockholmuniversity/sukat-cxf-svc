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

import groovy.util.logging.Slf4j
import org.gcontracts.annotations.Requires
import org.springframework.ldap.core.DistinguishedName
import se.su.it.svc.commons.LdapAttributeValidator
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuRole
import se.su.it.svc.manager.ConfigManager
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.query.SuRoleQuery
import se.su.it.svc.server.annotations.AuthzRole

import javax.jws.WebParam
import javax.jws.WebService

/**
 * Implementing class for RoleService CXF Web Service.
 * This Class handles all Role activities in SUKAT.
 */

@WebService @Slf4j
@AuthzRole(role = "sukat-account-admin")
public class RoleServiceImpl implements RoleService {

  /**
   * This method adds the specified uid to the roles in the list.
   *
   *
   * @param uid uid of the user.
   * @param roleDNList List<String> of DN's for the roles
   * @return void.
   */

  @Requires({
    uid &&
        roleDNList?.size() > 0 &&
        !LdapAttributeValidator.validateAttributes([uid:uid ])
  })
  public void addUidToRoles(
      @WebParam(name = "uid") String uid,
      @WebParam(name = "roleDNList") List<String> roleDNList)
  {

    SuPerson person = SuPersonQuery.getSuPersonFromUID(ConfigManager.LDAP_RO, uid)

    DistinguishedName uidDN = new DistinguishedName(person.getDn())

    for (roleDN in roleDNList) {
      log.debug("addUidToRoles - Trying to find role for DN<${roleDN}>")
      SuRole role = SuRoleQuery.getSuRoleFromDN(ConfigManager.LDAP_RW, roleDN)

      if (!role) {
        log.warn("addUidToRoles - Could not add uid <${person.uid}> to role <${roleDN}>, role not found!")
        continue
      }

      log.debug("addUidToRoles - Role <${role.cn}> found for DN<${roleDN}>")

      List roList = role.roleOccupant?.collect { ro -> new DistinguishedName(ro) }

      if (!(uidDN in roList)) {
        role.roleOccupant.add(uidDN.toString())
        role.update()
        log.info("addUidToRoles - Uid<${person.uid}> added as occupant to role <${role.cn}> ")
      } else {
        log.debug("addUidToRoles - Occupant <${person.uid}> already exist for role <${role.cn}>")
      }
    }
  }

  /**
   * This method removes the specified uid from the roles in the list.
   *
   *
   * @param uid uid of the user.
   * @param roleDNList List<String> of DN's for the roles
   * @return void.
   */
  @Requires({
    uid && roleDNList?.size() > 0
  })
  public void removeUidFromRoles(
      @WebParam(name = "uid") String uid,
      @WebParam(name = "roleDNList") List<String> roleDNList)
  {

    SuPerson person = SuPersonQuery.getSuPersonFromUID(ConfigManager.LDAP_RO, uid)
    DistinguishedName uidDN = new DistinguishedName(person.getDn())

    for (tmpRoleDN in roleDNList) {
      DistinguishedName roleDN = new DistinguishedName(tmpRoleDN)

      log.debug("removeUidFromRoles - Trying to find role for DN<${roleDN.toString()}>")

      SuRole role = SuRoleQuery.getSuRoleFromDN(ConfigManager.LDAP_RW, roleDN.toString())

      if (!role) {
        log.warn("removeUidFromRoles - Could not remove uid <${person.uid}> from role <${roleDN}>, role not found!")
        continue
      }

      log.debug("removeUidFromRoles - Role <${role.cn}> found for DN<${roleDN.toString()}>")

      def roList = role.roleOccupant.collect { ro -> new DistinguishedName(ro) }

      if (uidDN in roList) {
        roList.remove(uidDN)
        role.roleOccupant = new LinkedList<String>(roList.collect { DistinguishedName dn -> dn.toString() })
        role.update()
        log.info("removeUidFromRoles - Uid<${person.uid}> removed as occupant from role <${role.cn}> ")
      } else {
        log.debug("removeUidFromRoles - Occupant <${person.uid}> not found for role <${role.cn}>")
      }
    }
  }
}
