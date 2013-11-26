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

import groovy.util.logging.Slf4j
import se.su.it.svc.server.config.ConfigHolder

@Slf4j
@Singleton
class ConfigManager implements ConfigHolder {

  private final ConfigObject config

  public static String LDAP_RO
  public static String LDAP_RW

  public static final String CUSTOM_CONFIG_PROP = 'config'
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
  private ConfigManager() {
    /** Parsing to properties first so the file type is a properties
     * file not a groovy config file (cause of the cxf) framework being written i java.
     * */
    ConfigObject config = loadDefaultConfig()

    File customConfigFile = getConfigFile(System.getProperty(CUSTOM_CONFIG_PROP))

    if (customConfigFile.exists()) {
      ConfigObject customConfig = parseConfig(customConfigFile.toURI().toURL())
      config.merge(customConfig)
    }

    this.config = config

    /** Set variables and initialize Gldapo */

    checkMandatoryProperties()

    LDAP_RO = config.ldap.ro.name
    LDAP_RW = config.ldap.rw.name
  }

  private static synchronized File getConfigFile(String configFileName) {
    File file = new File(configFileName ?: "")

    if (!file.exists()) {
      log.warn "Missing application configuration file => $configFileName"
    }
    return file
  }

  public static synchronized ConfigObject parseConfig(URL configUrl) {
    def classLoader = new GroovyClassLoader(ConfigManager.class.classLoader)
    def script = classLoader.parseClass(configUrl.text).newInstance()

    return new ConfigSlurper().parse(script as Script, configUrl)
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

  public synchronized void printConfiguration() {
    log.info "*** ConfigManager: Final Configuration ***"

    TreeMap sorted = new TreeMap<String, Object>(config?.flatten())

    for (Map.Entry<String, Object> entry : sorted.entrySet()) {
      if (entry.key.contains("password")) {
        log.info "$entry.key => *********"
      } else {
        log.info "$entry.key => $entry.value"
      }
    }
  }

  private final synchronized ConfigObject loadDefaultConfig() {
    URLClassLoader cl = (URLClassLoader) this.class.classLoader
    URL defaultConfigUrl = cl.getResource(DEFAULT_CONFIG_FILE_PATH);

    if (! defaultConfigUrl) {
      throw new IllegalStateException("Default configuration not found at: $defaultConfigUrl")
    }

    return parseConfig(defaultConfigUrl)
  }
}
