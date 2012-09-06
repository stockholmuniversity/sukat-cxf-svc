package se.su.it.svc.query

import se.su.it.svc.ldap.SuServiceDescription
import se.su.it.svc.manager.EhCacheManager
import se.su.it.svc.manager.GldapoManager

/**
 * This class is a helper class for doing GLDAPO queries on the SuServiceDescription GLDAPO schema.
 */
public class SuServiceDescriptionQuery {

  /**
   * the CacheManager provides an instance of EhCache and some overridden methods (get/put/remove)
   * !important: when getting an object from LDAP which is to be changed, we always need to get it from the master,
   *             ie: using the props.ldap.serverrw (readWrite, to ensure that we are changing the up-to-date value)
   *             and NOT fetching the object from the cache.
   */
  def static cacheManager = EhCacheManager.getInstance()

  /**
   * Returns an Array of SuServiceDescription objects.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @return an <code>ArrayList<SuServiceDescription></code> of SuServiceDescription objects or an empty array if no service description was found.
   * @see se.su.it.svc.ldap.SuServiceDescription
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuServiceDescription[] getSuServiceDescriptions(String directory) {
    def query = { qDirectory ->
      SuServiceDescription.findAll(directory: qDirectory, base: "") {
        and {
          eq("objectclass", "suServiceDescription")
        }
      }
    }

    def params = [key: ":getSuServiceDescription:", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suServiceDescriptionList = (SuServiceDescription[]) cacheManager.get(params, {query(directory)})

    return suServiceDescriptionList
  }
}
