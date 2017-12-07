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

package se.su.it.svc.commons

import groovy.util.logging.Slf4j
import se.su.it.svc.ldap.SuPerson

@Slf4j
public class LdapAttributeValidator {

  private static final String validateAttributesString = "validateAttributes"

  public static String validateAttributes(Map<String, Object> map) {
    String error = null

    try {
      map.each { String attributeName, Object val ->
        switch (attributeName.toLowerCase()) {
          case "uid"                         : validateUid(val); break
          case "affiliation"                 : validateAffiliations(val); break
          case "edupersonprimaryaffiliation" : validateEduPersonPrimaryAffiliation(val); break
          case "domain"                      : validateDomain(val); break
          case "nin"                         : validateNin(val); break
          case "ssn"                         : validateSsn(val); break
          case "ssnornin"                    : validateSsnOrNin(val); break
          case "givenname"                   : validategivenName(val); break
          case "sn"                          : validateSn(val); break
          case "svcsuperson"                 : validateSvcSuPersonVO(val); break
          case "mailroutingaddress"          : validateMailRoutingAddress(val); break
          case "maillocaladdresses"          : validateMailLocalAddresses(val); break
          default:
            log.debug("${validateAttributesString} - Attribute <${attributeName}> dont have a validation role!")
            break
        }
      }
    } catch (x) {
      error = x.message
    }

    return error
  }

  private static void validateUid(Object uid) {
    if (uid == null)
      throwMe(validateAttributesString,"Attribute validation failed for uid <${uid}>. uid can not be null.")

    if (! (uid ==~ /^[a-z0-9]*$/))
      throwMe(validateAttributesString,"Attribute validation failed for uid <${uid}>. Only alphanumeric characters are allowed.")

    if (! (uid ==~ /^.{2,11}$/))
      throwMe(validateAttributesString,"Attribute validation failed for uid <${uid}>. uid:s are between 2 and 11 chars in length.")
  }

  private static void validateAffiliations(Object affiliations) {
    if (affiliations == null) {
      throwMe(validateAttributesString,"Attribute validation failed for affiliations <${affiliations}>. affiliations can not be null.")
    }

    if (!affiliations instanceof String[]) {
      throwMe(validateAttributesString,"Attribute validation failed for affiliations <${affiliations}>. affiliations need to be a String object.")
    }

    ((String[])affiliations).each { affiliation ->
      if (!SuPerson.AFFILIATIONS.contains(affiliation.toLowerCase())) {
        throwMe(validateAttributesString,"Attribute validation failed for affiliations <${tmpEduPersonPrimaryAffiliation}>. affiliations need to be one of [${SuPerson.AFFILIATIONS.join(",")}].")
      }
    }
  }

  private static void validateEduPersonPrimaryAffiliation(Object eduPersonPrimaryAffiliation) {
    if (eduPersonPrimaryAffiliation == null)
      throwMe(validateAttributesString,"Attribute validation failed for eduPersonPrimaryAffiliation <${eduPersonPrimaryAffiliation}>. eduPersonPrimaryAffiliation can not be null.")
    if (!eduPersonPrimaryAffiliation instanceof String)
      throwMe(validateAttributesString,"Attribute validation failed for eduPersonPrimaryAffiliation <${eduPersonPrimaryAffiliation}>. eduPersonPrimaryAffiliation need to be a String object.")
    String tmpEduPersonPrimaryAffiliation = (String)eduPersonPrimaryAffiliation
    if (!SuPerson.AFFILIATIONS.contains(tmpEduPersonPrimaryAffiliation.toLowerCase()))
      throwMe(validateAttributesString,"Attribute validation failed for eduPersonPrimaryAffiliation <${tmpEduPersonPrimaryAffiliation}>. eduPersonPrimaryAffiliation need to be one of [${SuPerson.AFFILIATIONS.join(",")}].")
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

  /**
   * Validate norEduPersonNIN according to https://confluence.it.su.se/confluence/x/IhIfAw
   *
   * @param nin the norEduPersonNIN to validate
   */
  private static void validateNin(Object nin) {
    if (nin == null)
      throwMe(validateAttributesString,"Attribute validation failed for nin <${nin}>. nin can not be null.")
    if (!nin instanceof String)
      throwMe(validateAttributesString,"Attribute validation failed for nin <${nin}>. nin need to be a String object.")
    if(! (nin ==~ /[0-9]{8}[A-Z0-9][0-9]{3}/) ) {
      throwMe(validateAttributesString,"Attribute validation failed for nin <${nin}>. nin need to be a 12 in length.")
    }
  }

    /**
     * Validate ssn according to https://confluence.it.su.se/confluence/x/EhIfAw
     *
     * @param ssn the socailSecurityNumber to validate.
     */
    private static void validateSsn(Object ssn)
    {
        if (ssn.length() != 10)
        {
            throwMe("Given ssn (${ssn}) is not 10 characters long.")
        }

        // Special accounts such as testaccount
        if (ssn ==~ /^00[2-9]\d00A\d\d\d$/)
        {
            throwMe("Given ssn (${ssn}) is for a specialaccount")
        }
        else
        {
            def year = ssn.substring(0, 2)
            if ((year ==~ /^\d\d$/) == false)
            {
                throwMe("Given ssn (${ssn}) contains an invalid year (${year}).")
            }

            def monthString = ssn.substring(2, 4)
            def month = monthString as int
            if (month < 01 || month > 12)
            {
                throwMe("Given ssn (${ssn}) contans an invalid month (${monthString}).")
            }

            def dayString = ssn.substring(4, 6)
            def day = dayString as int
            if ((day < 01 || day > 31) && (day < 61 || day > 91)) // 61 to 91 for samordningsnummer
            {
                throwMe("Given ssn (${ssn}) contains an invalid day (${dayString}).")
            }

            def letter = ssn.substring(6, 7)
            if (letter ==~ /^\d$/)
            {
                def birthno = ssn.substring(6, 9)
                if ((birthno ==~ /^\d\d\d$/) == false)
                {
                    throwMe("Given ssn (${ssn}) contains an invalid birthnumber (${birthno}).")
                }
            }
            else
            {
                if ((letter ==~ /^[APRSTUV]$/) == false)
                {
                    throwMe("The letter (${letter}) is not valid for interimspersonnummer or SUKAT-personnummer.")
                }

            }

            def checksum = ssn.substring(9) as int
            if (checksum != calculateSsnChecksum(ssn))
            {
                throwMe("Given ssn (${ssn}) contains an invalid checksum (${checksum}).")
            }
        }

    }

  /**
   * Validate according to socialSecurityNumber or norEduPersonNIN
   *
   * @param ssnOrNin the string to validate
   * @see LdapAttributeValidator#validateSsn(java.lang.Object)
   * @see LdapAttributeValidator#validateNin(java.lang.Object)
   */
  private static void validateSsnOrNin(Object ssnOrNin) {
    Throwable throwable = null

    try {
      validateSsn(ssnOrNin)
    } catch (ex) {
      throwable = ex
    }

    try {
      if( throwable ) {
        validateNin(ssnOrNin)
        throwable = null
      }
    } catch (ex) {
      throwable = ex
    }

    if (throwable) {
      throw throwable
    }
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

  private static void validateMailLocalAddresses(Object mailLocalAddresses) {
    if (mailLocalAddresses instanceof String[]) {
      for (mailLocalAddress in (String[]) mailLocalAddresses) {
        if (!checkValidMailAddress(mailLocalAddress)) {
          throwMe(validateAttributesString, "$mailLocalAddress is not a valid email address.")
        }
      }
    } else {
      throwMe(validateAttributesString,"Attribute validation failed for mailLocalAddresses <${mailLocalAddresses}>. " +
          "mailRoutingAddress needs to be a String array.")
    }
  }

    private static void throwMe(String message)
    {
        throw new IllegalArgumentException(message)
    }

  private static void throwMe(String function, String message) {
    throw new IllegalArgumentException("${function} - ${message}!")
  }

  private static boolean checkValidMailAddress(String mailAddress) {
    def emailPattern = /[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})/
    if (!(mailAddress ==~ emailPattern)) {
      return false
    }
    return true
  }

    /**
     * Calculate a nin or ssn checksum.
     *
     * @param ssn 10 or 12 character string
     */
    private static int calculateSsnChecksum(String ssn)
    {
        def letter = ssn.substring(6, 7)
        if (letter ==~ /^[A-Z]$/)
        {
            // Letters in interimspersonnummer and SUKAT-personnummer gets replaced by 1
            ssn = ssn.substring(0, 6) + "1" + ssn.substring(7)
        }

        int sum = 0;

        for (i in 0..8)
        {
            def digit = ssn.substring(i, i + 1) as int

            if (i % 2)
            {
                sum += digit * 1
            }
            else
            {
                def part = digit * 2

                if (part > 9)
                {
                    // Split 10, 11, ..., 18 into 1+0 1+1, ..., 1+8
                    // 2 * 9 makes maximum value 18
                    sum += 1
                    sum += part - 10
                }
                else
                {
                    sum += part
                }
            }
        }

        // In a sum such as 23 use only the last digit (3).
        while (sum > 9)
        {
            sum = sum - 10
        }

        def checksum = 0
        if (sum != 0)
        {
            checksum = 10 - sum
        }

        return checksum
    }
}
