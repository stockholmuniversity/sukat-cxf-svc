package se.su.it.svc.util

import org.apache.log4j.Logger
import se.su.it.commons.ExecUtils
import se.su.it.commons.PasswordUtils
import se.su.it.svc.ldap.SuInitPerson
import se.su.it.svc.query.SuPersonQuery

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2013-06-11
 * Time: 14:40
 * To change this template use File | Settings | File Templates.
 */
class EnrollmentServiceUtils {
  private static final Logger logger = Logger.getLogger(EnrollmentServiceUtils.class)

  public static boolean enableUser(String uid, String password, SuInitPerson person) {
    boolean error = false
    String uidNumber = ""
    def perlScript = ["--user", "uadminw", "/local/sukat/libexec/enable-user.pl", "--uid", uid, "--password", password, "--gidnumber", "1200"]
    try {
      logger.debug("enableUser - Running perlscript to create user in KDC and AFS for uid<${uid}>")
      def res = ExecUtils.exec("/local/scriptbox/bin/run-token-script.sh", perlScript.toArray(new String[perlScript.size()]))
      Pattern p = Pattern.compile("OK \\(uidnumber:(\\d+)\\)")
      Matcher m = p.matcher(res.trim())
      if (m.matches()) {
        uidNumber = m.group(1)
      } else {
        error = true
      }
    } catch (Exception e) {
      error = true;
      logger.error("enableUser - Error when enabling uid<${uid}> in KDC and/or AFS! Error: " + e.message)
      logger.error("           - posixAccount attributes will not be written to SUKAT!")
    }
    //End call Perlscript to init user in kdc, afs and unixshell
    if (!error) {
      logger.debug("enableUser - Perlscript success for uid<${uid}>")
      logger.debug("enableUser - Writing posixAccount attributes to sukat for uid<${uid}>")
      person.objectClass.add("posixAccount")
      person.loginShell = "/usr/local/bin/bash"
      person.homeDirectory = "/afs/su.se/home/" + uid.charAt(0) + "/" + uid.charAt(1) + "/" + uid
      person.uidNumber = uidNumber
      person.gidNumber = "1200"

      SuPersonQuery.saveSuInitPerson(person)
    }
    return !error
  }
}
