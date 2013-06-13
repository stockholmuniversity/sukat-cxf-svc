package se.su.it.svc.commons

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAttribute

@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
public class SvcUidPwd implements Serializable{
  static final long serialVersionUID = -687991772884255073L
  @XmlAttribute
  String uid
  @XmlAttribute
  String password
}
