package se.su.it.svc

import javax.jws.WebService
import javax.jws.WebParam
import se.su.it.svc.annotations.*
import se.su.it.svc.ldap.SuCard
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.commons.SvcAudit
import org.apache.log4j.Logger


@WebService
public class CardInfoServiceImpl implements CardInfoService {
  private static final Logger logger = Logger.getLogger(CardInfoServiceImpl.class)

  /**
   * Returns a list (<code>ArrayList<SuCard></code>) of SuCard objects for a specific user, specified by the parameter uid.
   *
   *
   * @param uid  the uid (user id) for the user that you want to find cards for.
   * @param onlyActive  if only active cards should be returned in the result.
   * @param audit an audit thing
   * @return an <code>ArrayList<SuCard></code> of SuCard objects.
   * @see se.su.it.svc.ldap.SuCard
   */
  public SuCard[] getAllCards(@WebParam(name = "uid") String uid, @WebParam(name = "onlyActive") boolean onlyActive, @WebParam(name = "audit") SvcAudit audit) {
    if(uid == null || onlyActive == null || audit == null)
      throw new java.lang.IllegalArgumentException("Null values not allowed in this function")
    SuPerson person = SuPerson.getPersonFromUID(uid)
    if(person) {
      def cards = SuCard.findAll(base: person.getDn()) {
        eq("objectClass","suCardOwner")
        if(onlyActive) {
          eq("suCardState", "urn:x-su:su-card:state:active")
        }
      }
      if(cards.size() > 0){
        logger.debug("getAllCards - Found: ${cards.collect{card -> card.suCardUUID}.join(",")} with params: uid=<${uid}> onlyActive=<${onlyActive?"true":"false"}>")
      } else {
        logger.debug("getAllCards - Found: 0 cards with params: uid=<${uid}> onlyActive=<${onlyActive?"true":"false"}>")
      }
      return cards
    }
    logger.debug("getAllCards - No cards found with params: uid=<${uid}> onlyActive=<${onlyActive?"true":"false"}>")
    return []
  }

}