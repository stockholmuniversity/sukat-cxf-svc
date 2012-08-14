package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAttribute

@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
class SuCard implements Serializable {
  static final long serialVersionUID = -687991492884005073L

  @GldapoSchemaFilter("(objectClass=suCardOwner)")
  @GldapoNamingAttribute
  @XmlAttribute
  String suCardTypeURN
  @XmlAttribute
  String suCardUUID
  @XmlAttribute
  String suCardSerial
  @XmlAttribute
  String suCardState
  @XmlAttribute
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
