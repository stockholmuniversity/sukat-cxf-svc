package se.su.it.svc.manager

import spock.lang.Ignore
import spock.lang.Specification

class ConfigManagerSpec extends Specification {

  def cleanup() {
    ConfigManager.metaClass = null;
  }

  def "ConfigManager: default constructor does nothing."() {
    when:
    ConfigManager configManager = new ConfigManager()

    then:
    configManager.@config == null
  }

  @Ignore
  def "ConfigManager(): ..."() {
    given:
    ConfigManager.metaClass.loadDefaultConfig = {}

    when:
    def configManager = new ConfigManager('src/test/resources/config-test.properties')

    then:
    configManager.@config.containsKey "foo"
  }
}
