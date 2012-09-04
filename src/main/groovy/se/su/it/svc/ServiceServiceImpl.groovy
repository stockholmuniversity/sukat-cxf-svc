package se.su.it.svc

import javax.jws.WebService
import org.apache.log4j.Logger
import javax.jws.WebParam
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuService
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.query.SuServiceQuery

/**
 * Implementing class for ServiceService CXF Web Service.
 * This Class handles all Service activities in SUKAT.
 */
@WebService
public class ServiceServiceImpl implements ServiceService {
  private static final Logger logger = Logger.getLogger(ServiceServiceImpl.class)

  /**
   * This method returns services for the specified uid.
   *
   *
   * @param uid  uid of the user.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return array of SuService.
   * @see se.su.it.svc.ldap.SuService
   * @see se.su.it.svc.commons.SvcAudit
   */
  public SuService[] getServices(@WebParam(name = "uid") String uid, @WebParam(name = "audit") SvcAudit audit) {
    if(uid == null || audit == null)
      throw new java.lang.IllegalArgumentException("Null values not allowed in this function")
    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RO, uid)
    if(person) {
      def services = SuServiceQuery.getSuServices(GldapoManager.LDAP_RO,person.getDn())
      logger.debug("getServices - Found: ${services.size()} service(s) ${services.collect{service -> service.suServiceType}.join(",")} with params: uid=<${uid}>")
      return services
    } else {
      throw new IllegalArgumentException("getServices no such uid found: "+uid)
    }
    logger.debug("getServices - No services found with params: uid=<${uid}>")
    return []
  }
}