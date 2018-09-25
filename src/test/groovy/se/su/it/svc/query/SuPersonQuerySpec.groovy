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

package se.su.it.svc.query

import se.su.it.svc.ldap.SuPerson

import spock.lang.Specification

class SuPersonQuerySpec extends Specification
{
    def cleanup()
    {
        SuPerson.metaClass = null
    }

    def "findPersonByNin: person is found"()
    {
        setup:
        SuPerson.metaClass.static.find = { Map arg1, Closure arg2 -> [:] }

        when:
        def res = SuPersonQuery.findPersonByNin("Test directory", "199610101234")

        then:
        res != null
    }

    def "findPersonByNin: no person is found"()
    {
        setup:
        SuPerson.metaClass.static.find = { Map arg1, Closure arg2 -> }

        when:
        def res = SuPersonQuery.findPersonByNin(null, "199610101234")

        then:
        res == null
    }

  def "getSuPersonFromUID should handle exception"() {
    given:
    GroovyMock(SuPerson, global:true)
    SuPerson.find(*_) >> { throw new IllegalArgumentException() }

    when:
    SuPersonQuery.getSuPersonFromUID(null, null)

    then:
    thrown(IllegalArgumentException)
  }

  def "getSuPersonFromUID should throw exception on empty return value"() {
    given:
    GroovyMock(SuPerson, global:true)
    SuPerson.find(*_) >> { return null }

    when:
    SuPersonQuery.getSuPersonFromUID(null, null)

    then:
    thrown(IllegalArgumentException)
  }

  def "findSuPersonByUID should return empty return value"() {
    given:
    GroovyMock(SuPerson, global:true)
    SuPerson.find(*_) >> { return null }

    when:
    def resp = SuPersonQuery.findSuPersonByUID(null, null)

    then:
    resp == null
  }

}
