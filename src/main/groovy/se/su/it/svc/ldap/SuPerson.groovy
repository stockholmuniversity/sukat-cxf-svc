package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter
import se.su.it.svc.commons.SvcSuPersonVO

/**
 * GLDAPO schema class for SU employees and students also used by web service.
 */
class SuPerson implements Serializable {

  static final long serialVersionUID = -687991492884005033L;

  @GldapoSchemaFilter("(objectClass=suPerson)")
  @GldapoNamingAttribute
  String uid
  Set<String> objectClass
  String eduPersonPrimaryAffiliation
  Set<String> eduPersonAffiliation
  Set<String> eduPersonEntitlement
  String socialSecurityNumber
  String givenName
  String sn
  String cn
  String displayName
  Set<String> title
  Set<String> roomNumber
  Set<String> telephoneNumber
  String mobile
  Set<String> sukatPULAttributes //Adress (hem),Fax (hem),Hemsida (privat/hem),Mail (privat/hem),Mobil,Mobil (privat/hem),Stad (hem),Telefon (privat/hem)
  String labeledURI //hemsida
  Set<String> mail
  Set<String> mailLocalAddress
  String sukatLOAFromDate //Tjänstledighet börjar
  String sukatLOAToDate   //Tjänstledighet slutar
  Set<String> eduPersonOrgUnitDN
  String eduPersonPrimaryOrgUnitDN
  String registeredAddress
  String mailRoutingAddress
  String departmentNumber
  String homeMobilePhone
  String homePhone
  Set<String> homePostalAddress
  String homeLocalityName
  String homePostalCode
  String description
  String sukatComment

  public void applySuPersonDifference(SvcSuPersonVO person) {
    if(this.eduPersonPrimaryAffiliation != person.eduPersonPrimaryAffiliation) this.eduPersonPrimaryAffiliation = person.eduPersonPrimaryAffiliation
    if(!isEqualSets(this.eduPersonAffiliation, person.eduPersonAffiliation)) this.eduPersonAffiliation = person.eduPersonAffiliation
    if(!isEqualSets(this.eduPersonEntitlement, person.eduPersonEntitlement)) this.eduPersonEntitlement = person.eduPersonEntitlement
    if(this.socialSecurityNumber != person.socialSecurityNumber) this.socialSecurityNumber = person.socialSecurityNumber
    if(this.givenName != person.givenName) this.givenName = person.givenName
    if(this.sn != person.sn) this.sn = person.sn
    if(this.cn != person.givenName + " " + person.sn) this.cn = person.givenName + " " + person.sn
    if(this.displayName != person.displayName) this.displayName = person.displayName
    if(!isEqualSets(this.title, person.title)) this.title = person.title
    if(!isEqualSets(this.roomNumber, person.roomNumber)) this.roomNumber = person.roomNumber
    if(!isEqualSets(this.telephoneNumber, person.telephoneNumber)) this.telephoneNumber = person.telephoneNumber
    if(this.mobile != person.mobile) this.mobile = person.mobile
    if(!isEqualSets(this.sukatPULAttributes, person.sukatPULAttributes)) this.sukatPULAttributes = person.sukatPULAttributes
    if(this.labeledURI != person.labeledURI) this.labeledURI = person.labeledURI
    if(!isEqualSets(this.mail, person.mail)) this.mail = person.mail
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