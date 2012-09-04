package se.su.it.svc.query

import se.su.it.svc.ldap.SuService

/**
 * This class is a helper class for doing GLDAPO queries on the SuService GLDAPO schema.
 */
public class SuServiceQuery {
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
    return SuService.findAll(directory:directory, base: dn) {
      and {
        eq("objectclass", "suServiceObject")
      }
    }
  }
}
