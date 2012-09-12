package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.commons.SvcAudit

@SuCxfSvcSpocpRole(role = "sukat-account-admin")
public interface AccountService {
  public void updateAffiliation(String uid, String affiliation, SvcAudit audit)
  public String resetPassword(String uid, SvcAudit audit)
}
