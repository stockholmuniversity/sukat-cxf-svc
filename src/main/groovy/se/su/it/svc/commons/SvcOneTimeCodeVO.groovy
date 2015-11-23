package se.su.it.svc.commons

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
public class SvcOneTimeCodeVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @XmlAttribute
    String uid

    @XmlAttribute
    String password

    @XmlAttribute
    String expire
}

