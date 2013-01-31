package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.audit.AuditAspectMethodDetails
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.commons.SvcSuPersonVO

@SuCxfSvcSpocpRole(role = "sukat-account-admin")
public interface RoleService {
  public void addUidToRoles(String uid,List<String> roleDNList, SvcAudit audit)
  public void removeUidFromRoles(String uid,List<String> roleDNList, SvcAudit audit)
}
