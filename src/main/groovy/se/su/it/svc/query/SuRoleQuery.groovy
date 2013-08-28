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

import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.manager.EhCacheManager
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.ldap.SuRole

/**
 * This class is a helper class for doing GLDAPO queries on the SuRole GLDAPO schema.
 */
public class SuRoleQuery {

  /**
   * the CacheManager provides an instance of EhCache and some overridden methods (get/put/remove)
   * !important: when getting an object from LDAP which is to be changed, we always need to get it from the master,
   *             ie: using the props.ldap.serverrw (readWrite, to ensure that we are changing the up-to-date value)
   *             and NOT fetching the object from the cache.
   */
  def static cacheManager = EhCacheManager.getInstance()

  /**
   * Returns a SuRole object, specified by the parameter dn.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param dn  the DN for the role that you want to find.
   * @return an <code><SuRole></code> or null.
   * @see se.su.it.svc.ldap.SuRole
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuRole getSuRoleFromDN(String directory, String dn) {
    def query = { qDirectory, qDn ->
      SuRole.find(directory: qDirectory, base: qDn) {
        and {
          eq("objectclass", "organizationalRole")
          eq("objectclass", "suRole")
        }
      }
    }

    def params = [key: ":getSuRoleFromDN:${dn}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suRole = (SuRole) cacheManager.get(params, { query(directory, dn) })

    return suRole
  }

  /**
   * Save a SuRole object to ldap.
   * and putting the changed object in the cache so that the objects returned by this svc is always up-to-date.
   *
   * @return void.
   * @see se.su.it.svc.ldap.SuRole
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void saveSuRole(SuRole role) {
    role.save()
    def params = [key: ":getSuRoleFromDN:${role.getDn().toString()}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: false]
    cacheManager.put(params, { role })
  }

}
