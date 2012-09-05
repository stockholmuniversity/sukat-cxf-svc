package se.su.it.svc.query

import se.su.it.svc.ldap.SuSubAccount
import se.su.it.svc.manager.GldapoManager

/**
 * This class is a helper class for doing GLDAPO queries on the SuSubAccount GLDAPO schema.
 */
public class SuSubAccountQuery {
  static void createSubAccount(String directory, SuSubAccount subAcc, String parentDN) {
    subAcc.directory = directory
    subAcc.parent = parentDN
    subAcc.objectClass = ["top", "account"]
    subAcc.save()
  }

  static SuSubAccount[] getSuSubAccounts(String directory, org.springframework.ldap.core.DistinguishedName dn) {
    return SuSubAccount.findAll(directory:directory, base: dn) {
      and {
        eq("objectclass", "top")
        eq("objectclass", "account")
      }
    }
  }
}
