package se.su.it.svc

import groovy.util.logging.Slf4j
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.commons.SvcCardOrderVO
import se.su.it.svc.query.SuCardOrderQuery
import se.su.it.svc.util.CardOrderServiceUtils

import javax.jws.WebParam
import javax.jws.WebService

@WebService @Slf4j
class CardOrderServiceImpl implements CardOrderService {

  SuCardOrderQuery suCardOrderQuery

  /** TODO: Implement
   * Audit
   * Tests
   *
   * */

  @Override
  SvcCardOrderVO[] findAllCardOrdersForUid(@WebParam(name="uid") String uid, @WebParam(name = "audit") SvcAudit audit) {
    if (!uid) {
      return []
    }

    if (!audit) {
      throw new IllegalArgumentException('Missing audit')
    }

    def cardOrders = (suCardOrderQuery.findAllCardOrdersForUid(uid))?:[]

    return (SvcCardOrderVO[]) cardOrders.toArray()
  }

  @Override
  String orderCard(SvcCardOrderVO cardOrderVO, SvcAudit audit) {

    if (!cardOrderVO) {
      return ''
    }

    if (!audit) {
      throw new IllegalArgumentException('Missing audit')
    }

    def result = CardOrderServiceUtils.validateCardOrderVO(cardOrderVO)

    if (result?.hasErrors) {
      log.error "orderCard: Supplied card order vo has errors."
      result?.errors?.each { key, value ->
        log.error "orderCard: Attribute $key has the following error $value"
      }
      // TODO: send proper error message.
      throw new IllegalArgumentException("Supplied VO has errors, see svc log for more info.")
    }

    String uuid = suCardOrderQuery.orderCard(cardOrderVO)



    return uuid
  }
}
