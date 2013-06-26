package se.su.it.svc.query

import se.su.it.svc.ldap.SuEnrollPerson
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
          eq("objectclass", "suPerson")
        }
      }
    }

    def params = [key: ":getSuPersonFromUID:${uid}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suPerson = (SuPerson) cacheManager.get(params, { query(directory, uid) })

    return suPerson
  }

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
  static SuPerson getSuPersonFromNin(String directory, String nin) {
    def query = { qDirectory, qNin ->
      SuPerson.find(directory: qDirectory, base: "") {
        and {
          eq("norEduPersonNIN", qNin)
          eq("objectclass", "suPerson")
        }
      }
    }

    def params = [key: ":getSuPersonFromNin:${nin}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suPerson = (SuPerson) cacheManager.get(params, { query(directory, nin) })

    return suPerson
  }

  /**
   * Returns a SuInitPerson object, specified by the parameter nin.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param nin  the nin (12 digit social security number) for the user that you want to find.
   * @return an <code><SuPerson></code> or null.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuInitPerson getSuInitPersonFromNin(String directory, String nin) {
    def query = { qDirectory, qNin ->
      SuInitPerson.find(directory: qDirectory, base: "") {
        and {
          eq("norEduPersonNIN", qNin)
          eq("objectclass", "person")
        }
      }
    }

    def params = [key: ":getSuInitPersonFromNin:${nin}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suInitPerson = (SuInitPerson) cacheManager.get(params, { query(directory, nin) })

    return suInitPerson
  }

  /**
   * Returns a SuInitPerson object, specified by the parameter ssn.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param ssn  the ssn (social security number) for the user that you want to find.
   * @return an <code><SuPerson></code> or null.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuInitPerson getSuInitPersonFromSsn(String directory, String ssn) {
    def query = { qDirectory, qSsn ->
      SuInitPerson.find(directory: qDirectory, base: "") {
        and {
          eq("socialSecurityNumber", qSsn)
          eq("objectclass", "person")
        }
      }
    }

    def params = [key: ":getSuInitPersonFromSsn:${ssn}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suInitPerson = (SuInitPerson) cacheManager.get(params, { query(directory, ssn) })

    return suInitPerson
  }

  /**
   * Returns a SuEnrollPerson object, specified by the parameter nin.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param nin  the nin (12 digit social security number) for the user that you want to find.
   * @return an <code><SuPerson></code> or null.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuEnrollPerson getSuEnrollPersonFromNin(String directory, String nin) {
    def query = { qDirectory, qNin ->
      SuEnrollPerson.find(directory: qDirectory, base: "") {
        and {
          eq("norEduPersonNIN", qNin)
          eq("objectclass", "person")
        }
      }
    }

    def params = [key: ":getSuEnrollPersonFromNin:${nin}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suEnrollPerson = (SuEnrollPerson) cacheManager.get(params, { query(directory, nin) })

    return suEnrollPerson
  }

  /**
   * Returns a SuEnrollPerson object, specified by the parameter ssn.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param ssn  the ssn (social security number) for the user that you want to find.
   * @return an <code><SuPerson></code> or null.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuEnrollPerson getSuEnrollPersonFromSsn(String directory, String ssn) {
    def query = { qDirectory, qSsn ->
      SuEnrollPerson.find(directory: qDirectory, base: "") {
        and {
          eq("socialSecurityNumber", qSsn)
          eq("objectclass", "person")
        }
      }
    }

    def params = [key: ":getSuEnrollPersonFromSsn:${ssn}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suEnrollPerson = (SuEnrollPerson) cacheManager.get(params, { query(directory, ssn) })

    return suEnrollPerson
  }

  /**
   * Returns a non-cached SuPerson object, specified by the parameter uid.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param uid  the uid (user id) for the user that you want to find.
   * @return an <code><SuPerson></code> or null.
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuPerson getSuPersonFromUIDNoCache(String directory, String uid) {
    return SuPerson.find(directory: directory, base: "") {
      and {
        eq("uid", uid)
        eq("objectclass", "suPerson")
      }
    }
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
   * Init SuEnrollPerson entry in sukat
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param suInitPerson a SuInitPerson object to be saved in SUKAT.
   * @return void.
   * @see se.su.it.svc.ldap.SuEnrollPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void initSuEnrollPerson(String directory, SuEnrollPerson suEnrollPerson) {
    suEnrollPerson.directory = directory
    suEnrollPerson.save()
  }

  /**
   * Save a SuInitPerson object to ldap.
   *
   * @return void.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void saveSuInitPerson(SuInitPerson person) {
    person.save()
  }

  /**
   * Save a SuEnrollPerson object to ldap.
   *
   * @return void.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void saveSuEnrollPerson(SuEnrollPerson person) {
    person.save()
  }

}
