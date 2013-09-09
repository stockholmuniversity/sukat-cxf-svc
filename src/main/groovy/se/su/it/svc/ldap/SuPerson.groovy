/*
 * Copyright (c) 2013, IT Services, Stockholm University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Stockholm University nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter
import groovy.util.logging.Slf4j
import se.su.it.svc.commons.SvcSuPersonVO

/** GLDAPO schema class for SU employees and students also used by web service. */

@Slf4j
class SuPerson implements Serializable {

  static final long serialVersionUID = -687991492884005033L

  public enum Affilation {
    EMPLOYEE('employee', 40),
    STUDENT('student', 30),
    ALUMNI('alumni', 20),
    MEMBER('member', 10),
    OTHER('other', 0)

    private final String value
    private final int rank

    public Affilation(value, rank) {
      this.value = value
      this.rank = rank
    }

    public getValue() {
      return value
    }
  }

  // static final List<String> AFFILIATIONS = ['employee', 'student', 'alumni', 'member', 'other']

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
  String mail
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
  String loginShell
  String homeDirectory
  String gidNumber
  String uidNumber

  //TODO: Try to understand what this is & do something about it
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
    checkAndCorrectEmptyValues()
  }

  public SvcSuPersonVO getSvcSuPersonVO(){
    SvcSuPersonVO svcSuPersonVO = new SvcSuPersonVO(
      uid:                   uid,
      socialSecurityNumber:  socialSecurityNumber,
      givenName:             givenName,
      sn:                    sn,
      displayName:           displayName,
      registeredAddress:     registeredAddress,
      mail:                  mail,

      /** The user has an account in SUKAT that is not a stub.*/
      accountIsActive:  (objectClass?.contains('posixAccount')) ?: false
    )

    return svcSuPersonVO
  }

  /**
   * Set new mail, update mailLocalAddress
   *
   * @param mail the new mail addresses
   */
  public void setMail(String mail) {
    if (mail) {
      if (this.mailLocalAddress) {
        this.mailLocalAddress?.add(mail)
      }
      else{
        this.mailLocalAddress = [mail]
      }
    }

    this.mail = mail
  }

  /**
   * Set the mailLocalAddress & add 'inetLocalMailRecipient' objectClass
   *
   * @param mailLocalAddress the new mailLocalAddress
   */
  public void setMailLocalAddress(Set<String> mailLocalAddress) {
    this.mailLocalAddress = mailLocalAddress

    if (this.mailLocalAddress) {
      this.objectClass?.add("inetLocalMailRecipient")
    }
  }

  private boolean isEqualSets(Set<String> org, Set<String> mod) {
    if(org == null && mod == null) return true
    if(org != null && mod == null) return false
    if(org == null && mod != null) return false
    return org.equals(mod)
  }

  //TODO: Try to understand what this is & do something about it
  private void checkAndCorrectEmptyValues() {
    this.eduPersonPrimaryAffiliation = stringCheck(this.eduPersonPrimaryAffiliation)
    this.eduPersonAffiliation = setCheck(this.eduPersonAffiliation)
    this.eduPersonEntitlement = setCheck(this.eduPersonEntitlement)
    this.socialSecurityNumber = stringCheck(this.socialSecurityNumber)
    this.givenName = stringCheck(this.givenName)
    this.sn = stringCheck(this.sn)
    this.cn = stringCheck(this.cn)
    this.displayName = stringCheck(this.displayName)
    this.title = setCheck(this.title)
    this.roomNumber = setCheck(this.roomNumber)
    this.telephoneNumber = setCheck(this.telephoneNumber)
    this.mobile = stringCheck(this.mobile)
    this.sukatPULAttributes = setCheck(this.sukatPULAttributes)
    this.labeledURI = stringCheck(this.labeledURI)
    this.mail = stringCheck(this.mail)
    this.mailLocalAddress = setCheck(this.mailLocalAddress)
    this.sukatLOAFromDate = stringCheck(this.sukatLOAFromDate)
    this.sukatLOAToDate = stringCheck(this.sukatLOAToDate)
    this.eduPersonOrgUnitDN = setCheck(this.eduPersonOrgUnitDN)
    this.eduPersonPrimaryOrgUnitDN = stringCheck(this.eduPersonPrimaryOrgUnitDN)
    this.registeredAddress = stringCheck(this.registeredAddress)
    this.mailRoutingAddress = stringCheck(this.mailRoutingAddress)
    this.departmentNumber = stringCheck(this.departmentNumber)
    this.homeMobilePhone = stringCheck(this.homeMobilePhone)
    this.homePhone = stringCheck(this.homePhone)
    this.homePostalAddress = setCheck(this.homePostalAddress)
    this.homeLocalityName = stringCheck(this.homeLocalityName)
    this.homePostalCode = stringCheck(this.homePostalCode)
    this.description = stringCheck(this.description)
    this.sukatComment = stringCheck(this.sukatComment)
  }

  private String stringCheck(String aString) {
    if (aString != null && aString.trim().isEmpty()) {
      return null
    }
    return aString
  }

  private Set<String> setCheck(Set<String> aSet) {
    if (aSet == null) {
      return null
    }
    for (Iterator<String> itr = aSet.iterator(); itr.hasNext();) {
      String aString = itr.next()
      if (aString != null && aString.trim().isEmpty()) {
        itr.remove()
      }
    }
    return aSet
  }

  /**
   * Sets primary affiliation
   *
   * @param eduPersonPrimaryAffiliation the affiliation
   * @param suEnrollPerson the SuEnrollPerson
   */
  public void setAffiliations(String[] affiliations) throws IllegalArgumentException {
    log.debug "setAffiliations: Received affiliations ${affiliations?.join(', ')}"

    if (affiliations == null) {
      // TODO: See if we should be able to reset
      throw new IllegalArgumentException("Affiliations can't be null.")
    }

    def validAffiliations = Affilation*.value

    /* If an invalid affiliation is supplied we throw an IllegalArgumentException */
    if (!validAffiliations.containsAll(affiliations)) {
      affiliations.every { affiliation ->
        if (!validAffiliations.contains(affiliation)) {
          throw new IllegalArgumentException("Supplied affiliation $affiliation is invalid.")
        }
      }
    }

    objectClass.add("eduPerson")
    log.debug "setAffiliations: affiliations set to ${affiliations?.join(', ')}"
    eduPersonAffiliation = affiliations

    String primary = null

    for (targetAffiliation in Affilation.enumConstants) {
      if (affiliations.contains(targetAffiliation.value)) {
        primary = targetAffiliation.value
        break
      }
    }

    log.debug "setAffiliations: Primary affiliation set to $primary"
    eduPersonPrimaryAffiliation = primary
  }
}
