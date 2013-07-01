package se.su.it.svc

import se.su.it.svc.commons.SvcAudit

public interface CardOrderService {
  List findAllCardOrdersForUid(String uid, SvcAudit audit)
}