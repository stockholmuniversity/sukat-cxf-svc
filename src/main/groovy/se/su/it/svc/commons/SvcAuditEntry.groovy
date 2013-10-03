package se.su.it.svc.commons

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
class SvcAuditEntry implements Serializable {

  static final long serialVersionUID = - 7177102741333441208L

  @XmlAttribute
  String key

  @XmlAttribute
  String value
}
