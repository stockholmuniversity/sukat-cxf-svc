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
    return SuService.find(directory:directory, base: dn) {
      and {
        eq("objectclass", "suServiceObject")
        eq("suServiceType", serviceType)
      }
    }
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
  }
}
