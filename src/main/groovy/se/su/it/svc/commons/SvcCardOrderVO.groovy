package se.su.it.svc.commons

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
class SvcCardOrderVO implements Serializable {

  @XmlAttribute
  String id
  @XmlAttribute
  String serial
  @XmlAttribute
  String owner
  @XmlAttribute
  String printer
  @XmlAttribute
  Date createTime
  @XmlAttribute
  String firstname
  @XmlAttribute
  String lastname
  @XmlAttribute
  String streetaddress1
  @XmlAttribute
  String streetaddress2
  @XmlAttribute
  String locality
  @XmlAttribute
  String zipcode
  @XmlAttribute
  String value
  @XmlAttribute
  String description
}
