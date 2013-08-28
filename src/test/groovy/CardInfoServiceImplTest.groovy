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

import spock.lang.*
import org.junit.Test
import se.su.it.svc.CardInfoServiceImpl
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuCard
import gldapo.GldapoSchemaRegistry
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.query.SuCardQuery
import se.su.it.svc.manager.EhCacheManager
import org.springframework.context.ApplicationContext
import net.sf.ehcache.hibernate.EhCache
import se.su.it.svc.manager.ApplicationContextProvider

class CardInfoServiceImplTest extends spock.lang.Specification {
  @Test
  def "Test getAllCards with null uid argument"() {
    setup:
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    cardInfoServiceImpl.getAllCards(null,false,new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getAllCards with null SvcAudit argument"() {
    setup:
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    cardInfoServiceImpl.getAllCards("testuid",false,null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getAllCards returns list of SuCard when person exists"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    def person = new SuPerson()
    def suCards = [new SuCard()]
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return person }
    person.metaClass.getDn = {""}
    SuCardQuery.metaClass.static.findAllCardsBySuPersonDnAndOnlyActiveOrNot = {String directory,String dn, boolean onlyActiveCards -> return suCards}
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    def ret = cardInfoServiceImpl.getAllCards("testuid",true,new SvcAudit())
    then:
    ret.size() == 1
    ret[0] instanceof SuCard
  }

  @Test
  def "Test getAllCards throws IllegalArgumentException when person don't exists"() {
    setup:
    GldapoSchemaRegistry.metaClass.add = { Object registration -> return }
    SuPersonQuery.metaClass.static.getSuPersonFromUID = {String directory,String uid -> return null }
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    def ret = cardInfoServiceImpl.getAllCards("testuid",true,new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getCardByUUID with null suCardUUID argument, should throw IllegalArgumentException"() {
    setup:
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    cardInfoServiceImpl.getCardByUUID(null,new SvcAudit())
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getCardByUUID with null SvcAudit argument, should throw IllegalArgumentException"() {
    setup:
    def cardInfoServiceImpl = new CardInfoServiceImpl()
    when:
    cardInfoServiceImpl.getCardByUUID("testcarduuid",null)
    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getCardByUUID when card doesn't exist, should throw IllegalArgumentException"() {
    given:
    SuCardQuery.metaClass.static.findCardBySuCardUUID = {String directory,String uid -> return null }

    def cardInfoServiceImpl = new CardInfoServiceImpl()

    when:
    cardInfoServiceImpl.getCardByUUID("testCardUUID", new SvcAudit())

    then:
    thrown(IllegalArgumentException)
  }

  @Test
  def "Test getCardByUUID default flow"() {
    given:
    SuCardQuery.metaClass.static.findCardBySuCardUUID = {String directory,String uid -> return new SuCard() }

    def cardInfoServiceImpl = new CardInfoServiceImpl()

    when:
    def res = cardInfoServiceImpl.getCardByUUID("testCardUUID", new SvcAudit())

    then:
    assert res
  }
}
