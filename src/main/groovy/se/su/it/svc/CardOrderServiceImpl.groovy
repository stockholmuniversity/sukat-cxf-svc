package se.su.it.svc
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.commons.SvcCardOrderVO

import javax.jws.WebParam
import javax.jws.WebService

@WebService
class CardOrderServiceImpl implements CardOrderService {

  def suCardOrderQuery

  @Override
  SvcCardOrderVO[] findAllCardOrdersForUid(@WebParam(name="uid") String uid, @WebParam(name = "audit") SvcAudit audit) {
    if (!uid) {
      return []
    }

    if (!audit) {
      throw new IllegalArgumentException('Missing audit')
    }

    /** TODO: Implement audit */



    def cardOrders = (suCardOrderQuery.findAllCardOrdersForUid(uid))?:[]

    return (SvcCardOrderVO[]) cardOrders.toArray()
  }
}
