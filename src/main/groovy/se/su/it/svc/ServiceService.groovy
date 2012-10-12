package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuService
import se.su.it.svc.ldap.SuServiceDescription
import se.su.it.svc.audit.AuditAspectMethodDetails

@SuCxfSvcSpocpRole(role = "sukat-service-admin")
public interface ServiceService {
  public SuService[] getServices(String uid, SvcAudit audit)
  public SuServiceDescription getServiceTemplate(SvcAudit audit)
  public SuServiceDescription[] getServiceTemplates(SvcAudit audit)
  @AuditAspectMethodDetails(details = "resetOrCreatePrincipal")
  public SuService enableServiceFully(String uid, String serviceType, String qualifier, String description, SvcAudit audit)
  public void blockService(String uid, String serviceType, SvcAudit audit)
  public void unblockService(String uid, String serviceType, SvcAudit audit)
}