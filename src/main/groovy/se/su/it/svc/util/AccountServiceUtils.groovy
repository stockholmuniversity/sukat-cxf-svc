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

package se.su.it.svc.util

import groovy.util.logging.Slf4j

import java.text.Normalizer

import se.su.it.svc.commons.SvcSubAccountVO
import se.su.it.svc.commons.SvcUidPwd

import se.su.it.svc.manager.ConfigManager

import se.su.it.svc.query.AccountQuery
import se.su.it.svc.query.SuPersonQuery

@Slf4j
class AccountServiceUtils {

    final static pfxBlacklist = [
        'anal',
        'root'
    ]

    /**
     * Generate a unique user identifier (UID) from given name and surname.
     *
     * @param givenName Given name
     * @param sn Surname
     *
     * @return User identifier (UID)
     */
    static String generateUid(String givenName, String sn)
    {
        // Make sure we only have a-z
        def g = Normalizer.normalize(givenName, Normalizer.Form.NFD)
        g = g.replaceAll("\\p{M}", "")
        g = g.toLowerCase()
        g = g.replaceAll("-", "")
        g = g.replaceAll("'", "")

        def s = Normalizer.normalize(sn, Normalizer.Form.NFD)
        s = s.replaceAll("\\p{M}", "")
        s = s.toLowerCase()
        s = s.replaceAll("-", "")
        s = s.replaceAll("'", "")

        for (pos in [[2, 2], [1, 3], [3, 1], [0, 4], [4, 0]])
        {
            def pfx = g.substring(0, pos[0]) + s.substring(0, pos[1])

            if (pfxBlacklist.contains(pfx))
            {
                continue
            }

            for (i in 0..100)
            {
                def sfx = ""
                for (j in 1..4)
                {
                    sfx += (int)(Math.random() * 10);
                }

                def uid = pfx + sfx

                // Accounts include placeholders for historic uids
                if (AccountQuery.findAccountByUid(ConfigManager.LDAP_RW, uid))
                {
                    log.info("Found account with uid ${uid} in SUKAT")
                    continue
                }

                if (SuPersonQuery.findSuPersonByUID(ConfigManager.LDAP_RW, uid))
                {
                    log.info("Found person with uid ${uid} in SUKAT")
                    continue
                }

                // Assert that a good uid was found before it hurts.
                if (uid ==~ /^[a-z]{4}[0-9]{4}$/)
                {
                    return uid
                }
                else
                {
                    throw new RuntimeException("Generated an invalid uid (${uid}) for ${givenName} ${sn}.")
                }
            }
        }

        throw new RuntimeException("Failed to generate UID for ${givenName} ${sn}.")
    }

  /**
   * Create sub account for the given uid and type.
   *
   * @param uid uid of the user.
   * @param type Sub account type.
   *
   * @return A SvcUidPwd
   */
  static SvcUidPwd createSubAccount(String uid, String type)
  {
        def res = GeneralUtils.execHelper("createPrincipal", "${uid}/${type}")

        return res
  }

  /**
   * Delete sub account for the given uid and type.
   *
   * @param uid uid of the user.
   * @param type Sub account type.
   */
  static void deleteSubAccount(String uid, String type)
  {
        GeneralUtils.execHelper("deletePrincipal", "${uid}/${type}")
  }

  /**
   * Convert a domain to a dn (ex. it.su.se -> dc=it,dc=su,dc=se)
   *
   * @param domain the domain to be converted
   * @return a dn string, or empty string if domain==null
   */
  public static String domainToDN(String domain) {
    String retString = ''

    if (domain) {
      def domainSplit = domain.split("\\.")

      domainSplit.eachWithIndex {String name, int index ->
        retString += (index > 0) ? ",dc=${name}":"dc=${name}"
      }
    }

    return retString
  }

  /**
   * Retrieve sub account for the given uid and type.
   *
   * @param uid uid of the user.
   * @param type Sub account type.
   *
   * @return A SvcSubAccountVO.
   */
  static SvcSubAccountVO getSubAccount(String uid, String type)
  {
        def sav = new SvcSubAccountVO()

        def res = GeneralUtils.execHelper("getPrincipal", "${uid}/${type}")

        sav.uid = res.uid

        return sav
  }
}
