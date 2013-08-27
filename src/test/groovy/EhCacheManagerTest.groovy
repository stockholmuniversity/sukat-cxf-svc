import net.sf.ehcache.Cache
import net.sf.ehcache.store.MemoryStoreEvictionPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import se.su.it.svc.manager.EhCacheManager
import spock.lang.Shared
import spock.lang.Specification

class EhCacheManagerTest extends Specification {

  @Shared
  def cacheManager

  @Before
  void setup() {
    cacheManager = EhCacheManager.getInstance()
  }

  @After
  void cleanup() {}

  @Test
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
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuInitPerson")
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuCard")
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuPerson")
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuRole")
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuService")
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuServiceDescription")
    assert cache.getSearchAttribute("se.su.it.svc.ldap.SuSubAccount")
  }

  @Test
  def "put: test basic flow, should add element to cache and return value"() {
    given:
    def params = [key: "testKey3"]
    def testFunction = { "initial function" }

    when:
    def res = cacheManager.put(params, testFunction)

    then:
    assert res == "initial function"
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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
