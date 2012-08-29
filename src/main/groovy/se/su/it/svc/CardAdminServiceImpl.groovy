package se.su.it.svc

import javax.jws.WebService
import javax.jws.WebParam
import se.su.it.svc.ldap.SuCard
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.commons.SvcAudit
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-08-29
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
@WebService
public class CardAdminServiceImpl implements CardAdminService{
  private static final Logger logger = Logger.getLogger(CardAdminServiceImpl.class)

  public boolean revokeCard(@WebParam(name = "suCardUUID") String suCardUUID, @WebParam(name = "audit") SvcAudit audit) {
    if(suCardUUID == null || audit == null)
      throw new java.lang.IllegalArgumentException("Null values not allowed in this function")
    SuCard card = SuCard.find(base: "") {
      eq("objectClass","suCardOwner")
      eq("suCardUUID",suCardUUID)
    }
    if(card != null) {
      card.suCardState="urn:x-su:su-card:state:revoked"
      card.save()
      //urn:x-su:su-card:state:active
    } else {
      logger.info("revokeCard: Could not find a card with uuid<${suCardUUID}>")
      return false
    }
  }
}
