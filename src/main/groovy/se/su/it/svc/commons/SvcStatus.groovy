package se.su.it.svc.commons

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAttribute

@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
public class SvcStatus {
  @XmlAttribute
  String name
  @XmlAttribute
  String version
  @XmlAttribute
  String buildtime
  @XmlAttribute
  String sname
  @XmlAttribute
  String sversion
  @XmlAttribute
  String sbuildtime
}
