package se.su.it.svc.query

import se.su.it.svc.ldap.SuCard
import se.su.it.svc.manager.ApplicationContextProvider
import se.su.it.svc.manager.EhCacheManager

/**
 * This class is a helper class for doing GLDAPO queries on the SuCard GLDAPO schema.
 */
public class SuCardQuery {

//  static applicationContext = ApplicationContextProvider.getApplicationContext()
//  static EhCacheManager cacheManager = (EhCacheManager)applicationContext.getBean('ehCacheManager')

  def static cacheManager = EhCacheManager.getInstance()
  /**
   * Returns a list (<code>ArrayList<SuCard></code>) of SuCard objects for a specific DistinguishedName, specified by the parameter dn.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param dn  the DistinguishedName for the user that you want to find cards for.
   * @param onlyActive  if only active cards should be returned in the result.
   * @return an <code>ArrayList<SuCard></code> of SuCard objects or an empty array if no card was found.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuCard[] findAllCardsBySuPersonDnAndOnlyActiveOrNot(String directory, org.springframework.ldap.core.DistinguishedName dn, boolean onlyActiveCards) {
    def cards = (SuCard[]) cacheManager.get([key: ":getAllCardsFor:${dn}", ttl: 120, cache: cacheManager.DEFAULT_CACHE_NAME],
      {
        SuCard.findAll(directory: directory, base: dn) {
          eq("objectClass", "suCardOwner")
          if (onlyActiveCards) {
            eq("suCardState", "urn:x-su:su-card:state:active")
          }
        }
      }
    )
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
    return SuCard.find(directory:directory, base: "") {
      eq("objectClass","suCardOwner")
      eq("suCardUUID",suCardUUID)
    }
  }

  /**
   * Save a SuCard object to ldap.
   *
   *
   * @return void.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void saveSuCard(SuCard suCard) {
    suCard.save()
  }
}
