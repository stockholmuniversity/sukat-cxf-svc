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
import se.su.it.svc.commons.LdapAttributeValidator
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.manager.ConfigManager
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.server.annotations.AuthzRole

import javax.jws.WebParam
import javax.jws.WebService

/**
 * Implementing class for EntitlementService CXF Web Service.
 * This Class handles all Entitlement admin activities in SUKAT.
 */
@WebService @Slf4j
@AuthzRole(role = "sukat-entitlement-admin")
public class EntitlementServiceImpl implements EntitlementService {

    /**
     * This method adds entitlement to the specified uid.
     *
     *
     * @param uid  uid of the user.
     * @param entitlement entitlement to add
     * @return void.
     * @see se.su.it.svc.ldap.SuPerson
     */
    @Requires({ uid && entitlement  &&
        !LdapAttributeValidator.validateAttributes([uid:uid, entitlement:entitlement ])})
    public void addEntitlement(
        @WebParam(name = "uid") String uid,
        @WebParam(name = "entitlement") String entitlement
    )
    {
        SuPerson person = SuPersonQuery.getSuPersonFromUID(ConfigManager.LDAP_RW, uid)

        // According to the LDAP schema eduPersonEntitlement is case-insesitive but it is not
        // certain that all systems that receive this through SAML feel the same. So to keep SUKAT
        // clean we compare case-sensetive and if there is a case-insesitive match LDAP will throw
        // and exception separating the different cases.
        if (person.eduPersonEntitlement.find { it.equals(entitlement) })
        {
           throw new IllegalArgumentException("Entitlement ${entitlement} already exist")
        }

        // Entitlements are used to flag manual students. Sometimes this is done before the account
        // is activated and therefor it might lack the eduPerson class.
        person.objectClass.add('eduPerson');

        person.eduPersonEntitlement.add(entitlement)
        SuPersonQuery.updateSuPerson(person)
    }

  /**
   * This method removes entitlement from the specified uid.
   *
   *
   * @param uid  uid of the user.
   * @param entitlement entitlement to remove
   * @return void.
   * @see se.su.it.svc.ldap.SuPerson
   */

  @Requires({ uid && entitlement  &&
      !LdapAttributeValidator.validateAttributes([uid:uid, entitlement:entitlement ])})
  public void removeEntitlement(
      @WebParam(name = "uid") String uid,
      @WebParam(name = "entitlement") String entitlement)
  {

    SuPerson person = SuPersonQuery.getSuPersonFromUID(ConfigManager.LDAP_RW, uid)

    /* Find the proper cased string of the entitlement on the person to delete. */
    String matchingEntitlement = person.eduPersonEntitlement.find { it.equalsIgnoreCase(entitlement) }

    if (matchingEntitlement) {
      person.eduPersonEntitlement.remove(matchingEntitlement)
      SuPersonQuery.updateSuPerson(person)
    } else {
      throw new IllegalArgumentException("entitlement $entitlement was not found for person with uid: $uid")
    }
  }
}
