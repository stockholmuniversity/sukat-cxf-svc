package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.commons.SvcAudit

@SuCxfSvcSpocpRole(role = "sukat-account-admin")
public interface WebServiceAdmin {
  void setRootLogLevel(String level, SvcAudit audit)
  void setApplicationLogLevel(String level, SvcAudit audit)
  void setContainerLogLevel(String level, SvcAudit audit)
  void setCxfLogLevel(String level, SvcAudit audit)
  void setSpringLogLevel(String level, SvcAudit audit)
}
