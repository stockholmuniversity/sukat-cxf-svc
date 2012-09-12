package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.audit.AuditAspectMethodDetails

@SuCxfSvcSpocpRole(role = "sukat-account-admin")
public interface AccountService {
  public void updatePrimaryAffiliation(String uid, String affiliation, SvcAudit audit)
  @AuditAspectMethodDetails(details = "setPassword")
  public String resetPassword(String uid, SvcAudit audit)
}
