package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuService
import se.su.it.svc.ldap.SuServiceDescription

@SuCxfSvcSpocpRole(role = "sukat-service-admin")
public interface ServiceService {
  public SuService[] getServices(String uid, SvcAudit audit)
  public SuServiceDescription[] getServiceTemplates(SvcAudit audit)
  public SuService enableServiceFully(String uid, String serviceType, String qualifier, String description, SvcAudit audit)
}