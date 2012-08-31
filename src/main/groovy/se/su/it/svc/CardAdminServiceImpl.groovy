package se.su.it.svc

import javax.jws.WebService
import javax.jws.WebParam
import se.su.it.svc.ldap.SuCard
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.commons.SvcAudit
import org.apache.log4j.Logger
import se.su.it.svc.query.SuCardQuery
import se.su.it.svc.manager.GldapoManager

/**
 * Implementing class for CardAdminService CXF Web Service.
 * This Class handles all University Card admin activities in SUKAT.
 */
@WebService
public class CardAdminServiceImpl implements CardAdminService{
  private static final Logger logger = Logger.getLogger(CardAdminServiceImpl.class)
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
    if(suCardUUID == null || audit == null)
      throw new java.lang.IllegalArgumentException("Null values not allowed in this function")
    SuCard card =SuCardQuery.findCardBySuCardUUID(GldapoManager.LDAP_RW,suCardUUID)
    if(card != null) {
      card.suCardState="urn:x-su:su-card:state:revoked"
      SuCardQuery.saveSuCard(card)
    } else {
      logger.info("revokeCard: Could not find a card with uuid<${suCardUUID}>")
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
      throw new java.lang.IllegalArgumentException("Null values not allowed in this function")
    SuCard card =SuCardQuery.findCardBySuCardUUID(GldapoManager.LDAP_RW,suCardUUID)
    if(card != null) {
      card.suCardPIN = pin
      SuCardQuery.saveSuCard(card)
    } else {
      logger.info("setCardPIN: Could not find a card with uuid<${suCardUUID}>")
      throw new IllegalArgumentException("revokeCard: Could not find a card with uuid<${suCardUUID}>")
    }
  }
}
