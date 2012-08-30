package se.su.it.svc

import javax.jws.WebService
import javax.jws.WebParam
import se.su.it.svc.ldap.SuCard
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.commons.SvcAudit
import org.apache.log4j.Logger
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.query.SuCardQuery
import se.su.it.svc.manager.GldapoManager

/**
 * Implementing class for CardInfoService CXF Web Service.
 * This Class handles all University Card information in SUKAT.
 */
@WebService
public class CardInfoServiceImpl implements CardInfoService {
  private static final Logger logger = Logger.getLogger(CardInfoServiceImpl.class)

  /**
   * Returns a list (<code>ArrayList<SuCard></code>) of SuCard objects for a specific user, specified by the parameter uid.
   *
   *
   * @param uid  the uid (user id) for the user that you want to find cards for.
   * @param onlyActive  if only active cards should be returned in the result.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return an <code>ArrayList<SuCard></code> of SuCard objects or an empty array if no card was found.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.commons.SvcAudit
   */
  public SuCard[] getAllCards(@WebParam(name = "uid") String uid, @WebParam(name = "onlyActive") boolean onlyActive, @WebParam(name = "audit") SvcAudit audit) {
    if(uid == null || onlyActive == null || audit == null)
      throw new java.lang.IllegalArgumentException("Null values not allowed in this function")
    SuPerson person = SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RO, uid)
    if(person) {
      def cards = SuCardQuery.findAllCardsBySuPersonDnAndOnlyActiveOrNot(GldapoManager.LDAP_RO,person.getDn(),onlyActive)
      logger.debug("getAllCards - Found: ${cards.size()} card(s) ${cards.collect{card -> card.suCardUUID}.join(",")} with params: uid=<${uid}> onlyActive=<${onlyActive?"true":"false"}>")
      return cards
    } else {
      throw new IllegalArgumentException("getAllCards no such uid found: "+uid)
    }
    logger.debug("getAllCards - No cards found with params: uid=<${uid}> onlyActive=<${onlyActive?"true":"false"}>")
    return []
  }
  /**
   * Returns a SuCard object for a specific suCardUUID, specified by the parameter suCardUUID.
   *
   *
   * @param suCardUUID  the card uuid for the card.
   * @param audit Audit object initilized with audit data about the client and user.
   * @return an SuCard object or null if no card was found.
   * @see se.su.it.svc.ldap.SuCard
   * @see se.su.it.svc.commons.SvcAudit
   */
  public SuCard getCardByUUID(@WebParam(name = "suCardUUID") String suCardUUID, @WebParam(name = "audit") SvcAudit audit) {
    if(suCardUUID == null || audit == null)
      throw new java.lang.IllegalArgumentException("Null values not allowed in this function")
    def card = SuCardQuery.findCardBySuCardUUID(GldapoManager.LDAP_RO,suCardUUID)
    logger.debug("getCardByUUID - Found: ${card?"1":"0"} card ${card?card.suCardUUID:""} with params: suCardUUID=<${suCardUUID}>")
    if(card == null) {
      throw new IllegalArgumentException("getCardByUUID: Could not find a card with uuid<${suCardUUID}>")
    }
    return card
  }

}