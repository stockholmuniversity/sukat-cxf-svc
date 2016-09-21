package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter

@GldapoSchemaFilter("(objectClass=account)")
class Account
{
    @GldapoNamingAttribute
    String uid
}

