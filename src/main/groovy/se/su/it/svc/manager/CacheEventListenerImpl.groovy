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

import gldapo.schema.annotation.GldapoNamingAttribute
import net.sf.ehcache.Ehcache
import net.sf.ehcache.Element
import net.sf.ehcache.event.CacheEventListener
import net.sf.ehcache.search.Attribute
import net.sf.ehcache.search.Results

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
