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
   * This method creates a new password and retire it for the specified uid.
   *
   *
   * @param uid  uid of the user.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return String with temporary password.
   * @see se.su.it.svc.commons.SvcAudit
   */
  public String resetAndExpirePwd(@WebParam(name = "uid") String uid, @WebParam(name = "audit") SvcAudit audit) {
    if(uid == null || audit == null)
      throw new java.lang.IllegalArgumentException("resetAndExpirePwd - Null argument values not allowed in this function")
    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, uid)
    if(person) {
      String pwd = Kadmin.newInstance().resetOrCreatePrincipal(uid.replaceFirst("\\.", "/"))
      Kadmin.newInstance().setPasswordExpiry(uid.replaceFirst("\\.", "/"), new Date())
      return pwd
    } else {
      throw new IllegalArgumentException("resetAndExpirePwd no such uid found: "+uid)
    }

    return null
  }
}
