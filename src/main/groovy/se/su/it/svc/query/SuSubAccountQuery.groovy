package se.su.it.svc.query

import se.su.it.svc.ldap.SuSubAccount
import se.su.it.svc.manager.GldapoManager

/**
 * This class is a helper class for doing GLDAPO queries on the SuSubAccount GLDAPO schema.
 */
public class SuSubAccountQuery {

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
    return SuSubAccount.findAll(directory:directory, base: dn) {
      and {
        eq("objectclass", "top")
        eq("objectclass", "account")
      }
    }
  }
}
