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

  def 'execHelper: happy path'()
  {
    setup:
    String.metaClass.execute = { Runtime.getRuntime().exec(['echo { "happy": "path" }']) }

    when:
    def ret = GeneralUtils.execHelper('happy', 'path')

    then:
    ret.happy == 'path'
  }

  def 'execHelper: execution fails'()
  {
    setup:
    String.metaClass.execute = { Runtime.getRuntime().exec(['ls file-does-not-exist']) }

    when:
    def ret = GeneralUtils.execHelper('ls', 'file-that-does-not-exist')

    then:
    thrown(RuntimeException)
  }

    def "generatePassword: properly generate a number of passwords"()
    {
        setup:
        int count = 0
        def iterations = 10000 // Try a number of times to see potential duplicates
        def bucket = []

        when:
        for (i in 1..iterations)
        {
            def password = GeneralUtils.generatePassword()
            if (password.length() != 11)
            {
                throw new RuntimeException("Password \'${password}\' is not 10 characters long")
            }

            if ((password ==~ /^.*[a-z]+.*$/) == false)
            {
                throw new RuntimeException("Password \'${password}\' does not contain a-z")
            }

            if ((password ==~ /^.*[A-Z]+.*$/) == false)
            {
                throw new RuntimeException("Password \'${password}\' does not contain A-Z")
                ret = 1
            }

            if ((password ==~ /^.*[0-9]+.*$/) == false)
            {
                throw new RuntimeException("Password \'${password}\' does not contain 0-9")
                ret = 1
            }

            // Test for chars that are hard to distinguish when printed
            if (password ==~ /^.*[IOl01]+.*$/)
            {
                throw new RuntimeException("Password \'${password}\' contains hard to distinguish characters")
                ret = 1
            }

            if (bucket.contains(password))
            {
                throw new RuntimeException("Password \'${password}\' has already been generated")
            }

            bucket.add(password)

            count++
        }

        then:
        count == iterations
    }

    def "publishMessage: happy path"()
    {
        setup:
        def mockFile = GroovyMock(File) {
            1 * write(*_)
            1 * renameTo(*_)
        }
        GroovySpy(File, global: true, constructorArgs: ['a-pretty-file-name'])

        when:
        GeneralUtils.publishMessage([key: 'test message value'])

        then:
        1 * new File(*_) >> mockFile
    }

    def "ssnToNin: qualification is successful for #ssn"()
    {
        expect:
        nin == GeneralUtils.ssnToNin(ssn)

        where:
        ssn          | nin
        "9010101013" | "199010101013"
        "009000A000" | "20009000A000"
    }

    def "ssnToNin: qualification fails for #ssn"()
    {
        when:
        GeneralUtils.ssnToNin(ssn)

        then:
        thrown(RuntimeException)

        where:
        ssn          | _
        "0001010000" | _
    }
}
