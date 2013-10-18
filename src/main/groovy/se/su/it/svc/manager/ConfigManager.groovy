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

@Slf4j
class ConfigManager {

  private final ConfigObject config

  public static String LDAP_RO
  public static String LDAP_RW

  public static final String DEFAULT_CONFIG_FILE_PATH = "WEB-INF/classes/defaultApplicationConfig.groovy"

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

    ConfigObject config = loadDefaultConfig()

    File customConfigFile = getConfigFile(configFileName)

    if (customConfigFile.exists()) {
      ConfigObject customConfig = parseConfig(customConfigFile.toURI().toURL())
      printConfiguration("*** ConfigManager: Loading Custom Configuration ***", customConfig)
      config.merge(customConfig)
    }

    this.config = config

    printConfiguration("*** ConfigManager: Final Configuration ***", config)

    /** Set variables and initialize Gldapo */

    checkMandatoryProperties()

    LDAP_RO = config.ldap.ro.name
    LDAP_RW = config.ldap.rw.name

    initializeGldapo(config as Map)
  }

  private static synchronized File getConfigFile(String configFileName) {
    File file = new File(configFileName)

    if (!file.exists()) {
      throw new IllegalStateException("Missing application configuration file => $configFileName")
    }
    return file
  }

  public static synchronized ConfigObject parseConfig(URL configUrl) {
    ConfigSlurper slurper = new ConfigSlurper()
    return slurper.parse(configUrl)
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

  private synchronized void printConfiguration(String name, ConfigObject config) {

    StringBuilder sb = new StringBuilder()
    sb.append("\n").append(name)

    config?.toProperties()?.sort { it.key }?.each { String key, value ->
      if (key.contains("password")) {
        sb.append("\n$key => *********")
      } else {
        sb.append("\n$key => $value")
      }
    }

    sb.append("\n")

    log.info sb.toString()
  }

  private final synchronized ConfigObject loadDefaultConfig() {
    URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
    URL defaultConfigUrl = cl.getResource(DEFAULT_CONFIG_FILE_PATH);
    ConfigObject config = new ConfigSlurper().parse(defaultConfigUrl)
    return config
  }

  /**
   * Initializes Gldapo (will be removed soon).
   */
  private final static void initializeGldapo(Map config) {
    Gldapo.initialize(config)
  }
}
