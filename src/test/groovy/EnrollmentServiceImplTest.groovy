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
import org.gcontracts.PreconditionViolation
import org.junit.After
import org.junit.Before
import org.junit.Test
import se.su.it.commons.Kadmin
import se.su.it.svc.EnrollmentServiceImpl
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.util.EnrollmentServiceUtils
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-13
 * Time: 08:36
 * To change this template use File | Settings | File Templates.
 */
class EnrollmentServiceImplTest extends Specification {
  @Shared
  EnrollmentServiceImpl service

  @Before
  def setup() {
    this.service = new EnrollmentServiceImpl()
  }

  @After
  def cleanup() {
    this.service = null
    EnrollmentServiceImpl.metaClass = null
    EnrollmentServiceUtils.metaClass = null
  }

  @Test
  def "Test resetAndExpirePwd with null uid argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.resetAndExpirePwd(null, new SvcAudit())
    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test resetAndExpirePwd with null SvcAudit argument"() {
    setup:
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.resetAndExpirePwd("testuid", null)
    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "Test resetAndExpirePwd without person exist"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def enrollmentServiceImpl = new EnrollmentServiceImpl()
    when:
    enrollmentServiceImpl.resetAndExpirePwd("testuid", new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "resetAndExpirePwd: test basic flow"() {
    given:
    def uid = "testuid"
    def audit = new SvcAudit()

    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String anUid -> return new SuPerson() }

    def kadmin = Mock(Kadmin)
    Kadmin.metaClass.static.newInstance = { -> return kadmin}

    when:
    def res = service.resetAndExpirePwd(uid, audit)

    then:
    assert res

    and:
    1 * kadmin.resetOrCreatePrincipal(*_) >> "xyz"

    and:
    1 * kadmin.setPasswordExpiry(*_)
  }

  @Test
  def "resetAndExpirePwd: test when uid param is null, should throw IllegalArgumentException"() {
    given:
    def uid = null
    def audit = new SvcAudit()

    when:
    service.resetAndExpirePwd(uid, audit)

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "resetAndExpirePwd: test when audit param is null, should throw IllegalArgumentException"() {
    given:
    def uid = "testuid"
    def audit = null

    when:
    service.resetAndExpirePwd(uid, audit)

    then:
    thrown(PreconditionViolation)
  }

  @Test
  def "resetAndExpirePwd: test when search for SuPerson returns null, should throw IllegalArgumentException"() {
    given:
    def uid = "testuid"
    def audit = new SvcAudit()

    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String anUid -> return null }

    when:
    service.resetAndExpirePwd(uid, audit)

    then:
    thrown(IllegalArgumentException)
  }
}
