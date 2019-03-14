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
import gldapo.schema.annotation.GldapoSynonymFor

import groovy.util.logging.Slf4j

import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.commons.SvcUidPwd

import se.su.it.svc.util.GeneralUtils

/** GLDAPO schema class for SU employees and students also used by web service. */

@Slf4j
@GldapoSchemaFilter("(objectClass=suPerson)")
class SuPerson implements Serializable {

  static final long serialVersionUID = -687991492884005033L

  public static enum Affilation {
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

  static final List<String> AFFILIATIONS = Affilation.enumConstants*.value

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
  String title
  @GldapoSynonymFor('title;lang-en')
  String title_en
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
  String homePostalAddress
  String homeLocalityName
  String homePostalCode
  String homeCountry
  String description
  String sukatComment
  String loginShell
  String homeDirectory
  String gidNumber
  String uidNumber

  /**
   * Create a SvcSuPersonVO filled with property values from this SuPerson
   *
   * @return a new SvcSuPersonVO
   */
  public SvcSuPersonVO createSvcSuPersonVO() {
    SvcSuPersonVO svcSuPersonVO = new SvcSuPersonVO()
    GeneralUtils.copyProperties(this, svcSuPersonVO)

    svcSuPersonVO.accountIsActive = (objectClass?.contains('posixAccount')) ?: false

    return svcSuPersonVO
  }

  /**
   * Update properties from a SvcSuPersonVO
   *
   * @param svcSuPersonVO the VO containing the properties
   */
  public void updateFromSvcSuPersonVO(SvcSuPersonVO svcSuPersonVO) {
    GeneralUtils.copyProperties(svcSuPersonVO, this)
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
      } else {
        this.mailLocalAddress = [mail]
      }
    }

    this.mail = mail
  }

  /**
   * Set the mailRoutingAddress & add 'inetLocalMailRecipient' objectClass
   *
   * @param mailRoutingAddress the new mailRoutingAddress
   */
  public void setMailRoutingAddress(String mailRoutingAddress) {
    this.mailRoutingAddress = mailRoutingAddress

    if (this.mailRoutingAddress) {
      this.objectClass?.add("inetLocalMailRecipient")
    }
  }
}
