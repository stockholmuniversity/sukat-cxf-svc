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

package se.su.it.svc.manager

import net.sf.ehcache.Cache
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Ehcache
import net.sf.ehcache.Element
import net.sf.ehcache.config.CacheConfiguration
import net.sf.ehcache.config.SearchAttribute
import net.sf.ehcache.config.Searchable
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by: Jack Enqvist (jaen4109)
 * Date: 2012-08-30 ~ 11:22
 *
 */
class EhCacheManager {

  @Autowired
  ConfigManager configManager

  private static final Logger logger = Logger.getLogger(EhCacheManager.class)
  def final Object NULL = "NULL"
  def final int DEFAULT_TTL = (60 * 10)
  private props = configManager?.config


  def final static EhCacheManager INSTANCE = new EhCacheManager()
  final CacheManager cacheManager
  def final String DEFAULT_CACHE_NAME = 'cache'

  private EhCacheManager(){
    cacheManager = CacheManager.getInstance()
  }

  public static getInstance(){
    return INSTANCE
  }


  public Ehcache getCache(String cacheName = DEFAULT_CACHE_NAME) {
    if(!cacheName){
      cacheName = DEFAULT_CACHE_NAME
    }

    def cacheInstance = cacheManager.getCache(cacheName)

    if (!cacheInstance) {
      CacheConfiguration config = new CacheConfiguration(cacheName, 0)
      try {
        config.setOverflowToDisk(props?.ehcache?.overflowToDisk ? props.ehcache?.overflowToDisk.toBoolean(): false)
        //TODO: This is the proposed, new, way to handle in-memory overflow. We need to decide on if we need fault tolerance (for a fee)
//        config.persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.LOCALTEMPSWAP))

        config.setTimeToLiveSeconds(props?.ehcache?.timeToLiveSeconds ? props.ehcache?.timeToLiveSeconds.toInteger() : 600);
        config.setMaxElementsInMemory(props?.ehcache?.maxElementsInMemory ? props.ehcache?.maxElementsInMemory.toInteger() : 10000)
        //TODO: This is the proposed, new, way to handle objects in local heap memory
//        config.setMaxEntriesLocalHeap(10000L)

        config.setEternal(props?.ehcache?.eternal ? props.ehcache?.eternal.toBoolean() : false)
        config.setTimeToIdleSeconds(props?.ehcache?.timeToIdleSeconds ? props.ehcache?.timeToIdleSeconds.toInteger() : 120)

        //TODO: remove this configuration if uncommenting config.persistence above
        config.setDiskPersistent(props?.ehcache?.diskPersistent ? props.ehcache?.diskPersistent.toBoolean() : false)
        config.setDiskExpiryThreadIntervalSeconds(props?.ehcache?.diskExpiryThreadIntervalSeconds ? props.ehcache?.diskExpiryThreadIntervalSeconds.toInteger() : 120)
        config.setMemoryStoreEvictionPolicy(props?.ehcache?.memoryStoreEvictionPolicy ? props.ehcache?.memoryStoreEvictionPolicy.toString() : "LRU")
      } catch (e) {
        logger.info("Cant load the cache config, check if config is present. cause: " + e.cause)
        e.printStackTrace()
      }



      Searchable searchable = new Searchable(keys: false, values: false)
      searchable.addSearchAttribute(new SearchAttribute().name("se.su.it.svc.ldap.SuPersonStub").className(CacheSearchAttributeExtractor.class.name))
      searchable.addSearchAttribute(new SearchAttribute().name("se.su.it.svc.ldap.SuCard").className(CacheSearchAttributeExtractor.class.name))
      searchable.addSearchAttribute(new SearchAttribute().name("se.su.it.svc.ldap.SuPerson").className(CacheSearchAttributeExtractor.class.name))
      searchable.addSearchAttribute(new SearchAttribute().name("se.su.it.svc.ldap.SuRole").className(CacheSearchAttributeExtractor.class.name))
      searchable.addSearchAttribute(new SearchAttribute().name("se.su.it.svc.ldap.SuService").className(CacheSearchAttributeExtractor.class.name))
      searchable.addSearchAttribute(new SearchAttribute().name("se.su.it.svc.ldap.SuServiceDescription").className(CacheSearchAttributeExtractor.class.name))
      searchable.addSearchAttribute(new SearchAttribute().name("se.su.it.svc.ldap.SuSubAccount").className(CacheSearchAttributeExtractor.class.name))
      config.addSearchable(searchable)

      cacheManager.addCache(new Cache(config))
      cacheInstance = cacheManager.getCache(cacheName)

      cacheInstance.getCacheEventNotificationService().registerListener(new CacheEventListenerImpl());
    }

    cacheInstance
  }

  def get(params, function) {
    def cache = getCache(params['cache'])

    def key=params['key']
    def value=null

    if(!key) {
      throw new IllegalArgumentException("Missing parameter key")
    }

    // only get the element from cache if forceRefresh is false
    if(!params['forceRefresh']) {
      try {
        Element element = cache.get(key)
        if(element) {
          logger.trace "cache hit on ${key}, hits: ${element.getHitCount()}"
          value = element.getValue()

          if(params['forceSerialize'])
            value = deSerializeObject(value)
        }
      } catch(e) {
        logger.error("Failed to work with the cache, getting element from source instead.", e)
      }
    }

    // Unless already found in cache we refresh the value
    if (value == null) {
      logger.trace("Cache key: $key was not found in the cache, adding $key to cache.")
      value = put(params, function)
    } else {
      logger.trace ("Cache key: $key was found in the cache, returning $key value from cache.")
    }

    // Return the value
    return (value == NULL) ? null : value;
  }

  def put(params, closure) {
    def cache = getCache(params['cache'])

    def key = params['key']
    def ttl = params['ttl'] ?: props?.ehcache?.defaultttl ?: DEFAULT_TTL

    try {
      def value = closure()

      def element = new Element(key, params['forceSerialize'] ? serializeObject(value) : value)
      element.timeToLive = ttl
      if (!value.hasProperty("valid") || value.valid == true ) {
        cache.put(element)
      } else {
        logger.info("Not caching: ${key}")
      }
      return value
    } catch (e) {
      e.printStackTrace()
    } catch (e) {
      logger.error("Failed to store element in cache.", e)
    }
    null
  }

  boolean isKeyInCache(def key, def params = [:]) {
    def cache = getCache(params['cache'])
    cache.isKeyInCache(key)
  }

  void remove(def key, def params = [:]) {
    def cache = getCache(params['cache'])
    cache.remove(key)
  }

  private serializeObject(obj) {
    // setup streams
    ByteArrayOutputStream bos = new ByteArrayOutputStream()
    def out = new ObjectOutputStream(bos)

    // Serialize object
    out.writeObject(obj)
    out.close()

    // Return the serialized stream
    return bos.toByteArray()
  }

  private deSerializeObject(obj) {
    if(obj == null) {
      return null
    }
    return new ByteArrayInputStream(obj).withObjectInputStream(getClass().classLoader) { is ->
      obj = is.readObject()
    }
  }


}
