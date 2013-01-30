package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter
import se.su.it.svc.commons.SvcSuPersonVO

/**
 * GLDAPO schema class for init superson.
 */
class SuInitPerson implements Serializable {

  static final long serialVersionUID = -687091492884005033L;

  @GldapoNamingAttribute
  String uid
  Set<String> objectClass
  String norEduPersonNIN
  String eduPersonPrincipalName
  String loginShell
  String homeDirectory
  String gidNumber
  String uidNumber
  String cn
  String sn
  String givenName
}