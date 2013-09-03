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

package se.su.it.svc.util

import groovy.util.logging.Slf4j
import org.apache.commons.collections.Predicate
import se.su.it.commons.ExecUtils
import se.su.it.commons.PrincipalUtils
import se.su.it.svc.commons.LdapAttributeValidator
import se.su.it.svc.commons.SvcUidPwd
import se.su.it.svc.ldap.PosixAccount
import se.su.it.svc.ldap.SuEnrollPerson
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.manager.Properties
import se.su.it.svc.query.SuPersonQuery

import java.util.regex.Matcher
import java.util.regex.Pattern

@Slf4j
class EnrollmentServiceUtils {

  /**
   * Path to create home directories in.
   */
  public static final String AFS_HOME_DIR_BASE = "/afs/su.se/home/"

  /**
   * Path to set as user shell
   */
  public static final String SHELL_PATH = "/usr/local/bin/bash"

  /**
   * Defalt GID for new users.
   */
  public static final String DEFAULT_USER_GID = "1200"

  /**
   * User to run enable-user script as.
   */
  public static final String SCRIPT_USER = "uadminw"

  /**
   * Path to 'enable-user' script
   */
  public static final String ENABLE_SCRIPT = "/local/sukat/libexec/enable-user.pl"

  /**
   * Path to 'run-token-script'
   */
  public static final String TOKEN_SCRIPT = "/local/scriptbox/bin/run-token-script.sh"

  /**
   * Enables the user through the 'enable-user.pl' script & saves the new data in SUKAT
   *
   * @param uid uid of the user to be enabled
   * @param password the password
   * @param person a object to set attributes on.
   * @return true the operation succeeds, false if it fails.
   */
  public static boolean enableUser(String uid, String password, PosixAccount person) {
    boolean error = false
    String uidNumber

    boolean skipCreate = Properties.instance.props.enrollment.skipCreate == "true"

    if (skipCreate) {
      log.warn "Skipping enable user since skipCreate is set to $skipCreate"
      uidNumber = "-1"
    } else {
      uidNumber = runEnableScript(uid, password)
      if (!uidNumber) {
        error = true
      }
    }

    /** End call Perlscript to init user in kdc, afs and unixshell */
    if (!error) {
      log.debug("enableUser - Perlscript success for uid<${uid}>")
      log.debug("enableUser - Writing posixAccount attributes to sukat for uid<${uid}>")

      person.objectClass.add("posixAccount")
      person.loginShell = SHELL_PATH
      person.homeDirectory = getHomeDirectoryPath(uid)
      person.uidNumber = uidNumber
      person.gidNumber = DEFAULT_USER_GID
      person.save()
    }

    return !error
  }

  /**
   * Run the script that enables the user in AFS & KDC
   *
   * @param uid the uid to enable
   * @param password the password
   * @return the uid of the enabled user, null if the operation fails.
   */
  public static String runEnableScript(String uid, String password) {
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
    } catch (ex) {
      log.error("enableUser - Error when enabling uid<${uid}> in KDC and/or AFS! Error: " + ex.message)
      log.error("           - posixAccount attributes will not be written to SUKAT!")
    }

    return uidNumber
  }

  /**
   * Generate a homeDirectory path string from uid
   *
   * @param uid the uid to base the home directory on.
   * @return homeDirectory absolute path or null if uid is null or empty
   */
  public static String getHomeDirectoryPath(String uid) {
    def invalid = LdapAttributeValidator.validateAttributes(uid: uid)
    invalid ? null : AFS_HOME_DIR_BASE + uid.charAt(0) + "/" + uid.charAt(1) + "/" + uid
  }

  /**
   * Find SuEnrollPerson by nin
   *
   * BEWARE!! Searching for ssn too!
   *
   * @param nin the nin to search for
   * @return SuEnrollPerson for the nin, or null if not found
   */
  static SuEnrollPerson findEnrollPerson(String nin) {
    SuEnrollPerson suEnrollPerson
    if (nin.length() == 10) {
      suEnrollPerson = SuPersonQuery.getSuEnrollPersonFromSsn(GldapoManager.LDAP_RW, nin)
    } else {
      suEnrollPerson = SuPersonQuery.getSuEnrollPersonFromNin(GldapoManager.LDAP_RW, nin)
      if (suEnrollPerson == null) { // Try to cut the 12 - digit ssn to 10
        suEnrollPerson = SuPersonQuery.getSuEnrollPersonFromSsn(GldapoManager.LDAP_RW, GeneralUtils.pnrToSsn(nin))
      }
    }
    return suEnrollPerson
  }

  /**
   * Set nin & and objectClass 'norEduPerson' on SuEnrollPerson
   *
   * @param nin the nin to use
   * @param suEnrollPerson the SuEnrollPerson
   */
  static void setNin(String nin, SuEnrollPerson suEnrollPerson) {
    if (nin?.length() == 12) {
      suEnrollPerson.objectClass.add("norEduPerson")
      suEnrollPerson.norEduPersonNIN = nin
    }
    suEnrollPerson.socialSecurityNumber = GeneralUtils.pnrToSsn(nin)
  }

  /**
   * Sets primary affiliation
   *
   * @param eduPersonPrimaryAffiliation the affiliation
   * @param suEnrollPerson the SuEnrollPerson
   */
  static void setPrimaryAffiliation(String eduPersonPrimaryAffiliation, SuPerson suPerson) {
    suPerson.eduPersonPrimaryAffiliation = eduPersonPrimaryAffiliation

    if (suPerson.eduPersonAffiliation != null) {
      if (!suPerson.eduPersonAffiliation.contains(eduPersonPrimaryAffiliation)) {
        suPerson.eduPersonAffiliation.add(eduPersonPrimaryAffiliation)
      }
    } else {
      suPerson.eduPersonAffiliation = [eduPersonPrimaryAffiliation]
    }
  }

  /**
   * Sets mail attributes & objectClass 'inetLocalMailRecipient' on SuEnrollPerson
   *
   * @param suEnrollPerson the SuEnrollPerson to set attributes on
   * @param domain the mail domain
   */
  static void setMailAttributes(SuEnrollPerson suEnrollPerson, String domain) {
    String myMail = suEnrollPerson.uid + "@" + domain

    suEnrollPerson.mail = [myMail]

    if (suEnrollPerson.mailLocalAddress) {
      if (!suEnrollPerson.mailLocalAddress.contains(myMail)) {
        suEnrollPerson.mailLocalAddress.add(myMail)
      }
    } else {
      suEnrollPerson.mailLocalAddress = [myMail]

      suEnrollPerson.objectClass.add("inetLocalMailRecipient")
    }
  }

  /**
   * Enroll an existing user
   *
   * @param nin the users nin
   * @param suEnrollPerson person to enroll
   * @param svcUidPwd user & password
   * @param eduPersonPrimaryAffiliation the primary affiliation to set
   * @param domain the domain
   * @param mailRoutingAddress the mailRoutingAddress
   */
  static void handleExistingUser(
          SuPerson suPerson,
          SvcUidPwd svcUidPwd,
          String eduPersonPrimaryAffiliation,
          String domain,
          String mailRoutingAddress) {
    log.debug("enrollUser - Now enabling uid <${suPerson.uid}>.")

    suPerson = new SuEnrollPerson(suPerson.properties)

    boolean enabledUser = enableUser(suPerson.uid, svcUidPwd.password, suPerson)

    if (!enabledUser) {
      log.error("enrollUser - enroll failed while excecuting perl scripts for uid <${suPerson.uid}>")
      throw new Exception("enrollUser - enroll failed in scripts.")
    }

    setPrimaryAffiliation(eduPersonPrimaryAffiliation, suPerson)
    setMailAttributes(suPerson, domain)

    if (mailRoutingAddress) {
      suPerson.mailRoutingAddress = mailRoutingAddress

      suPerson.objectClass.add("inetLocalMailRecipient")
    }

    SuPersonQuery.saveSuPerson(suPerson)
    log.info("enrollUser - User with uid <${suPerson.uid}> now enabled.")
  }

  /**
   * Enroll a new user
   *
   * @param nin
   * @param givenName
   * @param sn
   * @param svcUidPwd
   * @param eduPersonPrimaryAffiliation
   * @param domain
   * @param mailRoutingAddress
   */
  static void handleNewUser(String nin,
                            String givenName,
                            String sn,
                            SvcUidPwd svcUidPwd,
                            String eduPersonPrimaryAffiliation,
                            String domain,
                            String mailRoutingAddress) {
    log.debug("enrollUser - User with nin <${nin}> not found. Trying to create and enable user in sukat/afs/kerberos.")

    svcUidPwd.uid = generateUid(givenName, sn)

    SuEnrollPerson suCreateEnrollPerson =
      setupEnrollPerson(svcUidPwd, givenName, sn, eduPersonPrimaryAffiliation, domain, nin, mailRoutingAddress)
    SuPersonQuery.initSuEnrollPerson(GldapoManager.LDAP_RW, suCreateEnrollPerson)

    if (enableUser(suCreateEnrollPerson.uid, svcUidPwd.password, suCreateEnrollPerson)) {
      log.info("enrollUser - User with uid <${suCreateEnrollPerson.uid}> now enabled.")
    } else {
      log.error("enrollUser - enroll failed while excecuting perl scripts for uid <${suCreateEnrollPerson.uid}>")
      throw new Exception("enrollUser - enroll failed in scripts.")
    }
  }

  /**
   * Generate a uid based on the given name and surname
   *
   * @param givenName the given name
   * @param sn the surname
   * @return a uid
   */
  static String generateUid(String givenName, String sn) {
    def logger = log
    String uid = PrincipalUtils.suniqueUID(givenName, sn, new Predicate() {
      public boolean evaluate(Object object) {
        try {
          return SuPersonQuery.getSuPersonFromUID(GldapoManager.LDAP_RW, (String) object) == null;
        } catch (ex) {
          logger.error "Failed when getting SuPerson from GID", ex
          return false;
        }
      }
    })

    log.debug "Returning $uid for user with name $givenName $sn"
    return uid
  }

  /**
   * Set up a new SuEnrollPerson based on the incoming attributes.
   *
   * @param svcUidPwd
   * @param givenName
   * @param sn
   * @param eduPersonPrimaryAffiliation
   * @param domain
   * @param nin
   * @param mailRoutingAddress
   * @return
   */
  static SuEnrollPerson setupEnrollPerson(SvcUidPwd svcUidPwd,
                                          String givenName,
                                          String sn,
                                          String eduPersonPrimaryAffiliation,
                                          String domain,
                                          String nin,
                                          String mailRoutingAddress) {
    SuEnrollPerson suCreateEnrollPerson = new SuEnrollPerson()
    suCreateEnrollPerson.uid = svcUidPwd.uid
    suCreateEnrollPerson.cn = givenName + " " + sn
    suCreateEnrollPerson.sn = sn
    suCreateEnrollPerson.givenName = givenName
    suCreateEnrollPerson.displayName = givenName + " " + sn
    suCreateEnrollPerson.eduPersonPrimaryAffiliation = eduPersonPrimaryAffiliation
    suCreateEnrollPerson.eduPersonAffiliation = [eduPersonPrimaryAffiliation]
    suCreateEnrollPerson.mail = [svcUidPwd.uid + "@" + domain]
    suCreateEnrollPerson.mailLocalAddress = [svcUidPwd.uid + "@" + domain]

    if (mailRoutingAddress) {
      suCreateEnrollPerson.mailRoutingAddress = mailRoutingAddress
    }

    if (nin.length() == 12) {
      suCreateEnrollPerson.norEduPersonNIN = nin
      suCreateEnrollPerson.socialSecurityNumber = GeneralUtils.pnrToSsn(nin)
    } else {
      suCreateEnrollPerson.socialSecurityNumber = nin
    }

    suCreateEnrollPerson.eduPersonPrincipalName = GeneralUtils.uidToPrincipal(svcUidPwd.uid)
    suCreateEnrollPerson.objectClass = ["suPerson", "sSNObject", "norEduPerson", "eduPerson", "inetLocalMailRecipient", "inetOrgPerson", "organizationalPerson", "person", "top"]
    suCreateEnrollPerson.parent = AccountServiceUtils.domainToDN(domain)
    log.debug("createSuPerson - Writing initial sukat record to sukat for uid<${svcUidPwd.uid}>")
    return suCreateEnrollPerson
  }
}
