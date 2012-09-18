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

    def params = [key: ":getSuServicesFor:${dn}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suServices = (SuService[]) cacheManager.get(params, {query(directory, dn)})

    return suServices
  }

  /**
   * Returns an SuService object.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param dn  the DistinguishedName for the user that you want to find services for.
   * @param serviceType the specific serviceType to search for.
   * @return an <code><SuService></code> object or null if no service was found.
   * @see se.su.it.svc.ldap.SuService
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuService getSuServiceByType(String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType) {
    def query = { qDirectory, qDn, qServiceType ->
      SuService.find(directory: qDirectory, base: qDn) {
        and {
          eq("objectclass", "suServiceObject")
          eq("suServiceType", qServiceType)
        }
      }
    }
    def params = [key: ":getSuServiceByType:${serviceType}${dn}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suService = (SuService)cacheManager.get(params, {query(directory,dn,serviceType)})
    return suService
  }

  /**
   * Create a SUKAT service.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param suService a suService object to be saved in SUKAT.
   * @return void.
   * @see se.su.it.svc.ldap.SuService
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void createService(String directory, SuService suService) {
    suService.directory = directory
    suService.save()
  }

  /**
   * Save a SuService object to ldap.
   *
   *
   * @return void.
   * @see se.su.it.svc.ldap.SuService
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void saveSuService(SuService suService) {
    suService.save()
    def params = [key: ":getSuServiceByType:${suService.suServiceType}${suService.getDn()}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: false]
    cacheManager.put(params, { suService })
  }
}
