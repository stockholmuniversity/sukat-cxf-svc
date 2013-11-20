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

import gldapo.GldapoSchemaRegistry
import se.su.it.svc.ldap.SuCard
import spock.lang.Specification

class SuCardQuerySpec extends Specification {

  def setup(){
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
  }

  def cleanup(){
    SuCard.metaClass = null
    SuCardQuery.metaClass = null
    GldapoSchemaRegistry.metaClass = null
  }

  def "findAllCardsBySuPersonDnAndOnlyActiveOrNot should pass exception forward"() {
    given:
    SuCard.metaClass.static.findAll = { LinkedHashMap a, Closure c ->
      throw new NullPointerException()
    }

    when:
    SuCardQuery.findAllCardsBySuPersonDnAndOnlyActiveOrNot(null, null, true)

    then:
    thrown(NullPointerException)
  }

  def "findAllCardsBySuPersonDnAndOnlyActiveOrNot - happy path"() {
    given:
    SuCard.metaClass.static.findAll = { LinkedHashMap a, Closure c ->
      [new SuCard()]
    }

    when:
    def ret = SuCardQuery.findAllCardsBySuPersonDnAndOnlyActiveOrNot(null, null, true)

    then:
    ret.size() == 1
    ret.first() instanceof SuCard
  }

  def "findAllCardsBySuPersonDnAndOnlyActiveOrNot - checks for active cards"() {
    given:
    boolean searchForActive = false
    SuCardQuery.metaClass.static.eq = { String a, String b ->
      if (a == 'suCardState' && b == "urn:x-su:su-card:state:active")
        searchForActive = true
    }
    SuCard.metaClass.static.findAll = { LinkedHashMap a, Closure c ->
      c.call()
      return new SuCard[0]
    }

    when:
    SuCardQuery.findAllCardsBySuPersonDnAndOnlyActiveOrNot(null, null, true)

    then:
    searchForActive
  }

  def "findCardBySuCardUUID should forward exception"() {
    given:
    SuCard.metaClass.static.find = { LinkedHashMap a, Closure c ->
      throw new NullPointerException()
    }

    when:
    SuCardQuery.findCardBySuCardUUID(null, null)

    then:
    thrown(NullPointerException)
  }

  def "findCardBySuCardUUID - happy path"() {
    given:
    SuCard.metaClass.static.find = { LinkedHashMap a, Closure c ->
      new SuCard()
    }

    when:
    def ret = SuCardQuery.findCardBySuCardUUID(null, null)

    then:
    ret instanceof SuCard
  }
}
