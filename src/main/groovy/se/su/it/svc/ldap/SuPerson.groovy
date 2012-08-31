package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter
/**
 * GLDAPO schema class for SU employees and students also used by web service.
 */
class SuPerson implements Serializable {

  static final long serialVersionUID = -687991492884005033L;

  @GldapoSchemaFilter("(objectClass=suPerson)")

  @GldapoNamingAttribute
  String uid
}