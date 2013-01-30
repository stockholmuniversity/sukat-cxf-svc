package se.su.it.svc.query

import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.manager.EhCacheManager
import se.su.it.svc.ldap.SuInitPerson

/**
 * This class is a helper class for doing GLDAPO queries on the SuPerson GLDAPO schema.
 */
public class SuPersonQuery {

  /**
   * the CacheManager provides an instance of EhCache and some overridden methods (get/put/remove)
   * !important: when getting an object from LDAP which is to be changed, we always need to get it from the master,
   *             ie: using the props.ldap.serverrw (readWrite, to ensure that we are changing the up-to-date value)
   *             and NOT fetching the object from the cache.
   */
  def static cacheManager = EhCacheManager.getInstance()

  /**
   * Returns a SuPerson object, specified by the parameter uid.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param uid  the uid (user id) for the user that you want to find.
   * @return an <code><SuPerson></code> or null.
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuPerson getSuPersonFromUID(String directory, String uid) {
    def query = { qDirectory, qUid ->
      SuPerson.find(directory: qDirectory, base: "") {
        and {
          eq("uid", qUid)
          eq("objectclass", "superson")
        }
      }
    }

    def params = [key: ":getSuPersonFromUID:${uid}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suPerson = (SuPerson) cacheManager.get(params, { query(directory, uid) })

    return suPerson
  }

  /**
   * Save a SuPerson object to ldap.
   * and putting the changed object in the cache so that the objects returned by this svc is always up-to-date.
   *
   * @return void.
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void saveSuPerson(SuPerson person) {
    person.save()
    def params = [key: ":getSuPersonFromUID:${person.uid}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: false]
    cacheManager.put(params, { person })
  }

  /**
   * Init SuPerson entry in sukat
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param suInitPerson a SuInitPerson object to be saved in SUKAT.
   * @return void.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void initSuPerson(String directory, SuInitPerson suInitPerson) {
    suInitPerson.directory = directory
    suInitPerson.save()
  }

  /**
   * Save a SuInitPerson object to ldap.
   * and putting the changed object in the cache so that the objects returned by this svc is always up-to-date.
   *
   * @return void.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void saveSuInitPerson(SuInitPerson person) {
    person.save()
  }

}
