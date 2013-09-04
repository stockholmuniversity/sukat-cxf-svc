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

import org.apache.log4j.Logger

/**
 * Created by: Jack Enqvist (jaen4109)
 * Date: 2012-08-28 ~ 09:13
 *
 * this Singleton fetches a properties-fle from the filesystem and provides it as a groovy.util.ConfigObject.
 * the properties can then be accessed from the singleton objects field props.
 *
 * example:
 * def props = Config.getInstance().props
 *
 * def databaseUser     = props.database.user
 * def databasePassword = props.database.password
 * def databaseServer   = props.database.serverURL
 *
 */
class Config {
  private static final Logger logger = Logger.getLogger(Config.class)
  private static final Config INSTANCE = new Config()

  public ConfigObject props

  private Config() {
    loadProperties()
  }

  /**
   *
   * @return the singleton instance containing a groovy.util.ConfigObject called props.
   */
  static getInstance() {
    return INSTANCE
  }

  /**
   * loading the properties file from the filesystem at a predefined path.
   * populates the public member field props with a groovy.util.ConfigObject containing the properties
   */
  private loadProperties() {
    def props = new Properties()
    String definedConfigFileName = System.getProperty("config.properties")

    if (definedConfigFileName != null) {
      try {
        logger.info "Reading from defined input file $definedConfigFileName"
        new File(definedConfigFileName.trim()).withInputStream { stream ->
          props.load(stream)
        }
      } catch (ex) {
        logger.error "Exception when trying to read configuration file ${definedConfigFileName?.trim()} + ", ex
        logger.error "This instance will be highly unstable!"
      }
    } else {
      logger.info "No predefined config file set, loading default values"

      //Begin Default Values
      //Database
      props.put("database.url", "jdbc:mysql://localhost/gormtest")
      props.put("database.driver", "com.mysql.jdbc.Driver")
      props.put("database.user", "gormtest")
      props.put("database.password", "gormtest")

      props.put("enrollment.skipCreate", false) // We do skip create user for dev envs without config-file.

      //SuCard Database
      props.put("sucard.database.url", "jdbc:mysql://localhost/sucard")
      props.put("sucard.database.driver", "com.mysql.jdbc.Driver")
      props.put("sucard.database.user", "sucard")
      props.put("sucard.database.password", "sommar123")

      //Ldap
      props.put("ldap.serverro", "ldap://ldap-test.su.se")
      props.put("ldap.serverrw", "ldap://sukat-test-ldaprw02.it.su.se")
      props.put("ldap.accounts.default.parent", "dc=student,dc=su,dc=se")

      //Ssl
      props.put("http.port", 443)
      props.put("ssl.enabled", true);
      props.put("ssl.keystore", "cxf-svc-server.keystore")
      props.put("ssl.password", "changeit")

      //Spnego
      props.put("spnego.conf","/etc/spnego.conf");
      props.put("spnego.properties", "spnego.properties")
      props.put("spnego.realm", "SU.SE")
      props.put("spnego.kdc", "kerberos.su.se")

      //Ehcache
      props.put("ehcache.maxElementsInMemory", 10000)
      props.put("ehcache.eternal", false)
      props.put("ehcache.timeToIdleSeconds", 120)
      props.put("ehcache.timeToLiveSeconds", 600)
      props.put("ehcache.overflowToDisk", false)
      props.put("ehcache.diskPersistent", false)
      props.put("ehcache.diskExpiryThreadIntervalSeconds", 120)
      props.put("ehcache.memoryStoreEvictionPolicy", "LRU")
      //End Default Values
    }

    try {
      this.props = new ConfigSlurper().parse(props)
    } catch (ex) {
      logger.error "Slurping config file failed.", ex
    }

    logger.debug "ConfigObject: contains."
    this.props?.each { key, value ->
      logger.debug "$key = $value"
    }
  }


}
