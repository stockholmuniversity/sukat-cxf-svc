package se.su.it.svc.manager

import gldapo.Gldapo

class GldapoManager {

  public GldapoManager() {
    Gldapo.initialize(ConfigManager.getInstance().@config)
  }
}
