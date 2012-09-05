package se.su.it.svc.query

import se.su.it.svc.ldap.SuService
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.manager.EhCacheManager

/**
 * This class is a helper class for doing GLDAPO queries on the SuService GLDAPO schema.
 */
public class SuServiceQuery {
  /**
   * the CacheManager provides an instance of EhCache and some overridden methods (get/put/remove)
   * !important: when getting an object from LDAP which is to be changed, we always need to get it from the master,
   *             ie: using the props.ldap.serverrw (readWrite, to ensure that we are changing the up-to-date value)
   *             and NOT fetching the object from the cache.
   */
  def static cacheManager = EhCacheManager.getInstance()

  /**
   * Returns an Array of SuService objects.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param dn  the DistinguishedName for the user that you want to find cards for.
   * @return an <code>ArrayList<SuService></code> of SuService objects or an empty array if no service was found.
   * @see se.su.it.svc.ldap.SuService
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuService[] getSuServices(String directory, org.springframework.ldap.core.DistinguishedName dn) {
    def query = { qDirectory, qDn ->
      SuService.findAll(directory: qDirectory, base: qDn) {
        and {
          eq("objectclass", "suServiceObject")
        }
      }
    }

    def params = [key: ":getSuServiceDescription:", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suServices = (SuService[]) cacheManager.get(params, {query(directory, dn)})

    return suServices
  }
}
