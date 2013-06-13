package se.su.it.svc

import se.su.it.commons.PasswordUtils
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.commons.SvcUidPwd
import se.su.it.svc.ldap.SuInitPerson
import se.su.it.svc.util.EnrollmentServiceUtils

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
      throw new IllegalArgumentException("resetAndExpirePwd - no such uid found: "+uid)
    }

    return null
  }

  /**
   * This method enrolls person in sukat, kerberos and afs.
   *
   *
   * @param nin  nin of the person. This can be a 10 or 12 digit social security number.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return SvcUidPwd object with the uid and password.
   * @see se.su.it.svc.commons.SvcAudit
   */
  public SvcUidPwd enrollUserByNIN(String nin, SvcAudit audit) {
    if(nin == null || audit == null)
      throw new java.lang.IllegalArgumentException("enrollUserByNIN - Null argument values not allowed in this function")
    if(nin.length() != 10 && nin.length() != 12)
      throw new java.lang.IllegalArgumentException("enrollUserByNIN - nin argument format error. Should be 10 or 12 char in length.")
    SuInitPerson suInitPerson = null
    SvcUidPwd svcUidPwd = new SvcUidPwd()
    svcUidPwd.password = PasswordUtils.genRandomPassword(10, 10)
    if(nin.length() == 10) {
      suInitPerson = SuPersonQuery.getSuInitPersonFromSsn(GldapoManager.LDAP_RW, nin)
    } else {
      suInitPerson = SuPersonQuery.getSuInitPersonFromNin(GldapoManager.LDAP_RW, nin)
      if(suInitPerson == null){ // Try to cut the 12 - digit ssn to 10
        suInitPerson = SuPersonQuery.getSuInitPersonFromSsn(GldapoManager.LDAP_RW, nin.substring(2,12))
      }
    }
    if(suInitPerson){
      if(EnrollmentServiceUtils.enableUser(suInitPerson.uid, svcUidPwd.password, suInitPerson)) {
        SuPersonQuery.saveSuInitPerson(suInitPerson)
        svcUidPwd.uid = suInitPerson.uid
      } else {
        throw new Exception("enrollUserByNIN - enroll failed in scripts.")
        return null
      }
    } else {
      throw new IllegalArgumentException("enrollUserByNIN - no such nin found: "+nin)
      return null
    }
    return svcUidPwd
  }
}
