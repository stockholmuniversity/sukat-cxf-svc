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
import org.gcontracts.annotations.Requires
import se.su.it.svc.ldap.SuCard
import se.su.it.svc.manager.ConfigManager
import se.su.it.svc.query.SuCardQuery
import se.su.it.svc.server.annotations.AuthzRole

import javax.jws.WebParam
import javax.jws.WebService

/**
 * Implementing class for CardAdminService CXF Web Service.
 * This Class handles all University Card admin activities in SUKAT.
 */
@WebService @Slf4j
@AuthzRole(role = "sukat-card-admin")
public class CardAdminServiceImpl implements CardAdminService {

  def suCardOrderQuery

    /**
     * This method puts a university card in revoked state in both SUKAT and SUCardDB.
     *
     * @param suCardUUID  the card uuid for the card.
     * @return void.
     * @see se.su.it.svc.ldap.SuCard
     */
    @Requires({ suCardUUID && revokerUid })
    public void revokeCard(
            @WebParam(name = "suCardUUID") String suCardUUID,
            @WebParam(name = "revokerUid") String revokerUid
        )
    {
        SuCard card = SuCardQuery.findCardBySuCardUUID(ConfigManager.LDAP_RW, suCardUUID)

        if (card)
        {
            card.suCardState = "urn:x-su:su-card:state:revoked"
            card.update()
        }

        suCardOrderQuery.markCardAsDiscarded(suCardUUID, revokerUid)
    }

  /**
   * This method sets a PIN for the specified University Card
   *
   *
   * @param suCardUUID  the card uuid for the card.
   * @param pin the new pin for the card.
   * @return void.
   * @see se.su.it.svc.ldap.SuCard
   */
  public void setCardPIN(
          @WebParam(name = "suCardUUID") String suCardUUID,
          @WebParam(name = "pin") String pin)
  {

        throw new UnsupportedOperationException("setCardPIN - PIN-code on university cards are currently not in use, see IDM-877")

  }
}
