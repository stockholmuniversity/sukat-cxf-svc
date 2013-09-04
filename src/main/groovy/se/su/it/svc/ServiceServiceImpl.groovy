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

package se.su.it.svc

import org.apache.log4j.Logger
import se.su.it.commons.Kadmin
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuService
import se.su.it.svc.ldap.SuServiceDescription
import se.su.it.svc.ldap.SuSubAccount
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.query.SuServiceDescriptionQuery
import se.su.it.svc.query.SuServiceQuery
import se.su.it.svc.query.SuSubAccountQuery
import se.su.it.svc.util.GeneralUtils

import javax.jws.WebParam
import javax.jws.WebService

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
      throw new java.lang.IllegalArgumentException("getServices - Null argument values not allowed in this function")
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
   * This method returns a service description found in sukat.
   *
   *
   * @param serviceType ServiceType of the serviceDescription that is wanted.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return SuServiceDescription.
   * @see se.su.it.svc.ldap.SuServiceDescription
   * @see se.su.it.svc.commons.SvcAudit
   */
  public SuServiceDescription getServiceTemplate(@WebParam(name = "serviceType") String serviceType, @WebParam(name = "audit") SvcAudit audit) {
    if(serviceType == null || audit == null)
      throw new java.lang.IllegalArgumentException("getServiceTemplate - Null argument values not allowed in this function")
    return SuServiceDescriptionQuery.getSuServiceDescription(serviceType, GldapoManager.LDAP_RO)
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
      throw new java.lang.IllegalArgumentException("getServiceTemplates - Null argument values not allowed in this function")
    return SuServiceDescriptionQuery.getSuServiceDescriptions(GldapoManager.LDAP_RO)
  }

  /**
   * This method enables a service in SUKAT for the current user or creates it and enables it if do not exist.
   *
   *
   * @param uid  uid of the user.
   * @param serviceType the urn of the serviceType required
   * @param qualifier, a String that indicates whether to create a sub account for this service with qualifier as par of sub uid
   * @param description String with description for the sub account
   * @param audit Audit object initilized with audit data about the client and user.
   * @return SuService.
   * @see se.su.it.svc.ldap.SuService
   * @see se.su.it.svc.commons.SvcAudit
   */
  public SuService enableServiceFully(@WebParam(name = "uid") String uid, @WebParam(name = "serviceType") String serviceType, @WebParam(name = "qualifier") String qualifier, @WebParam(name = "description") String description, @WebParam(name = "audit") SvcAudit audit) {
    if(uid == null || serviceType == null || qualifier == null || description == null || audit == null)
      throw new java.lang.IllegalArgumentException("enableServiceFully - Null argument values not allowed in this function")

    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RO, uid)
    if(person == null)
      throw new java.lang.IllegalArgumentException("enableServiceFully no such uid found: "+uid)

    String subUid = ""
    // START Try to create sub account if it do not exist
    if(qualifier && qualifier.length() > 0) {
      subUid = uid + "." + qualifier;
      def subAccounts = SuSubAccountQuery.getSuSubAccounts(GldapoManager.LDAP_RO, person.getDn())
      if(!subAccounts.find {subAcc -> subAcc.uid == subUid}) {
        logger.debug("enableServiceFully - Trying to create sub account uid=<${subUid}> to be used by service=<${serviceType}> for uid=<${uid}>")
        SuSubAccount subAcc = new SuSubAccount()
        subAcc.parent = person.getDn().toString()
        subAcc.uid = subUid
        subAcc.description = description
        subAcc.objectClass = ["top", "account"]
        if(serviceType.equalsIgnoreCase("urn:x-su:service:type:jabber")) {
          subAcc.objectClass.add("jabberUser")
          subAcc.jabberID = GeneralUtils.uidToPrincipal(uid)
        }
        SuSubAccountQuery.createSubAccount(GldapoManager.LDAP_RW, subAcc)
        def subAccountPwd = Kadmin.newInstance().resetOrCreatePrincipal(GeneralUtils.uidToKrb5Principal(subUid))
        logger.info("enableServiceFully - Created sub account uid=<${subUid}> to be used by service=<${serviceType}> for uid=<${uid}>")
      } else {
        logger.info("enableServiceFully - Sub account uid=<${subUid}> to be used by service=<${serviceType}> for uid=<${uid}> already exist. Using it.")
      }
    }
    // END Try to create sub account if it do not exist
    SuService suService = SuServiceQuery.getSuServiceByType(GldapoManager.LDAP_RW, person.getDn(), serviceType)
    if(suService == null) {
      logger.debug("enableServiceFully - Trying to create service=<${serviceType}> for uid=<${uid}>")
      //create service
      suService = new SuService()
      suService.objectClass = ["top", "suServiceObject", "suService", "organizationalRole"]
      suService.cn = UUID.randomUUID().toString()
      suService.myowner = person.getDn().toString()
      suService.suServiceType = serviceType
      suService.suServiceStartTime = new Date().format("yyyyMMddHHmm'Z'")
      suService.suServiceStatus = "enabled"
      if(subUid.length() > 0) {
        suService.roleOccupant = "uid=${subUid},${person.getDn().toString()}"
        logger.debug("enableServiceFully - Setting roleOccupant of service=<${serviceType}> to <${suService.roleOccupant}>")
      }
      suService.parent=person.getDn().toString()
      SuServiceQuery.createService(GldapoManager.LDAP_RW,suService)
      logger.info("enableServiceFully - Created service=<${serviceType}> for uid=<${uid}>")
    } else {
      //enable service
      logger.debug("enableServiceFully - Service=<${serviceType}> for uid=<${uid}> already exist. Trying to enable it.")
      if (suService.suServiceStatus.equalsIgnoreCase("blocked") || suService.suServiceStatus.equalsIgnoreCase("locked"))
        throw new IllegalArgumentException("enableServiceFully Service " + suService.getDn().toString() +  " is blocked/locked")
      suService.suServiceStatus = "enabled"
      SuServiceQuery.saveSuService(suService)
      logger.info("enableServiceFully - Service=<${serviceType}> for uid=<${uid}> enabled.")
    }
    return suService
  }

  /**
   * This method blocks a service for specified serviceType and uid.
   *
   *
   * @param uid  uid of the user.
   * @param serviceType the urn of the serviceType required
   * @param audit Audit object initilized with audit data about the client and user.
   * @return void.
   * @see se.su.it.svc.ldap.SuService
   * @see se.su.it.svc.commons.SvcAudit
   */
  public void blockService(@WebParam(name = "uid") String uid, @WebParam(name = "serviceType") String serviceType, @WebParam(name = "audit") SvcAudit audit) {
    if(uid == null || serviceType == null || audit == null)
      throw new java.lang.IllegalArgumentException("blockService - Null argument values not allowed in this function")
    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RO, uid)
    if(person) {
      def service = SuServiceQuery.getSuServiceByType(GldapoManager.LDAP_RW, person.getDn(), serviceType)
      if(service != null) {
        if (service.suServiceStatus.equalsIgnoreCase("blocked") || service.suServiceStatus.equalsIgnoreCase("locked"))
          throw new IllegalArgumentException("blockService - service=<${serviceType}> for uid=<${uid}> is already blocked/locked")
        logger.debug("blockService - Trying to block service=<${serviceType}> for uid=<${uid}>")
        service.suServiceStatus = "blocked"
        SuServiceQuery.saveSuService(service)
        logger.info("blockService - Blocked service=<${serviceType}> for uid=<${uid}>")
        return
      }
    } else {
      throw new IllegalArgumentException("blockService no such uid found: "+uid)
    }
    logger.debug("blockService - No service found with params: uid=<${uid}> serviceType=<${serviceType}>")
    throw new IllegalArgumentException("blockService - No service found with params: uid=<${uid}> serviceType=<${serviceType}>")
    return
  }

  /**
   * This method unblocks a service for specified serviceType and uid to state disabled or enabled depending on the opt-in value in the service template.
   *
   *
   * @param uid  uid of the user.
   * @param serviceType the urn of the serviceType required
   * @param audit Audit object initilized with audit data about the client and user.
   * @return void.
   * @see se.su.it.svc.ldap.SuService
   * @see se.su.it.svc.commons.SvcAudit
   */
  public void unblockService(@WebParam(name = "uid") String uid, @WebParam(name = "serviceType") String serviceType, @WebParam(name = "audit") SvcAudit audit) {
    if(uid == null || serviceType == null || audit == null)
      throw new java.lang.IllegalArgumentException("unblockService - Null argument values not allowed in this function")
    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RO, uid)
    if(person) {
      def service = SuServiceQuery.getSuServiceByType(GldapoManager.LDAP_RW, person.getDn(), serviceType)
      if(service != null) {
        def serviceDescs=SuServiceDescriptionQuery.getSuServiceDescriptions(GldapoManager.LDAP_RO)
        def servDesc=serviceDescs.find {serverDescription -> serverDescription.suServiceType.equalsIgnoreCase(serviceType)}
        String status = servDesc?.suServicePolicy?.contains("opt-in") ? "disabled":"enabled"
        logger.debug("unblockService - Trying to unblock service=<${serviceType}> for uid=<${uid}>")
        service.suServiceStatus = status
        SuServiceQuery.saveSuService(service)
        logger.info("unblockService - Unblocked service=<${serviceType}> for uid=<${uid}> to service state=<${status}>")
        return
      }
    } else {
      throw new IllegalArgumentException("unblockService no such uid found: "+uid)
    }
    logger.debug("unblockService - No service found with params: uid=<${uid}> serviceType=<${serviceType}>")
    throw new IllegalArgumentException("unblockService - No service found with params: uid=<${uid}> serviceType=<${serviceType}>")
    return
  }
}

