package se.su.it.svc

import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.audit.AuditAspectMethodDetails

/**
 * Created by: Jack Enqvist (jaen4109)
 * Date: 2012-09-06 ~ 09:38
 */
@SuCxfSvcSpocpRole(role = "sukat-user-admin")
public interface EnrollmentService {
  @AuditAspectMethodDetails(details = "resetOrCreatePrincipal,setPasswordExpiry")
  public String resetAndExpirePwd(java.lang.String uid, SvcAudit audit)
}