package se.su.it.svc.manager

import gldapo.Gldapo

class GldapoManager {

  public GldapoManager(ConfigManager configManager) {
    def config = configManager.@config
    Gldapo.initialize(config)
  }
}
