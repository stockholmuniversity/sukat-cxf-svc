package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.commons.SvcAudit

@SuCxfSvcSpocpRole(role = "sukat-account-admin")
public interface AccountService {
  public void UpdateAffiliation(String uid, String affiliation, SvcAudit audit)
}
