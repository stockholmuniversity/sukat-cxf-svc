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

import java.util.regex.Matcher

import groovy.util.logging.Slf4j
import org.apache.commons.lang.NotImplementedException
import org.gcontracts.annotations.Ensures
import org.gcontracts.annotations.Requires
import se.su.it.commons.Kadmin
import se.su.it.commons.PasswordUtils
import se.su.it.svc.commons.LdapAttributeValidator
import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.commons.SvcSubAccountVO
import se.su.it.svc.commons.SvcUidPwd
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuPersonStub
import se.su.it.svc.manager.ConfigManager
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.server.annotations.AuditHideReturnValue
import se.su.it.svc.server.annotations.AuthzRole
import se.su.it.svc.util.AccountServiceUtils
import se.su.it.svc.util.GeneralUtils

import javax.annotation.Resource
import javax.jws.WebParam
import javax.jws.WebService
import javax.xml.ws.WebServiceContext

/**
 * Implementing class for AccountService CXF Web Service.
 * This Class handles all Account activities in SUKAT.
 */

@WebService @Slf4j
@AuthzRole(role = "sukat-account-admin")
public class AccountServiceImpl implements AccountService
{

    @Resource
    public WebServiceContext context;

    def configManager

  /**
   * Create sub account for the given uid and type.
   *
   * @param uid uid of the user.
   * @param type Sub account type.
   */
  @Requires({
    type &&
    ! LdapAttributeValidator.validateAttributes([
        uid: uid
    ])
  })
  public void createSubAccount(
        @WebParam(name = 'uid') String uid,
        @WebParam(name = 'type') String type
    )
  {
        AccountServiceUtils.createSubAccount(uid, type)
  }

  /**
   * Delete sub account for the given uid and type.
   *
   * @param uid uid of the user.
   * @param type Sub account type.
   */
  @Requires({
    type &&
    ! LdapAttributeValidator.validateAttributes([
        uid: uid
    ])
  })
  public void deleteSubAccount(
        @WebParam(name = 'uid') String uid,
        @WebParam(name = 'type') String type
    )
  {
        AccountServiceUtils.deleteSubAccount(uid, type)
  }

  /**
   * Retrieve sub account for the given uid and type.
   *
   * @param uid uid of the user.
   * @param type Sub account type.
   *
   * @return A SvcSubAccountVO.
   */
  @Requires({
    type &&
    ! LdapAttributeValidator.validateAttributes([
        uid: uid
    ])
  })
  @Ensures({ result instanceof SvcSubAccountVO })
  public SvcSubAccountVO getSubAccount(
        @WebParam(name = 'uid') String uid,
        @WebParam(name = 'type') String type
    )
  {
        return AccountServiceUtils.getSubAccount(uid, type)
  }

  /**
   * This method sets the primary affiliation for the specified uid.
   *
   * @param uid  uid of the user.
   * @param affiliation the affiliation for this uid
   * @throws IllegalArgumentException if the uid can't be found
   * @see se.su.it.svc.ldap.SuPerson
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            eduPersonPrimaryAffiliation: affiliation ])
  })
  public void updatePrimaryAffiliation(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'affiliation') String affiliation
  ) {
    SuPerson person = SuPersonQuery.getSuPersonFromUID(ConfigManager.LDAP_RW, uid)

    if(!person) {
      throw new IllegalArgumentException("updatePrimaryAffiliation no such uid found: "+uid)
    }

    person.eduPersonPrimaryAffiliation = affiliation

    log.debug("updatePrimaryAffiliation - Replacing affiliation=<${person?.eduPersonPrimaryAffiliation}> with affiliation=<${affiliation}> for uid=<${uid}>")
    SuPersonQuery.updateSuPerson(person)
    log.info("updatePrimaryAffiliation - Updated affiliation for uid=<${uid}> with affiliation=<${person.eduPersonPrimaryAffiliation}>")
  }

  /**
   * Get password for the specified uid.
   * @param uid uid of the user.
   *
   * @return String Password.
   */
  @Requires({
    uid
  })
  @Ensures({ result && result instanceof String })
  @AuditHideReturnValue
  public String getPassword(
          @WebParam(name = 'uid') String uid
  )
  {
        def res = GeneralUtils.execHelper("getPassword", uid)

        return res.password
  }

  /**
   * This method resets the password for the specified uid and returns the clear text password.
   *
   * @param uid  uid of the user.
   * @return String new password.
   * @throws IllegalArgumentException if the uid can't be found
   */
  @Requires({
    uid
  })
  @Ensures({ result && result instanceof String && result.size() == 10 })
  @AuditHideReturnValue
  public String resetPassword(
          @WebParam(name = 'uid') String uid
  ) {
    String trueUid = GeneralUtils.uidToKrb5Principal(uid)

    def kadmin = Kadmin.newInstance()

    if (kadmin.principalExists(trueUid)) {
      log.debug("resetPassword - Trying to reset password for uid=<${uid}>")
      String pwd = PasswordUtils.genRandomPassword(10, 10)
      kadmin.setPassword(trueUid, pwd)
      log.info("resetPassword - Password was reset for uid=<${uid}>")
      return pwd
    } else {
      log.warn("resetPassword - No such uid found: "+uid)
      throw new IllegalArgumentException("resetPassword - No such uid found: "+uid)
    }
  }

  /**
   * This method resets the password for the specified uid without returning the result.
   *
   * @param uid  uid of the user.
   * @throws IllegalArgumentException if the uid can't be found
   */
  @Requires({
    uid
  })
  public void scramblePassword(
          @WebParam(name = 'uid') String uid
  ) {
    try {
      resetPassword(uid)
    } catch (ex) {
      log.warn "scramblePassword - Exception while scrambling password for uid '${uid}': " + ex.message
      throw ex
    }
  }

  /**
   * This method updates the attributes for the specified uid.
   *
   * @param uid  uid of the user.
   * @param person pre-populated SvcSuPersonVO object, the attributes that differ in this object to the original will be updated in ldap.
   * @throws IllegalArgumentException if the uid can't be found
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.commons.SvcSuPersonVO
   */
  @Requires({
    !LdapAttributeValidator.validateAttributes([
            uid: uid,
            svcsuperson: person ])
  })
  public void updateSuPerson(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'person') SvcSuPersonVO person
  ){
    SuPerson originalPerson = SuPersonQuery.getSuPersonFromUID(ConfigManager.LDAP_RW, uid)

    originalPerson.updateFromSvcSuPersonVO(person)
    log.debug("updateSuPerson - Trying to update SuPerson uid<${originalPerson.uid}>")

    SuPersonQuery.updateSuPerson(originalPerson)
    log.info("updateSuPerson - Updated SuPerson uid<${originalPerson.uid}>")
  }

  /**
   * This method creates a SuPerson in sukat.
   *
   * @param uid of the SuPerson to be created.
   * @param ssn 6-10 digit social security number for the SuPerson.
   * @param givenName given name for the SuPerson.
   * @param sn surname of the SuPerson.
   * @throws IllegalArgumentException if a user with the supplied uid already exists
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.ldap.SuPersonStub
   * @see se.su.it.svc.commons.SvcSuPersonVO
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            ssn: ssn,
            givenName: givenName,
            sn: sn ])
  })
  public void createSuPerson(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'ssn') String ssn,
          @WebParam(name = 'givenName') String givenName,
          @WebParam(name = 'sn') String sn
  ) {

    if (SuPersonQuery.findSuPersonByUID(ConfigManager.LDAP_RW, uid)) {
      throw new IllegalArgumentException("createSuPerson - A user with uid <"+uid+"> already exists")
    }

    if (SuPersonQuery.getSuPersonFromSsn(ConfigManager.LDAP_RW, ssn)) {
      throw new IllegalArgumentException("createSuPerson - A user with socialSecurityNumber <"+ssn+"> already exists")
    }

    String parent = configManager.config.ldap.accounts.parent
    log.info "createSuPerson: parent is configured to be $parent"

    String directory = ConfigManager.LDAP_RW

    SuPersonStub suPersonStub = SuPersonStub.newInstance(uid, givenName, sn, ssn, parent, directory)

    log.debug("createSuPerson - Creating initial sukat record from function arguments for uid<${uid}>")
    suPersonStub.save()
  }

  /**
   * This method enrolls a user in sukat, kerberos and afs.
   *
   * @param uid                         uid of the user to activate
   * @param domain                      domain of user in sukat. This is used to set the DN if user will be created.
   * @param eduPersonPrimaryAffiliation the primary affiliation to set.
   * @return SvcUidPwd                  object with the uid and password.
   * @throws IllegalArgumentException if a user with the supplied uid can't be found
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            domain: domain,
            affiliation: affiliations ])
  })
  @Ensures({ result && result.uid && result.password && result.password.size() == 10 })
  @AuditHideReturnValue
  public SvcUidPwd activateSuPerson(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'domain') String domain,
          @WebParam(name = 'affiliations') String[] affiliations
  ) {

    SuPerson suPerson = SuPersonQuery.getSuPersonFromUID(ConfigManager.LDAP_RW, uid)

    SvcUidPwd svcUidPwd = new SvcUidPwd(uid: uid)
    svcUidPwd.password = PasswordUtils.genRandomPassword(10, 10)

    suPerson.activate(svcUidPwd, affiliations, domain)

    return svcUidPwd
  }

  /**
   * This method terminates the account for the specified uid in sukat.
   *
   * @param uid  uid of the user.
   * @throws NotImplementedException
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid ])
  })
  public void terminateSuPerson(
          @WebParam(name = 'uid') String uid) {
    throw new NotImplementedException()
  }

  /**
   * This method gets the mailroutingaddress for the specified uid in sukat.
   *
   * @param uid  uid of the user.
   * @return the users mailRoutingAddress
   * @throws IllegalArgumentException if the uid can't be found
   */
  @AuthzRole(role = "sukat-account-admin")
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid ])
  })
  @Ensures({ result == null || result instanceof String})
  public String getMailRoutingAddress(
          @WebParam(name = 'uid') String uid
  ) {

    SuPerson suPerson = SuPersonQuery.getSuPersonFromUID(ConfigManager.LDAP_RW, uid)

    return suPerson.mailRoutingAddress
  }

  /**
   * This method sets the mailroutingaddress for the specified uid in sukat.
   *
   * @param uid  uid of the user. 10 chars (YYMMDDXXXX)
   * @param mail mailaddress to be set for uid.
   * @throws IllegalArgumentException if the uid can't be found
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            mailroutingaddress: mailRoutingAddress ])
  })
  public void setMailRoutingAddress(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'mailRoutingAddress') String mailRoutingAddress
  ) {
    SuPerson suPerson = SuPersonQuery.getSuPersonFromUID(ConfigManager.LDAP_RW, uid)

    suPerson.mailRoutingAddress = mailRoutingAddress
    SuPersonQuery.updateSuPerson(suPerson)
    log.debug("setMailRoutingAddress - Changed mailroutingaddress to <${mailRoutingAddress}> for uid <${uid}>")
  }

  /**
   * Finds all SuPersons in ldap based on socialSecurityNumber
   *
   * @param ssn in 10 numbers (YYMMDDXXXX)
   * @return an array of SvcSuPersonVO, one for each account found or an empty list
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            ssn: ssn ])
  })
  @Ensures({ result != null && result instanceof SvcSuPersonVO[] })
  public SvcSuPersonVO[] findAllSuPersonsBySocialSecurityNumber(
          @WebParam(name = "socialSecurityNumber") String ssn
  ) {
    SuPerson[] suPersons = SuPersonQuery.getSuPersonFromSsn(ConfigManager.LDAP_RW, ssn)

    return suPersons ? suPersons*.createSvcSuPersonVO() : []
  }

  /**
   * Finds a SuPerson in ldap based on uid
   *
   * @param uid without (@domain)
   * @return SvcSuPersonVO instance if found otherwise returns null.
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid ])
  })
  @Ensures({ result == null || result instanceof SvcSuPersonVO })
  public SvcSuPersonVO findSuPersonByUid(
          @WebParam(name = 'uid') String uid
  ) {
    SuPerson suPerson = SuPersonQuery.findSuPersonByUID(ConfigManager.LDAP_RW, uid)

    return suPerson ? suPerson?.createSvcSuPersonVO() : null
  }

  /**
   * Accepts an array of mailLocalAddresses, the new entries gets compared with the SuPersons current
   * mailLocalAddress entries and new entries gets added to the suPerson entry.
   *
   * @param uid
   * @param mailLocalAddresses
   * @return new list of mailLocalAddresses bound to the suPerson after the update.
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
        uid: uid,
        mailLocalAddresses: mailLocalAddresses ]) &&
    mailLocalAddresses?.size() > 0
  })
  public String[] addMailLocalAddresses(@WebParam(name = "uid") String uid,
                                        @WebParam(name = "mailLocalAddresses") String[] mailLocalAddresses)
  {

    SuPerson suPerson = SuPersonQuery.getSuPersonFromUID(ConfigManager.LDAP_RW, uid)
    // Can't depend on the mailLocalAddress Set doing it's thing without removing case.

    String[] mailLocalAddress = suPerson.addMailLocalAddress(mailLocalAddresses as Set<String>)

    suPerson.update()

    return mailLocalAddress
  }
}
