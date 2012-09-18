package se.su.it.svc.manager

import net.sf.ehcache.event.CacheEventListener
import net.sf.ehcache.Ehcache
import net.sf.ehcache.Element
import net.sf.ehcache.search.Attribute
import se.su.it.svc.ldap.SuPerson
import net.sf.ehcache.search.Results
import net.sf.ehcache.search.Query
import net.sf.ehcache.search.expression.Or
import gldapo.schema.annotation.GldapoNamingAttribute

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-14
 * Time: 10:20
 * To change this template use File | Settings | File Templates.
 */
class CacheEventListenerImpl implements CacheEventListener{

  public void notifyElementRemoved(Ehcache ehcache, Element element) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void notifyElementPut(Ehcache ehcache, Element element) {
    def kalle = element.creationTime
  }

  public void notifyElementUpdated(Ehcache ehcache, Element element) {
    String elementClass = element.value.class.getName()
    String gldapoNamingAttribute = null
    element.value.getClass().declaredFields.each {
      if(it.getAnnotation(GldapoNamingAttribute)) {
       gldapoNamingAttribute = it.name
      }
    }
    if(gldapoNamingAttribute == null)
      return

    Attribute attribute = ehcache.getSearchAttribute(elementClass)

    def criteria = attribute.eq(true)

    Results results = ehcache.createQuery().addCriteria(criteria).includeKeys().includeValues().end().execute()

    results.all().each {result ->
      if(result.key != element.key) {
        if(result.value instanceof java.util.LinkedList) {
          result.value.eachWithIndex { listEntry, index ->
            if(listEntry.getAt(gldapoNamingAttribute) == element.value.getAt(gldapoNamingAttribute)) {
              //This Cache Key need to be updated with the element
              LinkedList newList = result.value.clone()
              newList.remove(index)
              newList.add(element.value)
              def newElement = new Element(result.key, newList)
              newElement.timeToLive = EhCacheManager.INSTANCE.DEFAULT_TTL
              ehcache.putQuiet(newElement)
            }
          }
        } else {
          if(result.value.getAt(gldapoNamingAttribute) == element.value.getAt(gldapoNamingAttribute)) {
            //This Cache Key need to be updated with the element
            def newElement = new Element(result.key, element.value)
            newElement.timeToLive = EhCacheManager.INSTANCE.DEFAULT_TTL
            ehcache.putQuiet(newElement)
          }
        }
      }
    }
  }

  public void notifyElementExpired(Ehcache ehcache, Element element) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void notifyElementEvicted(Ehcache ehcache, Element element) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void notifyRemoveAll(Ehcache ehcache) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void dispose() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException("Singleton instance");
  }
}
