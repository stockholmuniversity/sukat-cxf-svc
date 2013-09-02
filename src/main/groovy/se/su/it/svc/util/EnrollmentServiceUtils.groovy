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
import se.su.it.svc.commons.SvcUidPwd
import se.su.it.svc.ldap.SuEnrollPerson
import se.su.it.svc.ldap.SuInitPerson
import se.su.it.svc.manager.GldapoManager
import se.su.it.svc.query.SuPersonQuery

import java.util.regex.Matcher
import java.util.regex.Pattern

@Slf4j
class EnrollmentServiceUtils {

  public static boolean enableUser(String uid, String password, Object person) {

    boolean error = false
    String uidNumber = ""

    boolean skipCreate = (se.su.it.svc.manager.Properties.instance.props.enrollment.skipCreate == "true")

    if (skipCreate) {
      log.warn "Skipping enable user since skipCreate is set to $skipCreate"
      uidNumber = "-1"
    } else {
      def perlScript = ["--user", "uadminw", "/local/sukat/libexec/enable-user.pl", "--uid", uid, "--password", password, "--gidnumber", "1200"]

      try {
        log.debug("enableUser - Running perlscript to create user in KDC and AFS for uid<${uid}>")
        def res = ExecUtils.exec("/local/scriptbox/bin/run-token-script.sh", perlScript.toArray(new String[perlScript.size()]))
        Pattern p = Pattern.compile("OK \\(uidnumber:(\\d+)\\)")
        Matcher m = p.matcher(res.trim())
        if (m.matches()) {
          uidNumber = m.group(1)
        } else {
          error = true
        }
      } catch (Exception e) {
        error = true
        log.error("enableUser - Error when enabling uid<${uid}> in KDC and/or AFS! Error: " + e.message)
        log.error("           - posixAccount attributes will not be written to SUKAT!")
      }
    }

    /** End call Perlscript to init user in kdc, afs and unixshell */
    if (!error) {
      log.debug("enableUser - Perlscript success for uid<${uid}>")
      log.debug("enableUser - Writing posixAccount attributes to sukat for uid<${uid}>")
      person.objectClass.add("posixAccount")
      person.loginShell = "/usr/local/bin/bash"
      person.homeDirectory = "/afs/su.se/home/" + uid.charAt(0) + "/" + uid.charAt(1) + "/" + uid
      person.uidNumber = uidNumber
      person.gidNumber = "1200"

      if (person instanceof SuInitPerson) {
        SuPersonQuery.saveSuInitPerson((SuInitPerson) person)
      } else if (person instanceof SuEnrollPerson) {
        SuPersonQuery.saveSuEnrollPerson((SuEnrollPerson) person)
      } else {
        log.error("enableUser - Could not figure out wich objectClass to use. Sukat posix attributes write failed")
        error = true
      }
    }

    return !error
  }

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

  static void setNin(String nin, SuEnrollPerson suEnrollPerson) {
    if (nin?.length() == 12) {
      suEnrollPerson.objectClass.add("norEduPerson")
      suEnrollPerson.norEduPersonNIN = nin
    }
    suEnrollPerson.socialSecurityNumber = GeneralUtils.pnrToSsn(nin)
  }

  static void setPrimaryAffiliation(String eduPersonPrimaryAffiliation, SuEnrollPerson suEnrollPerson) {
    suEnrollPerson.eduPersonPrimaryAffiliation = eduPersonPrimaryAffiliation

    if (suEnrollPerson.eduPersonAffiliation != null) {
      if (!suEnrollPerson.eduPersonAffiliation.contains(eduPersonPrimaryAffiliation)) {
        suEnrollPerson.eduPersonAffiliation.add(eduPersonPrimaryAffiliation)
      }
    } else {
      suEnrollPerson.eduPersonAffiliation = [eduPersonPrimaryAffiliation]
    }
  }

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

  static void handleExistingUser(String nin,
                                 SuEnrollPerson suEnrollPerson,
                                 SvcUidPwd svcUidPwd,
                                 String eduPersonPrimaryAffiliation,
                                 String domain,
                                 String mailRoutingAddress) {
    log.debug("enrollUser - User with nin <${nin}> found. Now enabling uid <${suEnrollPerson.uid}>.")

    boolean enabledUser = enableUser(suEnrollPerson.uid, svcUidPwd.password, suEnrollPerson)

    if (!enabledUser) {
      log.error("enrollUser - enroll failed while excecuting perl scripts for uid <${suEnrollPerson.uid}>")
      throw new Exception("enrollUser - enroll failed in scripts.")
    }

    setNin(nin, suEnrollPerson)
    setPrimaryAffiliation(eduPersonPrimaryAffiliation, suEnrollPerson)
    setMailAttributes(suEnrollPerson, domain)

    if (mailRoutingAddress) {
      suEnrollPerson.mailRoutingAddress = mailRoutingAddress

      suEnrollPerson.objectClass.add("inetLocalMailRecipient")
    }

    SuPersonQuery.saveSuEnrollPerson(suEnrollPerson)
    svcUidPwd.uid = suEnrollPerson.uid
    log.info("enrollUser - User with uid <${suEnrollPerson.uid}> now enabled.")
  }

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

    suCreateEnrollPerson.eduPersonPrincipalName = svcUidPwd.uid + "@su.se"
    suCreateEnrollPerson.objectClass = ["suPerson", "sSNObject", "norEduPerson", "eduPerson", "inetLocalMailRecipient", "inetOrgPerson", "organizationalPerson", "person", "top"]
    suCreateEnrollPerson.parent = AccountServiceUtils.domainToDN(domain)
    log.debug("createSuPerson - Writing initial sukat record to sukat for uid<${svcUidPwd.uid}>")
    return suCreateEnrollPerson
  }
}
