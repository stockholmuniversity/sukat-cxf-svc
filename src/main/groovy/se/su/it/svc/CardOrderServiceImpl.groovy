package se.su.it.svc
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.query.SuCardOrderQuery

import javax.jws.WebParam
import javax.jws.WebService

@WebService
class CardOrderServiceImpl implements CardOrderService {

  @Override
  List findAllCardOrdersForUid(@WebParam(name="uid") String uid, @WebParam(name = "audit") SvcAudit audit) {
    if (!uid) {
      return []
    }

    if (!audit) {
      throw new IllegalArgumentException('Missing audit')
    }

    /** TODO: Implement audit */

    List cardOrders = SuCardOrderQuery.findAllCardOrdersForUid(uid)

    return cardOrders
  }

  @Override
  String fetchOrdersForUid(@WebParam(name="uid") String uid) {

    System.err.println "fetchOrdersForUid $uid"
    if (!uid) {
      return []
    }

    /** TODO: Implement audit */

    List cardOrders = SuCardOrderQuery.findAllCardOrdersForUid(uid)

    System.err.println "fetchOrdersForUid: cardOrders: ${cardOrders?.size()}"

    return 'value'
  }

  @Override
  boolean test() {
    return true  //To change body of implemented methods use File | Settings | File Templates.
  }
}
