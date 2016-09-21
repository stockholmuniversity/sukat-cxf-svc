package se.su.it.svc.query

import se.su.it.svc.ldap.Account

public class AccountQuery
{
    /**
    * Returns an Account object, specified by the parameter uid.
    *
    * @param directory which directory to use, see ConfigManager.
    * @param uid  the uid (user id) for the user that you want to find.
    * @return an <code><Account></code> or null.
    * @see se.su.it.svc.ldap.Account
    * @see se.su.it.svc.manager.ConfigManager
    */
    static Account findAccountByUid(String directory, String uid)
    {
        return Account.find(
            directory: directory,
            filter: "(uid=${uid})"
        )
    }
}

