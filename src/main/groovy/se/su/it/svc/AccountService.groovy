package se.su.it.svc

import javax.jws.WebService
import javax.jws.WebParam
import se.su.it.svc.annotations.*
import se.su.it.svc.domains.*
import se.su.it.svc.manager.ApplicationContextProvider
import se.su.it.svc.ldap.SuPerson
import org.springframework.core.io.Resource

@SuCxfSvcSpocpRole(role = "sukat-user-admin")
@WebService
public class AccountService {

  public String sayHi(@WebParam(name = "text") String text) {
    // GROM IMPLEMENTATION
    ApplicationContextProvider.bindTxSession()
    Test helloObj = new Test()
    helloObj.setName(text)
    helloObj.setVisitdate(new Date())
    helloObj.save()
    //

    //GLDAPO IMPLEMENTATION
    SuPerson person = SuPerson.find(base: "") {
      and {
        eq("uid", "jqvar")
        eq("objectclass", "superson")
      }
    }
    //

    return (person.displayName != null ? person.displayName:"no one") + " says HELLO to " + text
    //return "Hello " + text
  }
}
