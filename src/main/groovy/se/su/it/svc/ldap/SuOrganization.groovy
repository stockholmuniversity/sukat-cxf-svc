package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter

class SuOrganization implements Serializable {

  static final long serialVersionUID = -627991492884005048L;

  @GldapoSchemaFilter("(objectClass=suOrganization)")

  @GldapoNamingAttribute
  String ou
  String departmentNumber
  String norEduOrgUnitUniqueIdentifier
  String mydn

  static constraints = {
    ou(nullable:false)
    departmentNumber(nullable:true)
    norEduOrgUnitUniqueIdentifier(nullable:true)
  }


  String toString() {
    ou
  }
}