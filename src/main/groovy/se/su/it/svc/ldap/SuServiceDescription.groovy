package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoSchemaFilter
import gldapo.schema.annotation.GldapoNamingAttribute

/**
 * GLDAPO schema class for holding SU Service Description also used by web service.
 */
class SuServiceDescription implements Serializable {

  static final long serialVersionUID = -687991492884005233L;

  @GldapoSchemaFilter("(objectClass=suServiceDescription)")

  @GldapoNamingAttribute
  String cn
  String suServiceType
  String suServicePolicy
  String suServiceURI
  String description
  String displayName
}
