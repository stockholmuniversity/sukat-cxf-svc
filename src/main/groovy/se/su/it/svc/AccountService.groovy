package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.audit.AuditAspectMethodDetails
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.commons.SvcSuPersonVO

@SuCxfSvcSpocpRole(role = "sukat-account-admin")
public interface AccountService {
  public void updatePrimaryAffiliation(String uid, String affiliation, SvcAudit audit)
  @AuditAspectMethodDetails(details = "setPassword")
  public String resetPassword(String uid, SvcAudit audit)
  public void updateSuPerson(String uid,SvcSuPersonVO person, SvcAudit audit)
  public String createSuPerson(String uid, String domain, String nin, String givenName, String sn, SvcSuPersonVO person, boolean fullAccount, SvcAudit audit)
  public void terminateSuPerson(String uid, SvcAudit audit)
  public String getMailRoutingAddress(String uid, SvcAudit audit)
  public void setMailRoutingAddress(String uid, String mail, SvcAudit audit)
}
