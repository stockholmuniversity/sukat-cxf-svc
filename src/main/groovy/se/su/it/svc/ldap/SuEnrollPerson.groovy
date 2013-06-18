package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter
import se.su.it.svc.commons.SvcSuPersonVO

/**
 * GLDAPO schema class for init SuEnrollPerson.
 */
class SuEnrollPerson implements Serializable {

  static final long serialVersionUID = -687088492884005033L;

  @GldapoNamingAttribute
  String uid
  Set<String> objectClass
  String norEduPersonNIN
  String socialSecurityNumber
  String eduPersonPrincipalName
  String loginShell
  String homeDirectory
  String gidNumber
  String uidNumber
  String cn
  String sn
  String givenName
  String displayName
  String eduPersonPrimaryAffiliation
  Set<String> eduPersonAffiliation
  Set<String> mail
  Set<String> mailLocalAddress
}