package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoSchemaFilter
import gldapo.schema.annotation.GldapoNamingAttribute
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAttribute

/**
 * GLDAPO schema class for holding SU Service Description also used by web service.
 */
@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
class SuServiceDescription implements Serializable {

  static final long serialVersionUID = -687991492884005233L;

  @GldapoSchemaFilter("(objectClass=suServiceDescription)")

  @GldapoNamingAttribute
  @XmlAttribute
  String cn
  @XmlAttribute
  String suServiceType
  @XmlAttribute
  String suServicePolicy
  @XmlAttribute
  String suServiceURI
  @XmlAttribute
  String description
  @XmlAttribute
  String displayName
}
