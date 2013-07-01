package se.su.it.svc

import se.su.it.svc.commons.SvcAudit

public interface CardOrderService {
  public List findAllCardOrdersForUid(String uid, SvcAudit audit)
  public String fetchOrdersForUid(String uid)
  public boolean test()
}