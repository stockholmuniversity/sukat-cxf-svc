package se.su.it.svc.manager

import spock.lang.Specification

class ConfigManagerSpec extends Specification {

  def "ConfigManager: default constructor does nothing."() {
    when:
    ConfigManager configManager = new ConfigManager()

    then:
    configManager.@config == null
  }

  def "ConfigManager(): ..."() {
    when:
    def configManager = new ConfigManager('src/test/resources/config-test.properties')

    then:
    configManager.@config.containsKey "foo"
  }
}
