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
import org.springframework.beans.factory.InitializingBean

@Slf4j
class ConfigManager implements InitializingBean {

  private final ConfigObject config

  public static String LDAP_RO
  public static String LDAP_RW

  private static final String APP_CONFIG_FILE_PROPERTY_KEY = "cxf-server.application.conf"

  private static List<String> mandatoryProperties = [
      'enrollment.create.skip',
      'soap.publishedEndpointUrl',
      'ldap.accounts.parent',
      'ldap.ro.name',
      'ldap.ro.url',
      'ldap.rw.name',
      'ldap.rw.url',
      'sucard.database.url',
      'sucard.database.driver',
      'sucard.database.user',
      'sucard.database.password',
  ]

  /**
   * Singleton private constructor (unused but defined).
   */
  private ConfigManager() {}

  /**
   * Default constructor invoked via bean xml config.
   * @param configFileName
   */
  private ConfigManager(String configFileName) {

    /** Parsing to properties first so the file type is a properties
     * file not a groovy config file (cause of the cxf) framework being written i java.
     * */
    Properties properties = new Properties()
    File configFile = new File(configFileName)

    configFile?.withReader('UTF-8') { Reader reader ->
      properties.load(reader)
    }

    ConfigSlurper slurper = new ConfigSlurper()

    config = slurper.parse(properties)

    File file = new File(properties.getProperty(APP_CONFIG_FILE_PROPERTY_KEY))

    if (!file.exists()) {
      throw new IllegalStateException("Missing application configuration file.")
    }

    URL configUrl = file.toURI().toURL()
    config.merge(slurper.parse(configUrl))

    /** Set variables and initialize Gldapo */

    checkMandatoryProperties()

    LDAP_RO = config.ldap.ro.name
    LDAP_RW = config.ldap.rw.name

    if (configUrl) {
      initializeGldapo(configUrl)
    }
  }

  private void checkMandatoryProperties() {
    Properties properties = getProperties()
    for (property in mandatoryProperties) {
      if (properties.getProperty(property) == null) {
        throw new IllegalStateException("Missing mandatory property: $property")
      }
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

  public String toString() {

    StringBuilder sb = new StringBuilder()
    sb.append("\n**** ConfigManager Configuration ****")
    this?.config?.toProperties()?.sort { it.key }?.each { String key, value ->
      if (key.contains("password")) {
        if (value instanceof String && value?.size()) {
          sb.append("\n$key => *********")
        } else {
          sb.append("\n$key => ''")
        }
      } else {
        sb.append("\n$key => $value")
      }
    }
    sb.append("\n")

    return sb.toString()
  }

  /**
   * Initializes Gldapo (will be removed soon).
   */
  private final static void initializeGldapo(URL configUrl) {
    Gldapo.initialize(configUrl)
  }

  @Override
  /**
   * Log is not initialized at the time of execution in the development environment (works fine when war packaged...),
   * so a println is needed.
   */
  void afterPropertiesSet() throws Exception {
    log.info toString()
    println toString()
  }
}
