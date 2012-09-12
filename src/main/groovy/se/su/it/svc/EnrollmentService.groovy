package se.su.it.svc

import se.su.it.svc.commons.SvcUserCredential
import se.su.it.svc.commons.SvcAudit

/**
 * Created by: Jack Enqvist (jaen4109)
 * Date: 2012-09-06 ~ 09:38
 */
public interface EnrollmentService {
  public String enrollUserByUid(java.lang.String uid, SvcAudit audit)
  void expireUserPassword(java.lang.String uid, SvcAudit audit)
}