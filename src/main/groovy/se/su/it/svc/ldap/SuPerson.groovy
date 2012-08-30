package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter
/**
 * GLDAPO schema class for SU employees and students also used by web service.
 */
class SuPerson implements Serializable {

  static final long serialVersionUID = -687991492884005033L;

  @GldapoSchemaFilter("(objectClass=suPerson)")

  @GldapoNamingAttribute
  String uid
  String displayName
  Set<String> mail
  Set<String> mailLocalAddress
  String telephoneNumber
  String sn
  String cn
  String givenName
  Set<String> eduPersonAffiliation
  String eduPersonPrimaryAffiliation
  String socialSecurityNumber
  String eduPersonPrimaryOrgUnitDN
  String mailRoutingAddress
  Set<String> eduPersonEntitlement
  Set<String> title
  Set<String> eduPersonOrgUnitDN
  Set<String> objectClass
  String mydn
  String sukatVisibility
  String mobile
  Set<String> sukatPULAttributes
  String registeredAddress
  String roomNumber
  String description


  static constraints =
    {
      uid(nullable: false)
      displayName(nullable: true)
      mail(nullable: true)
      telephoneNumber(nullable: true)
      sn(nullable: true)
      cn(nullable: false)
      giveName(nullable: false)
      eduPersonAffiliation(nullable: true)
      socialSecurityNumber(nullable: false)
      eduPersonPrimaryOrgUnitDN(nullable: false)
      mailRoutingAddress(nullable: true)
      eduPersonEntitlement(nullable: true)
      title(nullable: true)
      eduPersonOrgUnitDN(nullable: true)
      sukatVisibility(nullable: true)
      mobile(nullable: true)
      sukatPULAttributes(nullable: true)
      registeredAddress(nullable: true)
      roomNumber(nullable: true)
      description(nullable: true)
    }
}