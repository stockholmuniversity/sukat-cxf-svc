package se.su.it.svc

import javax.jws.WebService
import javax.jws.WebParam
import se.su.it.svc.annotations.*
import se.su.it.commons.ExecUtils

@SuCxfSvcSpocpRole(role = "sukat-user-admin")
@WebService
public class CardInfoService {

  public String getAllCards(@WebParam(name = "uid") String uid, @WebParam(name = "onlyActive") boolean onlyActive) {
    String filter = onlyActive ? "objectClass=suCardOwner" : "(&(objectClass=suCardOwner)(suCardState=urn:x-su:su-card:state:active))";
  }
}
