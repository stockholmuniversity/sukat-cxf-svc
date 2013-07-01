package se.su.it.svc

import groovy.util.logging.Slf4j
import se.su.it.svc.query.SuCardOrderQuery

import javax.jws.WebService

@WebService @Slf4j
class CardOrderServiceImpl implements CardOrderService {

  @Override
  void findAllCardOrders() {
    SuCardOrderQuery.findAllCardOrders()
  }
}
