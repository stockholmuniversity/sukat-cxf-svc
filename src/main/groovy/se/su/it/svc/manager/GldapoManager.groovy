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

package se.su.it.svc.manager

import gldapo.Gldapo
import org.apache.log4j.Logger

/**
 * This class specifies the ldap integration<br />
 * We have 2 directories that use different sukat servers.<br />
 * LDAP_RO are the slave servers and we use them to do searches and queries.<br />
 * LDAP_RW is the master server, we use this one for modifications of data on sukat.<br />
 */
public class GldapoManager {
  private static final Logger logger = Logger.getLogger(GldapoManager.class)
  public static final String LDAP_RO = "ldapreadonly"
  public static final String LDAP_RW = "ldapreadwrite"

  public GldapoManager() {
    logger.info("Gldapo Init")
    def props = Config.getInstance().props

    Gldapo.initialize(
      directories: [(this.LDAP_RO):
      [url: props.ldap.serverro,
        base: "",
        userDn: "",
        password: "",
        ignorePartialResultException: false,
        env: [
          "java.naming.security.authentication": "GSSAPI",
          "javax.security.sasl.server.authentication": "true"
        ],
        searchControls: [
          countLimit: 500,
          timeLimit: 120000,
          searchScope: "subtree"
        ]
      ],(this.LDAP_RW):
        [url: props.ldap.serverrw,
          base: "",
          userDn: "",
          password: "",
          ignorePartialResultException: false,
          env: [
            "java.naming.security.authentication": "GSSAPI",
            "javax.security.sasl.server.authentication": "true"
          ],
          searchControls: [
            countLimit: 500,
            timeLimit: 120000,
            searchScope: "subtree"
          ]
        ]
      ],
      schemas: [se.su.it.svc.ldap.SuInitPerson,
                se.su.it.svc.ldap.SuEnrollPerson,
                se.su.it.svc.ldap.SuPerson,
                se.su.it.svc.ldap.SuRole,
                se.su.it.svc.ldap.SuCard,
                se.su.it.svc.ldap.SuServiceDescription,
                se.su.it.svc.ldap.SuService,
                se.su.it.svc.ldap.SuSubAccount]
    )
  }
}
