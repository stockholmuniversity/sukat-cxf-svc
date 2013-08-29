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

package se.su.it.svc.query

import groovy.util.logging.Slf4j
import se.su.it.svc.ldap.SuCard
import se.su.it.svc.manager.EhCacheManager
import se.su.it.svc.manager.GldapoManager

/**
 * This class is a helper class for doing GLDAPO queries on the SuCard GLDAPO schema.
 */
@Slf4j
public class SuCardQuery {

  /**
   * the CacheManager provides an instance of EhCache and some overridden methods (get/put/remove)
   * !important: when getting an object from LDAP which is to be changed, we always need to get it from the master,
   *             ie: using the props.ldap.serverrw (readWrite, to ensure that we are changing the up-to-date value)
   *             and NOT fetching the object from the cache.
   */
  def static cacheManager = EhCacheManager.getInstance()

  /**
   * Returns a list (<code>ArrayList<SuCard></code>) of SuCard objects for a specific DistinguishedName, specified by the parameter dn.
   * !important: this query is cached,
   *
   * @param directory which directory to use, see GldapoManager.
   * @param dn  the DistinguishedName for the user that you want to find cards for.
   * @param onlyActive  if only active cards should be returned in the result.
   * @return an <code>ArrayList<SuCard></code> of SuCard objects or an empty array if no card was found.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuCard[] findAllCardsBySuPersonDnAndOnlyActiveOrNot(String directory, org.springframework.ldap.core.DistinguishedName dn, boolean onlyActiveCards) {
    def query = { qDirectory, qDn, qOnlyActiveCards ->
      SuCard.findAll(directory: qDirectory, base: qDn) {
        eq("objectClass", "suCardOwner")
        if (qOnlyActiveCards) {
          eq("suCardState", "urn:x-su:su-card:state:active")
        }
      }
    }

    def params = [key: ":getAllCardsFor:${dn}:onlyActive:${onlyActiveCards}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def cards = (SuCard[]) cacheManager.get(params ,{query(directory,dn,onlyActiveCards)})

    return cards
  }

  /**
   * Returns a SuCard object for a specific suCardUUID, specified by the parameter suCardUUID.
   *
   * @param directory which directory to use.
   * @param suCardUUID  the card uuid for the card.
   * @return an SuCard object or null if no card was found.
   * @see se.su.it.svc.ldap.SuCard
   */
  static SuCard findCardBySuCardUUID(String directory, String suCardUUID) {
    SuCard card = null

    try {
      card = SuCard.find(directory: directory, base: '') {
        eq("objectClass", "suCardOwner")
        eq("suCardUUID", suCardUUID)
      }
    } catch (ex) {
      log.error "Failed finding suCardOwner for suCardUUID: $suCardUUID", ex
    }

    return card
  }

  /**
   * Save a SuCard object to ldap.
   * and putting the changed object in the cache so that the objects returned by this svc is always up-to-date.
   *
   *
   * @return void.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void saveSuCard(SuCard suCard) {
    suCard.save()
    def params = [key: ":findCardBySuCardUUID:${suCard.suCardUUID}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: false]
    cacheManager.put(params, { suCard })

  }
}
