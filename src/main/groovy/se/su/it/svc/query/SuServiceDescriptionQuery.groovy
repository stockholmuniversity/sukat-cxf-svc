package se.su.it.svc.query

import se.su.it.svc.ldap.SuServiceDescription

/**
 * This class is a helper class for doing GLDAPO queries on the SuServiceDescription GLDAPO schema.
 */
public class SuServiceDescriptionQuery {

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
    return SuServiceDescription.find(directory:directory, base: "") {
      and {
        eq("objectclass", "suServiceDescription")
      }
    }
  }
}
