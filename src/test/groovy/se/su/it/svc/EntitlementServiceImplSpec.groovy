package se.su.it.svc

import gldapo.GldapoSchemaRegistry
import org.gcontracts.PreconditionViolation
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.query.SuPersonQuery
import spock.lang.Specification

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
/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-03
 * Time: 08:34
 * To change this template use File | Settings | File Templates.
 */
class EntitlementServiceImplSpec extends Specification {

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
  }

  def cleanup() {
    SuPersonQuery.metaClass = null
    GldapoSchemaRegistry.metaClass = null
  }

  def "Test addEntitlement with null uid argument"() {
    setup:
    def entitlementServiceImpl = new EntitlementServiceImpl()

    when:
    entitlementServiceImpl.addEntitlement(null,"urn:mace:swami.se:gmai:test:test")

    then:
    thrown(PreconditionViolation)
  }

  def "Test addEntitlement with null entitlement argument"() {
    setup:
    def entitlementServiceImpl = new EntitlementServiceImpl()

    when:
    entitlementServiceImpl.addEntitlement("testuid",null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test addEntitlement when person dont exists"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String directory, String uid -> throw new IllegalArgumentException("foo") }
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    entitlementServiceImpl.addEntitlement("testuid","urn:mace:swami.se:gmai:test:test")
    then:
    thrown(IllegalArgumentException)
  }

  def "Test addEntitlement whith duplicate entitlement"() {
    setup:
    SuPerson person = new SuPerson()
    def tmpSet = new java.util.LinkedHashSet<String>()
    tmpSet.add("urn:mace:swami.se:gmai:test:test")
    person.eduPersonEntitlement = tmpSet
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    SuPersonQuery.metaClass.static.saveSuPerson = {SuPerson arg1 -> return void}
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    entitlementServiceImpl.addEntitlement("testuid","urn:mace:swami.se:gmai:test:test")
    then:
    thrown(IllegalArgumentException)
  }

  def "Test addEntitlement"() {
    setup:
    SuPerson person = new SuPerson()
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    SuPersonQuery.metaClass.static.updateSuPerson = {SuPerson arg1 -> return void}
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    entitlementServiceImpl.addEntitlement("testuid","urn:mace:swami.se:gmai:test:test")
    then:
    person.eduPersonEntitlement.contains("urn:mace:swami.se:gmai:test:test") == true
  }

  def "Test removeEntitlement with null uid argument"() {
    setup:
    def entitlementServiceImpl = new EntitlementServiceImpl()

    when:
    entitlementServiceImpl.removeEntitlement(null,"urn:mace:swami.se:gmai:test:test")

    then:
    thrown(PreconditionViolation)
  }

  def "Test removeEntitlement with null entitlement argument"() {
    setup:
    def entitlementServiceImpl = new EntitlementServiceImpl()

    when:
    entitlementServiceImpl.removeEntitlement("testuid",null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test removeEntitlement when person dont exists"() {
    setup:
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> throw new IllegalArgumentException("foo") }

    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    entitlementServiceImpl.removeEntitlement("testuid","urn:mace:swami.se:gmai:test:test")
    then:
    thrown(IllegalArgumentException)
  }

  def "Test removeEntitlement with no eduPersonEntitlement list in person object"() {
    setup:
    SuPerson person = new SuPerson()
    person.eduPersonEntitlement = null

    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> person }

    def entitlementServiceImpl = new EntitlementServiceImpl()

    when:
    entitlementServiceImpl.removeEntitlement("testuid","urn:mace:swami.se:gmai:test:test")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test removeEntitlement with no same entitlement in list"() {
    setup:

    SuPerson suPerson = new SuPerson()
    suPerson.eduPersonEntitlement = new LinkedHashSet<String>(["urn:mace:swami.se:gmai:test:test"])

    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> suPerson }

    def entitlementServiceImpl = new EntitlementServiceImpl()

    when:
    entitlementServiceImpl.removeEntitlement("testuid","urn:mace:swami.se:gmai:test:imnotthere")

    then:
    thrown(IllegalArgumentException)
  }

  def "Test removeEntitlement"() {
    setup:
    SuPerson person = new SuPerson()
    def tmpSet = new java.util.LinkedHashSet<String>()
    tmpSet.add("urn:mace:swami.se:gmai:test:test")
    person.eduPersonEntitlement = tmpSet
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    SuPersonQuery.metaClass.static.updateSuPerson = {SuPerson arg1 -> return void}
    def entitlementServiceImpl = new EntitlementServiceImpl()
    when:
    entitlementServiceImpl.removeEntitlement("testuid","urn:mace:swami.se:gmai:test:test")
    then:
    person.eduPersonEntitlement.contains("urn:mace:swami.se:gmai:test:test") == false
  }
}
