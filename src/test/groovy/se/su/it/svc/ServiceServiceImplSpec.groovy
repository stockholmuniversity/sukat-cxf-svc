package se.su.it.svc

import gldapo.GldapoSchemaRegistry
import org.gcontracts.PreconditionViolation
import org.springframework.ldap.core.DistinguishedName
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuService
import se.su.it.svc.ldap.SuServiceDescription
import se.su.it.svc.ldap.SuSubAccount

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

import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.query.SuServiceDescriptionQuery
import se.su.it.svc.query.SuServiceQuery
import se.su.it.svc.query.SuSubAccountQuery
import se.su.it.svc.util.AccountServiceUtils

import spock.lang.Shared
import spock.lang.Specification

class ServiceServiceImplSpec extends Specification {

    @Shared
    ServiceServiceImpl service

  def setup() {
    service = new ServiceServiceImpl()

    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
    SuService.metaClass.directory = "directory"
    SuService.metaClass.static.update = {->}
    SuService.metaClass.static.save = {->}
  }

  def cleanup() {
    AccountServiceUtils.metaClass = null
    SuPerson.metaClass = null
    SuService.metaClass = null
    SuPersonQuery.metaClass = null
    SuServiceQuery.metaClass = null
    SuServiceDescriptionQuery.metaClass = null
    SuSubAccountQuery.metaClass = null
    GldapoSchemaRegistry.metaClass = null
  }

    def "deleteService: happy path"()
    {
        setup:
        GroovyMock(AccountServiceUtils, global: true)
        GroovyMock(SuPersonQuery, global: true)
        GroovyMock(SuServiceQuery, global: true)
        GroovyMock(SuSubAccountQuery, global: true)

        def p = new SuPerson()
        p.metaClass.getDn = { "" }

        def s = GroovyMock(SuService) {
            asBoolean() >> true // https://github.com/spockframework/spock/issues/438
        }
        def sa = GroovyMock(SuSubAccount) {
            asBoolean() >> true // https://github.com/spockframework/spock/issues/438
            getUid() >> 'dsuser.jabber'
        }

        when:
        service.deleteService('dsuser', 'jabber')

        then:
        1 * AccountServiceUtils.deleteSubAccount(*_)
        1 * SuPersonQuery.getSuPersonFromUID(*_) >> p
        1 * SuServiceQuery.getSuServiceByType(*_) >> s
        1 * SuSubAccountQuery.getSuSubAccounts(*_) >> [sa]
        1 * s.delete()
        1 * sa.delete()
    }

    def "deleteService: unknown service type"()
    {
        when:
        service.deleteService('dsuser', 'pabber')

        then:
        thrown(IllegalArgumentException)
    }

    def "deleteService: SUKAT information has already been removed"()
    {
        setup:
        GroovyMock(AccountServiceUtils, global: true)
        GroovyMock(SuPersonQuery, global: true)
        GroovyMock(SuServiceQuery, global: true)
        GroovyMock(SuSubAccountQuery, global: true)

        def p = new SuPerson()
        p.metaClass.getDn = { "" }

        when:
        service.deleteService('dsuser', 'jabber')

        then:
        1 * AccountServiceUtils.deleteSubAccount(*_)
        1 * SuPersonQuery.getSuPersonFromUID(*_) >> p
        1 * SuServiceQuery.getSuServiceByType(*_)
        1 * SuSubAccountQuery.getSuSubAccounts(*_)
        0 * _.delete()
    }

    def "deleteService: other SUKAT subaccount"()
    {
        setup:
        GroovyMock(AccountServiceUtils, global: true)
        GroovyMock(SuPersonQuery, global: true)
        GroovyMock(SuServiceQuery, global: true)
        GroovyMock(SuSubAccountQuery, global: true)

        def p = new SuPerson()
        p.metaClass.getDn = { "" }

        def sa = GroovyMock(SuSubAccount) {
            asBoolean() >> true // https://github.com/spockframework/spock/issues/438
            getUid() >> 'dsuser.other'
        }

        when:
        service.deleteService('dsuser', 'jabber')

        then:
        1 * AccountServiceUtils.deleteSubAccount(*_)
        1 * SuPersonQuery.getSuPersonFromUID(*_) >> p
        1 * SuServiceQuery.getSuServiceByType(*_)
        1 * SuSubAccountQuery.getSuSubAccounts(*_) >> [sa]
        0 * _.delete()
    }

  def "Test getServices with null uid argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.getServices(null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test getServices returns list of SuCard when person exists"() {
    setup:
    def person = new SuPerson()
    person.metaClass.getDn = { "" }

    def serviceServiceImpl = new ServiceServiceImpl()

    GroovyMock(SuServiceQuery, global:true)
    SuServiceQuery.getSuServices(*_) >> { return [new SuService()] }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> person }

    when:
    def ret = serviceServiceImpl.getServices("testuid")

    then:
    ret.size() == 1
    ret[0] instanceof SuService
  }

  def "Test getServices returns empty list of SuCard when person exists"() {
    setup:
    def person = new SuPerson()
    person.metaClass.getDn = { "" }

    def serviceServiceImpl = new ServiceServiceImpl()

    GroovyMock(SuServiceQuery, global:true)
    SuServiceQuery.getSuServices(*_) >> { return [] }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> person }

    when:
    def ret = serviceServiceImpl.getServices("testuid")

    then:
    ret.size() == 0
  }

  def "Test getServices returns exception when person dont exists"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> throw new IllegalArgumentException("foo") }
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.getServices("testuid")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test enableServiceFully with null uid argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.enableServiceFully(null, "urn:x-su:service:type:jabber", "jabber", "A description")

    then:
    thrown(PreconditionViolation)
  }

  def "Test enableServiceFully with null serviceType argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.enableServiceFully("testuid", null, "jabber", "A description")

    then:
    thrown(PreconditionViolation)
  }

  def "Test enableServiceFully with null qualifier argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", null, "A description")

    then:
    thrown(PreconditionViolation)
  }

  def "Test enableServiceFully with null description argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", "jabber", null)

    then:
    thrown(PreconditionViolation)
  }

  def "enableServiceFully: description is set"()
  {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuSubAccountQuery.metaClass.static.getSuSubAccounts = {String directory, DistinguishedName dn -> return}
    SuSubAccount.metaClass.setParent = {String dn -> return void}
    SuSubAccountQuery.metaClass.static.createSubAccount = {String a, SuSubAccount b -> return void}
    AccountServiceUtils.metaClass.static.getSubAccount = { String a, String b -> [uid: 'esfTest'] }
    SuServiceQuery.metaClass.static.getSuServiceByType = { String a, DistinguishedName b, String c -> return new SuService(suServiceStatus: 'esfTest') }

    when:
    def ret = serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", "jabber", 'A description')

    then:
    ret.suServiceStatus == 'enabled'
  }

  def "enableServiceFully: subAccount does not exist"()
  {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuSubAccountQuery.metaClass.static.getSuSubAccounts = {String directory, DistinguishedName dn -> return}
    SuSubAccount.metaClass.setParent = {String dn -> return void}
    SuSubAccountQuery.metaClass.static.createSubAccount = {String a, SuSubAccount b -> return void}
    AccountServiceUtils.metaClass.static.getSubAccount = { String a, String b -> [:] }
    AccountServiceUtils.metaClass.static.createSubAccount = { String a, String b -> }
    SuServiceQuery.metaClass.static.getSuServiceByType = { String a, DistinguishedName b, String c -> return new SuService(suServiceStatus: 'esfTest') }

    when:
    def ret = serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", "jabber", '')

    then:
    ret.suServiceStatus == 'enabled'
  }

  def "Test enableServiceFully returns exception when person dont exists"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> throw new IllegalArgumentException("foo") }
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", "jabber", "A description")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test enableServiceFully with qualifier" () {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuSubAccountQuery.metaClass.static.getSuSubAccounts = {String directory, org.springframework.ldap.core.DistinguishedName dn -> return [new SuSubAccount(uid: "test2uid.jabber"),new SuSubAccount(uid: "testuid.jabber")]}
    SuSubAccountQuery.metaClass.static.createSubAccount = {String directory, SuSubAccount -> return void}
    AccountServiceUtils.metaClass.static.getSubAccount = { String a, String b -> [uid: 'esfTest'] }
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return null}
    SuService.metaClass.setParent = {String dn -> return void}
    SuServiceQuery.metaClass.static.createService = {String directory, SuService suService -> return void}
    SuServiceQuery.metaClass.static.saveSuService = {SuService suService -> return void}
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    def ret = serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", "jabber", "A description")

    then:
    ret.roleOccupant.startsWith("uid=testuid.jabber") == true
  }

  def "Test enableServiceFully with blocked service status" () {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuSubAccountQuery.metaClass.static.getSuSubAccounts = {String directory, org.springframework.ldap.core.DistinguishedName dn -> return [new SuSubAccount(uid: "test2uid.jabber"),new SuSubAccount(uid: "testuid.jabber")]}
    SuSubAccountQuery.metaClass.static.createSubAccount = {String directory, SuSubAccount -> return void}
    AccountServiceUtils.metaClass.static.getSubAccount = { String a, String b -> [uid: 'esfTest'] }
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return new SuService(suServiceStatus: "blocked")}
    SuService.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("cn=1234-abcd-567-efgh-890,uid=testuid,dc=it,dc=su,dc=se")}
    SuService.metaClass.setParent = {String dn -> return void}
    SuServiceQuery.metaClass.static.createService = {String directory, SuService suService -> return void}
    SuServiceQuery.metaClass.static.saveSuService = {SuService suService -> return void}
    def serviceServiceImpl = new ServiceServiceImpl()
    when:
    serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", "jabber", "A description")
    then:
    thrown(IllegalArgumentException)
  }

  def "Test enableServiceFully with locked service status" () {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> new SuPerson(uid: "testuid") }
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuSubAccountQuery.metaClass.static.getSuSubAccounts = {String directory, org.springframework.ldap.core.DistinguishedName dn -> return [new SuSubAccount(uid: "test2uid.jabber"),new SuSubAccount(uid: "testuid.jabber")]}
    SuSubAccountQuery.metaClass.static.createSubAccount = {String directory, SuSubAccount -> return void}
    AccountServiceUtils.metaClass.static.getSubAccount = { String a, String b -> [uid: 'esfTest'] }
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return new SuService(suServiceStatus: "locked")}
    SuService.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("cn=1234-abcd-567-efgh-890,uid=testuid,dc=it,dc=su,dc=se")}
    SuService.metaClass.setParent = {String dn -> return void}
    SuServiceQuery.metaClass.static.createService = {String directory, SuService suService -> return void}
    SuServiceQuery.metaClass.static.saveSuService = {SuService suService -> return void}
    def serviceServiceImpl = new ServiceServiceImpl()
    when:
    serviceServiceImpl.enableServiceFully("testuid", "urn:x-su:service:type:jabber", "jabber", "A description")
    then:
    thrown(IllegalArgumentException)
  }

  def "Test blockService with null uid argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.blockService(null, "urn:x-su:service:type:jabber")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test blockService with null serviceType argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.blockService("testuid", null)

    then:
    thrown(IllegalArgumentException)
  }

  def "Test blockService no service found"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> new SuPerson(uid: "testuid") }
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return null}

    when:
    serviceServiceImpl.blockService("testuid", "urn:x-su:service:type:jabber")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test blockService already blocked"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> new SuPerson(uid: "testuid") }
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return new SuService(suServiceStatus: "locked")}

    when:
    serviceServiceImpl.blockService("testuid", "urn:x-su:service:type:jabber")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test blockService"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> new SuPerson(uid: "testuid") }
    String serviceStatus = null
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return new SuService(suServiceStatus: "enabled")}
    SuService.metaClass.update = { ->
      serviceStatus = delegate.suServiceStatus
    }

    when:
    serviceServiceImpl.blockService("testuid", "urn:x-su:service:type:jabber")

    then:
    serviceStatus == "blocked"
  }

  def "Test unblockService with null uid argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.unblockService(null, "urn:x-su:service:type:jabber")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test unblockService with null serviceType argument"() {
    setup:
    def serviceServiceImpl = new ServiceServiceImpl()

    when:
    serviceServiceImpl.unblockService("testuid", null)

    then:
    thrown(IllegalArgumentException)
  }

  def "Test unblockService no service found"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> new SuPerson(uid: "testuid") }
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return null}

    when:
    serviceServiceImpl.unblockService("testuid", "urn:x-su:service:type:jabber")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test unblockService without opt-in"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> new SuPerson(uid: "testuid") }
    String serviceStatus = null
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return new SuService(suServiceStatus: "enabled")}
    SuServiceDescriptionQuery.metaClass.static.getSuServiceDescriptions = {String directory -> [new SuServiceDescription(suServiceType: "urn:x-su:service:type:jabber")]}
    SuService.metaClass.update = { ->
      serviceStatus = delegate.suServiceStatus
    }

    when:
    serviceServiceImpl.unblockService("testuid", "urn:x-su:service:type:jabber")

    then:
    serviceStatus == "enabled"
  }

  def "Test unblockService with opt-in"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> new SuPerson(uid: "testuid") }
    String serviceStatus = null
    def serviceServiceImpl = new ServiceServiceImpl()
    SuPerson.metaClass.getDn = {return new org.springframework.ldap.core.DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}
    SuServiceQuery.metaClass.static.getSuServiceByType = {String directory, org.springframework.ldap.core.DistinguishedName dn, String serviceType -> return new SuService(suServiceStatus: "enabled")}
    SuServiceDescriptionQuery.metaClass.static.getSuServiceDescriptions = {String directory -> [new SuServiceDescription(suServiceType: "urn:x-su:service:type:jabber", suServicePolicy: "urn:x-su:service:policy:opt-in")]}
    SuService.metaClass.update = { ->
      serviceStatus = delegate.suServiceStatus
    }

    when:
    serviceServiceImpl.unblockService("testuid", "urn:x-su:service:type:jabber")

    then:
    serviceStatus == "disabled"
  }

}
