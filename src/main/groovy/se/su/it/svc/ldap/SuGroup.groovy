package se.su.it.svc.ldap


import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter
/**
 * GLDAPO schema class for SU groups also used by web service.
 */
class SuGroup
{
   static final long serialVersionUID = -687991492884005034L;
	@GldapoSchemaFilter("(objectClass=suGroup)")

  @GldapoNamingAttribute
  String cn
  Set<String> uniqueMember
  String mydn

  static constraints =
  {
    cn(nullable:false)
    uniqueMember(nullable:true)
  }
}