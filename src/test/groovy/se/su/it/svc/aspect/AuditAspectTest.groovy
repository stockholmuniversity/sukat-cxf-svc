package se.su.it.svc.aspect

import gldapo.GldapoSchemaRegistry

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

import org.aopalliance.intercept.MethodInvocation
import se.su.it.svc.commons.SvcAudit
import spock.lang.Specification

import java.lang.reflect.Method

class AuditAspectTest extends Specification {

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
    AuditAspect.metaClass = null
  }

  def cleanup() {
    AuditAspect.metaClass = null
    GldapoSchemaRegistry.metaClass = null
  }

  def "Constructor"() {
    expect:
    AuditAspect.STATE_INPROGRESS == "IN PROGRESS"
    AuditAspect.STATE_SUCCESS    == "SUCCESS"
    AuditAspect.STATE_EXCEPTION  == "EXCEPTION"
    AuditAspect.UNKNOWN          == "<unknown>"
    AuditAspect.HIDDEN_VALUE     == "******"
  }

  def "invoke: Happy path without auditref"()  {
    given:
    AuditAspect auditAspect = Spy(AuditAspect) {
      1 * logBefore(*_)
      0 * logAfter(*_)
    }
    MethodInvocation methodInvocation = Mock(MethodInvocation) {
      1 * proceed(*_) >> 'foo'
    }

    when:
    def resp = auditAspect.invoke(methodInvocation)

    then:
    resp == 'foo'
  }

  def "invoke: Happy path with audit ref"()  {
    given:
    AuditAspect auditAspect = Spy(AuditAspect) {
      1 * logBefore(*_) >> 'ref'
      1 * logAfter(*_)
    }
    MethodInvocation methodInvocation = Mock(MethodInvocation) {
      1 * proceed(*_) >> 'foo'
    }

    when:
    def resp = auditAspect.invoke(methodInvocation)

    then:
    resp == 'foo'
  }


  def "invoke: When logging fails"()  {
    given:
    AuditAspect auditAspect = Spy(AuditAspect) {
      1 * logBefore(*_) >> { throw new RuntimeException('bar')}
    }
    MethodInvocation methodInvocation = Mock(MethodInvocation)

    when:
    auditAspect.invoke(methodInvocation)

    then:
    thrown(Exception)
  }

  def "invoke: When invocation fails"()  {
    given:
    AuditAspect auditAspect = Spy(AuditAspect) {
      1 * logBefore(*_) >> ['obj']
      1 * logException(*_)
    }
    MethodInvocation methodInvocation = Mock(MethodInvocation) {
      1 * proceed(*_) >> { throw new RuntimeException('foo') }
    }

    when:
    auditAspect.invoke(methodInvocation)

    then:
    thrown(Exception)
  }

  def "invoke: When invocation fails and there is no audit ref"()  {
    given:
    AuditAspect auditAspect = Spy(AuditAspect) {
      1 * logBefore(*_) >> null
      0 * logException(*_)
    }
    MethodInvocation methodInvocation = Mock(MethodInvocation) {
      1 * proceed(*_) >> { throw new RuntimeException('foo') }
    }

    when:
    auditAspect.invoke(methodInvocation)

    then:
    thrown(Exception)
  }

  def "logBefore: Test with regular class."() {
    given:
    Class<?> c = Class.forName('java.lang.String')
    Method method = c.getDeclaredMethod("charAt", int)

    AuditAspect auditAspect = new AuditAspect()

    when:
    def resp = (AuditEntity) auditAspect.logBefore(method, [])

    then:
    resp.operation == 'charAt'
  }

  def "logBefore: Test with regular class when last arg is SvcAudit obj."() {
    given:
    Class<?> c = Class.forName('java.lang.String')
    Method method = c.getDeclaredMethod("charAt", int)

    AuditAspect auditAspect = new AuditAspect()

    when:
    def resp = (AuditEntity) auditAspect.logBefore(method, [new SvcAudit()])

    then:
    resp.operation == 'charAt'
  }

  def "logAfter: Happy path"() {
    given:
    AuditAspect auditAspect = new AuditAspect()
    AuditEntity ae = AuditEntity.getInstance('1','2','3','4','5','6','7','8','9','10', ['11', '12'])

    when:
    auditAspect.logAfter(ae, 'foo')

    then:
    ae.text_return == 'foo'

    and: "raw has also changed, but is not equal to foo.bytes so we just check that it is not the original value."
    ae.raw_return != '9'
  }

  def "logAfter: Unhappy path"() {
    given:
    AuditAspect.metaClass.objectToString = { arg1 ->
      assert arg1 == 'foo'
      throw new RuntimeException('foo')
    }

    AuditAspect auditAspect = new AuditAspect()
    AuditEntity ae = AuditEntity.getInstance('1','2','3','4','5','6','7','8','9','10', ['11', '12'])

    when:
    auditAspect.logAfter(ae, 'foo')

    then:
    ae.text_return == '8'

    and:
    ae.raw_return == '9'
  }

  def "logException: Happy path"() {
    given:
    AuditAspect auditAspect = new AuditAspect()
    AuditEntity ae = AuditEntity.getInstance('1','2','3','4','5','6','7','8','9','10', ['11', '12'])

    when:
    auditAspect.logException(ae, new Exception('My Little Pony Exception'))

    then:
    ae.text_return == 'java.lang.Exception: My Little Pony Exception'

    and:
    ae.state == 'EXCEPTION'
  }

  def "logException: Unhappy path"() {
    given:
    AuditAspect auditAspect = new AuditAspect()
    Object ae = [text_return:'text_return', state:'state']

    when:
    auditAspect.logException(ae, new Exception('My Little Pony Exception'))

    then:
    ae.text_return == 'text_return'

    and:
    ae.state == 'state'
  }

  def "objectToString: When object is null"() {
    given:
    Object o = null

    expect:
    new AuditAspect().objectToString(o) == "null"
  }

  def "objectToString: Happy path"() {
    given:
    Object o = 'foo'
    expect:
    new AuditAspect().objectToString(o) == "foo"
  }

  def "objectToString: Happy path with object array as null."() {
    given:
    Object o = null

    expect:
    new AuditAspect().objectToString((Object[]) o) == "null"
  }

  def "objectToString: Happy path with object array."() {
    given:
    List objArray = []

    Expando expando = new Expando()
    expando.toString = {-> return 'some' }
    objArray.add(expando)

    expando = new Expando()
    expando.toString = {-> return 'text' }
    objArray.add(expando)

    expect:
    new AuditAspect().objectToString((Object[]) objArray) == '[some,text]'
  }
}
