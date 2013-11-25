package se.su.it.svc.manager

import spock.lang.Specification

class ConfigManagerSpec extends Specification {

  def cleanup() {
    ConfigManager.metaClass = null;
  }

  def "ConfigManager: default constructor parses conf."() {
    when:
    ConfigManager configManager = ConfigManager.getInstance()

    then:
    configManager.@config instanceof ConfigObject
  }

  def "ConfigManager(): reads default config"() {
    when:
    def configManager = ConfigManager.getInstance()

    then:
    configManager.@config.containsKey "test"
  }
}
