package se.su.it.svc

import javax.jws.WebService
import javax.jws.WebParam
import se.su.it.svc.annotations.*
import se.su.it.svc.ldap.SuCard
import se.su.it.svc.ldap.SuPerson

@SuCxfSvcSpocpRole(role = "sukat-user-admin")
@WebService
public class CardInfoService {

  public SuCard[] getAllCards(@WebParam(name = "uid") String uid, @WebParam(name = "onlyActive") boolean onlyActive) {
    SuPerson person = SuPerson.getPersonFromUID(uid)
    if(person) {
      def cards = SuCard.findAll(base: person.getDn()) {
        if(onlyActive) {
          eq("suCardState", "urn:x-su:su-card:state:active")
        }
      }
      return cards
    }
  }
}
