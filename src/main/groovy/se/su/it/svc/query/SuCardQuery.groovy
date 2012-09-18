package se.su.it.svc.query

import se.su.it.svc.ldap.SuCard
import se.su.it.svc.manager.ApplicationContextProvider
import se.su.it.svc.manager.EhCacheManager
import se.su.it.svc.manager.GldapoManager
import net.sf.ehcache.search.Attribute
import se.su.it.svc.ldap.SuPerson
import net.sf.ehcache.search.Results

/**
 * This class is a helper class for doing GLDAPO queries on the SuCard GLDAPO schema.
 */
public class SuCardQuery {

  /**
   * the CacheManager provides an instance of EhCache and some overridden methods (get/put/remove)
   * !important: when getting an object from LDAP which is to be changed, we always need to get it from the master,
   *             ie: using the props.ldap.serverrw (readWrite, to ensure that we are changing the up-to-date value)
   *             and NOT fetching the object from the cache.
   */
  def static cacheManager = EhCacheManager.getInstance()

  /**
   * Returns a list (<code>ArrayList<SuCard></code>) of SuCard objects for a specific DistinguishedName, specified by the parameter dn.
   * !important: this query is cached,
   *
   * @param directory which directory to use, see GldapoManager.
   * @param dn  the DistinguishedName for the user that you want to find cards for.
   * @param onlyActive  if only active cards should be returned in the result.
   * @return an <code>ArrayList<SuCard></code> of SuCard objects or an empty array if no card was found.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuCard[] findAllCardsBySuPersonDnAndOnlyActiveOrNot(String directory, org.springframework.ldap.core.DistinguishedName dn, boolean onlyActiveCards) {
    def query = { qDirectory, qDn, qOnlyActiveCards ->
      SuCard.findAll(directory: qDirectory, base: qDn) {
        eq("objectClass", "suCardOwner")
        if (qOnlyActiveCards) {
          eq("suCardState", "urn:x-su:su-card:state:active")
        }
      }
    }

    def params = [key: ":getAllCardsFor:${dn}:onlyActive:${onlyActiveCards}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def cards = (SuCard[]) cacheManager.get(params ,{query(directory,dn,onlyActiveCards)})

    return cards
  }

  /**
   * Returns a SuCard object for a specific suCardUUID, specified by the parameter suCardUUID.
   *
   * @param directory which directory to use, see GldapoManager.
   * @param suCardUUID  the card uuid for the card.
   * @return an SuCard object or null if no card was found.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuCard findCardBySuCardUUID(String directory,String suCardUUID) {
    def query = { qDirectory, qSuCardUUID ->
      SuCard.find(directory: qDirectory, base: '') {
        eq("objectClass", "suCardOwner")
        eq("suCardUUID", qSuCardUUID)
      }
    }
    def params = [key: ":findCardBySuCardUUID:${suCardUUID}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RO)]
    def cards = (SuCard)cacheManager.get(params, {query(directory, suCardUUID)})
    return cards

//      return (SuCard)query(directory, suCardUUID)
  }

  /**
   * Save a SuCard object to ldap.
   * and putting the changed object in the cache so that the objects returned by this svc is always up-to-date.
   *
   *
   * @return void.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void saveSuCard(SuCard suCard) {
    suCard.save()
    def params = [key: ":findCardBySuCardUUID:${suCard.suCardUUID}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: false]
    cacheManager.put(params, { suCard })

  }
}
