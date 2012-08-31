package se.su.it.svc

import se.su.it.svc.commons.SvcAudit
import javax.jws.WebService

/**
 * Implementing class for EntitlementService CXF Web Service.
 * This Class handles all Entitlement admin activities in SUKAT.
 */
@WebService
public class EntitlementServiceImpl implements EntitlementService {
  public void addEntitlement(String uid, String entitlement, SvcAudit audit) {

  }
}
