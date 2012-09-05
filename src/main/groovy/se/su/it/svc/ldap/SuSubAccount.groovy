package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoSchemaFilter
import gldapo.schema.annotation.GldapoNamingAttribute

class SuSubAccount implements Serializable {

  static final long serialVersionUID = -687001492884005033L;

  @GldapoSchemaFilter("(objectClass=top)(objectClass=account)")

  @GldapoNamingAttribute
  String uid
  String description
  Set<String> objectClass
}