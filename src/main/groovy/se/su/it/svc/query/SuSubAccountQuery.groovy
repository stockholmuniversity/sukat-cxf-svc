package se.su.it.svc.query

import se.su.it.svc.ldap.SuSubAccount
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.manager.EhCacheManager

/**
 * This class is a helper class for doing GLDAPO queries on the SuSubAccount GLDAPO schema.
 */
public class SuSubAccountQuery {
  /**
   * the CacheManager provides an instance of EhCache and some overridden methods (get/put/remove)
   * !important: when getting an object from LDAP which is to be changed, we always need to get it from the master,
   *             ie: using the props.ldap.serverrw (readWrite, to ensure that we are changing the up-to-date value)
   *             and NOT fetching the object from the cache.
   */
  def static cacheManager = EhCacheManager.getInstance()

  /**
   * Create a SUKAT sub account.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param subAcc a SuSubAccount object to be saved in SUKAT.
   * @return void.
   * @see se.su.it.svc.ldap.SuSubAccount
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void createSubAccount(String directory, SuSubAccount subAcc) {
    subAcc.directory = directory
    subAcc.save()
    //refresh other cache keys
    this.getSuSubAccounts(GldapoManager.LDAP_RW,subAcc.getParent())
  }

  /**
   * Returns an array of SuSubAccount objects or empty array if non is found.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param dn  the DistinguishedName for the user that you want to find sub accounts for.
   * @return an <code>ArrayList<SuSubAccount></code> or an empty array if no sub account was found.
   * @see se.su.it.svc.ldap.SuSubAccount
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuSubAccount[] getSuSubAccounts(String directory, org.springframework.ldap.core.DistinguishedName dn) {
    def query = { qDirectory, qDn ->
      SuSubAccount.findAll(directory: qDirectory, base: qDn) {
        and {
          eq("objectclass", "top")
          eq("objectclass", "account")
        }
      }
    }

    def params = [key: ":getSuSubAccounts:${dn}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def subAccounts = (SuSubAccount[]) cacheManager.get(params ,{query(directory,dn)})
    return subAccounts
  }
}
