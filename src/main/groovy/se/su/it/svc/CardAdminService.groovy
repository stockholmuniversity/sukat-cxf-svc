package se.su.it.svc

import se.su.it.svc.ldap.SuCard
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.annotations.SuCxfSvcSpocpRole

@SuCxfSvcSpocpRole(role = "sukat-card-admin")
public interface CardAdminService {
  void revokeCard(String suCardUUID, SvcAudit audit)
  void setCardPIN(String suCardUUID, String pin, SvcAudit audit)
}
