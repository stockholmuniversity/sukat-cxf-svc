package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAttribute
/**
 * GLDAPO schema class for SU employees and students also used by web service.
 */
@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
class SuPerson implements Serializable {

  static final long serialVersionUID = -687991492884005033L;

  @GldapoSchemaFilter("(objectClass=suPerson)")

  @GldapoNamingAttribute
  @XmlAttribute
  String uid
  @XmlAttribute
  Set<String> objectClass
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

  public void applySuPersonDifference(SuPerson person) {
    if(this.eduPersonPrimaryAffiliation != person.eduPersonPrimaryAffiliation) this.eduPersonPrimaryAffiliation = person.eduPersonPrimaryAffiliation
    if(!isEqualSets(this.eduPersonAffiliation, person.eduPersonAffiliation)) this.eduPersonAffiliation = person.eduPersonAffiliation
    if(!isEqualSets(this.eduPersonEntitlement, person.eduPersonEntitlement)) this.eduPersonEntitlement = person.eduPersonEntitlement
    if(this.socialSecurityNumber != person.socialSecurityNumber) this.socialSecurityNumber = person.socialSecurityNumber
    if(this.givenName != person.givenName) this.givenName = person.givenName
    if(this.sn != person.sn) this.sn = person.sn
    if(this.cn != person.cn) this.cn = person.cn
    if(this.displayName != person.displayName) this.displayName = person.displayName
    if(this.title != person.title) this.title = person.title
    if(this.roomNumber != person.roomNumber) this.roomNumber = person.roomNumber
    if(this.telephoneNumber != person.telephoneNumber) this.telephoneNumber = person.telephoneNumber
    if(this.mobile != person.mobile) this.mobile = person.mobile
    if(!isEqualSets(this.sukatPULAttributes, person.sukatPULAttributes)) this.sukatPULAttributes = person.sukatPULAttributes
    if(this.labeledURI != person.labeledURI) this.labeledURI = person.labeledURI
    if(this.mail != person.mail) this.mail = person.mail
    if(!isEqualSets(this.mailLocalAddress, person.mailLocalAddress)) this.mailLocalAddress = person.mailLocalAddress
    if(this.sukatLOAFromDate != person.sukatLOAFromDate) this.sukatLOAFromDate = person.sukatLOAFromDate
    if(this.sukatLOAToDate != person.sukatLOAToDate) this.sukatLOAToDate = person.sukatLOAToDate
    if(!isEqualSets(this.eduPersonOrgUnitDN, person.eduPersonOrgUnitDN)) this.eduPersonOrgUnitDN = person.eduPersonOrgUnitDN
    if(this.eduPersonPrimaryOrgUnitDN != person.eduPersonPrimaryOrgUnitDN) this.eduPersonPrimaryOrgUnitDN = person.eduPersonPrimaryOrgUnitDN
    if(this.registeredAddress != person.registeredAddress) this.registeredAddress = person.registeredAddress
    if(this.mailRoutingAddress != person.mailRoutingAddress) this.mailRoutingAddress = person.mailRoutingAddress
    if(this.departmentNumber != person.departmentNumber) this.departmentNumber = person.departmentNumber
    if(this.homeMobilePhone != person.homeMobilePhone) this.homeMobilePhone = person.homeMobilePhone
    if(this.homePhone != person.homePhone) this.homePhone = person.homePhone
    if(!isEqualSets(this.homePostalAddress, person.homePostalAddress)) this.homePostalAddress = person.homePostalAddress
    if(this.homeLocalityName != person.homeLocalityName) this.homeLocalityName = person.homeLocalityName
    if(this.homePostalCode != person.homePostalCode) this.homePostalCode = person.homePostalCode
    if(this.description != person.description) this.description = person.description
    if(this.sukatComment != person.sukatComment) this.sukatComment = person.sukatComment
  }

  private boolean isEqualSets(Set<String> org, Set<String> mod) {
    if(org == null && mod == null) return true
    if(org != null && mod == null) return false
    if(org == null && mod != null) return false
    return org.equals(mod)
  }
}