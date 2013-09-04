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

import groovy.util.logging.Slf4j
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuCard
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.query.SuCardOrderQuery
import se.su.it.svc.query.SuCardQuery

import javax.jws.WebParam
import javax.jws.WebService

/**
 * Implementing class for CardAdminService CXF Web Service.
 * This Class handles all University Card admin activities in SUKAT.
 */
@WebService @Slf4j
public class CardAdminServiceImpl implements CardAdminService{

  /**
   * This method puts a university card in revoked state in both sukat and sucard db.
   *
   *
   * @param suCardUUID  the card uuid for the card.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return void.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.commons.SvcAudit
   */

  public void revokeCard(@WebParam(name = "suCardUUID") String suCardUUID, @WebParam(name = "audit") SvcAudit audit) {
    if (suCardUUID == null || audit == null)
      throw new IllegalArgumentException("revokeCard - Null argument values not allowed in this function")
    SuCard card = SuCardQuery.findCardBySuCardUUID(GldapoManager.LDAP_RW, suCardUUID)
    if (card != null) {
      card.suCardState = "urn:x-su:su-card:state:revoked"
      SuCardQuery.saveSuCard(card)
      try {
        new SuCardOrderQuery().markCardAsDiscarded(suCardUUID, audit?.uid)
      } catch (ex) {
        log.error "Failed to mark card $card as discarded in sucarddb", ex
      }
    } else {
      log.info("revokeCard: Could not find a card with uuid<${suCardUUID}>")
      throw new IllegalArgumentException("revokeCard: Could not find a card with uuid<${suCardUUID}>")
    }
  }

  /**
   * This method sets a PIN for the specified University Card
   *
   *
   * @param suCardUUID  the card uuid for the card.
   * @param pin the new pin for the card.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return void.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.commons.SvcAudit
   */
  public void setCardPIN(@WebParam(name = "suCardUUID") String suCardUUID, @WebParam(name = "pin") String pin, @WebParam(name = "audit") SvcAudit audit) {
    if(suCardUUID == null || pin == null || audit == null)
      throw new java.lang.IllegalArgumentException("setCardPIN - Null argument values not allowed in this function")
    SuCard card =SuCardQuery.findCardBySuCardUUID(GldapoManager.LDAP_RW,suCardUUID)
    if(card != null) {
      card.suCardPIN = pin
      SuCardQuery.saveSuCard(card)
    } else {
      log.info("setCardPIN: Could not find a card with uuid<${suCardUUID}>")
      throw new IllegalArgumentException("revokeCard: Could not find a card with uuid<${suCardUUID}>")
    }
  }
}
