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




import gldapo.GldapoSchemaRegistry
import se.su.it.commons.Kadmin
import se.su.it.svc.ServiceServiceImpl
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuService
import se.su.it.svc.ldap.SuServiceDescription
import se.su.it.svc.ldap.SuSubAccount
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.query.SuServiceDescriptionQuery
import se.su.it.svc.query.SuServiceQuery
import se.su.it.svc.query.SuSubAccountQuery
import spock.lang.Specification

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-04
 * Time: 11:26
 * To change this template use File | Settings | File Templates.
 */
class ServiceServiceImplTest extends Specification {

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
  }

  def cleanup() {
    SuPerson.metaClass = null
    SuPersonQuery.metaClass = null
    SuService.metaClass = null
    SuServiceQuery.metaClass = null
    SuServiceDescriptionQuery.metaClass
    SuSubAccountQuery.metaClass = null
    Kadmin.metaClass = null
    GldapoSchemaRegistry.metaClass = null
  }

  def "Test getServices with null uid argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.getServices(null,new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  def "Test getServices with null SvcAudit argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.getServices("testuid",null)

    then:
    thrown(IllegalArgumentException)
  }

  def "Test getServices returns list of SuCard when person exists"() {
    setup:
    def person = new SuPerson()
    def suServices = [new SuService()]
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    person.metaClass.getDn = {""}
    SuServiceQuery.metaClass.static.getSuServices = {String directory,String dn -> return suServices}
    def serviceServiceImpl = new ServiceServiceImpl()
    when:
    def ret = serviceServiceImpl.getServices("testuid",new SvcAudit())
    then:
    ret.size() == 1
    ret[0] instanceof SuService
  }

  def "Test getServices returns empty list of SuCard when person exists"() {
    setup:
    def person = new SuPerson()
    def suServices = []
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    person.metaClass.getDn = {""}
    SuServiceQuery.metaClass.static.getSuServices = {String directory,String dn -> return suServices}
    def serviceServiceImpl = new ServiceServiceImpl()
    when:
    def ret = serviceServiceImpl.getServices("testuid",new SvcAudit())
    then:
    ret.size() == 0
  }

  def "Test getServices returns exception when person dont exists"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def serviceServiceImpl = new ServiceServiceImpl()
    when:
    def ret = serviceServiceImpl.getServices("testuid",new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  def "Test getServiceTemplates with null SvcAudit argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.getServiceTemplates(null)

    then:
    thrown(IllegalArgumentException)
  }

  def "Test enableServiceFully with null uid argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.enableServiceFully(null, "urn:x-su:service:type:jabber", "jabber", "A description", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  def "Test enableServiceFully with null serviceType argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.enableServiceFully("testuid", null, "jabber", "A description", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  def "Test enableServiceFully with null qualifier argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", null, "A description", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  def "Test enableServiceFully with null description argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", "jabber", null, new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  def "Test enableServiceFully with null SvcAudit argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", "jabber", "A description",null)

    then:
    thrown(IllegalArgumentException)
  }

  def "Test enableServiceFully returns exception when person dont exists"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def serviceServiceImpl = new ServiceServiceImpl()
    when:
    def ret = serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", "jabber", "A description", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  def "Test enableServiceFully with qualifier" () {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuSubAccountQuery.metaClass.static.getSuSubAccounts = {String directory, org.springframework.ldap.core.DistinguishedName dn -> return [new SuSubAccount(uid: "test2uid.jabber"),new SuSubAccount(uid: "testuid.jabber")]}
    SuSubAccountQuery.metaClass.static.createSubAccount = {String directory, SuSubAccount -> return void}
    Kadmin.metaClass.resetOrCreatePrincipal = {String subUid -> return void}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return null}
    SuService.metaClass.setParent = {String dn -> return void}
    SuServiceQuery.metaClass.static.createService = {String directory, SuService suService -> return void}
    SuServiceQuery.metaClass.static.saveSuService = {SuService suService -> return void}
    def serviceServiceImpl = new ServiceServiceImpl()
    when:
    def ret = serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", "jabber", "A description", new SvcAudit())
    then:
    ret.roleOccupant.startsWith("uid=testuid.jabber") == true
  }

  def "Test enableServiceFully with blocked service status" () {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuSubAccountQuery.metaClass.static.getSuSubAccounts = {String directory, org.springframework.ldap.core.DistinguishedName dn -> return [new SuSubAccount(uid: "test2uid.jabber"),new SuSubAccount(uid: "testuid.jabber")]}
    SuSubAccountQuery.metaClass.static.createSubAccount = {String directory, SuSubAccount -> return void}
    Kadmin.metaClass.resetOrCreatePrincipal = {String subUid -> return void}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return new SuService(suServiceStatus: "blocked")}
    SuService.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("cn=1234-abcd-567-efgh-890,uid=testuid,dc=it,dc=su,dc=se")}
    SuService.metaClass.setParent = {String dn -> return void}
    SuServiceQuery.metaClass.static.createService = {String directory, SuService suService -> return void}
    SuServiceQuery.metaClass.static.saveSuService = {SuService suService -> return void}
    def serviceServiceImpl = new ServiceServiceImpl()
    when:
    def ret = serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", "jabber", "A description", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  def "Test enableServiceFully with locked service status" () {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuSubAccountQuery.metaClass.static.getSuSubAccounts = {String directory, org.springframework.ldap.core.DistinguishedName dn -> return [new SuSubAccount(uid: "test2uid.jabber"),new SuSubAccount(uid: "testuid.jabber")]}
    SuSubAccountQuery.metaClass.static.createSubAccount = {String directory, SuSubAccount -> return void}
    Kadmin.metaClass.resetOrCreatePrincipal = {String subUid -> return void}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return new SuService(suServiceStatus: "locked")}
    SuService.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("cn=1234-abcd-567-efgh-890,uid=testuid,dc=it,dc=su,dc=se")}
    SuService.metaClass.setParent = {String dn -> return void}
    SuServiceQuery.metaClass.static.createService = {String directory, SuService suService -> return void}
    SuServiceQuery.metaClass.static.saveSuService = {SuService suService -> return void}
    def serviceServiceImpl = new ServiceServiceImpl()
    when:
    def ret = serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", "jabber", "A description", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  def "Test blockService with null uid argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.blockService(null, "urn:x-su:service:type:jabber", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  def "Test blockService with null serviceType argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.blockService("testuid", null, new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  def "Test blockService with null SvcAudit argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.blockService("testuid", "urn:x-su:service:type:jabber", null)

    then:
    thrown(IllegalArgumentException)
  }

  def "Test blockService no service found"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return null}

    when:
    serviceServiceImpl.blockService("testuid", "urn:x-su:service:type:jabber", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }


  def "Test blockService already blocked"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return new SuService(suServiceStatus: "locked")}

    when:
    serviceServiceImpl.blockService("testuid", "urn:x-su:service:type:jabber", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  def "Test blockService"() {
    setup:
    String serviceStatus = null
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return new SuService(suServiceStatus: "enabled")}
    SuServiceQuery.metaClass.static.saveSuService = {SuService suService ->
      serviceStatus = suService.suServiceStatus
      return void
    }

    when:
    serviceServiceImpl.blockService("testuid", "urn:x-su:service:type:jabber", new SvcAudit())

    then:
    serviceStatus == "blocked"
  }

  def "Test unblockService with null uid argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.unblockService(null, "urn:x-su:service:type:jabber", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  def "Test unblockService with null serviceType argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.unblockService("testuid", null, new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  def "Test unblockService with null SvcAudit argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.unblockService("testuid", "urn:x-su:service:type:jabber", null)

    then:
    thrown(IllegalArgumentException)
  }

  def "Test unblockService no service found"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return null}

    when:
    serviceServiceImpl.unblockService("testuid", "urn:x-su:service:type:jabber", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  def "Test unblockService without opt-in"() {
    setup:
    String serviceStatus = null
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return new SuService(suServiceStatus: "enabled")}
    SuServiceDescriptionQuery.metaClass.static.getSuServiceDescriptions = {String directory -> [new SuServiceDescription(suServiceType: "urn:x-su:service:type:jabber")]}
    SuServiceQuery.metaClass.static.saveSuService = {SuService suService ->
      serviceStatus = suService.suServiceStatus
      return void
    }

    when:
    serviceServiceImpl.unblockService("testuid", "urn:x-su:service:type:jabber", new SvcAudit())

    then:
    serviceStatus == "enabled"
  }

  def "Test unblockService with opt-in"() {
    setup:
    String serviceStatus = null
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return new SuService(suServiceStatus: "enabled")}
    SuServiceDescriptionQuery.metaClass.static.getSuServiceDescriptions = {String directory -> [new SuServiceDescription(suServiceType: "urn:x-su:service:type:jabber", suServicePolicy: "urn:x-su:service:policy:opt-in")]}
    SuServiceQuery.metaClass.static.saveSuService = {SuService suService ->
      serviceStatus = suService.suServiceStatus
      return void
    }

    when:
    serviceServiceImpl.unblockService("testuid", "urn:x-su:service:type:jabber", new SvcAudit())

    then:
    serviceStatus == "disabled"
  }

}
