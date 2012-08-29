package se.su.it.svc.commons

/**
 *  Audit class for all webservice requests.<br />
 *  This object is used by the Spring AOP Aspect enabled class AuditAspect.<br />
 * <b>uid</b> UserId of client invoker.<br />
 * <b>ipAddress</b> ip of Application Server.<br />
 * <b>client</b> ip of Client invoker.<br />
 */
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAttribute

@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
public class SvcAudit implements Serializable{
  @XmlAttribute
  String uid
  @XmlAttribute
  String ipAddress
  @XmlAttribute
  String client
}
