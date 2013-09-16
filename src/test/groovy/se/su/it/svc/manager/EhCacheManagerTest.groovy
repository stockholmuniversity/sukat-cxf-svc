package se.su.it.svc.manager

import gldapo.GldapoSchemaRegistry
import net.sf.ehcache.Cache
import net.sf.ehcache.store.MemoryStoreEvictionPolicy

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

import spock.lang.Shared
import spock.lang.Specification

class EhCacheManagerTest extends Specification {

  @Shared
  def cacheManager

  void setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
    cacheManager = EhCacheManager.getInstance()
  }

  void cleanup() {
    GldapoSchemaRegistry.metaClass = null
  }

  def "getCache: get cache with default configuration"() {
    when:
    Cache cache = cacheManager.getCache()

    then:
    assert cache

    and:
    assert !cache.getCacheConfiguration().overflowToDisk
    assert cache.getCacheConfiguration().timeToLiveSeconds == 600
    assert cache.getCacheConfiguration().maxElementsInMemory == 10000
    assert !cache.getCacheConfiguration().eternal
    assert cache.getCacheConfiguration().timeToIdleSeconds == 120
    assert !cache.getCacheConfiguration().diskPersistent
    assert cache.getCacheConfiguration().diskExpiryThreadIntervalSeconds == 120
    assert cache.getCacheConfiguration().memoryStoreEvictionPolicy == MemoryStoreEvictionPolicy.LRU

    and:
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuPersonStub")
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuCard")
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuPerson")
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuRole")
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuService")
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuServiceDescription")
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuSubAccount")
  }

  def "put: test basic flow, should add element to cache and return value"() {
    given:
    def params = [key: "testKey3"]
    def testFunction = { "initial function" }

    when:
    def res = cacheManager.put(params, testFunction)

    then:
    assert res == "initial function"
  }

  def "get: test when key is missing, should throw IllegalArgumentException"() {
    given:
    def params = [key: ""]
    def testFunction = { "initial function" }

    cacheManager.put(params, testFunction)

    when:
    cacheManager.get(params, testFunction)

    then:
    thrown(IllegalArgumentException)
  }

  def "get: test when 'forceRefresh'=true, should put new element in cache and return it"() {
    def params = [key: "testKey1"]
    def testFunction = { "initial function" }

    cacheManager.put(params, testFunction)

    def newParams = [key: "testKey1", forceRefresh: true]
    def newFunction = { "new function" }

    when:
    def res = cacheManager.get(newParams, newFunction)

    then:
    assert res == "new function"
  }

  def "get: test when 'forceRefresh'=false, should fetch element from cache"() {
    def params = [key: "testKey2"]
    def testFunction = { "initial function" }

    cacheManager.put(params, testFunction)

    def newParams = [key: "testKey2", forceRefresh: false]
    def newFunction = { "new function" }

    when:
    def res = cacheManager.get(newParams, newFunction)

    then:
    assert res == "initial function"
  }

  def "get: test when element has null value, should return null"() {
    given:
    def params = [key: "testKeyNull"]
    def testFunction = new Object()

    cacheManager.put(params, testFunction)

    when:
    def res = cacheManager.get(params, testFunction)

    then:
    assert !res
  }
}
