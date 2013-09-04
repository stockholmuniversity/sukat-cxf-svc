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

package se.su.it.svc.commons

import javax.xml.bind.annotation.*

@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
public class SvcSuPersonVO implements Serializable{
  static final long serialVersionUID = -687991492884235073L
  @XmlAttribute
  String uid
  @XmlAttribute
  String eduPersonPrimaryAffiliation
  @XmlElement(name="eduPersonAffiliation")
  Set<String> eduPersonAffiliation
  @XmlElement(name="eduPersonEntitlement")
  Set<String> eduPersonEntitlement
  @XmlAttribute
  String socialSecurityNumber
  @XmlAttribute
  String givenName
  @XmlAttribute
  String sn
  @XmlAttribute
  String displayName
  @XmlElement(name="title")
  Set<String> title
  @XmlElement(name="roomNumber")
  Set<String> roomNumber
  @XmlElement(name="telephoneNumber")
  Set<String> telephoneNumber
  @XmlAttribute
  String mobile
  @XmlElement(name="sukatPULAttributes")
  Set<String> sukatPULAttributes //Adress (hem),Fax (hem),Hemsida (privat/hem),Mail (privat/hem),Mobil,Mobil (privat/hem),Stad (hem),Telefon (privat/hem)
  @XmlAttribute
  String labeledURI //hemsida
  @XmlElement(name="mail")
  Set<String> mail
  @XmlElement(name="mailLocalAddress")
  Set<String> mailLocalAddress
  @XmlAttribute
  String sukatLOAFromDate //Tjänstledighet börjar
  @XmlAttribute
  String sukatLOAToDate   //Tjänstledighet slutar
  @XmlElement(name="eduPersonOrgUnitDN")
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
  @XmlElement(name="homePostalAddress")
  Set<String> homePostalAddress
  @XmlAttribute
  String homeLocalityName
  @XmlAttribute
  String homePostalCode
  @XmlAttribute
  String description
  @XmlAttribute
  String sukatComment
  @XmlAttribute
  Boolean accountIsActive
}
