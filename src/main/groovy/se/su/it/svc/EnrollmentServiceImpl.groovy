package se.su.it.svc

import se.su.it.svc.commons.SvcAudit
import javax.jws.WebService
import javax.jws.WebParam
import org.apache.log4j.Logger
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.manager.GldapoManager
import se.su.it.commons.Kadmin

/**
 * Implementing class for EnrollmentService CXF Web Service.
 * This Class handles all enrollment activities in SUKAT/KDC.
 */

@WebService
class EnrollmentServiceImpl implements EnrollmentService{
  private static final Logger logger = Logger.getLogger(EnrollmentServiceImpl.class)

  /**
   * This method enrolls user for the specified uid and returns the password.
   *
   *
   * @param uid  uid of the user.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return String with password.
   * @see se.su.it.svc.commons.SvcAudit
   */
  public String enrollUserByUid(@WebParam(name = "uid") String uid, @WebParam(name = "audit") SvcAudit audit) {
    if(uid == null || audit == null)
      throw new java.lang.IllegalArgumentException("enrollUserByUid - Null argument values not allowed in this function")
    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)
    if(person) {
      if(!person.objectClass?.contains("posixAccount")) {
        if(!person.eduPersonEntitlement?.contains("urn:x-su:autoenable-keeprouting")) {
          if(person.eduPersonEntitlement == null)
            person.eduPersonEntitlement = []
          person.eduPersonEntitlement << "urn:x-su:autoenable-keeprouting"
          SuPersonQuery.saveSuPerson(person)
        }
      }
      return Kadmin.newInstance().resetOrCreatePrincipal(uid.replaceFirst("\\.", "/"))
    } else {
      throw new IllegalArgumentException("enrollUserByUid no such uid found: "+uid)
    }

    return null
  }

  public void expireUserPassword(@WebParam(name = "uid") String uid, @WebParam(name = "audit") SvcAudit audit) {
    if(uid == null || audit == null)
      throw new java.lang.IllegalArgumentException("enrollUserByUid - Null argument values not allowed in this function")
  }
}
