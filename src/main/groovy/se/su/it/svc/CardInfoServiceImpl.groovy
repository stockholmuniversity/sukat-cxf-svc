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
import org.gcontracts.annotations.Ensures
import org.gcontracts.annotations.Requires
import org.springframework.ldap.core.DistinguishedName
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuCard
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.query.SuCardQuery
import se.su.it.svc.query.SuPersonQuery

import javax.jws.WebService

/**
 * Implementing class for CardInfoService CXF Web Service.
 * This Class handles all University Card information in SUKAT.
 */
@WebService @Slf4j
public class CardInfoServiceImpl implements CardInfoService {

  /**
   * Returns a list (<code>ArrayList<SuCard></code>) of SuCard objects for a specific user, specified by the parameter uid.
   *
   *
   * @param uid  the uid (user id) for the user that you want to find cards for.
   * @param onlyActive  if only active cards should be returned in the result.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return an <code>ArrayList<SuCard></code> of SuCard objects or an empty array if no card was found.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Override
  @Requires({ uid && audit && onlyActive != null })
  @Ensures({ result != null && result instanceof SuCard[] })
  public SuCard[] getAllCards(String uid, boolean onlyActive, SvcAudit audit) {
    def cards = new SuCard[0]
    def person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RO, uid)

    if (person) {
      String directory = GldapoManager.LDAP_RO
      DistinguishedName dn = person.getDn()
      cards = SuCardQuery.findAllCardsBySuPersonDnAndOnlyActiveOrNot(directory, dn, onlyActive)
    } else {
      log.warn("getAllCards: no such uid found: " + uid)
    }

    return cards
  }

  /**
   * Returns a SuCard object for a specific suCardUUID, specified by the parameter suCardUUID.
   *
   * @param suCardUUID  the card uuid for the card.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return an SuCard object or null if no card was found.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.commons.SvcAudit
   */
  @Override
  @Requires({ suCardUUID && audit })
  @Ensures({ result && result instanceof SuCard })
  public SuCard getCardByUUID(String suCardUUID, SvcAudit audit) {
    return SuCardQuery.findCardBySuCardUUID(GldapoManager.LDAP_RO, suCardUUID)
  }
}
