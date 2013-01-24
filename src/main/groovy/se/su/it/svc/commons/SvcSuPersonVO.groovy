package se.su.it.svc.commons

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
public class SvcSuPersonVO {
  @XmlAttribute
  String eduPersonPrimaryAffiliation
  @XmlAttribute
  Set<String> eduPersonAffiliation
  @XmlAttribute
  Set<String> eduPersonEntitlement
  @XmlAttribute
  String socialSecurityNumber
  @XmlAttribute
  String givenName
  @XmlAttribute
  String sn
  @XmlAttribute
  String cn
  @XmlAttribute
  String displayName
  @XmlAttribute
  String title
  @XmlAttribute
  String roomNumber
  @XmlAttribute
  String telephoneNumber
  @XmlAttribute
  String mobile
  @XmlAttribute
  Set<String> sukatPULAttributes //Adress (hem),Fax (hem),Hemsida (privat/hem),Mail (privat/hem),Mobil,Mobil (privat/hem),Stad (hem),Telefon (privat/hem)
  @XmlAttribute
  String labeledURI //hemsida
  @XmlAttribute
  String mail
  @XmlAttribute
  Set<String> mailLocalAddress
  @XmlAttribute
  Date sukatLOAFromDate //Tjänstledighet börjar
  @XmlAttribute
  Date sukatLOAToDate   //Tjänstledighet slutar
  @XmlAttribute
  Set<String> eduPersonOrgUnitDN
  @XmlAttribute
  String eduPersonPrimaryOrgUnitDN
  @XmlAttribute
  String registeredAddress
  @XmlAttribute
  String mailRoutingAddress
  @XmlAttribute
  String departmentNumber
  @XmlAttribute
  String homeMobilePhone
  @XmlAttribute
  String homePhone
  @XmlAttribute
  Set<String> homePostalAddress
  @XmlAttribute
  String homeLocalityName
  @XmlAttribute
  String homePostalCode
  @XmlAttribute
  String description
  @XmlAttribute
  String sukatComment
}
