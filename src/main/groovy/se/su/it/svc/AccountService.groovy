package se.su.it.svc

import javax.jws.WebService
import javax.jws.WebParam
import se.su.it.svc.annotations.*
import se.su.it.commons.ExecUtils

@SuCxfSvcSpocpRole(role = "sukat-user-admin")
@WebService
public class AccountService {

  public String getPassword(@WebParam(name = "uid") String uid) {
    String cmd = "/local/sukat/libexec/kdcpass.pl"
    String[] args = [{ uid.replaceFirst("\\.", "/") }]
    return ExecUtils.exec(cmd, args, "su-sukatsvc-service-admin");
  }
}
