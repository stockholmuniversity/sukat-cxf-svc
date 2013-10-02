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
import groovy.util.logging.Slf4j
import se.su.it.svc.ldap.*

@Slf4j
class ConfigManager {

  private final ConfigObject config

  public static final String LDAP_RO = "ldapreadonly"
  public static final String LDAP_RW = "ldapreadwrite"

  /**
   * Singleton private constructor (unused but defined).
   */
  private ConfigManager() {}

  /**
   * Default constructor invoked via bean xml config.
   * @param configFileName
   */
  private ConfigManager(String configFileName) {
    try {
      /** Parsing to properties first so the file type is a properties
       * file not a groovy config file (cause of the cxf) framework being written i java.
       * */
      Properties properties = new Properties()
      File configFile = new File(configFileName)

      configFile?.withReader('UTF-8') { Reader reader ->
        properties.load(reader)
      }

      log.info "ConfigManager: Initializing with config file: $configFileName"

      this.config = new ConfigSlurper().parse(properties)

      log.info "ConfigManager: Initialization complete."
    } catch (ex) {
      log.error "Failed to parse config file", ex
      throw ex
    }
    try {
      initializeGldapo()
    } catch (ex) {
      log.error "Gldapo initialization failed.", ex
      throw ex
    }
  }
  /**
   * Copy of the configuration returned in the form of properties.
   * @return Properties config
   */
  public Properties getProperties() {
    return config.toProperties()
  }
  /**
   * Copy of the configuration returned in the form of a ConfigObject.
   * @return ConfigObject config
   */
  public ConfigObject getConfig() {
    return new ConfigSlurper().parse(getProperties())
  }
  /**
   * Initializes Gldapo (will be removed soon).
   */
  private final void initializeGldapo() {
    String readOnly = config.app.ldap.serverro
    String readWrite = config.app.ldap.serverrw
    log.info "ConfigManager: ReadOnly url: $readOnly"
    log.info "ConfigManager: ReadWrite url: $readWrite"

    Gldapo.initialize(
        directories: [(LDAP_RO):
            [url: readOnly,
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
            ],(LDAP_RW):
            [url: readWrite,
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
        schemas: [
            SuPersonStub,
            SuRole,
            SuCard,
            SuPerson,
            SuServiceDescription,
            SuService,
            SuSubAccount,
            ]
    )
  }

}
