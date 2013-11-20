package se.su.it.svc

import gldapo.GldapoSchemaRegistry
import org.gcontracts.PreconditionViolation
import org.springframework.ldap.core.DistinguishedName
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuRole
import se.su.it.svc.query.SuPersonQuery

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

import se.su.it.svc.query.SuRoleQuery
import spock.lang.Specification

class RoleServiceImplSpec extends Specification {

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
    SuRole.metaClass.update = {->}
  }

  def cleanup() {
    SuRole.metaClass = null
    SuRoleQuery.metaClass = null
    SuPersonQuery.metaClass = null
    GldapoSchemaRegistry.metaClass = null
  }

  def "Test addUidToRoles with null uid argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()

    when:
    roleServiceImpl.addUidToRoles(null,["dummyDN"])

    then:
    thrown(PreconditionViolation)
  }

  def "Test addUidToRoles with null roleDNList argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()

    when:
    roleServiceImpl.addUidToRoles("testuid",null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test addUidToRoles with empty roleDNList argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()

    when:
    roleServiceImpl.addUidToRoles("testuid",[])

    then:
    thrown(PreconditionViolation)
  }

  def "Test addUidToRoles without person exist"() {
    setup:
    def myRoles = ["cn=Test1,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE",
      "cn=Test2,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE"]
    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> throw new IllegalArgumentException("foo") }
    def roleServiceImpl = new RoleServiceImpl()

    when:
    roleServiceImpl.addUidToRoles("testuid", myRoles)

    then:
    thrown(IllegalArgumentException)
  }

  def "Test addUidToRoles with person exist"() {
    setup:
    String saved = ""
    SuPerson person = new SuPerson(uid: "testuid")
    SuRole suRole = new SuRole()
    suRole.cn = "Test1"
    suRole.roleOccupant = ["uid=dummy,dc=it,dc=su,dc=se"]
    SuRole suRole2 = new SuRole()
    suRole2.cn = "Test2"
    suRole2.roleOccupant = ["uid=dummy,dc=it,dc=su,dc=se","uid=testuid, dc=it, dc=su, dc=se"]

    def myRoles = [
        "cn=Test1,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE",
      "cn=Test2,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE"]

    person.metaClass.getDn = { "uid=testuid,dc=it,dc=su,dc=se" }

    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> person }

    /**
    SuRoleQuery.metaClass.static.getSuRoleFromDN = { String directory, String roleDN ->
      if (roleDN.startsWith("cn=Test1")) return suRole;
      if (roleDN.startsWith("cn=Test2")) return suRole2;
    }
    */

    GroovyMock(SuRoleQuery, global:true)
    SuRoleQuery.getSuRoleFromDN(_,_) >> { String arg1, String roleDN ->
      if (roleDN.startsWith("cn=Test1")) return suRole;
      if (roleDN.startsWith("cn=Test2")) return suRole2;
    }

    def roleServiceImpl = new RoleServiceImpl()

    SuRole.metaClass.update {-> saved = delegate.cn }

    when:
    roleServiceImpl.addUidToRoles("testuid", myRoles)

    then:
    saved == "Test1"
    suRole.roleOccupant.size() == 2
    suRole2.roleOccupant.size() == 2
  }

  def "Test removeUidFromRoles with null uid argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()

    when:
    roleServiceImpl.removeUidFromRoles(null,["dummyDN"])

    then:
    thrown(PreconditionViolation)
  }

  def "Test removeUidFromRoles with null roleDNList argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()

    when:
    roleServiceImpl.removeUidFromRoles("testuid",null)

    then:
    thrown(PreconditionViolation)
  }

  def "Test removeUidFromRoles with empty roleDNList argument"() {
    setup:
    def roleServiceImpl = new RoleServiceImpl()

    when:
    roleServiceImpl.removeUidFromRoles("testuid",[])

    then:
    thrown(PreconditionViolation)
  }

  def "Test removeUidFromRoles without person exist"() {
    setup:
    def myRoles = ["cn=Test1,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE",
      "cn=Test2,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE"]

    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> throw new IllegalArgumentException("foo") }

    def roleServiceImpl = new RoleServiceImpl()

    when:
    roleServiceImpl.removeUidFromRoles("testuid", myRoles)

    then:
    thrown(IllegalArgumentException)
  }

  def "Test removeUidFromRoles with person exist"() {
    setup:
    String saved = ""
    SuPerson person = new SuPerson(uid: "testuid")
    SuRole suRole = new SuRole()
    suRole.cn = "Test1"
    suRole.roleOccupant = ["uid=dummy,dc=it,dc=su,dc=se"]
    SuRole suRole2 = new SuRole()
    suRole2.cn = "Test2"
    suRole2.roleOccupant = ["uid=dummy,dc=it,dc=su,dc=se","uid=testuid, dc=it, dc=su, dc=se"]
    def myRoles = ["cn=Test1,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE",
      "cn=Test2,ou=Team Utveckling,ou=Systemsektionen,ou=Avdelningen för IT och media,ou=Universitetsförvaltningen,o=Stockholms universitet,c=SE"]
    person.metaClass.getDn = {new DistinguishedName("uid=testuid,dc=it,dc=su,dc=se")}

    SuPersonQuery.metaClass.static.getSuPersonFromUID = { String a, String b -> person }

    GroovyMock(SuRoleQuery, global:true)
    SuRoleQuery.getSuRoleFromDN(_,_) >> { arg1, roleDN ->
      if(roleDN.startsWith("cn=Test1")) return suRole;
      if(roleDN.startsWith("cn=Test2")) return suRole2;
    }
    SuRole.metaClass.update {-> saved = delegate.cn }
    def roleServiceImpl = new RoleServiceImpl()

    when:
    roleServiceImpl.removeUidFromRoles("testuid", myRoles)

    then:
    saved == "Test2"
    suRole.roleOccupant.size() == 1
    suRole2.roleOccupant.size() == 1
  }
}
