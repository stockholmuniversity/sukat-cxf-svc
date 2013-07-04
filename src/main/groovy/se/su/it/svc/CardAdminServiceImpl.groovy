package se.su.it.svc

import groovy.util.logging.Slf4j
import se.su.it.svc.query.SuCardOrderQuery

import javax.jws.WebService
import javax.jws.WebParam
import se.su.it.svc.ldap.SuCard
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.query.SuCardQuery
import se.su.it.svc.manager.GldapoManager

/**
 * Implementing class for CardAdminService CXF Web Service.
 * This Class handles all University Card admin activities in SUKAT.
 */
@WebService @Slf4j
public class CardAdminServiceImpl implements CardAdminService{

  /**
   * This method puts a university card in revoked state.
   *
   *
   * @param suCardUUID  the card uuid for the card.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return void.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.commons.SvcAudit
   */



  public void revokeCard(@WebParam(name = "suCardUUID") String suCardUUID, @WebParam(name = "audit") SvcAudit audit) {
    if (suCardUUID == null || audit == null)
      throw new IllegalArgumentException("revokeCard - Null argument values not allowed in this function")
    SuCard card = SuCardQuery.findCardBySuCardUUID(GldapoManager.LDAP_RW, suCardUUID)
    if (card != null) {
      card.suCardState = "urn:x-su:su-card:state:revoked"
      SuCardQuery.saveSuCard(card)
      try {
        new SuCardOrderQuery().markCardAsDiscarded(suCardUUID, audit?.uid)
      } catch (ex) {
        log.error "Failed to mark card $card as discarded in sucarddb", ex
      }
    } else {
      log.info("revokeCard: Could not find a card with uuid<${suCardUUID}>")
      throw new IllegalArgumentException("revokeCard: Could not find a card with uuid<${suCardUUID}>")
    }
  }

  /**
   * This method sets a PIN for the specified University Card
   *
   *
   * @param suCardUUID  the card uuid for the card.
   * @param pin the new pin for the card.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return void.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.commons.SvcAudit
   */
  public void setCardPIN(@WebParam(name = "suCardUUID") String suCardUUID, @WebParam(name = "pin") String pin, @WebParam(name = "audit") SvcAudit audit) {
    if(suCardUUID == null || pin == null || audit == null)
      throw new java.lang.IllegalArgumentException("setCardPIN - Null argument values not allowed in this function")
    SuCard card =SuCardQuery.findCardBySuCardUUID(GldapoManager.LDAP_RW,suCardUUID)
    if(card != null) {
      card.suCardPIN = pin
      SuCardQuery.saveSuCard(card)
    } else {
      log.info("setCardPIN: Could not find a card with uuid<${suCardUUID}>")
      throw new IllegalArgumentException("revokeCard: Could not find a card with uuid<${suCardUUID}>")
    }
  }
}
