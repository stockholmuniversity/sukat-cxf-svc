package se.su.it.svc.query

import se.su.it.svc.ldap.SuPerson

/**
 * This class is a helper class for doing GLDAPO queries on the SuPerson GLDAPO schema.
 */
public class SuPersonQuery {
  /**
   * Returns a SuPerson object, specified by the parameter uid.
   *
   * The ldap query uses read only servers (slaves).
   *
   * @param directory which directory to use, see GldapoManager.
   * @param uid  the uid (user id) for the user that you want to find.
   * @return an <code>ArrayList<SuCard></code> of SuCard objects or an empty array if no card was found.
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuPerson getSuPersonFromUID(String directory, String uid) {
    return SuPerson.find(directory:directory, base: "") {
      and {
        eq("uid", uid)
        eq("objectclass", "superson")
      }
    }
  }

}
