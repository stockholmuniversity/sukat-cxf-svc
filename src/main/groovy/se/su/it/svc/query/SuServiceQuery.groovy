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

import org.springframework.ldap.core.DistinguishedName
import se.su.it.svc.ldap.SuService

/**
 * This class is a helper class for doing GLDAPO queries on the SuService GLDAPO schema.
 */
public class SuServiceQuery {

  /**
   * Returns an Array of SuService objects.
   *
   * @param directory which directory to use, see ConfigManager.
   * @param dn  the DistinguishedName for the user that you want to find cards for.
   * @return an <code>ArrayList<SuService></code> of SuService objects or an empty array if no service was found.
   * @see se.su.it.svc.ldap.SuService
   * @see se.su.it.svc.manager.ConfigManager
   */
  static SuService[] getSuServices(String directory, DistinguishedName dn) {
    return SuService.findAll(directory: directory, base: dn) {
      and {
        eq("objectclass", "suServiceObject")
      }
    }
  }

  /**
   * Returns an SuService object.
   *
   * @param directory which directory to use, see ConfigManager.
   * @param dn  the DistinguishedName for the user that you want to find services for.
   * @param serviceType the specific serviceType to search for.
   * @return an <code><SuService></code> object or null if no service was found.
   * @see se.su.it.svc.ldap.SuService
   * @see se.su.it.svc.manager.ConfigManager
   */
  static SuService getSuServiceByType(String directory, DistinguishedName dn, String serviceType) {
    return SuService.find(directory: directory, base: dn) {
      and {
        eq("objectclass", "suServiceObject")
        eq("suServiceType", serviceType)
      }
    }
  }
}
