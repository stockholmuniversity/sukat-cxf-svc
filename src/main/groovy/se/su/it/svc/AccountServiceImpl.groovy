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
import org.apache.commons.lang.NotImplementedException
import org.gcontracts.annotations.Ensures
import org.gcontracts.annotations.Requires
import se.su.it.commons.Kadmin
import se.su.it.commons.PasswordUtils
import se.su.it.svc.commons.LdapAttributeValidator
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.commons.SvcUidPwd
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuPersonStub
import se.su.it.svc.manager.Config
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.util.GeneralUtils

import javax.jws.WebParam
import javax.jws.WebService

/**
 * Implementing class for AccountService CXF Web Service.
 * This Class handles all Account activities in SUKAT.
 */

@WebService @Slf4j
public class AccountServiceImpl implements AccountService {

  def configHolder

  /**
   * This method sets the primary affiliation for the specified uid.
   *
   * @param uid  uid of the user.
   * @param affiliation the affiliation for this uid
   * @param audit Audit object initilized with audit data about the client and user.
   * @throws IllegalArgumentException if the uid can't be found
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            eduPersonPrimaryAffiliation: affiliation,
            audit: audit ])
  })
  public void updatePrimaryAffiliation(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'affiliation') String affiliation,
          @WebParam(name = 'audit') SvcAudit audit
  ) {
    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)

    if(!person) {
      throw new IllegalArgumentException("updatePrimaryAffiliation no such uid found: "+uid)
    }

    person.eduPersonPrimaryAffiliation = affiliation

    log.debug("updatePrimaryAffiliation - Replacing affiliation=<${person?.eduPersonPrimaryAffiliation}> with affiliation=<${affiliation}> for uid=<${uid}>")
    SuPersonQuery.updateSuPerson(person)
    log.info("updatePrimaryAffiliation - Updated affiliation for uid=<${uid}> with affiliation=<${person.eduPersonPrimaryAffiliation}>")
  }

  /**
   * This method resets the password for the specified uid and returns the clear text password.
   *
   * @param uid  uid of the user.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return String new password.
   * @throws IllegalArgumentException if the uid can't be found
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Requires({
    uid && audit
  })
  @Ensures({ result && result instanceof String && result.size() == 10 })
  public String resetPassword(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'audit') SvcAudit audit
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
      log.debug("resetPassword - No such uid found: "+uid)
      throw new IllegalArgumentException("resetPassword - No such uid found: "+uid)
    }
  }

  /**
   * This method updates the attributes for the specified uid.
   *
   * @param uid  uid of the user.
   * @param person pre-populated SvcSuPersonVO object, the attributes that differ in this object to the original will be updated in ldap.
   * @param audit Audit object initilized with audit data about the client and user.
   * @throws IllegalArgumentException if the uid can't be found
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.commons.SvcSuPersonVO
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Requires({
    !LdapAttributeValidator.validateAttributes([
            uid: uid,
            svcsuperson: person,
            audit: audit ])
  })
  public void updateSuPerson(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'person') SvcSuPersonVO person,
          @WebParam(name = 'audit') SvcAudit audit
  ){
    SuPerson originalPerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)

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
   * @param audit Audit object initilized with audit data about the client and user.
   * @throws IllegalArgumentException if a user with the supplied uid already exists
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.ldap.SuPersonStub
   * @see se.su.it.svc.commons.SvcSuPersonVO
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            ssn: ssn,
            givenName: givenName,
            sn: sn,
            audit: audit ])
  })
  public void createSuPerson(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'ssn') String ssn,
          @WebParam(name = 'givenName') String givenName,
          @WebParam(name = 'sn') String sn,
          @WebParam(name = 'audit') SvcAudit audit
  ) {

    if (SuPersonQuery.findSuPersonByUID(GldapoManager.LDAP_RW, uid)) {
      throw new IllegalArgumentException("createSuPerson - A user with uid <"+uid+"> already exists")
    }

    if (!configHolder.props.ldap.accounts.default.containsKey("parent")) {
      throw new IllegalArgumentException("Missing parent.")
    }

    String parent = configHolder.props.ldap.accounts.default.parent
    log.info "createSuPerson: parent is configured to be $parent"

    String directory = GldapoManager.LDAP_RW

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
   * @param audit                       Audit object initilized with audit data about the client and user.
   * @return SvcUidPwd                  object with the uid and password.
   * @throws IllegalArgumentException if a user with the supplied uid can't be found
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            domain: domain,
            affiliation: affiliations,
            audit: audit])
  })
  @Ensures({ result && result.uid && result.password && result.password.size() == 10 })
  public SvcUidPwd activateSuPerson(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'domain') String domain,
          @WebParam(name = 'affiliations') String[] affiliations,
          @WebParam(name = 'audit') SvcAudit audit
  ) {

    SuPerson suPerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)

    SvcUidPwd svcUidPwd = new SvcUidPwd(uid: uid)
    svcUidPwd.password = PasswordUtils.genRandomPassword(10, 10)

    suPerson.activate(svcUidPwd, affiliations, domain)

    return svcUidPwd
  }

  /**
   * This method terminates the account for the specified uid in sukat.
   *
   * @param uid  uid of the user.
   * @param audit Audit object initilized with audit data about the client and user.
   * @throws NotImplementedException
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            audit: audit ])
  })
  public void terminateSuPerson(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'audit') SvcAudit audit) {
    throw new NotImplementedException()
  }

  /**
   * This method gets the mailroutingaddress for the specified uid in sukat.
   *
   * @param uid  uid of the user.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return the users mailRoutingAddress
   * @throws IllegalArgumentException if the uid can't be found
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            audit: audit ])
  })
  @Ensures({ result == null || result instanceof String})
  public String getMailRoutingAddress(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'audit') SvcAudit audit
  ) {

    SuPerson suPerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)

    return suPerson.mailRoutingAddress
  }

  /**
   * This method sets the mailroutingaddress for the specified uid in sukat.
   *
   * @param uid  uid of the user. 10 chars (YYMMDDXXXX)
   * @param mail mailaddress to be set for uid.
   * @param audit Audit object initilized with audit data about the client and user.
   * @throws IllegalArgumentException if the uid can't be found
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            mailroutingaddress: mailRoutingAddress,
            audit: audit ])
  })
  public void setMailRoutingAddress(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'mailRoutingAddress') String mailRoutingAddress,
          @WebParam(name = 'audit') SvcAudit audit
  ) {
    SuPerson suPerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)

    suPerson.mailRoutingAddress = mailRoutingAddress
    SuPersonQuery.updateSuPerson(suPerson)
    log.debug("setMailRoutingAddress - Changed mailroutingaddress to <${mailRoutingAddress}> for uid <${uid}>")
  }

  /**
   * Finds all SuPersons in ldap based on socialSecurityNumber
   *
   * @param ssn in 10 numbers (YYMMDDXXXX)
   * @param audit Audit object initilized with audit data about the client and user.
   * @return an array of SvcSuPersonVO, one for each account found or an empty list
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            ssn: ssn,
            audit: audit ])
  })
  @Ensures({ result != null && result instanceof SvcSuPersonVO[] })
  public SvcSuPersonVO[] findAllSuPersonsBySocialSecurityNumber(
          @WebParam(name = "socialSecurityNumber") String ssn,
          @WebParam(name = "audit") SvcAudit audit
  ) {
    SuPerson[] suPersons = SuPersonQuery.getSuPersonFromSsn(GldapoManager.LDAP_RW, ssn)

    return suPersons ? suPersons*.createSvcSuPersonVO() : []
  }

  /**
   * Finds a SuPerson in ldap based on uid
   *
   * @param uid without (@domain)
   * @param audit Audit object initilized with audit data about the client and user.
   * @return SvcSuPersonVO instance if found otherwise returns null.
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            audit: audit ])
  })
  @Ensures({ result == null || result instanceof SvcSuPersonVO })
  public SvcSuPersonVO findSuPersonByUid(
          @WebParam(name = 'uid') String uid,
          @WebParam(name = 'audit') SvcAudit audit
  ) {
    SuPerson suPerson = SuPersonQuery.findSuPersonByUID(GldapoManager.LDAP_RW, uid)

    return suPerson ? suPerson?.createSvcSuPersonVO() : null
  }

  /**
   * Accepts an array of mailLocalAddresses, the new entries gets compared with the SuPersons current
   * mailLocalAddress entries and new entries gets added to the suPerson entry.
   *
   * @param uid
   * @param mailLocalAddresses
   * @param audit
   * @return new list of mailLocalAddresses bound to the suPerson after the update.
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
        uid: uid,
        mailLocalAddresses: mailLocalAddresses,
        audit: audit ]) &&
    mailLocalAddresses?.size() > 0
  })
  public String[] addMailLocalAddresses(@WebParam(name = "uid") String uid,
                                        @WebParam(name = "mailLocalAddresses") String[] mailLocalAddresses,
                                        @WebParam(name = "audit") SvcAudit audit) {

    SuPerson suPerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)
    // Can't depend on the mailLocalAddress Set doing it's thing without removing case.

    String[] mailLocalAddress = suPerson.addMailLocalAddress(mailLocalAddresses as Set<String>)

    suPerson.update()

    return mailLocalAddress
  }
}
