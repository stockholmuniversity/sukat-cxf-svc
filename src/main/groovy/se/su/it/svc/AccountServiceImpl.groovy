package se.su.it.svc

import se.su.it.svc.commons.LdapAttributeValidator
import se.su.it.svc.manager.EhCacheManager
import se.su.it.svc.util.EnrollmentServiceUtils

import javax.jws.WebService
import org.apache.log4j.Logger
import se.su.it.svc.commons.SvcAudit
import javax.jws.WebParam
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.ldap.SuPerson
import se.su.it.commons.Kadmin
import se.su.it.commons.PasswordUtils
import se.su.it.svc.audit.AuditAspectMethodDetails
import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.ldap.SuInitPerson
import se.su.it.svc.util.AccountServiceUtils
import java.util.regex.Pattern
import java.util.regex.Matcher
import se.su.it.commons.ExecUtils
import org.apache.commons.lang.NotImplementedException

/**
 * Implementing class for AccountService CXF Web Service.
 * This Class handles all Account activities in SUKAT.
 */

@WebService
public class AccountServiceImpl implements AccountService{
  private static final Logger logger = Logger.getLogger(AccountServiceImpl.class)
  /**
   * This method sets the primary affiliation for the specified uid.
   *
   *
   * @param uid  uid of the user.
   * @param affiliation the affiliation for this uid
   * @param audit Audit object initilized with audit data about the client and user.
   * @return array of SuService.
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.commons.SvcAudit
   */
  public void updatePrimaryAffiliation(@WebParam(name = "uid") String uid, @WebParam(name = "affiliation") String affiliation, @WebParam(name = "audit") SvcAudit audit) {
    //if(uid == null || affiliation == null || audit == null)
    //  throw new java.lang.IllegalArgumentException("updatePrimaryAffiliation - Null argument values not allowed in this function")
    /*try {
      LdapAttributeValidator.validateAttributes(["uid":uid,"eduPersonPrimaryAffiliation":affiliation,"audit":audit])
    } catch (Exception ex) {
      throw new java.lang.IllegalArgumentException("updatePrimaryAffiliation - " + ex.message)
    } */
    String attributeError = LdapAttributeValidator.validateAttributes(["uid":uid,"eduPersonPrimaryAffiliation":affiliation,"audit":audit])
    if (attributeError)
      throw new java.lang.IllegalArgumentException("updatePrimaryAffiliation - ${attributeError}")

    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)
    if(person) {
      logger.debug("updatePrimaryAffiliation - Replacing affiliation=<${person?.eduPersonPrimaryAffiliation}> with affiliation=<${affiliation}> for uid=<${uid}>")
      person.eduPersonPrimaryAffiliation = affiliation
      SuPersonQuery.saveSuPerson(person)
      logger.info("updatePrimaryAffiliation - Updated affiliation for uid=<${uid}> with affiliation=<${person.eduPersonPrimaryAffiliation}>")
    } else {
      throw new IllegalArgumentException("updatePrimaryAffiliation no such uid found: "+uid)
    }
  }

  /**
   * This method resets the password for the specified uid and returns the clear text password.
   *
   *
   * @param uid  uid of the user.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return String new password.
   * @see se.su.it.svc.commons.SvcAudit
   */
  public String resetPassword(@WebParam(name = "uid") String uid, @WebParam(name = "audit") SvcAudit audit) {
    if (uid == null || audit == null)
      throw new java.lang.IllegalArgumentException("resetPassword - Null argument values not allowed in this function")
    String trueUid = uid.replaceFirst("\\.", "/")
    if (Kadmin.newInstance().principalExists(trueUid)) {
      logger.debug("resetPassword - Trying to reset password for uid=<${uid}>")
      String pwd = PasswordUtils.genRandomPassword(10, 10)
      Kadmin.newInstance().setPassword(trueUid, pwd)
      logger.info("resetPassword - Password was reset for uid=<${uid}>")
      return pwd
    } else {
      logger.debug("resetPassword - No such uid found: "+uid)
      throw new java.lang.IllegalArgumentException("resetPassword - No such uid found: "+uid)
    }
    return null
  }

  /**
   * This method updates the attributes for the specified uid.
   *
   * @param uid  uid of the user.
   * @param person pre-populated SvcSuPersonVO object, the attributes that differ in this object to the original will be updated in ldap.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return void.
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.commons.SvcSuPersonVO
   * @see se.su.it.svc.commons.SvcAudit
   */
  public void updateSuPerson(@WebParam(name = "uid") String uid, @WebParam(name = "person") SvcSuPersonVO person, @WebParam(name = "audit") SvcAudit audit){
    String attributeError = LdapAttributeValidator.validateAttributes(["uid":uid,"svcsuperson":person,"audit":audit])
    if (attributeError)
      throw new java.lang.IllegalArgumentException("updateSuPerson - ${attributeError}")

    SuPerson originalPerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)
    if(originalPerson) {
      originalPerson.applySuPersonDifference(person)
      logger.debug("updateSuPerson - Trying to update SuPerson uid<${originalPerson.uid}>")
      SuPersonQuery.saveSuPerson(originalPerson)
      logger.info("updateSuPerson - Updated SuPerson uid<${originalPerson.uid}>")
    } else {
      throw new IllegalArgumentException("updateSuPerson - No such uid found: "+uid)
    }
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
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.commons.SvcSuPersonVO
   * @see se.su.it.svc.commons.SvcAudit
   */
  public String createSuPerson(@WebParam(name = "uid") String uid, @WebParam(name = "domain") String domain, @WebParam(name = "nin") String nin, @WebParam(name = "givenName") String givenName, @WebParam(name = "sn") String sn, @WebParam(name = "person") SvcSuPersonVO person, @WebParam(name = "fullAccount") boolean fullAccount, @WebParam(name = "audit") SvcAudit audit) {
    String attributeError = LdapAttributeValidator.validateAttributes(["uid":uid,"domain":domain,"nin":nin,"givenName":givenName,"sn":sn,"svcsuperson":person,"audit":audit])
    if (attributeError)
      throw new java.lang.IllegalArgumentException("createSuPerson - ${attributeError}")
    if(SuPersonQuery.getSuPersonFromUIDNoCache(GldapoManager.LDAP_RW, uid))
      throw new java.lang.IllegalArgumentException("createSuPerson - A user with uid <"+uid+"> already exists")

    //Begin init entry in sukat
    logger.debug("createSuPerson - Creating initial sukat record from function arguments for uid<${uid}>")
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
    logger.debug("createSuPerson - Writing initial sukat record to sukat for uid<${uid}>")
    SuPersonQuery.initSuPerson(GldapoManager.LDAP_RW, suInitPerson)
    //End init entry in sukat

    //Begin call Perlscript to init user in kdc, afs and unixshell
    //Maybe we want to replace this with a call to the message bus in the future
    String password = PasswordUtils.genRandomPassword(10, 10)
    if(fullAccount != null && fullAccount == true) {
      EnrollmentServiceUtils.enableUser(uid, password, suInitPerson)
    } else {
      logger.warn("createSuPerson - FullAccount attribute not set. PosixAccount entries will not be set and no AFS or KDC entries will be generated.")
      logger.warn("createSuPerson - Password returned will be fake/dummy")
    }
    //End call Perlscript to init user in kdc, afs and unixshell

    logger.debug("createSuPerson - Updating standard attributes according to function argument object for uid<${uid}>")
    updateSuPerson(uid,person,audit)
    logger.info("createSuPerson - Uid<${uid}> created")
    logger.debug("createSuPerson - Returning password for uid<${uid}>")
    return password
  }

  /**
   * This method terminates the account for the specified uid in sukat.
   *
   * @param uid  uid of the user.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return void.
   * @see se.su.it.svc.commons.SvcAudit
   */
  public void terminateSuPerson(@WebParam(name = "uid") String uid, @WebParam(name = "audit") SvcAudit audit) {
    String attributeError = LdapAttributeValidator.validateAttributes(["uid":uid,"audit":audit])
    if (attributeError)
      throw new java.lang.IllegalArgumentException("terminateSuPerson - ${attributeError}")

    SuPerson terminatePerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)
    if(terminatePerson) {
      //TODO: This serves as a stubfunction right now. We need input on how a terminate should work
      //TODO: Below is some code that might do the trick, but what about mail address and stuff
      //TODO: For now we cast exception to notify clients.
      throw new NotImplementedException("terminateSuPerson - This function is not yet implemented!")
      //terminatePerson.eduPersonAffiliation ["other"]
      //terminatePerson.eduPersonPrimaryAffiliation = "other"
      //logger.debug("terminateSuPerson - Trying to terminate SuPerson uid<${terminatePerson.uid}>")
      //SuPersonQuery.saveSuPerson(terminatePerson)
      //Kadmin kadmin = Kadmin.newInstance()
      //kadmin.resetOrCreatePrincipal(uid);
      //logger.info("terminateSuPerson - Terminated SuPerson uid<${terminatePerson.uid}>")
    } else {
      throw new IllegalArgumentException("terminateSuPerson - No such uid found: "+uid)
    }
  }

  /**
   * This method gets the mailroutingaddress for the specified uid in sukat.
   *
   * @param uid  uid of the user.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return void.
   * @see se.su.it.svc.commons.SvcAudit
   */
  public String getMailRoutingAddress(@WebParam(name = "uid") String uid, @WebParam(name = "audit") SvcAudit audit) {
    String attributeError = LdapAttributeValidator.validateAttributes(["uid":uid,"audit":audit])
    if (attributeError)
      throw new java.lang.IllegalArgumentException("getMailRoutingAddress - ${attributeError}")

    SuPerson suPerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)
    if(suPerson) {
      return suPerson.mailRoutingAddress
    } else {
      throw new IllegalArgumentException("getMailRoutingAddress - No such uid found: "+uid)
    }
    return null
  }

  /**
   * This method sets the mailroutingaddress for the specified uid in sukat.
   *
   * @param uid  uid of the user.
   * @param mail mailaddress to be set for uid.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return void.
   * @see se.su.it.svc.commons.SvcAudit
   */
  public void setMailRoutingAddress(@WebParam(name = "uid") String uid, @WebParam(name = "mailRoutingAddress") String mailRoutingAddress, @WebParam(name = "audit") SvcAudit audit) {
    String attributeError = LdapAttributeValidator.validateAttributes(["uid":uid,
                                                                       "mailroutingaddress":mailRoutingAddress,
                                                                       "audit":audit])
    if (attributeError)
      throw new java.lang.IllegalArgumentException("setMailRoutingAddress - ${attributeError}")

    SuPerson suPerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)
    if(suPerson) {
      suPerson.mailRoutingAddress = mailRoutingAddress
      SuPersonQuery.saveSuPerson(suPerson)
      logger.debug("setMailRoutingAddress - Changed mailroutingaddress to <${mailRoutingAddress}> for uid <${uid}>")
    } else {
      throw new IllegalArgumentException("setMailRoutingAddress - No such uid found: "+uid)
    }
  }

  /**
   * Finds a SuPerson in ldap based on norEduPersonNin
   * @param nin in 12 numbers (ex. 198101010101)
   * @param audit Audit object initilized with audit data about the client and user.
   * @return SvcSuPersonVO instance if found.
   */

  public SvcSuPersonVO findSuPersonByNorEduPersonNIN(
      @WebParam(name = "norEduPersonNIN") String nin, @WebParam(name = "audit") SvcAudit audit) {

    String attributeError = LdapAttributeValidator.validateAttributes(["ssnornin": nin, "audit": audit])
    if (attributeError)
      throw new IllegalArgumentException("ssnornin - ${attributeError}")

    SuPerson suPerson = SuPersonQuery.getSuPersonFromNin(GldapoManager.LDAP_RW, nin)

    if (!suPerson) {
      throw new Exception("ssnornin - No suPerson with the supplied ssnornin was found: " + nin)
    }

    return new SvcSuPersonVO(uid:suPerson.uid)
  }

  /**
   * Finds a SuPerson in ldap based on socialSecurityNumber
   * @param ssn in 10 numbers (ex. 7201010101)
   * @param audit Audit object initilized with audit data about the client and user.
   * @return SvcSuPersonVO instance if found.
   */

  public SvcSuPersonVO findSuPersonBySocialSecurityNumber(
      @WebParam(name = "socialSecurityNumber") String ssn, @WebParam(name = "audit") SvcAudit audit) {

    SvcSuPersonVO svcSuPersonVO = new SvcSuPersonVO()

    String attributeError = LdapAttributeValidator.validateAttributes(["ssnornin": ssn, "audit": audit])
    if (attributeError)
      throw new IllegalArgumentException("ssnornin - ${attributeError}")

    SuPerson suPerson = SuPersonQuery.getSuPersonFromSsn(GldapoManager.LDAP_RW, ssn)

    if (suPerson) {
      svcSuPersonVO.uid               = suPerson.uid
      svcSuPersonVO.givenName         = suPerson.givenName
      svcSuPersonVO.sn                = suPerson.sn
      svcSuPersonVO.displayName       = suPerson.displayName
      svcSuPersonVO.registeredAddress = suPerson.registeredAddress
      svcSuPersonVO.mail = new LinkedHashSet(suPerson.mail)

    }

    return svcSuPersonVO
  }

}
