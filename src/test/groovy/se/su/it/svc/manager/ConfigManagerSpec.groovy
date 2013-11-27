package se.su.it.svc.manager

import spock.lang.Specification

class ConfigManagerSpec extends Specification {

  def cleanup() {
    ConfigManager.metaClass = null;
  }

  def "ConfigManager: default constructor parses conf."() {
    when:
    ConfigManager configManager = new ConfigManager()

    then:
    configManager.@config instanceof ConfigObject
  }

  def "ConfigManager(): reads default config"() {
    when:
    def configManager = new ConfigManager()

    then:
    configManager.@config.containsKey "test"
  }
}
