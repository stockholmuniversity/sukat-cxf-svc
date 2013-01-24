package se.su.it.svc

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
    if(uid == null || affiliation == null || audit == null)
      throw new java.lang.IllegalArgumentException("updatePrimaryAffiliation - Null argument values not allowed in this function")
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

  public void updateSuPerson(@WebParam(name = "person") SuPerson person, @WebParam(name = "audit") SvcAudit audit){
    if (person == null || audit == null)
      throw new java.lang.IllegalArgumentException("updateSuPerson - Null argument values not allowed in this function")
    if (person.uid == null)
      throw new java.lang.IllegalArgumentException("updateSuPerson - Person uid cant be null")
    SuPerson originalPerson = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, person.uid)
    if(person) {
      originalPerson.applySuPersonDifference(person)
      logger.debug("updateSuPerson - Trying to update SuPerson uid<${originalPerson.uid}>")
      SuPersonQuery.saveSuPerson(originalPerson)
      logger.info("updateSuPerson - Updated SuPerson uid<${originalPerson.uid}>")
    } else {
      throw new IllegalArgumentException("updateSuPerson - No such uid found: "+person.uid)
    }
  }
}
