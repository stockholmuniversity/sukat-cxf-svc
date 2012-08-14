package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter

class SuCard implements Serializable {
  static final long serialVersionUID = -687991492884005073L

  @GldapoSchemaFilter("(objectClass=suCardOwner)")
  @GldapoNamingAttribute
  String suCardTypeURN
  String suCardUUID
  String suCardSerial
  String suCardState
  String cn

  static constraints =
    {
      suCardTypeURN(nullable:false)
      suCardUUID(nullable:false)
      suCardSerial(nullable:false)
      suCardState(nullable:false)
      cn(nullable:false)
    }
}
