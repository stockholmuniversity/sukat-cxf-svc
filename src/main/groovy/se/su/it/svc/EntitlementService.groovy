package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.commons.SvcAudit

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-08-31
 * Time: 12:09
 * To change this template use File | Settings | File Templates.
 */
@SuCxfSvcSpocpRole(role = "sukat-entitlement-admin")
public interface EntitlementService {
  public void addEntitlement(String uid, String entitlement, SvcAudit audit)
}
