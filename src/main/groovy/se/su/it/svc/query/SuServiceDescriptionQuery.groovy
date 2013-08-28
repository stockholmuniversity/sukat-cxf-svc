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

import se.su.it.svc.ldap.SuServiceDescription
import se.su.it.svc.manager.EhCacheManager
import se.su.it.svc.manager.GldapoManager

/**
 * This class is a helper class for doing GLDAPO queries on the SuServiceDescription GLDAPO schema.
 */
public class SuServiceDescriptionQuery {

  /**
   * the CacheManager provides an instance of EhCache and some overridden methods (get/put/remove)
   * !important: when getting an object from LDAP which is to be changed, we always need to get it from the master,
   *             ie: using the props.ldap.serverrw (readWrite, to ensure that we are changing the up-to-date value)
   *             and NOT fetching the object from the cache.
   */
  def static cacheManager = EhCacheManager.getInstance()

  /**
   * Returns an Array of SuServiceDescription objects.
   *
   *
   * @param serviceType String with ServiceType to fetch
   * @param directory which directory to use, see GldapoManager.
   * @return an <code><SuServiceDescription></code>.
   * @see se.su.it.svc.ldap.SuServiceDescription
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuServiceDescription getSuServiceDescription(String serviceType, String directory) {
    def query = { qDirectory ->
      SuServiceDescription.find(directory: qDirectory, base: "") {
        and {
          eq("objectclass", "suServiceDescription")
          eq("suServiceType", serviceType)
        }
      }
    }

    def params = [key: ":getSuServiceDescription:", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suServiceDescription = (SuServiceDescription) cacheManager.get(params, {query(directory)})

    return suServiceDescription
  }

  /**
   * Returns an Array of SuServiceDescription objects.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @return an <code>ArrayList<SuServiceDescription></code> of SuServiceDescription objects or an empty array if no service description was found.
   * @see se.su.it.svc.ldap.SuServiceDescription
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuServiceDescription[] getSuServiceDescriptions(String directory) {
    def query = { qDirectory ->
      SuServiceDescription.findAll(directory: qDirectory, base: "") {
        and {
          eq("objectclass", "suServiceDescription")
        }
      }
    }

    def params = [key: ":getSuServiceDescriptions:", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suServiceDescriptionList = (SuServiceDescription[]) cacheManager.get(params, {query(directory)})

    return suServiceDescriptionList
  }
}
