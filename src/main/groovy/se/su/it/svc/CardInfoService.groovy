package se.su.it.svc

import se.su.it.svc.ldap.SuCard
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.annotations.SuCxfSvcSpocpRole

@SuCxfSvcSpocpRole(role = "sukat-card-reader")
public interface CardInfoService {
  SuCard[] getAllCards(String uid, boolean onlyActive, SvcAudit audit);
  SuCard getCardByUUID(String suCardUUID, SvcAudit audit);
}
