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

import org.gcontracts.annotations.Ensures
import org.gcontracts.annotations.Requires
import se.su.it.commons.Kadmin
import se.su.it.commons.PasswordUtils
import se.su.it.svc.commons.LdapAttributeValidator
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.commons.SvcUidPwd
import se.su.it.svc.ldap.SuEnrollPerson
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.util.EnrollmentServiceUtils

import javax.jws.WebService

/**
 * Implementing class for EnrollmentService CXF Web Service.
 * This Class handles all enrollment activities in SUKAT/KDC.
 */

@WebService
class EnrollmentServiceImpl implements EnrollmentService {

  /**
   * This method creates a new password and retire it for the specified uid.
   *
   *
   * @param uid  uid of the user.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return String with temporary password.
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Requires({ uid && audit })
  @Ensures({ result })
  public String resetAndExpirePwd(String uid, SvcAudit audit) {
    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)

    if(person) {
      String pwd = Kadmin.newInstance().resetOrCreatePrincipal(uid.replaceFirst("\\.", "/"))
      Kadmin.newInstance().setPasswordExpiry(uid.replaceFirst("\\.", "/"), new Date())
      return pwd
    } else {
      throw new IllegalArgumentException("resetAndExpirePwd - no such uid found: "+uid)
    }
  }

  /**
   * This method enrolls a user in sukat, kerberos and afs. If not found in sukat user will be created.
   *
   *
   * @param domain              domain of user in sukat. This is used to set the DN if user will be created.
   * @param givenName           givenName of the user. This is used to set the givenName (förnamn) if user will be created.
   * @param sn                  sn of the user. This is used to set the sn (efternamn) if user will be created.
   * @param sn                  affiliation of the user. This is used to set the affiliation if user will be created.
   * @param nin                 nin of the person. This can be a 10 or 12 digit social security number.
   * @param mailRoutingAddress  The mail routing address of the user.
   * @param audit               Audit object initilized with audit data about the client and user.
   * @return SvcUidPwd          object with the uid and password.
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            ssnornin: nin,
            domain: domain,
            givenName: givenName,
            sn: sn,
            eduPersonPrimaryAffiliation: eduPersonPrimaryAffiliation,
            audit: audit])
  })
  @Ensures({ result && result.uid && result.password && result.password.size() == 10 })
  public SvcUidPwd enrollUserWithMailRoutingAddress(
      String domain,
      String givenName,
      String sn,
      String eduPersonPrimaryAffiliation,
      String nin,
      String mailRoutingAddress,
      SvcAudit audit) {

    /** Config value set in config.properties to allow for mocking out user creation */

    SvcUidPwd svcUidPwd = new SvcUidPwd()
    svcUidPwd.password = PasswordUtils.genRandomPassword(10, 10)

    SuEnrollPerson suEnrollPerson = EnrollmentServiceUtils.findEnrollPerson(nin)

    if (suEnrollPerson) {
      EnrollmentServiceUtils.handleExistingUser(nin, suEnrollPerson, svcUidPwd, eduPersonPrimaryAffiliation, domain, mailRoutingAddress)
    } else {
      /** User not found in SUKAT, create user now */
      EnrollmentServiceUtils.handleNewUser(nin, givenName, sn, svcUidPwd, eduPersonPrimaryAffiliation, domain, mailRoutingAddress)
    }

    return svcUidPwd
  }

  /**
   * This method enrolls a user in sukat, kerberos and afs. If not found in sukat user will be created.
   *
   *
   * @param domain      domain of user in sukat. This is used to set the DN if user will be created.
   * @param givenName   givenName of the user. This is used to set the givenName (förnamn) if user will be created.
   * @param sn          sn of the user. This is used to set the sn (efternamn) if user will be created.
   * @param sn          affiliation of the user. This is used to set the affiliation if user will be created.
   * @param nin         nin of the person. This can be a 10 or 12 digit social security number.
   * @param audit       Audit object initilized with audit data about the client and user.
   * @return SvcUidPwd  object with the uid and password.
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            ssnornin: nin,
            domain: domain,
            givenName: givenName,
            sn: sn,
            eduPersonPrimaryAffiliation: eduPersonPrimaryAffiliation,
            audit: audit])
  })
  @Ensures({ result && result.uid && result.password && result.password.size() == 10 })
  public SvcUidPwd enrollUser(
          String domain,
          String givenName,
          String sn,
          String eduPersonPrimaryAffiliation,
          String nin,
          SvcAudit audit) {

    /** Config value set in config.properties to allow for mocking out user creation */

    SvcUidPwd svcUidPwd = new SvcUidPwd()
    svcUidPwd.password = PasswordUtils.genRandomPassword(10, 10)

    SuEnrollPerson suEnrollPerson = EnrollmentServiceUtils.findEnrollPerson(nin)

    if (suEnrollPerson) {
      EnrollmentServiceUtils.handleExistingUser(nin, suEnrollPerson, svcUidPwd, eduPersonPrimaryAffiliation, domain, null)
    } else {
      /** User not found in SUKAT, create user now */
      EnrollmentServiceUtils.handleNewUser(nin, givenName, sn, svcUidPwd, eduPersonPrimaryAffiliation, domain, null)
    }

    return svcUidPwd
  }
}
