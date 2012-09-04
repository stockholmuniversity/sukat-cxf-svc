package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuService

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-04
 * Time: 09:56
 * To change this template use File | Settings | File Templates.
 */
@SuCxfSvcSpocpRole(role = "sukat-entitlement-admin")
public interface ServiceService {
  public SuService[] getServices(String uid, SvcAudit audit)
}