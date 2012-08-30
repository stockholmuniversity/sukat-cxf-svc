package se.su.it.svc

import se.su.it.svc.ldap.SuCard
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.annotations.SuCxfSvcSpocpRole

@SuCxfSvcSpocpRole(role = "sukat-card-admin")
public interface CardAdminService {
  public void revokeCard(String suCardUUID, SvcAudit audit);
}
