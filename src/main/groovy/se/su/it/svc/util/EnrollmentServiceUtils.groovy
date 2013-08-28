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
import org.apache.log4j.Logger
import se.su.it.commons.ExecUtils
import se.su.it.svc.ldap.SuEnrollPerson
import se.su.it.svc.ldap.SuInitPerson
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
}
