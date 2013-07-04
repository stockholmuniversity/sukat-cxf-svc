package se.su.it.svc.util

import groovy.util.logging.Slf4j
import se.su.it.svc.commons.SvcCardOrderVO

@Slf4j
class CardOrderServiceUtils {

  public static Map validateCardOrderVO(SvcCardOrderVO cardOrderVO) {
    Map response = [:]
    response.hasErrors = false
    response.errors = [:]

    if (!cardOrderVO) {
      throw new IllegalArgumentException("CardOrderVO: Object is invalid")
    }

    List shouldBeUnset = ['id', 'value', 'description', 'createTime', 'serial']

    for (attr in shouldBeUnset) {
      if (cardOrderVO["$attr"]) {
        response.hasErrors = true
        response.errors["$attr"] = "needs to be unset"
        log.error "validateCardOrderVO: Attribute $attr must be unset. Current value is: ${cardOrderVO["$attr"]}"
      }
    }

    List notNullable = ['owner', 'firstname', 'lastname', 'streetaddress1', 'locality', 'zipcode']

    for (attr in notNullable) {
      if (!cardOrderVO["$attr"]) {
        response.hasErrors = true
        response.errors["$attr"] = "nullable"
        log.error "validateCardOrderVO: Attribute $attr can't be unset. Current value is: ${cardOrderVO["$attr"]}"
      }
    }

    return response
  }
}
