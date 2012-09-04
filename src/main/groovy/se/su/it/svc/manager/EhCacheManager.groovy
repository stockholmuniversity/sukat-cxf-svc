package se.su.it.svc.manager

import net.sf.ehcache.CacheManager
import net.sf.ehcache.Ehcache
import net.sf.ehcache.Cache
import net.sf.ehcache.config.CacheConfiguration
import net.sf.ehcache.Element
import org.apache.log4j.Logger;
/**
 * Created by: Jack Enqvist (jaen4109)
 * Date: 2012-08-30 ~ 11:22
 *
 */
class EhCacheManager {

  private static final Logger logger = Logger.getLogger(EhCacheManager.class)
  def final Object NULL = "NULL"
  def final int DEFAULT_TTL = 300
  private props = Properties.getInstance().props


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

    if(!cacheInstance) {
      cacheInstance = cacheManager.addCacheIfAbsent(cacheName)
      Cache cache = cacheManager.getCache(cacheName)
      CacheConfiguration config = cache.getCacheConfiguration()
      config.setTimeToIdleSeconds(props.ehcache.timetoidleseconds as Long)
      config.setTimeToLiveSeconds(props.ehcache.timetoliveseconds as Long)
      config.setMaxEntriesLocalHeap(props.ehcache.maxentrieslocalheap as Long)
      config.setMaxElementsOnDisk(props.ehcache.maxelementsondisk as int)
      config.setOverflowToDisk(props.ehcache.overflowtodisk as Boolean)
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

    // Unless alreay found in cache we refresh the value
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
    def ttl = params['ttl'] ?: props.ehcache.defaultttl ?: DEFAULT_TTL

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
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    def out = new ObjectOutputStream(bos);

    // Serialize object
    out.writeObject(obj);
    out.close();

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
