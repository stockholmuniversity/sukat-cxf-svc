package se.su.it.svc

import groovy.util.logging.Slf4j
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.query.SuCardOrderQuery

import javax.jws.WebParam
import javax.jws.WebService

@WebService @Slf4j
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
    log.info "fetchOrderForUid called for $uid"
    if (!uid) {
      return []
    }

    /** TODO: Implement audit */

    List cardOrders = SuCardOrderQuery.findAllCardOrdersForUid(uid)

    log.info "cardOrders: ${cardOrders?.size()}"

    for (order in cardOrders) {
      log.info "Order: ${order?.dump()}"
    }

    // return cardOrders
    return 'value'
  }
}
