package se.su.it.svc

import se.su.it.svc.ldap.SuCard
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.annotations.SuCxfSvcSpocpRole
/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-08-29
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
@SuCxfSvcSpocpRole(role = "sukat-card-admin")
public interface CardAdminService {
  public boolean revokeCard(String suCardUUID, SvcAudit audit);
}
