package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.audit.AuditAspectMethodDetails
import se.su.it.svc.commons.SvcSuPersonVO

@SuCxfSvcSpocpRole(role = "sukat-account-admin")
public interface AccountService {
  void updatePrimaryAffiliation(String uid, String affiliation, SvcAudit audit)
  @AuditAspectMethodDetails(details = "setPassword")
  String resetPassword(String uid, SvcAudit audit)
  void updateSuPerson(String uid,SvcSuPersonVO person, SvcAudit audit)
  String createSuPerson(String uid, String domain, String nin, String givenName, String sn, SvcSuPersonVO person, boolean fullAccount, SvcAudit audit)
  void terminateSuPerson(String uid, SvcAudit audit)
  String getMailRoutingAddress(String uid, SvcAudit audit)
  void setMailRoutingAddress(String uid, String mail, SvcAudit audit)
  SvcSuPersonVO findSuPersonByNorEduPersonNIN(String norEduPersonNIN, SvcAudit audit)
  SvcSuPersonVO findSuPersonBySocialSecurityNumber(String socialSecurityNumber, SvcAudit audit)
  SvcSuPersonVO findSuPersonByUid(String uid, SvcAudit audit)
}
