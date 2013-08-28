/*
 * Copyright (c) 2013, IT Services, Stockholm University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Stockholm University nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package se.su.it.svc

import org.gcontracts.annotations.Ensures

import javax.jws.WebService
import groovy.util.logging.Slf4j
import org.gcontracts.annotations.Requires
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.commons.SvcCardOrderVO
import se.su.it.svc.util.CardOrderServiceUtils

@WebService @Slf4j
class CardOrderServiceImpl implements CardOrderService {

  def suCardOrderQuery

  @Override
  @Requires({ uid && audit })
  @Ensures({ result instanceof SvcCardOrderVO[] })
  public SvcCardOrderVO[] findAllCardOrdersForUid(String uid, SvcAudit audit) {
    def cardOrders = suCardOrderQuery.findAllCardOrdersForUid(uid) ?: []

    return (SvcCardOrderVO[]) cardOrders.toArray()
  }

  @Override
  @Requires({ cardOrderVO && audit })
  @Ensures({ result?.size() == 36 })
  public String orderCard(SvcCardOrderVO cardOrderVO, SvcAudit audit) {

    def result = CardOrderServiceUtils.validateCardOrderVO(cardOrderVO)

    if (result?.hasErrors) {
      log.error "orderCard: Supplied card order vo has errors."

      result?.errors?.each { key, value ->
        log.error "orderCard: Attribute $key has the following error $value"
      }

      throw new IllegalArgumentException("Supplied VO has errors, see svc log for more info.")
    }

    return suCardOrderQuery.orderCard(cardOrderVO)
  }
}
