package se.su.it.svc.manager

import net.sf.ehcache.search.attribute.AttributeExtractor
import net.sf.ehcache.Element
import sun.reflect.generics.scope.ClassScope

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-09-18
 * Time: 08:24
 * To change this template use File | Settings | File Templates.
 */
public class CacheSearchAttributeExtractor implements AttributeExtractor{

  public Object attributeFor(Element element, String s) {
    if (element == null || element.value == null){
      return null
    }
    if(element.value instanceof java.util.LinkedList) {
      if(element.value.find {entry -> entry.class.getName() == s }) {
        return true
      }
    } else if(element.value.class.getName() == s) {
      return true
    }
    return null  //no match
  }
}
