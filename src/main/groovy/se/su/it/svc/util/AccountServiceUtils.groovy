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

import se.su.it.svc.commons.SvcSubAccountVO
import se.su.it.svc.commons.SvcUidPwd

class AccountServiceUtils {

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
        def res = GeneralUtils.execHelper("createSubAccount", "${uid}/${type}")

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
        GeneralUtils.execHelper("deleteSubAccount", "${uid}/${type}")
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

        def res = GeneralUtils.execHelper("getSubAccount", "${uid}/${type}")

        sav.uid = res.uid

        return sav
  }
}
