package se.su.it.svc.commons

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-08-21
 * Time: 08:36
 * To change this template use File | Settings | File Templates.
 */
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAttribute

@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
public class SvcAudit {
  @XmlAttribute
  String uid
  @XmlAttribute
  String ipAddress
  @XmlAttribute
  String client
}
