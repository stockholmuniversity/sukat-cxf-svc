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

import se.su.it.svc.ldap.SuSubAccount
import se.su.it.svc.manager.ConfigManager
import se.su.it.svc.manager.EhCacheManager

/**
 * This class is a helper class for doing GLDAPO queries on the SuSubAccount GLDAPO schema.
 */
public class SuSubAccountQuery {
  /**
   * the CacheManager provides an instance of EhCache and some overridden methods (get/put/remove)
   * !important: when getting an object from LDAP which is to be changed, we always need to get it from the master,
   *             ie: using the props.ldap.serverrw (readWrite, to ensure that we are changing the up-to-date value)
   *             and NOT fetching the object from the cache.
   */
  def static cacheManager = EhCacheManager.getInstance()

  /**
   * Create a SUKAT sub account.
   *
   *
   * @param directory which directory to use, see ConfigManager.
   * @param subAcc a SuSubAccount object to be saved in SUKAT.
   * @return void.
   * @see se.su.it.svc.ldap.SuSubAccount
   * @see se.su.it.svc.manager.ConfigManager
   */
  static void createSubAccount(String directory, SuSubAccount subAcc) {
    subAcc.directory = directory
    subAcc.save()
    //refresh other cache keys
    this.getSuSubAccounts(ConfigManager.LDAP_RW,subAcc.getParent())
  }

  /**
   * Returns an array of SuSubAccount objects or empty array if non is found.
   *
   *
   * @param directory which directory to use, see ConfigManager.
   * @param dn  the DistinguishedName for the user that you want to find sub accounts for.
   * @return an <code>ArrayList<SuSubAccount></code> or an empty array if no sub account was found.
   * @see se.su.it.svc.ldap.SuSubAccount
   * @see se.su.it.svc.manager.ConfigManager
   */
  static SuSubAccount[] getSuSubAccounts(String directory, org.springframework.ldap.core.DistinguishedName dn) {
    def query = { qDirectory, qDn ->
      SuSubAccount.findAll(directory: qDirectory, base: qDn) {
        and {
          eq("objectclass", "top")
          eq("objectclass", "account")
        }
      }
    }

    def params = [key: ":getSuSubAccounts:${dn}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == ConfigManager.LDAP_RW)]
    def subAccounts = (SuSubAccount[]) cacheManager.get(params ,{query(directory,dn)})
    return subAccounts
  }
}
