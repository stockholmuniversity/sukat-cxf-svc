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

package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter
import groovy.util.logging.Slf4j
import se.su.it.commons.ExecUtils
import se.su.it.svc.commons.LdapAttributeValidator
import se.su.it.svc.commons.SvcSuPersonVO
import se.su.it.svc.commons.SvcUidPwd
import se.su.it.svc.manager.Config
import se.su.it.svc.query.SuPersonQuery
import se.su.it.svc.util.AccountServiceUtils
import se.su.it.svc.util.GeneralUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

/** GLDAPO schema class for SU employees and students also used by web service. */

@Slf4j
class SuPerson implements Serializable {

  static final long serialVersionUID = -687991492884005033L

  /** Path to create home directories in. */
  public static final String AFS_HOME_DIR_BASE = "/afs/su.se/home/"

  /** Path to set as user shell */
  public static final String SHELL_PATH = "/usr/local/bin/bash"

  /** Default GID for new users. */
  public static final String DEFAULT_USER_GID = "1200"

  /** User to run enable-user script as. */
  public static final String SCRIPT_USER = "uadminw"

  /** Path to 'enable-user' script */
  public static final String ENABLE_SCRIPT = "/local/sukat/libexec/enable-user.pl"

  /** Path to 'run-token-script' */
  public static final String TOKEN_SCRIPT = "/local/scriptbox/bin/run-token-script.sh"

  public static enum Affilation {
    EMPLOYEE('employee', 40),
    STUDENT('student', 30),
    ALUMNI('alumni', 20),
    MEMBER('member', 10),
    OTHER('other', 0)

    private final String value
    private final int rank

    public Affilation(value, rank) {
      this.value = value
      this.rank = rank
    }

    public getValue() {
      return value
    }
  }

  static final List<String> AFFILIATIONS = Affilation.enumConstants*.value

  @GldapoSchemaFilter("(objectClass=suPerson)")
  @GldapoNamingAttribute
  String uid
  Set<String> objectClass
  String eduPersonPrimaryAffiliation
  Set<String> eduPersonAffiliation
  Set<String> eduPersonEntitlement
  String socialSecurityNumber
  String givenName
  String sn
  String cn
  String displayName
  Set<String> title
  Set<String> roomNumber
  Set<String> telephoneNumber
  String mobile
  Set<String> sukatPULAttributes //Adress (hem),Fax (hem),Hemsida (privat/hem),Mail (privat/hem),Mobil,Mobil (privat/hem),Stad (hem),Telefon (privat/hem)
  String labeledURI //hemsida
  String mail
  Set<String> mailLocalAddress
  String sukatLOAFromDate //Tjänstledighet börjar
  String sukatLOAToDate   //Tjänstledighet slutar
  Set<String> eduPersonOrgUnitDN
  String eduPersonPrimaryOrgUnitDN
  String registeredAddress
  String mailRoutingAddress
  String departmentNumber
  String homeMobilePhone
  String homePhone
  Set<String> homePostalAddress
  String homeLocalityName
  String homePostalCode
  String description
  String sukatComment
  String loginShell
  String homeDirectory
  String gidNumber
  String uidNumber

  /**
   * Create a SvcSuPersonVO filled with property values from this SuPerson
   *
   * @return a new SvcSuPersonVO
   */
  public SvcSuPersonVO createSvcSuPersonVO() {
    SvcSuPersonVO svcSuPersonVO = new SvcSuPersonVO()
    GeneralUtils.copyProperties(this, svcSuPersonVO)

    svcSuPersonVO.accountIsActive = (objectClass?.contains('posixAccount')) ?: false

    return svcSuPersonVO
  }

  /**
   * Update properties from a SvcSuPersonVO
   *
   * @param svcSuPersonVO the VO containing the properties
   */
  public void updateFromSvcSuPersonVO(SvcSuPersonVO svcSuPersonVO) {
    GeneralUtils.copyProperties(svcSuPersonVO, this)
  }

  /**
   * Set new mail, update mailLocalAddress
   *
   * @param mail the new mail addresses
   */
  public void setMail(String mail) {
    if (mail) {
      if (this.mailLocalAddress) {
        this.mailLocalAddress?.add(mail)
      } else {
        this.mailLocalAddress = [mail]
      }
    }

    this.mail = mail
  }

  /**
   * Set the mailLocalAddress & add 'inetLocalMailRecipient' objectClass
   *
   * @param mailLocalAddress the new mailLocalAddress
   */
  public void setMailLocalAddress(Set<String> mailLocalAddress) {
    this.mailLocalAddress = mailLocalAddress

    if (this.mailLocalAddress) {
      this.objectClass?.add("inetLocalMailRecipient")
    }
  }

  /**
   * Sets affiliations & calculates primary affiliation
   *
   * @param affiliations the new affiliations
   */
  private void updateAffiliations(String[] affiliations) throws IllegalArgumentException {
    log.debug "updateAffiliations: Received affiliations ${affiliations?.join(', ')}"

    if (affiliations == null) {
      // TODO: See if we should be able to reset
      throw new IllegalArgumentException("Affiliations can't be null.")
    }

    /* If an invalid affiliation is supplied we throw an IllegalArgumentException */
    if (!AFFILIATIONS.containsAll(affiliations)) {
      affiliations.every { affiliation ->
        if (!AFFILIATIONS.contains(affiliation)) {
          throw new IllegalArgumentException("Supplied affiliation $affiliation is invalid.")
        }
      }
    }

    objectClass.add("eduPerson")
    log.debug "updateAffiliations: affiliations set to ${affiliations?.join(', ')}"
    eduPersonAffiliation = affiliations

    String primary = null

    for (targetAffiliation in Affilation.enumConstants) {
      if (affiliations.contains(targetAffiliation.value)) {
        primary = targetAffiliation.value
        break
      }
    }

    log.debug "updateAffiliations: Primary affiliation set to $primary"
    eduPersonPrimaryAffiliation = primary
  }

  /**
   * Enables the user through the 'enable-user.pl' script & saves the new data in SUKAT
   *
   * @param uid uid of the user to be enabled
   * @param password the password
   * @return true the operation succeeds, false if it fails.
   */
  private boolean enable(String uid, String password) {
    boolean error = false
    boolean skipCreate = Config.instance.props.enrollment.skipCreate == "true"

    if (skipCreate) {
      log.warn "Skipping enable user since skipCreate is set to $skipCreate"
      this.uidNumber = "-1"
    } else {
      boolean enabled = this.runEnableScript(uid, password)
      if (!enabled) {
        error = true
      }
    }

    if (error) { return false }

    /** End call Perlscript to init user in kdc, afs and unixshell */
    log.debug("enableUser - Perlscript success for uid<${uid}>")
    log.debug("enableUser - Writing posixAccount attributes to sukat for uid<${uid}>")

    this.objectClass.add("posixAccount")
    this.loginShell = SHELL_PATH
    this.homeDirectory = fetchHomeDirectoryPath()
    this.gidNumber = DEFAULT_USER_GID
    SuPersonQuery.updateSuPerson(this)

    return true
  }

  /**
   * Run the script that enables the user in AFS & KDC
   *
   * @param uid the uid to enable
   * @param password the password
   * @return the uid of the enabled user, null if the operation fails.
   */
  private boolean runEnableScript(String uid, String password) {
    String uidNumber = null

    def perlScript = [
        "--user", SCRIPT_USER,
        ENABLE_SCRIPT,
        "--uid", uid,
        "--password", password,
        "--gidnumber", DEFAULT_USER_GID ]

    try {
      log.debug("enableUser - Running perlscript to create user in KDC and AFS for uid<${uid}>")
      def res = ExecUtils.exec(TOKEN_SCRIPT, perlScript.toArray(new String[perlScript.size()]))
      Pattern p = Pattern.compile("OK \\(uidnumber:(\\d+)\\)")
      Matcher m = p.matcher(res.trim())
      if (m.matches()) {
        uidNumber = m.group(1)
      }
      this.uidNumber = uidNumber
    } catch (ex) {
      log.error("enableUser - Error when enabling uid: <$uid> in KDC and/or AFS! " +
          "PosixAccount attributes will not be written to SUKAT!", ex)
      return false
    }
    return true
  }

  /**
   * Generate a homeDirectory path string from uid
   *
   * @return homeDirectory absolute path or null if uid is null or empty
   */
  private String fetchHomeDirectoryPath() {
    if (!uid) { return null }
    def invalid = LdapAttributeValidator.validateAttributes(uid: uid)
    invalid ? null : AFS_HOME_DIR_BASE + uid.charAt(0) + "/" + uid.charAt(1) + "/" + uid
  }

  /**
   * Activate an existing user
   *
   * @param suEnrollPerson person to enroll
   * @param svcUidPwd user & password
   * @param eduPersonPrimaryAffiliation the primary affiliation to set
   * @param domain the domain
   */
  public void activate(
      SvcUidPwd svcUidPwd,
      String[] affiliations,
      String domain) {
    log.debug("enrollUser - Now enabling uid <${uid}>.")

    boolean enabledUser = enable(uid, svcUidPwd.password)

    if (!enabledUser) {
      log.error("enrollUser - enroll failed while excecuting perl scripts for uid <$uid>")
      throw new RuntimeException("enrollUser - enroll failed in scripts.")
    }

    updateAffiliations(affiliations)
    mail = uid + "@" + domain

    SuPersonQuery.moveSuPerson(this, AccountServiceUtils.domainToDN(domain))
    SuPersonQuery.updateSuPerson(this)
    log.info("enrollUser - User with uid <$uid> now enabled.")
  }

}
