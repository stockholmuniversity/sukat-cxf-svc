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
import se.su.it.svc.ldap.SuServiceDescription
import se.su.it.svc.query.SuServiceDescriptionQuery
import se.su.it.svc.ldap.SuSubAccount
import se.su.it.svc.query.SuSubAccountQuery
import se.su.it.commons.Kadmin

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

  /**
   * This method returns service descriptions found in sukat.
   *
   *
   * @param audit Audit object initilized with audit data about the client and user.
   * @return array of SuServiceDescription.
   * @see se.su.it.svc.ldap.SuServiceDescription
   * @see se.su.it.svc.commons.SvcAudit
   */
  public SuServiceDescription[] getServiceTemplates(@WebParam(name = "audit") SvcAudit audit) {
    if(audit == null)
      throw new java.lang.IllegalArgumentException("Null values not allowed in this function")
    return SuServiceDescriptionQuery.getSuServiceDescriptions(GldapoManager.LDAP_RO)
  }

  /**
   * This method enables a service in SUKAT for the current user or creates it and enables it if do not exist.
   *
   *
   * @param uid  uid of the user.
   * @param qualifier, a String that indicates whether to create a sub account for this service with qualifier as par of sub uid
   * @param description String with description for the sub account
   * @param audit Audit object initilized with audit data about the client and user.
   * @return SuService.
   * @see se.su.it.svc.ldap.SuService
   * @see se.su.it.svc.commons.SvcAudit
   */
  public SuService enableServiceFully(@WebParam(name = "uid") String uid, @WebParam(name = "serviceType") String serviceType, @WebParam(name = "qualifier") String qualifier, @WebParam(name = "description") String description, @WebParam(name = "audit") SvcAudit audit) {
    if(uid == null || serviceType == null || qualifier == null || description == null || audit == null)
      throw new java.lang.IllegalArgumentException("Null values not allowed in this function")

    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RO, uid)
    if(person == null)
      throw new java.lang.IllegalArgumentException("enableServiceFully no such uid found: "+uid)

    // START Try to create sub account if it do not exist
    if(qualifier && qualifier.length() > 0) {
      String subUid = uid + "." + qualifier;
      def subAccounts = SuSubAccountQuery.getSuSubAccounts(GldapoManager.LDAP_RO, person.getDn())
      if(!subAccounts.find {subAcc -> subAcc.uid == subUid}) {
        SuSubAccount subAcc = new SuSubAccount(uid: subUid, description: description)
        SuSubAccountQuery.createSubAccount(GldapoManager.LDAP_RW, subAcc, person.getDn().toString())
        Kadmin.newInstance().resetOrCreatePrincipal(subUid.replaceFirst("\\.", "/"))
      }
    }
    // END Try to create sub account if it do not exist
  }
}