package se.su.it.svc

import javax.jws.WebService
import org.apache.log4j.Logger
import se.su.it.svc.commons.SvcAudit
import javax.jws.WebParam

/**
 * Implementing class for AccountService CXF Web Service.
 * This Class handles all Account activities in SUKAT.
 */

@WebService
public class AccountServiceImpl implements AccountService{
  private static final Logger logger = Logger.getLogger(AccountServiceImpl.class)

  public void updateAffiliation(@WebParam(name = "uid") String uid, @WebParam(name = "affiliation") String affiliation, @WebParam(name = "audit") SvcAudit audit) {
    if(uid == null || affiliation == null || audit == null)
      throw new java.lang.IllegalArgumentException("updateAffiliation - Null argument values not allowed in this function")
  }
}
