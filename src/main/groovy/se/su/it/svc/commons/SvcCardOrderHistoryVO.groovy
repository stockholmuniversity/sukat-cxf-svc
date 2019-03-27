package se.su.it.svc.commons

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
class SvcCardOrderHistoryVO implements Serializable
{
    @XmlAttribute
    Date timestamp
    @XmlAttribute
    String value
    @XmlAttribute
    String comment
}
