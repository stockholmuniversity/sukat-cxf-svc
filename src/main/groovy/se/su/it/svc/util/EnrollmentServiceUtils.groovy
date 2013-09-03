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
import se.su.it.commons.ExecUtils
import se.su.it.svc.commons.LdapAttributeValidator
import se.su.it.svc.ldap.PosixAccount
import se.su.it.svc.manager.Properties

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
}
