package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAttribute
/**
 * GLDAPO schema class for SU University Cards also used by web service.
 */
@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
class SuCard implements Serializable{
  static final long serialVersionUID = -687991492884005073L
  @GldapoSchemaFilter("(objectClass=suCardOwner)")

  @GldapoNamingAttribute
  @XmlAttribute
  String cn
  @XmlAttribute
  String suCardUUID
  @XmlAttribute
  String suCardTypeURN
  @XmlAttribute
  String suCardSerial
  @XmlAttribute
  String suCardState

  static constraints =
    {
      suCardTypeURN(nullable:false)
      suCardUUID(nullable:false)
      suCardSerial(nullable:false)
      suCardState(nullable:false)
      cn(nullable:false)
    }
}
