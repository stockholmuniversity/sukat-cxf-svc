package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.commons.SvcAudit

@SuCxfSvcSpocpRole(role = "sukat-account-admin")
public interface WebServiceAdmin {
  public void setRootLogLevel(String level, SvcAudit audit)
  public void setApplicationLogLevel(String level, SvcAudit audit)
  public void setContainerLogLevel(String level, SvcAudit audit)
  public void setCxfLogLevel(String level, SvcAudit audit)
  public void setSpringLogLevel(String level, SvcAudit audit)
}
