package se.su.it.svc

import groovy.util.logging.Slf4j
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.query.SuCardOrderQuery

import javax.jws.WebParam
import javax.jws.WebService

@WebService @Slf4j
class CardOrderServiceImpl implements CardOrderService {

  @Override
  void findAllCardOrders(@WebParam(name = "audit") SvcAudit audit) {
    SuCardOrderQuery.findAllCardOrders()
  }
}
