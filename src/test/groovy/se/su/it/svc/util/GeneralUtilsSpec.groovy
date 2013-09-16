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

package se.su.it.svc.util

import gldapo.GldapoSchemaRegistry
import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.ldap.SuPersonStub
import spock.lang.Specification
import spock.lang.Unroll

class GeneralUtilsSpec extends Specification {

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
  }

  def cleanup() {
    GldapoSchemaRegistry.metaClass = null
  }

  @Unroll
  void "pnrToSsn: When given pnr: '#pnr' we expect '#expected'"() {
    expect:
    GeneralUtils.pnrToSsn(pnr) == expected

    where:
    pnr           | expected
    '_'*11        | '_'*11        // 11 chars, nothing happens.
    '++' + '_'*10 | '_'*10        // 12 chars, first 2 chars should be cut.
    '++' + '_'*11 | '++' + '_'*11 // 13 chars, nothing happens.
  }

  @Unroll
  void "uidToKrb5Principal: When given uid: \'#uid\' we expect '\'#expected\'"() {
    expect:
    GeneralUtils.uidToKrb5Principal(uid) == expected

    where:
    uid              | expected
    null             | null
    ''               | ''
    '****'           | '****'
    '****.****'      | '****/****'
    '****.'          | '****/'
    '.****'          | '/****'
    '****.****/****' | '****/****/****'
    '****.****.****' | '****/****.****'
  }

  @Unroll
  def "uidToPrincipal: When given uid: '#uid' expect '#principal'"() {
    expect: GeneralUtils.uidToPrincipal(uid) == principal

    where:
    uid    | principal
    null   | null
    ''     | '' + GeneralUtils.SU_SE_SCOPE
    'test' | 'test' + GeneralUtils.SU_SE_SCOPE
  }

  def "copyProperties: should copy property values from one object to another"() {
    given:
    def source = new SvcSuPersonVO(
            uid: 'foo',
            description: 'bar')
    def target = new SvcSuPersonVO()

    when: GeneralUtils.copyProperties(source, target)

    then:
    target.uid == source.uid
    target.description == source.description
  }

  def "copyProperties: should not copy class, metaClass or serialVersionUID"() {
    given:
    def source = new SuPersonStub(
            uid: 'foo',
            cn: 'bar')
    def target = new SvcSuPersonVO()

    when: GeneralUtils.copyProperties(source, target)

    then:
    target.uid == source.uid

    and:
    target.class != source.class
    target.metaClass != source.metaClass
    target.serialVersionUID != source.serialVersionUID
  }
}
