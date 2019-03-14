package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter

@GldapoSchemaFilter("(objectClass=namedObject)")
class NamedObject
{
    @GldapoNamingAttribute
    String cn

    Set<String> mailLocalAddress
}

