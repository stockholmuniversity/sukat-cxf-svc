package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuService
import se.su.it.svc.ldap.SuServiceDescription
import se.su.it.svc.audit.AuditAspectMethodDetails

@SuCxfSvcSpocpRole(role = "sukat-service-admin")
public interface ServiceService {
  SuService[] getServices(String uid, SvcAudit audit)
  SuServiceDescription getServiceTemplate(String serviceType, SvcAudit audit)
  SuServiceDescription[] getServiceTemplates(SvcAudit audit)
  @AuditAspectMethodDetails(details = "resetOrCreatePrincipal")
  SuService enableServiceFully(String uid, String serviceType, String qualifier, String description, SvcAudit audit)
  void blockService(String uid, String serviceType, SvcAudit audit)
  void unblockService(String uid, String serviceType, SvcAudit audit)
}