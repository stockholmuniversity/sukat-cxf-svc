package se.su.it.svc.commons

import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2013-02-26
 * Time: 11:22
 * To change this template use File | Settings | File Templates.
 */
public class LdapAttributeValidator {
  private static final Logger logger = Logger.getLogger(LdapAttributeValidator.class)
  private static final String validateAttributesString = "validateAttributes"
  private static final List<String> affiliations = ["student","member","employee","alumni","other"]

  public static String validateAttributes(Map<String, Object> map) {
    String error = null
    map.each {String attributeName, Object val ->
      if (error)
        return
      switch (attributeName.toLowerCase()) {
        case "audit"                        : try {validateAudit(val)}                        catch (Exception x) {error = x.message};break
        case "uid"                          : try {validateUid(val)}                          catch (Exception x) {error = x.message};break
        case "edupersonprimaryaffiliation"  : try {validateEduPersonPrimaryAffiliation(val)}  catch (Exception x) {error = x.message};break
        case "domain"                       : try {validateDomain(val)}                       catch (Exception x) {error = x.message};break
        case "nin"                          : try {validateNin(val)}                          catch (Exception x) {error = x.message};break
        case "givenname"                    : try {validategivenName(val)}                    catch (Exception x) {error = x.message};break
        case "sn"                           : try {validateSn(val)}                           catch (Exception x) {error = x.message};break
        case "svcsuperson"                  : try {validateSvcSuPersonVO(val)}                catch (Exception x) {error = x.message};break
        case "mailroutingaddress"           : try {validateMailRoutingAddress(val)}           catch (Exception x) {error = x.message};break
        default: logger.debug("${validateAttributesString} - Attribute <${attributeName}> dont have a validation role!");break
      }
    }
    return error
  }

  private static void validateAudit(Object audit) {
    if (audit == null) {
      throwMe(validateAttributesString,"Attribute validation failed for audit object <${audit}>. audit can not be null.")
    }
    if (!audit instanceof SvcAudit) {
      throwMe(validateAttributesString,"Attribute validation failed for audit object <${audit}>. audit need to be a SvcAudit object.")
    }
  }

  private static void validateUid(Object uid) {
    if (uid == null)
      throwMe(validateAttributesString,"Attribute validation failed for uid <${uid}>. uid can not be null.")
    if (!uid instanceof String)
      throwMe(validateAttributesString,"Attribute validation failed for uid <${uid}>. uid need to be a String object.")
    String tmpUid = (String)uid
    if (uid == null || tmpUid.length() < 2 || tmpUid.length() > 8)
      throwMe(validateAttributesString,"Attribute validation failed for uid <${tmpUid}>. uid need to be at least min 2 and max 8 chars in length.")
  }

  private static void validateEduPersonPrimaryAffiliation(Object eduPersonPrimaryAffiliation) {
    if (eduPersonPrimaryAffiliation == null)
      throwMe(validateAttributesString,"Attribute validation failed for eduPersonPrimaryAffiliation <${eduPersonPrimaryAffiliation}>. eduPersonPrimaryAffiliation can not be null.")
    if (!eduPersonPrimaryAffiliation instanceof String)
      throwMe(validateAttributesString,"Attribute validation failed for eduPersonPrimaryAffiliation <${eduPersonPrimaryAffiliation}>. eduPersonPrimaryAffiliation need to be a String object.")
    String tmpEduPersonPrimaryAffiliation = (String)eduPersonPrimaryAffiliation
    if (!affiliations.contains(tmpEduPersonPrimaryAffiliation.toLowerCase()))
      throwMe(validateAttributesString,"Attribute validation failed for eduPersonPrimaryAffiliation <${tmpEduPersonPrimaryAffiliation}>. eduPersonPrimaryAffiliation need to be one of [${affiliations.join(",")}].")
  }

  private static void validateDomain(Object domain) {
    if (domain == null)
      throwMe(validateAttributesString,"Attribute validation failed for domain <${domain}>. domain can not be null.")
    if (!domain instanceof String)
      throwMe(validateAttributesString,"Attribute validation failed for domain <${domain}>. domain need to be a String object.")
    String tmpDomain = (String)domain
    if (!tmpDomain ==~ /^([a-zA-Z0-9]{1}([a-zA-Z0-9\-]*[a-zA-Z0-9])*)(\.[a-zA-Z0-9]{1}([a-zA-Z0-9\-]*[a-zA-Z0-9])*)*(\.[a-zA-Z]{1}([a-zA-Z0-9\-]*[a-zA-Z0-9])*)\.?$/)
      throwMe(validateAttributesString,"Attribute validation failed for domain <${domain}>. domain need to be a valid FQDN.")
  }

  private static void validateNin(Object nin) {
    if (nin == null)
      throwMe(validateAttributesString,"Attribute validation failed for nin <${nin}>. nin can not be null.")
    if (!nin instanceof String)
      throwMe(validateAttributesString,"Attribute validation failed for nin <${nin}>. nin need to be a String object.")
    String tmpNin = (String)nin
    if (!(tmpNin ==~ /^[12]{1}[90]{1}[0-9]{6}[0-9]{4}$/))
      throwMe(validateAttributesString,"Attribute validation failed for nin <${tmpNin}>. nin need to be a valid 12 digit socialsecuritynumber.")
  }

  private static void validategivenName(Object givenName) {
    if (givenName == null)
      throwMe(validateAttributesString,"Attribute validation failed for givenName <${givenName}>. givenName can not be null.")
    if (!givenName instanceof String)
      throwMe(validateAttributesString,"Attribute validation failed for givenName <${givenName}>. givenName need to be a String object.")
    String tmpGivenName = (String)givenName
    if (tmpGivenName.length() < 2)
      throwMe(validateAttributesString,"Attribute validation failed for givenName <${tmpGivenName}>. givenName need to be at least 2 chars in length.")
  }

  private static void validateSn(Object sn) {
    if (sn == null)
      throwMe(validateAttributesString,"Attribute validation failed for sn <${sn}>. sn can not be null.")
    if (!sn instanceof String)
      throwMe(validateAttributesString,"Attribute validation failed for sn <${sn}>. sn need to be a String object.")
    String tmpSn = (String)sn
    if (tmpSn.length() < 2)
      throwMe(validateAttributesString,"Attribute validation failed for sn <${tmpSn}>. sn need to be at least 2 chars in length.")
  }

  private static void validateSvcSuPersonVO(Object svcPerson) {
    if (svcPerson == null)
      throwMe(validateAttributesString,"Attribute validation failed for SvcSuPersonVO object <${svcPerson}>. SvcSuPersonVO object can not be null.")
    if (!svcPerson instanceof SvcSuPersonVO){
      throwMe(validateAttributesString,"Attribute validation failed for SvcSuPersonVO object <${svcPerson}>. SvcSuPersonVO object need to be of class SvcSuPersonVO.")
    }
    SvcSuPersonVO tmpSP = (SvcSuPersonVO)svcPerson
  }

  private static void validateMailRoutingAddress(Object mailRoutingAddress) {
    if (mailRoutingAddress == null)
      throwMe(validateAttributesString,"Attribute validation failed for mailRoutingAddress <${mailRoutingAddress}>. mailRoutingAddress can not be null.")
    if (!mailRoutingAddress instanceof String)
      throwMe(validateAttributesString,"Attribute validation failed for mailRoutingAddress <${mailRoutingAddress}>. mailRoutingAddress need to be a String object.")
    String tmpMailRoutingAddress = (String)mailRoutingAddress
    if (!checkValidMailAddress(tmpMailRoutingAddress))
      throwMe(validateAttributesString,"Attribute validation failed for mailRoutingAddress <${tmpMailRoutingAddress}>. mailRoutingAddress need to be a valid email address.")
  }

  private static void throwMe(String function, String message) {
    throw new java.lang.IllegalArgumentException("${function} - ${message}!")
  }

  private static boolean checkValidMailAddress(String mailAddress) {
    def emailPattern = /[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})/
    if (!(mailAddress ==~ emailPattern)) {
      return false
    }
    return true
  }
}
