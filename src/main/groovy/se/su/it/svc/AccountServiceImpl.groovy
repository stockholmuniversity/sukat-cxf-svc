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
import se.su.it.svc.ldap.SuInitPerson
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.util.AccountServiceUtils
import se.su.it.svc.util.EnrollmentServiceUtils

import javax.jws.WebParam
import javax.jws.WebService

/**
 * Implementing class for AccountService CXF Web Service.
 * This Class handles all Account activities in SUKAT.
 */
@Slf4j
@WebService
public class AccountServiceImpl implements AccountService {

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
  public void updatePrimaryAffiliation(String uid, String affiliation, SvcAudit audit) {
    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)

    if(!person) {
      throw new IllegalArgumentException("updatePrimaryAffiliation no such uid found: "+uid)
    }

    person.eduPersonPrimaryAffiliation = affiliation

    log.debug("updatePrimaryAffiliation - Replacing affiliation=<${person?.eduPersonPrimaryAffiliation}> with affiliation=<${affiliation}> for uid=<${uid}>")
    SuPersonQuery.saveSuPerson(person)
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
  public String resetPassword(String uid, SvcAudit audit) {
    String trueUid = uid.replaceFirst("\\.", "/")

    if (Kadmin.newInstance().principalExists(trueUid)) {
      log.debug("resetPassword - Trying to reset password for uid=<${uid}>")
      String pwd = PasswordUtils.genRandomPassword(10, 10)
      Kadmin.newInstance().setPassword(trueUid, pwd)
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
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            svcsuperson: person,
            audit: audit ])
  })
  public void updateSuPerson(String uid, SvcSuPersonVO person, SvcAudit audit){
    SuPerson originalPerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)

    if(!originalPerson) {
      throw new IllegalArgumentException("updateSuPerson - No such uid found: "+uid)
    }

    originalPerson.applySuPersonDifference(person)
    log.debug("updateSuPerson - Trying to update SuPerson uid<${originalPerson.uid}>")

    SuPersonQuery.saveSuPerson(originalPerson)
    log.info("updateSuPerson - Updated SuPerson uid<${originalPerson.uid}>")
  }

  /**
   * This method creates a SuPerson in sukat.
   *
   * @param uid of the SuPerson to be created.
   * @param domain domain for the SuPerson.
   * @param nin 12-digit social security number for the SuPerson.
   * @param givenName given name for the SuPerson.
   * @param sn surname of the SuPerson.
   * @param person pre-populated SvcSuPersonVO object. This will be used to populate standard attributes for the SuPerson.
   * @param  boolean fullAccount if true will try to create AFS and KDC entries else the posix part will be missing.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return String with newly created password for the SuPerson.
   * @throws IllegalArgumentException if a user with the supplied uid already exists
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.commons.SvcSuPersonVO
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            domain: domain,
            nin: nin,
            givenName: givenName,
            sn: sn,
            svcsuperson: person,
            audit: audit ])
  })
  @Ensures({ result && result instanceof String && result.size() == 10 })
  public String createSuPerson(String uid, String domain, String nin, String givenName, String sn, SvcSuPersonVO person, boolean fullAccount, SvcAudit audit) {
    if(SuPersonQuery.getSuPersonFromUIDNoCache(GldapoManager.LDAP_RW, uid))
      throw new IllegalArgumentException("createSuPerson - A user with uid <"+uid+"> already exists")

    //Begin init entry in sukat
    log.debug("createSuPerson - Creating initial sukat record from function arguments for uid<${uid}>")
    SuInitPerson suInitPerson = new SuInitPerson()
    suInitPerson.uid = uid
    suInitPerson.cn = givenName + " " + sn
    suInitPerson.sn = sn
    suInitPerson.givenName = givenName
    if(nin.length() == 12)
      suInitPerson.norEduPersonNIN = nin
    suInitPerson.eduPersonPrincipalName = uid + "@su.se"
    suInitPerson.objectClass = ["suPerson","sSNObject","norEduPerson","eduPerson","inetLocalMailRecipient","inetOrgPerson","organizationalPerson","person","top"]
    suInitPerson.parent = AccountServiceUtils.domainToDN(domain)
    log.debug("createSuPerson - Writing initial sukat record to sukat for uid<${uid}>")
    SuPersonQuery.initSuPerson(GldapoManager.LDAP_RW, suInitPerson)
    //End init entry in sukat

    //Begin call Perlscript to init user in kdc, afs and unixshell
    //Maybe we want to replace this with a call to the message bus in the future
    String password = PasswordUtils.genRandomPassword(10, 10)
    if (fullAccount) {
      EnrollmentServiceUtils.enableUser(uid, password, suInitPerson)
    } else {
      log.warn("createSuPerson - FullAccount attribute not set. PosixAccount entries will not be set and no AFS or KDC entries will be generated.")
      log.warn("createSuPerson - Password returned will be fake/dummy")
    }
    //End call Perlscript to init user in kdc, afs and unixshell

    log.debug("createSuPerson - Updating standard attributes according to function argument object for uid<${uid}>")
    updateSuPerson(uid,person,audit)
    log.info("createSuPerson - Uid<${uid}> created")
    log.debug("createSuPerson - Returning password for uid<${uid}>")
    return password
  }

  /**
   * This method terminates the account for the specified uid in sukat.
   *
   * @param uid  uid of the user.
   * @param audit Audit object initilized with audit data about the client and user.
   * @throws IllegalArgumentException if the uid can't be found
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            audit: audit ])
  })
  public void terminateSuPerson(String uid, SvcAudit audit) {
    SuPerson terminatePerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)

    if(terminatePerson) {
      //TODO: This serves as a stubfunction right now. We need input on how a terminate should work
      //TODO: Below is some code that might do the trick, but what about mail address and stuff
      //TODO: For now we cast exception to notify clients.
      throw new NotImplementedException("terminateSuPerson - This function is not yet implemented!")
      //TODO: terminatePerson.eduPersonAffiliation ["other"]
      //TODO: terminatePerson.eduPersonPrimaryAffiliation = "other"
      //TODO: log.debug("terminateSuPerson - Trying to terminate SuPerson uid<${terminatePerson.uid}>")
      //TODO: SuPersonQuery.saveSuPerson(terminatePerson)
      //TODO: Kadmin kadmin = Kadmin.newInstance()
      //TODO: kadmin.resetOrCreatePrincipal(uid);
      //TODO: log.info("terminateSuPerson - Terminated SuPerson uid<${terminatePerson.uid}>")
    } else {
      throw new IllegalArgumentException("terminateSuPerson - No such uid found: "+uid)
    }
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
  public String getMailRoutingAddress(String uid, SvcAudit audit) {
    SuPerson suPerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)

    if(!suPerson) {
      throw new IllegalArgumentException("getMailRoutingAddress - No such uid found: "+uid)
    }

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
  public void setMailRoutingAddress(String uid, String mailRoutingAddress, SvcAudit audit) {
    SuPerson suPerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)

    if(!suPerson) {
      throw new IllegalArgumentException("setMailRoutingAddress - No such uid found: "+uid)
    }

    suPerson.mailRoutingAddress = mailRoutingAddress
    SuPersonQuery.saveSuPerson(suPerson)
    log.debug("setMailRoutingAddress - Changed mailroutingaddress to <${mailRoutingAddress}> for uid <${uid}>")
  }

  /**
   * Finds a SuPerson in ldap based on norEduPersonNin
   *
   * @param nin in 12 numbers (ex. YYYYMMDDXXXX)
   * @param audit Audit object initilized with audit data about the client and user.
   * @return SvcSuPersonVO instance if found.
   * @throws IllegalArgumentException if the uid can't be found
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            nin: nin,
            audit: audit ])
  })
  @Ensures({ result && result instanceof SvcSuPersonVO })
  public SvcSuPersonVO findSuPersonByNorEduPersonNIN(@WebParam(name = "norEduPersonNIN") String nin, SvcAudit audit) {
    SuPerson suPerson = SuPersonQuery.getSuPersonFromNin(GldapoManager.LDAP_RW, nin)

    if (!suPerson) {
      throw new IllegalArgumentException("ssnornin - No suPerson with the supplied ssn or nin was found: " + nin)
    }

    return new SvcSuPersonVO(uid:suPerson.uid)
  }

  /**
   * Finds a SuPerson in ldap based on socialSecurityNumber
   *
   * @param ssn in 10 numbers (YYMMDDXXXX)
   * @param audit Audit object initilized with audit data about the client and user.
   * @return SvcSuPersonVO instance if found.
   * @throws IllegalArgumentException if no user can be found.
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            ssn: ssn,
            audit: audit ])
  })
  @Ensures({ result && result instanceof SvcSuPersonVO })
  public SvcSuPersonVO findSuPersonBySocialSecurityNumber(@WebParam(name = "socialSecurityNumber") String ssn, SvcAudit audit) {
    SuPerson suPerson = SuPersonQuery.getSuPersonFromSsn(GldapoManager.LDAP_RW, ssn)
    if (!suPerson) {
      throw new IllegalArgumentException("findSuPersonBySocialSecurityNumber - No suPerson with the supplied ssn: " + ssn)
    }

    SvcSuPersonVO svcSuPersonVO = new SvcSuPersonVO(
      uid:                  suPerson.uid,
      socialSecurityNumber: suPerson.socialSecurityNumber,
      givenName:            suPerson.givenName,
      sn:                   suPerson.sn,
      displayName:          suPerson.displayName,
      registeredAddress:    suPerson.registeredAddress,
      mail:                 new LinkedHashSet(suPerson.mail),

      /** The user has an account in SUKAT that is not a stub.*/
      accountIsActive:      (suPerson?.objectClass?.contains('posixAccount')) ?: false
    )
    return svcSuPersonVO
  }

  /**
   * Finds a SuPerson in ldap based on uid
   *
   * @param uid without (@domain)
   * @param audit Audit object initilized with audit data about the client and user.
   * @return SvcSuPersonVO instance if found.
   */
  @Requires({
    ! LdapAttributeValidator.validateAttributes([
            uid: uid,
            audit: audit ])
  })
  @Ensures({ result && result instanceof SvcSuPersonVO })
  public SvcSuPersonVO findSuPersonByUid(String uid, SvcAudit audit) {
    SuPerson suPerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)

    if (!suPerson) {
      throw new IllegalArgumentException("findSuPersonByUid - No suPerson with the supplied uid: " + uid)
    }

    SvcSuPersonVO svcSuPersonVO = new SvcSuPersonVO(
    uid:                   suPerson.uid,
    socialSecurityNumber:  suPerson.socialSecurityNumber,
    givenName:             suPerson.givenName,
    sn:                    suPerson.sn,
    displayName:           suPerson.displayName,
    registeredAddress:     suPerson.registeredAddress,
    mail:  new LinkedHashSet(suPerson.mail),

    /** The user has an account in SUKAT that is not a stub.*/
    accountIsActive:  (suPerson?.objectClass?.contains('posixAccount'))?:false
    )

    return svcSuPersonVO
  }
}
