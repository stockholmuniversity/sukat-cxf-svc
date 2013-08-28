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

import gldapo.schema.annotation.GldapoSchemaFilter
import gldapo.schema.annotation.GldapoNamingAttribute
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlRootElement
import org.springframework.ldap.core.DistinguishedName
import gldapo.schema.annotation.GldapoSynonymFor
import javax.xml.bind.annotation.XmlElement

/**
 * GLDAPO schema class for SU Service also used by web service.
 */
@XmlAccessorType( XmlAccessType.NONE )
@XmlRootElement
class SuService implements Serializable {

  static final long serialVersionUID = -687991492884005133L;

  @GldapoSchemaFilter("(objectClass=suServiceObject)")

  @GldapoNamingAttribute
  @XmlAttribute
  String cn

  @GldapoSynonymFor("owner")
  @XmlAttribute
  String myowner
  @XmlAttribute
  String suServiceType
  @XmlAttribute
  String suServiceStatus
  @XmlAttribute
  String suServiceStartTime
  @XmlAttribute
  String roleOccupant
  @XmlElement(name="objectClass")
  Set<String> objectClass
}
