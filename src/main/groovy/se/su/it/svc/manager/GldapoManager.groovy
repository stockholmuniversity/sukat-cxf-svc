package se.su.it.svc.manager

import gldapo.Gldapo

class GldapoManager {

  public GldapoManager() {
    def config = ConfigManager.getInstance().@config
    def classLoader = this.class.classLoader

    // Convert the schema strings to classes.
    config.schemas = config.schemas.collect {
      if (it instanceof String)
        classLoader.loadClass(it)
      else
        null
    }

    Gldapo.initialize(config)
  }
}
