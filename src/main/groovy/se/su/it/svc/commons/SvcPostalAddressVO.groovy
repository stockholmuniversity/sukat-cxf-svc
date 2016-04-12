package se.su.it.svc.commons

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
public class SvcPostalAddressVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @XmlAttribute
    String street1

    @XmlAttribute
    String street2

    @XmlAttribute
    String code

    @XmlAttribute
    String locality

    @XmlAttribute
    String country
}

