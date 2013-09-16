package se.su.it.svc.aspect

import gldapo.GldapoSchemaRegistry
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
class AuditEntitySpec extends Specification {

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
  }

  def cleanup() {
    GldapoSchemaRegistry.metaClass = null
  }

  def "Constructor: Test constructor"() {
    when: 'Constructor should never be called directly, thus private.'
    AuditEntity auditEntity = new AuditEntity()

    then:
    auditEntity.created       == null
    auditEntity.ip_address    == null
    auditEntity.uid           == null
    auditEntity.client        == null
    auditEntity.operation     == null
    auditEntity.text_args     == null
    auditEntity.raw_args      == null
    auditEntity.text_return   == null
    auditEntity.raw_return    == null
    auditEntity.state         == null
    auditEntity.methodDetails == null
  }

  def "getInstance: Test factory method"() {
    when:
    AuditEntity auditEntity = AuditEntity.getInstance('1','2','3','4','5','6','7','8','9','10',['11','12'])

    then:
    auditEntity.created       == '1'
    auditEntity.ip_address    == '2'
    auditEntity.uid           == '3'
    auditEntity.client        == '4'
    auditEntity.operation     == '5'
    auditEntity.text_args     == '6'
    auditEntity.raw_args      == '7'
    auditEntity.text_return   == '8'
    auditEntity.raw_return    == '9'
    auditEntity.state         == '10'
    auditEntity.methodDetails == ['11','12']
  }
  def "test toString"() {
    given:
    AuditEntity auditEntity = AuditEntity.getInstance('1','2','3','4','5','6','7','8','9','10',['11','12'])

    expect:
    auditEntity.toString() == "se.su.it.svc.aspect.AuditEntity(created:1, ip_address:2, uid:3, client:4, operation:5, text_args:6, text_return:8, state:10, methodDetails:[11, 12])"
  }
}
