package se.su.it.svc.commons

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAttribute
/**
 *  This object is returned by the Status Web Service.<br />
 * <b>name</b> Web Service Application name.<br />
 * <b>version</b> Web Service Application version.<br />
 * <b>buildtime</b> Web Service Application buildtime.<br />
 * <b>sname</b> Web Service Server name.<br />
 * <b>sversion</b> Web Service Server version.<br />
 * <b>sbuildtime</b> Web Service Server buildtime.<br />
 */
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
