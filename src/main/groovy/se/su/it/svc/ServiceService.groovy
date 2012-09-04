package se.su.it.svc

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuService
import se.su.it.svc.ldap.SuServiceDescription

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-04
 * Time: 09:56
 * To change this template use File | Settings | File Templates.
 */
@SuCxfSvcSpocpRole(role = "sukat-service-admin")
public interface ServiceService {
  public SuService[] getServices(String uid, SvcAudit audit)
  public SuServiceDescription[] getServiceTemplates(SvcAudit audit)
}