package se.su.it.svc

import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.commons.SvcCardOrderVO

public interface CardOrderService {
  SvcCardOrderVO[] findAllCardOrdersForUid(String uid, SvcAudit audit)
}